package simulation.cache.dataCache;

import simulation.block.dataBlock.DataBlock;
import simulation.cache.cacheStats.CacheStats;
import simulation.clock.Clock;
import simulation.directory.directory.Directory;
import simulation.sharedMemory.SharedMemory;
import simulation.states.BlockState;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class DataCache {

    private Semaphore memoryBus;

    private Semaphore myLock;

    private ArrayList<DataCache> localCaches;

    private ArrayList<DataCache> remoteCaches;

    private Semaphore interConnectionBus;

    private SharedMemory sharedMemory;

    private Directory localDirectory;

    private Directory remoteDirectory;

    private DataBlock[] cache;

    private int usedCycles;

    public DataCache(Semaphore myLock) {
        this.myLock = myLock;
        this.localCaches = new ArrayList<>();
        this.remoteCaches = new ArrayList<>();
        this.cache = new DataBlock[4];
        for (int i = 0; i < 4; i++) {
            this.cache[i] = new DataBlock(-1);
        }
        this.usedCycles = 0;
    }

    public Integer getDataRequired(int dataAddress){
        int blockAssigned = dataAddress / 16;
        int positionCache = blockAssigned % 4;
        // Check for miss
        if(cache[positionCache].getNumBlock() != blockAssigned || cache[positionCache].getState() == BlockState.INVALID) {
            Integer victimResult = modifyVictim(blockAssigned, positionCache);
            if (victimResult == null) {
                return null;
            }
            // Define which data cache we are working, as well as it's directory.
            CacheStats cacheStats = getCacheStats(blockAssigned);
            // If the block is remote to our processor, we need the interconnection bus.
            // Since we needed to duplicate our code, we had to make a support method.
            if(cacheStats.isRemote()) {
                if(interConnectionBus.tryAcquire()) {
                    try {
                        return getDataRequiredAux(cacheStats, positionCache, dataAddress);
                    } finally {
                        // release interconnection bus.
                        interConnectionBus.release();
                        Clock.executeBarrier();
                        usedCycles++;
                    }
                } else {
                    return null;
                }
            } else {
                return getDataRequiredAux(cacheStats, positionCache, dataAddress);
            }
        } else {
            int wordPosition = (dataAddress / 4) % 4;
            return cache[positionCache].getData()[wordPosition];
        }
    }

    /**
     * Support method for "getDataRequired". Helps us avoid duplicated code.
     * @param cacheStats Class we all the information regarding the next steps to take.
     * @param positionCache Position of the block we are modifying in our cach.
     * @param dataAddress Block's address in memory.
     * @return If null failure, else success.
     */
    private Integer getDataRequiredAux(CacheStats cacheStats, int positionCache, int dataAddress) {
        if(cacheStats.getDirectoryLock().tryAcquire()){
            try {
                for (int i = 0; i < cacheStats.getDirectoryCycles(); i++) {
                    Clock.executeBarrier();
                    usedCycles++;
                }
                if(cacheStats.getBlockDirectory().findState(dataAddress) != BlockState.MODIFIED) {
                    if(memoryBus.tryAcquire()) {
                        try {
                            for (int i = 0; i < cacheStats.getMemoryCycles(); i++) {
                                Clock.executeBarrier();
                                usedCycles++;
                            }
                            cache[positionCache] = sharedMemory.getDataBlock(dataAddress);
                            cache[positionCache].setState(BlockState.SHARED);
                            // Check if uncached. Can't be modified, because we already checked that.
                            if (cacheStats.getBlockDirectory().findState(dataAddress) == BlockState.UNCACHED) {
                                cacheStats.getBlockDirectory().changeCacheState(dataAddress,this, true, BlockState.SHARED);
                            } else {
                                cacheStats.getBlockDirectory().setSpecificCacheState(dataAddress, this, true);
                            }
                            int wordPosition = (dataAddress / 4) % 4;
                            return cache[positionCache].getData()[wordPosition];
                        } finally {
                            memoryBus.release();
                            Clock.executeBarrier();
                            usedCycles++;
                        }
                    } else {
                        return null;
                    }
                } else {
                    if(memoryBus.tryAcquire()) {
                        try {
                            for (int i = 0; i < cacheStats.getMemoryCycles(); i++) {
                                Clock.executeBarrier();
                                usedCycles++;
                            }
                            // Check which cache is modified.
                            // We will only have one, since we can only have one modified cache at a time.
                            ArrayList<DataCache> caches = cacheStats.getBlockDirectory().getValidCaches(dataAddress);
                            DataCache modifiedCache = null;
                            int cacheCycles = 0;
                            for (int i = 0; i < localCaches.size(); i++) {
                                if(caches.get(0) == localCaches.get(i)) {
                                    modifiedCache = localCaches.get(i);
                                    cacheCycles = 1;
                                }
                            }
                            // If the cache is not a local cache, then we look into the remote cache.
                            if(modifiedCache != null) {
                                for (int i = 0; i < remoteCaches.size(); i++) {
                                    if(caches.get(0) == remoteCaches.get(i)) {
                                        modifiedCache = remoteCaches.get(i);
                                        // If remote cache, then we need 5 cycles to reach it.
                                        cacheCycles = 5;
                                    }
                                }
                            }
                            if(modifiedCache.getMyLock().tryAcquire()) {
                                try {
                                    for (int i = 0; i < cacheCycles; i++) {
                                        Clock.executeBarrier();
                                        usedCycles++;
                                    }
                                    sharedMemory.saveDataBlock(modifiedCache.getCache()[positionCache]);
                                    modifiedCache.getCache()[positionCache].setState(BlockState.SHARED);
                                    cacheStats.getBlockDirectory().changeCacheState(dataAddress,
                                            modifiedCache,
                                            true,
                                            BlockState.SHARED);
                                    cache[positionCache] = sharedMemory.getDataBlock(dataAddress);
                                    cache[positionCache].setState(BlockState.SHARED);
                                    cacheStats.getBlockDirectory().setSpecificCacheState(dataAddress,this, true);
                                } finally {
                                    modifiedCache.getMyLock().release();
                                    Clock.executeBarrier();
                                    usedCycles++;
                                }
                            } else {
                                return null;
                            }
                        } finally {
                            // Release memory.
                            memoryBus.release();
                            Clock.executeBarrier();
                            usedCycles++;
                        }
                    }
                }
            } finally {
                // Release directory.
                cacheStats.getDirectoryLock().release();
                Clock.executeBarrier();
                usedCycles++;
            }
        } else {
            return null;
        }
        int wordPosition = (dataAddress / 4) % 4;
        return cache[positionCache].getData()[wordPosition];
    }

    private Integer modifyVictim(int blockAssigned, int positionCache) {
        // Check if victim block is modified or shared. Otherwise, we won't need to change anything.
        if(cache[positionCache].getState() == BlockState.MODIFIED || cache[positionCache].getState() == BlockState.SHARED){
            // Define which data cache we are working, as well as it's directory and it's memory address.
            // Given that we each cache block knows it's block number, we can find it's memory address.
            int dataAddress = cache[positionCache].getNumBlock()*16;
            CacheStats cacheStats = getCacheStats(cache[positionCache].getNumBlock());
            if(cacheStats.isRemote()) {
                if(interConnectionBus.tryAcquire()) {
                    try {
                        return modifyVictimState(cacheStats, blockAssigned, positionCache, dataAddress);
                    } finally {
                        interConnectionBus.release();
                        Clock.executeBarrier();
                        usedCycles++;
                    }
                }
            }
            return modifyVictimState(cacheStats, blockAssigned, positionCache, dataAddress);
        }
        return 1;
    }

    private Integer modifyVictimState(CacheStats cacheStats, int blockAssigned, int positionCache, int dataAddress) {
        if(cacheStats.getDirectoryLock().tryAcquire()) {
            try {
                for (int i = 0; i < cacheStats.getDirectoryCycles(); i++) {
                    Clock.executeBarrier();
                    usedCycles++;
                }
                // Check if victim has been modified
                if (cache[positionCache].getState() == BlockState.MODIFIED) {
                    if (memoryBus.tryAcquire()) {
                        try {
                            for (int i = 0; i < cacheStats.getMemoryCycles(); i++) {
                                Clock.executeBarrier();
                                usedCycles++;
                            }
                            sharedMemory.saveDataBlock(cache[positionCache]);
                            // Block state is modified in the directory.
                            // Set to uncached, because we know for sure that no one else has it, since
                            // it's last state was modified.
                            cacheStats.getBlockDirectory().setCacheState(dataAddress, false,
                                    false, false, BlockState.UNCACHED);
                            // Now we change it to invalid in our cache. This prevents problems if we can't acquire more resources.
                            cache[positionCache].setState(BlockState.INVALID);
                        } /* Releasing memory bus */ finally {
                            memoryBus.release();
                            Clock.executeBarrier();
                            usedCycles++;
                        }
                    } else {
                        return null;
                    }
                } else {
                    // In case that our block is shared by more than one cache, we need to set the
                    // state for this cache as false, otherwise we changed to uncached.
                    // Also, if only one of the caches have it on shared, then we can simply put it in uncached.
                    if(cacheStats.getBlockDirectory().countStates(dataAddress) > 1 && cacheStats.getBlockDirectory().findState(dataAddress) == BlockState.SHARED) {
                        cacheStats.getBlockDirectory().setSpecificCacheState(dataAddress,this,false);
                    } else {
                        cacheStats.getBlockDirectory().setCacheState(dataAddress, false,
                                false, false, BlockState.UNCACHED);
                    }
                    // In case we can't continue because we are lacking resources we need to make
                    // sure that we changed our cache state.
                    cache[positionCache].setState(BlockState.INVALID);
                }
            } /* Releasing victim directory */ finally {
                cacheStats.getDirectoryLock().release();
                Clock.executeBarrier();
                usedCycles++;
            }
        }
        else {
            return null;
        }
        return 1;
    }

    private CacheStats getCacheStats(int blockAssigned) {
        if((localCaches.size() == 1 && blockAssigned < 16) || (localCaches.size() == 0 && blockAssigned >= 16)){
            return new CacheStats(localDirectory, localDirectory.getDirectoryLock(), 16, 1, false);
        } else {
            return new CacheStats(remoteDirectory, remoteDirectory.getDirectoryLock(), 40, 5, true);
        }
    }

    public int getUsedCyclesOfLastRead(){
        int usedCycles = this.usedCycles;
        this.usedCycles = 0;
        return usedCycles;
    }

    public DataBlock[] getCache() {
        return cache;
    }

    public Semaphore getMyLock() {
        return myLock;
    }

    public void setMemoryBus(Semaphore memoryBus) {
        this.memoryBus = memoryBus;
    }

    public void setInterConnectionBus(Semaphore interConnectionBus) {
        this.interConnectionBus = interConnectionBus;
    }

    public void setSharedMemory(SharedMemory sharedMemory) {
        this.sharedMemory = sharedMemory;
    }

    public void setLocalDirectory(Directory localDirectory) {
        this.localDirectory = localDirectory;
    }

    public void setRemoteDirectory(Directory remoteDirectory) {
        this.remoteDirectory = remoteDirectory;
    }

    public void addLocalCaches(DataCache localCache) {
        this.localCaches.add(localCache);
    }

    public void addRemoteCaches(DataCache remoteCache) {
        this.remoteCaches.add(remoteCache);
    }
}

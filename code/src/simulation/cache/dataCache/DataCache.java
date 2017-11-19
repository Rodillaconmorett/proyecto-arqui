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

    public Integer getDataRequired(int dataAddress, String coreName){
        int blockAssigned = dataAddress / 16;
        int positionCache = blockAssigned % 4;
        // Check for miss
        if(cache[positionCache].getNumBlock() != blockAssigned || cache[positionCache].getState() == BlockState.INVALID) {
            Integer victimResult = modifyVictim(blockAssigned,positionCache);
            if (victimResult == null) {
                return null;
            }
            // Define which data cache we are working, as well as it's directory.
            CacheStats cacheStats = getCacheStats(blockAssigned);
            if(cacheStats.getDirectoryLock().tryAcquire()){
                try {
                    for (int i = 0; i < cacheStats.getDirectoryCycles(); i++) {
                        Clock.executeBarrier();
                        usedCycles++;
                    }
                    if(cacheStats.getBlockDirectory().findState(dataAddress) != BlockState.MODIFIED){
                        if(memoryBus.tryAcquire()){
                            try {
                                for (int i = 0; i < cacheStats.getMemoryCycles(); i++) {
                                    Clock.executeBarrier();
                                    usedCycles++;
                                }
                                cache[positionCache] = sharedMemory.getDataBlock(dataAddress);
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
                        // TODO
                    }
                } finally {
                    cacheStats.getDirectoryLock().release();
                    Clock.executeBarrier();
                    usedCycles++;
                }
            } else {
                return null;
            }
        } else {
            int wordPosition = (dataAddress / 4) % 4;
            return cache[positionCache].getData()[wordPosition];
        }
        return null;
    }

    private Integer modifyVictim(int blockAssigned, int positionCache) {
        // Check if victim block is modified or shared. Otherwise, we won't need to change anything.
        if(cache[positionCache].getState() == BlockState.MODIFIED || cache[positionCache].getState() == BlockState.SHARED){
            // Define which data cache we are working, as well as it's directory and it's memory address.
            // Given that we each cache block knows it's block number, we can find it's memory address.
            int dataAddress = cache[positionCache].getNumBlock()*16;
            CacheStats cacheStats = getCacheStats(cache[positionCache].getNumBlock());
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
                                //localDirectory.modificar y el bloque queda en u
                                cacheStats.getBlockDirectory().setCacheState(dataAddress, false,
                                        false, false, BlockState.UNCACHED);
                                cache[blockAssigned].setState(BlockState.INVALID);
                            } /* Releasing memory bus */ finally {
                                memoryBus.release();
                                Clock.executeBarrier();
                                usedCycles++;
                            }
                        } else {
                            cacheStats.getDirectoryLock().release();
                            Clock.executeBarrier();
                            usedCycles++;
                            return null;
                        }
                    } else {
                        // In case that our block is shared by more than one cache, we need to set the
                        // state for this cache as false, otherwise we changed to uncached.
                        if(cacheStats.getBlockDirectory().countStates(dataAddress) > 1 && cacheStats.getBlockDirectory().findState(dataAddress) == BlockState.SHARED) {
                            cacheStats.getBlockDirectory().setSpecificCacheState(dataAddress,this,false);
                        } else {
                            cacheStats.getBlockDirectory().setCacheState(dataAddress, false,
                                    false, false, BlockState.UNCACHED);
                        }
                        // In case we can't continue because we are lacking resources we need to make
                        // sure that we changed our cache state.
                        cache[blockAssigned].setState(BlockState.INVALID);
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
        }
        return 1;
    }

    private CacheStats getCacheStats(int blockAssigned) {
        if((localCaches.size() == 2 && blockAssigned < 16) || (localCaches.size() == 1 && blockAssigned >= 16)){
            return new CacheStats(localDirectory, localDirectory.getDirectoryLock(), 16, 1);
        } else {
            return new CacheStats(remoteDirectory, remoteDirectory.getDirectoryLock(), 40, 5);
        }
    }

    public int getUsedCyclesOfLastRead(){
        int usedCycles = this.usedCycles;
        this.usedCycles = 0;
        return usedCycles;
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

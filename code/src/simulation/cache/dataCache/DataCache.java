package simulation.cache.dataCache;

import simulation.block.dataBlock.DataBlock;
import simulation.cache.cacheStats.CacheStats;
import simulation.clock.Clock;
import simulation.directory.directory.Directory;
import simulation.sharedMemory.SharedMemory;
import simulation.states.BlockState;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 * Object representing our data caches.
 */
public class DataCache {
    /// Memory bus.
    private Semaphore memoryBus;
    /// My cache lock.
    private Semaphore myLock;
    /// References to our to th other local caches.
    private ArrayList<DataCache> localCaches;
    /// References to th other remote caches.
    private ArrayList<DataCache> remoteCaches;
    /// Interconnection bus.
    private Semaphore interConnectionBus;
    /// Shared memory managed by the data cache.
    private SharedMemory sharedMemory;
    /// Local directory.
    private Directory localDirectory;
    /// Remote directory.
    private Directory remoteDirectory;
    /// Data inside our cache.
    private DataBlock[] cache;
    /// Number of cycles used in the last fetch.
    private int usedCycles;

    /**
     * Default constructor.
     * @param myLock Our cache lock.
     */
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

    /**
     * Given an address, returns it's data value.
     * @param dataAddress
     * @return
     */
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
                            if(modifiedCache == null) {
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

    /**
     * Modify a victim block in either it's directory or/and memory.
     * @param blockAssigned Block number.
     * @param positionCache Position in cache.
     * @return Return if it was possible or not to modified.
     */
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

    /**
     * Support method of our modifyVictim.
     * @param cacheStats Cache information needed to fetch an address.
     * @param blockAssigned Number of the block required.
     * @param positionCache Position in cache of the block.
     * @param dataAddress Address needed.
     * @return Return either 1 if success or null if failure.
     */
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

    /**
     * Stored data in the base + offset position in memory.
     * @param data Data to be stored.
     * @param base Base address.
     * @param offset Offset.
     * @return Return false if failed to finish or success if finished.
     */
    public boolean storeDate(int data, int base, int offset) {
        // First, we must calculate the address.
        int dataAddress = base + offset;
        // Calculate block assigned.
        int blockAssigned = dataAddress/16;
        // Position in cache.
        int positionCache = blockAssigned % 4;
        // Word position.
        int wordPosition = (dataAddress/4) % 4;
        // Define which data cache we are working, as well as it's directory.
        CacheStats cacheStats = getCacheStats(blockAssigned);
        // Is it a miss?
        if(cache[positionCache].getNumBlock() != blockAssigned || cache[positionCache].getState() == BlockState.INVALID) {
            Integer victimResult = modifyVictim(blockAssigned, positionCache);
            if (victimResult == null) {
                return false;
            }
            // Try to lock the interconnection bus.
            if(cacheStats.isRemote()) {
                if(interConnectionBus.tryAcquire()) {
                    try {
                        return storeDateAuxMiss(cacheStats, positionCache, dataAddress, wordPosition, data);
                    } finally {
                        // release interconnection bus.
                        interConnectionBus.release();
                        Clock.executeBarrier();
                        usedCycles++;
                    }
                } else {
                    return false;
                }
            } else {
                return storeDateAuxMiss(cacheStats, positionCache, dataAddress, wordPosition, data);
            }
        } /* Es un hit */ else {
            // Ask if the block is modified.
            if( cache[positionCache].getState() == BlockState.MODIFIED) {
                // We stored our data.
                cache[positionCache].getData()[wordPosition] = data;
                return true;
            } else {
                // Try to lock the interconnection bus.
                if(cacheStats.isRemote()) {
                        if(interConnectionBus.tryAcquire()) {
                            try {
                                return storeDateAuxHit(cacheStats, positionCache, dataAddress, wordPosition, data);
                            } finally {
                                // release interconnection bus.
                                interConnectionBus.release();
                                Clock.executeBarrier();
                                usedCycles++;
                            }
                        } else {
                            return false;
                        }
                } else {
                    return storeDateAuxHit(cacheStats, positionCache, dataAddress, wordPosition, data);
                }
            }
        }
    }

    /**
     * Support method that help us implement a store hit.
     * @param cacheStats Information regarding locks and cycles.
     * @param positionCache Position in our cache where we store our data.
     * @param dataAddress Address of the block we want to modified in our memory.
     * @param wordPosition Position of the word to store.
     * @param data Value to store.
     * @return True if success, else returns false.
     */
    public boolean storeDateAuxHit(CacheStats cacheStats, int positionCache, int dataAddress, int wordPosition, int data) {
        if(cacheStats.getDirectoryLock().tryAcquire()) {
            try {
                // Directory cycles to fetch directory.
                for (int i = 0; i < cacheStats.getDirectoryCycles(); i++) {
                    Clock.executeBarrier();
                    usedCycles++;
                }
                // Check if we are the only ones with the cache.
                if(cacheStats.getBlockDirectory().countStates(dataAddress) > 1) {
                    ArrayList<DataCache> caches = cacheStats.getBlockDirectory().getValidCaches(dataAddress);
                    for (int i = 0; i < caches.size(); i++) {
                        // Number of cycles needed to fetch cache.
                        int cacheCycles = 1;
                        for (int j = 0; j < remoteCaches.size(); j++) {
                            if(caches.get(i) == remoteCaches.get(j)) {
                                cacheCycles = 5;
                            }
                        }
                        // Try to fetch the cache that needs to be modified.
                        if(caches.get(i).getMyLock().tryAcquire()) {
                            try {
                                for (int j = 0; j < cacheCycles; j++) {
                                    Clock.executeBarrier();
                                    usedCycles++;
                                }
                                caches.get(i).cache[positionCache].setState(BlockState.INVALID);
                                cacheStats.getBlockDirectory().setSpecificCacheState(dataAddress, caches.get(i), false);
                            } finally {
                                caches.get(i).getMyLock().release();
                                Clock.executeBarrier();
                                usedCycles++;
                            }
                        } else {
                            return false;
                        }
                    }
                    // If all caches were set invalid, then we can finally set our cache to modified.
                    cache[positionCache].getData()[wordPosition] = data;
                    cache[positionCache].setState(BlockState.MODIFIED);
                    cacheStats.getBlockDirectory().changeCacheState(dataAddress, this, true, BlockState.MODIFIED);
                    return true;
                } else {
                    // Saves data and modify directory entry.
                    cache[positionCache].getData()[wordPosition] = data;
                    cache[positionCache].setState(BlockState.MODIFIED);
                    cacheStats.getBlockDirectory().changeCacheState(dataAddress, this, true, BlockState.MODIFIED);
                    return true;
                }
            } finally {
                // Release directory.
                cacheStats.getDirectoryLock().release();
                Clock.executeBarrier();
                usedCycles++;
            }
        } else {
            return false;
        }
    }

    /**
     * Support method that help us implement a store miss.
     * @param cacheStats Information regarding locks and cycles.
     * @param positionCache Position in our cache where we store our data.
     * @param dataAddress Address of the block we want to modified in our memory.
     * @param wordPosition Position of the word to store.
     * @param data Value to store.
     * @return True if success, else returns false.
     */
    public boolean storeDateAuxMiss(CacheStats cacheStats, int positionCache, int dataAddress, int wordPosition, int data) {
        if(cacheStats.getDirectoryLock().tryAcquire()) {
            try {
                // Directory cycles to fetch directory.
                for (int i = 0; i < cacheStats.getDirectoryCycles(); i++) {
                    Clock.executeBarrier();
                    usedCycles++;
                }
                if(cacheStats.getBlockDirectory().findState(dataAddress) == BlockState.MODIFIED) {
                    ArrayList<DataCache> caches = cacheStats.getBlockDirectory().getValidCaches(dataAddress);
                    for (int i = 0; i < caches.size(); i++) {
                        // Number of cycles needed to fetch cache.
                        int cacheCycles = 1;
                        for (int j = 0; j < remoteCaches.size(); j++) {
                            if(caches.get(i) == remoteCaches.get(j)) {
                                cacheCycles = 5;
                            }
                        }
                        // Try to fetch the cache that needs to be modified.
                        if(caches.get(i).getMyLock().tryAcquire()) {
                            try {
                                for (int j = 0; j < cacheCycles; j++) {
                                    Clock.executeBarrier();
                                    usedCycles++;
                                }
                                // Now, we need to fetch our memory.
                                if(memoryBus.tryAcquire()) {
                                    try {
                                        for (int j = 0; j < cacheStats.getMemoryCycles(); j++) {
                                            Clock.executeBarrier();
                                            usedCycles++;
                                        }
                                        // Save to memory and then set as invalid.
                                        sharedMemory.saveDataBlock(caches.get(i).getCache()[positionCache]);
                                        caches.get(i).getCache()[positionCache].setState(BlockState.INVALID);
                                        cacheStats.getBlockDirectory().setCacheState(dataAddress, false, false, false, BlockState.UNCACHED);
                                        // Save what we had in the modified in cache in our cache.
                                        cache[positionCache] = caches.get(i).getCache()[positionCache];
                                    } finally {
                                        // Release our memory bus.
                                        memoryBus.release();
                                        Clock.executeBarrier();
                                        usedCycles++;
                                    }
                                }
                            } finally {
                                caches.get(i).getMyLock().release();
                                Clock.executeBarrier();
                                usedCycles++;
                            }
                        } else {
                            return false;
                        }
                    }
                    // Save data and change directory.
                    cache[positionCache].getData()[wordPosition] = data;
                    cache[positionCache].setState(BlockState.MODIFIED);
                    cacheStats.getBlockDirectory().changeCacheState(dataAddress, this, true, BlockState.MODIFIED);
                    return true;
                } else if (cacheStats.getBlockDirectory().findState(dataAddress) == BlockState.SHARED) {
                    ArrayList<DataCache> caches = cacheStats.getBlockDirectory().getValidCaches(dataAddress);
                    for (int i = 0; i < caches.size(); i++) {
                        // Number of cycles needed to fetch cache.
                        int cacheCycles = 1;
                        for (int j = 0; j < remoteCaches.size(); j++) {
                            if (caches.get(i) == remoteCaches.get(j)) {
                                cacheCycles = 5;
                            }
                        }
                        // Try to fetch the cache that needs to be modified.
                        if (caches.get(i).getMyLock().tryAcquire()) {
                            try {
                                for (int j = 0; j < cacheCycles; j++) {
                                    Clock.executeBarrier();
                                    usedCycles++;
                                }
                                // Need to put all other caches in invalid.
                                caches.get(i).cache[positionCache].setState(BlockState.INVALID);
                                cacheStats.getBlockDirectory().setSpecificCacheState(dataAddress, caches.get(i), false);
                            } finally {
                                caches.get(i).getMyLock().release();
                                Clock.executeBarrier();
                                usedCycles++;
                            }
                        } else {
                            return false;
                        }
                    }
                    cacheStats.getBlockDirectory().setCacheState(dataAddress, false, false, false, BlockState.UNCACHED);
                    // Now, we need to fetch our memory.
                    if(memoryBus.tryAcquire()) {
                        try {
                            for (int j = 0; j < cacheStats.getMemoryCycles(); j++) {
                                Clock.executeBarrier();
                                usedCycles++;
                            }
                            // Get data block first.
                            cache[positionCache] = sharedMemory.getDataBlock(dataAddress);
                            cache[positionCache].setState(BlockState.SHARED);
                            cacheStats.getBlockDirectory().changeCacheState(dataAddress, this, true, BlockState.SHARED);
                        } finally {
                            // Release our memory bus.
                            memoryBus.release();
                            Clock.executeBarrier();
                            usedCycles++;
                        }
                    } else {
                        return false;
                    }
                    cache[positionCache].getData()[wordPosition] = data;
                    cache[positionCache].setState(BlockState.MODIFIED);
                    cacheStats.getBlockDirectory().changeCacheState(dataAddress, this, true, BlockState.MODIFIED);
                    return true;
                } else {
                    // If not shared or modified, then it will be uncached.
                    // All we need to do now, is simply save the data in our cache.
                    if(memoryBus.tryAcquire()) {
                        try {
                            for (int j = 0; j < cacheStats.getMemoryCycles(); j++) {
                                Clock.executeBarrier();
                                usedCycles++;
                            }
                            // Get data block first.
                            cache[positionCache] = sharedMemory.getDataBlock(dataAddress);
                        } finally {
                            // Release our memory bus.
                            memoryBus.release();
                            Clock.executeBarrier();
                            usedCycles++;
                        }
                    } else {
                        return false;
                    }
                    cache[positionCache].getData()[wordPosition] = data;
                    cache[positionCache].setState(BlockState.MODIFIED);
                    cacheStats.getBlockDirectory().changeCacheState(dataAddress, this, true, BlockState.MODIFIED);
                    return true;
                }
            } finally {
                // Release directory.
                cacheStats.getDirectoryLock().release();
                Clock.executeBarrier();
                usedCycles++;
            }
        } else {
            return false;
        }
    }

    /**
     *
     * @param blockAssigned
     * @return
     */
    private CacheStats getCacheStats(int blockAssigned) {
        if((localCaches.size() == 1 && blockAssigned < 16) || (localCaches.size() == 0 && blockAssigned >= 16)){
            return new CacheStats(localDirectory, localDirectory.getDirectoryLock(), 16, 1, false);
        } else {
            return new CacheStats(remoteDirectory, remoteDirectory.getDirectoryLock(), 40, 5, true);
        }
    }

    /**
     * Return the number of used cycles of the last fetch.
     * @return Number of cycles.
     */
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

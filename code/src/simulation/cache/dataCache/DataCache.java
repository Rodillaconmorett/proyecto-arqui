package simulation.cache.dataCache;

import simulation.block.dataBlock.DataBlock;
import simulation.clock.Clock;
import simulation.directory.directory.Directory;
import simulation.directory.directoryInput.DirectoryInput;
import simulation.sharedMemory.SharedMemory;
import simulation.states.BlockState;

import java.util.concurrent.Semaphore;

public abstract class DataCache {

    private Semaphore memoryBus;

    private Semaphore localDirectoryLock;

    private Semaphore remoteDirectoryLock;

    private Semaphore interConnectionBus;

    private SharedMemory sharedMemory;

    private Directory localDirectory;

    private Directory remoteDirectory;

    private DataBlock[] cache;

    private int usedCycles;

    public DataCache() {
        this.cache = new DataBlock[4];
        for (int i = 0; i < 4; i++) {
            this.cache[i] = new DataBlock(-1);
        }
        this.usedCycles = 0;
    }

    public Integer getDataRequired(int dataAddress,
                                   Semaphore[] localDataCacheLock,
                                   Semaphore[] remoteDataCacheLock){
        int blockAssigned = dataAddress / 16;
        int positionCache = blockAssigned % 4;
        // Check for miss
        if(cache[positionCache].getNumBlock() != blockAssigned
                || cache[positionCache].getState() == BlockState.INVALID){
            // Check if victim block is modified or shared
            if(cache[positionCache].getState() == BlockState.MODIFIED
                    || cache[positionCache].getState() == BlockState.SHARED){
                Directory victimDirectory;
                Semaphore victimDirectoryLock;
                int memoryCycles;
                int directoryCycles;
                // Define which data cache we are working with
                if((localDataCacheLock.length == 2 && blockAssigned < 16)
                        || (localDataCacheLock.length == 1 && blockAssigned >= 16)){
                    victimDirectory = localDirectory;
                    victimDirectoryLock = localDirectoryLock;
                    memoryCycles = 16;
                    directoryCycles = 1;
                }
                else /*if(localDataCacheLock.length == 1)*/ {
                    victimDirectory = remoteDirectory;
                    victimDirectoryLock = remoteDirectoryLock;
                    memoryCycles = 40;
                    directoryCycles = 5;
                }

                if(victimDirectoryLock.tryAcquire()) {
                    try {
                        for (int i = 0; i < directoryCycles; i++) {
                            Clock.executeBarrier();
                            usedCycles++;
                        }
                        // Check if victim has been modified
                        if (cache[positionCache].getState() == BlockState.MODIFIED) {
                            if (memoryBus.tryAcquire()) {
                                try {
                                    for (int i = 0; i < memoryCycles; i++) {
                                        Clock.executeBarrier();
                                        usedCycles++;
                                    }
                                    sharedMemory.saveDataBlock(cache[positionCache]);
                                    //localDirectory.modificar y el bloque queda en u
                                    victimDirectory.setCacheState(dataAddress, false,
                                            false, false, BlockState.UNCACHED);
                                    cache[blockAssigned].setState(BlockState.INVALID);
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
                            if(victimDirectory.countStates(dataAddress) > 1 && victimDirectory.findState(dataAddress) == BlockState.SHARED) {
                                victimDirectory.setSpecificCacheState(dataAddress,this,false);
                            } else {
                                victimDirectory.setCacheState(dataAddress, false,
                                        false, false, BlockState.UNCACHED);
                            }
                            // In case we can't continue because we are lacking resources we need to make
                            // sure that we changed our cache state.
                            cache[blockAssigned].setState(BlockState.INVALID);
                        }
                    } /* Releasing victim directory */ finally {
                        victimDirectoryLock.release();
                        Clock.executeBarrier();
                        usedCycles++;
                    }
                }
                else {
                    return null;
                }
            }
            Directory blockDirectory;
            Semaphore blockDirectoryLock;
            int memoryCycles;
            int directoryCycles;
            // Define which data cache we are working with
            if((localDataCacheLock.length == 2 && blockAssigned < 16)
                    || (localDataCacheLock.length == 1 && blockAssigned >= 16)){
                blockDirectory = localDirectory;
                blockDirectoryLock = localDirectoryLock;
                memoryCycles = 16;
                directoryCycles = 1;
            }
            else /*if(localDataCacheLock.length == 1)*/ {
                blockDirectory = remoteDirectory;
                blockDirectoryLock = remoteDirectoryLock;
                memoryCycles = 40;
                directoryCycles = 5;
            }
            if(blockDirectoryLock.tryAcquire()){
                try {
                    for (int i = 0; i < directoryCycles; i++) {
                        Clock.executeBarrier();
                        usedCycles++;
                    }
                    if(blockDirectory.findState(dataAddress) != BlockState.MODIFIED){
                        if(memoryBus.tryAcquire()){
                            try {
                                for (int i = 0; i < memoryCycles; i++) {
                                    Clock.executeBarrier();
                                    usedCycles++;
                                }
                                cache[positionCache] = sharedMemory.getDataBlock(dataAddress);
                                if (blockDirectory.findState(dataAddress) == BlockState.UNCACHED) {
                                    blockDirectory.changeCacheState(dataAddress,this, true, BlockState.SHARED);
                                } else {
                                    blockDirectory.setSpecificCacheState(dataAddress, this, true);
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
                    blockDirectoryLock.release();
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
    }

    public int getUsedCyclesOfLastRead(){
        int usedCycles = this.usedCycles;
        this.usedCycles = 0;
        return usedCycles;
    }

    public void setMemoryBus(Semaphore memoryBus) {
        this.memoryBus = memoryBus;
    }

    public void setLocalDirectoryLock(Semaphore localDirectoryLock) {
        this.localDirectoryLock = localDirectoryLock;
    }

    public void setRemoteDirectoryLock(Semaphore remoteDirectoryLock) {
        this.remoteDirectoryLock = remoteDirectoryLock;
    }

    public void setInterConectionBus(Semaphore interConectionBus) {
        this.interConnectionBus = interConectionBus;
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
}

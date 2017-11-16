package simulation.cache.dataCache;

import simulation.block.dataBlock.DataBlock;
import simulation.clock.Clock;
import simulation.directory.directory.Directory;
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
        if(cache[positionCache].getNumBlock() != blockAssigned){//miss
            if(cache[positionCache].getState() == BlockState.MODIFIED
                    || cache[positionCache].getState() == BlockState.SHARED){
                boolean directoryVictimLocal;
                if(localDataCacheLock.length == 2){
                    directoryVictimLocal = blockAssigned < 16;
                }
                else /*if(localDataCacheLock.length == 1)*/ {
                    directoryVictimLocal = blockAssigned >= 16;
                }
                if(directoryVictimLocal){
                    if(localDirectoryLock.tryAcquire()){
                        //localDirectory.modificar
                        if(cache[positionCache].getState() == BlockState.MODIFIED){
                            for (int i = 0; i < 1; i++) {
                                Clock.executeBarrier();
                                usedCycles++;
                            }
                            if(memoryBus.tryAcquire()){
                                for (int i = 0; i < 16; i++) {
                                    Clock.executeBarrier();
                                    usedCycles++;
                                }
                                DataBlock newDataBlock = new DataBlock(cache[positionCache].getNumBlock());
                                newDataBlock.setData(cache[positionCache].getData());
                                sharedMemory.saveDataBlock(newDataBlock);
                                //localDirectory.modificar y el bloque queda en u
                                memoryBus.release();
                                localDirectoryLock.release();
                            }
                            else {
                                localDirectoryLock.release();
                                return null;
                            }
                        }
                    }
                    else {
                        return null;
                    }
                }
                else{
                    if(remoteDirectoryLock.tryAcquire()){
                        //remoteDirectory.modificar
                        if(cache[positionCache].getState() == BlockState.MODIFIED){
                            for (int i = 0; i < 5; i++) {
                                Clock.executeBarrier();
                                usedCycles++;
                            }
                            if(memoryBus.tryAcquire()){
                                for (int i = 0; i < 40; i++) {
                                    Clock.executeBarrier();
                                    usedCycles++;
                                }
                                DataBlock newDataBlock = new DataBlock(cache[positionCache].getNumBlock());
                                newDataBlock.setData(cache[positionCache].getData());
                                sharedMemory.saveDataBlock(newDataBlock);
                                //localDirectory.modificar y el bloque queda en u
                                memoryBus.release();
                                remoteDirectoryLock.release();
                            }
                            else {
                                remoteDirectoryLock.release();
                                return null;
                            }
                        }
                    }
                    else {
                        return null;
                    }
                }
            }
            /***************************************/

        }
        int wordPosition = (dataAddress / 4) % 4;
        return cache[positionCache].getData()[wordPosition];
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

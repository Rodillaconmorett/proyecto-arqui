package simulation.cache.dataCache;

import simulation.directory.directory.Directory;
import simulation.sharedMemory.SharedMemory;

import java.util.concurrent.Semaphore;

public abstract class DataCache {

    private Semaphore memoryBus;

    private Semaphore localDirectoryLock;

    private Semaphore remoteDirectoryLock;

    private Semaphore interConectionBus;

    private SharedMemory sharedMemory;

    private Directory localDirectory;

    private Directory remoteDirectory;

    public DataCache() {}

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
        this.interConectionBus = interConectionBus;
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

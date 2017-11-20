package simulation.cache.cacheStats;


import simulation.directory.directory.Directory;

import java.util.concurrent.Semaphore;

public class CacheStats {

    private Directory blockDirectory;

    private Semaphore directoryLock;

    private int memoryCycles;

    private int directoryCycles;

    private boolean isRemote;

    public CacheStats(Directory blockDirectory, Semaphore directoryLock, int memoryCycles, int directoryCycles, boolean isRemote){
        this.blockDirectory = blockDirectory;
        this.directoryLock = directoryLock;
        this.memoryCycles = memoryCycles;
        this.directoryCycles = directoryCycles;
        this.isRemote = isRemote;
    }

    public Directory getBlockDirectory() {
        return blockDirectory;
    }

    public Semaphore getDirectoryLock() {
        return directoryLock;
    }

    public int getMemoryCycles() {
        return memoryCycles;
    }

    public int getDirectoryCycles() {
        return directoryCycles;
    }

    public boolean isRemote() {
        return isRemote;
    }
}

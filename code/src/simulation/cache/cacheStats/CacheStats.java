package simulation.cache.cacheStats;


import simulation.directory.directory.Directory;

import java.util.concurrent.Semaphore;

/**
 * Information need to look for a block of memory.
 */
public class CacheStats {
    /// Directory that contains the block information.
    private Directory blockDirectory;
    /// Directory lock.
    private Semaphore directoryLock;
    /// Number of cycles needed to fetch a data from memory.
    private int memoryCycles;
    /// Number of cycles needed to fetch the directory.
    private int directoryCycles;
    /// Tell us if the block is located in remote structures.
    private boolean isRemote;

    /**
     * Default constructor.
     * @param blockDirectory Directory.
     * @param directoryLock Directory lock.
     * @param memoryCycles Memory cycles needed to fetch.
     * @param directoryCycles Directory cycles needed to fetch.
     * @param isRemote Tell us if the block in locatd in a remote structure.
     */
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

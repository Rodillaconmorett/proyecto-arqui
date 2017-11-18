package simulation.directory.directory;

import simulation.cache.dataCache.DataCache;
import simulation.directory.directoryInput.DirectoryInput;
import simulation.states.BlockState;

import java.util.concurrent.Semaphore;

/**
 * Our directory, that manages a given number of blocks in shared memory.
 */
public class Directory {
    /// Lock that will prevent other caches from using it, if another one is using it.
    private Semaphore directoryLock;
    /// Directory's entrees.
    private DirectoryInput[] inputs;
    /// Initial address of the first block in shared memory that this directory manages.
    private int initialAddress;
    /// References to our caches.
    private DataCache[] caches;

    /**
     * Default constructor.
     * @param inputCount Number of entrees that our directory will contain.
     * @param initialAddress Initial address of the first block contained in the directory.
     * @param lock Lock that we will use to prevent synchronization errors.
     */
    public Directory(int inputCount, int initialAddress, Semaphore lock, DataCache[] caches) {
        this.caches = caches;
        this.directoryLock = lock;
        this.initialAddress = initialAddress;
        this.inputs = new DirectoryInput[inputCount];
        for (int i = 0; i < this.inputs.length; i++) {
            this.inputs[i] = new DirectoryInput();
        }
    }

    /**
     * Given an address, we return the respective block's state.
     * @param address Address of the block we want to find.
     * @return Block's state.
     */
    public BlockState findState(int address) {
        int finalAddress = (address-initialAddress)/4;
        return inputs[finalAddress].getState();
    }
    /**
     * Given an address, we return the respective directory entree that represents the state of the block.
     * @param address Address of the block we want to find.
     * @return State of the block in the different caches.
     */
    public DirectoryInput getCachesState(int address) {
        int finalAddress = (address-initialAddress)/4;
        return inputs[finalAddress];
    }

    /**
     * Change the state of a directory entree.
     * @param address Address of the entree we want to modified.
     * @param cache_0 New state for the cache 0.
     * @param cache_1 New state for the cache 1.
     * @param cache_2 New state for the cache 2.
     * @param newState New state for the entree.
     */
    public void setCacheState(int address, boolean cache_0, boolean cache_1, boolean cache_2, BlockState newState) {
        int finalAddress = (address-initialAddress)/16;
        inputs[finalAddress].setState(newState);
        inputs[finalAddress].setCacheState(0,cache_0);
        inputs[finalAddress].setCacheState(1,cache_1);
        inputs[finalAddress].setCacheState(2,cache_2);
    }

    /**
     * Check the position of our cache in the inputs array.
     * @param address Input address.
     * @param cache Cache we want to look for.
     * @param state New state.
     */
    public void setSpecificCacheState(int address, DataCache cache, boolean state){
        int finalAddress = (address-initialAddress)/16;
        for (int i = 0; i < caches.length; i++) {
            if(caches[i] == cache) {
                inputs[finalAddress].setCacheState(i,state);
            }
        }
    }

    public void changeCacheState(int address, DataCache cache, boolean state, BlockState blockState){
        int finalAddress = (address-initialAddress)/16;
        inputs[finalAddress].setState(blockState);
        for (int i = 0; i < caches.length; i++) {
            if(caches[i] == cache) {
                inputs[finalAddress].setCacheState(i,state);
            }
        }
    }

    public int countStates(int address) {
        int finalAddress = (address-initialAddress)/16;
        return inputs[finalAddress].countValidStates();
    }
}

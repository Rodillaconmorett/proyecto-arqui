package simulation.directory.directoryInput;

import simulation.states.BlockState;

/**
 * Directory entree, where we can find the state of each memory block.
 */
public class DirectoryInput {

    /// State of the block.
    private BlockState state;
    /// State of the caches.
    private boolean[] caches;

    /**
     * Default constructor.
     */
    public DirectoryInput() {
        state = BlockState.UNCACHED;
        caches = new boolean[3];
        for (int i = 0; i < caches.length; i++) {
            caches[i] = false;
        }
    }

    public BlockState getState() {
        return state;
    }

    public void setState(BlockState state) {
        this.state = state;
    }

    /**
     * Set a cache state given it's number and the new state. Also, if the other caches are valid, changes them to a new state.
     * @param numCache Cache number.
     * @param cacheState New state for that cache.
     */
    public void setCacheState(int numCache, boolean cacheState) {
        caches[numCache] = cacheState;
    }

    /**
     * Return the number of valid states.
     * @return Number of valid states.
     */
    public int countValidStates() {
        int counter = 0;
        for (int i = 0; i < caches.length; i++) {
            if(caches[i]){
                counter++;
            }
        }
        return counter;
    }

    public boolean[] getCaches() {
        return caches;
    }
}

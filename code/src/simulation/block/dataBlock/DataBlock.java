package simulation.block.dataBlock;

import simulation.states.BlockState;

/**
 * Object that holds our program data.
 */
public class DataBlock {

    /// This is where our data is stored.
    private int[] data;
    /// Actual state of our block.
    private BlockState state;
    /// Block number
    private int numBlock;
    /**
     * Constructor of our data block.
     * @param numBlock Location of our block in our shared memory.
     */
    public DataBlock(int numBlock){
        numBlock = numBlock;
        state = BlockState.UNCACHED;
        data = new int[4];
        // We must set all integers to 0 when initializing our data block.
        for (int i = 0; i < 4; i++) {
            data[i] = 0;
        }
    }

    public int getNumBlock() {
        return numBlock;
    }

    public void setNumBlock(int numBlock) {
        this.numBlock = numBlock;
    }

    public int[] getData() {
        return data;
    }

    public void setData(int[] block) {
        data = block;
    }

    public BlockState getState() {
        return state;
    }

    public void setState(BlockState newState) {
        state = newState;
    }

}

package simulation.block.dataBlock;

import simulation.states.BlockState;

/**
 * Object that holds our program data.
 */
public class DataBlock {

    /// This is where our data is stored.
    private int[] data;
    /// Address of the block in our shared memory.
    private int address;
    /// Actual state of our block.
    private BlockState state;
    /**
     * Constructor of our data block.
     * @param finalAddress Location of our block in our shared memory.
     */
    public DataBlock(int finalAddress){
        address = finalAddress;
        state = BlockState.UNCACHED;
        data = new int[4];
        // We must set all integers to 0 when initializing our data block.
        for (int i = 0; i < 4; i++) {
            data[i] = 0;
        }
    }

    public int[] getData() {
        return data;
    }

    public void setData(int[] block) {
        data = block;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int memAddress) {
        address = memAddress;
    }

    public BlockState getState() {
        return state;
    }

    public void setState(BlockState newState) {
        state = newState;
    }

}

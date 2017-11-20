package simulation.sharedMemory;


import simulation.block.dataBlock.DataBlock;

/**
 * Class where the program manages the shared memory of all the processors.
 */
public class SharedMemory {

    /// Memory blocks that are managed by our shared memory.
    private DataBlock[] blocks;

    /**
     * Constructor of our shared memory object.
     * @param blockCount Number of blocks that our shared memory holds.
     */
    public SharedMemory(int blockCount) {
        blocks = new DataBlock[blockCount];
        // We need to initialize all blocks and set all integers to 0.
        for(int i = 0; i<blocks.length; i++) {
            blocks[i] = new DataBlock(i);
        }
    }

    /**
     * Given a data block, we save in it's respective address.
     * @param block Block to save in memory.
     */
    public void saveDataBlock(DataBlock block) {
        blocks[block.getNumBlock()] = block;
    }

    /**
     * Given an address, returns a block of data.
     * @param address Address of the desired block.
     * @return Data block located in the given address.
     */
    public DataBlock getDataBlock(int address) {
        // To find the right block, we need to divide by 16.
        int finalAddress = address/16;
        return blocks[finalAddress];
    }

}

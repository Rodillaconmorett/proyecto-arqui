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

    public void saveDataBlock(DataBlock block) {
        blocks[block.getAddress()] = block;
    }

    public DataBlock getDataBlock(int address) {
        int finalAddress = address/16;
        return blocks[finalAddress];
    }

}

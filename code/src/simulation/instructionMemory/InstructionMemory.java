package simulation.instructionMemory;


import simulation.block.instructionBlock.InstructionBlock;

/**
 * Object that represents our instruction memory.
 */
public class InstructionMemory {
    /// Memory block that holds our instructions.
    private InstructionBlock[] blocks;

    /**
     * Default constructor.
     * @param blockCount Number of possible counts.
     */
    public InstructionMemory(int blockCount) {
        blocks = new InstructionBlock[blockCount];
        // We need to initialize all blocks and set all integers to 0.
        for(int i = 0; i<blocks.length; i++) {
            blocks[i] = new InstructionBlock(i);
        }
    }

    /**
     * Given an address and a block, saves that block in that location.
     * @param address Locations in our memory where the block will be saved.
     * @param block Block to save.
     */
    public void saveDataBlock(int address, InstructionBlock block) {
        block.setAddress(address);
        blocks[address] = block;
    }

    public InstructionBlock getDataBlock(int address) {
        return blocks[address];
    }
}

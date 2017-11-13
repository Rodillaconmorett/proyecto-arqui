package simulation.instructionMemory;


import simulation.block.instructionBlock.InstructionBlock;

/**
 * Object that represents our instruction memory.
 */
public class InstructionMemory {
    /// Memory block that holds our instructions.
    private InstructionBlock[] blocks;
    /// Location of the first instruction.
    private int initialAddress;

    /**
     * Default constructor.
     * @param blockCount Number of possible counts.
     */
    public InstructionMemory(int blockCount, int initialAddress) {
        initialAddress = initialAddress;
        blocks = new InstructionBlock[blockCount];
        // We need to initialize all blocks and set all integers to 0.
        for(int i = 0; i<blocks.length; i++) {
            blocks[i] = new InstructionBlock(i);
        }
    }

    /**
     * Given an address and a block, saves that block in that location.
     * @param block Block to save.
     */
    public void saveDataBlock(InstructionBlock block) {
        blocks[block.getNumBlock()] = block;
    }

    public InstructionBlock getDataBlock(int address) {
        int finalAddress = (address-initialAddress)/16;
        return blocks[finalAddress];
    }
}

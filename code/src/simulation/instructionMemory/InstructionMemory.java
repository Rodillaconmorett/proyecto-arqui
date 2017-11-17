package simulation.instructionMemory;


import simulation.block.instruction.Instruction;
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
     *
     * @param blockCount Number of possible counts.
     */
    public InstructionMemory(int blockCount, int initialAddress) {
        this.initialAddress = initialAddress;
        blocks = new InstructionBlock[blockCount];
        // We need to initialize all blocks and set all integers to 0.
        int position = initialAddress / 16;
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = new InstructionBlock(i + position);
        }
    }

    /**
     * Given an address and a block, saves that block in that location.
     *
     * @param block Block to save.
     */
    public void saveInstructionBlock(InstructionBlock block) {
        int position = initialAddress / 16;
        blocks[block.getNumBlock() - position] = block;
    }

    /**
     * @param address addres in  memory (256..636) or (128..380)
     * @return
     */
    public InstructionBlock getInstructionBlock(int address) {
        int finalAddress = (address - initialAddress) / 16;
        return blocks[finalAddress];
    }

    public void saveIntruction(Instruction instruction, int address) {
        int finalAddress = (address - initialAddress) / 16;
        int index = (address / 4) % 4;
        blocks[finalAddress].setIntruction(instruction, index);
    }

    public Instruction getInst(int address) {
        int finalAddress = (address - initialAddress) / 16;
        int index = (address % 16) / 4;
        return blocks[finalAddress].getInst(index);
    }
}

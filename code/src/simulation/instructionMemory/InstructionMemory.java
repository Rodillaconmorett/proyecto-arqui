package simulation.instructionMemory;


import simulation.block.instruction.Instruction;
import simulation.block.instructionBlock.InstructionBlock;

import java.util.concurrent.Semaphore;

/**
 * Object that represents our instruction memory.
 */
public class InstructionMemory {
    /// Memory block that holds our instructions.
    private InstructionBlock[] blocks;
    /// Location of the first instruction.
    private int initialAddress;
    /// Instruction memory lock.
    private Semaphore myLock;

    /**
     * Default constructor.
     *
     * @param blockCount Number of possible counts.
     */
    public InstructionMemory(int blockCount, int initialAddress) {
        this.myLock = new Semaphore(1);
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
     * Given an address, returns a block of instructions.
     * @param address Address in  memory (256..636) or (128..380)
     * @return
     */
    public InstructionBlock getInstructionBlock(int address) {
        int finalAddress = (address - initialAddress) / 16;
        return blocks[finalAddress];
    }

    /**
     * Loads instruction into the memory, one by one.
     * @param instruction
     * @param address
     */
    public void saveInstruction(Instruction instruction, int address) {
        int finalAddress = (address - initialAddress) / 16;
        int index = (address / 4) % 4;
        blocks[finalAddress].setIntruction(instruction, index);
    }

    public Semaphore getMyLock() {
        return myLock;
    }
}

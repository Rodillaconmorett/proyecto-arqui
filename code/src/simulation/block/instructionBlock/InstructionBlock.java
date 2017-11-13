package simulation.block.instructionBlock;

import simulation.block.instruction.Instruction;
import simulation.states.BlockState;

/**
 * Object that represents each one of our instructions blocks that are in our instruction memory.
 */
public class InstructionBlock {

    /// Each instruction block holds 4 instructions, which in return holds 4 integers.
    private Instruction[] instructions;
    /// Address of the block in our instruction memory.
    private int address;
    /// State of our block.
    private BlockState state;

    public InstructionBlock(int finalAddress) {
        address = finalAddress;
        state = BlockState.UNCACHED;
        instructions = new Instruction[4];
        for (int i = 0; i < 4; i++) {
            // At first our instructions will always be empty.
            instructions[i] = new Instruction(0,0,0,0);
        }
    }

    public Instruction[] getInstructions() {
        return instructions;
    }

    public void setInstructions(Instruction[] newInstructions) {
        instructions = newInstructions;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int newAddress) {
        address = newAddress;
    }

    public BlockState getState() {
        return state;
    }

    public void setState(BlockState newState) {
        state = newState;
    }

}

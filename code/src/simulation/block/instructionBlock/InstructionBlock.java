package simulation.block.instructionBlock;

import simulation.block.instruction.Instruction;
import simulation.states.BlockState;

/**
 * Object that represents each one of our instructions blocks that are in our instruction memory.
 */
public class InstructionBlock {

    /// Each instruction block holds 4 instructions, which in return holds 4 integers.
    private Instruction[] instructions;
    /// State of our block.
    private BlockState state;
    /// Block number
    private int numBlock;

    public InstructionBlock(int numBlock) {
        numBlock = numBlock;
        state = BlockState.UNCACHED;
        instructions = new Instruction[4];
        for (int i = 0; i < 4; i++) {
            // At first our instructions will always be empty.
            instructions[i] = new Instruction(0,0,0,0);
        }
    }

    public int getNumBlock() {
        return numBlock;
    }

    public void setNumBlock(int numBlock) {
        this.numBlock = numBlock;
    }

    public Instruction[] getInstructions() {
        return instructions;
    }

    public void setInstructions(Instruction[] newInstructions) {
        instructions = newInstructions;
    }

    public BlockState getState() {
        return state;
    }

    public void setState(BlockState newState) {
        state = newState;
    }

    public  void setIntruction (Instruction instruction, int index){ instructions[index]= instruction; }

    public Instruction getInst(int index){
        return instructions[index];
    }
}

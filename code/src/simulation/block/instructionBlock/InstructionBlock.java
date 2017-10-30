package simulation.block.instructionBlock;

import simulation.block.instruction.Instruction;

public interface InstructionBlock {

    /**
     * Gets the tag of the block.
     * @return The tag of the block.
     */
    int getTag();

    /**
     * Gets the instruction based on the parameter.
     * @param i The index of the instruction.
     * @return The instruction at the index i on the block.
     */
    Instruction getInstruction(int i);
}

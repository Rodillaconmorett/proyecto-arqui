package simulation.block.instructionBlock.impl;

import simulation.block.instruction.Instruction;
import simulation.block.instructionBlock.InstructionBlock;

public class InstructionBlockImpl implements InstructionBlock{
    private Instruction[] instructions;
    private int tag;
    private final int INSTRUCTION_BLOCK_SIZE = 4;

    public InstructionBlockImpl(Instruction[] instructions, int tag){
        this.instructions = instructions;
        this.tag = tag;
    }

    @Override
    public int getTag() {
        return this.tag;
    }

    @Override
    public Instruction getInstruction(int i) {
        if(i < 0 || i > INSTRUCTION_BLOCK_SIZE - 1){
            return null;
        }
        else {
            return this.instructions[i];
        }
    }
}

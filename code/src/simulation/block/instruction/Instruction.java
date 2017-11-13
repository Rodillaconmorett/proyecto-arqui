package simulation.block.instruction;

/**
 * This object represents the instructions that our cores will be running.
 */
public class Instruction {
    /// Instruction code.
    private int[] instruction;

    /**
     * Builds a new instruction instance
     * @param id The type of the instruction.
     * @param p1 The first parameter of the instruction
     * @param p2 The second parameter of the instruction
     * @param p3 The third parameter of the instruction
     */
    public Instruction(int id, int p1, int p2, int p3){
        this.instruction = new int[4];
        this.instruction[0] = id;
        this.instruction[1] = p1;
        this.instruction[2] = p2;
        this.instruction[3] = p3;
    }

    /**
     * Gets the type of the instruction
     * @return The type of the instruction
     */
    public int getTypeOfInstruction(){
        return instruction[0];
    }

    /**
     * Gets the first parameter of the instruction
     * @return The first parameter of the instruction
     */
    public int getFirstParameter(){
        return instruction[1];
    }

    /**
     * Gets the second parameter of the instruction
     * @return The second parameter of the instruction
     */
    public int getSecondParameter(){
        return instruction[2];
    }

    /**
     * Gets the third parameter of the instruction
     * @return The third parameter of the instruction
     */
    public int getThirdParameter(){
        return instruction[3];
    }
}

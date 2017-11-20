package simulation.thread;

import java.util.Vector;

/**
 * Representation of running programs in our computer.
 */
public class Thread {
    /// Process counter.
    private int pc;
    /// Initial process counter.
    private int initialPc;
    /// Registers values of this process.
    private int[] registers;
    /// Vector of memory address pointing to it's instructions in our instruction memory.
    private Vector<Integer> instructions;
    /// Number of cycles completed.
    private int cycles;
    /// Tell us if the process has finished running.
    private boolean finished;
    /// Name of the process.
    private String name;

    /**
     * Default cnstructor.
     * @param pc Initial process counter.
     * @param instructions Instruction's address to run, pointing to our instruction memory.
     * @param name Name of the process.
     */
    public Thread(int pc, Vector<Integer> instructions, String name) {
        this.pc = pc;
        this.finished = false;
        this.initialPc = pc;
        registers = new int[32];
        this.instructions = instructions;
        this.cycles = 0;
        this.name = name;
    }

    /**
     * Save the current context. Used in context switches.
     */
    public void saveContext(int[] registers, int pc) {
        this.registers = registers;
        this.pc = pc;
    }

    public int[] getRegisters() {
        return registers;
    }

    public int getPc() {
        return pc;
    }

    public int getCycles() {
        return cycles;
    }

    public void addCycles(int numberCycles) {
        cycles += numberCycles;
    }

    /**
     * Gets the instruction specified at index.
     *
     * @param index The specified instruction index;
     * @return If index less than instructions length, returns the address of the instruction at the index.
     * Otherwise, returns -1
     */
    public int getInstruction(int index){
        int finalIndex = index-initialPc;
        if(finalIndex < instructions.size())
            return instructions.elementAt(finalIndex);
        return -1;
    }

    /**
     * Changes the pc adding the number of pc that advance.
     *
     * @param nextPc the number of pc that advance.
     */
    public void changePc(int nextPc) {
        pc += nextPc;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int getInitialPc() {
        return initialPc;
    }

    public Vector<Integer> getInstructions() {
        return instructions;
    }

    public String getName() {
        return name;
    }
}

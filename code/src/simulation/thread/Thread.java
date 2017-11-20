package simulation.thread;

import java.util.Vector;
import java.util.concurrent.Semaphore;

public class Thread {
    private int pc;
    private int initialPc;
    private int[] registers;
    private Vector<Integer> instructions;
    private int cycles;
    private boolean finished;
    private String name;
    private Semaphore myLock;

    public Thread(int pc, Vector<Integer> instructions, String name) {
        this.pc = pc;
        this.finished = false;
        this.initialPc = pc;
        registers = new int[32];
        this.instructions = instructions;
        this.cycles = 0;
        this.name = name;
        this.myLock = new Semaphore(1);
    }

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

    public Semaphore getMyLock() {
        return myLock;
    }
}

package simulation.thread;

import java.util.List;
import java.util.Vector;

public class Thread {
    private int pc;
    private int[] registers;
    private Vector<Integer> instructions;
    private int cycles;

    public Thread(int pc, Vector<Integer> instructions){
        this.pc = pc;
        registers  = new int[32];
        this.instructions = instructions;
        this.cycles = 0;
    }

    public void saveContext(int[] registers, int pc){
        this.registers = registers;
        this.pc = pc;
    }

    public int[] getRegisters(){
        return registers;
    }

    public int getPc(){
        return pc;
    }

    public int getCycles() {
        return cycles;
    }

    public void addCycles(int numberCycles){
        cycles += numberCycles;
    }

    /**
     * Gets the instruction specified at index.
     * @param index The specified instruction index;
     * @return If index less than instructions length, returns the address of the instruction at the index.
     * Otherwise, returns -1
     */
    public int getInstruction(int index){
        if(index < instructions.size())
        return instructions.elementAt(index);
        return -1;
    }

    /**
     * Changes the pc adding the number of pc that advance.
     * @param nextPc the number of pc that advance.
     */
    public void changePc(int nextPc){
        pc += nextPc;
    }

    public void impVec() {
        for (int i = 0; i < instructions.size(); i++) {
            System.out.println(instructions.elementAt(i));
        }
    }
}

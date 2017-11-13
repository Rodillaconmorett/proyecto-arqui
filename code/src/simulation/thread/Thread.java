package simulation.thread;

public class Thread {
    private int pc;
    private int[] registers; // 32 enteros
    private int[] instructions; // cada instruccion de de 4 enteros
    private int cycles;

    public Thread(int pc, int[] registers, int[] instructions){
        this.pc = pc;
        this.registers = registers;
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

    public int getInstruction(int index){
        return instructions[index];
    }

    /**
     * Changes the pc adding the number of pc that advance.
     * @param nextPc the number of pc that advance.
     */
    public void changePc(int nextPc){
        pc += nextPc;
    }
}

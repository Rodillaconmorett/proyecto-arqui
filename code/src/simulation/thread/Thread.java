package simulation.thread;

public class Thread {
    private int pc;
    private int[] registers; // 32 enteros
    private int[] instructions; // cada instruccion de de 4 enteros
    private long time_start;  //inicio de la ejecucion de un hilo
    private long time_end;

    public Thread(int pc, int[] registers, int[] instructions){
        this.pc = pc;
        this.registers = registers;
        this.instructions = instructions;
    }



    public void guardarContexto(){

    }

}

package simulation.simulator;

import simulation.clock.Clock;
import simulation.instructionMemory.InstructionMemory;
import simulation.processor.impl.Processor_P0;
import simulation.processor.impl.Processor_P1;
import simulation.sharedMemory.SharedMemory;
import simulation.thread.Thread;

public class Simulator {

    public Simulator() {

    }

    public void simulate(int threadCount) {
        Clock.setCoreCount(threadCount);
        SharedMemory sharedMemory = new SharedMemory(24);
        InstructionMemory instructionMemory_0 = new InstructionMemory(24,256);
        InstructionMemory instructionMemory_1 = new InstructionMemory(16,128);
//        Processor_P0 processor_p0 = new Processor_P0();
//        Processor_P1 processor_p1 = new Processor_P1();
    }
//TODO
//    public Thread readThread(String fileName) {
//        Thread thread = new Thread();
//        int[] registers = new int[32];
//        return thread;
//    }

    /*
    this.id = id;
    registros = new int[32];
    instruccion = new int[4];
    File archivo = null;
    FileReader fr = null;
    BufferedReader br = null;
        try {
        String nomb= "contexto"+id+".txt";
        archivo = new File (nomb);
        fr = new FileReader (archivo);
        br = new BufferedReader(fr);
        // Lectura del fichero
        String linea;
        linea=br.readLine();
        String[] vecInt = linea.split(",");
        for(int i=0; i<32;++i)
            registros[i] = Integer.parseInt(vecInt[i]);
        linea=br.readLine();
        vecInt = linea.split(",");
        pc= Integer.parseInt(vecInt[0]);
        quantum = Integer.parseInt(vecInt[1]);
        time_start = Long.parseLong(vecInt[2]);
        System.out.println("pc: "+ pc+"  quan:" + quantum+ "  timeSt: "+ time_start );
    }
        catch(Exception e){
        e.printStackTrace();
    }finally{
        try{
            if( null != fr ){
                fr.close();
            }
        }catch (Exception e2){
            e2.printStackTrace();
        }
    }
    */

    /* guardarContexto()
    FileWriter fichero = null;
    PrintWriter pw = null;
        try
    {
        String nomb= "contexto"+id+".txt";
        fichero = new FileWriter(nomb);
        pw = new PrintWriter(fichero);
        for (int i = 0; i < 32; i++)
            pw.print(registros[i]+",");
        pw.println("");
        pw.println(pc+","+quantum+","+time_start);
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        try {
            if (null != fichero)
                fichero.close();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    } */
}

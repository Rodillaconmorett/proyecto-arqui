package simulation.processor;

import simulation.block.instruction.Instruction;
import simulation.cache.dataCache.DataCache;
import simulation.cache.dataCache.impl.DataCache_P0;
import simulation.cache.instructionCache.InstructionCache;
import simulation.core.Core;
import simulation.directory.directory.Directory;
import simulation.instructionMemory.InstructionMemory;
import simulation.sharedMemory.SharedMemory;
import simulation.thread.Thread;

import javax.swing.*;
import java.io.*;
import java.util.Vector;
import java.util.concurrent.Semaphore;

public class Processor {

    private Core[] cores;

    public Processor(SharedMemory sharedMemory,
                     InstructionMemory instructionMemory,
                     Directory localDirectory,
                     Directory remoteDirectory,
                     int coreCount, int threadCount, int initialAdressInst) {
        Semaphore instructionBus = new Semaphore(1);
        InstructionCache instructionCache = new InstructionCache(instructionBus, instructionMemory);
        Semaphore memoryBus = new Semaphore(1);
        DataCache dataCache = new DataCache_P0();

        Thread [] threads= new Thread[threadCount];
        createThreadsAndMemoryInstr(threads,instructionMemory,initialAdressInst);

        /*threads[0].impVec();
        System.out.println("Estoy aca");*/

        cores = new Core[coreCount];

        cores[0]=new Core(instructionCache,dataCache,100, threads);
        cores[0].run();
        for (int i = 0; i < cores.length; i++) {
            //TODO
            //New cores
        }
    }

    public void createThreadsAndMemoryInstr(Thread[] threads, InstructionMemory instructionMemory, int initialAddressInst){

        for (int i = 0; i < threads.length ; i++) {


            File archivo = null;
            FileReader fr = null;
            BufferedReader br = null;
            try {
                String name = JOptionPane.showInputDialog("What's its name? "+i);
                String nomb= name+".txt"; // nombre del archivo
                archivo = new File (nomb);
                fr = new FileReader (archivo);
                br = new BufferedReader(fr);
                // Lectura del fichero
                String linea;
                Vector<Integer> adress = new Vector<Integer>();
                while((linea=br.readLine())!=null) {
                    String[] vecInt = linea.split(" ");
                    Instruction instruction = new Instruction(Integer.parseInt(vecInt[0]), Integer.parseInt(vecInt[1]), Integer.parseInt(vecInt[2]), Integer.parseInt(vecInt[3]));
                    instructionMemory.saveIntruction(instruction,initialAddressInst);
                    adress.add(initialAddressInst);
                    initialAddressInst +=4;
                }
                threads[i]= new Thread(0,adress);
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

        }


    }


}

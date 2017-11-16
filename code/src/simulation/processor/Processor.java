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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Semaphore;

public class Processor {

    private Core[] cores;
    private Thread [] threads;
    private List<Integer> indexThreads;
    private InstructionCache instructionCache;
    private Semaphore instructionBus;
    private Semaphore [] threadSem;
    private Semaphore memoryBus;
    private DataCache dataCache;

    public Processor(SharedMemory sharedMemory,
                     InstructionMemory instructionMemory,
                     Directory localDirectory,
                     Directory remoteDirectory,
                     int coreCount, int threadCount, int initialAdressInst) {
        this.instructionBus = new Semaphore(1);
        this.threadSem= new Semaphore[threadCount];
        for (int i = 0; i < threadCount; i++) {
            this.threadSem[i] = new Semaphore(1);
        }
        this.instructionCache = new InstructionCache(instructionBus, instructionMemory);
        this.memoryBus = new Semaphore(1);
        this.dataCache = new DataCache_P0();
        this.threads= new Thread[threadCount];
        indexThreads= new ArrayList<Integer>();
        createThreadsAndMemoryInstr(instructionMemory,initialAdressInst);

        cores = new Core[coreCount];

//        cores[0]=new Core(instructionCache,dataCache,100, threads,indexThreads, threadSem,memoryBus);
//        cores[0].run();
        for (int i = 0; i < coreCount; i++) {
            //TODO
            //New cores
            cores[i]=new Core(instructionCache,dataCache,100, threads, threadSem,memoryBus);
        }
    }

    public void createThreadsAndMemoryInstr(InstructionMemory instructionMemory, int initialAddressInst){

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


    public void start() {
        for (int i = 0; i < 1; i++) {
            //TODO
            //New cores
            cores[i].start();
        }

    }
}
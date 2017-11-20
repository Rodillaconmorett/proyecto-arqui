package simulation.simulator;

import simulation.block.instruction.Instruction;
import simulation.cache.dataCache.DataCache;
import simulation.clock.Clock;
import simulation.directory.directory.Directory;
import simulation.instructionMemory.InstructionMemory;
import simulation.processor.Processor;
import simulation.sharedMemory.SharedMemory;
import simulation.thread.Thread;
import simulation.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.Semaphore;

/**
 * Class in charge of setting up our simulation.
 */
public class Simulator {

    /**
     * Constructor.
     * @param coreCount Number of cores that our computer has.
     * @param quantum Quantum given to each instruction.
     */
    public Simulator(int coreCount, int quantum) {
        // Setting up our clock.
        Clock.setCoreCount(coreCount);
        // Shared memory. It is made of 24 blocks.
        SharedMemory sharedMemory = new SharedMemory(24);
        // Instruction memory. We need to create one for each processor. Also, we need to make sure that each
        // instruction memory knows where it's first instruction starts.
        InstructionMemory instructionMemory_0 = new InstructionMemory(24, Config.INSTRUCTION_MEMORY_0_INITIAL_ADDRESS);
        InstructionMemory instructionMemory_1 = new InstructionMemory(16, Config.INSTRUCTION_MEMORY_1_INITIAL_ADDRESS);
        // Data cache. Each one of our core has a data cache.
        DataCache dataCache_1 = new DataCache(new Semaphore(1));
        DataCache dataCache_2 = new DataCache(new Semaphore(1));
        DataCache dataCache_3 = new DataCache(new Semaphore(1));
        // Setting up local and remote caches for each cache.
        dataCache_1.addLocalCaches(dataCache_2);
        dataCache_1.addRemoteCaches(dataCache_3);
        dataCache_2.addLocalCaches(dataCache_1);
        dataCache_2.addRemoteCaches(dataCache_3);
        dataCache_3.addRemoteCaches(dataCache_1);
        dataCache_3.addRemoteCaches(dataCache_2);
        // Each core has it's own distributed shared memory space, so each one of then has a memory bus.
        Semaphore memoryBus_1 = new Semaphore(1);
        Semaphore memoryBus_2 = new Semaphore(1);
        // Assigning the memory buses for each data cache.
        dataCache_1.setMemoryBus(memoryBus_1);
        dataCache_2.setMemoryBus(memoryBus_1);
        dataCache_3.setMemoryBus(memoryBus_2);
        // Assigning the shared memory for each data cache.
        dataCache_1.setSharedMemory(sharedMemory);
        dataCache_2.setSharedMemory(sharedMemory);
        dataCache_3.setSharedMemory(sharedMemory);
        // Only one core at a time can read from a remote source. That is what the inter connection bus is for.
        Semaphore interConnectionBus = new Semaphore(1);
        // Assigning the inter connection bus.
        dataCache_1.setInterConnectionBus(interConnectionBus);
        dataCache_2.setInterConnectionBus(interConnectionBus);
        dataCache_3.setInterConnectionBus(interConnectionBus);
        // Setting a data cache array.
        DataCache[] caches = new DataCache[3];
        caches[0] = dataCache_1;
        caches[1] = dataCache_2;
        caches[2] = dataCache_3;
        // Reading from the ".txt" files. Also, adding all files into an array.
        File folder = new File("threads");
        ArrayList<File> files = new ArrayList<File>(Arrays.asList(folder.listFiles()));
        // Assigning the amount of programs that each processor will execute.
        Thread[] threadsP0 = new Thread[folder.list().length*2/3];
        Thread[] threadsP1 = new Thread[folder.list().length-folder.list().length*2/3];
        int threadCountP0 = folder.list().length*2/3;
        int threadCountP1 = folder.list().length-threadCountP0;
        // Setting up each program process.
        for (int i = 0; i < threadCountP0; i++) {
            if(i>0) {
                threadsP0[i] = setUpInstructions(instructionMemory_0, threadsP0[i-1].getInitialPc()+threadsP0[i-1].getInstructions().size()*4, files.get(i));
            } else {
                threadsP0[i] = setUpInstructions(instructionMemory_0, Config.INSTRUCTION_MEMORY_0_INITIAL_ADDRESS, files.get(i));
            }
        }
        for (int i = threadCountP0; i < folder.list().length; i++) {
            if(i>threadCountP0) {
                threadsP1[i-threadCountP0] = setUpInstructions(instructionMemory_1, threadsP1[i-1-threadCountP0].getInitialPc()+threadsP1[i-1-threadCountP0].getInstructions().size()*4, files.get(i));
            } else {
                threadsP1[i-threadCountP0] = setUpInstructions(instructionMemory_1, Config.INSTRUCTION_MEMORY_1_INITIAL_ADDRESS, files.get(i));
            }
        }
        // Setting up the directories and assigning then to our data caches.
        Directory directory_0 = new Directory(16, 0, caches);
        Directory directory_1 = new Directory(8, 256, caches);
        dataCache_1.setLocalDirectory(directory_0);
        dataCache_1.setRemoteDirectory(directory_1);
        dataCache_2.setLocalDirectory(directory_0);
        dataCache_2.setRemoteDirectory(directory_1);
        dataCache_3.setLocalDirectory(directory_1);
        dataCache_3.setRemoteDirectory(directory_0);
        // Assigning the cores to their respective processor.
        DataCache[] caches_P0 = new DataCache[]{dataCache_1,dataCache_2};
        DataCache[] caches_P1 = new DataCache[]{dataCache_3};
        // Creating our processors.
        Processor processor_0 = new Processor(sharedMemory,
                instructionMemory_0,
                threadsP0,
                threadCountP0,
                quantum,
                "Processor 0",
                caches_P0
                );
        Processor processor_1 = new Processor(sharedMemory,
                instructionMemory_1,
                threadsP1,
                threadCountP1,
                quantum,
                "Processor 1",
                caches_P1);
        // Start the simulation.
        processor_0.start();
        processor_1.start();
    }

    /**
     * Method that reads from a ".txt" file and turn into instructions that are then save in the instruction memory,
     * with it's respective pc.
     * @param instructionMemory Instruction memory that will contain the program's instructions.
     * @param initialAddress Initial address of the program's instructions in our instruction memory.
     * @param fileToRead File that we are reading.
     * @return Returns a program thread.
     */
    private Thread setUpInstructions(InstructionMemory instructionMemory, int initialAddress, File fileToRead) {
        Thread thread = null;
        FileReader fr = null;
        BufferedReader br = null;
        int actualAddress = initialAddress;
        try {
            fr = new FileReader (fileToRead);
            br = new BufferedReader(fr);
            // Read from file.
            String line;
            Vector<Integer> instructions = new Vector<Integer>();
            while( (line=br.readLine())!= null) {
                String[] vecInt = line.split(" ");
                Instruction instruction = new Instruction(Integer.parseInt(vecInt[0]), Integer.parseInt(vecInt[1]), Integer.parseInt(vecInt[2]), Integer.parseInt(vecInt[3]));
                instructionMemory.saveInstruction(instruction, actualAddress);
                instructions.add(actualAddress);
                actualAddress += 4;
            }
            // Our thread's name.
            String threadName = fileToRead.getName().substring(0,fileToRead.getName().indexOf('.'));
            thread = new Thread(initialAddress, instructions, threadName);
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
        return thread;
    }

}

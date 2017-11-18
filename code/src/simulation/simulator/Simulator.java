package simulation.simulator;

import simulation.block.instruction.Instruction;
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

public class Simulator {

    public Simulator(int coreCount, int quantum) {
        Clock.setCoreCount(coreCount);
        SharedMemory sharedMemory = new SharedMemory(24);
        InstructionMemory instructionMemory_0 = new InstructionMemory(24, Config.INSTRUCTION_MEMORY_0_INITIAL_ADDRESS);
        InstructionMemory instructionMemory_1 = new InstructionMemory(16, Config.INSTRUCTION_MEMORY_1_INITIAL_ADDRESS);

        Semaphore directoryLock_0 = new Semaphore(1);
        Directory directory_0 = new Directory(16, 0, directoryLock_0);
        Semaphore directoryLock_1 = new Semaphore(1);
        Directory directory_1 = new Directory(8, 0, directoryLock_1);

        File folder = new File("threads");
        ArrayList<File> files = new ArrayList<File>(Arrays.asList(folder.listFiles()));

        Thread[] threadsP0 = new Thread[folder.list().length*2/3];
        Thread[] threadsP1 = new Thread[folder.list().length-folder.list().length*2/3];

        int threadCountP0 = folder.list().length*2/3;
        int threadCountP1 = folder.list().length-threadCountP0;

        for (int i = 0; i < threadCountP0; i++) {
            if(i>0) {
                threadsP0[i] = setUpInstructions(instructionMemory_0, Config.INSTRUCTION_MEMORY_0_INITIAL_ADDRESS+threadsP0[i-1].getInstructions().size()*4, files.get(i));
            } else {
                threadsP0[i] = setUpInstructions(instructionMemory_0, Config.INSTRUCTION_MEMORY_0_INITIAL_ADDRESS, files.get(i));
            }
        }
        for (int i = threadCountP0; i < folder.list().length; i++) {
            if(i>threadCountP0) {
                threadsP1[i-threadCountP0] = setUpInstructions(instructionMemory_1, Config.INSTRUCTION_MEMORY_1_INITIAL_ADDRESS+threadsP1[i-1-threadCountP0].getInstructions().size()*4, files.get(i));
            } else {
                threadsP1[i-threadCountP0] = setUpInstructions(instructionMemory_1, Config.INSTRUCTION_MEMORY_1_INITIAL_ADDRESS, files.get(i));
            }
        }

        Semaphore dataCacheLock_0 = new Semaphore(1);
        Semaphore dataCacheLock_1 = new Semaphore(1);
        Semaphore dataCacheLock_2 = new Semaphore(1);

        Processor processor_0 = new Processor(sharedMemory,
                instructionMemory_0,
                directory_0,
                directory_1,
                threadsP0,
                Config.CORE_COUNT_P0,
                threadCountP0,
                Config.INSTRUCTION_MEMORY_0_INITIAL_ADDRESS,
                quantum,
                "Processor 0",
                new Semaphore[]{dataCacheLock_0,dataCacheLock_1},
                new Semaphore[]{dataCacheLock_2});

        Processor processor_1 = new Processor(sharedMemory,
                instructionMemory_1,
                directory_0,
                directory_1,
                threadsP1,
                Config.CORE_COUNT_P1,
                threadCountP1,
                Config.INSTRUCTION_MEMORY_1_INITIAL_ADDRESS,
                quantum,
                "Processor 1",
                new Semaphore[]{dataCacheLock_2},
                new Semaphore[]{dataCacheLock_0,dataCacheLock_1});

        processor_0.start();
        processor_1.start();
    }

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
            thread = new Thread(initialAddress, instructions);
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

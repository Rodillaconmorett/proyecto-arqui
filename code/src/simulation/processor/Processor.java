package simulation.processor;

import simulation.Config;
import simulation.block.instruction.Instruction;
import simulation.cache.dataCache.DataCache;
import simulation.cache.dataCache.impl.DataCache_P0;
import simulation.cache.instructionCache.InstructionCache;
import simulation.core.Core;
import simulation.directory.directory.Directory;
import simulation.instructionMemory.InstructionMemory;
import simulation.sharedMemory.SharedMemory;
import simulation.thread.Thread;

import java.util.List;
import java.util.concurrent.Semaphore;

public class Processor {

    private Core[] cores;
    private Thread[] threads;
    private List<Integer> indexThreads;
    private InstructionCache instructionCache;
    private Semaphore instructionBus;
    private Semaphore[] threadSem;
    private Semaphore memoryBus;
    private DataCache dataCache;
    private String procesorName;

    public Processor(SharedMemory sharedMemory,
                     InstructionMemory instructionMemory,
                     Directory localDirectory,
                     Directory remoteDirectory,
                     Thread[] threads,
                     int coreCount,
                     int threadCount,
                     int initialAddressInst,
                     int quantum,
                     String procesorName) {
        this.instructionBus = new Semaphore(1);
        this.procesorName = procesorName;
        this.threadSem= new Semaphore[threadCount];
        for (int i = 0; i < threadCount; i++) {
            this.threadSem[i] = new Semaphore(1);
        }
        this.instructionCache = new InstructionCache(instructionBus, instructionMemory);
        this.memoryBus = new Semaphore(1);
        this.dataCache = new DataCache_P0();
        this.threads = threads;
        cores = new Core[coreCount];
        for (int i = 0; i < coreCount; i++) {
            cores[i] = new Core(instructionCache, dataCache, quantum, threads, threadSem, memoryBus, procesorName+": Core: "+i);
            Config.threads.add(cores[i]);
        }
    }

    public void start() {
        for (int i = 0; i < cores.length; i++) {
            cores[i].start();
        }

    }
}
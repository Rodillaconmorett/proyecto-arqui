package simulation.processor;

import simulation.Config;
import simulation.block.instruction.Instruction;
import simulation.cache.dataCache.DataCache;
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
    private DataCache[] caches;
    private Thread[] threads;
    private InstructionCache instructionCache;
    private Semaphore instructionBus;
    private Semaphore[] threadSem;
    private String processorName;

    public Processor(SharedMemory sharedMemory,
                     InstructionMemory instructionMemory,
                     Thread[] threads,
                     int threadCount,
                     int quantum,
                     String processorName,
                     DataCache[] caches) {
        this.caches = caches;
        this.instructionBus = new Semaphore(1);
        this.processorName = processorName;
        this.threadSem = new Semaphore[threadCount];
        for (int i = 0; i < threadCount; i++) {
            this.threadSem[i] = new Semaphore(1);
        }
        this.threads = threads;

        switch (this.processorName){
            case "Processor 0":
                InstructionCache instructionCache_0 = new InstructionCache(instructionMemory);
                cores = new Core[2];
                cores[0] = new Core(instructionCache_0,
                        caches[0],
                        quantum,
                        threads,
                        threadSem,
                        processorName+": Core: 0");
                InstructionCache instructionCache_1 = new InstructionCache(instructionMemory);
                cores[1] = new Core(instructionCache_1,
                        caches[1],
                        quantum,
                        threads,
                        threadSem,
                        processorName+": Core: 1");
                break;
            case "Processor 1":
                cores = new Core[1];
                InstructionCache instructionCache_2 = new InstructionCache(instructionMemory);
                cores[0] = new Core(instructionCache_2,
                        caches[0],
                        quantum,
                        threads,
                        threadSem,
                        processorName+": Core: 0");
                break;
        }
    }

    public void start() {
        for (int i = 0; i < cores.length; i++) {
            cores[i].start();
        }

    }
}
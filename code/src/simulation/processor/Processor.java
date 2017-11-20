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

/**
 * Our representation of the processors of our computer.
 */
public class Processor {
    /// Cores that belong to our processor.
    private Core[] cores;
    /// Data caches that that correspond to each core.
    private DataCache[] caches;
    /// Threads that will be executed in this processor.
    private Thread[] threads;
    /// Instruction caches that correspond to each core.
    private InstructionCache instructionCache;
    /// Instruction bus.
    private Semaphore instructionBus;
    /// Semaphores that belong to each program thread.
    private Semaphore[] threadSem;
    /// Name of the processor.
    private String processorName;

    /**
     * Default constructor.
     * @param sharedMemory Shared memory.
     * @param instructionMemory Instruction memory.
     * @param threads Program threads that will be executed.
     * @param threadCount Number of programs that will run in our processor.
     * @param quantum Quantum given to each thread, so that they can finish their instructions.
     * @param processorName Name of our processor.
     * @param caches Our caches.
     */
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
        // Different set ups for each one of our processors.
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

    /**
     * Start the execution of our cores.
     */
    public void start() {
        for (int i = 0; i < cores.length; i++) {
            cores[i].start();
        }

    }
}
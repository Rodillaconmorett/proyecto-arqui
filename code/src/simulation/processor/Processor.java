package simulation.processor;

import simulation.cache.dataCache.DataCache;
import simulation.cache.dataCache.impl.DataCache_P0;
import simulation.cache.instructionCache.InstructionCache;
import simulation.core.Core;
import simulation.core.impl.CoreImpl;
import simulation.directory.directory.Directory;
import simulation.instructionMemory.InstructionMemory;
import simulation.sharedMemory.SharedMemory;
import simulation.thread.Thread;

import java.util.concurrent.Semaphore;

public class Processor {

    private Core[] cores;

    public Processor(SharedMemory sharedMemory,
                     InstructionMemory instructionMemory,
                     Directory localDirectory,
                     Directory remoteDirectory,
                     int coreCount) {
        Semaphore instructionBus = new Semaphore(1);
        InstructionCache instructionCache = new InstructionCache(instructionBus, instructionMemory);
        Semaphore memoryBus = new Semaphore(1);
        DataCache dataCache = new DataCache_P0();
        cores = new Core[coreCount];
        for (int i = 0; i < cores.length; i++) {
            //TODO
            //New cores
        }
    }



}

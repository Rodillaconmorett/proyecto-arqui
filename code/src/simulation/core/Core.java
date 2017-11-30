package simulation.core;

import simulation.Config;
import simulation.SafePrint;
import simulation.block.instruction.Instruction;
import simulation.cache.dataCache.DataCache;
import simulation.cache.instructionCache.InstructionCache;
import simulation.clock.Clock;
import simulation.thread.Thread;

import java.util.concurrent.Semaphore;

public class Core extends java.lang.Thread {

    ///
    private InstructionCache instructionCache;
    private DataCache dataCache;
    private String coreName;
    private int pcRegister;
    private int coreCount;
    private int currentThreadIndex;
    private int currentContextIndex;
    private int[] registers;
    private int quantum;
    private int quantumLeftCycles;
    private Thread[] threads;
    private Semaphore[] threadSem;

    /**
     * Builds a new core.
     * @param instructionCache The instruction cache assigned to the core.
     * @param dataCache The data cache assigned to the core.
     * @param quantum The quantum of the entire simulation.
     * @param threads The threads assigned to the processor that owns the core.
     * @param threadSem An array of locks that protect each thread of the treads param.
     * @param coreName The name of the core.
     */
    public Core(InstructionCache instructionCache,
                DataCache dataCache,
                int coreCount,
                int quantum,
                Thread[] threads,
                Semaphore[] threadSem,
                String coreName) {
        this.instructionCache = instructionCache;
        this.coreCount = coreCount;
        this.dataCache = dataCache;
        this.quantum = quantum;
        this.quantumLeftCycles = this.quantum;
        this.threads = threads;
        this.coreName = coreName;
        this.currentThreadIndex = 0;
        this.currentContextIndex = -1;
        this.threadSem = threadSem;
    }


    /**
     * Tries to run the assigned threads and stops when all threads were completely executed.
     */
    @Override
    public void run() {
        while (true) {
            if (threadSem[currentThreadIndex].tryAcquire() && !threads[currentThreadIndex].isFinished()) {
                while (true) {
                    registers = threads[currentThreadIndex].getRegisters();
                    pcRegister = threads[currentThreadIndex].getPc();
                    Instruction instruction = read(pcRegister);
                    if (instruction.getTypeOfInstruction() == 63) {
                        if (Config.DISPLAY_INFO) {
                            printInstruction(instruction, pcRegister);
                        }
                        threads[currentThreadIndex].saveContext(registers, pcRegister);
                        if (Config.DISPLAY_REGISTER) {
                            SafePrint.printRegisters(threads[currentThreadIndex], coreName);
                        }
                        threads[currentThreadIndex].setFinished(true);
                        currentThreadIndex = (currentThreadIndex + 1) % threads.length;
                        Clock.executeBarrier();
                        break;
                    } else {
                        switch (instruction.getTypeOfInstruction()) {
                            case 2:
                                ExecuteJR(instruction);
                                break;
                            case 3:
                                ExecuteJAL(instruction);
                                break;
                            case 4:
                                ExecuteBEQZ(instruction);
                                break;
                            case 5:
                                ExecuteBNEZ(instruction);
                                break;
                            case 8:
                                ExecuteDADDI(instruction);
                                break;
                            case 12:
                                ExecuteDMUL(instruction);
                                break;
                            case 14:
                                ExecuteDDIV(instruction);
                                break;
                            case 32:
                                ExecuteDADD(instruction);
                                break;
                            case 34:
                                ExecuteDSUB(instruction);
                                break;
                            case 35:
                                ExecuteLW(instruction);
                                break;
                            case 43:
                                ExecuteSW(instruction);
                                break;
                        }
                        if (quantumLeftCycles < 1) {
                            quantumLeftCycles = quantum;
                            threads[currentThreadIndex].saveContext(registers, pcRegister);
                            threadSem[currentThreadIndex].release();
                            currentThreadIndex = (currentThreadIndex + 1) % threads.length;
                            break;
                        }
                        threads[currentThreadIndex].saveContext(registers, pcRegister);
                    }
                }
            } else {
                if(coreCount > 1) {
                    int counter = 0;
                    for (int i = 0; i < threads.length; i++) {
                        if(!threads[i].isFinished()) {
                            counter++;
                        }
                    }
                    if (threadsDidFinished()) {
                        coreCount--;
                        Clock.reduceCoreCount();
                        return;
                    } else {
                        Clock.executeBarrier();
                    }
                } else {
                    if (threadsDidFinished()){
                        Clock.reduceCoreCount();
                        System.out.println("Estado de la memoria compartida al finalizar el core "+coreName+":");
                        for (int i = 0; i < 24; i++) {
                            for (int j = 0; j < 4; j++) {
                                System.out.print(dataCache.sharedMemory.getDataBlock(i).getData()[j] + " ");
                            }
                            System.out.println();
                        }
                        return;
                    } else {
                        Clock.executeBarrier();
                    }
                }
                currentThreadIndex = (currentThreadIndex + 1) % threads.length;
            }
        }
    }


    /**
     * Reads the address from the instruction address.
     *
     * @param addressInstruction Address of the instruction we want to read.
     * @return Instruction to be executed.
     */
    private Instruction read(int addressInstruction) {
        Instruction instruction = null;
        do {
            instruction = instructionCache.readInstruction(addressInstruction, coreName);
            if (instruction == null) {
                Clock.executeBarrier();
                quantumLeftCycles--;
            }
        }
        while (instruction == null);
        quantumLeftCycles -= instructionCache.getUsedCyclesOfLastRead();
        return instruction;
    }

    /**
     * Executes the 'DADDI' instruction.
     * @param instruction The container of the parameters that the instruction gonna read/write.
     */
    private void ExecuteDADDI(Instruction instruction) {
        if(Config.DISPLAY_INFO) {
            printInstruction(instruction, pcRegister);
        }
        registers[instruction.getSecondParameter()] = registers[instruction.getFirstParameter()] + instruction.getThirdParameter();
        Clock.executeBarrier();
        quantumLeftCycles--;
        pcRegister+=4;
    }

    /**
     * Executes the 'DADD' instruction.
     * @param instruction The container of the parameters that the instruction gonna read/write.
     */
    private void ExecuteDADD(Instruction instruction) {
        if(Config.DISPLAY_INFO) {
            printInstruction(instruction, pcRegister);
        }
        registers[instruction.getThirdParameter()] = registers[instruction.getFirstParameter()] + registers[instruction.getSecondParameter()];
        Clock.executeBarrier();
        quantumLeftCycles--;
        pcRegister+=4;
    }

    /**
     * Executes the 'DSUB' instruction.
     * @param instruction The container of the parameters that the instruction gonna read/write.
     */
    private void ExecuteDSUB(Instruction instruction) {
        if(Config.DISPLAY_INFO) {
            printInstruction(instruction, pcRegister);
        }
        registers[instruction.getThirdParameter()] = registers[instruction.getFirstParameter()] - registers[instruction.getSecondParameter()];
        Clock.executeBarrier();
        quantumLeftCycles--;
        pcRegister+=4;
    }

    /**
     * Executes the 'DMUL' instruction.
     * @param instruction The container of the parameters that the instruction gonna read/write.
     */
    private void ExecuteDMUL(Instruction instruction) {
        if(Config.DISPLAY_INFO) {
            printInstruction(instruction, pcRegister);
        }
        registers[instruction.getThirdParameter()] = registers[instruction.getFirstParameter()] * registers[instruction.getSecondParameter()];
        Clock.executeBarrier();
        quantumLeftCycles--;
        pcRegister+=4;
    }

    /**
     * Executes the 'DDIV' instruction.
     * @param instruction The container of the parameters that the instruction gonna read/write.
     */
    private void ExecuteDDIV(Instruction instruction) {
        if(Config.DISPLAY_INFO) {
            printInstruction(instruction, pcRegister);
        }
        registers[instruction.getThirdParameter()] = registers[instruction.getFirstParameter()] / registers[instruction.getSecondParameter()];
        pcRegister+=4;
        Clock.executeBarrier();
        quantumLeftCycles--;
    }

    /**
     * Executes the 'BEQZ' instruction.
     * @param instruction The container of the parameters that the instruction gonna read/write.
     */
    private void ExecuteBEQZ(Instruction instruction) {
        if(Config.DISPLAY_INFO) {
            printInstruction(instruction, pcRegister);
        }
        if (registers[instruction.getFirstParameter()] == 0) {
            pcRegister += (instruction.getThirdParameter()*4);
        }
        pcRegister+=4;
        Clock.executeBarrier();
        quantumLeftCycles--;
    }

    /**
     * Executes the 'BNEZ' instruction.
     * @param instruction The container of the parameters that the instruction gonna read/write.
     */
    private void ExecuteBNEZ(Instruction instruction) {
        if(Config.DISPLAY_INFO) {
            printInstruction(instruction, pcRegister);
        }
        if (registers[instruction.getFirstParameter()] != 0) {
            pcRegister += (instruction.getThirdParameter()*4);
        }
        pcRegister+=4;
        Clock.executeBarrier();
        quantumLeftCycles--;
    }

    /**
     * Executes the 'JAL' instruction.
     * @param instruction The container of the parameters that the instruction gonna read/write.
     */
    private void ExecuteJAL(Instruction instruction) {
        if(Config.DISPLAY_INFO) {
            printInstruction(instruction, pcRegister);
        }
        pcRegister += 4;
        registers[31] = pcRegister;
        pcRegister = pcRegister + (instruction.getThirdParameter());
        Clock.executeBarrier();
        quantumLeftCycles--;
    }

    /**
     * Executes the 'JR' instruction.
     * @param instruction The container of the parameters that the instruction gonna read/write.
     */
    private void ExecuteJR(Instruction instruction) {
        if(Config.DISPLAY_INFO) {
            printInstruction(instruction, pcRegister);
        }
        pcRegister = registers[instruction.getFirstParameter()];
        Clock.executeBarrier();
        quantumLeftCycles--;
    }

    /**
     * Executes the 'LW' instruction.
     * @param instruction The container of the parameters that the instruction gonna read/write.
     */
    private void ExecuteLW(Instruction instruction) {
        Integer memoryRequired = null;
        do {
            while(!dataCache.getMyLock().tryAcquire()){
                Clock.executeBarrier();
                quantumLeftCycles--;
            }
            memoryRequired = dataCache
                    .getDataRequired(instruction.getThirdParameter() + registers[instruction.getFirstParameter()]);
            if (memoryRequired == null) {
                quantumLeftCycles -= dataCache.getUsedCyclesOfLastRead();
                dataCache.getMyLock().release();
                Clock.executeBarrier();
                quantumLeftCycles--;
            } else {
                quantumLeftCycles -= dataCache.getUsedCyclesOfLastRead();
                pcRegister += 4;
                dataCache.getMyLock().release();
                Clock.executeBarrier();
                quantumLeftCycles--;
            }
        } while (memoryRequired == null);
        Clock.executeBarrier();
        quantumLeftCycles--;
        registers[instruction.getSecondParameter()] = memoryRequired;
    }

    /**
     * Executes the 'SW' instruction.
     * @param instruction The container of the parameters that the instruction gonna read/write.
     */
    private void ExecuteSW(Instruction instruction) {
        boolean success;
            if(dataCache.getMyLock().tryAcquire()) {
                try {
                    success = dataCache.storeDate(registers[instruction.getSecondParameter()], registers[instruction.getFirstParameter()], instruction.getThirdParameter());
                    if(success) {
                        quantumLeftCycles -= dataCache.getUsedCyclesOfLastRead();
                        pcRegister += 4;
                    }
                } finally {
                    dataCache.getMyLock().release();
                    Clock.executeBarrier();
                    quantumLeftCycles--;
                }
            }
    }

    /**
     * Checks if all threads were executed completely.
     * @return True if and only if all threads are finished.
     */
    private boolean threadsDidFinished() {
        for(int i = 0; i < threads.length; i++) {
            if(!threads[i].isFinished()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Prints the instruction code and the current pc register.
     * @param instruction The container of the instruction core.
     * @param pc The pc that the instruction
     */
    private void printInstruction(Instruction instruction, int pc) {
        SafePrint.print(this.coreName +" Executed => "+instruction.print() + " | PC: " + pc);
    }

}

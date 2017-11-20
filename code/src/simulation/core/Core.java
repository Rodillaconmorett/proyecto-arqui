package simulation.core;

import simulation.Config;
import simulation.SafePrint;
import simulation.block.instruction.Instruction;
import simulation.cache.dataCache.DataCache;
import simulation.cache.instructionCache.InstructionCache;
import simulation.clock.Clock;
import simulation.thread.Thread;

import java.util.*;
import java.util.concurrent.Semaphore;

public class Core extends java.lang.Thread {

    ///
    private InstructionCache instructionCache;
    private DataCache dataCache;
    private String coreName;
    private int pcRegister;
    private int currentThreadIndex;
    private int currentContextIndex;
    private int[] registers;
    private int quantum;
    private int quantumLeftCycles;
    private Thread[] threads;
    private Semaphore[] threadSem;

    /**
     * Builds a new core
     *
     * @param instructionCache
     * @param dataCache
     * @param quantum
     * @param threads
     * @param threadSem
     */
    public Core(InstructionCache instructionCache,
                DataCache dataCache,
                int quantum,
                Thread[] threads,
                Semaphore[] threadSem,
                String coreName) {
        this.instructionCache = instructionCache;
        this.dataCache = dataCache;
        this.quantum = quantum;
        this.quantumLeftCycles = this.quantum;
        this.threads = threads;
        this.coreName = coreName;
        this.currentThreadIndex = 0;
        this.currentContextIndex = -1;
        this.threadSem = threadSem;
    }


    @Override
    public void run() {
        while (true) {
            if (threadSem[currentThreadIndex].tryAcquire() && !threads[currentThreadIndex].isFinished()) {
                while (true) {
                    if (currentThreadIndex != currentContextIndex) {
                        currentContextIndex = currentThreadIndex;
                        registers = threads[currentThreadIndex].getRegisters();
                        pcRegister = threads[currentThreadIndex].getPc();
                    }
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
                    }
                }
            } else {
                currentThreadIndex = (currentThreadIndex + 1) % threads.length;
                int counter = 0;
                for (int i = 0; i < threads.length; i++) {
                    if(!threads[i].isFinished()) {
                        counter++;
                    }
                }
                if (threadsDidFinished() || counter < 1){
                    Clock.reduceCoreCount();
                    break;
                } else {
                    Clock.executeBarrier();
                }
            }
        }
    }


    /**
     * Reads the instruction address
     *
     * @param addressInstruction
     * @return
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

    private void ExecuteDADDI(Instruction instruction) {
        if(Config.DISPLAY_INFO) {
            printInstruction(instruction, pcRegister);
        }
        registers[instruction.getSecondParameter()]
                = registers[instruction.getFirstParameter()]
                + instruction.getThirdParameter();
        Clock.executeBarrier();
        quantumLeftCycles--;
        pcRegister+=4;
    }

    private void ExecuteDADD(Instruction instruction) {
        if(Config.DISPLAY_INFO) {
            printInstruction(instruction, pcRegister);
        }
        registers[instruction.getThirdParameter()]
                = registers[instruction.getFirstParameter()]
                + registers[instruction.getSecondParameter()];
        Clock.executeBarrier();
        quantumLeftCycles--;
        pcRegister+=4;
    }

    private void ExecuteDSUB(Instruction instruction) {
        if(Config.DISPLAY_INFO) {
            printInstruction(instruction, pcRegister);
        }
        registers[instruction.getThirdParameter()]
                = registers[instruction.getFirstParameter()]
                - registers[instruction.getSecondParameter()];
        Clock.executeBarrier();
        quantumLeftCycles--;
        pcRegister+=4;
    }

    private void ExecuteDMUL(Instruction instruction) {
        if(Config.DISPLAY_INFO) {
            printInstruction(instruction, pcRegister);
        }
        registers[instruction.getThirdParameter()]
                = registers[instruction.getFirstParameter()]
                * registers[instruction.getSecondParameter()];
        Clock.executeBarrier();
        quantumLeftCycles--;
        pcRegister+=4;
    }

    private void ExecuteDDIV(Instruction instruction) {
        if(Config.DISPLAY_INFO) {
            printInstruction(instruction, pcRegister);
        }
        registers[instruction.getThirdParameter()]
                = registers[instruction.getFirstParameter()]
                / registers[instruction.getSecondParameter()];
        pcRegister+=4;
        Clock.executeBarrier();
        quantumLeftCycles--;
    }

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

    private void ExecuteJAL(Instruction instruction) {
        if(Config.DISPLAY_INFO) {
            printInstruction(instruction, pcRegister);
        }
        pcRegister+=4;
        registers[31] = pcRegister;
        pcRegister = pcRegister + (instruction.getThirdParameter());
        Clock.executeBarrier();
        quantumLeftCycles--;
    }

    private void ExecuteJR(Instruction instruction) {
        if(Config.DISPLAY_INFO) {
            printInstruction(instruction, pcRegister);
        }
        pcRegister = registers[instruction.getFirstParameter()];
        Clock.executeBarrier();
        quantumLeftCycles--;
    }

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

    private void ExecuteSW(Instruction instruction) {

    }

    private boolean threadsDidFinished() {
        for(int i = 0; i < threads.length; i++) {
            if(!threads[i].isFinished()) {
                return false;
            }
        }
        return true;
    }

    private void printInstruction(Instruction instruction, int pc) {
        SafePrint.print(this.coreName +" Executed => "+instruction.print() + " | PC: " + pc);
    }

}

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
    private Semaphore memoryBus;

    /**
     * Builds a new core
     *
     * @param instructionCache
     * @param dataCache
     * @param quantum
     * @param threads
     * @param threadSem
     * @param memoryBus
     */
    public Core(InstructionCache instructionCache,
                DataCache dataCache,
                int quantum,
                Thread[] threads,
                Semaphore[] threadSem,
                Semaphore memoryBus,
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
        this.memoryBus = memoryBus;
    }

    @Override
    public void run() {
        while (true) {
            if (threadSem[currentThreadIndex].tryAcquire()) {
                while (true) {
                    if (currentThreadIndex != currentContextIndex) {
                        if (currentContextIndex >= 0) {
                            threads[currentContextIndex].saveContext(registers, pcRegister);
                        }
                        currentContextIndex = currentThreadIndex;
                        registers = threads[currentThreadIndex].getRegisters();
                        pcRegister = threads[currentThreadIndex].getPc();
                    }
                    int addressInstruction = threads[currentThreadIndex].getInstruction(pcRegister);
                    if (addressInstruction < 0) {
                        threads[currentThreadIndex].saveContext(registers, pcRegister);
                        currentThreadIndex = (currentThreadIndex + 1) % threads.length;
                    } else {
                        Instruction instruction = read(addressInstruction);
                        if (instruction.getTypeOfInstruction() == 63) {
                            int initialPC = threads[currentThreadIndex].getInitialPc();
                            int pcExecuted = (pcRegister-initialPC)*4;
                            if(Config.DISPLAY_INFO) {
                                printInstruction(instruction, pcExecuted+initialPC);
                            }
                            threads[currentThreadIndex].saveContext(registers, pcRegister);
                            for (int i = 0; i <threads[currentThreadIndex].getRegisters().length ; i++) {
                                    System.out.println(coreName+ ": Thread: "+ currentThreadIndex+" => R"+i+": "+threads[currentThreadIndex].getRegisters()[i]);
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
                                threadSem[currentThreadIndex].release();
                                currentThreadIndex = (currentThreadIndex + 1) % threads.length;
                                quantumLeftCycles = quantum;
                                break;
                            }
                        }
                    }
                }
            }else{
                currentThreadIndex = (currentThreadIndex + 1) % threads.length;
                int counter = 0;
                for (Thread thread : threads) {
                    if (!thread.isFinished()) {
                        counter++;
                    }
                }
                if (threadsDidFinished() || counter < 1){
                    Clock.setCoreCount(Clock.getCoreCount()-1);
                    Clock.executeBarrier();
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
        int initialPC = threads[currentThreadIndex].getInitialPc();
        int pcExecuted = (pcRegister-initialPC)*4;
        if(Config.DISPLAY_INFO) {
            printInstruction(instruction, pcExecuted+initialPC);
        }
        registers[instruction.getSecondParameter()]
                = registers[instruction.getFirstParameter()]
                + instruction.getThirdParameter();
        Clock.executeBarrier();
        quantumLeftCycles--;
        pcRegister++;
    }

    private void ExecuteDADD(Instruction instruction) {
        int initialPC = threads[currentThreadIndex].getInitialPc();
        int pcExecuted = (pcRegister-initialPC)*4;
        if(Config.DISPLAY_INFO) {
            printInstruction(instruction, pcExecuted+initialPC);
        }
        registers[instruction.getThirdParameter()]
                = registers[instruction.getFirstParameter()]
                + registers[instruction.getSecondParameter()];
        Clock.executeBarrier();
        quantumLeftCycles--;
        pcRegister++;
    }

    private void ExecuteDSUB(Instruction instruction) {

        int initialPC = threads[currentThreadIndex].getInitialPc();
        int pcExecuted = (pcRegister-initialPC)*4;
        if(Config.DISPLAY_INFO) {
            printInstruction(instruction, pcExecuted+initialPC);
        }

        registers[instruction.getThirdParameter()]
                = registers[instruction.getFirstParameter()]
                - registers[instruction.getSecondParameter()];
        Clock.executeBarrier();
        quantumLeftCycles--;
        pcRegister++;
    }

    private void ExecuteDMUL(Instruction instruction) {
        int initialPC = threads[currentThreadIndex].getInitialPc();
        int pcExecuted = (pcRegister-initialPC)*4;
        if(Config.DISPLAY_INFO) {
            printInstruction(instruction, pcExecuted+initialPC);
        }
        registers[instruction.getThirdParameter()]
                = registers[instruction.getFirstParameter()]
                * registers[instruction.getSecondParameter()];
        Clock.executeBarrier();
        quantumLeftCycles--;
        pcRegister++;
    }

    private void ExecuteDDIV(Instruction instruction) {
        int initialPC = threads[currentThreadIndex].getInitialPc();
        int pcExecuted = (pcRegister-initialPC)*4;
        if(Config.DISPLAY_INFO) {
            printInstruction(instruction, pcExecuted+initialPC);
        }
        registers[instruction.getThirdParameter()]
                = registers[instruction.getFirstParameter()]
                / registers[instruction.getSecondParameter()];
        pcRegister++;
        Clock.executeBarrier();
        quantumLeftCycles--;
    }

    private void ExecuteBEQZ(Instruction instruction) {
        int initialPC = threads[currentThreadIndex].getInitialPc();
        int pcExecuted = (pcRegister-initialPC)*4;
        if(Config.DISPLAY_INFO) {
            printInstruction(instruction, pcExecuted+initialPC);
        }
        if (registers[instruction.getFirstParameter()] == 0) {
            pcRegister += instruction.getThirdParameter();
        }
        pcRegister++;
        Clock.executeBarrier();
        quantumLeftCycles--;
    }

    private void ExecuteBNEZ(Instruction instruction) {
        int initialPC = threads[currentThreadIndex].getInitialPc();
        int pcExecuted = (pcRegister-initialPC)*4;
        if(Config.DISPLAY_INFO) {
            printInstruction(instruction, pcExecuted+initialPC);
        }
        if (registers[instruction.getFirstParameter()] != 0) {
            pcRegister += instruction.getThirdParameter();

        }
        pcRegister++;
        Clock.executeBarrier();
        quantumLeftCycles--;
    }

    private void ExecuteJAL(Instruction instruction) {
        int initialPC = threads[currentThreadIndex].getInitialPc();
        int pcExecuted = (pcRegister-initialPC)*4;
        if(Config.DISPLAY_INFO) {
            printInstruction(instruction, pcExecuted+initialPC);
        }
        registers[31] = pcRegister + 1;
        pcRegister++;
        pcRegister = pcRegister + (instruction.getThirdParameter() / 4);
        Clock.executeBarrier();
        quantumLeftCycles--;
    }

    private void ExecuteJR(Instruction instruction) {
        int initialPC = threads[currentThreadIndex].getInitialPc();
        int pcExecuted = (pcRegister-initialPC)*4;
        if(Config.DISPLAY_INFO) {
            printInstruction(instruction, pcExecuted+initialPC);
        }
        pcRegister = registers[instruction.getFirstParameter()];
        Clock.executeBarrier();
        quantumLeftCycles--;
    }

    private void ExecuteLW(Instruction instruction) {
        Integer memoryRequired = null;
        do {
            //memoryRequired = dataCache;
        }
        while (memoryRequired == null);
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

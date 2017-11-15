package simulation.core;

import simulation.block.instruction.Instruction;
import simulation.cache.dataCache.DataCache;
import simulation.cache.instructionCache.InstructionCache;
import simulation.clock.Clock;
import simulation.thread.Thread;

import java.util.*;

public class Core extends java.lang.Thread {

    ///
    private InstructionCache instructionCache;
    private DataCache dataCache;
    private int pcRegister;
    private int currentThreadIndex;
    private int currentContextIndex;
    private int[] registers;
    private int quantum;
    private int quantumLeftCycles;
    private List<Thread> threads;

    /**
     * Builds a new core
     * @param instructionCache
     * @param dataCache
     * @param quantum
     * @param threads
     */
    public Core(InstructionCache instructionCache,
                    DataCache dataCache,
                    int quantum,
                    Thread[] threads
    ) {
        this.instructionCache = instructionCache;
        this.dataCache = dataCache;
        this.quantum = quantum;
        this.quantumLeftCycles = this.quantum;
        this.threads = new ArrayList();
        Collections.addAll(this.threads, threads);
        this.currentThreadIndex = 0;
        this.currentContextIndex = -1;
    }

    @Override
    public void run() { while (!threads.isEmpty()) {
        if(currentThreadIndex != currentContextIndex){
            if(currentContextIndex >= 0){
                threads.get(currentContextIndex).saveContext(registers,pcRegister);
            }
            currentContextIndex = currentThreadIndex;
            registers = threads.get(currentThreadIndex).getRegisters();
            pcRegister = threads.get(currentThreadIndex).getPc();
            quantumLeftCycles = quantum;
        }
        int addressInstruction = threads.get(currentThreadIndex).getInstruction(pcRegister);
        if (addressInstruction < 0) {
            threads.get(currentThreadIndex).saveContext(registers,pcRegister);
            threads.remove(currentThreadIndex);
            currentContextIndex = -1;
        }
        else {
            Instruction instruction = read(addressInstruction);
            if (instruction.getTypeOfInstruction() == 63) {
                threads.get(currentThreadIndex).saveContext(registers,pcRegister);
                threads.remove(currentThreadIndex);
                for (int i = 0; i < 1; i++){
                    Clock.executeBarrier();
                }
                currentContextIndex = -1;
            }
            else {
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
                if(quantumLeftCycles < 1){
                    currentThreadIndex = (currentThreadIndex + 1) % threads.size();
                }
            }
        }
    }}

    /**
     * Reads the instruction address
     * @param addressInstruction
     * @return
     */
    private Instruction read(int addressInstruction) {
        Instruction instruction = null;
        do{
            instruction = instructionCache.readInstruction(addressInstruction);
            if(instruction == null){
                Clock.executeBarrier();
                quantumLeftCycles--;
            }
        }
        while(instruction == null);
        quantumLeftCycles -= instructionCache.getUsedCyclesOfLastRead();
        return instruction;
    }

    private void ExecuteDADDI(Instruction instruction) {
        registers[instruction.getSecondParameter()]
                = registers[instruction.getFirstParameter()]
                + instruction.getThirdParameter();
            Clock.executeBarrier();
            quantumLeftCycles--;
        //System.out.println(registers[instruction.getSecondParameter()]);
        pcRegister++;
    }

    private void ExecuteDADD(Instruction instruction) {
        registers[instruction.getThirdParameter()]
                = registers[instruction.getFirstParameter()]
                + registers[instruction.getSecondParameter()];
            Clock.executeBarrier();
            quantumLeftCycles--;
        pcRegister++;
    }

    private void ExecuteDSUB(Instruction instruction) {
        registers[instruction.getThirdParameter()]
                = registers[instruction.getFirstParameter()]
                - registers[instruction.getSecondParameter()];
            Clock.executeBarrier();
            quantumLeftCycles--;
        pcRegister++;
    }

    private void ExecuteDMUL(Instruction instruction) {
        registers[instruction.getThirdParameter()]
                = registers[instruction.getFirstParameter()]
                * registers[instruction.getSecondParameter()];
            Clock.executeBarrier();
            quantumLeftCycles--;
        pcRegister++;
    }

    private void ExecuteDDIV(Instruction instruction) {
        registers[instruction.getThirdParameter()]
                = registers[instruction.getFirstParameter()]
                / registers[instruction.getSecondParameter()];

            Clock.executeBarrier();
            quantumLeftCycles--;
        pcRegister++;
    }

    private void ExecuteBEQZ(Instruction instruction) {
        if(registers[instruction.getFirstParameter()] == 0){
            pcRegister = registers[instruction.getThirdParameter()];
        }
        else{
            pcRegister++;
        }
            Clock.executeBarrier();
            quantumLeftCycles--;
    }

    private void ExecuteBNEZ(Instruction instruction) {
        if(registers[instruction.getFirstParameter()] != 0){
            pcRegister = registers[instruction.getThirdParameter()];
        }
        else{
            pcRegister++;
        }
            Clock.executeBarrier();
            quantumLeftCycles--;
    }

    private void ExecuteJAL(Instruction instruction) {
        registers[31] = pcRegister;
        pcRegister += registers[instruction.getFirstParameter()];
            Clock.executeBarrier();
            quantumLeftCycles--;
    }

    private void ExecuteJR(Instruction instruction) {
        pcRegister = registers[instruction.getFirstParameter()];
            Clock.executeBarrier();
            quantumLeftCycles--;
    }

    private void ExecuteLW(Instruction instruction) {
        Integer memoryRequired = null;
        do{
            //memoryRequired = dataCache;
        }
        while(memoryRequired == null);
    }

    private void ExecuteSW(Instruction instruction) {

    }
}

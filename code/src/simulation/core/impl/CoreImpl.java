package simulation.core.impl;

import simulation.block.instruction.Instruction;
import simulation.cache.dataCache.DataCache;
import simulation.cache.instructionCache.InstructionCache;
import simulation.clock.Clock;
import simulation.core.Core;
import simulation.thread.Thread;

public class CoreImpl extends java.lang.Thread implements Core {

    ///
    private InstructionCache instructionCache;
    private DataCache dataCache;
    private int pcRegister;
    private int currentThreadIndex;
    private int currentContextIndex;
    private int[] registers;
    private int quantum;
    private int quantumLeftCycles;
    private Thread[] threads;

    /**
     * Builds a new core
     * @param instructionCache
     * @param dataCache
     * @param quantum
     * @param threads
     */
    public CoreImpl(InstructionCache instructionCache,
                    DataCache dataCache,
                    int quantum,
                    Thread[] threads
                    ) {
        this.instructionCache = instructionCache;
        this.dataCache = dataCache;
        this.quantum = quantum;
        this.quantumLeftCycles = this.quantum;
        this.threads = threads;
        this.currentThreadIndex = 0;
        this.currentContextIndex = -1;
    }

    @Override
    public void run() {
        execute();
    }

    @Override
    public void execute() {
        if(currentThreadIndex < threads.length){
            if(currentThreadIndex != currentContextIndex){
                if(currentContextIndex >= 0){
                    threads[currentContextIndex].saveContext(registers,pcRegister);
                }
                currentContextIndex = currentThreadIndex;
                registers = threads[currentThreadIndex].getRegisters();
                pcRegister = threads[currentThreadIndex].getPc();
                quantumLeftCycles = quantum;
            }
            int indexInstruction = threads[currentThreadIndex].getInstruction(pcRegister);
            Instruction instruction = read(indexInstruction);
            switch (instruction.getTypeOfInstruction()) {
                case 8:
                    ExecuteDADDI(instruction);
                    break;
                case 32:
                    ExecuteDADD(instruction);
                    break;
                case 34:
                    ExecuteDSUB(instruction);
                    break;
                case 12:
                    ExecuteDMUL(instruction);
                    break;
                case 14:
                    ExecuteDDIV(instruction);
                    break;
                case 4:
                    ExecuteBEQZ(instruction);
                    break;
                case 5:
                    ExecuteBNEZ(instruction);
                    break;
                case 3:
                    ExecuteJAL(instruction);
                    break;
                case 2:
                    ExecuteJR(instruction);
                    break;
                case 35:
                    ExecuteLW(instruction);
                    break;
                case 43:
                    ExecuteSW(instruction);
                    break;
                case 63:
                    ExecuteFIN(instruction);
                    break;
            }
            if(quantumLeftCycles < 1){
                currentThreadIndex
                        = (currentThreadIndex + 1) % threads.length;
            }
        }

    }

    /**
     * Reads the instruction index
     * @param indexInstruction
     * @return
     */
    private Instruction read(int indexInstruction) {
        Instruction instruction = null;
        do{
            instruction = instructionCache.readInstruction(indexInstruction);
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

    }

    private void ExecuteDADD(Instruction instruction) {

    }

    private void ExecuteDMUL(Instruction instruction) {

    }

    private void ExecuteDSUB(Instruction instruction) {

    }

    private void ExecuteLW(Instruction instruction) {

    }

    private void ExecuteFIN(Instruction instruction) {

    }

    private void ExecuteDDIV(Instruction instruction) {

    }

    private void ExecuteBEQZ(Instruction instruction) {

    }

    private void ExecuteBNEZ(Instruction instruction) {

    }

    private void ExecuteJAL(Instruction instruction) {

    }

    private void ExecuteJR(Instruction instruction) {

    }

    private void ExecuteSW(Instruction instruction) {

    }
}

package simulation.core.impl;

import simulation.block.instruction.Instruction;
import simulation.cache.dataCache.DataCache;
import simulation.cache.instructionCache.InstructionCache;
import simulation.core.Core;
import simulation.thread.Thread;

public class CoreImpl implements Core {

    ///
    private InstructionCache instructionCache;
    private DataCache dataCache;
    private int pcRegister;
    private int currentThread;
    private int currentContext;
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
        this.currentThread = 0;
        this.currentContext = -1;
    }

    @Override
    public void execute() {
        if(currentThread < threads.length){
            if(currentThread != currentContext){
                if(currentContext >= 0){

                }
            }
            int indexInstruction = 0;
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
        }

    }

    /**
     * Reads the instruction index
     * @param indexInstruction
     * @return
     */
    private Instruction read(int indexInstruction) {
        return null;
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

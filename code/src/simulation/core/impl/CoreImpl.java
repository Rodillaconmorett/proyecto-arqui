package simulation.core.impl;

import simulation.block.instruction.Instruction;
import simulation.cache.dataCache.DataCache;
import simulation.cache.instructionCache.InstructionCache;
import simulation.core.Core;
import simulation.thread.Thread;

public class CoreImpl implements Core {

    private InstructionCache instructionCache;
    private DataCache dataCache;
    private int pcRegister;
    private int quantum;
    private Thread[] threads;

    @Override
    public void execute(int rawInstruction) {

    }

    private Instruction read(int rawInstruction) {
        return null;
    }

    private void ExecuteBI(Instruction instruction) {

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

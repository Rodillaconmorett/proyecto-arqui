package simulation.cache.instructionCache;

import simulation.Config;
import simulation.SafePrint;
import simulation.block.instruction.Instruction;
import simulation.block.instructionBlock.InstructionBlock;
import simulation.clock.Clock;
import simulation.instructionMemory.InstructionMemory;

import java.util.concurrent.Semaphore;

public class InstructionCache {
    private Semaphore busInstruction;
    private InstructionMemory memory;
    private InstructionBlock[] cache;
    private int usedCycles;

    public InstructionCache(Semaphore busInstruction, InstructionMemory memory){
        this.busInstruction = busInstruction;
        this.memory = memory;
        this.cache = new InstructionBlock[4];
        for (int i = 0; i < 4; i++) {
            this.cache[i] = new InstructionBlock(-1);
        }
        this.usedCycles = 0;
    }

    public Instruction readInstruction(int addressInstruction, String coreName) {
        int blockAssigned = addressInstruction / 16;
        int positionCache = blockAssigned % 4;
        if(cache[positionCache].getNumBlock() != blockAssigned){
            if(!busInstruction.tryAcquire()) {
                return null;
            }
            InstructionBlock instructions = memory.getInstructionBlock(addressInstruction);
            cache[positionCache] = instructions;
            for (int i = 0; i < 16; i++) {
                if(Config.DISPLAY_INFO) {
                   SafePrint.print(coreName+": Waiting for memory. Cycle: " + i);
                }
                Clock.executeBarrier();
                usedCycles++;
            }
            busInstruction.release();
        }
        int wordPosition = (addressInstruction / 4) % 4;
        return cache[positionCache].getInstructions()[wordPosition];
    }

    public int getUsedCyclesOfLastRead(){
        int usedCycles = this.usedCycles;
        this.usedCycles = 0;
        return usedCycles;
    }
}

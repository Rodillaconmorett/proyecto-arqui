package simulation.cache.instructionCache;

import simulation.Config;
import simulation.SafePrint;
import simulation.block.instruction.Instruction;
import simulation.block.instructionBlock.InstructionBlock;
import simulation.clock.Clock;
import simulation.instructionMemory.InstructionMemory;

import java.util.concurrent.Semaphore;

/**
 * Cache of instruction.
 */
public class InstructionCache {
    /// Cache lock.
    private Semaphore busInstruction;
    /// Instruction memory.
    private InstructionMemory memory;
    /// Instructions located in our each cache position.
    private InstructionBlock[] cache;
    /// Number of used cycles in the last operation.
    private int usedCycles;

    /**
     * Default constructor.
     * @param memory Instruction memory managed by this cache.
     */
    public InstructionCache(InstructionMemory memory){
        this.busInstruction = new Semaphore(1);
        this.memory = memory;
        this.cache = new InstructionBlock[4];
        for (int i = 0; i < 4; i++) {
            this.cache[i] = new InstructionBlock(-1);
        }
        this.usedCycles = 0;
    }

    /**
     * Read an address from the instruction memory and return it's value.
     * @param addressInstruction Address of the instruction.
     * @param coreName Name of the core asking for the instruction.
     * @return Instruction.
     */
    public Instruction readInstruction(int addressInstruction, String coreName) {
        int blockAssigned = addressInstruction / 16;
        int positionCache = blockAssigned % 4;
        if(cache[positionCache].getNumBlock() != blockAssigned){
            if(!busInstruction.tryAcquire()) {
                return null;
            }
            if(memory.getMyLock().tryAcquire()) {
                try {
                    for (int i = 0; i < 16; i++) {
                        if (Config.DISPLAY_INFO) {
                            SafePrint.print(coreName + ": Waiting for memory. Cycle: " + i);
                        }
                        Clock.executeBarrier();
                        usedCycles++;
                    }
                    InstructionBlock instructions = memory.getInstructionBlock(addressInstruction);
                    cache[positionCache] = instructions;
                } finally {
                    memory.getMyLock().release();
                    Clock.executeBarrier();
                    usedCycles++;
                }
            } else {
                busInstruction.release();
                Clock.executeBarrier();
                usedCycles++;
                return null;
            }
            busInstruction.release();
            Clock.executeBarrier();
            usedCycles++;
        }
        int wordPosition = (addressInstruction / 4) % 4;
        return cache[positionCache].getInstructions()[wordPosition];
    }

    /**
     * Return the number of cycles used to read an instruction.
     * @return Number of cycles that were used.
     */
    public int getUsedCyclesOfLastRead(){
        int usedCycles = this.usedCycles;
        this.usedCycles = 0;
        return usedCycles;
    }
}

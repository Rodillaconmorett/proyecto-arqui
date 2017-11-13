package simulation.directory.directory;

import simulation.directory.directoryInput.DirectoryInput;
import simulation.states.BlockState;

import java.util.concurrent.Semaphore;

/**
 * Our directory, that manages a given number of blocks in shared memory.
 */
public class Directory {
    /// Lock that will prevent other caches from using it, if another one is using it.
    private Semaphore directoryLock;
    /// Directory's entrees.
    private DirectoryInput[] inputs;
    /// Initial address of the first block in shared memory that this directory manages.
    private int initialAddress;

    public Directory(int inputCount, int initialAddress, Semaphore lock) {
        directoryLock = lock;
        initialAddress = initialAddress;
        inputs = new DirectoryInput[inputCount];
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = new DirectoryInput();
        }
    }

    public BlockState findState(int address) {
        int finalAddress = (address-initialAddress)/4;
        return inputs[finalAddress].getState();
    }

    public DirectoryInput getCachesState(int address) {
        int finalAddress = (address-initialAddress)/4;
        return inputs[finalAddress];
    }

    public void setCacheState(int address, boolean cache_0, boolean cache_1, boolean cache_2, BlockState newState) {
        int finalAddress = (address-initialAddress)/4;
        inputs[finalAddress].setState(newState);
        inputs[finalAddress].setCacheState(0,cache_0);
        inputs[finalAddress].setCacheState(1,cache_1);
        inputs[finalAddress].setCacheState(2,cache_2);
    }

}

package simulation;

import java.util.ArrayList;

/**
 * This class will hold the simulation's settings.
 */
public class Config {
    // Display information at the end of each instruction.
    public static boolean DISPLAY_INFO = true;
    // Let out program know if at the end of each cycle, we need to stop for the a user input.
    public static boolean DISPLAY_CYCLE_END = false;
    // At the end of each thread, this variable will define if we either show or not the register results.
    public static boolean DISPLAY_REGISTER = false;
    // Initial address of our Instruction Memory 0.
    public static final int INSTRUCTION_MEMORY_0_INITIAL_ADDRESS = 256;
    // Initial address of our Instruction Memory 1.
    public static final int INSTRUCTION_MEMORY_1_INITIAL_ADDRESS = 128;
    // Number of cores that our first processor will have.
    public static final int CORE_COUNT_P0 = 2;
    // Number of cores that our second processor will have.
    public static final int CORE_COUNT_P1 = 1;
    // Our simulation threads.
    public static ArrayList<Thread> threads = new ArrayList<Thread>();
    // Private constructor. (Singleton pattern).
    private Config(){};
}

package simulation.clock;

import java.util.concurrent.*;

/**
 * Class in charge of maintaining the concurrency of the different cores of our simulation.
 */
public class Clock {

    /// Number of cores that will be run in our simulation.
    private static int coreCount = 0;
    /// Number of cores that already finished a instruction.
    private static int counter = 0;
    /// Lock that will prevent concurency problems regarding variables' modifications.
    private static final Semaphore mutex = new Semaphore(1);
    /// Barrier that prevent our different cores from continuing, if other cores are yet to finish a instruction.
    private static final Semaphore barrier = new Semaphore(1);
    /// Number if cycles that occurred since the beginning of our simulation.
    private static int cycle = 0;
    /// Instance of our clock.
    private static Clock instance = null;
    /// Private constructor. Singleton patterns must always have one.
    private Clock() {}
    /// Create a new instance of our clock.
    public static Clock getInstance() {
        if(instance == null){
            instance = new Clock();
        }
        return instance;
    }

    /**
     * Set the number of cores that we will work with.
     * @param max Number of cores.
     */
    public static void setCoreCount(int max) {
        coreCount = max;
    }

    /**
     * Returns the number of cores that we are working with.
     * @return Number of cores.
     */
    public static int getCoreCount() {
        return coreCount;
    }

    /**
     * Returns the cycle count, since the beginning of our simulation.
     * @return Cycles counted.
     */
    public static int getCycle() {
        return cycle;
    }

    /**
     * Reset the cycle count back to 0.
     */
    public static void resetCycle() {
        cycle = 0;
    }

    /**
     * Executes our barrier, so that no core can go on, if we have other running cores.
     */
    public static void executeBarrier() {
        try {
            // Increase counter.
            mutex.acquire();
            counter++;
            mutex.release();
            // Should the counter be less than our core count, we need to wait in our barrier.
            if(counter < coreCount) {
                // Acquire our barrier and wait for the last core to finish.
                barrier.acquire();
                // After all cores have finished, we must remember to reduce our counter.
                mutex.acquire();
                counter--;
                mutex.release();
            } else {
                // After the last core finishes, we must increase our cycle count and reduce our counter.
                mutex.acquire();
                cycle++;
                counter--;
                mutex.release();
            }
            // Relese our barrier.
            barrier.release();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}

package simulation;

import simulation.thread.Thread;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

/*
* Synchronize prints and user inputs.
* */
public class SafePrint {

    /**
     * Private constructor.
     */
    private SafePrint(){};

    /**
     * Print a message.
     * @param message String to print.
     */
    public static void print(String message) {
        System.out.println(message);
    }

    /**
     * Print the registers of a thread. Synchronized to avoid other registers trying to print it's results,
     * while another is doing it.
     * @param thread Thread to print.
     * @param coreName Name of the core that is printing the registers.
     */
    public static synchronized void printRegisters(Thread thread, String coreName) {
        for (int i = 0; i < thread.getRegisters().length; i++) {
            System.out.println(coreName + ": Thread: " + thread.getName() + " => R" + i + ": " + thread.getRegisters()[i]);
        }
    }

    /**
     * Read an input from a user. Synchronized, so no other thread prints a message to console while
     * trying to read from console.
     * @param message Message to display as guide.
     * @return User input.
     */
    public static synchronized String readInput(String message) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(message);
        while (true) {
            String userInput = scanner.nextLine();
            return userInput;
        }
    }
}

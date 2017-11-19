package simulation;

import simulation.thread.Thread;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class SafePrint {

    private SafePrint(){};

    public static void print(String message) {
        System.out.println(message);
    }

    public static synchronized void printRegisters(Thread thread, String coreName) {
        for (int i = 0; i < thread.getRegisters().length; i++) {
            System.out.println(coreName + ": Thread: " + thread.getName() + " => R" + i + ": " + thread.getRegisters()[i]);
        }
    }

    public static synchronized String readInput(String message) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(message);
        while (true) {
            String userInput = scanner.nextLine();
            return userInput;
        }
    }
}

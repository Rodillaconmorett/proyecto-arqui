package simulation;


import java.io.Console;
import java.util.Scanner;

public class SafePrint {

    private SafePrint(){};

    public static void print(String message) {
        System.out.println(message);
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

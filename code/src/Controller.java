import simulation.Config;
import simulation.simulator.Simulator;

import java.util.ArrayList;
import java.util.Scanner;

public class Controller {

    private static int DEFAULT_VALUE = -1;

    /** Method that return an user selected integer, it makes sure that the input is actually an integer.
     *  It uses a primary message to tell the user what the input is about. Also receives a second message to tell the user what happened if the input was not an integer.
     *  In this case an empty string is considered the default value(defined in this class).
     *  @param primaryMessage Message that asking for a value.
     *  @param errorMessage Message that will be displayed in case of a bad entry.
     *  @return User input or our default value.
     */
    private static int receiveIntegerFromUser(String primaryMessage, String errorMessage) {
        Scanner scanner = new Scanner(System.in);
        System.out.println(primaryMessage);
        while(true) {
            String userInput = scanner.nextLine();
            try {
                return Integer.parseInt(userInput);
            }
            catch (Exception e) {
                if(e.getClass().getCanonicalName() == "java.lang.NumberFormatException") {
                    if (userInput.isEmpty()) {
                        return DEFAULT_VALUE;
                    }
                    System.out.println(errorMessage);
                }
                else
                    e.printStackTrace();
            }
        }
    }

    /**
     * Scane for a String input.
     * @param primaryMessage Displayed message.
     * @return User input.
     */
    private static String recieveStringFromUser(String primaryMessage) {
        Scanner scanner = new Scanner(System.in);
        System.out.println(primaryMessage);
        while(true) {
            String userInput = scanner.nextLine();
            return userInput;
        }
    }


    public static void main(String[] args) {
        //Introduction message. Just some explanation.
        System.out.println("Welcome! This is a simulation of a multiprocessor of MIPS threads.");
        System.out.println("**");
        System.out.println("Our simulatd computer will have two processors. One with two cores, and the other one with only one.");
        System.out.println("Given a set of simulated threads, each with several instructions to run.");
        System.out.println("We will simulate the execution of these threads.");
        //Let's ask for the quantum, alright.
        int quantum = -1;
        do {
            quantum = receiveIntegerFromUser("Please, select a quatum for our cores. It must be bigger than 20.", "Oh, sorry you must enter an integer.");
        } while(quantum < 20);
        //Now, let's see if the user would like to see every single execution.
        //This means, that every instruction executed, will display what it did and how long it took.
        boolean infoDisplay = false;
        String input = "";
        boolean check = false;
        do {
            input = recieveStringFromUser("Do you wish to see information at the end of each instruction? Y[YES] / N[NO]");
            if(input.contentEquals("Y") || input.contentEquals("y") || input.contentEquals("N") || input.contentEquals("n")) {
                check = true;
            } else {
                System.out.println("Please, choose either Y || N. Can also be in lower case.");
            }
        } while(!check);
        if(input.contentEquals("Y") || input.contentEquals("y")) {
            Config.DISPLAY_INFO = true;
        } else {
            Config.DISPLAY_INFO = false;
        }
        check = false;
        do {
            input = recieveStringFromUser("Do you wish to stop at the end of each cycle? Y[YES] / N[NO]");
            if(input.contentEquals("Y") || input.contentEquals("y") || input.contentEquals("N") || input.contentEquals("n")) {
                check = true;
            } else {
                System.out.println("Please, choose either Y || N. Can also be in lower case.");
            }
        } while(!check);
        if(input.contentEquals("Y") || input.contentEquals("y")) {
            Config.DISPLAY_CYCLE_END = true;
        } else {
            Config.DISPLAY_CYCLE_END = false;
        }
        do {
            input = recieveStringFromUser("Do you wish to see each threads' registers when it finishes? Y[YES] / N[NO]");
            if(input.contentEquals("Y") || input.contentEquals("y") || input.contentEquals("N") || input.contentEquals("n")) {
                check = true;
            } else {
                System.out.println("Please, choose either Y || N. Can also be in lower case.");
            }
        } while(!check);
        if(input.contentEquals("Y") || input.contentEquals("y")) {
            Config.DISPLAY_REGISTER = true;
        } else {
            Config.DISPLAY_REGISTER = false;
        }
        Simulator simulator = new Simulator(Config.CORE_COUNT_P1+Config.CORE_COUNT_P0, quantum);
        for(int i = 0; i < Config.threads.size(); i++) {
            try {
                Config.threads.get(i).join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Thanks for using our simulation! Bye.");
    }
}

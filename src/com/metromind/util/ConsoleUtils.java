package com.metromind.util;

import java.util.Scanner;

/**
 * Utility helper for formatted console rendering, menus, and input sanitization.
 */
public class ConsoleUtils {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BOLD = "\u001B[1m";

    public static void printHeader(String title) {
        System.out.println();
        System.out.println("================================================================================");
        System.out.println("   " + title.toUpperCase());
        System.out.println("================================================================================");
    }

    public static void printSubHeader(String subTitle) {
        System.out.println();
        System.out.println("--- " + subTitle + " ---");
    }

    public static void printDivider() {
        System.out.println("--------------------------------------------------------------------------------");
    }

    public static int readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("(!) Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("(!) Invalid number format. Please enter digits only.");
            }
        }
    }

    public static double readDouble(Scanner scanner, String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("(!) Value must be between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("(!) Invalid decimal number.");
            }
        }
    }

    public static String readNonEmptyString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("(!) Input cannot be empty. Please enter a valid string.");
        }
    }

    public static void pressEnterToContinue(Scanner scanner) {
        System.out.print("\nPress [Enter] to return to the menu...");
        scanner.nextLine();
    }
}

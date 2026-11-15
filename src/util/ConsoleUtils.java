package util;

import java.util.Scanner;

/**
 * ConsoleUtils -- Premium Terminal UI toolkit for MetroMind.
 *
 * Provides ANSI color support with automatic Windows compatibility detection,
 * Unicode box-drawing characters, styled message prefixes, and input helpers.
 *
 * Color/styling is automatically enabled on modern terminals (Windows Terminal,
 * PowerShell 7+, VS Code, IntelliJ). Set {@code ansiEnabled = false} to force
 * plain-text fallback for legacy CMD or piped output.
 *
 * Usage:
 *   ConsoleUtils.printHeader("ROUTE RESULTS");
 *   ConsoleUtils.printSuccess("Journey planned successfully.");
 *   ConsoleUtils.printError("Station not found.");
 *   ConsoleUtils.printWarning("Track closed for maintenance.");
 *   ConsoleUtils.printInfo("Tip: type part of the station name.");
 */
public class ConsoleUtils {

    // -------------------------------------------------------------------------
    // ANSI escape codes
    // -------------------------------------------------------------------------
    public static final String RESET   = "\u001B[0m";
    public static final String BOLD    = "\u001B[1m";
    public static final String DIM     = "\u001B[2m";
    public static final String CYAN    = "\u001B[36m";
    public static final String GREEN   = "\u001B[32m";
    public static final String YELLOW  = "\u001B[33m";
    public static final String RED     = "\u001B[31m";
    public static final String BLUE    = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String GRAY    = "\u001B[90m";
    public static final String WHITE   = "\u001B[97m";

    // Legacy aliases kept for backward compatibility
    public static final String ANSI_RESET  = RESET;
    public static final String ANSI_CYAN   = CYAN;
    public static final String ANSI_GREEN  = GREEN;
    public static final String ANSI_YELLOW = YELLOW;
    public static final String ANSI_RED    = RED;
    public static final String ANSI_BOLD   = BOLD;

    // -------------------------------------------------------------------------
    // Feature flags & detection
    // -------------------------------------------------------------------------
    /**
     * Set to false to suppress all ANSI codes and render clean plain text.
     * Auto-detected: enabled on Windows Terminal / PowerShell / VS Code / IntelliJ.
     */
    public static boolean ansiEnabled = detectAnsiSupport();

    /**
     * Set to false to force ASCII borders (+, -, =, |) instead of Unicode box characters.
     * Auto-detected based on stdout Charset encoding capability.
     */
    public static boolean boxDrawingSupported = detectUnicodeBoxSupport();

    /**
     * Whether the Indian Rupee symbol (\u20B9) can be encoded by stdout.
     * Falls back to "Rs." if unsupported.
     */
    public static boolean rupeeSupported = detectRupeeSupport();

    private static boolean detectAnsiSupport() {
        String noColor = System.getenv("NO_COLOR");
        String ci      = System.getenv("CI");
        String wt      = System.getenv("WT_SESSION");      // Windows Terminal
        String vscode  = System.getenv("TERM_PROGRAM");    // VS Code terminal
        String term    = System.getenv("TERM");

        if (noColor != null) return false;
        if (ci != null) return false;
        if (wt != null) return true;
        if ("vscode".equals(vscode)) return true;
        if (term != null && !term.equals("dumb")) return true;
        // Windows -- default true for modern PowerShell / Windows Terminal
        return true;
    }

    private static boolean detectUnicodeBoxSupport() {
        try {
            java.nio.charset.Charset cs = getStdoutCharset();
            return cs.newEncoder().canEncode("\u2550") && cs.newEncoder().canEncode("\u2500");
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean detectRupeeSupport() {
        try {
            java.nio.charset.Charset cs = getStdoutCharset();
            return cs.newEncoder().canEncode("\u20B9");
        } catch (Throwable t) {
            return false;
        }
    }

    private static java.nio.charset.Charset getStdoutCharset() {
        String[] encProps = {"stdout.encoding", "sun.stdout.encoding", "native.encoding", "file.encoding"};
        for (String prop : encProps) {
            String val = System.getProperty(prop);
            if (val != null && !val.trim().isEmpty()) {
                try {
                    return java.nio.charset.Charset.forName(val.trim());
                } catch (Exception ignored) {}
            }
        }
        return java.nio.charset.Charset.defaultCharset();
    }

    /** Returns the Rupee symbol or "Rs." fallback if the console encoding cannot print \u20B9 */
    public static String rupee() {
        return rupeeSupported ? "\u20B9" : "Rs.";
    }

    /** Returns bullet / middle dot or "." fallback */
    public static String dot() {
        return boxDrawingSupported ? "\u00B7" : "*";
    }

    // -------------------------------------------------------------------------
    // Core helpers
    // -------------------------------------------------------------------------

    /** Wraps {@code text} in the given ANSI color code, or returns plain text if disabled. */
    public static String colored(String text, String color) {
        if (!ansiEnabled) return text;
        return color + text + RESET;
    }

    /** Returns {@code text} wrapped in ANSI bold, or plain text if disabled. */
    public static String bold(String text) {
        if (!ansiEnabled) return text;
        return BOLD + text + RESET;
    }

    /** Returns {@code text} wrapped in dim/gray, or plain text if disabled. */
    public static String dim(String text) {
        if (!ansiEnabled) return text;
        return DIM + text + RESET;
    }

    // -------------------------------------------------------------------------
    // Structural print helpers
    // -------------------------------------------------------------------------

    // Box-drawing characters as Java Unicode escapes (safe for all file encodings)
    // \u2550 = double horizontal (=)   \u2554 = top-left (double)   \u2557 = top-right
    // \u255A = bottom-left             \u255D = bottom-right        \u2551 = double vertical
    private static final String DBL_H  = "\u2550"; // =
    private static final String DBL_TL = "\u2554"; // top-left
    private static final String DBL_TR = "\u2557"; // top-right
    private static final String DBL_BL = "\u255A"; // bottom-left
    private static final String DBL_BR = "\u255D"; // bottom-right
    private static final String DBL_V  = "\u2551"; // |

    // Single-line box chars
    private static final String SGL_H  = "\u2500"; // -
    private static final String SGL_V  = "\u2502"; // |
    private static final String SGL_TL = "\u250C"; // top-left
    private static final String SGL_TR = "\u2510"; // top-right
    private static final String SGL_BL = "\u2514"; // bottom-left
    private static final String SGL_BR = "\u2518"; // bottom-right
    private static final String SGL_LM = "\u251C"; // left mid-tee
    private static final String SGL_RM = "\u2524"; // right mid-tee

    /**
     * Prints a prominent cyan box-drawing header.
     */
    public static void printHeader(String title) {
        String line = repeat(dblH(), 74);
        String top  = dblTL() + line + dblTR();
        String bot  = dblBL() + line + dblBR();
        String pad  = padRight("   " + title.toUpperCase(), 74);
        String mid  = dblV()  + pad + dblV();

        System.out.println();
        System.out.println(ansiEnabled ? (BOLD + CYAN + top + RESET) : top);
        System.out.println(ansiEnabled ? (BOLD + CYAN + mid + RESET) : mid);
        System.out.println(ansiEnabled ? (BOLD + CYAN + bot + RESET) : bot);
    }

    /**
     * Prints a subtle cyan sub-section header.
     */
    public static void printSubHeader(String subTitle) {
        String label = "  " + subTitle + " ";
        int remaining = Math.max(0, 76 - label.length());
        String dashes = repeat(sglH(), remaining);
        String full = label + dashes;
        System.out.println();
        System.out.println(ansiEnabled ? (CYAN + full + RESET) : full);
    }

    /**
     * Prints a subtle gray horizontal rule.
     */
    public static void printDivider() {
        String line = "  " + repeat(sglH(), 74);
        System.out.println(ansiEnabled ? (GRAY + line + RESET) : line);
    }

    // -------------------------------------------------------------------------
    // Styled message prefixes
    // -------------------------------------------------------------------------

    /** Prints a green success message: [+] msg */
    public static void printSuccess(String msg) {
        if (ansiEnabled) {
            System.out.println(GREEN + BOLD + "  [+] " + RESET + GREEN + msg + RESET);
        } else {
            System.out.println("  [+] " + msg);
        }
    }

    /** Prints a red error message: [x] msg */
    public static void printError(String msg) {
        if (ansiEnabled) {
            System.out.println(RED + BOLD + "  [x] " + RESET + RED + msg + RESET);
        } else {
            System.out.println("  [x] " + msg);
        }
    }

    /** Prints a yellow warning message: [!] msg */
    public static void printWarning(String msg) {
        if (ansiEnabled) {
            System.out.println(YELLOW + BOLD + "  [!] " + RESET + YELLOW + msg + RESET);
        } else {
            System.out.println("  [!] " + msg);
        }
    }

    /** Prints a cyan info message: [i] msg */
    public static void printInfo(String msg) {
        if (ansiEnabled) {
            System.out.println(CYAN + BOLD + "  [i] " + RESET + CYAN + msg + RESET);
        } else {
            System.out.println("  [i] " + msg);
        }
    }

    /** Prints a styled input prompt (does NOT append newline). */
    public static void printPrompt(String msg) {
        if (ansiEnabled) {
            System.out.print(WHITE + BOLD + "  > " + RESET + WHITE + msg + RESET);
        } else {
            System.out.print("  > " + msg);
        }
    }

    // -------------------------------------------------------------------------
    // Menu box helpers (used by Main.java)
    // -------------------------------------------------------------------------

    /** Returns the single-line box chars for external use. */
    public static String sglH()  { return boxDrawingSupported ? SGL_H  : "-"; }
    public static String sglV()  { return boxDrawingSupported ? SGL_V  : "|"; }
    public static String sglTL() { return boxDrawingSupported ? SGL_TL : "+"; }
    public static String sglTR() { return boxDrawingSupported ? SGL_TR : "+"; }
    public static String sglBL() { return boxDrawingSupported ? SGL_BL : "+"; }
    public static String sglBR() { return boxDrawingSupported ? SGL_BR : "+"; }
    public static String sglLM() { return boxDrawingSupported ? SGL_LM : "+"; }
    public static String sglRM() { return boxDrawingSupported ? SGL_RM : "+"; }
    public static String dblH()  { return boxDrawingSupported ? DBL_H  : "="; }
    public static String dblV()  { return boxDrawingSupported ? DBL_V  : "|"; }
    public static String dblTL() { return boxDrawingSupported ? DBL_TL : "+"; }
    public static String dblTR() { return boxDrawingSupported ? DBL_TR : "+"; }
    public static String dblBL() { return boxDrawingSupported ? DBL_BL : "+"; }
    public static String dblBR() { return boxDrawingSupported ? DBL_BR : "+"; }
    public static String dblLM() { return boxDrawingSupported ? "\u2560" : "+"; }
    public static String dblRM() { return boxDrawingSupported ? "\u2563" : "+"; }

    // -------------------------------------------------------------------------
    // Input helpers
    // -------------------------------------------------------------------------

    public static int readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            printPrompt(prompt);
            if (!scanner.hasNextLine()) return min;
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) return value;
                printError("Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                printError("Invalid input. Please enter digits only.");
            }
        }
    }

    public static double readDouble(Scanner scanner, String prompt, double min, double max) {
        while (true) {
            printPrompt(prompt);
            if (!scanner.hasNextLine()) return min;
            String input = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(input);
                if (value >= min && value <= max) return value;
                printError("Value must be between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                printError("Invalid decimal number. Please try again.");
            }
        }
    }

    public static String readNonEmptyString(Scanner scanner, String prompt) {
        while (true) {
            printPrompt(prompt);
            if (!scanner.hasNextLine()) return "";
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) return line;
            printError("Input cannot be empty. Please enter a valid value.");
        }
    }

    public static void pressEnterToContinue(Scanner scanner) {
        System.out.println();
        String rule = "  " + repeat(sglH(), 74);
        System.out.println(ansiEnabled ? (GRAY + rule + RESET) : rule);
        System.out.print(ansiEnabled
                ? (DIM + "  Press [Enter] to continue..." + RESET)
                : "  Press [Enter] to continue...");
        if (scanner.hasNextLine()) scanner.nextLine();
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    public static String padRight(String s, int n) {
        if (s.length() >= n) return s.substring(0, n);
        return s + repeat(" ", n - s.length());
    }

    public static String repeat(String ch, int n) {
        if (n <= 0) return "";
        StringBuilder sb = new StringBuilder(n * ch.length());
        for (int i = 0; i < n; i++) sb.append(ch);
        return sb.toString();
    }
}

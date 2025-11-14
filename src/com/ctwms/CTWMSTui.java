package com.ctwms;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * Experimental launcher that wraps {@link CTWMSApplication} with a splash-style TUI.
 * Future iterations can swap this placeholder design with richer terminal layouts,
 * but for now it simply introduces the experience and hands off to the existing console.
 */
public class CTWMSTui {
    private static final int WIDTH = 72;
    private static final String BORDER = "=".repeat(WIDTH);
    private static final String SUB_BORDER = "-".repeat(WIDTH);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        new CTWMSTui().launch();
    }

    private void launch() {
        clearScreen();
        renderSplash();
        renderQuickStats();
        renderShortcuts();
        waitForEnter("\nPress Enter to launch the CTWMS console experience...");
        clearScreen();

        // Delegate to the current console app so we can experiment without disrupting production flows.
        new CTWMSApplication().start();
    }

    private void renderSplash() {
        System.out.println(BORDER);
        System.out.println(centerText("CTWMS TERMINAL EXPERIENCE", WIDTH));
        System.out.println(BORDER);
        System.out.println(centerText("Campus Task Workflow Management System", WIDTH));
        System.out.println(centerText("interactive prototype", WIDTH));
        System.out.println(SUB_BORDER);
        System.out.println(centerText("Today is " + LocalDate.now().format(DATE_FORMATTER), WIDTH));
        System.out.println(SUB_BORDER);
        String[] logo = {
                "    ██████╗████████╗██╗    ██╗███╗   ███╗███████╗",
                "   ██╔════╝╚══██╔══╝██║    ██║████╗ ████║██╔════╝",
                "   ██║        ██║   ██║ █╗ ██║██╔████╔██║█████╗  ",
                "   ██║        ██║   ██║███╗██║██║╚██╔╝██║██╔══╝  ",
                "   ╚██████╗   ██║   ╚███╔███╔╝██║ ╚═╝ ██║███████╗",
                "    ╚═════╝   ╚═╝    ╚══╝╚══╝ ╚═╝     ╚═╝╚══════╝"
        };
        for (String line : logo) {
            System.out.println(centerText(line, WIDTH));
        }
        System.out.println(SUB_BORDER);
        System.out.println(centerText("Manage – Inspect – Undo – Report", WIDTH));
        System.out.println(BORDER);
    }

    private void renderQuickStats() {
        System.out.println("\nQuick Overview");
        System.out.println(SUB_BORDER);
        System.out.printf("  %-20s : %s%n", "Data Structures", "LinkedList · ArrayList · Queue · Stack");
        System.out.printf("  %-20s : %s%n", "Core Modules", "Personnel · Services · Tasks · Undo");
        System.out.printf("  %-20s : %s%n", "Current Mode", "Classic console interface");
        System.out.println(SUB_BORDER);
    }

    private void renderShortcuts() {
        System.out.println("\nWorkflow Shortcuts");
        System.out.println(SUB_BORDER);
        System.out.println("  1) Manage Personnel – register, search and organize campus members");
        System.out.println("  2) Manage Services – curate the campus service catalog");
        System.out.println("  3) Manage Tasks – intake and process task requests");
        System.out.println("  4) Undo Center – revert your latest changes safely");
        System.out.println("  5) Dashboard – snapshot of system health");
        System.out.println("  0) Exit – leave the session anytime");
        System.out.println(SUB_BORDER);
        System.out.println("Tip: use the new pause prompts to review tables before returning to a menu.");
    }

    private void waitForEnter(String prompt) {
        System.out.print(prompt);
        scanner.nextLine();
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private String centerText(String text, int width) {
        if (text == null) {
            text = "";
        }
        if (text.length() >= width) {
            return text;
        }
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text + " ".repeat(width - text.length() - padding);
    }
}

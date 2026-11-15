import fare.FareEngine;
import graph.MetroGraph;
import model.*;
import service.MetroMindService;
import util.AsciiRouteVisualizer;
import util.ConsoleUtils;

import java.io.IOException;
import java.util.*;

/**
 * MetroMind: Bengaluru Namma Metro Transit Routing & Fare Engine
 * Direct Menu-Driven Console Application featuring Graphical Terminal Track Visualization
 * and Automatic Best-Route Recommendation.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final MetroMindService service = new MetroMindService();
    private static JourneySummary lastJourney = null;

    public static void main(String[] args) {
        printWelcomeBanner();

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = ConsoleUtils.readInt(scanner, "Select an option [1-7]: ", 1, 7);

            switch (choice) {
                case 1:
                    handleFindBestRoute();
                    break;
                case 2:
                    handleExploreNetwork();
                    break;
                case 3:
                    handleViewFareStructure();
                    break;
                case 4:
                    handleNetworkOperations();
                    break;
                case 5:
                    handleRecentJourneys();
                    break;
                case 6:
                    handleExportData();
                    break;
                case 7:
                    running = false;
                    printExitMessage();
                    break;
            }
        }
    }

    // =========================================================================
    // WELCOME BANNER
    // =========================================================================
    private static void printWelcomeBanner() {
        // \u2550 = double horiz, \u2554 = dbl top-left, \u2557 = dbl top-right
        // \u255A = dbl bot-left,  \u255D = dbl bot-right, \u2551 = dbl vertical
        String h   = ConsoleUtils.dblH();
        String v   = ConsoleUtils.dblV();
        String tl  = ConsoleUtils.dblTL();
        String tr  = ConsoleUtils.dblTR();
        String bl  = ConsoleUtils.dblBL();
        String br  = ConsoleUtils.dblBR();

        String eq  = ConsoleUtils.repeat(h, 74);
        String top = tl + eq + tr;
        String bot = bl + eq + br;

        System.out.println();
        if (ConsoleUtils.ansiEnabled) {
            System.out.println(ConsoleUtils.BOLD + ConsoleUtils.CYAN + top + ConsoleUtils.RESET);
            System.out.println(ConsoleUtils.BOLD + ConsoleUtils.CYAN + v + ConsoleUtils.RESET
                    + center("M E T R O M I N D   T R A N S I T   E N G I N E", 74)
                    + ConsoleUtils.BOLD + ConsoleUtils.CYAN + v + ConsoleUtils.RESET);
            System.out.println(ConsoleUtils.BOLD + ConsoleUtils.CYAN + v + ConsoleUtils.RESET
                    + center("Bengaluru Namma Metro  " + ConsoleUtils.dot() + "  Routing & Fare Computation", 74)
                    + ConsoleUtils.BOLD + ConsoleUtils.CYAN + v + ConsoleUtils.RESET);
            System.out.println(ConsoleUtils.BOLD + ConsoleUtils.CYAN + bot + ConsoleUtils.RESET);
        } else {
            System.out.println(top);
            System.out.println(v + center("M E T R O M I N D   T R A N S I T   E N G I N E", 74) + v);
            System.out.println(v + center("Bengaluru Namma Metro  " + ConsoleUtils.dot() + "  Routing & Fare Computation", 74) + v);
            System.out.println(bot);
        }

        System.out.println();
        System.out.println(ConsoleUtils.colored("  Operational Network  ", ConsoleUtils.BOLD)
                + ConsoleUtils.colored("63 stations  " + ConsoleUtils.dot() + "  5 Namma Metro lines", ConsoleUtils.CYAN));
        System.out.println();

        // Line overview table (using simple ASCII <-> for arrows)
        String[][] lines = {
            {"Purple", "Challaghatta  <->  Majestic  <->  Whitefield (Kadugodi)"},
            {"Green",  "Madavara (BIEC)  <->  Yeshwanthpur  <->  Majestic  <->  Silk Institute"},
            {"Yellow", "RV Road  <->  Jayadeva  <->  Central Silk Board  <->  Electronic City"},
            {"Pink",   "Kalena Agrahara  <->  MG Road  <->  Cantonment  <->  Nagawara"},
            {"Blue",   "Central Silk Board  <->  Marathahalli  <->  KR Puram  <->  KIAL Airport"},
        };
        for (String[] row : lines) {
            String lineName = row[0];
            String route    = row[1];
            String nameCol  = ConsoleUtils.colored("  o " + padRight(lineName + " Line", 13), lineColor(lineName));
            System.out.println(nameCol + ConsoleUtils.colored(route, ConsoleUtils.GRAY));
        }

        System.out.println();
        ConsoleUtils.printInfo("Ready to navigate your commute. Plan your journey below.");
        System.out.println();
    }

    // =========================================================================
    // MAIN MENU
    // =========================================================================
    private static void printMainMenu() {
        // \u2500 = single horiz, \u2502 = single vert
        // \u250C/\u2510/\u2514/\u2518 = corners, \u251C/\u2524 = tees
        String H  = ConsoleUtils.sglH();
        String V  = ConsoleUtils.sglV();
        String TL = ConsoleUtils.sglTL();
        String TR = ConsoleUtils.sglTR();
        String BL = ConsoleUtils.sglBL();
        String BR = ConsoleUtils.sglBR();
        String LM = ConsoleUtils.sglLM();
        String RM = ConsoleUtils.sglRM();

        String inner = ConsoleUtils.repeat(H, 69);
        String sep   = "  " + LM + inner + RM;
        String top   = "  " + TL + inner + TR;
        String bot   = "  " + BL + inner + BR;

        System.out.println();
        if (ConsoleUtils.ansiEnabled) {
            System.out.println(ConsoleUtils.CYAN + top + ConsoleUtils.RESET);
            System.out.println(ConsoleUtils.CYAN + "  " + V + ConsoleUtils.RESET
                    + ConsoleUtils.BOLD + "  MAIN MENU" + ConsoleUtils.RESET
                    + ConsoleUtils.repeat(" ", 57)
                    + ConsoleUtils.CYAN + V + ConsoleUtils.RESET);
            System.out.println(ConsoleUtils.CYAN + sep + ConsoleUtils.RESET);
        } else {
            System.out.println(top);
            System.out.println("  " + V + "  MAIN MENU" + ConsoleUtils.repeat(" ", 57) + V);
            System.out.println(sep);
        }

        printMenuItem(V, "1", ConsoleUtils.YELLOW, "Find Best Route & Visual Track Map",
                "Smart route  .  graphical schematic  .  fare breakdown");
        printMenuItem(V, "2", ConsoleUtils.YELLOW, "Explore Transit Lines, Stations & Interchanges",
                "Network directory  .  line listings  .  junction hubs");
        printMenuItem(V, "3", ConsoleUtils.YELLOW, "View Official BMRCL Fare Structure & Concession Slabs",
                "Distance slabs  .  surcharges  .  passenger concessions");
        printMenuItem(V, "4", ConsoleUtils.YELLOW, "Network Operations Simulator",
                "Maintenance  .  station/track closure  .  dynamic rerouting");
        printMenuItem(V, "5", ConsoleUtils.YELLOW, "View Recent Journey History",
                "Last 25 journeys  .  re-examine trips");
        printMenuItem(V, "6", ConsoleUtils.YELLOW, "Export Data",
                "Receipt (.txt)  .  route CSV  .  full network CSV");
        printMenuItemLast(V, "7", ConsoleUtils.RED, "Exit", "");

        if (ConsoleUtils.ansiEnabled) {
            System.out.println(ConsoleUtils.CYAN + bot + ConsoleUtils.RESET);
        } else {
            System.out.println(bot);
        }
        System.out.println();
    }

    private static void printMenuItem(String V, String num, String numColor, String label, String desc) {
        if (ConsoleUtils.ansiEnabled) {
            System.out.println(ConsoleUtils.CYAN + "  " + V + ConsoleUtils.RESET
                    + "  " + ConsoleUtils.BOLD + numColor + "[" + num + "]" + ConsoleUtils.RESET
                    + "  " + padRight(label, 56)
                    + ConsoleUtils.CYAN + V + ConsoleUtils.RESET);
            if (!desc.isEmpty()) {
                System.out.println(ConsoleUtils.CYAN + "  " + V + ConsoleUtils.RESET
                        + "       " + ConsoleUtils.GRAY + padRight(truncate(desc, 56), 56) + ConsoleUtils.RESET
                        + ConsoleUtils.CYAN + V + ConsoleUtils.RESET);
            }
        } else {
            System.out.println("  " + V + "  [" + num + "]  " + padRight(label, 56) + V);
            if (!desc.isEmpty()) {
                System.out.println("  " + V + "       " + padRight(truncate(desc, 56), 56) + V);
            }
        }
    }

    private static void printMenuItemLast(String V, String num, String numColor, String label, String desc) {
        if (ConsoleUtils.ansiEnabled) {
            System.out.println(ConsoleUtils.CYAN + "  " + V + ConsoleUtils.RESET
                    + "  " + ConsoleUtils.BOLD + numColor + "[" + num + "]" + ConsoleUtils.RESET
                    + "  " + padRight(label, 56)
                    + ConsoleUtils.CYAN + V + ConsoleUtils.RESET);
        } else {
            System.out.println("  " + V + "  [" + num + "]  " + padRight(label, 56) + V);
        }
    }

    // =========================================================================
    // EXIT MESSAGE
    // =========================================================================
    private static void printExitMessage() {
        String h   = ConsoleUtils.dblH();
        String v   = ConsoleUtils.dblV();
        String tl  = ConsoleUtils.dblTL();
        String tr  = ConsoleUtils.dblTR();
        String bl  = ConsoleUtils.dblBL();
        String br  = ConsoleUtils.dblBR();

        String eq  = ConsoleUtils.repeat(h, 74);
        String top = tl + eq + tr;
        String bot = bl + eq + br;

        System.out.println();
        if (ConsoleUtils.ansiEnabled) {
            System.out.println(ConsoleUtils.CYAN + ConsoleUtils.BOLD + top + ConsoleUtils.RESET);
            System.out.println(ConsoleUtils.CYAN + v + ConsoleUtils.RESET
                    + center("Thank you for using MetroMind!", 74)
                    + ConsoleUtils.CYAN + v + ConsoleUtils.RESET);
            System.out.println(ConsoleUtils.CYAN + v + ConsoleUtils.RESET
                    + center("Have a safe journey on Namma Metro Bengaluru. Goodbye!", 74)
                    + ConsoleUtils.CYAN + v + ConsoleUtils.RESET);
            System.out.println(ConsoleUtils.CYAN + ConsoleUtils.BOLD + bot + ConsoleUtils.RESET);
        } else {
            System.out.println(top);
            System.out.println(v + center("Thank you for using MetroMind!", 74) + v);
            System.out.println(v + center("Have a safe journey on Namma Metro Bengaluru. Goodbye!", 74) + v);
            System.out.println(bot);
        }
        System.out.println();
    }

    // =========================================================================
    // FEATURE 1: FIND BEST ROUTE & GRAPHICAL TERMINAL VISUALIZATION
    // =========================================================================
    private static void handleFindBestRoute() {
        ConsoleUtils.printHeader("FIND BEST ROUTE -- OPTIMAL COMMUTER PATH");

        Station origin = promptSelectStation("Origin Station: ");
        if (origin == null) return;

        Station destination = promptSelectStation("Destination Station: ");
        if (destination == null) return;

        if (origin.equals(destination)) {
            ConsoleUtils.printWarning("Origin and destination stations are the same. Zero travel required.");
            ConsoleUtils.pressEnterToContinue(scanner);
            return;
        }

        ConsoleUtils.printSubHeader("Select Ticket / Pass Category");
        TicketType[] ticketTypes = TicketType.values();
        for (int i = 0; i < ticketTypes.length; i++) {
            System.out.printf("   [%d]  %-38s  %s%n",
                    i + 1,
                    ticketTypes[i].getTitle(),
                    ConsoleUtils.colored(String.format("(%.0f%% discount)", ticketTypes[i].getDiscountPercentage()),
                            ConsoleUtils.GRAY));
        }
        int tickChoice = ConsoleUtils.readInt(scanner, "Choose ticket type [1-" + ticketTypes.length + "]: ",
                1, ticketTypes.length);
        TicketType selectedTicket = ticketTypes[tickChoice - 1];

        // Automatically compute the BEST ROUTE only!
        JourneySummary summary = service.planBestJourney(origin, destination, selectedTicket);
        lastJourney = summary;
        RouteResult route = summary.getRouteResult();

        if (!route.isFound()) {
            ConsoleUtils.printError("No operational path found between "
                    + origin.getName() + " and " + destination.getName() + ".");
            ConsoleUtils.printWarning("Intermediate tracks or stations may be closed for maintenance.");
            ConsoleUtils.pressEnterToContinue(scanner);
            return;
        }

        // 1. RENDER GRAPHICAL SCHEMATIC IN TERMINAL
        AsciiRouteVisualizer.renderGraphicalRoute(route);

        // 2. DISPLAY FARE BREAKDOWN
        ConsoleUtils.printSubHeader("Fare Computation Breakdown");
        FareBreakdown fare = summary.getSelectedFare();
        System.out.printf("   %-24s %s%n", "Selected Category:",
                ConsoleUtils.colored(fare.getTicketType().getTitle(), ConsoleUtils.WHITE + ConsoleUtils.BOLD));
        System.out.printf("   %-24s %s%n", "Applicable Slab:",
                ConsoleUtils.colored(fare.getSlabName(), ConsoleUtils.CYAN));
        System.out.printf("   %-24s %s%n", "Base Fare:",
                ConsoleUtils.colored(String.format("%s%.2f", ConsoleUtils.rupee(), fare.getBaseFare()), ConsoleUtils.WHITE));
        if (fare.getAirportSurcharge() > 0) {
            System.out.printf("   %-24s %s%n", "Airport Line Surcharge:",
                    ConsoleUtils.colored(String.format("+%s%.2f  (KIAL Airport Corridor)",
                            ConsoleUtils.rupee(), fare.getAirportSurcharge()), ConsoleUtils.YELLOW));
        }
        if (fare.getDiscountAmount() > 0) {
            System.out.printf("   %-24s %s%n", "Discount Applied:",
                    ConsoleUtils.colored(String.format("-%s%.2f  (%.0f%% concession)",
                            ConsoleUtils.rupee(), fare.getDiscountAmount(), fare.getTicketType().getDiscountPercentage()),
                            ConsoleUtils.GREEN));
        }
        ConsoleUtils.printDivider();
        System.out.printf("   %-24s %s%n", "FINAL PAYABLE AMOUNT:",
                ConsoleUtils.colored(String.format("%s%.2f", ConsoleUtils.rupee(), fare.getFinalFare()),
                        ConsoleUtils.GREEN + ConsoleUtils.BOLD));
        ConsoleUtils.printDivider();

        // 3. COMPARISON TABLE ACROSS ALL PASSES
        ConsoleUtils.printSubHeader("All Fare Options Comparison");
        System.out.printf("   %-40s | %-11s | %-11s | %-10s%n",
                ConsoleUtils.colored("Ticket Type", ConsoleUtils.BOLD),
                ConsoleUtils.colored("Base", ConsoleUtils.BOLD),
                ConsoleUtils.colored("Discount", ConsoleUtils.BOLD),
                ConsoleUtils.colored("Payable", ConsoleUtils.BOLD));
        ConsoleUtils.printDivider();
        for (Map.Entry<TicketType, FareBreakdown> entry : summary.getFareOptions().entrySet()) {
            FareBreakdown fb = entry.getValue();
            System.out.printf("   %-40s | %s%-10.2f | -%s%-9.2f | %s%n",
                    entry.getKey().getTitle(),
                    ConsoleUtils.rupee(), fb.getBaseFare() + fb.getAirportSurcharge(),
                    ConsoleUtils.rupee(), fb.getDiscountAmount(),
                    ConsoleUtils.colored(String.format("%s%-9.2f", ConsoleUtils.rupee(), fb.getFinalFare()), ConsoleUtils.GREEN));
        }

        // 4. PROMPT FOR EXPORT
        System.out.println();
        ConsoleUtils.printPrompt("Export this journey receipt to a text file? (y/N): ");
        String ans = scanner.nextLine().trim();
        if (ans.equalsIgnoreCase("y") || ans.equalsIgnoreCase("yes")) {
            try {
                String path = service.exportReceipt(summary);
                ConsoleUtils.printSuccess("Receipt exported successfully to: " + path);
            } catch (IOException e) {
                ConsoleUtils.printError("Failed to export receipt: " + e.getMessage());
            }
        }

        ConsoleUtils.pressEnterToContinue(scanner);
    }

    // =========================================================================
    // FEATURE 2: EXPLORE METRO NETWORK
    // =========================================================================
    private static void handleExploreNetwork() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("EXPLORE METRO NETWORK DIRECTORY");
            System.out.println("   [1]  View All Operational Lines");
            System.out.println("   [2]  View Stations along a Specific Line");
            System.out.println("   [3]  View All Interchange / Transfer Hubs");
            System.out.println("   [4]  Search Station by Name");
            System.out.println("   [5]  List All Network Stations Alphabetically");
            System.out.println("   [6]  Return to Main Menu");
            ConsoleUtils.printDivider();

            int choice = ConsoleUtils.readInt(scanner, "Select option [1-6]: ", 1, 6);
            switch (choice) {
                case 1:
                    ConsoleUtils.printSubHeader("Operational Transit Lines");
                    for (MetroLine line : MetroLine.values()) {
                        List<Station> stList = service.getStationsByLine(line);
                        String lineLabel = line.getDisplayName().replace(" Line", "");
                        System.out.printf("   %s %-25s %s  %s%n",
                                ConsoleUtils.colored("*", lineColor(lineLabel)),
                                ConsoleUtils.colored(line.getDisplayName(), ConsoleUtils.WHITE + ConsoleUtils.BOLD),
                                ConsoleUtils.colored("(" + stList.size() + " stations)", ConsoleUtils.CYAN),
                                ConsoleUtils.colored(line.getDescription(), ConsoleUtils.GRAY));
                    }
                    ConsoleUtils.pressEnterToContinue(scanner);
                    break;
                case 2:
                    MetroLine[] lines = MetroLine.values();
                    ConsoleUtils.printSubHeader("Select Line to Inspect");
                    for (int i = 0; i < lines.length; i++) {
                        System.out.printf("   [%d]  %s%n", i + 1, lines[i].getDisplayName());
                    }
                    int lineIdx = ConsoleUtils.readInt(scanner,
                            "Choose line [1-" + lines.length + "]: ", 1, lines.length) - 1;
                    MetroLine chosenLine = lines[lineIdx];

                    List<Station> lineStations = service.getStationsByLine(chosenLine);
                    ConsoleUtils.printSubHeader("Stations on " + chosenLine.getDisplayName()
                            + " (" + lineStations.size() + ")");
                    for (int i = 0; i < lineStations.size(); i++) {
                        Station s = lineStations.get(i);
                        String interchange = s.isInterchange()
                                ? ConsoleUtils.colored("  [INTERCHANGE]", ConsoleUtils.YELLOW)
                                : "";
                        String status = !s.isActive()
                                ? ConsoleUtils.colored("  [MAINTENANCE CLOSED]", ConsoleUtils.RED)
                                : "";
                        System.out.printf("   %2d.  %-32s%s%s%n", i + 1, s.getName(), interchange, status);
                    }
                    ConsoleUtils.pressEnterToContinue(scanner);
                    break;
                case 3:
                    List<Station> interchanges = service.getInterchangeStations();
                    ConsoleUtils.printSubHeader("Major Interchange Junction Hubs (" + interchanges.size() + ")");
                    for (Station s : interchanges) {
                        System.out.printf("   * %-35s -> %s%n",
                                ConsoleUtils.colored(s.getName(), ConsoleUtils.WHITE + ConsoleUtils.BOLD),
                                ConsoleUtils.colored(s.getLinesString(), ConsoleUtils.CYAN));
                    }
                    ConsoleUtils.pressEnterToContinue(scanner);
                    break;
                case 4:
                    String search = ConsoleUtils.readNonEmptyString(scanner, "Station name or keyword: ");
                    List<Station> matches = service.searchStations(search);
                    if (matches.isEmpty()) {
                        ConsoleUtils.printWarning("No station matching '" + search + "' was found in the network.");
                    } else {
                        ConsoleUtils.printSuccess("Found " + matches.size() + " station(s):");
                        for (Station s : matches) {
                            String status = s.isActive()
                                    ? ConsoleUtils.colored("Active", ConsoleUtils.GREEN)
                                    : ConsoleUtils.colored("Closed", ConsoleUtils.RED);
                            System.out.printf("   * %-32s | Lines: %-28s | %s%n",
                                    s.getName(), s.getLinesString(), status);
                        }
                    }
                    ConsoleUtils.pressEnterToContinue(scanner);
                    break;
                case 5:
                    List<Station> all = new ArrayList<>(service.getAllStations());
                    Collections.sort(all);
                    ConsoleUtils.printSubHeader("All Stations Directory (" + all.size() + " Total)");
                    for (int i = 0; i < all.size(); i++) {
                        Station s = all.get(i);
                        String status = s.isActive()
                                ? ConsoleUtils.colored("Active", ConsoleUtils.GREEN)
                                : ConsoleUtils.colored("Closed", ConsoleUtils.RED);
                        System.out.printf("   [%2d]  %-35s | %-32s | %s%n",
                                i + 1, s.getName(), s.getLinesString(), status);
                    }
                    ConsoleUtils.pressEnterToContinue(scanner);
                    break;
                case 6:
                    back = true;
                    break;
            }
        }
    }

    // =========================================================================
    // FEATURE 3: FARE STRUCTURE & CONCESSION SLABS
    // =========================================================================
    private static void handleViewFareStructure() {
        ConsoleUtils.printHeader("OFFICIAL BMRCL FARE COMPUTATION POLICY");
        FareEngine.Slab[] slabs = service.getFareEngine().getAllSlabs();

        ConsoleUtils.printSubHeader("1. Distance Slabs & Base Pricing");
        System.out.printf("   %-24s | %s%n",
                ConsoleUtils.colored("Distance Range", ConsoleUtils.BOLD),
                ConsoleUtils.colored("Standard Token Fare", ConsoleUtils.BOLD));
        ConsoleUtils.printDivider();
        for (FareEngine.Slab slab : slabs) {
            System.out.printf("   %-24s | %s%n",
                    slab.getLabel(),
                    ConsoleUtils.colored(String.format("%s%.2f", ConsoleUtils.rupee(), slab.getFare()),
                            ConsoleUtils.CYAN + ConsoleUtils.BOLD));
        }

        ConsoleUtils.printSubHeader("2. Special Corridor Surcharges");
        System.out.println("   " + ConsoleUtils.colored("KIAL Airport Express Corridor",
                ConsoleUtils.YELLOW + ConsoleUtils.BOLD)
                + "  --  "
                + ConsoleUtils.colored("+" + ConsoleUtils.rupee() + FareEngine.AIRPORT_SURCHARGE
                        + " infrastructure surcharge", ConsoleUtils.YELLOW));
        ConsoleUtils.printInfo("Surcharge applies when commuting to/from KIAL Airport Terminal.");

        ConsoleUtils.printSubHeader("3. Passenger Concessions & Discounts");
        for (TicketType t : TicketType.values()) {
            System.out.printf("   %-38s : %s%n",
                    ConsoleUtils.colored(t.getTitle(), ConsoleUtils.WHITE + ConsoleUtils.BOLD),
                    ConsoleUtils.colored(String.format("%.0f%% discount on base fare",
                            t.getDiscountPercentage()), ConsoleUtils.GREEN));
            System.out.println("   " + ConsoleUtils.colored("   " + t.getDescription(), ConsoleUtils.GRAY));
        }

        ConsoleUtils.printDivider();
        ConsoleUtils.pressEnterToContinue(scanner);
    }

    // =========================================================================
    // FEATURE 4: NETWORK OPERATIONS & SIMULATOR
    // =========================================================================
    private static void handleNetworkOperations() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("NETWORK OPERATIONS & SIMULATOR");
            ConsoleUtils.printInfo("Simulate maintenance, station closures, or dynamic network expansions.");
            System.out.println("   [1]  Toggle Station Maintenance Status  (Close / Reopen Station)");
            System.out.println("   [2]  Toggle Track Segment Status         (Block / Unblock Track)");
            System.out.println("   [3]  Add a New Metro Station to the Network");
            System.out.println("   [4]  Add a New Track Connection between Two Stations");
            System.out.println("   [5]  Return to Main Menu");
            ConsoleUtils.printDivider();

            int choice = ConsoleUtils.readInt(scanner, "Select option [1-5]: ", 1, 5);
            switch (choice) {
                case 1:
                    Station st = promptSelectStation("Station to toggle status: ");
                    if (st != null) {
                        boolean newStatus = !st.isActive();
                        service.toggleStationStatus(st.getName(), newStatus);
                        if (newStatus) {
                            ConsoleUtils.printSuccess("Station '" + st.getName() + "' is now ACTIVE / OPEN.");
                        } else {
                            ConsoleUtils.printWarning("Station '" + st.getName()
                                    + "' is now CLOSED FOR MAINTENANCE.");
                        }
                        ConsoleUtils.printInfo(
                                "Future route calculations will automatically navigate around this station.");
                    }
                    ConsoleUtils.pressEnterToContinue(scanner);
                    break;
                case 2:
                    ConsoleUtils.printInfo("Specify track endpoints:");
                    Station st1 = promptSelectStation("Station 1: ");
                    if (st1 == null) break;
                    Station st2 = promptSelectStation("Station 2: ");
                    if (st2 == null) break;

                    System.out.println("   Set track between "
                            + ConsoleUtils.colored(st1.getName(), ConsoleUtils.CYAN) + " and "
                            + ConsoleUtils.colored(st2.getName(), ConsoleUtils.CYAN) + ":");
                    System.out.println("   [1]  Mark Operational / Open");
                    System.out.println("   [2]  Mark Blocked / Maintenance Closure");
                    int trackOp = ConsoleUtils.readInt(scanner, "Select [1-2]: ", 1, 2);
                    boolean op = (trackOp == 1);
                    boolean modified = service.toggleTrackStatus(st1.getName(), st2.getName(), op);
                    if (modified) {
                        if (op) {
                            ConsoleUtils.printSuccess("Track segment updated to: OPEN");
                        } else {
                            ConsoleUtils.printWarning("Track segment updated to: BLOCKED");
                        }
                    } else {
                        ConsoleUtils.printError("No direct connection found between these two stations.");
                    }
                    ConsoleUtils.pressEnterToContinue(scanner);
                    break;
                case 3:
                    String id = "CS" + (service.getAllStations().size() + 1);
                    String name = ConsoleUtils.readNonEmptyString(scanner, "New Station Name: ");
                    MetroLine[] allLines = MetroLine.values();
                    ConsoleUtils.printSubHeader("Select Primary Line");
                    for (int i = 0; i < allLines.length; i++) {
                        System.out.printf("   [%d]  %s%n", i + 1, allLines[i].getDisplayName());
                    }
                    int lIdx = ConsoleUtils.readInt(scanner,
                            "Choose line [1-" + allLines.length + "]: ", 1, allLines.length) - 1;
                    Station newStation = service.addStation(id, name, allLines[lIdx]);
                    ConsoleUtils.printSuccess("Station '" + newStation.getName()
                            + "' created and assigned to " + allLines[lIdx].getDisplayName());
                    ConsoleUtils.pressEnterToContinue(scanner);
                    break;
                case 4:
                    ConsoleUtils.printInfo("Connect two stations with a new track segment:");
                    Station sA = promptSelectStation("First Station: ");
                    if (sA == null) break;
                    Station sB = promptSelectStation("Second Station: ");
                    if (sB == null) break;
                    MetroLine[] mLines = MetroLine.values();
                    for (int i = 0; i < mLines.length; i++) {
                        System.out.printf("   [%d]  %s%n", i + 1, mLines[i].getDisplayName());
                    }
                    int selLine = ConsoleUtils.readInt(scanner,
                            "Choose transit line [1-" + mLines.length + "]: ", 1, mLines.length) - 1;
                    double dist = ConsoleUtils.readDouble(scanner,
                            "Track distance in km (e.g. 2.4): ", 0.1, 100.0);
                    double mins = ConsoleUtils.readDouble(scanner,
                            "Travel duration in minutes (e.g. 4.0): ", 0.5, 120.0);

                    service.addTrackConnection(sA, sB, mLines[selLine], dist, mins);
                    ConsoleUtils.printSuccess("Track successfully built between "
                            + sA.getName() + " and " + sB.getName());
                    ConsoleUtils.pressEnterToContinue(scanner);
                    break;
                case 5:
                    back = true;
                    break;
            }
        }
    }

    // =========================================================================
    // FEATURE 5: RECENT JOURNEYS
    // =========================================================================
    private static void handleRecentJourneys() {
        ConsoleUtils.printHeader("RECENT JOURNEY HISTORY");
        List<JourneySummary> list = service.getRecentJourneys();
        if (list.isEmpty()) {
            ConsoleUtils.printWarning("No journeys planned in this session yet.");
            ConsoleUtils.pressEnterToContinue(scanner);
            return;
        }

        // Header row
        System.out.printf("   %-10s | %-19s | %-20s -> %-20s | %-8s | %s%n",
                ConsoleUtils.colored("Trip ID",     ConsoleUtils.BOLD),
                ConsoleUtils.colored("Timestamp",   ConsoleUtils.BOLD),
                ConsoleUtils.colored("Origin",      ConsoleUtils.BOLD),
                ConsoleUtils.colored("Destination", ConsoleUtils.BOLD),
                ConsoleUtils.colored("Distance",    ConsoleUtils.BOLD),
                ConsoleUtils.colored("Fare",        ConsoleUtils.BOLD));
        ConsoleUtils.printDivider();
        for (JourneySummary js : list) {
            RouteResult rr = js.getRouteResult();
            System.out.printf("   %-10s | %-19s | %-20s -> %-20s | %-5.2f km | %s%n",
                    js.getJourneyId(), js.getFormattedTimestamp(),
                    truncate(rr.getOrigin().getName(), 20),
                    truncate(rr.getDestination().getName(), 20),
                    rr.getTotalDistanceKm(),
                    ConsoleUtils.colored(String.format("%s%.2f", ConsoleUtils.rupee(), js.getSelectedFare().getFinalFare()),
                            ConsoleUtils.GREEN));
        }

        System.out.println();
        ConsoleUtils.printPrompt("Enter Trip ID to re-examine (or press Enter to return): ");
        String tripId = scanner.nextLine().trim();
        if (!tripId.isEmpty()) {
            boolean found = false;
            for (JourneySummary js : list) {
                if (js.getJourneyId().equalsIgnoreCase(tripId)) {
                    AsciiRouteVisualizer.renderGraphicalRoute(js.getRouteResult());
                    found = true;
                    break;
                }
            }
            if (!found) {
                ConsoleUtils.printError("Trip ID '" + tripId + "' not found in recent journeys.");
            }
        }
        ConsoleUtils.pressEnterToContinue(scanner);
    }

    // =========================================================================
    // STRETCH FEATURES: EXPORT DATA
    // =========================================================================
    private static void handleExportData() {
        ConsoleUtils.printHeader("DATA EXPORT");
        System.out.println("   [1]  Export Last Journey as Printable Text Ticket");
        System.out.println("   [2]  Export Last Journey Turn-by-Turn Route to CSV");
        System.out.println("   [3]  Export Entire Metro Network & Station Directory to CSV");
        System.out.println("   [4]  Return to Main Menu");
        ConsoleUtils.printDivider();

        int choice = ConsoleUtils.readInt(scanner, "Choose export option [1-4]: ", 1, 4);
        try {
            switch (choice) {
                case 1:
                    if (lastJourney == null) {
                        ConsoleUtils.printWarning("No journey has been planned yet. Plan a journey first.");
                    } else {
                        String file = service.exportReceipt(lastJourney);
                        ConsoleUtils.printSuccess("Ticket receipt exported successfully to: " + file);
                    }
                    break;
                case 2:
                    if (lastJourney == null) {
                        ConsoleUtils.printWarning("No journey has been planned yet. Plan a journey first.");
                    } else {
                        String file = service.exportRouteCsv(lastJourney);
                        ConsoleUtils.printSuccess("Route path CSV exported successfully to: " + file);
                    }
                    break;
                case 3:
                    String file = service.exportNetworkCsv();
                    ConsoleUtils.printSuccess("Complete Metro Network directory exported to: " + file);
                    break;
                case 4:
                    return;
            }
        } catch (IOException e) {
            ConsoleUtils.printError("Export failed due to I/O error: " + e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue(scanner);
    }

    // =========================================================================
    // SHARED HELPERS
    // =========================================================================
    private static Station promptSelectStation(String prompt) {
        while (true) {
            ConsoleUtils.printPrompt(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                ConsoleUtils.printError("Station name cannot be empty.");
                continue;
            }

            Station exact = service.getStation(input);
            if (exact != null) return exact;

            List<Station> matches = service.searchStations(input);
            if (matches.isEmpty()) {
                ConsoleUtils.printError("No station found matching '" + input + "'.");
                ConsoleUtils.printInfo(
                        "Tip: Type part of the name, e.g. 'Majestic', 'Whitefield', 'Silk Board'.");
                ConsoleUtils.printPrompt("Try again? (y/N): ");
                String retry = scanner.nextLine().trim();
                if (!retry.equalsIgnoreCase("y") && !retry.equalsIgnoreCase("yes")) {
                    return null;
                }
            } else if (matches.size() == 1) {
                Station matched = matches.get(0);
                ConsoleUtils.printSuccess("Matched station: "
                        + ConsoleUtils.colored(matched.getName(), ConsoleUtils.CYAN + ConsoleUtils.BOLD));
                return matched;
            } else {
                ConsoleUtils.printInfo("Multiple matching stations found:");
                for (int i = 0; i < matches.size(); i++) {
                    System.out.printf("   [%d]  %s  %s%n", i + 1,
                            matches.get(i).getName(),
                            ConsoleUtils.colored("(" + matches.get(i).getLinesString() + ")",
                                    ConsoleUtils.GRAY));
                }
                int sel = ConsoleUtils.readInt(scanner,
                        "Select station [1-" + matches.size() + "]: ", 1, matches.size());
                return matches.get(sel - 1);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Static utility helpers
    // -------------------------------------------------------------------------

    private static String center(String text, int width) {
        if (text.length() >= width) return text.substring(0, width);
        int pad = (width - text.length()) / 2;
        return ConsoleUtils.repeat(" ", pad) + text
                + ConsoleUtils.repeat(" ", width - pad - text.length());
    }

    private static String padRight(String s, int n) {
        if (s.length() >= n) return s.substring(0, n);
        return s + ConsoleUtils.repeat(" ", n - s.length());
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 3) + "...";
    }

    /** Returns the ANSI color closest to the metro line's brand color. */
    private static String lineColor(String lineName) {
        if (lineName == null) return ConsoleUtils.WHITE;
        switch (lineName.toLowerCase()) {
            case "purple": return ConsoleUtils.MAGENTA;
            case "green":  return ConsoleUtils.GREEN;
            case "yellow": return ConsoleUtils.YELLOW;
            case "pink":   return "\u001B[95m"; // bright magenta
            case "blue":   return ConsoleUtils.BLUE;
            default:       return ConsoleUtils.WHITE;
        }
    }
}

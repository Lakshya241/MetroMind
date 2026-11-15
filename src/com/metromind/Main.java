package com.metromind;

import com.metromind.fare.FareEngine;
import com.metromind.model.*;
import com.metromind.routing.RouteEngine.Criteria;
import com.metromind.service.MetroMindService;
import com.metromind.util.ConsoleUtils;

import java.io.IOException;
import java.util.*;

/**
 * MetroMind: Shortest-Route and Fare Computation Engine
 * Interactive Menu-Driven Console Application.
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
            int choice = ConsoleUtils.readInt(scanner, "Select an option [1-8]: ", 1, 8);

            switch (choice) {
                case 1:
                    handlePlanJourney();
                    break;
                case 2:
                    handleCompareRoutes();
                    break;
                case 3:
                    handleExploreNetwork();
                    break;
                case 4:
                    handleViewFareStructure();
                    break;
                case 5:
                    handleNetworkOperations();
                    break;
                case 6:
                    handleRecentJourneys();
                    break;
                case 7:
                    handleExportData();
                    break;
                case 8:
                    running = false;
                    printExitMessage();
                    break;
            }
        }
    }

    private static void printWelcomeBanner() {
        System.out.println("================================================================================");
        System.out.println(" *           METROMIND: BENGALURU NAMMA METRO ROUTING & FARE ENGINE           *");
        System.out.println(" *      Multi-Line Shortest-Path, Interchanges & Smart-Fare Calculator         *");
        System.out.println("================================================================================");
        System.out.println(" Initialized with " + service.getAllStations().size() + " stations across " +
                MetroLine.values().length + " Namma Metro lines with real-time transfer penalties.");
        System.out.println(" Operational Network: Purple Line, Green Line, Yellow Line, Pink Line, Blue Line (Airport & ORR).");
        System.out.println(" Ready to navigate your commute across Bengaluru.\n");
    }

    private static void printMainMenu() {
        ConsoleUtils.printHeader("MAIN NAVIGATION MENU");
        System.out.println("  [1] Plan Journey (Route Finding, Transfers & Fare Breakdown)");
        System.out.println("  [2] Multi-Criteria Route Comparison (Shortest vs Fastest vs Transfers)");
        System.out.println("  [3] Explore Metro Network (Lines, Stations & Interchanges)");
        System.out.println("  [4] View Fare Structure & Concession Slabs");
        System.out.println("  [5] Network Management & Disruption Simulation (Maintenance/Closure)");
        System.out.println("  [6] View Recent Journey History");
        System.out.println("  [7] Export Data (Receipts, Route CSV, Network CSV - Stretch Features)");
        System.out.println("  [8] Exit");
        ConsoleUtils.printDivider();
    }

    // =========================================================================
    // FEATURE 1: PLAN A JOURNEY
    // =========================================================================
    private static void handlePlanJourney() {
        ConsoleUtils.printHeader("PLAN YOUR JOURNEY");

        Station origin = promptSelectStation("Enter Origin Station: ");
        if (origin == null) return;

        Station destination = promptSelectStation("Enter Destination Station: ");
        if (destination == null) return;

        if (origin.equals(destination)) {
            System.out.println("(!) Origin and destination stations are the same. Zero travel required.");
            ConsoleUtils.pressEnterToContinue(scanner);
            return;
        }

        ConsoleUtils.printSubHeader("Select Routing Preference");
        System.out.println("  [1] Shortest Physical Distance (Minimizes track kilometers)");
        System.out.println("  [2] Fastest Travel Time (Minimizes journey minutes + transfer buffers)");
        System.out.println("  [3] Fewest Interchanges (Minimizes line changes/train switches)");
        int critChoice = ConsoleUtils.readInt(scanner, "Choose preference [1-3]: ", 1, 3);
        Criteria criteria;
        switch (critChoice) {
            case 2: criteria = Criteria.FASTEST_TIME; break;
            case 3: criteria = Criteria.FEWEST_INTERCHANGES; break;
            default: criteria = Criteria.SHORTEST_DISTANCE; break;
        }

        ConsoleUtils.printSubHeader("Select Ticket / Pass Category");
        TicketType[] ticketTypes = TicketType.values();
        for (int i = 0; i < ticketTypes.length; i++) {
            System.out.printf("  [%d] %-35s (%.0f%% discount)\n",
                    i + 1, ticketTypes[i].getTitle(), ticketTypes[i].getDiscountPercentage());
        }
        int tickChoice = ConsoleUtils.readInt(scanner, "Choose ticket type [1-" + ticketTypes.length + "]: ", 1, ticketTypes.length);
        TicketType selectedTicket = ticketTypes[tickChoice - 1];

        // Execute computation
        JourneySummary summary = service.planJourney(origin, destination, criteria, selectedTicket);
        lastJourney = summary;
        displayJourneyResult(summary);

        // Prompt to export receipt
        System.out.print("\nWould you like to export this journey receipt to a text file? (y/N): ");
        String ans = scanner.nextLine().trim();
        if (ans.equalsIgnoreCase("y") || ans.equalsIgnoreCase("yes")) {
            try {
                String path = service.exportReceipt(summary);
                System.out.println("(+) Receipt exported successfully to: " + path);
            } catch (IOException e) {
                System.out.println("(!) Failed to export receipt: " + e.getMessage());
            }
        }

        ConsoleUtils.pressEnterToContinue(scanner);
    }

    private static void displayJourneyResult(JourneySummary summary) {
        RouteResult route = summary.getRouteResult();

        if (!route.isFound()) {
            System.out.println("\n(!) No operational path found between " +
                    route.getOrigin().getName() + " and " + route.getDestination().getName() + ".");
            System.out.println("    (Tracks or intermediate stations may be currently closed for maintenance).");
            return;
        }

        ConsoleUtils.printHeader("TRIP ITINERARY & NAVIGATION DETAILS [" + summary.getJourneyId() + "]");
        System.out.printf("  Origin Station   : %s\n", route.getOrigin().getName());
        System.out.printf("  Destination      : %s\n", route.getDestination().getName());
        System.out.printf("  Strategy Used    : %s\n", route.getOptimizationCriteria());
        System.out.printf("  Total Stations   : %d stops\n", route.getTotalStations());
        System.out.printf("  Total Distance   : %.2f km\n", route.getTotalDistanceKm());
        System.out.printf("  Estimated Time   : %.1f minutes\n", route.getTotalTimeMinutes());
        System.out.printf("  Line Transfers   : %d interchange(s)\n", route.getInterchangeCount());

        if (route.getInterchangeCount() > 0) {
            System.out.print("  Interchange Hubs : ");
            for (int i = 0; i < route.getInterchanges().size(); i++) {
                if (i > 0) System.out.print(" -> ");
                System.out.print(route.getInterchanges().get(i).getName());
            }
            System.out.println();
        }

        ConsoleUtils.printSubHeader("Step-by-Step Directions");
        int segIndex = 1;
        for (RouteSegment seg : route.getSegments()) {
            System.out.printf("\n  Leg %d: Board the [%s]\n", segIndex++, seg.getLine().getDisplayName().toUpperCase());
            System.out.printf("         From: %s  --->  To: %s\n", seg.getStartStation().getName(), seg.getEndStation().getName());
            System.out.printf("         Stops (%d): ", seg.getStationCount());

            List<Station> sts = seg.getStations();
            for (int i = 0; i < sts.size(); i++) {
                if (i > 0) System.out.print(" -> ");
                System.out.print(sts.get(i).getName());
            }
            System.out.printf("\n         Subtotal: %.2f km | Approx. %.1f mins\n", seg.getDistanceKm(), seg.getTimeMinutes());
        }

        ConsoleUtils.printSubHeader("Fare Computation Breakdown");
        FareBreakdown fare = summary.getSelectedFare();
        System.out.printf("  Selected Category    : %s\n", fare.getTicketType().getTitle());
        System.out.printf("  Applicable Slab      : %s\n", fare.getSlabName());
        System.out.printf("  Base Fare            : ₹%.2f\n", fare.getBaseFare());
        if (fare.getAirportSurcharge() > 0) {
            System.out.printf("  Airport Line Surchg. : ₹%.2f\n", fare.getAirportSurcharge());
        }
        if (fare.getDiscountAmount() > 0) {
            System.out.printf("  Discount Applied     : -₹%.2f (%.0f%% concession)\n",
                    fare.getDiscountAmount(), fare.getTicketType().getDiscountPercentage());
        }
        System.out.println("  ---------------------------------------------");
        System.out.printf("  FINAL PAYABLE AMOUNT : ₹%.2f\n", fare.getFinalFare());
        System.out.println("  ---------------------------------------------");

        ConsoleUtils.printSubHeader("All Fare Options Comparison");
        System.out.printf("  %-38s | %-10s | %-10s | %-10s\n", "Ticket Type", "Base", "Discount", "Payable");
        System.out.println("  ----------------------------------------------------------------------");
        for (Map.Entry<TicketType, FareBreakdown> entry : summary.getFareOptions().entrySet()) {
            FareBreakdown fb = entry.getValue();
            System.out.printf("  %-38s | ₹%-9.2f | -₹%-9.2f | ₹%-9.2f\n",
                    entry.getKey().getTitle(), fb.getBaseFare() + fb.getAirportSurcharge(),
                    fb.getDiscountAmount(), fb.getFinalFare());
        }
    }

    // =========================================================================
    // FEATURE 2: MULTI-CRITERIA ROUTE COMPARISON
    // =========================================================================
    private static void handleCompareRoutes() {
        ConsoleUtils.printHeader("MULTI-CRITERIA ROUTE COMPARISON");
        System.out.println("Evaluate all 3 routing algorithms side-by-side for your trip.\n");

        Station origin = promptSelectStation("Enter Origin Station: ");
        if (origin == null) return;

        Station destination = promptSelectStation("Enter Destination Station: ");
        if (destination == null) return;

        Map<Criteria, RouteResult> results = service.compareRoutes(origin, destination);

        ConsoleUtils.printSubHeader("Comparison Summary: " + origin.getName() + " -> " + destination.getName());
        System.out.printf("  %-25s | %-12s | %-14s | %-12s | %-10s | %-10s\n",
                "Routing Criteria", "Distance", "Estimated Time", "Interchanges", "Stops", "Token Fare");
        System.out.println("  --------------------------------------------------------------------------------------------");

        for (Map.Entry<Criteria, RouteResult> entry : results.entrySet()) {
            Criteria crit = entry.getKey();
            RouteResult rr = entry.getValue();

            if (!rr.isFound()) {
                System.out.printf("  %-25s | %-62s\n", crit.getLabel(), "NO PATH AVAILABLE (Check for line closures)");
                continue;
            }

            FareBreakdown fb = service.getFareEngine().calculateFare(rr, TicketType.TOKEN);
            System.out.printf("  %-25s | %-9.2f km | %-11.1f min | %-12d | %-10d | ₹%-9.2f\n",
                    crit.getLabel(),
                    rr.getTotalDistanceKm(),
                    rr.getTotalTimeMinutes(),
                    rr.getInterchangeCount(),
                    rr.getTotalStations(),
                    fb.getFinalFare());
        }

        ConsoleUtils.printDivider();
        System.out.println("Tip: Commuters traveling with luggage often prefer 'Fewest Line Interchanges'");
        System.out.println("     to avoid walking across interchange platforms!");
        ConsoleUtils.pressEnterToContinue(scanner);
    }

    // =========================================================================
    // FEATURE 3: EXPLORE METRO NETWORK
    // =========================================================================
    private static void handleExploreNetwork() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("EXPLORE METRO NETWORK DIRECTORY");
            System.out.println("  [1] View All Operational Lines");
            System.out.println("  [2] View Stations along a Specific Line");
            System.out.println("  [3] View All Interchange / Transfer Hubs");
            System.out.println("  [4] Search Station by Name");
            System.out.println("  [5] List All Network Stations Alphabetically");
            System.out.println("  [6] Return to Main Menu");
            ConsoleUtils.printDivider();

            int choice = ConsoleUtils.readInt(scanner, "Select option [1-6]: ", 1, 6);
            switch (choice) {
                case 1:
                    ConsoleUtils.printSubHeader("Operational Transit Lines");
                    for (MetroLine line : MetroLine.values()) {
                        List<Station> stList = service.getStationsByLine(line);
                        System.out.printf("  * %-18s (%2d stations) - %s\n",
                                line.getDisplayName(), stList.size(), line.getDescription());
                    }
                    ConsoleUtils.pressEnterToContinue(scanner);
                    break;
                case 2:
                    MetroLine[] lines = MetroLine.values();
                    ConsoleUtils.printSubHeader("Select Line to Inspect");
                    for (int i = 0; i < lines.length; i++) {
                        System.out.printf("  [%d] %s\n", i + 1, lines[i].getDisplayName());
                    }
                    int lineIdx = ConsoleUtils.readInt(scanner, "Choose line [1-" + lines.length + "]: ", 1, lines.length) - 1;
                    MetroLine chosenLine = lines[lineIdx];

                    List<Station> lineStations = service.getStationsByLine(chosenLine);
                    ConsoleUtils.printSubHeader("Stations on " + chosenLine.getDisplayName() + " (" + lineStations.size() + ")");
                    for (int i = 0; i < lineStations.size(); i++) {
                        Station s = lineStations.get(i);
                        System.out.printf("  %2d. %-28s %s %s\n",
                                i + 1, s.getName(),
                                s.isInterchange() ? "[INTERCHANGE]" : "",
                                s.isActive() ? "" : "[MAINTENANCE CLOSED]");
                    }
                    ConsoleUtils.pressEnterToContinue(scanner);
                    break;
                case 3:
                    List<Station> interchanges = service.getInterchangeStations();
                    ConsoleUtils.printSubHeader("Major Interchange Junction Hubs (" + interchanges.size() + ")");
                    for (Station s : interchanges) {
                        System.out.printf("  * %-25s -> Connecting: %s\n", s.getName(), s.getLinesString());
                    }
                    ConsoleUtils.pressEnterToContinue(scanner);
                    break;
                case 4:
                    String search = ConsoleUtils.readNonEmptyString(scanner, "Enter station name or keyword to search: ");
                    List<Station> matches = service.searchStations(search);
                    if (matches.isEmpty()) {
                        System.out.println("(!) No station matching '" + search + "' was found in the network.");
                    } else {
                        System.out.println("(+) Found " + matches.size() + " station(s):");
                        for (Station s : matches) {
                            System.out.printf("  - %-25s | Lines: %s | Status: %s\n",
                                    s.getName(), s.getLinesString(), s.isActive() ? "ACTIVE" : "CLOSED");
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
                        System.out.printf("  [%2d] %-28s | Lines: %-30s | %s\n",
                                i + 1, s.getName(), s.getLinesString(), s.isActive() ? "Active" : "Closed");
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
    // FEATURE 4: FARE STRUCTURE & CONCESSION SLABS
    // =========================================================================
    private static void handleViewFareStructure() {
        ConsoleUtils.printHeader("OFFICIAL FARE COMPUTATION POLICY");
        FareEngine.Slab[] slabs = service.getFareEngine().getAllSlabs();

        System.out.println("1. DISTANCE SLABS & BASE PRICING:");
        System.out.printf("   %-22s | %-12s\n", "Distance Range", "Standard Token Fare");
        System.out.println("   --------------------------------------");
        for (FareEngine.Slab slab : slabs) {
            System.out.printf("   %-22s | ₹%.2f\n", slab.getLabel(), slab.getFare());
        }

        System.out.println("\n2. SPECIAL CORRIDOR SURCHARGES:");
        System.out.println("   * Airport Express Line : +₹" + FareEngine.AIRPORT_SURCHARGE +
                " surcharge applies when transit utilizes the Airport Express corridor.");

        System.out.println("\n3. PASSENGER CONCESSIONS & DISCOUNTS:");
        for (TicketType t : TicketType.values()) {
            System.out.printf("   * %-35s : %.0f%% Discount on base fare\n", t.getTitle(), t.getDiscountPercentage());
            System.out.printf("     %s\n", t.getDescription());
        }

        ConsoleUtils.printDivider();
        ConsoleUtils.pressEnterToContinue(scanner);
    }

    // =========================================================================
    // FEATURE 5: NETWORK OPERATIONS & DISRUPTION SIMULATOR
    // =========================================================================
    private static void handleNetworkOperations() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("NETWORK OPERATIONS & SIMULATOR");
            System.out.println("Simulate maintenance, station closures, or dynamic network expansions.");
            System.out.println("  [1] Toggle Station Maintenance Status (Close / Reopen Station)");
            System.out.println("  [2] Toggle Track Segment Status (Block / Unblock Track)");
            System.out.println("  [3] Add a New Metro Station to the Network");
            System.out.println("  [4] Add a New Track Connection between Two Stations");
            System.out.println("  [5] Return to Main Menu");
            ConsoleUtils.printDivider();

            int choice = ConsoleUtils.readInt(scanner, "Select option [1-5]: ", 1, 5);
            switch (choice) {
                case 1:
                    Station st = promptSelectStation("Enter station to toggle status: ");
                    if (st != null) {
                        boolean newStatus = !st.isActive();
                        service.toggleStationStatus(st.getName(), newStatus);
                        System.out.println("(+) Station '" + st.getName() + "' is now " +
                                (newStatus ? "ACTIVE / OPEN" : "CLOSED FOR MAINTENANCE"));
                        System.out.println("    Future route calculations will automatically navigate around this station!");
                    }
                    ConsoleUtils.pressEnterToContinue(scanner);
                    break;
                case 2:
                    System.out.println("Specify track endpoints:");
                    Station st1 = promptSelectStation("Station 1: ");
                    if (st1 == null) break;
                    Station st2 = promptSelectStation("Station 2: ");
                    if (st2 == null) break;

                    System.out.println("Set track between " + st1.getName() + " and " + st2.getName() + ":");
                    System.out.println("  [1] Mark Operational / Open");
                    System.out.println("  [2] Mark Blocked / Maintenance Closure");
                    int trackOp = ConsoleUtils.readInt(scanner, "Select [1-2]: ", 1, 2);
                    boolean op = (trackOp == 1);
                    boolean modified = service.toggleTrackStatus(st1.getName(), st2.getName(), op);
                    if (modified) {
                        System.out.println("(+) Track segment successfully updated to: " + (op ? "OPEN" : "BLOCKED"));
                    } else {
                        System.out.println("(!) No direct connection found between these two stations.");
                    }
                    ConsoleUtils.pressEnterToContinue(scanner);
                    break;
                case 3:
                    String id = "CS" + (service.getAllStations().size() + 1);
                    String name = ConsoleUtils.readNonEmptyString(scanner, "Enter New Station Name: ");
                    MetroLine[] allLines = MetroLine.values();
                    System.out.println("Select primary line for this station:");
                    for (int i = 0; i < allLines.length; i++) {
                        System.out.printf("  [%d] %s\n", i + 1, allLines[i].getDisplayName());
                    }
                    int lIdx = ConsoleUtils.readInt(scanner, "Choose line [1-" + allLines.length + "]: ", 1, allLines.length) - 1;
                    Station newStation = service.addStation(id, name, allLines[lIdx]);
                    System.out.println("(+) Station '" + newStation.getName() + "' created and assigned to " + allLines[lIdx].getDisplayName());
                    ConsoleUtils.pressEnterToContinue(scanner);
                    break;
                case 4:
                    System.out.println("Connect two stations:");
                    Station sA = promptSelectStation("First Station: ");
                    if (sA == null) break;
                    Station sB = promptSelectStation("Second Station: ");
                    if (sB == null) break;
                    MetroLine[] mLines = MetroLine.values();
                    for (int i = 0; i < mLines.length; i++) {
                        System.out.printf("  [%d] %s\n", i + 1, mLines[i].getDisplayName());
                    }
                    int selLine = ConsoleUtils.readInt(scanner, "Choose transit line [1-" + mLines.length + "]: ", 1, mLines.length) - 1;
                    double dist = ConsoleUtils.readDouble(scanner, "Enter track distance in km (e.g. 2.4): ", 0.1, 100.0);
                    double mins = ConsoleUtils.readDouble(scanner, "Enter travel duration in minutes (e.g. 4.0): ", 0.5, 120.0);

                    service.addTrackConnection(sA, sB, mLines[selLine], dist, mins);
                    System.out.println("(+) Track successfully built between " + sA.getName() + " and " + sB.getName());
                    ConsoleUtils.pressEnterToContinue(scanner);
                    break;
                case 5:
                    back = true;
                    break;
            }
        }
    }

    // =========================================================================
    // FEATURE 6: RECENT JOURNEYS
    // =========================================================================
    private static void handleRecentJourneys() {
        ConsoleUtils.printHeader("RECENT JOURNEY HISTORY");
        List<JourneySummary> list = service.getRecentJourneys();
        if (list.isEmpty()) {
            System.out.println("(!) No journeys planned in this session yet.");
            ConsoleUtils.pressEnterToContinue(scanner);
            return;
        }

        System.out.printf("  %-10s | %-19s | %-20s -> %-20s | %-8s | %-8s\n",
                "Trip ID", "Timestamp", "Origin", "Destination", "Distance", "Fare");
        System.out.println("  ---------------------------------------------------------------------------------------------");
        for (int i = 0; i < list.size(); i++) {
            JourneySummary js = list.get(i);
            RouteResult rr = js.getRouteResult();
            System.out.printf("  %-10s | %-19s | %-20s -> %-20s | %-5.2f km | ₹%-7.2f\n",
                    js.getJourneyId(), js.getFormattedTimestamp(),
                    rr.getOrigin().getName(), rr.getDestination().getName(),
                    rr.getTotalDistanceKm(), js.getSelectedFare().getFinalFare());
        }

        System.out.print("\nEnter Trip ID to re-examine or export (or press Enter to return): ");
        String tripId = scanner.nextLine().trim();
        if (!tripId.isEmpty()) {
            for (JourneySummary js : list) {
                if (js.getJourneyId().equalsIgnoreCase(tripId)) {
                    displayJourneyResult(js);
                    break;
                }
            }
        }
        ConsoleUtils.pressEnterToContinue(scanner);
    }

    // =========================================================================
    // STRETCH FEATURES: EXPORT DATA
    // =========================================================================
    private static void handleExportData() {
        ConsoleUtils.printHeader("DATA EXPORT (STRETCH FEATURES)");
        System.out.println("  [1] Export Last Journey as Printable Text Ticket");
        System.out.println("  [2] Export Last Journey Turn-by-Turn Route to CSV");
        System.out.println("  [3] Export Entire Metro Network & Station Directory to CSV");
        System.out.println("  [4] Return to Main Menu");
        ConsoleUtils.printDivider();

        int choice = ConsoleUtils.readInt(scanner, "Choose export option [1-4]: ", 1, 4);
        try {
            switch (choice) {
                case 1:
                    if (lastJourney == null) {
                        System.out.println("(!) No journey has been planned yet to export. Plan a journey first.");
                    } else {
                        String file = service.exportReceipt(lastJourney);
                        System.out.println("(+) Ticket receipt exported successfully to: " + file);
                    }
                    break;
                case 2:
                    if (lastJourney == null) {
                        System.out.println("(!) No journey has been planned yet to export. Plan a journey first.");
                    } else {
                        String file = service.exportRouteCsv(lastJourney);
                        System.out.println("(+) Route path CSV exported successfully to: " + file);
                    }
                    break;
                case 3:
                    String file = service.exportNetworkCsv();
                    System.out.println("(+) Complete Metro Network directory exported to: " + file);
                    break;
                case 4:
                    return;
            }
        } catch (IOException e) {
            System.out.println("(!) Export failed due to I/O error: " + e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue(scanner);
    }

    private static Station promptSelectStation(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("(!) Station name cannot be empty.");
                continue;
            }

            Station exact = service.getStation(input);
            if (exact != null) {
                return exact;
            }

            // Search for partial matches
            List<Station> matches = service.searchStations(input);
            if (matches.isEmpty()) {
                System.out.println("(!) No station found matching '" + input + "'.");
                System.out.println("    Tip: Type part of the name or check Option 3 to explore stations.");
                System.out.print("Try again? (y/N): ");
                String retry = scanner.nextLine().trim();
                if (!retry.equalsIgnoreCase("y") && !retry.equalsIgnoreCase("yes")) {
                    return null;
                }
            } else if (matches.size() == 1) {
                Station matched = matches.get(0);
                System.out.println("    -> Matched station: " + matched.getName());
                return matched;
            } else {
                System.out.println("Multiple matching stations found:");
                for (int i = 0; i < matches.size(); i++) {
                    System.out.printf("  [%d] %s (%s)\n", i + 1, matches.get(i).getName(), matches.get(i).getLinesString());
                }
                int sel = ConsoleUtils.readInt(scanner, "Select station [1-" + matches.size() + "]: ", 1, matches.size());
                return matches.get(sel - 1);
            }
        }
    }

    private static void printExitMessage() {
        System.out.println("\n================================================================================");
        System.out.println(" Thank you for using MetroMind: Shortest-Route and Fare Computation Engine!      ");
        System.out.println(" Have a safe and pleasant journey on the metro! Goodbye.                        ");
        System.out.println("================================================================================\n");
    }
}

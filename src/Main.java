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

    private static void printWelcomeBanner() {
        System.out.println("================================================================================");
        System.out.println(" *           METROMIND: BENGALURU NAMMA METRO ROUTING & FARE ENGINE           *");
        System.out.println(" *        Automated Best-Route Optimization & Terminal Graphical Maps         *");
        System.out.println("================================================================================");
        System.out.println(" Operational Network: 63 stations across 5 Namma Metro lines:");
        System.out.println("   [Purple Line]  Challaghatta <-> Majestic <-> Whitefield (Kadugodi)");
        System.out.println("   [Green Line]   Madavara (BIEC) <-> Yeshwanthpur <-> Majestic <-> Silk Institute");
        System.out.println("   [Yellow Line]  RV Road <-> Jayadeva <-> Central Silk Board <-> Electronic City");
        System.out.println("   [Pink Line]    Kalena Agrahara <-> MG Road <-> Cantonment <-> Nagawara");
        System.out.println("   [Blue Line]    Central Silk Board <-> Marathahalli <-> KR Puram <-> KIAL Airport");
        System.out.println(" Ready to navigate your commute.\n");
    }

    private static void printMainMenu() {
        ConsoleUtils.printHeader("MAIN NAVIGATION MENU");
        System.out.println("  [1] Find Best Route & Visual Track Map (Graphical Schematic & Fare)");
        System.out.println("  [2] Explore Transit Lines, Stations & Interchanges");
        System.out.println("  [3] View Official BMRCL Fare Structure & Concession Slabs");
        System.out.println("  [4] Network Operations Simulator (Maintenance, Station/Track Closure & Rerouting)");
        System.out.println("  [5] View Recent Journey History");
        System.out.println("  [6] Export Data (Receipts, Route CSV, Network CSV - Stretch Features)");
        System.out.println("  [7] Exit");
        ConsoleUtils.printDivider();
    }

    // =========================================================================
    // FEATURE 1: FIND BEST ROUTE & GRAPHICAL TERMINAL VISUALIZATION
    // =========================================================================
    private static void handleFindBestRoute() {
        ConsoleUtils.printHeader("FIND BEST ROUTE (OPTIMAL COMMUTER PATH)");

        Station origin = promptSelectStation("Enter Origin Station: ");
        if (origin == null) return;

        Station destination = promptSelectStation("Enter Destination Station: ");
        if (destination == null) return;

        if (origin.equals(destination)) {
            System.out.println("(!) Origin and destination stations are the same. Zero travel required.");
            ConsoleUtils.pressEnterToContinue(scanner);
            return;
        }

        ConsoleUtils.printSubHeader("Select Ticket / Pass Category");
        TicketType[] ticketTypes = TicketType.values();
        for (int i = 0; i < ticketTypes.length; i++) {
            System.out.printf("  [%d] %-38s (%.0f%% discount)\n",
                    i + 1, ticketTypes[i].getTitle(), ticketTypes[i].getDiscountPercentage());
        }
        int tickChoice = ConsoleUtils.readInt(scanner, "Choose ticket type [1-" + ticketTypes.length + "]: ", 1, ticketTypes.length);
        TicketType selectedTicket = ticketTypes[tickChoice - 1];

        // Automatically compute the BEST ROUTE only!
        JourneySummary summary = service.planBestJourney(origin, destination, selectedTicket);
        lastJourney = summary;
        RouteResult route = summary.getRouteResult();

        if (!route.isFound()) {
            System.out.println("\n(!) No operational path found between " +
                    origin.getName() + " and " + destination.getName() + ".");
            System.out.println("    (Intermediate tracks or stations may be closed for maintenance).");
            ConsoleUtils.pressEnterToContinue(scanner);
            return;
        }

        // 1. RENDER GRAPHICAL SCHEMATIC IN TERMINAL
        AsciiRouteVisualizer.renderGraphicalRoute(route);

        // 2. DISPLAY FARE BREAKDOWN
        ConsoleUtils.printSubHeader("Fare Computation Breakdown");
        FareBreakdown fare = summary.getSelectedFare();
        System.out.printf("  Selected Category    : %s\n", fare.getTicketType().getTitle());
        System.out.printf("  Applicable Slab      : %s\n", fare.getSlabName());
        System.out.printf("  Base Fare            : ₹%.2f\n", fare.getBaseFare());
        if (fare.getAirportSurcharge() > 0) {
            System.out.printf("  Airport Line Surchg. : ₹%.2f (KIAL Airport Corridor)\n", fare.getAirportSurcharge());
        }
        if (fare.getDiscountAmount() > 0) {
            System.out.printf("  Discount Applied     : -₹%.2f (%.0f%% concession)\n",
                    fare.getDiscountAmount(), fare.getTicketType().getDiscountPercentage());
        }
        System.out.println("  ---------------------------------------------");
        System.out.printf("  FINAL PAYABLE AMOUNT : ₹%.2f\n", fare.getFinalFare());
        System.out.println("  ---------------------------------------------");

        // 3. COMPARISON TABLE ACROSS ALL PASSES
        ConsoleUtils.printSubHeader("All Fare Options Comparison");
        System.out.printf("  %-40s | %-10s | %-10s | %-10s\n", "Ticket Type", "Base", "Discount", "Payable");
        System.out.println("  ------------------------------------------------------------------------");
        for (Map.Entry<TicketType, FareBreakdown> entry : summary.getFareOptions().entrySet()) {
            FareBreakdown fb = entry.getValue();
            System.out.printf("  %-40s | ₹%-9.2f | -₹%-9.2f | ₹%-9.2f\n",
                    entry.getKey().getTitle(), fb.getBaseFare() + fb.getAirportSurcharge(),
                    fb.getDiscountAmount(), fb.getFinalFare());
        }

        // 4. PROMPT FOR EXPORT
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

    // =========================================================================
    // FEATURE 2: EXPLORE METRO NETWORK
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
                        System.out.printf("  * %-25s (%2d stations) - %s\n",
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
                        System.out.printf("  %2d. %-32s %s %s\n",
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
                        System.out.printf("  * %-35s -> Connecting: %s\n", s.getName(), s.getLinesString());
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
                            System.out.printf("  - %-32s | Lines: %s | Status: %s\n",
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
                        System.out.printf("  [%2d] %-35s | Lines: %-32s | %s\n",
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
    // FEATURE 3: FARE STRUCTURE & CONCESSION SLABS
    // =========================================================================
    private static void handleViewFareStructure() {
        ConsoleUtils.printHeader("OFFICIAL BMRCL FARE COMPUTATION POLICY");
        FareEngine.Slab[] slabs = service.getFareEngine().getAllSlabs();

        System.out.println("1. DISTANCE SLABS & BASE PRICING:");
        System.out.printf("   %-22s | %-12s\n", "Distance Range", "Standard Token Fare");
        System.out.println("   --------------------------------------");
        for (FareEngine.Slab slab : slabs) {
            System.out.printf("   %-22s | ₹%.2f\n", slab.getLabel(), slab.getFare());
        }

        System.out.println("\n2. SPECIAL CORRIDOR SURCHARGES:");
        System.out.println("   * KIAL Airport Express Corridor : +₹" + FareEngine.AIRPORT_SURCHARGE +
                " infrastructure surcharge applies when commuting to/from KIAL Airport Terminal.");

        System.out.println("\n3. PASSENGER CONCESSIONS & DISCOUNTS:");
        for (TicketType t : TicketType.values()) {
            System.out.printf("   * %-38s : %.0f%% Discount on base fare\n", t.getTitle(), t.getDiscountPercentage());
            System.out.printf("     %s\n", t.getDescription());
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
    // FEATURE 5: RECENT JOURNEYS
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
        for (JourneySummary js : list) {
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
                    AsciiRouteVisualizer.renderGraphicalRoute(js.getRouteResult());
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
                System.out.println("    Tip: Type part of the name (e.g., 'Majestic', 'Whitefield', 'Silk Board').");
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
        System.out.println(" Have a safe and pleasant journey on Namma Metro Bengaluru! Goodbye.             ");
        System.out.println("================================================================================\n");
    }
}

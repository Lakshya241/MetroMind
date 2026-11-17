import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ============================================================================
 * METROMIND: BENGALURU NAMMA METRO TRANSIT ROUTING & FARE APPLICATION
 * ============================================================================
 * User interface, fare calculation engine, network configuration, and
 * visual track rendering layer. Works in conjunction with MetroDSA.java.
 *
 * Core Responsibilities:
 *   - Interactive Menu-Driven Terminal UI
 *   - BMRCL Distance Slab & Concession Fare Engine
 *   - Bengaluru Namma Metro 63-Station Network Initializer
 *   - Visual Track Schematic Renderer (ASCII Terminal Graphics)
 *   - Analytical CSV & Text Receipt Exporter
 *   - Automated Verification Test Suite (10/10 Self-Tests)
 *
 * Compilation & Execution:
 *   javac *.java
 *   java Main            (Interactive Console App)
 *   java Main --test     (Run 10 Verification Self-Tests)
 * ============================================================================
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final MetroMindService service = new MetroMindService();
    private static JourneySummary lastJourney = null;

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("--test")) {
            runVerificationTests();
            return;
        }

        printWelcomeBanner();

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = ConsoleUtils.readInt(scanner, "Select an option [1-8]: ", 1, 8);

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
                    runVerificationTests();
                    ConsoleUtils.pressEnterToContinue(scanner);
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
        System.out.println("  [6] Export Data (Receipts, Route CSV, Network CSV)");
        System.out.println("  [7] Run Automated Verification Tests (10/10 Self-Tests)");
        System.out.println("  [8] Exit");
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

        // Automatically compute the BEST ROUTE via RouteEngine in MetroDSA
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
    // FEATURE 6: EXPORT DATA
    // =========================================================================
    private static void handleExportData() {
        ConsoleUtils.printHeader("DATA EXPORT");
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

    // =========================================================================
    // FEATURE 7: AUTOMATED VERIFICATION SELF-TESTS (10/10)
    // =========================================================================
    public static void runVerificationTests() {
        System.out.println("\n==================================================");
        System.out.println("   METROMIND (NAMMA METRO) VERIFICATION SUITE     ");
        System.out.println("==================================================");

        int totalTests = 0;
        int passedTests = 0;

        MetroMindService testService = new MetroMindService();

        // TEST 1: Station Directory & Connectivity
        totalTests++;
        System.out.print("[TEST 1] Bengaluru Station Directory & Connectivity: ");
        int stationCount = testService.getAllStations().size();
        int interchangeCount = testService.getInterchangeStations().size();
        if (stationCount >= 30 && interchangeCount >= 5) {
            System.out.println("PASSED (" + stationCount + " stations, " + interchangeCount + " interchanges)");
            passedTests++;
        } else {
            System.out.println("FAILED (" + stationCount + " stations, " + interchangeCount + " interchanges)");
        }

        // TEST 2: Direct Single-Line Journey (Whitefield to Majestic on Purple Line)
        totalTests++;
        System.out.print("[TEST 2] Direct Purple Line Route (Whitefield -> Majestic): ");
        Station whitefield = testService.getStation("Whitefield (Kadugodi)");
        Station majestic = testService.getStation("Nadaprabhu Kempegowda Station, Majestic");
        JourneySummary whitefieldToMajestic = testService.planJourney(whitefield, majestic, RouteEngine.Criteria.SHORTEST_DISTANCE, TicketType.TOKEN);
        if (whitefieldToMajestic.getRouteResult().isFound() && whitefieldToMajestic.getRouteResult().getInterchangeCount() == 0) {
            System.out.printf("PASSED (Distance: %.2f km, Stops: %d)\n",
                    whitefieldToMajestic.getRouteResult().getTotalDistanceKm(),
                    whitefieldToMajestic.getRouteResult().getTotalStations());
            passedTests++;
        } else {
            System.out.println("FAILED");
        }

        // TEST 3: Direct Green Line Route (Madavara to Silk Institute)
        totalTests++;
        System.out.print("[TEST 3] Direct Green Line Route (Madavara -> Silk Institute): ");
        Station madavara = testService.getStation("Madavara (BIEC)");
        Station silkInstitute = testService.getStation("Silk Institute");
        JourneySummary greenLineTrip = testService.planJourney(madavara, silkInstitute, RouteEngine.Criteria.FEWEST_INTERCHANGES, TicketType.TOKEN);
        if (greenLineTrip.getRouteResult().isFound() && greenLineTrip.getRouteResult().getInterchangeCount() == 0) {
            System.out.printf("PASSED (Direct 0-transfer Green Line: %.2f km)\n",
                    greenLineTrip.getRouteResult().getTotalDistanceKm());
            passedTests++;
        } else {
            System.out.println("FAILED");
        }

        // TEST 4: Multi-Line Interchange Route (Whitefield to Lalbagh via Majestic)
        totalTests++;
        System.out.print("[TEST 4] Multi-Line Interchange Route (Whitefield -> Lalbagh): ");
        Station lalbagh = testService.getStation("Lalbagh");
        JourneySummary transferTrip = testService.planJourney(whitefield, lalbagh, RouteEngine.Criteria.FEWEST_INTERCHANGES, TicketType.TOKEN);
        if (transferTrip.getRouteResult().isFound() && transferTrip.getRouteResult().getInterchangeCount() >= 1) {
            System.out.printf("PASSED (Transfers: %d at %s)\n",
                    transferTrip.getRouteResult().getInterchangeCount(),
                    transferTrip.getRouteResult().getInterchanges().get(0).getName());
            passedTests++;
        } else {
            System.out.println("FAILED");
        }

        // TEST 5: Fare Calculation Slabs & Namma Metro Discounts
        totalTests++;
        System.out.print("[TEST 5] Fare Calculation Slabs & Namma Metro Discounts: ");
        FareBreakdown tokenFare = whitefieldToMajestic.getSelectedFare();
        FareBreakdown smartCardFare = testService.getFareEngine().calculateFare(whitefieldToMajestic.getRouteResult(), TicketType.SMART_CARD);
        FareBreakdown studentFare = testService.getFareEngine().calculateFare(whitefieldToMajestic.getRouteResult(), TicketType.STUDENT_PASS);

        boolean fareMatch = (tokenFare.getBaseFare() == 50.0) &&
                (smartCardFare.getFinalFare() == 45.0) &&
                (studentFare.getFinalFare() == 37.5);

        if (fareMatch) {
            System.out.println("PASSED (Token: ₹50, SmartCard: ₹45, Student: ₹37.5)");
            passedTests++;
        } else {
            System.out.printf("FAILED (Token: %.1f, SmartCard: %.1f, Student: %.1f)\n",
                    tokenFare.getBaseFare(), smartCardFare.getFinalFare(), studentFare.getFinalFare());
        }

        // TEST 6: Airport Line Surcharge (Hebbal to KIAL Airport Terminal)
        totalTests++;
        System.out.print("[TEST 6] Airport Line Surcharge (Hebbal -> KIAL Airport Terminal): ");
        Station hebbal = testService.getStation("Hebbal");
        Station kialAirport = testService.getStation("KIAL Airport Terminal");
        JourneySummary airportTrip = testService.planJourney(hebbal, kialAirport, RouteEngine.Criteria.FASTEST_TIME, TicketType.TOKEN);
        FareBreakdown airportFare = airportTrip.getSelectedFare();
        if (airportTrip.getRouteResult().usesAirportExpress() && airportFare.getAirportSurcharge() == 20.0) {
            System.out.printf("PASSED (Base: ₹%.1f + Surcharge: ₹%.1f = Total: ₹%.1f)\n",
                    airportFare.getBaseFare(), airportFare.getAirportSurcharge(), airportFare.getFinalFare());
            passedTests++;
        } else {
            System.out.println("FAILED");
        }

        // TEST 7: Station Disruption & Dynamic Rerouting (Close Majestic)
        totalTests++;
        System.out.print("[TEST 7] Dynamic Disruption Rerouting (Close Majestic): ");
        Station indiranagar = testService.getStation("Indiranagar");
        Station rajajinagar = testService.getStation("Rajajinagar");

        testService.toggleStationStatus("Nadaprabhu Kempegowda Station, Majestic", false);
        JourneySummary afterClosure = testService.planJourney(indiranagar, rajajinagar, RouteEngine.Criteria.FEWEST_INTERCHANGES, TicketType.TOKEN);
        testService.toggleStationStatus("Nadaprabhu Kempegowda Station, Majestic", true);

        if (afterClosure.getRouteResult().isFound() && !afterClosure.getRouteResult().getPath().contains(majestic)) {
            System.out.println("PASSED (Successfully avoided closed station Majestic and rerouted via alternative lines)");
            passedTests++;
        } else {
            System.out.println("FAILED");
        }

        // TEST 8: File Export (Receipt .txt and Route .csv)
        totalTests++;
        System.out.print("[TEST 8] File Export (Receipt .txt and Route .csv): ");
        try {
            String txtFile = testService.exportReceipt(whitefieldToMajestic);
            String csvFile = testService.exportRouteCsv(whitefieldToMajestic);
            String netCsv = testService.exportNetworkCsv();

            File f1 = new File(txtFile);
            File f2 = new File(csvFile);
            File f3 = new File(netCsv);

            if (f1.exists() && f1.length() > 0 && f2.exists() && f2.length() > 0 && f3.exists() && f3.length() > 0) {
                System.out.println("PASSED (Exported: " + txtFile + ", " + csvFile + ", " + netCsv + ")");
                passedTests++;
            } else {
                System.out.println("FAILED (Files missing or empty)");
            }
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
        }

        // TEST 9: Automated Best Route Optimization Engine
        totalTests++;
        System.out.print("[TEST 9] Automated Best Route Optimization: ");
        JourneySummary bestTrip = testService.planBestJourney(whitefield, lalbagh, TicketType.TOKEN);
        if (bestTrip.getRouteResult().isFound() && bestTrip.getRouteResult().getTotalStations() > 0) {
            System.out.printf("PASSED (Selected: %s | Distance: %.2f km | Time: %.1f min)\n",
                    bestTrip.getRouteResult().getOptimizationCriteria(),
                    bestTrip.getRouteResult().getTotalDistanceKm(),
                    bestTrip.getRouteResult().getTotalTimeMinutes());
            passedTests++;
        } else {
            System.out.println("FAILED");
        }

        // TEST 10: Terminal Graphical Visualizer
        totalTests++;
        System.out.print("[TEST 10] Terminal Graphical Track Schematic Rendering: ");
        try {
            AsciiRouteVisualizer.renderGraphicalRoute(bestTrip.getRouteResult());
            System.out.println("PASSED (Visual schematic rendered successfully)");
            passedTests++;
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }

        System.out.println("==================================================");
        System.out.printf("   TEST RESULTS: %d / %d TESTS PASSED (%.1f%%)\n",
                passedTests, totalTests, (passedTests * 100.0 / totalTests));
        System.out.println("==================================================");

        if (passedTests == totalTests) {
            System.out.println(">>> ALL 10 TESTS PASSED! TRANSIT ENGINE FULLY VERIFIED! <<<");
        }
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

// ============================================================================
// 1. FARE ENGINE & TICKET CONCESSIONS
// ============================================================================

enum TicketType {
    TOKEN("Standard Token / QR Ticket", 0.0, "Standard single journey token with full fare."),
    SMART_CARD("Namma Metro Smart Card (Varshik / NCMC)", 10.0, "Rechargeable smart card offering 10% discount on base fare."),
    SMART_CARD_OFFPEAK("Namma Metro Smart Card (Off-Peak / Weekend)", 20.0, "Smart card with enhanced 20% discount outside rush hours and holidays."),
    STUDENT_PASS("Student Concession Pass", 25.0, "Verified student concession offering 25% discount on base fare."),
    SENIOR_CITIZEN("Senior Citizen Pass", 30.0, "Special senior citizen concession offering 30% discount on base fare.");

    private final String title;
    private final double discountPercentage;
    private final String description;

    TicketType(String title, double discountPercentage, String description) {
        this.title = title;
        this.discountPercentage = discountPercentage;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format("%s (%.0f%% Off)", title, discountPercentage);
    }
}

class FareBreakdown implements Serializable {
    private static final long serialVersionUID = 1L;

    private final double distanceKm;
    private final String slabName;
    private final double baseFare;
    private final double airportSurcharge;
    private final TicketType ticketType;
    private final double discountAmount;
    private final double finalFare;

    public FareBreakdown(double distanceKm, String slabName, double baseFare,
                         double airportSurcharge, TicketType ticketType,
                         double discountAmount, double finalFare) {
        this.distanceKm = distanceKm;
        this.slabName = slabName;
        this.baseFare = baseFare;
        this.airportSurcharge = airportSurcharge;
        this.ticketType = ticketType;
        this.discountAmount = discountAmount;
        this.finalFare = finalFare;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public String getSlabName() {
        return slabName;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public double getAirportSurcharge() {
        return airportSurcharge;
    }

    public TicketType getTicketType() {
        return ticketType;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public double getFinalFare() {
        return finalFare;
    }
}

class JourneySummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String journeyId;
    private final LocalDateTime timestamp;
    private final RouteResult routeResult;
    private final Map<TicketType, FareBreakdown> fareOptions;
    private final TicketType selectedTicketType;

    public JourneySummary(String journeyId, RouteResult routeResult,
                          Map<TicketType, FareBreakdown> fareOptions,
                          TicketType selectedTicketType) {
        this.journeyId = journeyId;
        this.timestamp = LocalDateTime.now();
        this.routeResult = routeResult;
        this.fareOptions = fareOptions;
        this.selectedTicketType = selectedTicketType;
    }

    public String getJourneyId() {
        return journeyId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public RouteResult getRouteResult() {
        return routeResult;
    }

    public Map<TicketType, FareBreakdown> getFareOptions() {
        return fareOptions;
    }

    public TicketType getSelectedTicketType() {
        return selectedTicketType;
    }

    public FareBreakdown getSelectedFare() {
        return fareOptions.get(selectedTicketType);
    }

    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}

class FareEngine {
    public static final double AIRPORT_SURCHARGE = 20.0;
    public static final double MINIMUM_FARE = 10.0;

    public static class Slab {
        private final double minKm;
        private final double maxKm;
        private final double fare;
        private final String label;

        public Slab(double minKm, double maxKm, double fare, String label) {
            this.minKm = minKm;
            this.maxKm = maxKm;
            this.fare = fare;
            this.label = label;
        }

        public double getMinKm() { return minKm; }
        public double getMaxKm() { return maxKm; }
        public double getFare() { return fare; }
        public String getLabel() { return label; }
    }

    private static final Slab[] SLABS = new Slab[]{
            new Slab(0.0, 2.0, 10.0, "0.0 - 2.0 km"),
            new Slab(2.0, 5.0, 20.0, "2.1 - 5.0 km"),
            new Slab(5.0, 12.0, 30.0, "5.1 - 12.0 km"),
            new Slab(12.0, 21.0, 40.0, "12.1 - 21.0 km"),
            new Slab(21.0, 32.0, 50.0, "21.1 - 32.0 km"),
            new Slab(32.0, Double.MAX_VALUE, 60.0, "> 32.0 km")
    };

    public FareBreakdown calculateFare(RouteResult route, TicketType ticketType) {
        if (route == null || !route.isFound() || route.getPath().size() <= 1) {
            return new FareBreakdown(0.0, "Same Station", 0.0, 0.0, ticketType, 0.0, 0.0);
        }

        double distance = route.getTotalDistanceKm();
        Slab matchedSlab = getSlabForDistance(distance);
        double baseFare = matchedSlab.fare;

        double airportSurcharge = route.usesAirportExpress() ? AIRPORT_SURCHARGE : 0.0;

        double discountPercent = ticketType.getDiscountPercentage();
        double discountAmount = (baseFare * discountPercent) / 100.0;

        double finalFare = (baseFare - discountAmount) + airportSurcharge;
        finalFare = Math.max(MINIMUM_FARE, Math.round(finalFare * 100.0) / 100.0);

        return new FareBreakdown(distance, matchedSlab.label, baseFare, airportSurcharge,
                ticketType, discountAmount, finalFare);
    }

    public Map<TicketType, FareBreakdown> calculateAllFareOptions(RouteResult route) {
        Map<TicketType, FareBreakdown> options = new EnumMap<>(TicketType.class);
        for (TicketType type : TicketType.values()) {
            options.put(type, calculateFare(route, type));
        }
        return options;
    }

    public Slab getSlabForDistance(double distanceKm) {
        for (Slab slab : SLABS) {
            if (distanceKm <= slab.maxKm) {
                return slab;
            }
        }
        return SLABS[SLABS.length - 1];
    }

    public Slab[] getAllSlabs() {
        return SLABS.clone();
    }
}

// ============================================================================
// 2. NETWORK DATA INITIALIZER (63 BENGALURU METRO STATIONS)
// ============================================================================

class NetworkDataInitializer {
    public static MetroGraph buildDefaultNetwork() {
        MetroGraph graph = new MetroGraph();

        // 1. PURPLE LINE (Challaghatta to Whitefield)
        Station challaghatta    = graph.getOrCreateStation("PL01", "Challaghatta", MetroLine.PURPLE);
        Station kengeri         = graph.getOrCreateStation("PL02", "Kengeri", MetroLine.PURPLE);
        Station mysuruRoad      = graph.getOrCreateStation("PL03", "Mysuru Road", MetroLine.PURPLE);
        Station vijayanagar     = graph.getOrCreateStation("PL04", "Vijayanagar", MetroLine.PURPLE);
        Station ksrRailway      = graph.getOrCreateStation("PL05", "KSR Bengaluru Railway Station", MetroLine.PURPLE);
        Station majestic        = graph.getOrCreateStation("PL06", "Nadaprabhu Kempegowda Station, Majestic", MetroLine.PURPLE, MetroLine.GREEN);
        Station vidhanaSoudha   = graph.getOrCreateStation("PL07", "Vidhana Soudha", MetroLine.PURPLE);
        Station mgRoad          = graph.getOrCreateStation("PL08", "MG Road", MetroLine.PURPLE, MetroLine.PINK);
        Station indiranagar     = graph.getOrCreateStation("PL09", "Indiranagar", MetroLine.PURPLE);
        Station svRoad          = graph.getOrCreateStation("PL10", "Swami Vivekananda Road", MetroLine.PURPLE);
        Station baiyappanahalli = graph.getOrCreateStation("PL11", "Baiyappanahalli", MetroLine.PURPLE);
        Station krPuram         = graph.getOrCreateStation("PL12", "KR Puram", MetroLine.PURPLE, MetroLine.BLUE);
        Station garudacharpalya = graph.getOrCreateStation("PL13", "Garudacharpalya", MetroLine.PURPLE);
        Station hoodi           = graph.getOrCreateStation("PL14", "Hoodi", MetroLine.PURPLE);
        Station sathyaSaiHosp   = graph.getOrCreateStation("PL15", "Sri Sathya Sai Hospital", MetroLine.PURPLE);
        Station whitefield      = graph.getOrCreateStation("PL16", "Whitefield (Kadugodi)", MetroLine.PURPLE);

        graph.addConnection(challaghatta, kengeri, MetroLine.PURPLE, 2.1, 3.5, true);
        graph.addConnection(kengeri, mysuruRoad, MetroLine.PURPLE, 4.3, 6.0, true);
        graph.addConnection(mysuruRoad, vijayanagar, MetroLine.PURPLE, 3.2, 4.5, true);
        graph.addConnection(vijayanagar, ksrRailway, MetroLine.PURPLE, 3.0, 4.5, true);
        graph.addConnection(ksrRailway, majestic, MetroLine.PURPLE, 1.2, 2.0, true);
        graph.addConnection(majestic, vidhanaSoudha, MetroLine.PURPLE, 1.8, 3.0, true);
        graph.addConnection(vidhanaSoudha, mgRoad, MetroLine.PURPLE, 2.2, 3.5, true);
        graph.addConnection(mgRoad, indiranagar, MetroLine.PURPLE, 3.4, 5.0, true);
        graph.addConnection(indiranagar, svRoad, MetroLine.PURPLE, 1.1, 2.0, true);
        graph.addConnection(svRoad, baiyappanahalli, MetroLine.PURPLE, 1.5, 2.5, true);
        graph.addConnection(baiyappanahalli, krPuram, MetroLine.PURPLE, 3.2, 4.5, true);
        graph.addConnection(krPuram, garudacharpalya, MetroLine.PURPLE, 2.6, 4.0, true);
        graph.addConnection(garudacharpalya, hoodi, MetroLine.PURPLE, 2.4, 3.5, true);
        graph.addConnection(hoodi, sathyaSaiHosp, MetroLine.PURPLE, 2.1, 3.0, true);
        graph.addConnection(sathyaSaiHosp, whitefield, MetroLine.PURPLE, 2.5, 3.5, true);

        // 2. GREEN LINE (Madavara to Silk Institute)
        Station madavara        = graph.getOrCreateStation("GL01", "Madavara (BIEC)", MetroLine.GREEN);
        Station nagasandra      = graph.getOrCreateStation("GL02", "Nagasandra", MetroLine.GREEN);
        Station peenyaIndustry  = graph.getOrCreateStation("GL03", "Peenya Industry", MetroLine.GREEN);
        Station yeshwanthpur    = graph.getOrCreateStation("GL04", "Yeshwanthpur", MetroLine.GREEN, MetroLine.BLUE);
        Station sandalSoap      = graph.getOrCreateStation("GL05", "Sandal Soap Factory", MetroLine.GREEN);
        Station rajajinagar     = graph.getOrCreateStation("GL06", "Rajajinagar", MetroLine.GREEN);
        Station mantriSquare    = graph.getOrCreateStation("GL07", "Mantri Square Sampige Road", MetroLine.GREEN);
        Station chickpet        = graph.getOrCreateStation("GL08", "Chickpet", MetroLine.GREEN);
        Station krMarket        = graph.getOrCreateStation("GL09", "Krishna Rajendra Market", MetroLine.GREEN);
        Station lalbagh         = graph.getOrCreateStation("GL10", "Lalbagh", MetroLine.GREEN);
        Station southEndCircle  = graph.getOrCreateStation("GL11", "South End Circle", MetroLine.GREEN);
        Station jayanagar       = graph.getOrCreateStation("GL12", "Jayanagar", MetroLine.GREEN);
        Station rvRoad          = graph.getOrCreateStation("GL13", "Rashtreeya Vidyalaya (RV) Road", MetroLine.GREEN, MetroLine.YELLOW);
        Station banashankari    = graph.getOrCreateStation("GL14", "Banashankari", MetroLine.GREEN);
        Station jpNagar         = graph.getOrCreateStation("GL15", "JP Nagar", MetroLine.GREEN);
        Station yelachenahalli  = graph.getOrCreateStation("GL16", "Yelachenahalli", MetroLine.GREEN);
        Station silkInstitute   = graph.getOrCreateStation("GL17", "Silk Institute", MetroLine.GREEN);

        graph.addConnection(madavara, nagasandra, MetroLine.GREEN, 3.1, 4.5, true);
        graph.addConnection(nagasandra, peenyaIndustry, MetroLine.GREEN, 2.4, 3.5, true);
        graph.addConnection(peenyaIndustry, yeshwanthpur, MetroLine.GREEN, 3.5, 5.0, true);
        graph.addConnection(yeshwanthpur, sandalSoap, MetroLine.GREEN, 1.3, 2.0, true);
        graph.addConnection(sandalSoap, rajajinagar, MetroLine.GREEN, 1.7, 2.5, true);
        graph.addConnection(rajajinagar, mantriSquare, MetroLine.GREEN, 2.6, 4.0, true);
        graph.addConnection(mantriSquare, majestic, MetroLine.GREEN, 1.8, 2.5, true);
        graph.addConnection(majestic, chickpet, MetroLine.GREEN, 1.4, 2.5, true);
        graph.addConnection(chickpet, krMarket, MetroLine.GREEN, 1.2, 2.0, true);
        graph.addConnection(krMarket, lalbagh, MetroLine.GREEN, 1.6, 2.5, true);
        graph.addConnection(lalbagh, southEndCircle, MetroLine.GREEN, 1.5, 2.5, true);
        graph.addConnection(southEndCircle, jayanagar, MetroLine.GREEN, 1.2, 2.0, true);
        graph.addConnection(jayanagar, rvRoad, MetroLine.GREEN, 1.3, 2.0, true);
        graph.addConnection(rvRoad, banashankari, MetroLine.GREEN, 1.6, 2.5, true);
        graph.addConnection(banashankari, jpNagar, MetroLine.GREEN, 1.4, 2.5, true);
        graph.addConnection(jpNagar, yelachenahalli, MetroLine.GREEN, 2.2, 3.5, true);
        graph.addConnection(yelachenahalli, silkInstitute, MetroLine.GREEN, 6.5, 9.0, true);

        // 3. YELLOW LINE (RV Road to Bommasandra / Electronics City)
        Station ragigudda       = graph.getOrCreateStation("YL02", "Ragigudda", MetroLine.YELLOW);
        Station jayadeva        = graph.getOrCreateStation("YL03", "Jayadeva Hospital", MetroLine.YELLOW, MetroLine.PINK);
        Station btmLayout       = graph.getOrCreateStation("YL04", "BTM Layout", MetroLine.YELLOW);
        Station centralSilkBoard= graph.getOrCreateStation("YL05", "Central Silk Board", MetroLine.YELLOW, MetroLine.BLUE);
        Station bommanahalli    = graph.getOrCreateStation("YL06", "Bommanahalli", MetroLine.YELLOW);
        Station kudluGate       = graph.getOrCreateStation("YL07", "Kudlu Gate", MetroLine.YELLOW);
        Station singasandra     = graph.getOrCreateStation("YL08", "Singasandra", MetroLine.YELLOW);
        Station electronicCity  = graph.getOrCreateStation("YL09", "Electronic City", MetroLine.YELLOW);
        Station infosysKonappana= graph.getOrCreateStation("YL10", "Infosys Foundation Konappana", MetroLine.YELLOW);
        Station bommasandra     = graph.getOrCreateStation("YL11", "Bommasandra", MetroLine.YELLOW);

        graph.addConnection(rvRoad, ragigudda, MetroLine.YELLOW, 1.8, 2.5, true);
        graph.addConnection(ragigudda, jayadeva, MetroLine.YELLOW, 1.6, 2.5, true);
        graph.addConnection(jayadeva, btmLayout, MetroLine.YELLOW, 1.5, 2.5, true);
        graph.addConnection(btmLayout, centralSilkBoard, MetroLine.YELLOW, 1.7, 2.5, true);
        graph.addConnection(centralSilkBoard, bommanahalli, MetroLine.YELLOW, 1.6, 2.5, true);
        graph.addConnection(bommanahalli, kudluGate, MetroLine.YELLOW, 2.3, 3.5, true);
        graph.addConnection(kudluGate, singasandra, MetroLine.YELLOW, 1.8, 2.5, true);
        graph.addConnection(singasandra, electronicCity, MetroLine.YELLOW, 2.5, 3.5, true);
        graph.addConnection(electronicCity, infosysKonappana, MetroLine.YELLOW, 1.4, 2.0, true);
        graph.addConnection(infosysKonappana, bommasandra, MetroLine.YELLOW, 2.7, 4.0, true);

        // 4. PINK LINE (Kalena Agrahara to Nagawara)
        Station kalenaAgrahara  = graph.getOrCreateStation("PK01", "Kalena Agrahara", MetroLine.PINK);
        Station iimBangalore    = graph.getOrCreateStation("PK02", "IIM Bangalore", MetroLine.PINK);
        Station jpNagar4thPhase = graph.getOrCreateStation("PK03", "JP Nagar 4th Phase", MetroLine.PINK);
        Station dairyCircle     = graph.getOrCreateStation("PK04", "Dairy Circle", MetroLine.PINK);
        Station lakkasandra     = graph.getOrCreateStation("PK05", "Lakkasandra", MetroLine.PINK);
        Station langfordTown    = graph.getOrCreateStation("PK06", "Langford Town", MetroLine.PINK);
        Station shivajiNagar    = graph.getOrCreateStation("PK07", "Shivaji Nagar", MetroLine.PINK);
        Station cantonment      = graph.getOrCreateStation("PK08", "Bengaluru Cantonment", MetroLine.PINK);
        Station tanneryRoad     = graph.getOrCreateStation("PK09", "Tannery Road", MetroLine.PINK);
        Station nagawara        = graph.getOrCreateStation("PK10", "Nagawara", MetroLine.PINK, MetroLine.BLUE);

        graph.addConnection(kalenaAgrahara, iimBangalore, MetroLine.PINK, 2.4, 3.5, true);
        graph.addConnection(iimBangalore, jpNagar4thPhase, MetroLine.PINK, 1.5, 2.5, true);
        graph.addConnection(jpNagar4thPhase, jayadeva, MetroLine.PINK, 1.6, 2.5, true);
        graph.addConnection(jayadeva, dairyCircle, MetroLine.PINK, 2.1, 3.0, true);
        graph.addConnection(dairyCircle, lakkasandra, MetroLine.PINK, 1.4, 2.0, true);
        graph.addConnection(lakkasandra, langfordTown, MetroLine.PINK, 1.5, 2.5, true);
        graph.addConnection(langfordTown, mgRoad, MetroLine.PINK, 1.9, 3.0, true);
        graph.addConnection(mgRoad, shivajiNagar, MetroLine.PINK, 1.6, 2.5, true);
        graph.addConnection(shivajiNagar, cantonment, MetroLine.PINK, 1.4, 2.0, true);
        graph.addConnection(cantonment, tanneryRoad, MetroLine.PINK, 2.2, 3.5, true);
        graph.addConnection(tanneryRoad, nagawara, MetroLine.PINK, 3.4, 5.0, true);

        // 5. BLUE LINE (Outer Ring Road & Airport Express: Silk Board to KIAL Airport)
        Station hsrLayout       = graph.getOrCreateStation("BL02", "HSR Layout", MetroLine.BLUE);
        Station bellandur       = graph.getOrCreateStation("BL03", "Bellandur (Ecospace)", MetroLine.BLUE);
        Station kadubeesanahalli= graph.getOrCreateStation("BL04", "Kadubeesanahalli", MetroLine.BLUE);
        Station marathahalli    = graph.getOrCreateStation("BL05", "Marathahalli", MetroLine.BLUE);
        Station doddanekundi    = graph.getOrCreateStation("BL06", "Doddanekundi", MetroLine.BLUE);
        Station kalyanNagar     = graph.getOrCreateStation("BL08", "Kalyan Nagar", MetroLine.BLUE);
        Station hebbal          = graph.getOrCreateStation("BL09", "Hebbal", MetroLine.BLUE);
        Station yelahanka       = graph.getOrCreateStation("BL10", "Yelahanka", MetroLine.BLUE);
        Station doddajala       = graph.getOrCreateStation("BL11", "Doddajala", MetroLine.BLUE);
        Station kialAirport     = graph.getOrCreateStation("BL12", "KIAL Airport Terminal", MetroLine.BLUE);

        graph.addConnection(centralSilkBoard, hsrLayout, MetroLine.BLUE, 2.2, 3.0, true);
        graph.addConnection(hsrLayout, bellandur, MetroLine.BLUE, 3.1, 4.0, true);
        graph.addConnection(bellandur, kadubeesanahalli, MetroLine.BLUE, 1.9, 2.5, true);
        graph.addConnection(kadubeesanahalli, marathahalli, MetroLine.BLUE, 2.3, 3.0, true);
        graph.addConnection(marathahalli, doddanekundi, MetroLine.BLUE, 2.0, 2.5, true);
        graph.addConnection(doddanekundi, krPuram, MetroLine.BLUE, 3.4, 4.5, true);
        graph.addConnection(krPuram, kalyanNagar, MetroLine.BLUE, 4.2, 5.5, true);
        graph.addConnection(kalyanNagar, nagawara, MetroLine.BLUE, 3.1, 4.0, true);
        graph.addConnection(nagawara, hebbal, MetroLine.BLUE, 3.8, 5.0, true);
        graph.addConnection(hebbal, yeshwanthpur, MetroLine.BLUE, 5.6, 7.5, true);
        graph.addConnection(hebbal, yelahanka, MetroLine.BLUE, 6.7, 7.5, true);
        graph.addConnection(yelahanka, doddajala, MetroLine.BLUE, 9.4, 10.0, true);
        graph.addConnection(doddajala, kialAirport, MetroLine.BLUE, 8.2, 8.5, true);

        return graph;
    }
}

// ============================================================================
// 3. SERVICE FACADE
// ============================================================================

class MetroMindService {
    private final MetroGraph graph;
    private final RouteEngine routeEngine;
    private final FareEngine fareEngine;
    private final List<JourneySummary> recentJourneys;
    private final AtomicInteger journeyCounter;

    public MetroMindService() {
        this(NetworkDataInitializer.buildDefaultNetwork());
    }

    public MetroMindService(MetroGraph graph) {
        this.graph = Objects.requireNonNull(graph, "MetroGraph cannot be null");
        this.routeEngine = new RouteEngine(this.graph);
        this.fareEngine = new FareEngine();
        this.recentJourneys = new ArrayList<>();
        this.journeyCounter = new AtomicInteger(1001);
    }

    public MetroGraph getGraph() { return graph; }
    public RouteEngine getRouteEngine() { return routeEngine; }
    public FareEngine getFareEngine() { return fareEngine; }

    public JourneySummary planBestJourney(Station origin, Station destination, TicketType ticketType) {
        RouteResult routeResult = routeEngine.findBestRoute(origin, destination);
        Map<TicketType, FareBreakdown> allFares = fareEngine.calculateAllFareOptions(routeResult);

        String id = "MM-" + journeyCounter.getAndIncrement();
        JourneySummary summary = new JourneySummary(id, routeResult, allFares, ticketType);

        if (routeResult.isFound()) {
            recentJourneys.add(summary);
            if (recentJourneys.size() > 25) {
                recentJourneys.remove(0);
            }
        }
        return summary;
    }

    public JourneySummary planJourney(Station origin, Station destination, RouteEngine.Criteria criteria, TicketType ticketType) {
        RouteResult routeResult = routeEngine.findRoute(origin, destination, criteria);
        Map<TicketType, FareBreakdown> allFares = fareEngine.calculateAllFareOptions(routeResult);

        String id = "MM-" + journeyCounter.getAndIncrement();
        JourneySummary summary = new JourneySummary(id, routeResult, allFares, ticketType);

        if (routeResult.isFound()) {
            recentJourneys.add(summary);
            if (recentJourneys.size() > 25) {
                recentJourneys.remove(0);
            }
        }
        return summary;
    }

    public Station getStation(String name) {
        return graph.getStation(name);
    }

    public List<Station> searchStations(String query) {
        return graph.searchStations(query);
    }

    public Collection<Station> getAllStations() {
        return graph.getAllStations();
    }

    public List<Station> getStationsByLine(MetroLine line) {
        return graph.getStationsByLine(line);
    }

    public List<Station> getInterchangeStations() {
        return graph.getInterchangeStations();
    }

    public List<JourneySummary> getRecentJourneys() {
        return Collections.unmodifiableList(recentJourneys);
    }

    public boolean toggleStationStatus(String stationName, boolean active) {
        return graph.toggleStationStatus(stationName, active);
    }

    public boolean toggleTrackStatus(String from, String to, boolean operational) {
        return graph.toggleTrackStatus(from, to, operational);
    }

    public Station addStation(String id, String name, MetroLine... lines) {
        return graph.getOrCreateStation(id, name, lines);
    }

    public void addTrackConnection(Station from, Station to, MetroLine line, double dist, double time) {
        graph.addConnection(from, to, line, dist, time, true);
    }

    public String exportReceipt(JourneySummary journey) throws IOException {
        return FileExporter.exportJourneyReceipt(journey);
    }

    public String exportRouteCsv(JourneySummary journey) throws IOException {
        return FileExporter.exportRouteToCsv(journey);
    }

    public String exportNetworkCsv() throws IOException {
        return FileExporter.exportNetworkDirectoryCsv(graph);
    }
}

// ============================================================================
// 4. TERMINAL GRAPHICAL VISUALIZER
// ============================================================================

class AsciiRouteVisualizer {
    public static void renderGraphicalRoute(RouteResult route) {
        if (route == null || !route.isFound()) {
            System.out.println("(!) No graphical route available for display.");
            return;
        }

        System.out.println();
        System.out.println("================================================================================");
        System.out.println("              VISUAL METRO TRACK SCHEMATIC (TERMINAL GRAPHICS)                 ");
        System.out.println("================================================================================");

        renderCorridorOverview(route);
        renderTrackSchematic(route);
        renderMetricsBar(route);
        System.out.println("================================================================================\n");
    }

    private static void renderCorridorOverview(RouteResult route) {
        System.out.println("CORRIDOR OVERVIEW:");
        StringBuilder sb = new StringBuilder();
        List<RouteSegment> segments = route.getSegments();

        if (segments.isEmpty() && !route.getPath().isEmpty()) {
            System.out.println("  [" + route.getOrigin().getName() + "] (Single Station)\n");
            return;
        }

        for (int i = 0; i < segments.size(); i++) {
            RouteSegment seg = segments.get(i);
            if (i == 0) {
                sb.append("  [").append(seg.getStartStation().getName()).append("]");
            }
            sb.append(" ==(").append(seg.getLine().getDisplayName()).append(": ")
              .append(seg.getStationCount()).append(" stops)=>> [")
              .append(seg.getEndStation().getName()).append("]");
        }
        System.out.println(sb.toString());
        System.out.println();
    }

    private static void renderTrackSchematic(RouteResult route) {
        System.out.println("DETAILED ROUTE SCHEMATIC:");
        List<Station> path = route.getPath();
        List<Connection> connections = route.getConnections();
        List<Station> interchanges = route.getInterchanges();

        if (path.isEmpty()) return;

        Station origin = path.get(0);
        Station destination = path.get(path.size() - 1);

        System.out.println("  +----------------------------------------------------------------------------+");
        System.out.printf("  | [START]      *  %-58s |\n", truncate(origin.getName(), 58));

        MetroLine currentLine = (connections.isEmpty()) ? null : connections.get(0).getLine();

        for (int i = 0; i < connections.size(); i++) {
            Connection conn = connections.get(i);
            Station st = conn.getTo();
            boolean isDestination = (i == connections.size() - 1);

            if (conn.getLine() != currentLine) {
                System.out.println("  |              |                                                             |");
                System.out.printf("  | [INTERCHANGE] <-> %-56s |\n", truncate(conn.getFrom().getName(), 56));
                System.out.println("  |  #======================================================================#  |");
                System.out.printf("  |  #  TRANSFER: Switch from [%-11s] -> Platform for [%-15s] #  |\n",
                        truncate(currentLine.getDisplayName(), 11), truncate(conn.getLine().getDisplayName(), 15));
                System.out.printf("  |  #  Transfer Walking & Dwell Buffer: %-4.1f mins                          #  |\n",
                        conn.getLine().getTransferPenaltyMinutes());
                System.out.println("  |  #======================================================================#  |");
                currentLine = conn.getLine();
            }

            System.out.printf("  |              |   | [%s] (%.1f km, %.1f min)\n",
                    conn.getLine().getDisplayName(), conn.getDistanceKm(), conn.getTimeMinutes());

            if (isDestination) {
                System.out.printf("  | [DESTINATION] #  %-58s |\n", truncate(destination.getName(), 58));
            } else {
                String tag = interchanges.contains(st) ? " [Junction]" : "";
                System.out.printf("  |              o  %-58s |\n", truncate(st.getName() + tag, 58));
            }
        }

        System.out.println("  +----------------------------------------------------------------------------+");
    }

    private static void renderMetricsBar(RouteResult route) {
        System.out.println();
        System.out.printf("  TOTAL COMMUTE : %.2f km  |  ~%.1f minutes  |  %d Stops  |  %d Line Transfers\n",
                route.getTotalDistanceKm(),
                route.getTotalTimeMinutes(),
                route.getTotalStations(),
                route.getInterchangeCount());
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 3) + "...";
    }
}

// ============================================================================
// 5. FILE EXPORTER & TERMINAL UTILITIES
// ============================================================================

class FileExporter {
    private static final String EXPORT_DIR = "exports";

    static {
        File dir = new File(EXPORT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static String exportJourneyReceipt(JourneySummary journey) throws IOException {
        String filename = EXPORT_DIR + File.separator + "receipt_" + journey.getJourneyId() + ".txt";
        RouteResult route = journey.getRouteResult();
        FareBreakdown fare = journey.getSelectedFare();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("========================================================================\n");
            writer.write("                    METROMIND TRANSIT NAVIGATION TICKET                \n");
            writer.write("========================================================================\n");
            writer.write(String.format("Ticket ID       : %s\n", journey.getJourneyId()));
            writer.write(String.format("Issued At       : %s\n", journey.getFormattedTimestamp()));
            writer.write(String.format("Origin          : %s\n", route.getOrigin().getName()));
            writer.write(String.format("Destination     : %s\n", route.getDestination().getName()));
            writer.write(String.format("Optimization    : %s\n", route.getOptimizationCriteria()));
            writer.write("------------------------------------------------------------------------\n");
            writer.write(String.format("Total Stations  : %d stops\n", route.getTotalStations()));
            writer.write(String.format("Total Distance  : %.2f km\n", route.getTotalDistanceKm()));
            writer.write(String.format("Estimated Time  : %.1f minutes\n", route.getTotalTimeMinutes()));
            writer.write(String.format("Interchanges    : %d line switches\n", route.getInterchangeCount()));
            if (route.getInterchangeCount() > 0) {
                writer.write("Transfer Points : ");
                for (int i = 0; i < route.getInterchanges().size(); i++) {
                    if (i > 0) writer.write(", ");
                    writer.write(route.getInterchanges().get(i).getName());
                }
                writer.write("\n");
            }
            writer.write("------------------------------------------------------------------------\n");
            writer.write("JOURNEY ITINERARY SEGMENTS:\n");
            int segNum = 1;
            for (RouteSegment seg : route.getSegments()) {
                writer.write(String.format("  [%d] Line: %-15s | From: %-20s -> To: %-20s\n",
                        segNum++, seg.getLine().getDisplayName(), seg.getStartStation().getName(), seg.getEndStation().getName()));
                writer.write(String.format("      Stops: %-2d | Distance: %-6.2f km | Time: %-5.1f min\n",
                        seg.getStationCount(), seg.getDistanceKm(), seg.getTimeMinutes()));
            }
            writer.write("------------------------------------------------------------------------\n");
            writer.write("FARE COMPUTATION AUDIT:\n");
            writer.write(String.format("Ticket Category : %s\n", fare.getTicketType().getTitle()));
            writer.write(String.format("Distance Slab   : %s\n", fare.getSlabName()));
            writer.write(String.format("Base Fare       : ₹%.2f\n", fare.getBaseFare()));
            if (fare.getAirportSurcharge() > 0) {
                writer.write(String.format("Airport Surcharge: ₹%.2f\n", fare.getAirportSurcharge()));
            }
            writer.write(String.format("Concession/Disc : -₹%.2f (%.0f%%)\n",
                    fare.getDiscountAmount(), fare.getTicketType().getDiscountPercentage()));
            writer.write(String.format("TOTAL PAYABLE   : ₹%.2f\n", fare.getFinalFare()));
            writer.write("========================================================================\n");
            writer.write("           Thank you for choosing MetroMind Rapid Transit!              \n");
            writer.write("========================================================================\n");
        }
        return filename;
    }

    public static String exportRouteToCsv(JourneySummary journey) throws IOException {
        String filename = EXPORT_DIR + File.separator + "route_" + journey.getJourneyId() + ".csv";
        RouteResult route = journey.getRouteResult();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("StepNumber,StationName,Line,InterchangeStatus,DistanceKmFromPrevious,TimeMinFromPrevious\n");
            List<Station> path = route.getPath();
            List<Connection> connections = route.getConnections();

            if (!path.isEmpty()) {
                writer.write(String.format("1,\"%s\",%s,%s,0.00,0.0\n",
                        path.get(0).getName(),
                        connections.isEmpty() ? "None" : connections.get(0).getLine().getDisplayName(),
                        path.get(0).isInterchange() ? "YES" : "NO"));

                for (int i = 0; i < connections.size(); i++) {
                    Connection c = connections.get(i);
                    Station st = c.getTo();
                    writer.write(String.format("%d,\"%s\",\"%s\",%s,%.2f,%.1f\n",
                            i + 2,
                            st.getName(),
                            c.getLine().getDisplayName(),
                            st.isInterchange() ? "YES" : "NO",
                            c.getDistanceKm(),
                            c.getTimeMinutes()));
                }
            }
        }
        return filename;
    }

    public static String exportNetworkDirectoryCsv(MetroGraph graph) throws IOException {
        String filename = EXPORT_DIR + File.separator + "metro_network_stations.csv";
        Collection<Station> stations = graph.getAllStations();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("StationID,StationName,Lines,IsInterchange,IsActive\n");
            for (Station s : stations) {
                writer.write(String.format("\"%s\",\"%s\",\"%s\",%s,%s\n",
                        s.getId(),
                        s.getName(),
                        s.getLinesString(),
                        s.isInterchange() ? "YES" : "NO",
                        s.isActive() ? "ACTIVE" : "MAINTENANCE"));
            }
        }
        return filename;
    }
}

class ConsoleUtils {
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
            if (!scanner.hasNextLine()) {
                return min;
            }
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
            if (!scanner.hasNextLine()) {
                return min;
            }
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
            if (!scanner.hasNextLine()) {
                return "";
            }
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("(!) Input cannot be empty. Please enter a valid string.");
        }
    }

    public static void pressEnterToContinue(Scanner scanner) {
        System.out.print("\nPress [Enter] to return to the menu...");
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
    }
}

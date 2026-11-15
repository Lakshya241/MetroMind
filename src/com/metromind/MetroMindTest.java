package com.metromind;

import com.metromind.fare.FareEngine;
import com.metromind.model.*;
import com.metromind.routing.RouteEngine.Criteria;
import com.metromind.service.MetroMindService;

import java.io.File;

/**
 * Automated Verification Suite for Bengaluru Namma Metro Engine.
 * Tests graph construction, multi-criteria routing, fare slabs,
 * corridor surcharges, disruption handling, and file export.
 */
public class MetroMindTest {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   METROMIND (NAMMA METRO) VERIFICATION SUITE     ");
        System.out.println("==================================================");

        int totalTests = 0;
        int passedTests = 0;

        MetroMindService service = new MetroMindService();

        // TEST 1: Station Directory & Connectivity
        totalTests++;
        System.out.print("[TEST 1] Bengaluru Station Directory & Connectivity: ");
        int stationCount = service.getAllStations().size();
        int interchangeCount = service.getInterchangeStations().size();
        if (stationCount >= 30 && interchangeCount >= 5) {
            System.out.println("PASSED (" + stationCount + " stations, " + interchangeCount + " interchanges)");
            passedTests++;
        } else {
            System.out.println("FAILED (" + stationCount + " stations, " + interchangeCount + " interchanges)");
        }

        // TEST 2: Direct Single-Line Journey (Whitefield to Majestic on Purple Line)
        totalTests++;
        System.out.print("[TEST 2] Direct Purple Line Route (Whitefield -> Majestic): ");
        Station whitefield = service.getStation("Whitefield (Kadugodi)");
        Station majestic = service.getStation("Nadaprabhu Kempegowda Station, Majestic");
        JourneySummary whitefieldToMajestic = service.planJourney(whitefield, majestic, Criteria.SHORTEST_DISTANCE, TicketType.TOKEN);
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
        Station madavara = service.getStation("Madavara (BIEC)");
        Station silkInstitute = service.getStation("Silk Institute");
        JourneySummary greenLineTrip = service.planJourney(madavara, silkInstitute, Criteria.FEWEST_INTERCHANGES, TicketType.TOKEN);
        if (greenLineTrip.getRouteResult().isFound() && greenLineTrip.getRouteResult().getInterchangeCount() == 0) {
            System.out.printf("PASSED (Direct 0-transfer Green Line: %.2f km)\n",
                    greenLineTrip.getRouteResult().getTotalDistanceKm());
            passedTests++;
        } else {
            System.out.println("FAILED");
        }

        // TEST 4: Multi-Line Interchange Routing (Whitefield to Lalbagh via Majestic)
        totalTests++;
        System.out.print("[TEST 4] Multi-Line Interchange Route (Whitefield -> Lalbagh): ");
        Station lalbagh = service.getStation("Lalbagh");
        JourneySummary transferTrip = service.planJourney(whitefield, lalbagh, Criteria.FEWEST_INTERCHANGES, TicketType.TOKEN);
        if (transferTrip.getRouteResult().isFound() && transferTrip.getRouteResult().getInterchangeCount() >= 1) {
            System.out.printf("PASSED (Transfers: %d at %s)\n",
                    transferTrip.getRouteResult().getInterchangeCount(),
                    transferTrip.getRouteResult().getInterchanges().get(0).getName());
            passedTests++;
        } else {
            System.out.println("FAILED");
        }

        // TEST 5: Fare Calculation Slabs & Smart Card Discounts
        totalTests++;
        System.out.print("[TEST 5] Fare Calculation Slabs & Namma Metro Discounts: ");
        // Whitefield to Majestic is ~24.1 km -> Slab 21.1-32.0 km -> Base ₹50
        FareBreakdown tokenFare = whitefieldToMajestic.getSelectedFare();
        FareBreakdown smartCardFare = service.getFareEngine().calculateFare(whitefieldToMajestic.getRouteResult(), TicketType.SMART_CARD);
        FareBreakdown studentFare = service.getFareEngine().calculateFare(whitefieldToMajestic.getRouteResult(), TicketType.STUDENT_PASS);

        boolean fareMatch = (tokenFare.getBaseFare() == 50.0) &&
                (smartCardFare.getFinalFare() == 45.0) && // 10% off of 50 = 45
                (studentFare.getFinalFare() == 37.5);    // 25% off of 50 = 37.5

        if (fareMatch) {
            System.out.println("PASSED (Token: ₹50, SmartCard: ₹45, Student: ₹37.5)");
            passedTests++;
        } else {
            System.out.printf("FAILED (Token: %.1f, SmartCard: %.1f, Student: %.1f)\n",
                    tokenFare.getBaseFare(), smartCardFare.getFinalFare(), studentFare.getFinalFare());
        }

        // TEST 6: Airport Express Line Surcharge (Hebbal to KIAL Airport Terminal)
        totalTests++;
        System.out.print("[TEST 6] Airport Line Surcharge (Hebbal -> KIAL Airport Terminal): ");
        Station hebbal = service.getStation("Hebbal");
        Station kialAirport = service.getStation("KIAL Airport Terminal");
        JourneySummary airportTrip = service.planJourney(hebbal, kialAirport, Criteria.FASTEST_TIME, TicketType.TOKEN);
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
        // Test route from Indiranagar (Purple Line) to Rajajinagar (Green Line)
        Station indiranagar = service.getStation("Indiranagar");
        Station rajajinagar = service.getStation("Rajajinagar");

        // Simulate closing Majestic for maintenance
        service.toggleStationStatus("Nadaprabhu Kempegowda Station, Majestic", false);
        JourneySummary afterClosure = service.planJourney(indiranagar, rajajinagar, Criteria.FEWEST_INTERCHANGES, TicketType.TOKEN);

        // Restore Majestic
        service.toggleStationStatus("Nadaprabhu Kempegowda Station, Majestic", true);

        if (afterClosure.getRouteResult().isFound() && !afterClosure.getRouteResult().getPath().contains(majestic)) {
            System.out.println("PASSED (Successfully avoided closed station Majestic and rerouted via alternative lines)");
            passedTests++;
        } else {
            System.out.println("FAILED (Path still uses Majestic or not found)");
        }

        // TEST 8: File Export (Receipt TXT & CSV)
        totalTests++;
        System.out.print("[TEST 8] File Export (Receipt .txt and Route .csv): ");
        try {
            String txtFile = service.exportReceipt(whitefieldToMajestic);
            String csvFile = service.exportRouteCsv(whitefieldToMajestic);
            String netCsv = service.exportNetworkCsv();

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

        System.out.println("==================================================");
        System.out.printf("   TEST RESULTS: %d / %d TESTS PASSED (%.1f%%)\n",
                passedTests, totalTests, (passedTests * 100.0 / totalTests));
        System.out.println("==================================================");

        if (passedTests == totalTests) {
            System.out.println(">>> ALL TESTS PASSED! BENGALURU METRO ENGINE FULLY VERIFIED! <<<");
        }
    }
}

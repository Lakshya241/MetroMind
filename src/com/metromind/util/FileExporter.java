package com.metromind.util;

import com.metromind.graph.MetroGraph;
import com.metromind.model.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Stretch Feature: Exports journey details, tickets, and network data
 * to human-readable text receipts and analytical CSV spreadsheets.
 */
public class FileExporter {

    private static final String EXPORT_DIR = "exports";

    static {
        File dir = new File(EXPORT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Exports a formal travel ticket / receipt to a text file.
     */
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
            writer.write(String.format("Base Fare       : INR %.2f\n", fare.getBaseFare()));
            if (fare.getAirportSurcharge() > 0) {
                writer.write(String.format("Airport Surcharge: INR %.2f\n", fare.getAirportSurcharge()));
            }
            writer.write(String.format("Concession/Disc : - INR %.2f (%.0f%%)\n",
                    fare.getDiscountAmount(), fare.getTicketType().getDiscountPercentage()));
            writer.write(String.format("TOTAL PAYABLE   : INR %.2f\n", fare.getFinalFare()));
            writer.write("========================================================================\n");
            writer.write("           Thank you for choosing MetroMind Rapid Transit!              \n");
            writer.write("========================================================================\n");
        }
        return filename;
    }

    /**
     * Exports full route step-by-step path to CSV.
     */
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

    /**
     * Exports the entire network directory to CSV.
     */
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

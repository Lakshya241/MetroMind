package util;

import model.Connection;
import model.MetroLine;
import model.RouteResult;
import model.RouteSegment;
import model.Station;

import java.util.List;

/**
 * Terminal-based Graphical Visualizer for Metro Routes.
 * Renders a visual schematic track map with line badges,
 * transfer bridges, and milestone overviews right inside the terminal.
 */
public class AsciiRouteVisualizer {

    public static void renderGraphicalRoute(RouteResult route) {
        if (route == null || !route.isFound()) {
            System.out.println("(!) No graphical route available for display.");
            return;
        }

        System.out.println();
        System.out.println("================================================================================");
        System.out.println("              VISUAL METRO TRACK SCHEMATIC (TERMINAL GRAPHICS)                 ");
        System.out.println("================================================================================");

        // 1. High-Level Corridor Overview
        renderCorridorOverview(route);

        // 2. Detailed Track Map Schematic
        renderTrackSchematic(route);

        // 3. Journey Summary Metrics Bar
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
            boolean isInterchange = interchanges.contains(conn.getFrom());

            if (conn.getLine() != currentLine) {
                // Line switch occurred at conn.getFrom()
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

            // Track connector line
            System.out.printf("  |              |   | [%s] (%.1f km, %.1f min)\n",
                    conn.getLine().getDisplayName(), conn.getDistanceKm(), conn.getTimeMinutes());

            // Station node
            if (isDestination) {
                System.out.printf("  | [DESTINATION] #  %-58s |\n", truncate(st.getName(), 58));
            } else if (st.isInterchange()) {
                System.out.printf("  |              o  %-58s |\n", truncate(st.getName() + " [Junction]", 58));
            } else {
                System.out.printf("  |              o  %-58s |\n", truncate(st.getName(), 58));
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

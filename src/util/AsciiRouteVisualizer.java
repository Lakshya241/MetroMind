package util;

import model.Connection;
import model.MetroLine;
import model.RouteResult;
import model.RouteSegment;
import model.Station;

import java.util.List;

/**
 * Terminal-based Graphical Visualizer for Metro Routes.
 *
 * Renders a polished schematic track map with Unicode box-drawing characters,
 * colored line badges, interchange transfer bridges, and a journey metrics bar.
 */
public class AsciiRouteVisualizer {

    // Box width (inner content area, not counting the two side '|' chars)
    private static final int BOX_INNER = 74;

    // Box-drawing character references (via ConsoleUtils helpers to avoid
    // any encoding issues when this file is compiled on different platforms)
    // Double-line box
    private static String DH()  { return ConsoleUtils.dblH();  }  // = (double horiz)
    private static String DV()  { return ConsoleUtils.dblV();  }  // | (double vert)
    private static String DTL() { return ConsoleUtils.dblTL(); }  // top-left corner
    private static String DTR() { return ConsoleUtils.dblTR(); }  // top-right corner
    private static String DBL() { return ConsoleUtils.dblBL(); }  // bottom-left corner
    private static String DBR() { return ConsoleUtils.dblBR(); }  // bottom-right corner
    // Double horiz separator with double-vert sides (\u2560 / \u2563 or + in ASCII)
    private static String DLT() { return ConsoleUtils.dblLM(); } // double left-tee
    private static String DRT() { return ConsoleUtils.dblRM(); } // double right-tee

    // Single-line
    private static String SH()  { return ConsoleUtils.sglH();  }  // - (single horiz)
    private static String SLT() { return ConsoleUtils.sglLM(); } // single left-tee
    private static String SRT() { return ConsoleUtils.sglRM(); } // single right-tee

    public static void renderGraphicalRoute(RouteResult route) {
        if (route == null || !route.isFound()) {
            ConsoleUtils.printWarning("No graphical route available for display.");
            return;
        }

        System.out.println();
        printBoxTop();
        printBoxTitle("  VISUAL METRO TRACK SCHEMATIC");
        printBoxSep();

        // 1. High-Level Corridor Overview
        renderCorridorOverview(route);

        // 2. Detailed Track Map Schematic
        printBoxSep();
        renderTrackSchematic(route);

        // 3. Journey Metrics Bar
        printBoxSep();
        renderMetricsBar(route);
        printBoxBottom();
        System.out.println();
    }

    // -------------------------------------------------------------------------
    // Section renderers
    // -------------------------------------------------------------------------

    private static void renderCorridorOverview(RouteResult route) {
        List<RouteSegment> segments = route.getSegments();

        printBoxLine(ConsoleUtils.colored("  CORRIDOR OVERVIEW", ConsoleUtils.BOLD));

        if (segments.isEmpty() && !route.getPath().isEmpty()) {
            printBoxLine("  " + badge(route.getOrigin().getName())
                    + "  (Single Station - no travel needed)");
            return;
        }

        // Build corridor string; may exceed box width on very long routes -- that's acceptable
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < segments.size(); i++) {
            RouteSegment seg = segments.get(i);
            if (i == 0) {
                sb.append(badge(seg.getStartStation().getName()));
            }
            String linePart = " ==[" + seg.getLine().getDisplayName()
                    + " . " + seg.getStationCount() + " stops]==> ";
            sb.append(ConsoleUtils.colored(linePart, ConsoleUtils.CYAN));
            sb.append(badge(seg.getEndStation().getName()));
        }
        printBoxLine("  " + sb);
    }

    private static void renderTrackSchematic(RouteResult route) {
        printBoxLine(ConsoleUtils.colored("  DETAILED ROUTE SCHEMATIC", ConsoleUtils.BOLD));

        List<Station>    path         = route.getPath();
        List<Connection> connections  = route.getConnections();

        if (path.isEmpty()) return;

        Station origin = path.get(0);

        // Origin node
        String originLabel = ConsoleUtils.colored("  [START]  ", ConsoleUtils.GREEN + ConsoleUtils.BOLD)
                           + ConsoleUtils.colored(truncate(origin.getName(), 54),
                                   ConsoleUtils.WHITE + ConsoleUtils.BOLD);
        printBoxLine(originLabel);

        MetroLine currentLine = connections.isEmpty() ? null : connections.get(0).getLine();

        for (int i = 0; i < connections.size(); i++) {
            Connection conn = connections.get(i);
            Station    st   = conn.getTo();
            boolean isDestination = (i == connections.size() - 1);

            // ---- Interchange / line-switch block ----------------------------
            if (conn.getLine() != currentLine && currentLine != null) {
                String pivot = conn.getFrom().getName();
                printBoxLine(ConsoleUtils.colored("  |", ConsoleUtils.GRAY));
                printBoxLine(ConsoleUtils.colored(
                        "  +-- INTERCHANGE ------------------------------------------------",
                        ConsoleUtils.YELLOW));
                printBoxLine(ConsoleUtils.colored("  |  ", ConsoleUtils.YELLOW)
                           + ConsoleUtils.colored("<> " + truncate(pivot, 48),
                                   ConsoleUtils.YELLOW + ConsoleUtils.BOLD));
                printBoxLine(ConsoleUtils.colored("  |  ", ConsoleUtils.YELLOW)
                           + ConsoleUtils.colored("From: " + currentLine.getDisplayName(),
                                   ConsoleUtils.MAGENTA)
                           + ConsoleUtils.colored("   ->   ", ConsoleUtils.GRAY)
                           + ConsoleUtils.colored("To: " + conn.getLine().getDisplayName(),
                                   ConsoleUtils.CYAN));
                printBoxLine(ConsoleUtils.colored("  |  ", ConsoleUtils.YELLOW)
                           + ConsoleUtils.dim("Walking + dwell: ~"
                           + String.format("%.1f", conn.getLine().getTransferPenaltyMinutes()) + " min"));
                printBoxLine(ConsoleUtils.colored(
                        "  +------------------------------------------------------------",
                        ConsoleUtils.YELLOW));
                currentLine = conn.getLine();
            }

            // ---- Track connector line ---------------------------------------
            String trackLine = ConsoleUtils.colored("  |", ConsoleUtils.GRAY)
                             + "   " + ConsoleUtils.colored("[" + conn.getLine().getDisplayName() + "]",
                                     ConsoleUtils.CYAN)
                             + ConsoleUtils.dim(String.format("  %.1f km  %.1f min",
                                     conn.getDistanceKm(), conn.getTimeMinutes()));
            printBoxLine(trackLine);

            // ---- Station node ----------------------------------------------
            if (isDestination) {
                String destLabel = ConsoleUtils.colored("  [ END ]  ",
                        ConsoleUtils.RED + ConsoleUtils.BOLD)
                                 + ConsoleUtils.colored(truncate(st.getName(), 54),
                                         ConsoleUtils.WHITE + ConsoleUtils.BOLD);
                printBoxLine(destLabel);
            } else if (st.isInterchange()) {
                String jLabel = ConsoleUtils.colored("  o", ConsoleUtils.YELLOW + ConsoleUtils.BOLD)
                              + " " + ConsoleUtils.colored(truncate(st.getName(), 50), ConsoleUtils.YELLOW)
                              + ConsoleUtils.dim("  [Junction]");
                printBoxLine(jLabel);
            } else {
                printBoxLine(ConsoleUtils.colored("  o", ConsoleUtils.GRAY)
                        + "  " + truncate(st.getName(), 58));
            }
        }
    }

    private static void renderMetricsBar(RouteResult route) {
        printBoxLine(ConsoleUtils.colored("  JOURNEY SUMMARY", ConsoleUtils.BOLD));
        String metrics = String.format(
            "  Distance: %s   Est. Time: %s   Stops: %s   Transfers: %s",
            ConsoleUtils.colored(String.format("%.2f km", route.getTotalDistanceKm()),
                    ConsoleUtils.CYAN + ConsoleUtils.BOLD),
            ConsoleUtils.colored(String.format("~%.0f min", route.getTotalTimeMinutes()),
                    ConsoleUtils.CYAN + ConsoleUtils.BOLD),
            ConsoleUtils.colored(String.valueOf(route.getTotalStations()),
                    ConsoleUtils.CYAN + ConsoleUtils.BOLD),
            ConsoleUtils.colored(String.valueOf(route.getInterchangeCount()),
                    ConsoleUtils.CYAN + ConsoleUtils.BOLD)
        );
        printBoxLine(metrics);
    }

    // -------------------------------------------------------------------------
    // Box-drawing helpers
    // -------------------------------------------------------------------------

    private static void printBoxTop() {
        String s = DTL() + ConsoleUtils.repeat(DH(), BOX_INNER) + DTR();
        System.out.println(ConsoleUtils.ansiEnabled
                ? (ConsoleUtils.CYAN + ConsoleUtils.BOLD + s + ConsoleUtils.RESET) : s);
    }

    private static void printBoxBottom() {
        String s = DBL() + ConsoleUtils.repeat(DH(), BOX_INNER) + DBR();
        System.out.println(ConsoleUtils.ansiEnabled
                ? (ConsoleUtils.CYAN + ConsoleUtils.BOLD + s + ConsoleUtils.RESET) : s);
    }

    private static void printBoxSep() {
        String s = DLT() + ConsoleUtils.repeat(DH(), BOX_INNER) + DRT();
        System.out.println(ConsoleUtils.ansiEnabled
                ? (ConsoleUtils.CYAN + s + ConsoleUtils.RESET) : s);
    }

    private static void printBoxTitle(String text) {
        int visLen = stripAnsi(text).length();
        int pad = Math.max(0, (BOX_INNER - visLen) / 2);
        String padded = ConsoleUtils.repeat(" ", pad) + text;
        int paddedVis = stripAnsi(padded).length();
        String trailing = ConsoleUtils.repeat(" ", Math.max(0, BOX_INNER - paddedVis));
        String s = DV() + padded + trailing + DV();
        System.out.println(ConsoleUtils.ansiEnabled
                ? (ConsoleUtils.CYAN + ConsoleUtils.BOLD + s + ConsoleUtils.RESET) : s);
    }

    /**
     * Prints a content line inside the outer box, padding the visible
     * (non-ANSI) content to exactly BOX_INNER characters wide.
     */
    private static void printBoxLine(String content) {
        int visibleLen = stripAnsi(content).length();
        int padding = Math.max(0, BOX_INNER - visibleLen);
        System.out.println(DV() + content + ConsoleUtils.repeat(" ", padding) + DV());
    }

    // -------------------------------------------------------------------------
    // Utility helpers
    // -------------------------------------------------------------------------

    /** Returns a bracketed badge string for a station name. */
    private static String badge(String name) {
        return ConsoleUtils.colored("[ " + truncate(name, 32) + " ]",
                ConsoleUtils.WHITE + ConsoleUtils.BOLD);
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 3) + "...";
    }

    /** Strips ANSI escape sequences to get the visible character count. */
    private static String stripAnsi(String s) {
        if (s == null) return "";
        return s.replaceAll("\u001B\\[[;\\d]*m", "");
    }
}

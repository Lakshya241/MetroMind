package com.metromind.service;

import com.metromind.fare.FareEngine;
import com.metromind.graph.MetroGraph;
import com.metromind.model.*;
import com.metromind.routing.RouteEngine;
import com.metromind.routing.RouteEngine.Criteria;
import com.metromind.util.FileExporter;
import com.metromind.util.NetworkDataInitializer;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service Layer (Facade) orchestrating the transit graph,
 * routing engine, fare engine, and audit logging.
 */
public class MetroMindService {
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

    public MetroGraph getGraph() {
        return graph;
    }

    public RouteEngine getRouteEngine() {
        return routeEngine;
    }

    public FareEngine getFareEngine() {
        return fareEngine;
    }

    /**
     * Executes journey planning for given origin, destination, optimization preference, and ticket type.
     */
    public JourneySummary planJourney(Station origin, Station destination, Criteria criteria, TicketType ticketType) {
        RouteResult routeResult = routeEngine.findRoute(origin, destination, criteria);
        Map<TicketType, FareBreakdown> allFares = fareEngine.calculateAllFareOptions(routeResult);

        String id = "MM-" + journeyCounter.getAndIncrement();
        JourneySummary summary = new JourneySummary(id, routeResult, allFares, ticketType);

        if (routeResult.isFound()) {
            recentJourneys.add(summary);
            // Retain latest 25 journeys
            if (recentJourneys.size() > 25) {
                recentJourneys.remove(0);
            }
        }
        return summary;
    }

    /**
     * Multi-criteria route comparison:
     * Computes Shortest Distance, Fastest Travel Time, and Fewest Interchanges simultaneously.
     */
    public Map<Criteria, RouteResult> compareRoutes(Station origin, Station destination) {
        Map<Criteria, RouteResult> comparison = new LinkedHashMap<>();
        for (Criteria c : Criteria.values()) {
            comparison.put(c, routeEngine.findRoute(origin, destination, c));
        }
        return comparison;
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

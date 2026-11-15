package com.metromind.routing;

import com.metromind.graph.MetroGraph;
import com.metromind.model.*;

import java.util.*;

/**
 * Advanced multi-criteria transit routing engine.
 * Implements Dijkstra's algorithm with state expansion for:
 * 1. Shortest Physical Distance (km)
 * 2. Fastest Travel Time (minutes, including transfer dwell delays)
 * 3. Fewest Interchanges / Line Switches (transfer minimization)
 */
public class RouteEngine {
    private final MetroGraph graph;

    public RouteEngine(MetroGraph graph) {
        this.graph = Objects.requireNonNull(graph, "MetroGraph cannot be null");
    }

    /**
     * Enum for optimization criteria
     */
    public enum Criteria {
        SHORTEST_DISTANCE("Shortest Distance"),
        FASTEST_TIME("Fastest Travel Time"),
        FEWEST_INTERCHANGES("Fewest Line Interchanges");

        private final String label;
        Criteria(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    /**
     * Compute optimal route according to the requested criteria.
     */
    public RouteResult findRoute(Station origin, Station destination, Criteria criteria) {
        if (origin == null || destination == null) {
            return RouteResult.notFound(origin, destination, criteria.getLabel());
        }

        if (!origin.isActive() || !destination.isActive()) {
            return RouteResult.notFound(origin, destination, criteria.getLabel());
        }

        if (origin.equals(destination)) {
            List<Station> single = Collections.singletonList(origin);
            return new RouteResult(origin, destination, single, Collections.emptyList(),
                    Collections.emptyList(), Collections.emptyList(), 0.0, 0.0, criteria.getLabel());
        }

        switch (criteria) {
            case SHORTEST_DISTANCE:
                return dijkstraDistance(origin, destination);
            case FASTEST_TIME:
                return dijkstraTime(origin, destination);
            case FEWEST_INTERCHANGES:
                return dijkstraFewestInterchanges(origin, destination);
            default:
                return dijkstraDistance(origin, destination);
        }
    }

    /**
     * Finds the route minimizing total physical kilometers.
     */
    private RouteResult dijkstraDistance(Station origin, Station destination) {
        PriorityQueue<PathNode> pq = new PriorityQueue<>();
        Map<Station, Double> minCost = new HashMap<>();

        pq.add(new PathNode(origin, null, 0.0, 0.0, 0.0, 0, null, null));
        minCost.put(origin, 0.0);

        PathNode targetNode = null;

        while (!pq.isEmpty()) {
            PathNode current = pq.poll();
            Station u = current.getStation();

            if (current.getCost() > minCost.getOrDefault(u, Double.MAX_VALUE)) {
                continue;
            }

            if (u.equals(destination)) {
                targetNode = current;
                break;
            }

            for (Connection edge : graph.getConnections(u)) {
                if (!edge.isOperational()) continue;
                Station v = edge.getTo();

                double newDistance = current.getDistanceKm() + edge.getDistanceKm();
                double newTime = current.getTimeMinutes() + edge.getTimeMinutes();

                // Add transfer penalty if line changed
                int newTransfers = current.getTransfers();
                if (current.getCurrentLine() != null && current.getCurrentLine() != edge.getLine()) {
                    newTime += edge.getLine().getTransferPenaltyMinutes();
                    newTransfers++;
                }

                double cost = newDistance;

                if (cost < minCost.getOrDefault(v, Double.MAX_VALUE)) {
                    minCost.put(v, cost);
                    pq.add(new PathNode(v, edge.getLine(), cost, newDistance, newTime, newTransfers, current, edge));
                }
            }
        }

        if (targetNode == null) {
            return RouteResult.notFound(origin, destination, Criteria.SHORTEST_DISTANCE.getLabel());
        }

        return reconstructRoute(origin, destination, targetNode, Criteria.SHORTEST_DISTANCE.getLabel());
    }

    /**
     * Finds the route minimizing total journey time (track time + transfer penalty).
     */
    private RouteResult dijkstraTime(Station origin, Station destination) {
        // State expansion: (Station, MetroLine) -> minTime
        PriorityQueue<PathNode> pq = new PriorityQueue<>();
        Map<String, Double> minCost = new HashMap<>();

        pq.add(new PathNode(origin, null, 0.0, 0.0, 0.0, 0, null, null));
        minCost.put(origin.getName() + ":START", 0.0);

        PathNode targetNode = null;

        while (!pq.isEmpty()) {
            PathNode current = pq.poll();
            Station u = current.getStation();

            if (u.equals(destination)) {
                targetNode = current;
                break;
            }

            String stateKey = u.getName() + ":" + (current.getCurrentLine() != null ? current.getCurrentLine().name() : "START");
            if (current.getCost() > minCost.getOrDefault(stateKey, Double.MAX_VALUE)) {
                continue;
            }

            for (Connection edge : graph.getConnections(u)) {
                if (!edge.isOperational()) continue;
                Station v = edge.getTo();

                double transferDelay = 0.0;
                int newTransfers = current.getTransfers();
                if (current.getCurrentLine() != null && current.getCurrentLine() != edge.getLine()) {
                    transferDelay = edge.getLine().getTransferPenaltyMinutes();
                    newTransfers++;
                }

                double newTime = current.getTimeMinutes() + edge.getTimeMinutes() + transferDelay;
                double newDistance = current.getDistanceKm() + edge.getDistanceKm();
                double cost = newTime;

                String nextStateKey = v.getName() + ":" + edge.getLine().name();

                if (cost < minCost.getOrDefault(nextStateKey, Double.MAX_VALUE)) {
                    minCost.put(nextStateKey, cost);
                    pq.add(new PathNode(v, edge.getLine(), cost, newDistance, newTime, newTransfers, current, edge));
                }
            }
        }

        if (targetNode == null) {
            return RouteResult.notFound(origin, destination, Criteria.FASTEST_TIME.getLabel());
        }

        return reconstructRoute(origin, destination, targetNode, Criteria.FASTEST_TIME.getLabel());
    }

    /**
     * Finds the route minimizing the number of train line switches (interchanges).
     * Primary objective: transfers * 10,000 + secondary: distanceKm.
     */
    private RouteResult dijkstraFewestInterchanges(Station origin, Station destination) {
        PriorityQueue<PathNode> pq = new PriorityQueue<>();
        Map<String, Double> minCost = new HashMap<>();

        pq.add(new PathNode(origin, null, 0.0, 0.0, 0.0, 0, null, null));
        minCost.put(origin.getName() + ":START", 0.0);

        PathNode targetNode = null;

        while (!pq.isEmpty()) {
            PathNode current = pq.poll();
            Station u = current.getStation();

            if (u.equals(destination)) {
                targetNode = current;
                break;
            }

            String stateKey = u.getName() + ":" + (current.getCurrentLine() != null ? current.getCurrentLine().name() : "START");
            if (current.getCost() > minCost.getOrDefault(stateKey, Double.MAX_VALUE)) {
                continue;
            }

            for (Connection edge : graph.getConnections(u)) {
                if (!edge.isOperational()) continue;
                Station v = edge.getTo();

                int addedTransfer = 0;
                double transferDelay = 0.0;
                if (current.getCurrentLine() != null && current.getCurrentLine() != edge.getLine()) {
                    addedTransfer = 1;
                    transferDelay = edge.getLine().getTransferPenaltyMinutes();
                }

                int newTransfers = current.getTransfers() + addedTransfer;
                double newDistance = current.getDistanceKm() + edge.getDistanceKm();
                double newTime = current.getTimeMinutes() + edge.getTimeMinutes() + transferDelay;

                // Lexicographical cost: primary transfers, secondary distance
                double cost = (newTransfers * 10000.0) + newDistance;

                String nextStateKey = v.getName() + ":" + edge.getLine().name();

                if (cost < minCost.getOrDefault(nextStateKey, Double.MAX_VALUE)) {
                    minCost.put(nextStateKey, cost);
                    pq.add(new PathNode(v, edge.getLine(), cost, newDistance, newTime, newTransfers, current, edge));
                }
            }
        }

        if (targetNode == null) {
            return RouteResult.notFound(origin, destination, Criteria.FEWEST_INTERCHANGES.getLabel());
        }

        return reconstructRoute(origin, destination, targetNode, Criteria.FEWEST_INTERCHANGES.getLabel());
    }

    /**
     * Reconstructs full station path, connections, interchanges, and line segments.
     */
    private RouteResult reconstructRoute(Station origin, Station destination, PathNode targetNode, String criteriaLabel) {
        List<Station> path = new ArrayList<>();
        List<Connection> connections = new ArrayList<>();

        PathNode curr = targetNode;
        while (curr != null) {
            path.add(curr.getStation());
            if (curr.getConnectionUsed() != null) {
                connections.add(curr.getConnectionUsed());
            }
            curr = curr.getPrevious();
        }

        Collections.reverse(path);
        Collections.reverse(connections);

        // Identify interchanges and group into segments
        List<Station> interchanges = new ArrayList<>();
        List<RouteSegment> segments = new ArrayList<>();

        if (!connections.isEmpty()) {
            MetroLine currentLine = connections.get(0).getLine();
            Station segStart = connections.get(0).getFrom();
            List<Station> segStations = new ArrayList<>();
            segStations.add(segStart);
            double segDist = 0.0;
            double segTime = 0.0;

            for (int i = 0; i < connections.size(); i++) {
                Connection c = connections.get(i);
                if (c.getLine() != currentLine) {
                    // Line transfer occurred at c.getFrom()
                    interchanges.add(c.getFrom());
                    segments.add(new RouteSegment(currentLine, segStart, c.getFrom(), segStations, segDist, segTime));

                    currentLine = c.getLine();
                    segStart = c.getFrom();
                    segStations = new ArrayList<>();
                    segStations.add(segStart);
                    segDist = 0.0;
                    segTime = 0.0;
                }
                segStations.add(c.getTo());
                segDist += c.getDistanceKm();
                segTime += c.getTimeMinutes();
            }
            segments.add(new RouteSegment(currentLine, segStart, connections.get(connections.size() - 1).getTo(),
                    segStations, segDist, segTime));
        }

        return new RouteResult(origin, destination, path, connections, segments, interchanges,
                targetNode.getDistanceKm(), targetNode.getTimeMinutes(), criteriaLabel);
    }
}

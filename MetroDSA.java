import java.io.Serializable;
import java.util.*;

/**
 * ============================================================================
 * METRO DSA: CORE DATA STRUCTURES & GRAPH ALGORITHMS MODULE
 * ============================================================================
 * This file encapsulates all core Data Structures & Algorithms (DSA):
 *   1. GRAPH REPRESENTATION:
 *      - Station (Vertex) with multi-line memberships and operational status
 *      - Connection (Weighted Edge) with physical distance and travel time
 *      - MetroGraph: Adjacency List graph representation with bidirectional tracks
 *
 *   2. SHORTEST PATHFINDING ALGORITHMS (DIJKSTRA):
 *      - dijkstraDistance: Minimum physical distance (km) using Min-Priority Queue
 *      - dijkstraTime: State-expanded Dijkstra (Station, MetroLine) modeling
 *        line switch walking & dwell transfer penalties (minutes)
 *      - dijkstraFewestInterchanges: Lexicographically optimized Dijkstra minimizing
 *        platform interchanges as primary objective and track distance as secondary
 *      - findBestRoute: Evaluates candidate routes using Commuter Friction Cost Function:
 *        Score = Travel Time + (Interchanges * 7.0) + (Distance * 0.15)
 * ============================================================================
 */
public class MetroDSA {
    public static final String VERSION = "2.0-DSA";

    public static void printDsaSummary() {
        System.out.println("MetroDSA Module: Graph Adjacency List + State-Expanded Dijkstra Engine loaded.");
    }
}

// ============================================================================
// TRANSIT LINE DEFINITION
// ============================================================================

enum MetroLine {
    PURPLE("Purple Line", "Challaghatta - Majestic - Indiranagar - Whitefield", 4.0),
    GREEN("Green Line", "Madavara (BIEC) - Yeshwanthpur - Majestic - Silk Institute", 4.0),
    YELLOW("Yellow Line", "RV Road - Jayadeva - Silk Board - Electronic City - Bommasandra", 3.5),
    PINK("Pink Line", "Kalena Agrahara - Jayadeva - MG Road - Nagawara", 4.0),
    BLUE("Blue Line (Airport & ORR)", "Central Silk Board - Marathahalli - KR Puram - Hebbal - KIAL Airport", 3.0);

    private final String displayName;
    private final String description;
    private final double transferPenaltyMinutes;

    MetroLine(String displayName, String description, double transferPenaltyMinutes) {
        this.displayName = displayName;
        this.description = description;
        this.transferPenaltyMinutes = transferPenaltyMinutes;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public double getTransferPenaltyMinutes() {
        return transferPenaltyMinutes;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

// ============================================================================
// GRAPH VERTEX: STATION
// ============================================================================

class Station implements Serializable, Comparable<Station> {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String name;
    private final Set<MetroLine> lines;
    private boolean active;

    public Station(String id, String name) {
        this.id = id.trim();
        this.name = name.trim();
        this.lines = new HashSet<>();
        this.active = true;
    }

    public Station(String id, String name, MetroLine... initialLines) {
        this(id, name);
        if (initialLines != null) {
            for (MetroLine line : initialLines) {
                if (line != null) {
                    this.lines.add(line);
                }
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<MetroLine> getLines() {
        return Collections.unmodifiableSet(lines);
    }

    public void addLine(MetroLine line) {
        if (line != null) {
            lines.add(line);
        }
    }

    public boolean isInterchange() {
        return lines.size() > 1;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getLinesString() {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (MetroLine line : lines) {
            if (count > 0) sb.append(", ");
            sb.append(line.getDisplayName());
            count++;
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Station)) return false;
        Station station = (Station) o;
        return name.equalsIgnoreCase(station.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase());
    }

    @Override
    public int compareTo(Station o) {
        return this.name.compareToIgnoreCase(o.name);
    }

    @Override
    public String toString() {
        return name + (isInterchange() ? " [Interchange: " + getLinesString() + "]" : " [" + getLinesString() + "]");
    }
}

// ============================================================================
// GRAPH EDGE: CONNECTION
// ============================================================================

class Connection implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Station from;
    private final Station to;
    private final MetroLine line;
    private final double distanceKm;
    private final double timeMinutes;
    private boolean operational;

    public Connection(Station from, Station to, MetroLine line, double distanceKm, double timeMinutes) {
        this.from = Objects.requireNonNull(from, "Origin station cannot be null");
        this.to = Objects.requireNonNull(to, "Destination station cannot be null");
        this.line = Objects.requireNonNull(line, "Metro line cannot be null");
        this.distanceKm = Math.max(0.1, distanceKm);
        this.timeMinutes = Math.max(0.5, timeMinutes);
        this.operational = true;
    }

    public Station getFrom() {
        return from;
    }

    public Station getTo() {
        return to;
    }

    public MetroLine getLine() {
        return line;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getTimeMinutes() {
        return timeMinutes;
    }

    public boolean isOperational() {
        return operational && from.isActive() && to.isActive();
    }

    public void setOperational(boolean operational) {
        this.operational = operational;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s (%s, %.2f km, %.1f min)%s",
                from.getName(), to.getName(), line.getDisplayName(),
                distanceKm, timeMinutes, operational ? "" : " [CLOSED]");
    }
}

// ============================================================================
// ROUTE LEG: ROUTE SEGMENT
// ============================================================================

class RouteSegment implements Serializable {
    private static final long serialVersionUID = 1L;

    private final MetroLine line;
    private final Station startStation;
    private final Station endStation;
    private final List<Station> stations;
    private final double distanceKm;
    private final double timeMinutes;

    public RouteSegment(MetroLine line, Station startStation, Station endStation,
                        List<Station> stations, double distanceKm, double timeMinutes) {
        this.line = line;
        this.startStation = startStation;
        this.endStation = endStation;
        this.stations = new ArrayList<>(stations);
        this.distanceKm = distanceKm;
        this.timeMinutes = timeMinutes;
    }

    public MetroLine getLine() {
        return line;
    }

    public Station getStartStation() {
        return startStation;
    }

    public Station getEndStation() {
        return endStation;
    }

    public List<Station> getStations() {
        return Collections.unmodifiableList(stations);
    }

    public int getStationCount() {
        return stations.size();
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getTimeMinutes() {
        return timeMinutes;
    }
}

// ============================================================================
// ROUTE RESULT CONTAINER
// ============================================================================

class RouteResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Station origin;
    private final Station destination;
    private final List<Station> path;
    private final List<Connection> connections;
    private final List<RouteSegment> segments;
    private final List<Station> interchanges;
    private final double totalDistanceKm;
    private final double totalTimeMinutes;
    private final String optimizationCriteria;
    private final boolean found;

    public RouteResult(Station origin, Station destination,
                       List<Station> path,
                       List<Connection> connections,
                       List<RouteSegment> segments,
                       List<Station> interchanges,
                       double totalDistanceKm,
                       double totalTimeMinutes,
                       String optimizationCriteria) {
        this.origin = origin;
        this.destination = destination;
        this.path = path != null ? new ArrayList<>(path) : Collections.emptyList();
        this.connections = connections != null ? new ArrayList<>(connections) : Collections.emptyList();
        this.segments = segments != null ? new ArrayList<>(segments) : Collections.emptyList();
        this.interchanges = interchanges != null ? new ArrayList<>(interchanges) : Collections.emptyList();
        this.totalDistanceKm = totalDistanceKm;
        this.totalTimeMinutes = totalTimeMinutes;
        this.optimizationCriteria = optimizationCriteria;
        this.found = path != null && !path.isEmpty();
    }

    public static RouteResult notFound(Station origin, Station destination, String optimizationCriteria) {
        return new RouteResult(origin, destination, null, null, null, null, 0.0, 0.0, optimizationCriteria);
    }

    public Station getOrigin() {
        return origin;
    }

    public Station getDestination() {
        return destination;
    }

    public List<Station> getPath() {
        return Collections.unmodifiableList(path);
    }

    public List<Connection> getConnections() {
        return Collections.unmodifiableList(connections);
    }

    public List<RouteSegment> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    public List<Station> getInterchanges() {
        return Collections.unmodifiableList(interchanges);
    }

    public int getInterchangeCount() {
        return interchanges.size();
    }

    public int getTotalStations() {
        return path.size();
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public double getTotalTimeMinutes() {
        return totalTimeMinutes;
    }

    public String getOptimizationCriteria() {
        return optimizationCriteria;
    }

    public boolean isFound() {
        return found;
    }

    public boolean usesAirportExpress() {
        for (Station s : path) {
            if (s.getName().toLowerCase().contains("airport") || s.getName().toLowerCase().contains("kial")) {
                return true;
            }
        }
        for (Connection c : connections) {
            if (c.getLine() == MetroLine.BLUE && (c.getFrom().getName().contains("Airport") || c.getTo().getName().contains("Airport"))) {
                return true;
            }
        }
        return false;
    }
}

// ============================================================================
// GRAPH DATA STRUCTURE: METRO GRAPH (ADJACENCY LIST)
// ============================================================================

class MetroGraph implements Serializable {
    private static final long serialVersionUID = 1L;

    // Fast O(log V) case-insensitive lookup of stations by name
    private final Map<String, Station> stations;
    // Adjacency List: Station -> List of outgoing track Connections
    private final Map<Station, List<Connection>> adjacencyList;

    public MetroGraph() {
        this.stations = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.adjacencyList = new HashMap<>();
    }

    public synchronized Station addStation(Station station) {
        if (station == null) return null;
        Station existing = stations.get(station.getName());
        if (existing != null) {
            for (MetroLine line : station.getLines()) {
                existing.addLine(line);
            }
            return existing;
        }
        stations.put(station.getName(), station);
        adjacencyList.putIfAbsent(station, new ArrayList<>());
        return station;
    }

    public synchronized Station getOrCreateStation(String id, String name, MetroLine... lines) {
        Station s = stations.get(name);
        if (s == null) {
            s = new Station(id, name, lines);
            addStation(s);
        } else {
            if (lines != null) {
                for (MetroLine line : lines) {
                    s.addLine(line);
                }
            }
        }
        return s;
    }

    public Station getStation(String name) {
        if (name == null || name.trim().isEmpty()) return null;
        return stations.get(name.trim());
    }

    public List<Station> searchStations(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String clean = query.trim().toLowerCase();
        List<Station> matches = new ArrayList<>();
        for (Station s : stations.values()) {
            if (s.getName().toLowerCase().contains(clean)) {
                matches.add(s);
            }
        }
        return matches;
    }

    public synchronized void addConnection(Station from, Station to, MetroLine line,
                                          double distanceKm, double timeMinutes, boolean bidirectional) {
        if (from == null || to == null || line == null) return;

        from.addLine(line);
        to.addLine(line);
        addStation(from);
        addStation(to);

        Connection forward = new Connection(from, to, line, distanceKm, timeMinutes);
        adjacencyList.get(from).add(forward);

        if (bidirectional) {
            Connection backward = new Connection(to, from, line, distanceKm, timeMinutes);
            adjacencyList.get(to).add(backward);
        }
    }

    public List<Connection> getConnections(Station station) {
        if (station == null) return Collections.emptyList();
        return adjacencyList.getOrDefault(station, Collections.emptyList());
    }

    public Collection<Station> getAllStations() {
        return Collections.unmodifiableCollection(stations.values());
    }

    public List<Station> getStationsByLine(MetroLine line) {
        List<Station> lineStations = new ArrayList<>();
        for (Station s : stations.values()) {
            if (s.getLines().contains(line)) {
                lineStations.add(s);
            }
        }
        Collections.sort(lineStations);
        return lineStations;
    }

    public List<Station> getInterchangeStations() {
        List<Station> interchanges = new ArrayList<>();
        for (Station s : stations.values()) {
            if (s.isInterchange()) {
                interchanges.add(s);
            }
        }
        Collections.sort(interchanges);
        return interchanges;
    }

    public boolean toggleStationStatus(String stationName, boolean active) {
        Station s = getStation(stationName);
        if (s != null) {
            s.setActive(active);
            return true;
        }
        return false;
    }

    public boolean toggleTrackStatus(String fromName, String toName, boolean operational) {
        Station u = getStation(fromName);
        Station v = getStation(toName);
        if (u == null || v == null) return false;

        boolean modified = false;
        List<Connection> outU = adjacencyList.get(u);
        if (outU != null) {
            for (Connection c : outU) {
                if (c.getTo().equals(v)) {
                    c.setOperational(operational);
                    modified = true;
                }
            }
        }
        List<Connection> outV = adjacencyList.get(v);
        if (outV != null) {
            for (Connection c : outV) {
                if (c.getTo().equals(u)) {
                    c.setOperational(operational);
                    modified = true;
                }
            }
        }
        return modified;
    }
}

// ============================================================================
// PRIORITY QUEUE STATE NODE (FOR DIJKSTRA)
// ============================================================================

class PathNode implements Comparable<PathNode> {
    private final Station station;
    private final MetroLine currentLine;
    private final double cost;
    private final double distanceKm;
    private final double timeMinutes;
    private final int transfers;
    private final PathNode previous;
    private final Connection connectionUsed;

    public PathNode(Station station, MetroLine currentLine, double cost,
                    double distanceKm, double timeMinutes, int transfers,
                    PathNode previous, Connection connectionUsed) {
        this.station = station;
        this.currentLine = currentLine;
        this.cost = cost;
        this.distanceKm = distanceKm;
        this.timeMinutes = timeMinutes;
        this.transfers = transfers;
        this.previous = previous;
        this.connectionUsed = connectionUsed;
    }

    public Station getStation() {
        return station;
    }

    public MetroLine getCurrentLine() {
        return currentLine;
    }

    public double getCost() {
        return cost;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getTimeMinutes() {
        return timeMinutes;
    }

    public int getTransfers() {
        return transfers;
    }

    public PathNode getPrevious() {
        return previous;
    }

    public Connection getConnectionUsed() {
        return connectionUsed;
    }

    @Override
    public int compareTo(PathNode o) {
        return Double.compare(this.cost, o.cost);
    }
}

// ============================================================================
// ROUTING ENGINE: DIJKSTRA ALGORITHMS & HEURISTICS
// ============================================================================

class RouteEngine {
    private final MetroGraph graph;

    public RouteEngine(MetroGraph graph) {
        this.graph = Objects.requireNonNull(graph, "MetroGraph cannot be null");
    }

    public enum Criteria {
        SHORTEST_DISTANCE("Shortest Distance"),
        FASTEST_TIME("Fastest Travel Time"),
        FEWEST_INTERCHANGES("Fewest Line Interchanges");

        private final String label;
        Criteria(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

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
     * Automatically computes and returns the single Best Overall Route.
     * Evaluates travel time, transfer convenience, and distance using a commuter friction score.
     */
    public RouteResult findBestRoute(Station origin, Station destination) {
        if (origin == null || destination == null) {
            return RouteResult.notFound(origin, destination, "Best Route");
        }

        RouteResult timeOpt = findRoute(origin, destination, Criteria.FASTEST_TIME);
        RouteResult transOpt = findRoute(origin, destination, Criteria.FEWEST_INTERCHANGES);
        RouteResult distOpt = findRoute(origin, destination, Criteria.SHORTEST_DISTANCE);

        RouteResult best = timeOpt;
        double bestScore = computeFrictionScore(timeOpt);

        double transScore = computeFrictionScore(transOpt);
        if (transScore < bestScore) {
            best = transOpt;
            bestScore = transScore;
        }

        double distScore = computeFrictionScore(distOpt);
        if (distScore < bestScore) {
            best = distOpt;
        }

        if (best == null || !best.isFound()) {
            return RouteResult.notFound(origin, destination, "Best Route");
        }

        return new RouteResult(best.getOrigin(), best.getDestination(), best.getPath(),
                best.getConnections(), best.getSegments(), best.getInterchanges(),
                best.getTotalDistanceKm(), best.getTotalTimeMinutes(),
                "Best Recommended Route (" + best.getOptimizationCriteria() + ")");
    }

    private double computeFrictionScore(RouteResult r) {
        if (r == null || !r.isFound()) return Double.MAX_VALUE;
        // 1 interchange equals 7 minutes of walking / dwell strain
        return r.getTotalTimeMinutes() + (r.getInterchangeCount() * 7.0) + (r.getTotalDistanceKm() * 0.15);
    }

    /**
     * Dijkstra's Algorithm for Shortest Physical Distance (km).
     * Time Complexity: O((V + E) log V)
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
     * State-Expanded Dijkstra's Algorithm for Fastest Travel Time (minutes).
     * Expanded State: (Station, MetroLine) to accurately model transfer delays.
     * Time Complexity: O((V * L + E) log (V * L)) where L <= 5 lines.
     */
    private RouteResult dijkstraTime(Station origin, Station destination) {
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
     * Lexicographically Optimized Dijkstra for Fewest Line Interchanges.
     * Cost Function: (Transfers * 10,000) + DistanceKm
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

                // Transfers weighted 10,000x over distance
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

package graph;

import model.Connection;
import model.MetroLine;
import model.Station;

import java.io.Serializable;
import java.util.*;

public class MetroGraph implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, Station> stations;
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

    public int getStationCount() {
        return stations.size();
    }

    public int getConnectionCount() {
        int count = 0;
        for (List<Connection> list : adjacencyList.values()) {
            count += list.size();
        }
        return count / 2;
    }
}

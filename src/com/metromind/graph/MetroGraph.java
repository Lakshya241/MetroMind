package com.metromind.graph;

import com.metromind.model.Connection;
import com.metromind.model.MetroLine;
import com.metromind.model.Station;

import java.io.Serializable;
import java.util.*;

/**
 * Graph data structure representing the Metro transit network.
 * Vertices are Stations, and Edges are bidirectional Connections with distance and time weights.
 */
public class MetroGraph implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, Station> stations;
    private final Map<Station, List<Connection>> adjacencyList;

    public MetroGraph() {
        this.stations = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.adjacencyList = new HashMap<>();
    }

    /**
     * Registers a station into the network.
     */
    public synchronized Station addStation(Station station) {
        if (station == null) return null;
        Station existing = stations.get(station.getName());
        if (existing != null) {
            // Merge lines if station already exists
            for (MetroLine line : station.getLines()) {
                existing.addLine(line);
            }
            return existing;
        }
        stations.put(station.getName(), station);
        adjacencyList.putIfAbsent(station, new ArrayList<>());
        return station;
    }

    /**
     * Retrieves or creates a station with the given name.
     */
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

    /**
     * Looks up a station by exact or case-insensitive name.
     */
    public Station getStation(String name) {
        if (name == null || name.trim().isEmpty()) return null;
        return stations.get(name.trim());
    }

    /**
     * Searches for stations containing the query string (substring match).
     */
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

    /**
     * Adds a track connection between two stations.
     */
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

    /**
     * Returns adjacent connections from the specified station.
     */
    public List<Connection> getConnections(Station station) {
        if (station == null) return Collections.emptyList();
        return adjacencyList.getOrDefault(station, Collections.emptyList());
    }

    /**
     * Returns all registered stations.
     */
    public Collection<Station> getAllStations() {
        return Collections.unmodifiableCollection(stations.values());
    }

    /**
     * Returns all stations that lie on a specific line.
     */
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

    /**
     * Returns all stations that act as interchange junctions.
     */
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

    /**
     * Toggles operational status of a station (e.g. for maintenance or emergency closure).
     */
    public boolean toggleStationStatus(String stationName, boolean active) {
        Station s = getStation(stationName);
        if (s != null) {
            s.setActive(active);
            return true;
        }
        return false;
    }

    /**
     * Toggles operational status of a track segment between two stations.
     */
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
        return count / 2; // Since bidirectional
    }
}

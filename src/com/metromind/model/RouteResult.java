package com.metromind.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the complete computed path from an origin to a destination station.
 */
public class RouteResult implements Serializable {
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

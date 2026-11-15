package com.metromind.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a direct track connection (edge) between two adjacent stations.
 */
public class Connection implements Serializable {
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

package com.metromind.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents an individual metro station.
 * A station may connect to one or multiple lines (Interchange station).
 */
public class Station implements Serializable, Comparable<Station> {
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

    public void removeLine(MetroLine line) {
        lines.remove(line);
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

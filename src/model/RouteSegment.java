package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a continuous leg of a journey traversed on a single Metro Line.
 */
public class RouteSegment implements Serializable {
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

    @Override
    public String toString() {
        return String.format("[%s] %s -> %s (%d stations, %.2f km, %.1f min)",
                line.getDisplayName(), startStation.getName(), endStation.getName(),
                stations.size(), distanceKm, timeMinutes);
    }
}

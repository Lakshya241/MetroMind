package route;

import model.Connection;
import model.MetroLine;
import model.Station;

/**
 * Node state utilized by the Dijkstra / Priority Queue pathfinding algorithms.
 */
public class PathNode implements Comparable<PathNode> {
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

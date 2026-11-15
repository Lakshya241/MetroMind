package model;

/**
 * Represents a transit line in the Bengaluru Namma Metro network.
 */
public enum MetroLine {
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

    public static MetroLine fromString(String text) {
        if (text == null) return null;
        for (MetroLine line : MetroLine.values()) {
            if (line.name().equalsIgnoreCase(text) || line.displayName.equalsIgnoreCase(text)) {
                return line;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

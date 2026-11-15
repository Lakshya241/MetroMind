package com.metromind.fare;

import com.metromind.model.FareBreakdown;
import com.metromind.model.RouteResult;
import com.metromind.model.TicketType;

import java.util.EnumMap;
import java.util.Map;


public class FareEngine {

    public static final double AIRPORT_SURCHARGE = 20.0;
    public static final double MINIMUM_FARE = 10.0;

    public static class Slab {
        private final double minKm;
        private final double maxKm;
        private final double fare;
        private final String label;

        public Slab(double minKm, double maxKm, double fare, String label) {
            this.minKm = minKm;
            this.maxKm = maxKm;
            this.fare = fare;
            this.label = label;
        }

        public double getMinKm() {
            return minKm;
        }

        public double getMaxKm() {
            return maxKm;
        }

        public double getFare() {
            return fare;
        }

        public String getLabel() {
            return label;
        }
    }

    private static final Slab[] SLABS = new Slab[]{
            new Slab(0.0, 2.0, 10.0, "0.0 - 2.0 km"),
            new Slab(2.0, 5.0, 20.0, "2.1 - 5.0 km"),
            new Slab(5.0, 12.0, 30.0, "5.1 - 12.0 km"),
            new Slab(12.0, 21.0, 40.0, "12.1 - 21.0 km"),
            new Slab(21.0, 32.0, 50.0, "21.1 - 32.0 km"),
            new Slab(32.0, Double.MAX_VALUE, 60.0, "> 32.0 km")
    };

    public FareBreakdown calculateFare(RouteResult route, TicketType ticketType) {
        if (route == null || !route.isFound() || route.getPath().size() <= 1) {
            return new FareBreakdown(0.0, "Same Station", 0.0, 0.0, ticketType, 0.0, 0.0);
        }

        double distance = route.getTotalDistanceKm();
        Slab matchedSlab = getSlabForDistance(distance);
        double baseFare = matchedSlab.fare;

        double airportSurcharge = route.usesAirportExpress() ? AIRPORT_SURCHARGE : 0.0;

        double discountPercent = ticketType.getDiscountPercentage();
        double discountAmount = (baseFare * discountPercent) / 100.0;

        double finalFare = (baseFare - discountAmount) + airportSurcharge;
        finalFare = Math.max(MINIMUM_FARE, Math.round(finalFare * 100.0) / 100.0);

        return new FareBreakdown(distance, matchedSlab.label, baseFare, airportSurcharge,
                ticketType, discountAmount, finalFare);
    }

    public Map<TicketType, FareBreakdown> calculateAllFareOptions(RouteResult route) {
        Map<TicketType, FareBreakdown> options = new EnumMap<>(TicketType.class);
        for (TicketType type : TicketType.values()) {
            options.put(type, calculateFare(route, type));
        }
        return options;
    }

    public Slab getSlabForDistance(double distanceKm) {
        for (Slab slab : SLABS) {
            if (distanceKm <= slab.maxKm) {
                return slab;
            }
        }
        return SLABS[SLABS.length - 1];
    }

    public Slab[] getAllSlabs() {
        return SLABS.clone();
    }
}

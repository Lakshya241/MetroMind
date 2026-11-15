package com.metromind.model;

import java.io.Serializable;


public class FareBreakdown implements Serializable {
    private static final long serialVersionUID = 1L;

    private final double distanceKm;
    private final String slabName;
    private final double baseFare;
    private final double airportSurcharge;
    private final TicketType ticketType;
    private final double discountAmount;
    private final double finalFare;

    public FareBreakdown(double distanceKm, String slabName, double baseFare,
                         double airportSurcharge, TicketType ticketType,
                         double discountAmount, double finalFare) {
        this.distanceKm = distanceKm;
        this.slabName = slabName;
        this.baseFare = baseFare;
        this.airportSurcharge = airportSurcharge;
        this.ticketType = ticketType;
        this.discountAmount = discountAmount;
        this.finalFare = finalFare;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public String getSlabName() {
        return slabName;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public double getAirportSurcharge() {
        return airportSurcharge;
    }

    public TicketType getTicketType() {
        return ticketType;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public double getFinalFare() {
        return finalFare;
    }

    public double getSavings() {
        return (baseFare + airportSurcharge) - finalFare;
    }

    @Override
    public String toString() {
        return String.format("Fare: ₹%.2f (Slab: %s, Base: ₹%.2f, Surcharge: ₹%.2f, Discount: -₹%.2f [%s])",
                finalFare, slabName, baseFare, airportSurcharge, discountAmount, ticketType.getTitle());
    }
}

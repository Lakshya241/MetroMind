package com.metromind.model;

/**
 * Supported ticket and pass types for Bengaluru Namma Metro fare calculation.
 */
public enum TicketType {
    TOKEN("Standard Token / QR Ticket", 0.0, "Standard single journey token with full fare."),
    SMART_CARD("Namma Metro Smart Card (Varshik / NCMC)", 10.0, "Rechargeable smart card offering 10% discount on base fare."),
    SMART_CARD_OFFPEAK("Namma Metro Smart Card (Off-Peak / Weekend)", 20.0, "Smart card with enhanced 20% discount outside rush hours and holidays."),
    STUDENT_PASS("Student Concession Pass", 25.0, "Verified student concession offering 25% discount on base fare."),
    SENIOR_CITIZEN("Senior Citizen Pass", 30.0, "Special senior citizen concession offering 30% discount on base fare.");

    private final String title;
    private final double discountPercentage;
    private final String description;

    TicketType(String title, double discountPercentage, String description) {
        this.title = title;
        this.discountPercentage = discountPercentage;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format("%s (%.0f%% Off)", title, discountPercentage);
    }
}

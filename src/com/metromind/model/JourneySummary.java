package com.metromind.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Complete summary representing a passenger's journey plan,
 * routing specifics, and fare calculations across various ticket types.
 */
public class JourneySummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String journeyId;
    private final LocalDateTime timestamp;
    private final RouteResult routeResult;
    private final Map<TicketType, FareBreakdown> fareOptions;
    private final TicketType selectedTicketType;

    public JourneySummary(String journeyId, RouteResult routeResult,
                          Map<TicketType, FareBreakdown> fareOptions,
                          TicketType selectedTicketType) {
        this.journeyId = journeyId;
        this.timestamp = LocalDateTime.now();
        this.routeResult = routeResult;
        this.fareOptions = fareOptions;
        this.selectedTicketType = selectedTicketType;
    }

    public String getJourneyId() {
        return journeyId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public RouteResult getRouteResult() {
        return routeResult;
    }

    public Map<TicketType, FareBreakdown> getFareOptions() {
        return fareOptions;
    }

    public TicketType getSelectedTicketType() {
        return selectedTicketType;
    }

    public FareBreakdown getSelectedFare() {
        return fareOptions.get(selectedTicketType);
    }

    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}

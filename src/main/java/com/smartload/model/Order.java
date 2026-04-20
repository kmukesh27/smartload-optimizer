package com.smartload.model;

import java.time.LocalDate;

public record Order(
        String id,
        long payoutCents,
        long weightLbs,
        long volumeCuft,
        String origin,
        String destination,
        LocalDate pickupDate,
        LocalDate deliveryDate,
        boolean isHazmat
) {
    public String laneKey() {
        return origin.trim().toLowerCase() + "|" + destination.trim().toLowerCase();
    }

    public boolean hasValidDates() {
        return !pickupDate.isAfter(deliveryDate);
    }
}

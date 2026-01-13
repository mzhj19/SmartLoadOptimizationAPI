package com.airasia.loadplanner.model;

import java.time.LocalDate;

public record Order(
    String id,
    long payoutCents,
    int weightLbs,
    int volumeCuft,
    String origin,
    String destination,
    LocalDate pickupDate,
    LocalDate deliveryDate,
    boolean isHazmat
) {}

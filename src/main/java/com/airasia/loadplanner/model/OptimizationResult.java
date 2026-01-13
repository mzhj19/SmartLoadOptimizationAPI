package com.airasia.loadplanner.model;

import java.util.List;

public record OptimizationResult(
    List<Order> selectedOrders,
    long totalPayoutCents,
    int totalWeightLbs,
    int totalVolumeCuft
) {}

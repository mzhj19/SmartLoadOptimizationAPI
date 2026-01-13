package com.airasia.loadplanner.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OptimizationRequest(
    @NotNull(message = "Truck is required")
    @Valid
    TruckDto truck,
    
    @NotNull(message = "Orders list is required")
    @Size(max = 22, message = "Maximum 22 orders allowed")
    @Valid
    List<OrderDto> orders
) {}

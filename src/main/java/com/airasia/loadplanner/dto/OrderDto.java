package com.airasia.loadplanner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderDto(
    @NotBlank(message = "Order ID is required")
    String id,
    
    @JsonProperty("payout_cents")
    @NotNull(message = "Payout is required")
    @Positive(message = "Payout must be positive")
    Long payoutCents,
    
    @JsonProperty("weight_lbs")
    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    Integer weightLbs,
    
    @JsonProperty("volume_cuft")
    @NotNull(message = "Volume is required")
    @Positive(message = "Volume must be positive")
    Integer volumeCuft,
    
    @NotBlank(message = "Origin is required")
    String origin,
    
    @NotBlank(message = "Destination is required")
    String destination,
    
    @JsonProperty("pickup_date")
    @NotBlank(message = "Pickup date is required")
    String pickupDate,
    
    @JsonProperty("delivery_date")
    @NotBlank(message = "Delivery date is required")
    String deliveryDate,
    
    @JsonProperty("is_hazmat")
    @NotNull(message = "Hazmat flag is required")
    Boolean isHazmat
) {}

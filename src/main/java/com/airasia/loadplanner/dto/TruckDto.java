package com.airasia.loadplanner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TruckDto(
    @NotBlank(message = "Truck ID is required")
    String id,
    
    @JsonProperty("max_weight_lbs")
    @NotNull(message = "Max weight is required")
    @Positive(message = "Max weight must be positive")
    Integer maxWeightLbs,
    
    @JsonProperty("max_volume_cuft")
    @NotNull(message = "Max volume is required")
    @Positive(message = "Max volume must be positive")
    Integer maxVolumeCuft
) {}

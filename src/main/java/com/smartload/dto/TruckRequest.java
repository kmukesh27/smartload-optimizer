package com.smartload.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TruckRequest(
        @NotBlank(message = "Truck id must not be blank")
        String id,

        @Positive(message = "max_weight_lbs must be positive")
        @JsonProperty("max_weight_lbs")
        long maxWeightLbs,

        @Positive(message = "max_volume_cuft must be positive")
        @JsonProperty("max_volume_cuft")
        long maxVolumeCuft
) {}

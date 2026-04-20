package com.smartload.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record OrderRequest(
        @NotBlank(message = "Order id must not be blank")
        String id,

        @PositiveOrZero(message = "payout_cents must be >= 0")
        @JsonProperty("payout_cents")
        long payoutCents,

        @Positive(message = "weight_lbs must be positive")
        @JsonProperty("weight_lbs")
        long weightLbs,

        @Positive(message = "volume_cuft must be positive")
        @JsonProperty("volume_cuft")
        long volumeCuft,

        @NotBlank(message = "origin must not be blank")
        String origin,

        @NotBlank(message = "destination must not be blank")
        String destination,

        @NotNull(message = "pickup_date is required")
        @JsonProperty("pickup_date")
        LocalDate pickupDate,

        @NotNull(message = "delivery_date is required")
        @JsonProperty("delivery_date")
        LocalDate deliveryDate,

        @JsonProperty("is_hazmat")
        boolean isHazmat
) {}

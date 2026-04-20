package com.smartload.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OptimizeRequest(
        @NotNull(message = "truck is required")
        @Valid
        TruckRequest truck,

        @NotNull(message = "orders list is required")
        @Size(max = 22, message = "Maximum 22 orders allowed per request")
        @Valid
        List<OrderRequest> orders
) {}

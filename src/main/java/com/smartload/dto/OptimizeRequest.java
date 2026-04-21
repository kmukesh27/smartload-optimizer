package com.smartload.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartload.model.OptimizationMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OptimizeRequest(
        @NotNull(message = "truck is required")
        @Valid
        TruckRequest truck,

        @NotNull(message = "orders list is required")
        @Valid
        List<OrderRequest> orders,

        @JsonProperty("optimization_mode")
        OptimizationMode optimizationMode,

        @JsonProperty("include_pareto_solutions")
        Boolean includeParetoSolutions,

        @JsonProperty("algorithm")
        Algorithm algorithm
) {
    public enum Algorithm {
        BITMASK_DP,
        BACKTRACKING
    }

    public OptimizationMode getEffectiveOptimizationMode() {
        return optimizationMode != null ? optimizationMode : OptimizationMode.MAX_REVENUE;
    }

    public boolean shouldIncludeParetoSolutions() {
        return includeParetoSolutions != null && includeParetoSolutions;
    }

    public Algorithm getEffectiveAlgorithm() {
        return algorithm != null ? algorithm : Algorithm.BITMASK_DP;
    }
}

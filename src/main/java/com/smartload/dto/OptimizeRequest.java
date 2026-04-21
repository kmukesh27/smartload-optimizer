package com.smartload.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartload.model.OptimizationMode;
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

    /**
     * Returns the optimization mode, defaulting to MAX_REVENUE if not specified.
     */
    public OptimizationMode getEffectiveOptimizationMode() {
        return optimizationMode != null ? optimizationMode : OptimizationMode.MAX_REVENUE;
    }

    /**
     * Returns whether to include Pareto solutions, defaulting to false.
     */
    public boolean shouldIncludeParetoSolutions() {
        return includeParetoSolutions != null && includeParetoSolutions;
    }

    /**
     * Returns the algorithm to use, defaulting to BITMASK_DP.
     */
    public Algorithm getEffectiveAlgorithm() {
        return algorithm != null ? algorithm : Algorithm.BITMASK_DP;
    }
}

package com.smartload.model;

/**
 * Defines optimization strategies for load planning.
 */
public enum OptimizationMode {
    /**
     * Maximize total payout (default, current behavior).
     */
    MAX_REVENUE,

    /**
     * Maximize weight and volume utilization.
     */
    MAX_UTILIZATION,

    /**
     * Weighted combination: 0.6 revenue + 0.4 utilization.
     */
    BALANCED
}

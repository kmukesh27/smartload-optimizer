package com.smartload.model;

import java.util.List;

public record OptimizationResult(
        List<Order> selectedOrders,
        long totalPayoutCents,
        long totalWeightLbs,
        long totalVolumeCuft
) {
    public static OptimizationResult empty() {
        return new OptimizationResult(List.of(), 0L, 0L, 0L);
    }

    public boolean isEmpty() {
        return selectedOrders.isEmpty();
    }
}

package com.smartload.service;

import com.smartload.model.OptimizationResult;
import com.smartload.model.Order;
import com.smartload.model.Truck;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Recursive backtracking optimizer with pruning.
 * Sorts orders by efficiency (payout/weight ratio) and prunes branches
 * when constraints are violated or when upper bound cannot beat current best.
 */
@Service
public class BacktrackingOptimizerService {

    private long bestPayout;
    private long bestWeight;
    private long bestVolume;
    private List<Order> bestSelection;
    private long[] suffixPayoutSum; // suffixPayoutSum[i] = sum of payouts from index i to n-1

    public OptimizationResult optimize(Truck truck, List<Order> orders) {
        if (orders.isEmpty()) {
            return OptimizationResult.empty();
        }

        // Sort by efficiency (payout per unit weight) descending
        Order[] sortedOrders = orders.toArray(new Order[0]);
        Arrays.sort(sortedOrders, Comparator.comparingDouble(
                (Order o) -> (double) o.payoutCents() / Math.max(1, o.weightLbs())
        ).reversed());

        // Precompute suffix payout sums for upper bound pruning
        int n = sortedOrders.length;
        suffixPayoutSum = new long[n + 1];
        suffixPayoutSum[n] = 0;
        for (int i = n - 1; i >= 0; i--) {
            suffixPayoutSum[i] = suffixPayoutSum[i + 1] + sortedOrders[i].payoutCents();
        }

        bestPayout = 0;
        bestWeight = 0;
        bestVolume = 0;
        bestSelection = new ArrayList<>();

        backtrack(sortedOrders, 0, 0, 0, 0, new ArrayList<>(), truck);

        if (bestSelection.isEmpty()) {
            return OptimizationResult.empty();
        }

        return new OptimizationResult(bestSelection, bestPayout, bestWeight, bestVolume);
    }

    private void backtrack(
            Order[] orders,
            int index,
            long currentWeight,
            long currentVolume,
            long currentPayout,
            List<Order> currentSelection,
            Truck truck
    ) {
        // Update best if current is better
        if (currentPayout > bestPayout) {
            bestPayout = currentPayout;
            bestWeight = currentWeight;
            bestVolume = currentVolume;
            bestSelection = new ArrayList<>(currentSelection);
        }

        // Base case: processed all orders
        if (index >= orders.length) {
            return;
        }

        // Pruning: upper bound check
        // Even if we take all remaining orders, can we beat best?
        long upperBound = currentPayout + suffixPayoutSum[index];
        if (upperBound <= bestPayout) {
            return; // Prune this branch
        }

        Order order = orders[index];

        // Option 1: Include this order (if it fits)
        long newWeight = currentWeight + order.weightLbs();
        long newVolume = currentVolume + order.volumeCuft();

        if (newWeight <= truck.maxWeightLbs() && newVolume <= truck.maxVolumeCuft()) {
            currentSelection.add(order);
            backtrack(orders, index + 1, newWeight, newVolume,
                    currentPayout + order.payoutCents(), currentSelection, truck);
            currentSelection.remove(currentSelection.size() - 1);
        }

        // Option 2: Exclude this order
        backtrack(orders, index + 1, currentWeight, currentVolume,
                currentPayout, currentSelection, truck);
    }
}

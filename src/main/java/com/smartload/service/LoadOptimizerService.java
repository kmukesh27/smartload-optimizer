package com.smartload.service;

import com.smartload.model.OptimizationMode;
import com.smartload.model.OptimizationResult;
import com.smartload.model.Order;
import com.smartload.model.ParetoSolution;
import com.smartload.model.Truck;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LoadOptimizerService {

    private static final int MAX_PARETO_SOLUTIONS = 10;

    public OptimizationResult optimize(Truck truck, List<Order> orders) {
        return optimize(truck, orders, OptimizationMode.MAX_REVENUE);
    }

    public OptimizationResult optimize(Truck truck, List<Order> orders, OptimizationMode mode) {
        if (orders.isEmpty()) {
            return OptimizationResult.empty();
        }

        int n = orders.size();
        Order[] arr = orders.toArray(new Order[0]);

        double bestScore = Double.NEGATIVE_INFINITY;
        int bestMask = 0;
        long bestPayout = 0L;
        long bestWeight = 0L;
        long bestVolume = 0L;

        int totalMasks = 1 << n;

        for (int mask = 1; mask < totalMasks; mask++) {
            long weight = 0L;
            long volume = 0L;
            long payout = 0L;
            boolean valid = true;

            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    weight += arr[i].weightLbs();
                    volume += arr[i].volumeCuft();
                    payout += arr[i].payoutCents();

                    if (weight > truck.maxWeightLbs() || volume > truck.maxVolumeCuft()) {
                        valid = false;
                        break;
                    }
                }
            }

            if (!valid) {
                continue;
            }

            double score = calculateScore(payout, weight, volume, truck, mode);

            if (score > bestScore) {
                bestScore = score;
                bestMask = mask;
                bestPayout = payout;
                bestWeight = weight;
                bestVolume = volume;
            }
        }

        if (bestMask == 0) {
            return OptimizationResult.empty();
        }

        List<Order> selected = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if ((bestMask & (1 << i)) != 0) {
                selected.add(arr[i]);
            }
        }

        return new OptimizationResult(selected, bestPayout, bestWeight, bestVolume);
    }

    public List<ParetoSolution> findParetoOptimalSolutions(Truck truck, List<Order> orders) {
        if (orders.isEmpty()) {
            return List.of();
        }

        int n = orders.size();
        Order[] arr = orders.toArray(new Order[0]);

        List<SolutionCandidate> candidates = new ArrayList<>();
        int totalMasks = 1 << n;

        for (int mask = 1; mask < totalMasks; mask++) {
            long weight = 0L;
            long volume = 0L;
            long payout = 0L;
            boolean valid = true;

            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    weight += arr[i].weightLbs();
                    volume += arr[i].volumeCuft();
                    payout += arr[i].payoutCents();

                    if (weight > truck.maxWeightLbs() || volume > truck.maxVolumeCuft()) {
                        valid = false;
                        break;
                    }
                }
            }

            if (valid) {
                double weightUtil = (double) weight / truck.maxWeightLbs() * 100.0;
                double volumeUtil = (double) volume / truck.maxVolumeCuft() * 100.0;

                List<String> orderIds = new ArrayList<>();
                for (int i = 0; i < n; i++) {
                    if ((mask & (1 << i)) != 0) {
                        orderIds.add(arr[i].id());
                    }
                }

                candidates.add(new SolutionCandidate(orderIds, payout, weightUtil, volumeUtil));
            }
        }

        List<ParetoSolution> paretoSet = new ArrayList<>();

        for (SolutionCandidate candidate : candidates) {
            ParetoSolution solution = new ParetoSolution(
                    candidate.orderIds,
                    candidate.payout,
                    candidate.weightUtil,
                    candidate.volumeUtil
            );

            boolean isDominated = false;
            for (SolutionCandidate other : candidates) {
                if (other == candidate) continue;

                ParetoSolution otherSolution = new ParetoSolution(
                        other.orderIds, other.payout, other.weightUtil, other.volumeUtil
                );

                if (otherSolution.dominates(solution)) {
                    isDominated = true;
                    break;
                }
            }

            if (!isDominated) {
                paretoSet.add(solution);
            }
        }

        paretoSet.sort((a, b) -> Long.compare(b.totalPayoutCents(), a.totalPayoutCents()));

        if (paretoSet.size() > MAX_PARETO_SOLUTIONS) {
            return paretoSet.subList(0, MAX_PARETO_SOLUTIONS);
        }

        return paretoSet;
    }

    private double calculateScore(long payout, long weight, long volume, Truck truck, OptimizationMode mode) {
        double weightUtil = (double) weight / truck.maxWeightLbs();
        double volumeUtil = (double) volume / truck.maxVolumeCuft();
        double avgUtil = (weightUtil + volumeUtil) / 2.0;

        return switch (mode) {
            case MAX_REVENUE -> payout;
            case MAX_UTILIZATION -> avgUtil * 1_000_000;
            case BALANCED -> {
                double normalizedPayout = payout / 1_000_000.0;
                yield 0.6 * normalizedPayout + 0.4 * avgUtil;
            }
        };
    }

    private record SolutionCandidate(
            List<String> orderIds,
            long payout,
            double weightUtil,
            double volumeUtil
    ) {}
}

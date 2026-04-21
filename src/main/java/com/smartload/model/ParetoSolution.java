package com.smartload.model;

import java.util.List;

/**
 * Represents a Pareto-optimal solution with multiple optimization objectives.
 * A solution is Pareto-optimal if no other solution is better in ALL objectives.
 */
public record ParetoSolution(
        List<String> orderIds,
        long totalPayoutCents,
        double utilizationWeightPercent,
        double utilizationVolumePercent
) {
    /**
     * Check if this solution dominates another solution.
     * A solution dominates another if it's at least as good in all objectives
     * and strictly better in at least one.
     */
    public boolean dominates(ParetoSolution other) {
        boolean atLeastAsGoodInAll =
                this.totalPayoutCents >= other.totalPayoutCents &&
                this.utilizationWeightPercent >= other.utilizationWeightPercent &&
                this.utilizationVolumePercent >= other.utilizationVolumePercent;

        boolean strictlyBetterInAtLeastOne =
                this.totalPayoutCents > other.totalPayoutCents ||
                this.utilizationWeightPercent > other.utilizationWeightPercent ||
                this.utilizationVolumePercent > other.utilizationVolumePercent;

        return atLeastAsGoodInAll && strictlyBetterInAtLeastOne;
    }
}

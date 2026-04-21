package com.smartload.model;

import java.util.List;

public record ParetoSolution(
        List<String> orderIds,
        long totalPayoutCents,
        double utilizationWeightPercent,
        double utilizationVolumePercent
) {
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

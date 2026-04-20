package com.smartload.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record OptimizeResponse(
        @JsonProperty("truck_id")
        String truckId,

        @JsonProperty("selected_order_ids")
        List<String> selectedOrderIds,

        @JsonProperty("total_payout_cents")
        long totalPayoutCents,

        @JsonProperty("total_weight_lbs")
        long totalWeightLbs,

        @JsonProperty("total_volume_cuft")
        long totalVolumeCuft,

        @JsonProperty("utilization_weight_percent")
        double utilizationWeightPercent,

        @JsonProperty("utilization_volume_percent")
        double utilizationVolumePercent
) {
    /**
     * Factory method that computes utilization automatically.
     */
    public static OptimizeResponse of(
            String truckId,
            List<String> selectedOrderIds,
            long totalPayoutCents,
            long totalWeightLbs,
            long totalVolumeCuft,
            long maxWeightLbs,
            long maxVolumeCuft
    ) {
        double weightPct = round2((double) totalWeightLbs / maxWeightLbs * 100.0);
        double volumePct = round2((double) totalVolumeCuft / maxVolumeCuft * 100.0);
        return new OptimizeResponse(
                truckId, selectedOrderIds,
                totalPayoutCents, totalWeightLbs, totalVolumeCuft,
                weightPct, volumePct
        );
    }

    private static double round2(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}

package com.smartload.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartload.model.ParetoSolution;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
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
        double utilizationVolumePercent,

        @JsonProperty("algorithm_used")
        String algorithmUsed,

        @JsonProperty("pareto_solutions")
        List<ParetoSolutionDto> paretoSolutions
) {
    
    public record ParetoSolutionDto(
            @JsonProperty("order_ids")
            List<String> orderIds,

            @JsonProperty("total_payout_cents")
            long totalPayoutCents,

            @JsonProperty("utilization_weight_percent")
            double utilizationWeightPercent,

            @JsonProperty("utilization_volume_percent")
            double utilizationVolumePercent
    ) {
        public static ParetoSolutionDto from(ParetoSolution solution) {
            return new ParetoSolutionDto(
                    solution.orderIds(),
                    solution.totalPayoutCents(),
                    round2(solution.utilizationWeightPercent()),
                    round2(solution.utilizationVolumePercent())
            );
        }
    }

    public static OptimizeResponse of(
            String truckId,
            List<String> selectedOrderIds,
            long totalPayoutCents,
            long totalWeightLbs,
            long totalVolumeCuft,
            long maxWeightLbs,
            long maxVolumeCuft
    ) {
        return of(truckId, selectedOrderIds, totalPayoutCents, totalWeightLbs, totalVolumeCuft,
                maxWeightLbs, maxVolumeCuft, "BITMASK_DP", null);
    }

    public static OptimizeResponse of(
            String truckId,
            List<String> selectedOrderIds,
            long totalPayoutCents,
            long totalWeightLbs,
            long totalVolumeCuft,
            long maxWeightLbs,
            long maxVolumeCuft,
            String algorithmUsed,
            List<ParetoSolution> paretoSolutions
    ) {
        double weightPct = round2((double) totalWeightLbs / maxWeightLbs * 100.0);
        double volumePct = round2((double) totalVolumeCuft / maxVolumeCuft * 100.0);

        List<ParetoSolutionDto> paretoDtos = paretoSolutions != null
                ? paretoSolutions.stream().map(ParetoSolutionDto::from).toList()
                : null;

        return new OptimizeResponse(
                truckId, selectedOrderIds,
                totalPayoutCents, totalWeightLbs, totalVolumeCuft,
                weightPct, volumePct,
                algorithmUsed, paretoDtos
        );
    }

    private static double round2(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}

package com.smartload.service;

import com.smartload.dto.OptimizeRequest;
import com.smartload.dto.OptimizeResponse;
import com.smartload.model.OptimizationResult;
import com.smartload.model.Order;
import com.smartload.model.ParetoSolution;
import com.smartload.model.Truck;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OptimizationPipelineService {

    private final OrderMapper orderMapper;
    private final RouteCompatibilityFilter routeFilter;
    private final LoadOptimizerService optimizer;
    private final BacktrackingOptimizerService backtrackingOptimizer;

    public OptimizationPipelineService(
            OrderMapper orderMapper,
            RouteCompatibilityFilter routeFilter,
            LoadOptimizerService optimizer,
            BacktrackingOptimizerService backtrackingOptimizer) {
        this.orderMapper = orderMapper;
        this.routeFilter = routeFilter;
        this.optimizer = optimizer;
        this.backtrackingOptimizer = backtrackingOptimizer;
    }

    @Cacheable(value = "optimizationResults", key = "#request.hashCode()")
    public OptimizeResponse execute(OptimizeRequest request) {
        // 1. Validate payload size
        orderMapper.validateRequest(request);

        // 2. Map to domain objects (validates dates, deduplication; strips hazmat)
        Truck truck = orderMapper.toTruck(request.truck());
        List<Order> eligibleOrders = orderMapper.toEligibleOrders(request.orders());

        // 3. Filter to orders on the same lane (same origin→destination)
        List<Order> compatibleOrders = routeFilter.filterToCompatibleLane(eligibleOrders);

        // 4. Run optimization with selected algorithm and mode
        OptimizationResult result;
        String algorithmUsed;

        if (request.getEffectiveAlgorithm() == OptimizeRequest.Algorithm.BACKTRACKING) {
            result = backtrackingOptimizer.optimize(truck, compatibleOrders);
            algorithmUsed = "BACKTRACKING";
        } else {
            result = optimizer.optimize(truck, compatibleOrders, request.getEffectiveOptimizationMode());
            algorithmUsed = "BITMASK_DP";
        }

        // 5. Compute Pareto-optimal solutions if requested
        List<ParetoSolution> paretoSolutions = null;
        if (request.shouldIncludeParetoSolutions()) {
            paretoSolutions = optimizer.findParetoOptimalSolutions(truck, compatibleOrders);
        }

        // 6. Build response
        List<String> selectedIds = result.selectedOrders()
                .stream()
                .map(Order::id)
                .toList();

        return OptimizeResponse.of(
                truck.id(),
                selectedIds,
                result.totalPayoutCents(),
                result.totalWeightLbs(),
                result.totalVolumeCuft(),
                truck.maxWeightLbs(),
                truck.maxVolumeCuft(),
                algorithmUsed,
                paretoSolutions
        );
    }
}

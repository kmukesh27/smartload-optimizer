package com.smartload.service;

import com.smartload.dto.OptimizeRequest;
import com.smartload.dto.OptimizeResponse;
import com.smartload.model.OptimizationResult;
import com.smartload.model.Order;
import com.smartload.model.Truck;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OptimizationPipelineService {

    private final OrderMapper orderMapper;
    private final RouteCompatibilityFilter routeFilter;
    private final LoadOptimizerService optimizer;

    public OptimizationPipelineService(
            OrderMapper orderMapper,
            RouteCompatibilityFilter routeFilter,
            LoadOptimizerService optimizer) {
        this.orderMapper = orderMapper;
        this.routeFilter = routeFilter;
        this.optimizer = optimizer;
    }

    public OptimizeResponse execute(OptimizeRequest request) {
        // 1. Validate payload size
        orderMapper.validateRequest(request);

        // 2. Map to domain objects (validates dates, deduplication; strips hazmat)
        Truck truck = orderMapper.toTruck(request.truck());
        List<Order> eligibleOrders = orderMapper.toEligibleOrders(request.orders());

        // 3. Filter to orders on the same lane (same origin→destination)
        List<Order> compatibleOrders = routeFilter.filterToCompatibleLane(eligibleOrders);

        // 4. Run bitmask DP
        OptimizationResult result = optimizer.optimize(truck, compatibleOrders);

        // 5. Build response
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
                truck.maxVolumeCuft()
        );
    }
}

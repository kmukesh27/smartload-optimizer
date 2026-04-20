package com.smartload.service;

import com.smartload.model.Order;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RouteCompatibilityFilter {

    public List<Order> filterToCompatibleLane(List<Order> orders) {
        if (orders.isEmpty()) {
            return List.of();
        }

        Map<String, List<Order>> byLane = orders.stream()
                .collect(Collectors.groupingBy(Order::laneKey));

        return byLane.values().stream()
                .max(Comparator.comparingLong(
                        (List<Order> lane) -> lane.stream().mapToLong(Order::payoutCents).sum()))
                .orElse(List.of());
    }
}

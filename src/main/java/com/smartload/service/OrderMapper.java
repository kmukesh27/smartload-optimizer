package com.smartload.service;

import com.smartload.dto.OptimizeRequest;
import com.smartload.dto.OrderRequest;
import com.smartload.dto.TruckRequest;
import com.smartload.exception.ValidationException;
import com.smartload.model.Order;
import com.smartload.model.Truck;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class OrderMapper {

    public Truck toTruck(TruckRequest req) {
        return new Truck(req.id(), req.maxWeightLbs(), req.maxVolumeCuft());
    }

    public List<Order> toEligibleOrders(List<OrderRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        Set<String> seenIds = new HashSet<>();
        List<String> errors = new ArrayList<>();
        List<Order> eligible = new ArrayList<>();

        for (OrderRequest req : requests) {
            if (!seenIds.add(req.id())) {
                errors.add("Duplicate order id: " + req.id());
                continue;
            }

            Order order = new Order(
                    req.id(),
                    req.payoutCents(),
                    req.weightLbs(),
                    req.volumeCuft(),
                    req.origin(),
                    req.destination(),
                    req.pickupDate(),
                    req.deliveryDate(),
                    req.isHazmat()
            );

            if (!order.hasValidDates()) {
                errors.add("Order " + req.id() + ": pickup_date must be <= delivery_date");
                continue;
            }

            if (order.isHazmat()) {
                continue;
            }

            eligible.add(order);
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Order validation failed: " + String.join("; ", errors));
        }

        return eligible;
    }


    public void validateRequest(OptimizeRequest request) {
        if (request.orders() != null && request.orders().size() > 22) {
            throw new ValidationException(
                    "Too many orders: maximum 22 allowed, received " + request.orders().size());
        }
    }
}

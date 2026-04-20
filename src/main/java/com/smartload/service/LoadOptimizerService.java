package com.smartload.service;

import com.smartload.model.OptimizationResult;
import com.smartload.model.Order;
import com.smartload.model.Truck;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LoadOptimizerService {

    public OptimizationResult optimize(Truck truck, List<Order> orders) {
        if (orders.isEmpty()) {
            return OptimizationResult.empty();
        }

        int n = orders.size();
        Order[] arr = orders.toArray(new Order[0]);

        long bestPayout = 0L;
        int bestMask = 0;
        long bestWeight = 0L;
        long bestVolume = 0L;

        int totalMasks = 1 << n;

        for (int mask = 1; mask < totalMasks; mask++) {
            long weight = 0L;
            long volume = 0L;
            long payout = 0L;

            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    weight += arr[i].weightLbs();
                    volume += arr[i].volumeCuft();
                    payout += arr[i].payoutCents();

                    if (weight > truck.maxWeightLbs() || volume > truck.maxVolumeCuft()) {
                        payout = -1;
                        break;
                    }
                }
            }

            if (payout > bestPayout) {
                bestPayout = payout;
                bestMask = mask;
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
}

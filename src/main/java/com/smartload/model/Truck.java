package com.smartload.model;

public record Truck(
        String id,
        long maxWeightLbs,
        long maxVolumeCuft
) {}

package com.MaxHighReach;

import java.util.List;

public class RouteInfo {
    private final int durationSeconds;
    private final double distanceMiles;
    private final List<double[]> polylinePoints;

    public RouteInfo(int durationSeconds, double distanceMiles, List<double[]> polylinePoints) {
        this.durationSeconds = durationSeconds;
        this.distanceMiles = distanceMiles;
        this.polylinePoints = polylinePoints;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public double getDistanceMiles() {
        return distanceMiles;
    }

    public List<double[]> getPolylinePoints() {
        return polylinePoints;
    }
}


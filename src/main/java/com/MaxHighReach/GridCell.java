package com.MaxHighReach;

public class GridCell {
    private double minLat;
    private double maxLat;
    private double minLon;
    private double maxLon;
    private String cellName;
    private String type;  // either "greater" or "lesser". Every greater has 4 lessers within it.
    private boolean isDominant; // for if we use the cell of the "greater" and "lesser" types at a specific location


    public GridCell(String type, double minLat, double maxLat, double minLon, double maxLon, String cellName, boolean isDominant) {
        this.type = type;
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;
        this.cellName = cellName;
        this.isDominant = isDominant;
    }


    public String getCellName() {
        return cellName;
    }


    public boolean isDominant() {
        return isDominant;
    }


    public boolean contains(double latitude, double longitude) {
        return latitude >= minLat && latitude <= maxLat && longitude >= minLon && longitude <= maxLon;
    }

    public String describeBounds() {
        return String.format(
            "GridCell '%s' [%s]: Latitudes (%.6f to %.6f), Longitudes (%.6f to %.6f)",
            cellName,
            type,
            minLat,
            maxLat,
            minLon,
            maxLon
        );
    }

}







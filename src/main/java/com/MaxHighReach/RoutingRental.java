package com.MaxHighReach;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RoutingRental {

    private int id;
    private String streetAddress;
    private String liftType;
    private double latitude;
    private double longitude;
    private String route; 
    private int driverNumber;
    private String driverInitial;
    private String driver;
    private String deliveryTruck;

    public RoutingRental(int id, String streetAddress, String liftType, double latitude, 
                         double longitude, String driverInitial, int driverNumber, String deliveryTruck) {
        this.id = id;
        this.streetAddress = streetAddress;
        this.liftType = liftType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.driverInitial = driverInitial;
        this.driverNumber = driverNumber;
        this.deliveryTruck = deliveryTruck;
    }

    @JsonProperty("id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @JsonProperty("streetAddress")
    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    @JsonProperty("liftType")
    public String getLiftType() {
        return liftType;
    }

    public void setLiftType(String liftType) {
        this.liftType = liftType;
    }

    @JsonProperty("latitude")
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @JsonProperty("longitude")
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    @JsonProperty("driverNumber")
    public int getDriverNumber() {
        return driverNumber;
    }

    public void setDriverNumber(int driverNumber) {
        this.driverNumber = driverNumber;
    }

    @JsonProperty("driverInitial")
    public String getDriverInitial() {
        return driverInitial;
    }

    public void setDriverInitial(String driverInitial) {
        this.driverInitial = driverInitial;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    @JsonProperty("deliveryTruck")
    public String getDeliveryTruck() {
        return deliveryTruck;
    }

    public void setDeliveryTruck(String deliveryTruck) {
        this.deliveryTruck = deliveryTruck;
    }

    @Override
    public String toString() {
        return "RoutingRental{" +
                "id=" + id +
                ", streetAddress='" + streetAddress + '\'' +
                ", liftType='" + liftType + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", driverInitial='" + driverInitial + '\'' +
                ", driverNumber=" + driverNumber +
                ", deliveryTruck='" + deliveryTruck + '\'' +
                '}';
    }
}

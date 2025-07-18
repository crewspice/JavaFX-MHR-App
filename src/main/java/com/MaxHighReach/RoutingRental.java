package com.MaxHighReach;

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
        this.streetAddress = "unknown";
        this.liftType = "unknown";
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.driverInitial = driverInitial;
        this.driverNumber = driverNumber;
        this.deliveryTruck = deliveryTruck;
    }

    public int getid() {
        return id;
    }

    public void setid(int id) {
        this.id = id;
    }

    public String getStreetAddress() {
        return streetAddress;
    }
    
    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }
    
    public String getLiftType() {
        return liftType;
    }
    
    public void setLiftType(String liftType) {
        this.liftType = liftType;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
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

    public int getDriverNumber() {
        return driverNumber;
    }

    public void setDriverNumber(int driverNumber) {
        this.driverNumber = driverNumber;
    }

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
                ", streetAdress=" + "null" +
                ", liftType=" + "null" +
                ", latitude=" + 0 +
                ", longitude=" + 0 +
                ", driverInitial='" + driverInitial + '\'' +
                ", driverNumber=" + driverNumber +
                ", deliveryTruck='" + deliveryTruck + '\'' +
                '}';
    }
}

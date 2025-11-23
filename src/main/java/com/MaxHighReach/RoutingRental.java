package com.MaxHighReach;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RoutingRental {

    private int id;
    private int orderId;
    private String type;
    private String name;
    private String serviceType;
    private String deliveryDate;
    private String serviceDate;
    private String status;
    private String siteName;
    private String streetAddress;
    private String newSiteName;
    private String newStreetAddress;
    private String newCity;
    private String city;
    private String liftType;
    private String newLiftType;
    private String time;
    private double latitude;
    private double longitude;
    private String route; 
    private int driverNumber;
    private String driverInitial;
    private String driver;
    private String truck;
    private String reason;
    private String locationNotes;
    private String preTripInstructions;
    private String orderedByContactName;
    private String orderedByContactNumber;
    private String siteContactName;
    private String siteContactNumber;

    public RoutingRental(int id, int orderId, String type, String name, String siteName, String streetAddress,
                         String city, String liftType, String time, double latitude, 
                         double longitude, String driverInitial, int driverNumber, String truck,
                         String orderedByContactName, String orderedByContactNumber,
                         String siteContactName, String siteContactNumber,
                         String locationNotes, String preTripInstructions) {
        this.id = id;
        this.orderId = orderId;
        this.type = type;
        this.name = name;
        this.siteName = siteName;
        this.streetAddress = streetAddress;
        this.city = city;
        this.liftType = liftType;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.driverInitial = driverInitial;
        this.driverNumber = driverNumber;
        this.truck = truck;
        this.orderedByContactName = orderedByContactName;
        this.orderedByContactNumber = orderedByContactNumber;
        this.siteContactName = siteContactName;
        this.siteContactNumber = siteContactNumber;
        this.locationNotes = locationNotes;
        this.preTripInstructions = preTripInstructions;
    }

    @JsonProperty("id")
    public int getId() {
        return id;
    }
    public void setId(int id) { this.id = id; }

    @JsonProperty("orderId")
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    @JsonProperty("type")
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @JsonProperty("name")
    public String getName() {return name; }
    public void setName(String name) { this.name = name; }

    @JsonProperty("deliveryDate")
    public String getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(String deliveryDate) { this.deliveryDate = deliveryDate; }

    @JsonProperty("serviceDate")
    public String getServiceDate() { return serviceDate; }
    public void setServiceDate(String serviceDate) { this.serviceDate = serviceDate; }

    @JsonProperty("serviceType")
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    @JsonProperty("status")
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @JsonProperty("siteName")
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }

    @JsonProperty("streetAddress")
    public String getStreetAddress() { return streetAddress; }
    public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }

    @JsonProperty("city")
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    @JsonProperty("newSiteName")
    public String getNewSiteName() { return newSiteName; }
    public void setNewSiteName(String newSiteName) { this.newSiteName = newSiteName; }

    @JsonProperty("newStreetAddress")
    public String getNewStreetAddress() { return newStreetAddress; }
    public void setNewStreetAddress(String newStreetAddress) { this.newStreetAddress = newStreetAddress; }

    @JsonProperty("newCity")
    public String getNewCity() { return newCity; }
    public void setNewCity(String newCity) { this.newCity = newCity; }

    @JsonProperty("liftType")
    public String getLiftType() { return liftType; }
    public void setLiftType(String liftType) { this.liftType = liftType; }

    @JsonProperty("newLiftType")
    public String getNewLiftType() { return newLiftType; }
    public void setNewLiftType(String newLiftType) { this.newLiftType = newLiftType; }

    @JsonProperty("time")
    public String getTime() { return time;}
    public void setTime(String time) { this.time = time; }

    @JsonProperty("latitude")
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    @JsonProperty("longitude")
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    @JsonProperty("driverNumber")
    public int getDriverNumber() { return driverNumber; }
    public void setDriverNumber(int driverNumber) { this.driverNumber = driverNumber; }

    @JsonProperty("driverInitial")
    public String getDriverInitial() { return driverInitial; }
    public void setDriverInitial(String driverInitial) { this.driverInitial = driverInitial; }

    public String getDriver() { return driver; }
    public void setDriver(String driver) { this.driver = driver; }

    @JsonProperty("truck")
    public String gettruck() { return truck; }
    public void settruck(String truck) { this.truck = truck; }

    @JsonProperty("reason")
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    @JsonProperty("orderedByContactName")
    public String getOrderedByContactName() { return orderedByContactName; }
    public void setOrderedByContactName(String orderedByContactName) { this.orderedByContactName = orderedByContactName; }

    @JsonProperty("orderedByContactNumber")
    public String getOrderedByContactNumber() { return orderedByContactNumber; }
    public void setOrderedByContactNumber(String orderedByContactNumber) { this.orderedByContactNumber = orderedByContactNumber; }

    @JsonProperty("siteContactName")
    public String getSiteContactName() { return siteContactName; }
    public void setSiteContactName(String siteContactName) { this.siteContactName = siteContactName; }

    @JsonProperty("siteContactNumber")
    public String getSiteContactNumber() { return siteContactNumber; }
    public void setSiteContactNumber(String siteContactNumber) { this.siteContactNumber = siteContactNumber; }

    @JsonProperty("locationNotes")
    public String getLocationNotes() { return locationNotes; }
    public void setLocationNotes(String locationNotes) { this.locationNotes = locationNotes;}

    @JsonProperty("preTripInstructions")
    public String getPreTripInstructions() {return preTripInstructions; }
    public void setPreTripInstructions(String preTripInstructions) {this.preTripInstructions = preTripInstructions;}

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
                ", truck='" + truck + '\'' +
                ", orderedByContactName='" + orderedByContactName + '\'' +
                ", orderedByContactNumber='" + orderedByContactNumber + '\'' +
                ", siteContactName='" + siteContactName + '\'' +
                ", siteContactNumber='" + siteContactNumber + '\'' +
                '}';
    }
}

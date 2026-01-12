package com.MaxHighReach;

public class Service {
    private final int serviceId;
    private final String serviceType;
    private final String time;
    private final String date;
    private final String reason;
    private final boolean billable;
    private final Integer previousServiceId;
    private final int newRentalOrderId;
    private final int newLiftId;
    private final String newLiftType;
    private final String newLiftSerial;
    private final int oldLiftId;
    private final String oldLiftType;
    private final String oldLiftSerial;
    private final String newSiteName;
    private final String newStreetAddress;
    private final String newCity;
    private final Double newLatitude;
    private final Double newLongitude;
    private final String notes;

    public Service(int serviceId, String serviceType,
                   String time, String date, String reason, boolean billable,
                   Integer previousServiceId, int newRentalOrderId, int newLiftId,
                   String newLiftType, String newLiftSerial, int oldLiftId,
                   String oldLiftType, String oldLiftSerial, 
                   String newSiteName, String newStreetAddress,
                   String newCity, double newLatitude, double newLongitude, 
                   String notes) {
        this.serviceId = serviceId;
        this.serviceType = serviceType;
        this.time = time;
        this.date = date;
        this.reason = reason;
        this.billable = billable;
        this.previousServiceId = previousServiceId;
        this.newRentalOrderId = newRentalOrderId;
        this.newLiftId = newLiftId;
        this.newLiftType = newLiftType;
        this.newLiftSerial = newLiftSerial;
        this.oldLiftId = oldLiftId;
        this.oldLiftType = oldLiftType;
        this.oldLiftSerial = oldLiftSerial;
        this.newSiteName = newSiteName;
        this.newStreetAddress = newStreetAddress;
        this.newCity = newCity;
        this.newLatitude = newLatitude;
        this.newLongitude = newLongitude;
        this.notes = notes;
    }

    public int getServiceId() { return serviceId; }
    public String getServiceType() { return serviceType; }
    public String getTime() { return time; }
    public String getDate() { return date; }
    public String getReason() { return reason; }
    public boolean isBillable() { return billable; }
    public Integer getPreviousServiceId() { return previousServiceId; }
    public int getNewRentalOrderId() { return newRentalOrderId; }
    public int getNewLiftId() { return newLiftId; }
    public String getNewLiftType() {return newLiftType; }
    public String getNewLiftSerial() { return newLiftSerial; }
    public int getOldLiftId() { return oldLiftId; }
    public String getOldLiftType() { return oldLiftType; }
    public String getOldLiftSerial() { return oldLiftSerial; }
    public String getNewSiteName() {return newSiteName; }
    public String getNewStreetAddress() {return newStreetAddress; }
    public String getNewCity() {return newCity; }
    public double getNewLatitude() {return newLatitude; }
    public double getNewLongitude() {return newLongitude; }
    public String getNotes() { return notes; }
}

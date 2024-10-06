package com.MaxHighReach;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CustomerRental {
    private final StringProperty customerId;  // Changed to SimpleIntegerProperty
    private final StringProperty name;
    private final StringProperty orderDate;
    private final StringProperty deliveryDate;
    private final StringProperty pickupDate;
    private final StringProperty deliveryTime;
    private final StringProperty addressBlockOne;
    private final StringProperty addressBlockTwo;
    private final StringProperty addressBlockThree;
    private final StringProperty driver;
    private final StringProperty liftType;
    private final StringProperty status;
    private final SimpleIntegerProperty refNumber;  // Changed to SimpleIntegerProperty
    private boolean selected;
    private boolean isFlagged;
    private boolean isContractWritten;
    private final SimpleIntegerProperty rentalOrderId;
    private final SimpleIntegerProperty rentalItemId;

    // Constructor with all relevant fields
    public CustomerRental(String customerId, String name, String deliveryDate, String deliveryTime, String driver, String status, int refNumber, int rentalId) {
        this.customerId = new SimpleStringProperty(customerId); // Initialize SimpleIntegerProperty
        this.name = new SimpleStringProperty(name);
        this.orderDate = new SimpleStringProperty("Unknown");
        this.deliveryDate = new SimpleStringProperty(deliveryDate);
        this.pickupDate = new SimpleStringProperty("Unknown");
        this.deliveryTime = new SimpleStringProperty(deliveryTime); // Initialize delivery time
        this.addressBlockOne = new SimpleStringProperty("Town of Windsor Fire #8");
        this.addressBlockTwo = new SimpleStringProperty("1283 Hilltop Circle");
        this.addressBlockThree = new SimpleStringProperty("Windsor");
        this.driver = new SimpleStringProperty(driver);
        this.liftType = new SimpleStringProperty("Unknown");
        this.status = new SimpleStringProperty(status);
        this.refNumber = new SimpleIntegerProperty(refNumber); // Initialize SimpleIntegerProperty
        this.selected = false;
        this.isFlagged = isFlagged;
        this.isContractWritten = isContractWritten;
        this.rentalOrderId = new SimpleIntegerProperty(rentalId);
        this.rentalItemId = new SimpleIntegerProperty(0);
    }

    // Constructor without driver and status
    public CustomerRental(String customerId, String name, String orderDate, String deliveryTime) {
        this(customerId, name, orderDate, deliveryTime, "", "Unknown", 99999, 0); // Default values
    }

    // Getters and setters
    public String getCustomerId() {
        return customerId.get();
    }

    public void setCustomerId(String customerId) {
        this.customerId.set(customerId); // Use set method for SimpleIntegerProperty
    }

    public StringProperty customerIdProperty() {
        return customerId; // Return the property for binding
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getOrderDate() {
        return orderDate.get();
    }

    public void setOrderDate(String orderDate) {
        this.orderDate.set(orderDate);
    }

    public StringProperty orderDateProperty() {
        return orderDate;
    }

    public String getDeliveryDate() {
        return deliveryDate.get();
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate.set(deliveryDate);
    }

    public StringProperty deliveryDateProperty() {
        return deliveryDate;
    }

    public String getPickupDate() {
        return pickupDate.get();
    }

    public void setPickupDate(String pickupDate) {
        this.pickupDate.set(pickupDate);
    }

    public StringProperty pickupDateProperty() {
        return pickupDate;
    }

    public String getDeliveryTime() {
        return deliveryTime.get(); // Getter for delivery time
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime.set(deliveryTime); // Setter for delivery time
    }

    public StringProperty deliveryTimeProperty() {
        return deliveryTime; // Return the property for binding
    }

    public String getAddressBlockOne() {
        return addressBlockOne.get();
    }

    public void setAddressBlockOne(String addressBlockOne) {
        this.addressBlockOne.set(addressBlockOne);
    }

    public StringProperty addressBlockOneProperty() {
        return addressBlockOne;
    }

    public String getAddressBlockTwo() {
        return addressBlockTwo.get();
    }

    public void setAddressBlockTwo(String addressBlockTwo) {
        this.addressBlockTwo.set(addressBlockTwo);
    }

    public StringProperty addressBlockTwoProperty() {
        return addressBlockTwo;
    }

    public String getAddressBlockThree() {
        return addressBlockThree.get();
    }

    public void setAddressBlockThree(String addressBlockThree) {
        this.addressBlockThree.set(addressBlockThree);
    }

    public StringProperty addressBlockThreeProperty() {
        return addressBlockThree;
    }

    public String getDriver() {
        return driver.get();
    }

    public void setDriver(String driver) {
        this.driver.set(driver);
    }

    public StringProperty driverProperty() {
        return driver;
    }

    public String getLiftType() {
        return liftType.get();
    }

    public void setLiftType(String liftType) {
        this.liftType.set(liftType);
    }

    public StringProperty liftTypeProperty() {
        return liftType;
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public StringProperty statusProperty() {
        return status;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isFlagged() {
        return isFlagged;
    }

    public void setFlagged(boolean flagged) {
        isFlagged = flagged;
    }

    public boolean isContractWritten() {
        return isContractWritten;
    }

    public void setContractWritten(boolean contractWritten) {
        isContractWritten = contractWritten;
    }

    // Getter and Setter for rentalId
    public int getRentalOrderId() {
        return rentalOrderId.get();
    }

    public void setRentalOrderId(int rentalId) {
        this.rentalOrderId.set(rentalId); // Use set method for SimpleIntegerProperty
    }

    public SimpleIntegerProperty rentalOrderIdProperty() {
        return rentalOrderId;
    }

    public int getRentalItemId() {
        return rentalItemId.get();
    }

    public void setRentalItemId(int itemId) {
        this.rentalItemId.set(itemId); // Use set method for SimpleIntegerProperty
    }

    public SimpleIntegerProperty rentalItemIdProperty() {
        return rentalItemId;// Return the property for binding
    }
}

package com.MaxHighReach;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CustomerRental {
    private final StringProperty customerId;  // Changed to SimpleIntegerProperty
    private final StringProperty name;
    private final StringProperty orderDate;
    private final StringProperty deliveryTime; // New property for delivery time
    private final StringProperty driver;
    private final StringProperty status;
    private final SimpleIntegerProperty refNumber;  // Changed to SimpleIntegerProperty
    private boolean selected;
    private boolean isFlagged;
    private boolean isContractWritten;
    private final SimpleIntegerProperty rentalId; // Changed to SimpleIntegerProperty

    // Constructor with all relevant fields
    public CustomerRental(String customerId, String name, String orderDate, String deliveryTime, String driver, String status, int refNumber, int rentalId) {
        this.customerId = new SimpleStringProperty(customerId); // Initialize SimpleIntegerProperty
        this.name = new SimpleStringProperty(name);
        this.orderDate = new SimpleStringProperty(orderDate);
        this.deliveryTime = new SimpleStringProperty(deliveryTime); // Initialize delivery time
        this.driver = new SimpleStringProperty(driver);
        this.status = new SimpleStringProperty(status);
        this.refNumber = new SimpleIntegerProperty(refNumber); // Initialize SimpleIntegerProperty
        this.selected = false;
        this.isFlagged = isFlagged;
        this.isContractWritten = isContractWritten;
        this.rentalId = new SimpleIntegerProperty(rentalId); // Initialize SimpleIntegerProperty
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

    public String getDeliveryTime() {
        return deliveryTime.get(); // Getter for delivery time
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime.set(deliveryTime); // Setter for delivery time
    }

    public StringProperty deliveryTimeProperty() {
        return deliveryTime; // Return the property for binding
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
    public int getRentalId() {
        return rentalId.get();
    }

    public void setRentalId(int rentalId) {
        this.rentalId.set(rentalId); // Use set method for SimpleIntegerProperty
    }

    public SimpleIntegerProperty rentalIdProperty() {
        return rentalId; // Return the property for binding
    }
}

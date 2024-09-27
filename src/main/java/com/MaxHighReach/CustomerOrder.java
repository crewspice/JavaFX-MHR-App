package com.MaxHighReach;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CustomerOrder {
    private int customerId;
    private final StringProperty name;
    private final StringProperty orderDate;
    private final StringProperty driver;
    private final StringProperty status;
    private int refNumber;
    private boolean selected;
    private boolean isFlagged;
    private boolean isContractWritten;
    private int rentalId; // New field for rental ID

    // Constructor with all relevant fields
    public CustomerOrder(int customerId, String name, String orderDate, String driver, String status, int refNumber, int rentalId) {
        this.customerId = customerId;
        this.name = new SimpleStringProperty(name);
        this.orderDate = new SimpleStringProperty(orderDate);
        this.driver = new SimpleStringProperty(driver);
        this.status = new SimpleStringProperty(status);
        this.refNumber = refNumber;
        this.selected = false;
        this.isFlagged = isFlagged;
        this.isContractWritten = isContractWritten;
        this.rentalId = rentalId; // Initialize rentalId
    }

    // Constructor without driver and status
    public CustomerOrder(int customerId, String name, String orderDate) {
        this(customerId, name, orderDate, "", "Unknown", 99999, 0); // Default values
    }

    // Getters and setters
    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
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
        return rentalId;
    }

    public void setRentalId(int rentalId) {
        this.rentalId = rentalId;
    }
}

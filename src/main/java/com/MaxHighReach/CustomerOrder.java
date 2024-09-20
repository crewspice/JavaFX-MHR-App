package com.MaxHighReach;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CustomerOrder {
    private int customerId;
    private final StringProperty name;
    private final StringProperty orderDate;
    private final StringProperty driver;
    private final StringProperty status; // Keep status as StringProperty
    private boolean selected;

    // Constructor with all relevant fields
    public CustomerOrder(int customerId, String name, String orderDate, String driver, String status) {
        this.customerId = customerId;
        this.name = new SimpleStringProperty(name);
        this.orderDate = new SimpleStringProperty(orderDate);
        this.driver = new SimpleStringProperty(driver);
        this.status = new SimpleStringProperty(status); // Initialize as StringProperty
        this.selected = false; // default value
    }

    // Constructor without driver and status (if needed, can be removed)
    public CustomerOrder(int customerId, String name, String orderDate) {
        this(customerId, name, orderDate, "", "Unknown"); // Default driver and status
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

    // Status field methods
    public String getStatus() { // Return the status as String
        return status.get();
    }

    public void setStatus(String status) { // Set status with a String
        this.status.set(status);
    }

    public StringProperty statusProperty() {
        return status; // Return the StringProperty
    }

    // Selected field methods
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}

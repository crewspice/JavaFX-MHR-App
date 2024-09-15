package com.MaxHighReach;

import com.MaxHighReach.utils.StatusNodeFactory;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

public class CustomerOrder {
    private int customerId;
    private final StringProperty name;
    private final StringProperty orderDate;
    private double amount;
    private final StringProperty driver;
    private final ObjectProperty<Node> status; // Updated to ObjectProperty<Node>
    private boolean selected;

    // Constructor with all fields
    public CustomerOrder(int customerId, String name, String orderDate, double amount, String driver, Node status) {
        this.customerId = customerId;
        this.name = new SimpleStringProperty(name);
        this.orderDate = new SimpleStringProperty(orderDate);
        this.amount = amount;
        this.driver = new SimpleStringProperty(driver);
        this.status = new SimpleObjectProperty<>(status); // Initialize with Node
        this.selected = false; // default value
    }

    // Constructor without driver and status
    public CustomerOrder(int customerId, String name, String orderDate, double amount) {
        this(customerId, name, orderDate, amount, "", StatusNodeFactory.createStatusNode("Unknown")); // Default status Node
    }

    // Constructor without status (for compatibility)
    public CustomerOrder(int customerId, String name, String orderDate, double amount, String driver) {
        this(customerId, name, orderDate, amount, driver, StatusNodeFactory.createStatusNode("Unknown")); // Default status Node
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
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

    // Updated status field methods
    public Node getStatus() {
        return status.get();
    }

    public void setStatus(Node status) {
        this.status.set(status);
    }

    public ObjectProperty<Node> statusProperty() {
        return status;
    }

    // Selected field methods
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}

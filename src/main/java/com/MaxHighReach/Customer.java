package com.MaxHighReach;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Customer {
    private StringProperty customerId; // Changed to StringProperty
    private StringProperty customerName;
    private StringProperty email;

    // Constructor
    public Customer(String customerId, String customerName, String email) {
        this.customerId = new SimpleStringProperty(customerId);
        this.customerName = new SimpleStringProperty(customerName);
        this.email = new SimpleStringProperty(email);
    }

    // Getters for properties
    public StringProperty customerIdProperty() {
        return customerName;
    }

    public String getCustomerId() {
        return customerId.get();
    }

    public StringProperty nameProperty() {
        return customerName;
    }

    public StringProperty emailProperty() {
        return email;
    }

    // Getters and Setters for regular String values (if needed)
    public String getCustomerName() {
        return customerName.get();
    }

    public void setCustomerName(String customerId) {
        this.customerName.set(customerId);
    }

    public String getName() {
        return customerName.get();
    }

    public void setName(String name) {
        this.customerName.set(name);
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }
}

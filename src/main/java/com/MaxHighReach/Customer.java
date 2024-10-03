package com.MaxHighReach;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Customer {
    private StringProperty customerId; // Changed to StringProperty
    private StringProperty name;
    private StringProperty email;

    // Constructor
    public Customer(String customerId, String name, String email) {
        this.customerId = new SimpleStringProperty(customerId);
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
    }

    // Getters for properties
    public StringProperty customerIdProperty() {
        return customerId;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty emailProperty() {
        return email;
    }

    // Getters and Setters for regular String values (if needed)
    public String getCustomerId() {
        return customerId.get();
    }

    public void setCustomerId(String customerId) {
        this.customerId.set(customerId);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }
}

package com.MaxHighReach;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.HashMap;
import java.util.Map;

public class Customer {
    private StringProperty customerId; // Changed to StringProperty
    private StringProperty customerName;
    private StringProperty email;
    private Map<String, String> orderingContactsPhoneNumbers = new HashMap<>();
    private Map<String, String> siteContactsPhoneNumbers = new HashMap<>();
    private Map<String, String> orderingContactsIds = new HashMap<>();
    private Map<String, String> siteContactsIds = new HashMap<>();

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

    public void addOrderingContact(String name, String phoneNumber, String contactId) {
        orderingContactsPhoneNumbers.put(name, phoneNumber);
        orderingContactsIds.put(name, contactId);
    }

    public void addSiteContact(String name, String phoneNumber, String contactId) {
        siteContactsPhoneNumbers.put(name, phoneNumber);
        siteContactsIds.put(name, contactId);
    }

    public Map<String, String> getOrderingContactsPhoneNumbers() {
        return orderingContactsPhoneNumbers;
    }

    public Map<String, String> getSiteContactsPhoneNumbers() {
        return siteContactsPhoneNumbers;
    }

    public String getOrderingContactId(String name) {
        if (orderingContactsIds.containsKey(name)) {
            return orderingContactsIds.get(name);
        } else {
            return null;
        }
    }

    public String getSiteContactId(String name) {
        if (siteContactsIds.containsKey(name)) {
            return siteContactsIds.get(name);
        } else {
            return null;
        }
    }

    public Map<String, String> getOrderingContactsIds() {
        return orderingContactsIds;
    }

    public Map<String, String> getSiteContactsIds() {
        return siteContactsIds;
    }

    public boolean isOrderingContactExtant(String name, String phoneNumber){
        return orderingContactsPhoneNumbers.containsKey(name) &&
                orderingContactsPhoneNumbers.get(name).equals(phoneNumber);
    }

    public boolean isSiteContactExtant(String name, String phoneNumber){
        return siteContactsPhoneNumbers.containsKey(name) &&
                siteContactsPhoneNumbers.get(name).equals(phoneNumber);
    }
}

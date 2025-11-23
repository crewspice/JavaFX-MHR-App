package com.MaxHighReach;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/* This class represents the data structure for single object lifts in the MHR inventory.
*  The ScissorLift class represents the graphic scissor lift on the app stage */
public class Lift {
    private StringProperty liftId;
    private StringProperty liftType = new SimpleStringProperty("");
    private StringProperty serialNumber = new SimpleStringProperty("");
    private StringProperty model = new SimpleStringProperty("");
    private boolean generic = false;

    public Lift(String liftId, String liftType) {
        this.liftId = new SimpleStringProperty(liftId);
        System.out.println("liftType from db is: " + liftType);
        setLiftType(liftType);
    }

    public StringProperty liftIdProperty() {return liftId;}

    public String getLiftId() {return liftId.get();}

    public void setLiftId(String liftId) {this.liftId.set(liftId);}

    public StringProperty liftTypeProperty() {return liftType;}

    public String getLiftType() {return liftType.get();}

    public void setLiftType(String liftType) {
        if (liftType == null) {
            this.liftType.set(null);
            return;
        }
    
        String input = liftType.toLowerCase().trim();
        String result = input; // start with normalized input
    
        // Step 1: find exact or close matches
        for (String validLiftType : Config.ASCENDING_LIFT_TYPES) {
            String lowerCaseLiftType = validLiftType.toLowerCase();
            if (input.equals(lowerCaseLiftType)) {
                result = validLiftType; // preserve canonical form
                break;
            }
        }
    
        // Step 2: handle special mappings
        if (result.equals("19")) {
            result = "19s";
        } else if (result.equals("mast")) {
            result = "12m";
        } else if (result.equals("26") && input.contains("s")) {
            result = "26s";
        }
    
        // Step 3: store
        this.liftType.set(result);
    }
    

    public StringProperty serialNumberProperty() {return serialNumber;}

    public String getSerialNumber() {return serialNumber.get();}

    public void setSerialNumber(String serialNumber) {this.serialNumber.set(serialNumber);}

    public StringProperty modelProperty() {return model;}

    public String getModel() {return model.get();}

    public void setModel(String model) {this.model.set(model);}

}


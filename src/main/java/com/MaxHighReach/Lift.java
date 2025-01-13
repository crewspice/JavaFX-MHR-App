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
        setLiftType(liftType);
    }

    public StringProperty liftIdProperty() {return liftId;}

    public String getLiftId() {return liftId.get();}

    public void setLiftId(String liftId) {this.liftId.set(liftId);}

    public StringProperty liftTypeProperty() {return liftType;}

    public String getLiftType() {return liftType.get();}

    public void setLiftType(String liftType) {
        // Convert input liftType to lowercase for case-insensitive matching
        String result = liftType.toLowerCase();
        // Step 1: Iterate over the lift types in the map and find matches
        for (String validLiftType : Config.ASCENDING_LIFT_TYPES) {
            String lowerCaseLiftType = validLiftType.toLowerCase();  // lowercase of the valid lift type
            if (result.contains(lowerCaseLiftType)) {
                // Replace the matched part with the standardized lift type
                result = lowerCaseLiftType;
            }
        }
        if (result.equals("19")) {
            result = "19s";
        }
        if (result.equals("mast")) {
            result = "12m";
        }
        if (result.equals("26")) {
            if (liftType.contains("S")) {
                result = "26s";
            }
        }
        // Step 2: Store the processed lift type
        this.liftType.set(result);
    }

    public StringProperty serialNumberProperty() {return serialNumber;}

    public String getSerialNumber() {return serialNumber.get();}

    public void setSerialNumber(String serialNumber) {this.serialNumber.set(serialNumber);}

    public StringProperty modelProperty() {return model;}

    public String getModel() {return model.get();}

    public void setModel(String model) {this.model.set(model);}

}


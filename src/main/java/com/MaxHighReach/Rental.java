package com.MaxHighReach;


import com.mysql.cj.x.protobuf.MysqlxDatatypes;
import javafx.application.Platform;
import javafx.beans.property.*;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Rental {
    private final StringProperty customerId;  // Changed to SimpleIntegerProperty
    private final StringProperty name;
    private final StringProperty orderedByName;
    private final StringProperty orderedByPhone;
    private final StringProperty orderDate;
    private final StringProperty deliveryDate;
    private final StringProperty callOffDate;
    private final SimpleBooleanProperty autoTerm;
    private final SimpleIntegerProperty rentalDuration;
    private final StringProperty pickupDate;
    private final StringProperty deliveryTime;
    private final StringProperty addressBlockOne;
    private final StringProperty addressBlockTwo;
    private final StringProperty addressBlockThree;
    private final StringProperty city;
    private final DoubleProperty longitude;
    private final DoubleProperty latitude;
    private final StringProperty siteContactName;
    private final StringProperty siteContactPhone;
    private final StringProperty poNumber;
    private final StringProperty locationNotes;
    private final StringProperty preTripInstructions;
    private final StringProperty driver;
    private final StringProperty driverInitial;
    private final SimpleIntegerProperty driverNumber;
    private final StringProperty serialNumber;
    private final SimpleIntegerProperty liftId;
    private final StringProperty liftType;
    private final StringProperty shortLiftType;
    private final StringProperty status;
    private boolean selected;
    private boolean isFlagged;
    private SimpleBooleanProperty isContractWritten;
    private SimpleBooleanProperty writingInvoice;
    private SimpleBooleanProperty isInvoiceWritten;
    private final SimpleIntegerProperty rentalOrderId;
    private final SimpleIntegerProperty rentalItemId;
    private final SimpleBooleanProperty singleItemOrder;
    private final SimpleBooleanProperty needsInvoice;
    private final StringProperty latestBilledDate;
    private final StringProperty deliveryTruck;
    private final StringProperty pickUpTruck;
   
    // Constructor with all relevant fields
    public Rental(String customerId, String name, String deliveryDate, String deliveryTime, String callOffDate,
                          String driver, int driverNumber, String status, String poNumber, int rentalId, boolean needsInvoice,
                          String latestBilledDate) {
        this.customerId = new SimpleStringProperty(customerId); // Initialize SimpleIntegerProperty
        this.name = new SimpleStringProperty(name);
        this.orderedByName = new SimpleStringProperty(null);
        this.orderedByPhone = new SimpleStringProperty(null);
        this.orderDate = new SimpleStringProperty("Unknown");
        this.deliveryDate = new SimpleStringProperty(deliveryDate);
        this.pickupDate = new SimpleStringProperty("Unknown");
        this.deliveryTime = new SimpleStringProperty(deliveryTime); // Initialize delivery time
        this.callOffDate = new SimpleStringProperty(callOffDate);
        this.autoTerm = new SimpleBooleanProperty(false);
        this.rentalDuration = new SimpleIntegerProperty(calculateRentalDuration());
        this.addressBlockOne = new SimpleStringProperty("Town of Windsor Fire #8");
        this.addressBlockTwo = new SimpleStringProperty("1283 Hilltop Circle");
        this.addressBlockThree = new SimpleStringProperty("Windsor");
        this.city = new SimpleStringProperty("Windsor");
        this.longitude = new SimpleDoubleProperty(0.0);
        this.latitude = new SimpleDoubleProperty(0.0);
        this.siteContactName = new SimpleStringProperty(null);
        this.siteContactPhone = new SimpleStringProperty(null);
        this.poNumber = new SimpleStringProperty(poNumber);
        this.locationNotes = new SimpleStringProperty(null);
        this.preTripInstructions = new SimpleStringProperty(null);
        this.driver = new SimpleStringProperty(driver);
        this.driverInitial = new SimpleStringProperty(driver);
        this.driverNumber = new SimpleIntegerProperty(driverNumber);
        this.serialNumber = new SimpleStringProperty("118280");
        this.liftId = new SimpleIntegerProperty(0);
        this.liftType = new SimpleStringProperty("Unknown");
        this.shortLiftType = new SimpleStringProperty("Unknown");
        this.status = new SimpleStringProperty(status);
        this.selected = false;
        this.isFlagged = isFlagged;
        this.isContractWritten = new SimpleBooleanProperty(false);
        this.writingInvoice = new SimpleBooleanProperty(false);
        this.isInvoiceWritten = new SimpleBooleanProperty(false);
        this.rentalOrderId = new SimpleIntegerProperty(rentalId);
        this.rentalItemId = new SimpleIntegerProperty(0);
        this.singleItemOrder = new SimpleBooleanProperty(false);
        this.needsInvoice = new SimpleBooleanProperty(needsInvoice);
        this.latestBilledDate = new SimpleStringProperty(latestBilledDate);
        this.deliveryTruck = new SimpleStringProperty(null);
        this.pickUpTruck = new SimpleStringProperty(null);

    }
   
    // Constructor without driver and status
    public Rental(String customerId, String name, String orderDate, String deliveryTime) {
        this(customerId, name, orderDate, deliveryTime, null, "", 0, "Unknown", "99999", 0, false, "Unknown"); // Default values
    }


    // this constructor made with utilization and map data in mind
    public Rental(String customerId, String name, String deliveryDate, String callOffDate, String poNumber,
                          String orderedByName, String orderedByPhone, boolean autoTerm, String addressBlockOne,
                          String addressBlockTwo, String addressBlockThree, int rentalItemId, String serialNumber,
                          boolean singleItemOrder, int rentalOrderId, String siteContactName, String siteContactPhone, 
                          double latitude, double longitude, String liftType, String status) {
        this.customerId = new SimpleStringProperty(customerId);
        this.name = new SimpleStringProperty(name);
        this.orderedByName = new SimpleStringProperty(orderedByName);
        this.orderedByPhone = new SimpleStringProperty(orderedByPhone);
        this.orderDate = new SimpleStringProperty("Unknown");
        this.deliveryDate = new SimpleStringProperty(deliveryDate);
        this.callOffDate = new SimpleStringProperty(callOffDate);
        this.autoTerm = new SimpleBooleanProperty(autoTerm);
        this.rentalDuration = new SimpleIntegerProperty(0);
        this.pickupDate = new SimpleStringProperty("Unknown");
        this.deliveryTime = new SimpleStringProperty("Unknown");
        this.addressBlockOne = new SimpleStringProperty(addressBlockOne);
        this.addressBlockTwo = new SimpleStringProperty(addressBlockTwo);
        this.addressBlockThree = new SimpleStringProperty(addressBlockThree);
        this.city = new SimpleStringProperty(addressBlockThree);
        this.longitude = new SimpleDoubleProperty(longitude);
        this.latitude = new SimpleDoubleProperty(latitude);
        this.siteContactName = new SimpleStringProperty(siteContactName);
        this.siteContactPhone = new SimpleStringProperty(siteContactPhone);
        this.poNumber = new SimpleStringProperty(poNumber);
        this.locationNotes = new SimpleStringProperty("");
        this.preTripInstructions = new SimpleStringProperty("");
        this.driver = new SimpleStringProperty("Unknown");
        this.driverInitial = new SimpleStringProperty("Unknown");
        this.driverNumber = new SimpleIntegerProperty(0);
        this.serialNumber = new SimpleStringProperty(serialNumber);
        this.liftId = new SimpleIntegerProperty(0);
        this.liftType = new SimpleStringProperty(liftType);
        this.shortLiftType = new SimpleStringProperty("Unknown");
        this.status = new SimpleStringProperty(status);
        this.isInvoiceWritten = new SimpleBooleanProperty(false);
        this.rentalOrderId = new SimpleIntegerProperty(rentalOrderId);
        this.rentalItemId = new SimpleIntegerProperty(rentalItemId);
        this.singleItemOrder = new SimpleBooleanProperty(singleItemOrder);
        this.needsInvoice = new SimpleBooleanProperty(false);
        this.latestBilledDate = new SimpleStringProperty("Unknown");
        this.deliveryTruck = new SimpleStringProperty(null);
        this.pickUpTruck = new SimpleStringProperty(null);
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


    public String getOrderedByName() {
        return orderedByName.get();
    }


    public void setOrderedByName(String orderedByName) {
        this.orderedByName.set(orderedByName);
    }


    public StringProperty orderedByNameProperty() {
        return orderedByName;
    }


    public String getOrderedByPhone() {
        return orderedByPhone.get();
    }


    public void setOrderedByPhone(String orderedByPhone) {
        this.orderedByPhone.set(orderedByPhone);
    }


    public StringProperty orderedByPhoneProperty() {
        return orderedByPhone;
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


    public String getCallOffDate() { return callOffDate.get(); }


    public void setCallOffDate(String callOffDate) {this.callOffDate.set(callOffDate); }


    public StringProperty callOffDateProperty() {return callOffDate; }


    public boolean isAutoTerm() {
        return autoTerm.get();
    }


    public void setAutoTerm(boolean autoTerm) {
        this.autoTerm.set(autoTerm);
    }


    public int calculateRentalDuration() {


        // Define two date formats with correct locales
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("MMM-dd", Locale.ENGLISH); // Specify Locale


        // Handle null or empty deliveryDate
        if (deliveryDate.get() == null || deliveryDate.get().isEmpty()) {
            throw new IllegalArgumentException("Delivery date cannot be null or empty.");
        }


        String localDeliveryDate = deliveryDate.get();


        LocalDate startDate = null;


        // Mapping month abbreviation to month number
        Map<String, Integer> monthMap = new HashMap<>();
        monthMap.put("JAN", 1);
        monthMap.put("FEB", 2);
        monthMap.put("MAR", 3);
        monthMap.put("APR", 4);
        monthMap.put("MAY", 5);
        monthMap.put("JUN", 6);
        monthMap.put("JUL", 7);
        monthMap.put("AUG", 8);
        monthMap.put("SEP", 9);
        monthMap.put("OCT", 10);
        monthMap.put("NOV", 11);
        monthMap.put("DEC", 12);


        try {
            // Try parsing with the first formatter (yyyy-MM-dd)
            startDate = LocalDate.parse(localDeliveryDate, formatter1);
        } catch (DateTimeParseException e) {


            // If parsing fails, try the second formatter (MMM-dd) with an uppercase month
            try {
                // Split the delivery date by '-'
                String[] parts = localDeliveryDate.split("-");
                String month = parts[0].toUpperCase();  // Ensure month abbreviation is uppercase
                String day = parts[1];


                // Check if the month abbreviation is valid
                if (monthMap.containsKey(month)) {
                    // Construct a valid date string
                    int monthNumber = monthMap.get(month);
                    String adjustedDate = LocalDate.now().getYear() + "-" + String.format("%02d", monthNumber) + "-" + day;


                    // Try parsing with the adjusted format
                    startDate = LocalDate.parse(adjustedDate, formatter1); // Use yyyy-MM-dd formatter
                } else {
                    throw new IllegalArgumentException("Invalid month abbreviation.");
                }
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Invalid delivery date format.");
            }
        }


        // Handle null or empty callOffDate by using the current date
        LocalDate endDate = LocalDate.now(); // Default to current date
        if (callOffDate.get() != null && !callOffDate.get().isEmpty()) {
            endDate = LocalDate.parse(callOffDate.get(), formatter1);
        }


        int billableDays = 0;


        // Iterate through the date range
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {


            // Count business days (exclude weekends and company holidays)
            if (isBusinessDay(currentDate)) {
                billableDays++;
            }
            currentDate = currentDate.plusDays(1);
        }


        return billableDays > 0 ? billableDays : 1;  // Ensure at least 1 day is billed
    }




    // Helper method to check if a date is a business day
    private boolean isBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        // Exclude weekends and company holidays
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY && !Config.isHoliday(date);
    }


    public int getRentalDuration() { return rentalDuration.get(); }


    public void setRentalDuration(int duration) { this.rentalDuration.set(duration); }


    public SimpleIntegerProperty rentalDurationProperty() { return rentalDuration; }


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


    public void splitAddressBlockTwo() {
        String address = getAddressBlockTwo();  // Get the current value of addressBlockTwo


        // Check if the address contains a comma
        if (address != null && address.contains(",")) {
            // Split the address at the first comma
            String[] addressParts = address.split(",", 2); // Limit to 2 parts (before and after the first comma)


            // Set the first part in addressBlockTwo and the second part in addressBlockThree
            setAddressBlockTwo(addressParts[0].trim());  // Trim to remove any extra spaces
            setAddressBlockThree(addressParts[1].trim()); // Trim to remove any extra spaces
        }
    }


    public String getAddressBlockThree() {
        return addressBlockThree.get();
    }


    public void setAddressBlockThree(String addressBlockThree) {
        this.addressBlockThree.set(addressBlockThree);
        this.city.set(addressBlockThree);
    }


    public StringProperty addressBlockThreeProperty() {
        return addressBlockThree;
    }


    public void setCityShort() {
        String str = addressBlockThree.getValue();
        System.out.println("set city short called and addressBlockThree is: "
                + str);
        if (str == null || str.isEmpty()) {
            city.set(""); // Handle null or empty str strings
            return;
        }


        String[] words = str.trim().split("\\s+"); // Split by whitespace
        if (words.length == 1) {
            city.set(str); // If there's only one word or none, return an empty string
            return;
        }


        city.set(String.join(" ", Arrays.copyOf(words, words.length - 1))); // Join all but the last word
    }


    public String getCity() {
        return city.get();
    }


    public void setCity(String city) {
        this.city.set(city);
    }


    public StringProperty cityProperty() {
        return city;
    }


    public void setLongitude(double longitude) {
        this.longitude.set(longitude);
    }

    public double getLongitude(){
        return longitude.get();
    }

    public DoubleProperty longitude() {return longitude; }

    public void setLatitude(double latitude) {
        this.latitude.set(latitude);
    }

    public double getLatitude(){
        return latitude.get();
    }

    public DoubleProperty latitude() {return latitude;}


    public String getSiteContactName() {
        return siteContactName.get();
    }


    public void setSiteContactName(String siteContactName) {
        this.siteContactName.set(siteContactName);
    }


    public StringProperty siteContactNameProperty() {
        return siteContactName;
    }


    public String getSiteContactPhone() {
        return siteContactPhone.get();
    }


    public void setSiteContactPhone(String siteContactPhone) {
        this.siteContactPhone.set(siteContactPhone);
    }


    public StringProperty siteContactPhoneProperty() {
        return siteContactPhone;
    }


    public String getPoNumber() {
        return poNumber.get();
    }


    public void setPoNumber(String poNumber) {
        this.poNumber.set(poNumber);
    }


    public StringProperty poNumberProperty() {
        return poNumber;
    }


    public String getLocationNotes() {
        return locationNotes.get();
    }


    public void setLocationNotes(String locationNotes) {
        this.locationNotes.set(locationNotes);
    }


    public StringProperty locationNotesProperty() {
        return locationNotes;
    }


    public String getPreTripInstructions() {
        return preTripInstructions.get();
    }


    public void setPreTripInstructions(String preTripInstructions) {
        this.preTripInstructions.set(preTripInstructions);
    }


    public StringProperty preTripInstructionsProperty() {
        return preTripInstructions;
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


    public void setupDriverCompositionalParts(){
        if (driver != null) {
            String localVarDriver = getDriver();
            Pattern pattern = Pattern.compile("^([A-Za-z]{1,2})(\\d+)?$");
            Matcher matcher = pattern.matcher(localVarDriver);


            if (matcher.matches()) {
                driverInitial.set(matcher.group(1));
                driverNumber.set(matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0);
            } else {
                driverInitial.set("x");
                driverNumber.set(0);
            }
        }


    }


    public String getDriverInitial() {
        return driverInitial.get();
    }


    public void setDriverInitial(String driverInitial) {
        this.driverInitial.set(driverInitial);
    }


    public StringProperty driverInitialProperty() {
        return driverInitial;
    }


    public int getDriverNumber() {
        return driverNumber.get();
    }


    public void setDriverNumber(int driverNumber) {
        this.driverNumber.set(driverNumber);
    }


    public SimpleIntegerProperty driverNumberProperty() {
        return driverNumber;
    }


    public int getSequenceNumber() {
        String driver = getDriver();
        if (driver != null) {
            String digits = driver.replaceAll("\\D", ""); // Remove all non-digits
            if (!digits.isEmpty()) {
                return Integer.parseInt(digits); // Parse the digits as an integer
            }
        }
        return 0;
    }


    public void setSequenceNumber(int sequenceNumber){
        String driver = getDriver();
        String newDriverName;
        if (driver != null) {
            newDriverName = driver.replaceAll("\\d+","");
        } else {
            newDriverName = "";
        }


        newDriverName += sequenceNumber;
        setDriver(newDriverName.trim());
    }


    public String getSerialNumber() {
        return serialNumber.get();
    }


    public void setSerialNumber(String serialNumber) {
        this.serialNumber.set(serialNumber);
    }


    public StringProperty serialNumberProperty() {
        return serialNumber;
    }


    public void setLiftId(int liftId) {
        this.liftId.set(liftId);
    }


    public int getLiftId() {
        return liftId.get();
    }


    public SimpleIntegerProperty liftIdProperty() {
        return liftId;
    }


    public void setLiftType(String liftType) {
        this.liftType.set(liftType);
        this.shortLiftType.set(generateShortLiftType(liftType));
    }


    public StringProperty liftTypeProperty() {
        return liftType;
    }


    public String getLiftType() {
        return liftType.get();
    }


    public String getShortLiftType() {
        return shortLiftType.get();
    }


    private String generateShortLiftType(String liftType) {
        if (liftType == null || liftType.isEmpty()) {
            return "Unknown"; // Fallback if liftType is null or empty
        }


        // Check the specified lift types and return the short version
        switch (liftType) {
            case "12' Mast":
                return "12m";
            case "45' Boom":
                return "45b";
            case "33' RT":
                return "33rt";
            case "19' Slim":
                return "19s";
            case "26' Slim":
                return "26s";
            case "26'":
                return "26";
            case "32'":
                return "32";
            case "40'":
                return "40";
            default:
                return liftType; // Return the original if it doesn't match
        }
    }

    public void decapitalizeLiftType() {
        if (liftType != null && liftType.get() != null) {
            String original = liftType.get();
            StringBuilder result = new StringBuilder();
    
            for (char c : original.toCharArray()) {
                if ("BRTSM".indexOf(c) != -1) {
                    result.append(Character.toLowerCase(c));
                } else {
                    result.append(c);
                }
            }
    
            liftType.set(result.toString());
        } 
    }
    

    public StringProperty shortLiftTypeProperty() {
        return shortLiftType;
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
        return isContractWritten.get();
    }


    public void setContractWritten(boolean contractWritten) {


        this.isContractWritten.set(contractWritten);
    }


    public boolean isWritingInvoice() {
        return writingInvoice.get();
    }


    public void setWritingInvoice(boolean writingInvoice) {
        this.writingInvoice.set(writingInvoice);
    }


    public boolean isInvoiceComposed() {
        return isInvoiceWritten.get();
    }


    public void setInvoiceComposed(boolean invoiceWritten) {
        this.isInvoiceWritten.set(invoiceWritten);
    }


    public boolean getNeedsInvoice() {
        return needsInvoice.get();
    }


    public void setNeedsInvoice(boolean needsInvoice) {
        this.needsInvoice.set(needsInvoice);
    }


    public BooleanProperty needsInvoice() {
        return this.needsInvoice();
    }


    public String getLatestBilledDate() {
        return latestBilledDate.get();
    }


    public void setLatestBilledDate(String billedDate) {
        this.latestBilledDate.set(billedDate);
    }


    public StringProperty latestBilledDateProperty() {
        return latestBilledDate;
    }

    public String getDeliveryTruck() {
        return deliveryTruck.get();
    }


    public void setDeliveryTruck(String deliveryTruck) {
        this.deliveryTruck.set(deliveryTruck);
    }


    public StringProperty deliveryTruckProperty() {
        return deliveryTruck;
    }

    public String getPickUpTruck() {
        return pickUpTruck.get();
    }


    public void setPickUpTruck(String pickUpTruck) {
        this.pickUpTruck.set(pickUpTruck);
    }


    public StringProperty pickUpTruckProperty() {
        return pickUpTruck;
    }

    public boolean isSingleItemOrder() {
        return singleItemOrder.get();
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





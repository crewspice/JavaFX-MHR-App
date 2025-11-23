package com.MaxHighReach;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.MaxHighReach.ServiceController.Contact;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import okhttp3.*;

public class ExpandServiceController extends BaseController {
    
    @FXML private Rectangle dragArea;
    private Rental expandedRental;
    private Service service;
    @FXML private Label mainTitle;
    @FXML private Label rentalIdLabel;
    @FXML private ToggleGroup serviceToggleGroup;
    @FXML private AnchorPane rootPane;
    @FXML private Rectangle calendarCover;
    @FXML private Button openCalendar;
    @FXML private Label serviceDateLabel;
    @FXML private Label serviceTimeLabel;
    @FXML private Label statusLabel;
    @FXML private ToggleButton changeOutButton;
    @FXML private ToggleButton serviceChangeOutButton;
    @FXML private ToggleButton serviceButton;
    @FXML private ToggleButton moveButton;
    @FXML private Button updateButton;
    @FXML private Button backButton;
    @FXML private DatePicker datePicker;
    @FXML private TilePane weeksRowTilePane;
    @FXML private TilePane serviceTimeTilePane;
    @FXML private ComboBox<String> serviceHourComboBox;
    @FXML private ToggleButton serviceTime8To10Button;
    @FXML private ToggleButton serviceTimeASAPButton;
    @FXML private ToggleButton serviceTimeAnyButton;
    @FXML private ToggleButton serviceCustomButton;
    @FXML private Label customerNameLabel;
    @FXML private Label addressBlockOneLabel;
    @FXML private Label addressBlockTwoLabel;
    @FXML private Label addressBlockThreeLabel;
    @FXML private Label rentalInfoLabel;
    @FXML private Line dividerLine;
    @FXML private Label orderedByLabel;
    @FXML private ComboBox<Contact> orderedByBox;
    @FXML private TextField orderedByField;
    @FXML private Label orderedByPhoneLabel;
    @FXML private TextField orderedByPhoneField;
    @FXML private Label siteContactLabel;
    @FXML private ComboBox<Contact> siteContactBox;
    @FXML private TextField siteContactField;
    @FXML private Label siteContactPhoneLabel;
    @FXML private TextField siteContactPhoneField;
    @FXML private Button locationNotesButton;
    @FXML private Label locationNotesLabel;
    @FXML private TextField locationNotesField;
    @FXML private Button preTripInstructionsButton;
    @FXML private Label preTripInstructionsLabel;
    @FXML private TextField preTripInstructionsField;
    @FXML private CheckBox chargeDeliveryTripCheckBox;
    @FXML private Label chargeDeliveryTripLabel;
    @FXML private Label reasonLabel;
    @FXML private TextField reasonField;
    @FXML private Label newAddressLabel;
    @FXML private TextField newAddressField;
    @FXML private ComboBox<String> addressSuggestionsBox;
    @FXML private Label newSiteLabel;
    @FXML private TextField newSiteField;
    @FXML private CheckBox sameSiteBox;
    @FXML private Label sameSiteLabel;
    @FXML private Label newLiftTypeLabel;
    @FXML private TilePane liftTypeTilePane;
    @FXML private ToggleButton twelveMastButton;

    private int originalScissorHeight = 545;
    private String serviceType = null;
    private ObservableList<Contact> orderingContacts = FXCollections.observableArrayList();
    private ObservableList<Contact> siteContacts = FXCollections.observableArrayList();
    private ToggleGroup weeksToggleGroup;
    private ToggleGroup serviceTimeToggleGroup;
    private ToggleGroup liftTypeToggleGroup;
    private ObservableList<String> addressSuggestions = FXCollections.observableArrayList();
	private OkHttpClient client = new OkHttpClient();
	private int addressTypeCounter = 0;

    public void initialize() {

        expandedRental = MaxReachPro.getRentalForExpanding();
        System.out.println("lift type upon expanding: " + expandedRental.getLiftType());
        try {

            serviceToggleGroup = new ToggleGroup();
            changeOutButton.setToggleGroup(serviceToggleGroup);
            serviceChangeOutButton.setToggleGroup(serviceToggleGroup);
            serviceButton.setToggleGroup(serviceToggleGroup);
            moveButton.setToggleGroup(serviceToggleGroup);
            serviceToggleGroup.selectToggle(null);

            setTooltipBelow(changeOutButton, "Change Out");
            setTooltipBelow(serviceChangeOutButton, "Service Change Out");
            setTooltipBelow(serviceButton, "Service");
            setTooltipBelow(moveButton, "Move");

            customerNameLabel.setText(expandedRental.getName());
        
            // Address formatting
            addressBlockOneLabel.setText(expandedRental.getAddressBlockOne());
            addressBlockTwoLabel.setText(
                expandedRental.getAddressBlockTwo() + ", " + expandedRental.getAddressBlockThree()
            );
        
            // Rental info: prefix P + mapped lift type name
            String rentalId = "P" + expandedRental.getRentalItemId();
            String liftTypeLong = Config.LIFT_BUTTON_TEXT_MAP.getOrDefault(expandedRental.getLiftType(),
                                                                        expandedRental.getLiftType());
            rentalInfoLabel.setText(rentalId + "  \u25C6  " + liftTypeLong);
        
            Color contentColor = Color.web(Config.getTertiaryColor());
            dividerLine.setStroke(contentColor);
            Color secondaryColor = Color.web(Config.getSecondaryColor());
        
            // Recolor images
            recolorButtonImage(changeOutButton, contentColor, secondaryColor);
            recolorButtonImage(serviceChangeOutButton, contentColor, secondaryColor);
            recolorButtonImage(serviceButton, contentColor, secondaryColor);
            recolorButtonImage(moveButton, contentColor, secondaryColor);
            
            serviceToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
                // Clear previous styles
                clearButtonSelectionStyles();
            
                // User selected a new toggle
                if (newToggle instanceof ToggleButton selectedButton) {
                    selectedButton.getStyleClass().add("service-type-button-selected");
                }
            });

            dividerLine.setStroke(contentColor);
            weeksToggleGroup = new ToggleGroup();
            for (javafx.scene.Node node : weeksRowTilePane.getChildren()) {
                if (node instanceof ToggleButton) {
                    ToggleButton toggleButton = (ToggleButton) node;
                    toggleButton.setToggleGroup(weeksToggleGroup); // Add each ToggleButton to the ToggleGroup
    
                    // Add action for each button
                    toggleButton.setOnAction(event -> {
                        weeksToggleGroup.selectToggle(toggleButton); // Ensure the selected button is set
                    });
                }
            }
    
            updateWeekdayToggleButtons(null);
    
            serviceTimeToggleGroup = new ToggleGroup();
            for (javafx.scene.Node node : serviceTimeTilePane.getChildren()) {
                if (node instanceof ToggleButton) {
                    ToggleButton toggleButton = (ToggleButton) node;
                    toggleButton.setToggleGroup(serviceTimeToggleGroup);  // Add each ToggleButton to the ToggleGroup
    
                    // Hide the custom ComboBoxes when a delivery time button is clicked
                    toggleButton.setOnAction(event -> {
                        if (toggleButton != serviceCustomButton) {
                            serviceHourComboBox.setVisible(false);
    //                      ampmComboBox.setVisible(false);
                            serviceCustomButton.setSelected(false); // Unselect custom button
                        }
                    });
                }
            }
    
            serviceTimeToggleGroup.selectToggle(serviceTime8To10Button);
    
            serviceHourComboBox.getItems().addAll("6", "7", "8", "9", "10", "11", "12", "1", "2", "3", "4");
    
            serviceCustomButton.setOnAction(event -> {
                boolean isSelected = serviceCustomButton.isSelected();
                serviceHourComboBox.setVisible(isSelected);
    //          ampmComboBox.setVisible(isSelected);
                if (!isSelected) {
                    serviceHourComboBox.getSelectionModel().clearSelection();
    //              ampmComboBox.getSelectionModel().clearSelection();
                }
            });
    
   
            orderedByBox.setPrefWidth(1);
            orderedByBox.setMinWidth(1);
            orderedByBox.setMaxWidth(1);
            siteContactBox.setPrefHeight(1);
            siteContactBox.setMinWidth(1);
            siteContactBox.setMaxWidth(1);
            orderedByBox.setOnAction(event -> handleContactSelection(orderedByBox, true));
            siteContactBox.setOnAction(event -> handleContactSelection(siteContactBox, false));

            populateComboBoxesForCustomer();
            prefillSiteContactByPhone();
    
            setTooltipBelow(locationNotesButton, "Location Notes");
            setTooltipBelow(preTripInstructionsButton, "Pre-trip Instructions");
    
            setupTextFieldListeners(locationNotesField, locationNotesButton, locationNotesLabel);
            setupTextFieldListeners(preTripInstructionsField, preTripInstructionsButton, preTripInstructionsLabel);
    
    
            addressSuggestionsBox.setPrefWidth(1);
            addressSuggestionsBox.setMinWidth(1);
            addressSuggestionsBox.setMaxWidth(1);
            addressSuggestionsBox.setVisibleRowCount(5);
    
            // Listener for addressField text changes
            newAddressField.textProperty().addListener((observable, oldValue, newValue) -> {
                // Check if the last action was a backspace
                boolean isBackspace = false;
    
    
                // If the text length has decreased, we assume a backspace occurred
                if (newValue.length() < oldValue.length()) {
                    isBackspace = true;
                }
    
    
                // Update suggestions based on input, unless it was a backspace
                if (!isBackspace) {
                    updateSuggestions(newValue);
                    addressTypeCounter++; // Only increment if it wasn't a backspace
                }
    
    
                // Check if there's text in the field and update the style accordingly
                if (newValue.isEmpty()) {
                    newAddressField.getStyleClass().remove("has-text");
                    newAddressField.getStyleClass().add("empty-unfocused");
                    //   suggestionsBox.setVisible(false); // Hide suggestions if empty
                } else {
                    newAddressField.getStyleClass().remove("empty-unfocused");
                    if (!newAddressField.getStyleClass().contains("has-text")) {
                        newAddressField.getStyleClass().add("has-text");
                    }
                }
            });
    
            // Handle focus events for newAddressField
            newAddressField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            //   suggestionsBox.setVisible(false);
                if (isNowFocused) {
                    newAddressField.getStyleClass().remove("empty-unfocused"); // Remove empty style when focused
                    //System.out.println("");
                } else if (newAddressField.getText().isEmpty()) {
                    newAddressField.getStyleClass().add("empty-unfocused"); // Add empty style when focus is lost
            //      suggestionsBox.setVisible(false); // Hide suggestions when focus is lost and input is empty
                }
            });
    
            // Listener for the ComboBox to handle selection
            addressSuggestionsBox.setOnAction(e -> {
                System.out.println("Action event on suggestionsBox");
                String selectedSuggestion = addressSuggestionsBox.getValue();
                if (selectedSuggestion != null) {
                    System.out.println("Selected suggestion: " + selectedSuggestion);
                    String formattedAddress = formatSelectedSuggestion(selectedSuggestion);
                    newAddressField.setText(formattedAddress); // Set selected suggestion in the TextField
                    addressSuggestionsBox.getSelectionModel().clearSelection(); // Clear selection to reset ComboBox
                //   suggestionsBox.setVisible(false);
                }
    
            });
    
            // Optional: Set up an additional listener if you want to catch selection via valueProperty
            addressSuggestionsBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    newAddressField.setText(newVal); // Populate newAddressField with selected suggestion
                //   suggestionsBox.setVisible(false);
                }
            });
    
            // Handle Enter key press in newAddressField to confirm selection
            newAddressField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    if (addressSuggestionsBox.isVisible() && addressSuggestionsBox.getValue() != null) {
                        newAddressField.setText(addressSuggestionsBox.getValue()); // Set selected suggestion
                //     suggestionsBox.setVisible(false);
                    } else {
                        newAddressField.getParent().requestFocus(); // Move focus if no selection
                    }
                }
            });
    
            liftTypeToggleGroup = new ToggleGroup();
            for (javafx.scene.Node node : liftTypeTilePane.getChildren()) {
                if (node instanceof ToggleButton) {
                    ToggleButton toggleButton = (ToggleButton) node;
                    toggleButton.setToggleGroup(liftTypeToggleGroup); // Add each ToggleButton to the ToggleGroup
    
                    // Add action for each button
                    toggleButton.setOnAction(event -> {
                        liftTypeToggleGroup.selectToggle(toggleButton); // Ensure the selected button is set
                    });
                }
            }
    
            sameSiteBox.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                if (isNowSelected) {
                    // Fill fields from expandedRental
                    newSiteField.setText(expandedRental.getAddressBlockOne());
                    newAddressField.setText(expandedRental.getAddressBlockTwo());
        
                    // Disable fields to prevent editing and remove cursor
                    newSiteField.setDisable(true);
                    newAddressField.setDisable(true);
        
                    // Optionally disable address suggestions ComboBox too
                    addressSuggestionsBox.setDisable(true);
                } else {
                    // Clear fields
                    newSiteField.clear();
                    newAddressField.clear();
        
                    // Re-enable fields and ComboBox
                    newSiteField.setDisable(false);
                    newAddressField.setDisable(false);
                    addressSuggestionsBox.setDisable(false);
                }
            });
            System.out.println("Initilizng expandController");
            super.initialize(dragArea);

            service = expandedRental.getService();
            if (service != null) {
                int serviceId = service.getServiceId();

                // Append to existing label text
                String oldText = mainTitle.getText();
                mainTitle.setText(oldText + serviceId); // collate onto existing text

                // Print debug
                System.out.println("✅ Successfully derived expandedRental().getService(): Service ID = " + serviceId);
            } else {
                System.out.println(" expandedRental.getService() returned null");
            }

            serviceType = service.getServiceType();
            System.out.println("Service type from expandedRental: " + serviceType);
            
            // Map serviceType to the correct ToggleButton
            ToggleButton buttonToSelect = null;
            
            switch (serviceType) {
                case "Change Out" -> buttonToSelect = changeOutButton;
                case "Service Change Out" -> buttonToSelect = serviceChangeOutButton;
                case "Service" -> buttonToSelect = serviceButton;
                case "Move" -> buttonToSelect = moveButton;
                default -> System.out.println("Unknown service type: " + serviceType);
            }
            
            if (buttonToSelect != null) {
                serviceToggleGroup.selectToggle(buttonToSelect);
                System.out.println("✅ Selected button for service type: " + serviceType);
            } else {
                System.out.println("No button matched service type: " + serviceType);
            }
            
            chargeDeliveryTripCheckBox.setSelected(service.isBillable());
            updateConditionalElements();
            
            
            int nthService = getServiceNumber(service.getServiceId());
            String ordinal = getOrdinal(nthService);
        
            // Use Unicode for superscript: 1ˢᵗ, 2ⁿᵈ, 3ʳᵈ, 4ᵗʰ ...
            String labelText = nthService + ordinal + " service for this contract    <- -> [buttons]";
            rentalIdLabel.setText(labelText);


            // LOCATION NOTES
            String ln = service.getLocationNotes();
            if (ln != null && !ln.equals("null") && !ln.isEmpty()) {
                System.out.println("✓ Setting locationNotesField: " + ln);
                locationNotesField.setText(ln);
                locationNotesButton.getStyleClass().add("schedule-delivery-button-has-value");
            } else {
                System.out.println("✗ locationNotes is null/empty");
            }

            // PRE-TRIP INSTRUCTIONS
            String pt = service.getPreTripInstructions();
            if (pt != null && !pt.equals("null") && !pt.isEmpty()) {
                System.out.println("✓ Setting preTripInstructionsField: " + pt);
                preTripInstructionsField.setText(pt);
                preTripInstructionsButton.getStyleClass().add("schedule-delivery-button-has-value");
            } else {
                System.out.println("✗ preTripInstructions is null/empty");
            }

            // REASON
            String reason = service.getReason();
            if (reason != null && !reason.equals("null") && !reason.isEmpty()) {
                System.out.println("✓ Setting reasonField: " + reason);
                reasonField.setText(reason);
            } else {
                System.out.println("✗ reason is null/empty");
            }

            // NEW STREET ADDRESS
            String newStreet = service.getNewStreetAddress();
            if (newStreet != null && !newStreet.equals("null") && !newStreet.isEmpty()) {
                System.out.println("✓ Setting newAddressField: " + newStreet);
                newAddressField.setText(newStreet);
            } else {
                System.out.println("✗ newStreetAddress is null/empty");
            }

            // NEW SITE NAME
            String newSite = service.getNewSiteName();  // Assuming getter is getNewSiteName()
            if (newSite != null && !newSite.equals("null") && !newSite.isEmpty()) {
                System.out.println("✓ Setting newSiteField: " + newSite);
                newSiteField.setText(newSite);
            } else {
                System.out.println("✗ newSiteName is null/empty");
            }

            if (service.getLocationNotes() == null) {
                sameSiteBox.setSelected(true);
            }

            String newLiftType = service.getNewLiftType();
            System.out.println("newLiftType is " + newLiftType);
            
            // Guard 1 — newLiftType missing
            if (newLiftType == null || newLiftType.equals("null") || newLiftType.isBlank()) {
                System.out.println("newLiftType is null/missing, skipping lift toggle selection");
            } else {
            
                // Try mapping it
                String mappedText = Config.LIFT_BUTTON_TEXT_MAP.get(newLiftType);
            
                if (mappedText == null) {
                    System.out.println("⚠ No mapping exists for newLiftType: " + newLiftType);
                } else {
                    System.out.println("Mapped to button text: " + mappedText);
            
                    // Find matching toggle button
                    for (Toggle toggle : liftTypeToggleGroup.getToggles()) {
            
                        if (toggle instanceof ToggleButton tb) {
                            System.out.println("Checking button: " + tb.getText());
            
                            if (tb.getText().equals(mappedText)) {
                                tb.setSelected(true);
                                System.out.println("✔ Selected toggle: " + tb.getText());
                                break; // do NOT return — just end the loop
                            }
                        }
                    }
                }
            }
            
            // 🚨 initialize() continues normally after this point, nothing is skipped
            System.out.println("Continuing with remaining initialize() code...");
            
            Platform.runLater(() -> getAllLabels().forEach(label -> label.setTextFill(contentColor)));



        } catch (Exception e) {
            System.err.println("Exception during ExpandServiceController.initialize():");
            e.printStackTrace();
        }    
        
    }

    // Method to update weekday toggle buttons for a specific TilePane and date
    private void updateWeekdayToggleButtons(LocalDate date) {
        if (date == null) {
            date = getDefaultServiceDate();
        }

        // Determine the start of the week (Monday)
        LocalDate startOfWeek = date.with(java.time.temporal.ChronoField.DAY_OF_WEEK, 1);

        ToggleButton monButton = (ToggleButton) weeksRowTilePane.lookup("#monButton");
        ToggleButton tueButton = (ToggleButton) weeksRowTilePane.lookup("#tueButton");
        ToggleButton wedButton = (ToggleButton) weeksRowTilePane.lookup("#wedButton");
        ToggleButton thuButton = (ToggleButton) weeksRowTilePane.lookup("#thuButton");
        ToggleButton friButton = (ToggleButton) weeksRowTilePane.lookup("#friButton");

        // Array of buttons for easier iteration
        ToggleButton[] buttons = {monButton, tueButton, wedButton, thuButton, friButton};

        // Update buttons with corresponding dates
        for (int i = 0; i < buttons.length; i++) {
            LocalDate buttonDate = startOfWeek.plusDays(i); // Calculate the date for this button


            // Set button text to date in M/d format
            if (buttons[i] != null) {
                    buttons[i].setText(buttonDate.format(DateTimeFormatter.ofPattern("M/d")));

                // Highlight the button if it matches the provided date

                    buttons[i].getStyleClass().remove("lift-type-button-stopped");
                    buttons[i].getStyleClass().remove("lift-type-button-stopped");
                    buttons[i].getStyleClass().add("lift-type-button-dormant");

                    buttons[i].getStyleClass().remove("lift-type-button-dormant");
                    buttons[i].getStyleClass().remove("lift-type-button-dormant");
                    buttons[i].getStyleClass().add("lift-type-button-stopped");

                    buttons[i].setSelected(buttonDate.equals(date));
                }

        }
    }
    
    private List<Label> getAllLabels() {
        List<Label> labels = new ArrayList<>();
        collectLabelsRecursive(rootPane, labels); // start from this controller's root only
        return labels;
    }
    

    private void collectLabelsRecursive(Parent parent, List<Label> labels) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Label label) {
                labels.add(label);
            } else if (node instanceof Parent p) {
                collectLabelsRecursive(p, labels);
            }
        }
    }
    

    @FXML
    private void handleChangeOut() {
        if ("Change Out".equals(serviceType)) {
            serviceType = null;
            MaxReachPro.getScissorLift().animateTransition(originalScissorHeight);
            setUniversalElementsVisibility(false);
        } else {
            serviceType = "Change Out";
            MaxReachPro.getScissorLift().animateTransition(225);
            setUniversalElementsVisibility(true);
        }
        updateConditionalElements();
    }
    
    @FXML
    private void handleServiceChangeOut() {
        if ("Service Change Out".equals(serviceType)) {
            serviceType = null;
            MaxReachPro.getScissorLift().animateTransition(originalScissorHeight);
            setUniversalElementsVisibility(false);
        } else {
            serviceType = "Service Change Out";
            MaxReachPro.getScissorLift().animateTransition(273);
            setUniversalElementsVisibility(true);
        }
        updateConditionalElements();
    }
    
    @FXML
    private void handleService() {
        if ("Service".equals(serviceType)) {
            serviceType = null;
            MaxReachPro.getScissorLift().animateTransition(originalScissorHeight);
            setUniversalElementsVisibility(false);
        } else {
            serviceType = "Service";
            MaxReachPro.getScissorLift().animateTransition(273);
            setUniversalElementsVisibility(true);
        }
        updateConditionalElements();
    }
    
    @FXML
    private void handleMove() {
        if ("Move".equals(serviceType)) {
            serviceType = null;
            MaxReachPro.getScissorLift().animateTransition(originalScissorHeight);
            setUniversalElementsVisibility(false);
        } else {
            serviceType = "Move";
            MaxReachPro.getScissorLift().animateTransition(240);
            setUniversalElementsVisibility(true);
        }
        updateConditionalElements();
    }


    
    private void setUniversalElementsVisibility(boolean visible) {
        // Date elements
        serviceDateLabel.setVisible(visible);
        datePicker.setVisible(visible);
        calendarCover.setVisible(visible);
        openCalendar.setVisible(visible);
        weeksRowTilePane.setVisible(visible);
    
        // ToggleButtons inside the weeks TilePane
        weeksRowTilePane.getChildren().forEach(node -> node.setVisible(visible));
    
        // Time elements
        serviceTimeLabel.setVisible(visible);
        serviceTimeTilePane.setVisible(visible);
    
        // Ensure serviceHourComboBox is only set to false
        if (!visible) {
            serviceHourComboBox.setVisible(false);
        }
    
        // ToggleButtons inside the time TilePane
        serviceTimeTilePane.getChildren().forEach(node -> node.setVisible(visible));
    
        // Ordered By elements
        orderedByLabel.setVisible(visible);
        orderedByBox.setVisible(visible);
        orderedByField.setVisible(visible);            // text field only
        orderedByPhoneLabel.setVisible(visible);
        orderedByPhoneField.setVisible(visible);       // text field only
    
        // Site Contact elements
        siteContactLabel.setVisible(visible);
        siteContactBox.setVisible(visible);
        siteContactField.setVisible(visible);          // text field only
        siteContactPhoneLabel.setVisible(visible);
        siteContactPhoneField.setVisible(visible);     // text field only
    
        // Location Notes elements
        locationNotesButton.setVisible(visible);
        if (!visible) {
            locationNotesLabel.setVisible(false);
            locationNotesField.setVisible(false);
        }   // text field only
    
        // Pre-trip Instructions elements
        preTripInstructionsButton.setVisible(visible);
        if (!visible) {
            preTripInstructionsLabel.setVisible(false);
            preTripInstructionsField.setVisible(false);
        }  // text field only
    
        // Charge Delivery Trip elements
        chargeDeliveryTripCheckBox.setVisible(visible);
        chargeDeliveryTripLabel.setVisible(visible);

        updateButton.setVisible(visible);

        statusLabel.setVisible(false);
    }

    private void updateConditionalElements() {
        // Hide all conditional elements by default
        reasonLabel.setVisible(false);
        reasonField.setVisible(false);
        newSiteLabel.setVisible(false);
        newSiteField.setVisible(false);
        newAddressLabel.setVisible(false);
        newAddressField.setVisible(false);
        addressSuggestionsBox.setVisible(false);
        newLiftTypeLabel.setVisible(false);
        liftTypeTilePane.setVisible(false);
        sameSiteBox.setVisible(false);
        sameSiteLabel.setVisible(false);
    
        if (serviceType == null) {
            chargeDeliveryTripCheckBox.setSelected(false);
            return;
        }
    
        double targetY;
    
        switch (serviceType) {
            case "Change Out":
                chargeDeliveryTripCheckBox.setSelected(true);
                reasonLabel.setVisible(true);
                reasonField.setVisible(true);
                newLiftTypeLabel.setVisible(true);
                liftTypeTilePane.setVisible(true);
                orderedByLabel.setText("Ordered By:");
                targetY = 505;
                break;
    
            case "Service Change Out":
            case "Service":
                chargeDeliveryTripCheckBox.setSelected(false);
                reasonLabel.setVisible(true);
                reasonField.setVisible(true);
                orderedByLabel.setText("Reported By:");
                targetY = 457;
                break;
    
            case "Move":
                chargeDeliveryTripCheckBox.setSelected(true);
                newSiteLabel.setVisible(true);
                newSiteField.setVisible(true);
                newAddressLabel.setVisible(true);
                newAddressField.setVisible(true);
                sameSiteBox.setVisible(true);
                sameSiteLabel.setVisible(true);
                orderedByLabel.setText("Ordered By:");
                targetY = 487;
                break;
    
            default:
                chargeDeliveryTripCheckBox.setSelected(false);
                targetY = updateButton.getLayoutY(); // Stay in place
                break;
        }
    
        animateUpdateButton(targetY);
    }
    
    private void animateUpdateButton(double targetY) {
        double currentY = updateButton.getLayoutY();
    
        if (Math.abs(currentY - targetY) < 0.5) {
            // Already at correct position
            return;
        }
    
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(updateButton.layoutYProperty(), currentY),
                new KeyValue(statusLabel.layoutYProperty(), currentY + 20)
            ),
            new KeyFrame(Duration.millis(1000), 
                new KeyValue(updateButton.layoutYProperty(), targetY, Interpolator.EASE_BOTH),
                new KeyValue(statusLabel.layoutYProperty(), targetY + 20, Interpolator.EASE_BOTH)
            )
        );
        timeline.play();
    }
    

    @FXML
    private void handleUpdateService(ActionEvent event) {
    
        // ========== 1. SERVICE TYPE CHECK ==========
        ToggleButton selectedService =
                (ToggleButton) serviceToggleGroup.getSelectedToggle();
    
        boolean serviceTypeMatches =
                (selectedService == changeOutButton && "Change Out".equals(service.getServiceType())) ||
                (selectedService == serviceChangeOutButton && "Service Change Out".equals(service.getServiceType())) ||
                (selectedService == serviceButton && "Service".equals(service.getServiceType())) ||
                (selectedService == moveButton && "Move".equals(service.getServiceType()));
    
    
        // ========== 2. REASON + LOCATION NOTES ==========
        boolean reasonMatches =
                Objects.equals(service.getReason(), reasonField.getText());
    
        boolean locationNotesMatch =
                Objects.equals(service.getLocationNotes(), locationNotesField.getText());
    
        boolean preTripInstructionsMatch =
                Objects.equals(service.getPreTripInstructions(), preTripInstructionsField.getText());
            
    
        // ===== 3. DATE MATCH =====
        String selectedDateStr = getDateForDB(weeksToggleGroup);
        LocalDate selectedDate = null; // <-- declare here
        boolean datesMatch = false;

        if (selectedDateStr != null) {
            LocalDate objectDate = LocalDate.parse(expandedRental.getDeliveryDate());
            selectedDate = LocalDate.parse(selectedDateStr); // now assigned
            datesMatch = objectDate.equals(selectedDate);
            System.out.println("object service date: " + objectDate);
            System.out.println("selected date: " + selectedDate);
        } else {
            System.out.println("No valid date selected.");
        }


        // ========== 4. TIME MATCH ==========
        ToggleButton selectedTimeToggle = (ToggleButton) serviceTimeToggleGroup.getSelectedToggle();
        String selectedTimeText;
    
        if (selectedTimeToggle == serviceCustomButton) {
            // Use ComboBox if custom selected
            selectedTimeText = serviceHourComboBox.getValue();
            System.out.println("Custom selected time from ComboBox: " + selectedTimeText);
        } else {
            selectedTimeText = selectedTimeToggle.getText();
            System.out.println("Selected time from toggle: " + selectedTimeText);
        }
    
        boolean timeMatches = Objects.equals(expandedRental.getDeliveryTime(), selectedTimeText);
        System.out.println("Time matches? " + timeMatches);
    
    
        // ========== 5. CONTACT NAME MATCH ==========
        boolean orderedByMatches = Objects.equals(expandedRental.getOrderedByName(), orderedByField.getText());
        boolean siteContactMatches = Objects.equals(expandedRental.getSiteContactName(), siteContactField.getText());
    
        System.out.println("OrderedBy matches? " + orderedByMatches);
        System.out.println("SiteContact matches? " + siteContactMatches);
    
    
        // ========== 6. BILLABLE MATCH (CHARGE TRIP) ==========
        boolean chargeTripSelected = chargeDeliveryTripCheckBox.isSelected();
        boolean billableMatches = (service.isBillable() == chargeTripSelected);
    
        System.out.println("Billable matches? " + billableMatches);
    
    
        // ========== FINAL BOOLEAN ==========
        boolean editServiceVars =
                serviceTypeMatches &&
                reasonMatches &&
                locationNotesMatch &&
                preTripInstructionsMatch &&
                datesMatch &&
                timeMatches &&
                orderedByMatches &&
                siteContactMatches &&
                billableMatches;
    
        System.out.println("FINAL editServiceVars = " + editServiceVars);
    

        if (!editServiceVars) {
            System.out.println("⚠ Changes detected — updating Service row in SQL...");
        
            String newServiceType = selectedService.getText();
            String newServiceDate = selectedDate.toString(); // YYYY-MM-DD
            String newServiceTime = selectedTimeText;
        
            Contact orderedContact = orderedByBox.getSelectionModel().getSelectedItem();
            Long newOrderedContactId = orderedContact != null ? orderedContact.contactId : null;
        
            Contact siteContact = siteContactBox.getSelectionModel().getSelectedItem();
            Long newSiteContactId = siteContact != null ? siteContact.contactId : null;
        
            String newReason = reasonField.getText();
            String newLocationNotes = locationNotesField.getText();
            String newPreTrip = preTripInstructionsField.getText();
        
            int newBillable = chargeDeliveryTripCheckBox.isSelected() ? 1 : 0;
        
            int serviceId = service.getServiceId();
        
            String sql = """
                UPDATE services
                SET
                    service_type = ?,
                    service_date = ?,
                    service_time = ?,
                    ordered_contact_id = ?,
                    site_contact_id = ?,
                    reason = ?,
                    location_notes = ?,
                    pre_trip_instructions = ?,
                    billable = ?
                WHERE service_id = ?
                """;
        
            try (Connection conn = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
                pstmt.setString(1, newServiceType);
                pstmt.setString(2, newServiceDate);
                pstmt.setString(3, newServiceTime);
        
                if (newOrderedContactId == null) pstmt.setNull(4, java.sql.Types.BIGINT);
                else pstmt.setLong(4, newOrderedContactId);
        
                if (newSiteContactId == null) pstmt.setNull(5, java.sql.Types.BIGINT);
                else pstmt.setLong(5, newSiteContactId);
        
                pstmt.setString(6, newReason);
                pstmt.setString(7, newLocationNotes);
                pstmt.setString(8, newPreTrip);
                pstmt.setInt(9, newBillable);
                pstmt.setInt(10, serviceId);
        
                int rows = pstmt.executeUpdate();
                System.out.println("✔ SQL Update Successful — rows updated: " + rows);
        
                // ----- Update statusLabel -----
                statusLabel.setText("Service update successful!");
                statusLabel.setStyle("-fx-text-fill: green;");
                statusLabel.setVisible(true);
        
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("❌ SQL Update Failed.");
        
                statusLabel.setText("Service update failed: " + e.getMessage());
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setVisible(true);
            }
        } else {
            // No changes detected
            statusLabel.setText("No changes to update.");
            statusLabel.setStyle("-fx-text-fill: gray;");
            statusLabel.setVisible(true);
        }
        

        boolean newRentalOrder;

        
    }
    

    @FXML
    private void handleOpenCalendar(ActionEvent event) { /* ... */ }

    @FXML
    private void handleLocationNotes(ActionEvent event) { 
        toggleDedicatedField(locationNotesButton, locationNotesLabel, locationNotesField);
    }

    @FXML
    private void handlePreTripInstructions(ActionEvent event) { 
        toggleDedicatedField(preTripInstructionsButton, preTripInstructionsLabel, preTripInstructionsField);
     }

    
    private LocalDate getDefaultServiceDate() {
        // Use Mountain Time
        ZoneId mountainZone = ZoneId.of("America/Denver");
        ZonedDateTime nowMT = ZonedDateTime.now(mountainZone);

        LocalDate today = nowMT.toLocalDate();
        LocalTime currentTime = nowMT.toLocalTime();

        LocalDate defaultDate;

        if (currentTime.isAfter(LocalTime.of(14, 0))) {
            // After 14:00: pick next business day
            defaultDate = getNextBusinessDay(today);
        } else {
            // Before 14:00: today if business day, else next business day
            if (isBusinessDay(today)) {
                defaultDate = today;
            } else {
                defaultDate = getNextBusinessDay(today);
            }
        }

        return defaultDate;
    }
        
    private void setTooltipBelow(Node node, String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(javafx.util.Duration.ZERO);
    
        // Show tooltip below the node
        node.setOnMouseEntered(event -> {
            try {
                double x = node.localToScreen(node.getBoundsInLocal()).getMinX();
                double y = node.localToScreen(node.getBoundsInLocal()).getMaxY(); // bottom of node
                tooltip.show(node, x, y);
            } catch (Exception e) {
                System.err.println("Error showing tooltip: " + e.getMessage());
            }
        });
    
        node.setOnMouseExited(event -> tooltip.hide());
    }

        
    private void setupTextFieldListeners(TextField textField, Button button, Label label) {
        textField.setOnAction(e -> toggleDedicatedField(button, label, textField));
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                toggleDedicatedField(button, label, textField);
            }
        });
    }

    
    private void toggleDedicatedField(Button button, Label label, TextField textField) {
        boolean isDedicatedFieldVisible = textField.isVisible();
    
        // Hide other dedicated field buttons while this one is visible
        locationNotesButton.setVisible(isDedicatedFieldVisible && button != locationNotesButton);
        preTripInstructionsButton.setVisible(isDedicatedFieldVisible && button != preTripInstructionsButton);
    
        // Toggle the label and text field visibility
        label.setVisible(!isDedicatedFieldVisible);
        textField.setVisible(!isDedicatedFieldVisible);
    
        // Hide the charge-for-delivery-trip elements when a dedicated text field is visible
        chargeDeliveryTripCheckBox.setVisible(isDedicatedFieldVisible);
        chargeDeliveryTripLabel.setVisible(isDedicatedFieldVisible);
    
        if (!isDedicatedFieldVisible) {
            // Text field is being shown
            textField.requestFocus();
            if (!textField.getText().isEmpty()) {
                textField.positionCaret(textField.getText().length());
            }
        } else {
            // Text field is being hidden
            if (!textField.getText().isEmpty()) {
                button.getStyleClass().add("schedule-delivery-button-has-value");
            } else {
                button.getStyleClass().remove("schedule-delivery-button-has-value");
            }
            preTripInstructionsButton.setVisible(true);
            locationNotesButton.setVisible(true);
    
            // Restore charge delivery trip elements
            chargeDeliveryTripCheckBox.setVisible(true);
            chargeDeliveryTripLabel.setVisible(true);
        }
    }
    
    
    private void handleContactSelection(ComboBox<Contact> comboBox, boolean isOrdering) {
        Contact selected = comboBox.getValue();
        if (selected == null) return;
    
        if (isOrdering) {
            orderedByField.setText(selected.name);
            orderedByPhoneField.setText(selected.phone);
        } else {
            siteContactField.setText(selected.name);
            siteContactPhoneField.setText(selected.phone);
        }
    }

    private void populateComboBoxesForCustomer() {
        orderingContacts.clear();
        siteContacts.clear();
    
        String customerName = expandedRental.getName();
        String customerId = null;  // customer_id is String
    
        String customerQuery = "SELECT customer_id FROM customers WHERE customer_name = ?";
        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             PreparedStatement customerStmt = connection.prepareStatement(customerQuery)) {
    
            customerStmt.setString(1, customerName);
            try (ResultSet rs = customerStmt.executeQuery()) {
                if (rs.next()) {
                    customerId = rs.getString("customer_id"); // String
                } else {
                    System.err.println("No customer found with name: " + customerName);
                    return;
                }
            }
    
            String contactsQuery = "SELECT contact_id, first_name, phone_number, is_ordering_contact, is_site_contact " +
                                   "FROM contacts WHERE customer_id = ?";
            try (PreparedStatement contactStmt = connection.prepareStatement(contactsQuery)) {
                contactStmt.setString(1, customerId);
    
                try (ResultSet rs = contactStmt.executeQuery()) {
                    while (rs.next()) {
                        Long contactId = rs.getLong("contact_id"); // Long
                        String name = rs.getString("first_name");
                        String phone = rs.getString("phone_number");
                        boolean isOrdering = rs.getBoolean("is_ordering_contact");
                        boolean isSite = rs.getBoolean("is_site_contact");
    
                        Contact contact = new Contact(contactId, name, phone);
    
                        if (isOrdering) orderingContacts.add(contact);
                        if (isSite) siteContacts.add(contact);
                    }
                }
            }
    
            orderedByBox.setItems(orderingContacts);
            siteContactBox.setItems(siteContacts);
    
            orderedByBox.setOnAction(event -> handleContactSelection(orderedByBox, true));
            siteContactBox.setOnAction(event -> handleContactSelection(siteContactBox, false));
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    

    private void prefillSiteContactByPhone() {
        String phone = expandedRental.getSiteContactPhone();
        if (phone == null || phone.isEmpty()) return;
    
        // Find contact by phone number
        Contact match = siteContacts.stream()
                .filter(c -> phone.equals(c.phone))
                .findFirst()
                .orElse(null);
    
        if (match != null) {
            siteContactField.setText(match.name);
            siteContactPhoneField.setText(match.phone);
            siteContactBox.getSelectionModel().select(match);
        }
    }

    
	private void updateSuggestions(String input) {
    	addressSuggestions.clear(); // Clear previous suggestions


    	// Only make the request if the input is sufficiently long
    	if (input.length() < 3) {
        	addressSuggestionsBox.getItems().setAll(addressSuggestions);
        	//	suggestionsBox.setVisible(false);
        	return;
    	}


    	String apiKey = Config.GOOGLE_KEY; // Replace with your actual Google Places API key
    	double latitude = 39.7392;  // Example: Denver, CO latitude
    	double longitude = -104.9903;  // Example: Denver, CO longitude
    	int radius = 50000;  // Bias within 50 km radius


    	String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=" +
            	URLEncoder.encode(input, StandardCharsets.UTF_8) +
            	"&key=" + apiKey +
            	"&location=" + latitude + "," + longitude +
            	"&radius=" + radius;




    	Request request = new Request.Builder().url(url).build();


    	client.newCall(request).enqueue(new Callback() {
        	@Override
        	public void onFailure(Call call, IOException e) {
            	e.printStackTrace();
            	//	Platform.runLater(() -> suggestionsBox.setVisible(false)); // Hide suggestions on failure
        	}


        	@Override
        	public void onResponse(Call call, Response response) throws IOException {
            	if (response.isSuccessful()) {
                	String jsonData = response.body().string();
                	// Parse the JSON response to extract suggestions
                	parseAddressSuggestions(jsonData);


                	// Update the suggestions box on the JavaFX Application Thread
                	Platform.runLater(() -> {
                    	addressSuggestionsBox.getItems().setAll(addressSuggestions);
                    	if (addressTypeCounter > 3) {
                        	addressSuggestionsBox.setVisible(!addressSuggestions.isEmpty()); // Show suggestions if not empty
                    	}
                	});
            	} else {
                	//   Platform.runLater(() -> suggestionsBox.setVisible(false)); // Hide suggestions on error
            	}
        	}
    	});
	}


	private void parseAddressSuggestions(String jsonData) {
    	try {
        	JSONObject jsonObject = new JSONObject(jsonData);
        	JSONArray predictions = jsonObject.getJSONArray("predictions");




        	// Map of replacements (longer terms to shorter abbreviations)
        	Map<String, String> replacements = new HashMap<>();
        	replacements.put("Street", "St");
        	replacements.put("Avenue", "Ave");
        	replacements.put("Boulevard", "Blvd");
        	replacements.put("Road", "Rd");
        	replacements.put("Lane", "Ln");
        	replacements.put("Court", "Ct");
        	replacements.put("Place", "Pl");
        	replacements.put("Square", "Sq");
        	replacements.put("Drive", "Dr");
        	replacements.put("Parkway", "Pkwy");
        	replacements.put("Highway", "Hwy");
        	replacements.put("Trail", "Trl");
        	replacements.put("Way", "Way");
        	replacements.put("Terrace", "Ter");
        	replacements.put("Expressway", "Exp");
        	replacements.put("Center", "Ctr");
        	replacements.put("County", "Cty");
        	replacements.put("North", "N");
        	replacements.put("South", "S");
        	replacements.put("East", "E");
        	replacements.put("West", "W");




        	for (int i = 0; i < predictions.length(); i++) {
            	JSONObject prediction = predictions.getJSONObject(i);
            	String suggestion = prediction.getString("description");




            	// Remove ", USA" if it exists
            	if (suggestion.endsWith(", CO, USA")) {
                	suggestion = suggestion.substring(0, suggestion.length() - 9); // Remove last 5 characters
            	}




            	// Replace long strings with their abbreviated forms
            	for (Map.Entry<String, String> entry : replacements.entrySet()) {
                	suggestion = suggestion.replace(entry.getKey(), entry.getValue());
            	}




            	addressSuggestions.add(suggestion); // Add modified suggestion to the list
        	}
    	} catch (JSONException e) {
        	e.printStackTrace();
    	}
	}


	private String formatSelectedSuggestion(String selectedSuggestion) {
    	// Remove the state and country from the suggestion
    	String[] parts = selectedSuggestion.split(",");
    	if (parts.length >= 2) {
        	// Return formatted address
        	return parts[0] + ", " + parts[1]; // Keep the first two parts (street, city)
    	}
    	return selectedSuggestion; // Return the original suggestion if no comma found
	}

    
    private void recolorButtonImage(ToggleButton button, Color contentColor, Color outlineColor) {
        if (!(button.getGraphic() instanceof ImageView iv)) {
            return;
        }
    
        Image original = iv.getImage();
        if (original == null) {
            return;
        }

    
        int width = (int) original.getWidth();
        int height = (int) original.getHeight();
        WritableImage newImage = new WritableImage(width, height);
    
        var reader = original.getPixelReader();
        var writer = newImage.getPixelWriter();
    
        int changedPixels = 0;
    
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixelColor = reader.getColor(x, y);
                Color newColor = pixelColor;
    
                if (pixelColor.getOpacity() == 0.0) {
                    // transparent, keep as is
                } else if (isNearBlack(pixelColor)) {
                    newColor = contentColor;
                    changedPixels++;
                } else if (isNearGray(pixelColor)) {
                    newColor = new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), pixelColor.getOpacity());
                    changedPixels++;
                }
    
                writer.setColor(x, y, newColor);
            }
        }
    
        iv.setImage(newImage); // <-- apply the recolored image back
    }

    

    private boolean isNearGray(Color color) {
        double r = color.getRed();
        double g = color.getGreen();
        double b = color.getBlue();
        double avg = (r + g + b) / 3.0;
        return Math.abs(r - avg) < 0.05 && Math.abs(g - avg) < 0.05 && Math.abs(b - avg) < 0.05;
    }
    
    private boolean isNearBlack(Color color) {
        return color.getRed() < 0.15 && color.getGreen() < 0.15 && color.getBlue() < 0.15;
    }

    private void clearButtonSelectionStyles() {
        for (Toggle button : serviceToggleGroup.getToggles()) {
            if (button instanceof ToggleButton tb) {
                tb.getStyleClass().remove("service-type-button-selected");
            }
        }
    }   
    
    
    private boolean isBusinessDay(LocalDate date) {
        return !(date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                date.getDayOfWeek() == DayOfWeek.SUNDAY ||
                Config.COMPANY_HOLIDAYS.contains(date));
    }

    private LocalDate getNextBusinessDay(LocalDate startDate) {
        LocalDate nextDate = startDate.plusDays(1);
        while (!isBusinessDay(nextDate)) {
            nextDate = nextDate.plusDays(1);
        }
        return nextDate;
    }

    private int getServiceNumber(Integer serviceId) {
        if (serviceId == null) return 1;
    
        int count = 1; // current service counts as 1
        Integer currentId = serviceId;
    
        String sql = "SELECT previous_service_id FROM services WHERE service_id = ?";
    
        try (Connection conn = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
            while (currentId != null) {
                pstmt.setInt(1, currentId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        currentId = rs.getObject("previous_service_id", Integer.class);
                        if (currentId != null) count++;
                    } else {
                        break; // no matching service found
                    }
                }
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return count;
    }
    
    /**
     * Returns ordinal suffix in normal text (st, nd, rd, th)
     */
    private String getOrdinal(int number) {
        int mod100 = number % 100;
        int mod10 = number % 10;
    
        if (mod100 >= 11 && mod100 <= 13) {
            return "th";
        }
    
        return switch (mod10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }
    
    private String getDateForDB(ToggleGroup toggleGroup) {
        ToggleButton selectedWeekdayButton = (ToggleButton) toggleGroup.getSelectedToggle();
       if (selectedWeekdayButton == null) {
           statusLabel.setText("Please select a weekday."); // Show error message
           statusLabel.setTextFill(Color.RED);
           statusLabel.setVisible(true);
           return null; // Exit the method early
       }

       String selectedWeekdayText = selectedWeekdayButton.getText();
       LocalDate today = LocalDate.now(); // Initialize to today
       int currentYear = today.getYear(); // Get the current year
       String[] parts = selectedWeekdayText.split("/");
       int month = Integer.parseInt(parts[0]); // Get the month
       int day = Integer.parseInt(parts[1]); // Get the day

       // Adjust selectedDate to the closest date
       LocalDate selectedDateThisYear = LocalDate.of(currentYear, month, day);
       LocalDate selectedDateLastYear = selectedDateThisYear.minusYears(1);
       LocalDate selectedDateNextYear = selectedDateThisYear.plusYears(1);

       LocalDate selectedDate;
       if (Math.abs(ChronoUnit.DAYS.between(today, selectedDateThisYear))
               <= Math.abs(ChronoUnit.DAYS.between(today, selectedDateNextYear))) {
            if (Math.abs(ChronoUnit.DAYS.between(today, selectedDateThisYear))
                     <= Math.abs(ChronoUnit.DAYS.between(today, selectedDateLastYear))) {
                selectedDate = selectedDateThisYear;
            } else {
                selectedDate = selectedDateLastYear;
            }
       } else {
           selectedDate = selectedDateNextYear;
       }

       if (selectedDate.getDayOfWeek() == DayOfWeek.SATURDAY || selectedDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
           statusLabel.setText("Selected date cannot be on a weekend."); // Show error message
           statusLabel.setTextFill(Color.RED);
           statusLabel.setVisible(true);
           return null; // Exit the method early
       }

       return selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // For database
    }

    @Override
    public double getTotalHeight() {
        if (serviceType == null) {
            return originalScissorHeight; // default rest height
        }
    
        switch (serviceType) {
            case "Change Out":
                return 225;
            case "Service Change Out":
                return 273;
            case "Service":
                return 273;
            case "Move":
                return 240;
            default:
                return originalScissorHeight;
        }
    }
    

}

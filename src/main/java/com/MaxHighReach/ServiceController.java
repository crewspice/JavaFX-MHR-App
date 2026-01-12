package com.MaxHighReach;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
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

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import okhttp3.*;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javafx.scene.control.Button;



public class ServiceController extends BaseController {
    @FXML private AnchorPane rootPane; // or whatever the root element in your FXML is

    @FXML private ToggleGroup serviceToggleGroup;
    @FXML private ToggleButton changeOutButton;
    @FXML private ToggleButton serviceChangeOutButton;
    @FXML private ToggleButton serviceButton;
    @FXML private ToggleButton moveButton;

    // Labels for rental details
    @FXML private Label customerNameLabel;
    @FXML private Label addressBlockOneLabel;
    @FXML private Label addressBlockTwoLabel;
    @FXML private Label addressBlockThreeLabel;
    @FXML private Label rentalItemIdLabel;
    @FXML private Label liftTypeLabel;
    @FXML private Label rentalInfoLabel;
    @FXML private Line dividerLine;
    @FXML private Label serviceDateLabel;
    @FXML private DatePicker datePicker;
    @FXML private TilePane weekViewTilePane;
    @FXML private Label[] dayLabels;
    @FXML private TilePane weeksRowTilePane;
    @FXML private Rectangle calendarCover;
    @FXML private Button openCalendar;
    @FXML private Label serviceTimeLabel;
    @FXML private TilePane serviceTimeTilePane;
    @FXML private ComboBox<String> serviceHourComboBox;
    @FXML private ToggleButton serviceTime8To10Button;
    @FXML private ToggleButton serviceTimeASAPButton;
    @FXML private ToggleButton serviceTimeAnyButton;
    @FXML private ToggleButton serviceCustomButton;
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
    @FXML private Button notesButton;
    @FXML private Label notesLabel;
    @FXML private TextField notesField;
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
    @FXML private Label specificLiftLabel;
    @FXML private HBox specificLiftHBox;
    @FXML private CheckBox noPreferenceCheckBox;
    @FXML private Label noPreferenceLabel;
    @FXML private Button scheduleButton;
    @FXML private Label statusLabel;
    
    private int originalScissorHeight = 545;
    private Rental expandedRental;
    private Customer expandedCustomer;
    private String serviceType = null;
    private boolean isCalendarExpanded = false;
    private boolean dateSelected = false;
    private ToggleGroup serviceTimeToggleGroup;
    private ObservableList<Contact> orderingContacts = FXCollections.observableArrayList();
    private ObservableList<Contact> siteContacts = FXCollections.observableArrayList();
    private ObservableList<String> addressSuggestions = FXCollections.observableArrayList();
	private OkHttpClient client = new OkHttpClient();
	private int addressTypeCounter = 0;
    private ToggleGroup weeksToggleGroup;
    private ToggleGroup liftTypeToggleGroup;
    private volatile List<Lift> siteLiftsSet;
    private volatile boolean liftsSetReady = false;
    private Lift selectedLift = null;
    private int MAX_LIFTS_DISPLAY = 4;

    public static class Contact {
        public final Long contactId;
        public final String name;
        public final String phone;
    
        public Contact(Long contactId, String name, String phone) {
            this.contactId = contactId;
            this.name = name;
            this.phone = phone;
        }
    
        @Override
        public String toString() {
            return name; // Shows name in ComboBox
        }
    }
    

    @FXML
    public void initialize() {
        // Toggle group setup
        serviceToggleGroup = new ToggleGroup();
        changeOutButton.setToggleGroup(serviceToggleGroup);
        serviceChangeOutButton.setToggleGroup(serviceToggleGroup);
        serviceButton.setToggleGroup(serviceToggleGroup);
        moveButton.setToggleGroup(serviceToggleGroup);
        serviceToggleGroup.selectToggle(null);
    
        // Tooltips below images
        setTooltipBelow(changeOutButton, "Change Out");
        setTooltipBelow(serviceChangeOutButton, "Service Change Out");
        setTooltipBelow(serviceButton, "Service");
        setTooltipBelow(moveButton, "Move");
    
        expandedRental = MaxReachPro.getRentalForExpanding();
        System.out.println("expandedRental lat: " + expandedRental.getLatitude());
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
        Color secondaryColor = Color.web(Config.getSecondaryColor());
    
        // Recolor images
        recolorButtonImage(changeOutButton, contentColor, secondaryColor);
        recolorButtonImage(serviceChangeOutButton, contentColor, secondaryColor);
        recolorButtonImage(serviceButton, contentColor, secondaryColor);
        recolorButtonImage(moveButton, contentColor, secondaryColor);
        
        serviceToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            // Clear previous styles
            clearButtonSelectionStyles();
        
            if (newToggle == null) { 
                // User deselected the toggle
                dividerLine.setOpacity(0.0);
                setUniversalElementsVisibility(false);
                return;
            }
        
            // User selected a new toggle
            if (newToggle instanceof ToggleButton selectedButton) {
                dividerLine.setOpacity(1.0);
                selectedButton.getStyleClass().add("service-type-button-selected");
                setUniversalElementsVisibility(false);
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

        setTooltipBelow(notesButton, "Notes");

        setupTextFieldListeners(notesField, notesButton, notesLabel);


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

        // Recolor all labels
        Platform.runLater(() -> getAllLabels().forEach(label -> label.setTextFill(contentColor)));
    
        startLiftsSetPreload();
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
    
    @FXML
    private void handleChangeOut() {
        if ("Change Out".equals(serviceType)) {
            serviceType = null;
            MaxReachPro.getScissorLift().animateTransition(originalScissorHeight);
            setUniversalElementsVisibility(false);
        } else {
            serviceType = "Change Out";
            double targetHeight = 225;
            if (liftsSetReady) targetHeight -= 108;
            MaxReachPro.getScissorLift().animateTransition(targetHeight);
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
            double targetHeight = 273;
            if (liftsSetReady) targetHeight -= 108;
            MaxReachPro.getScissorLift().animateTransition(targetHeight);
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
            double targetHeight = 273;
            if (liftsSetReady) targetHeight -= 108;
            MaxReachPro.getScissorLift().animateTransition(targetHeight);
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
            double targetHeight = 240;
            if (liftsSetReady) targetHeight -= 108;
            MaxReachPro.getScissorLift().animateTransition(targetHeight);
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
    
        // Notes elements
        notesButton.setVisible(visible);
        if (!visible) {
            notesLabel.setVisible(false);
            notesField.setVisible(false);
        }  // text field only
    
        // Charge Delivery Trip elements
        chargeDeliveryTripCheckBox.setVisible(visible);
        chargeDeliveryTripLabel.setVisible(visible);

        scheduleButton.setVisible(visible);
    }

    private void updateConditionalElements() {
        // Hide all conditional elements by default
        reasonLabel.setVisible(false);
        reasonField.setVisible(false);
        specificLiftLabel.setVisible(false);
        specificLiftHBox.setVisible(false);
        noPreferenceCheckBox.setVisible(false);
        noPreferenceLabel.setVisible(false);
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
                if (liftsSetReady) {
                    specificLiftLabel.setVisible(true);
                    specificLiftHBox.setVisible(true);
                    noPreferenceCheckBox.setVisible(true);
                    noPreferenceLabel.setVisible(true);
                    targetY += 108;
                }
                break;
    
            case "Service Change Out":
            case "Service":
                chargeDeliveryTripCheckBox.setSelected(false);
                reasonLabel.setVisible(true);
                reasonField.setVisible(true);
                orderedByLabel.setText("Reported By:");
                targetY = 457;
                if (liftsSetReady) {
                    specificLiftLabel.setVisible(true);
                    specificLiftHBox.setVisible(true);
                    noPreferenceCheckBox.setVisible(true);
                    noPreferenceLabel.setVisible(true);
                    targetY += 108;
                }
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
                if (liftsSetReady) {
                    specificLiftLabel.setVisible(true);
                    specificLiftHBox.setVisible(true);
                    noPreferenceCheckBox.setVisible(true);
                    noPreferenceLabel.setVisible(true);
                    targetY += 108;
                }               
                break;
    
            default:
                chargeDeliveryTripCheckBox.setSelected(false);
                targetY = scheduleButton.getLayoutY(); // Stay in place
                break;
        }
    
        animateScheduleButton(targetY);
    }
    
    private void animateScheduleButton(double targetY) {
        double currentY = scheduleButton.getLayoutY();
        double labelCurrentY = specificLiftLabel.getLayoutY();
        double hboxCurrentY  = specificLiftHBox.getLayoutY();
        double checkBoxCurrentY = noPreferenceCheckBox.getLayoutY();
        double prefLabelCurrentY = noPreferenceLabel.getLayoutY();
        double statusLabelCurrentY = statusLabel.getLayoutY();

        double checkBoxTargetY = targetY - 112;
        double labelTargetY   = targetY - 112;
        double hboxTargetY    = targetY - 90;
        double statusLabelTargetY = targetY + 20;

        if (Math.abs(currentY - targetY) < 0.5 &&
            Math.abs(labelCurrentY - labelTargetY) < 0.5 &&
            Math.abs(hboxCurrentY - hboxTargetY) < 0.5 &&
            Math.abs(statusLabelCurrentY - statusLabelTargetY) < 0.5) {
            return; // Already positioned
        }

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(scheduleButton.layoutYProperty(), currentY),
                new KeyValue(specificLiftLabel.layoutYProperty(), labelCurrentY),
                new KeyValue(specificLiftHBox.layoutYProperty(), hboxCurrentY),
                new KeyValue(noPreferenceLabel.layoutYProperty(), labelCurrentY),
                new KeyValue(noPreferenceCheckBox.layoutYProperty(), checkBoxCurrentY),
                new KeyValue(statusLabel.layoutYProperty(), statusLabelCurrentY)
            ),
            new KeyFrame(Duration.millis(1000),
                new KeyValue(scheduleButton.layoutYProperty(), targetY, Interpolator.EASE_BOTH),
                new KeyValue(specificLiftLabel.layoutYProperty(), labelTargetY, Interpolator.EASE_BOTH),
                new KeyValue(specificLiftHBox.layoutYProperty(), hboxTargetY, Interpolator.EASE_BOTH),
                new KeyValue(noPreferenceLabel.layoutYProperty(), labelTargetY, Interpolator.EASE_BOTH),
                new KeyValue(noPreferenceCheckBox.layoutYProperty(), checkBoxTargetY, Interpolator.EASE_BOTH),
                new KeyValue(statusLabel.layoutYProperty(), statusLabelTargetY, Interpolator.EASE_BOTH)
            )
        );

        timeline.play();
    }


    @FXML
    public void handleOpenCalendar(ActionEvent event) {
        Button sourceButton = (Button) event.getSource(); // Get the source of the event

        // Check which button was clicked (Delivery or End)
            if (!isCalendarExpanded) {
                openCalendar(); // Open the Delivery DatePicker
            } else {
                closeCalendar(); // Close the Delivery DatePicker
            }
            if (!isCalendarExpanded) {
                openCalendar(); // Open the Call Off DatePicker
            } else {
                closeCalendar(); // Close the Call Off DatePicker
            }
    }

    private void clearButtonSelectionStyles() {
        for (Toggle button : serviceToggleGroup.getToggles()) {
            if (button instanceof ToggleButton tb) {
                tb.getStyleClass().remove("service-type-button-selected");
            }
        }
    }

    public void openCalendar() {
        datePicker.show();
        isCalendarExpanded = true;
        datePicker.requestFocus(); // Focus on the DatePicker


        // When a date is selected, update the buttons and close the calendar
        datePicker.setOnAction(event -> {
            LocalDate selectedDate = datePicker.getValue();
            if (selectedDate != null) {
                dateSelected = true;
                updateWeekdayToggleButtons(selectedDate);
                isCalendarExpanded = false;
            }
        });


        // Hide the calendar when focus is lost (if no date is selected)
        datePicker.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && !dateSelected) {
                closeCalendar();
            }
        });
    }


    // Method to close the calendar for the specific DatePicker
    private void closeCalendar() {
        isCalendarExpanded = false;
        // Optionally, show a cover for the calendar
        calendarCover.setVisible(true);
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

    private void startLiftsSetPreload() {

        Task<List<Lift>> preloadTask = new Task<>() {
            @Override
            protected List<Lift> call() throws Exception {

                // Let UI fully render before doing DB work
                Thread.sleep(2000);

                List<Lift> lifts = new ArrayList<>();

                int rentalOrderId = expandedRental.getRentalOrderId();
                double baseLat = expandedRental.getLatitude();
                double baseLon = expandedRental.getLongitude();
                double threshold = Config.PROXIMITY_THRESHOLD_DEG;
                String customerName = expandedRental.getName();

                // --- COPY-PASTE SQL (for manual testing) ---
                String debugSql = """
                    SELECT 
                        ri.rental_item_id,
                        l.serial_number,
                        l.lift_type,
                        ro.latitude,
                        ro.longitude
                    FROM rental_items ri
                    JOIN lifts l
                        ON ri.lift_id = l.lift_id
                    JOIN rental_orders ro
                        ON ri.rental_order_id = ro.rental_order_id
                    JOIN customers c
                        ON ro.customer_id = c.customer_id
                    WHERE ri.item_status = 'Active'
                    AND c.customer_name = '%s'
                    AND (
                            ri.rental_order_id = %d
                        OR (
                            ABS(ro.latitude  - %f) <= %f
                            AND
                            ABS(ro.longitude - %f) <= %f
                        )
                    );
                    """.formatted(
                        customerName,
                        rentalOrderId,
                        baseLat, threshold,
                        baseLon, threshold
                    );

                System.out.println("\n===== LIFTS PRELOAD SQL =====");
                System.out.println(debugSql);
                System.out.println("=============================\n");

                // --- Prepared SQL (actual execution) ---
                String sql = """
                    SELECT 
                        ri.rental_item_id,
                        l.serial_number,
                        l.lift_type,
                        l.lift_id
                    FROM rental_items ri
                    JOIN lifts l
                        ON ri.lift_id = l.lift_id
                    JOIN rental_orders ro
                        ON ri.rental_order_id = ro.rental_order_id
                    JOIN customers c
                        ON ro.customer_id = c.customer_id
                    WHERE ri.item_status = 'Active'
                    AND c.customer_name = ?
                    AND (
                            ri.rental_order_id = ?
                        OR (
                            ABS(ro.latitude  - ?) <= ?
                            AND
                            ABS(ro.longitude - ?) <= ?
                        )
                    )
                    """;

                try (Connection connection = DriverManager.getConnection(
                            Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
                    PreparedStatement ps = connection.prepareStatement(sql)) {

                    ps.setString(1, customerName);
                    ps.setInt(2, rentalOrderId);
                    ps.setDouble(3, baseLat);
                    ps.setDouble(4, threshold);
                    ps.setDouble(5, baseLon);
                    ps.setDouble(6, threshold);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {

                            String serialNumber = rs.getString("serial_number");
                            String liftType     = rs.getString("lift_type");
                            int liftId = rs.getInt("lift_id");

                            System.out.println("🔧 Lift found → serial=" + serialNumber);

                            Lift lift = new Lift(String.valueOf(liftId), liftType);
                            lift.setSerialNumber(serialNumber);
                            lifts.add(lift);
                        }
                    }
                }

                return lifts;
            }
        };

        preloadTask.setOnSucceeded(e -> {
            siteLiftsSet = preloadTask.getValue();

            Platform.runLater(() -> {

                // No ambiguity → no UI, no "ready" state
                if (siteLiftsSet == null || siteLiftsSet.size() <= 1) {
                    liftsSetReady = false;
                    return;
                }
                specificLiftLabel.setText("Which lift needs service?");

                populateSpecificLiftHBox(siteLiftsSet);
            });
        });


        preloadTask.setOnFailed(e -> {
            liftsSetReady = false;
            preloadTask.getException().printStackTrace();
        });

        Thread t = new Thread(preloadTask, "lifts-preload-thread");
        t.setDaemon(true);
        t.start();
    }


    private void populateSpecificLiftHBox(List<Lift> lifts) {
        specificLiftHBox.getChildren().clear();
        liftsSetReady = false;

        if (lifts == null || lifts.isEmpty() || lifts.size() == 1) {
            specificLiftHBox.setVisible(false);
            return;
        }

        // Only show up to 4 lifts
        List<Lift> displayLifts = lifts.stream()
                                    .filter(l -> l.getLiftType() != null && !l.getLiftType().isBlank())
                                    .limit(MAX_LIFTS_DISPLAY)
                                    .toList();

        int liftCount = displayLifts.size();

        // Compute spacing: 5 px
        specificLiftHBox.setSpacing(5);

        // Centering logic: calculate padding based on app width
        double totalLiftWidth = liftCount * 70 + (liftCount - 1) * specificLiftHBox.getSpacing();
        double leftPadding = Math.max(0, (Config.WINDOW_WIDTH - totalLiftWidth) / 2 - 14); 
        specificLiftHBox.setPadding(new Insets(0, 0, 0, leftPadding));

        Color contentColor = Color.web(Config.getTertiaryColor());
        Color outlineColor = Color.BLACK;

        for (Lift lift : displayLifts) {
            String imagePath = "/images/" + lift.getLiftType() + ".png";

            Image image;
            try {
                image = new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream(imagePath),
                        "Missing image for liftType: " + lift.getLiftType())
                );
            } catch (Exception ex) {
                System.out.println("⚠️ No image for liftType: " + lift.getLiftType());
                continue;
            }

            image = recolorImage(image, contentColor);

            ImageView iv = new ImageView(image);
            iv.setFitWidth(30);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);

            Label serialLabel = new Label(lift.getSerialNumber());
            serialLabel.setWrapText(true);
            serialLabel.setTextAlignment(TextAlignment.CENTER);
            serialLabel.setAlignment(Pos.CENTER);
            serialLabel.setMaxWidth(70);
            serialLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
            serialLabel.setStyle("""
                -fx-font-size: 11px;
                -fx-font-weight: bold;
                -fx-background-color: rgba(255,255,255,0.8);
                -fx-padding: 2px;
            """);

            VBox liftBox = new VBox(4, iv, serialLabel);
            liftBox.setAlignment(Pos.CENTER);
            liftBox.setPrefWidth(70);
            liftBox.setMinHeight(50);
            liftBox.getStyleClass().add("lift-box");

            // Make selectable/toggleable
            liftBox.setOnMouseClicked(e -> {
                if (selectedLift == lift) {
                    // Deselect
                    selectedLift = null;
                    liftBox.setStyle("-fx-border-color: transparent;");
                } else {
                    // Deselect previous
                    for (Node child : specificLiftHBox.getChildren()) {
                        child.setStyle("-fx-border-color: transparent;");
                    }
                    selectedLift = lift;
                    String primaryColor = Config.getPrimaryColor();
                    liftBox.setStyle(
                        String.format("-fx-border-color: %s; -fx-border-width: 2px; -fx-border-radius: 5px;", primaryColor)
                    );
                }
            });


            Tooltip.install(liftBox, new Tooltip("Serial: " + lift.getSerialNumber()));

            specificLiftHBox.getChildren().add(liftBox);
        }
        noPreferenceCheckBox.setSelected(false);
        liftsSetReady = true;
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

    @FXML
    private void handleNotes(){
        toggleDedicatedField(notesButton, notesLabel, notesField);
    }

    private void toggleDedicatedField(Button button, Label label, TextField textField) {
        boolean isDedicatedFieldVisible = textField.isVisible();
    
        // Hide other dedicated field buttons while this one is visible
        notesButton.setVisible(isDedicatedFieldVisible && button != notesButton);
    
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
            notesButton.setVisible(true);
    
            // Restore charge delivery trip elements
            chargeDeliveryTripCheckBox.setVisible(true);
            chargeDeliveryTripLabel.setVisible(true);
        }
    }

    @FXML
    private void handleScheduleService() {
        int rentalItemId = expandedRental.getRentalItemId();
    
        // 1️⃣ Service Date from selected week
        LocalDate serviceDate = getSelectedDate(weeksToggleGroup);
        java.sql.Date sqlServiceDate = serviceDate != null ? java.sql.Date.valueOf(serviceDate) : null;
    
        // Service order date is always today
        java.sql.Date sqlServiceOrderDate = java.sql.Date.valueOf(LocalDate.now());
    
        // 3️⃣ Time
        Toggle selectedTimeToggle = serviceTimeToggleGroup.getSelectedToggle();
        String timeText = "None Selected";
        if (selectedTimeToggle != null) {
            String toggleText = ((ToggleButton) selectedTimeToggle).getText();
            if ("Custom".equalsIgnoreCase(toggleText)) {
                timeText = serviceHourComboBox.getValue() != null ? serviceHourComboBox.getValue().toString() : "No Time Selected";
            } else {
                timeText = toggleText;
            }
        }
    
        // 4️⃣ Lift type mapped
        Toggle selectedLiftToggle = liftTypeToggleGroup.getSelectedToggle();
        String liftTypeText = selectedLiftToggle != null ? ((ToggleButton) selectedLiftToggle).getText() : null;
        Integer originalLiftType = expandedRental.getLiftId();
        Integer liftTypeMapped = liftTypeText != null ? Config.LIFT_TYPE_MAP.get(liftTypeText) : originalLiftType;
        Integer oldLiftId = expandedRental.getLiftId(); // default behavior

        if (liftsSetReady) {

            // CASE 1: User selected a specific lift
            if (selectedLift != null) {
                try {
                    oldLiftId = Integer.parseInt(selectedLift.getLiftId());
                } catch (NumberFormatException e) {
                    statusLabel.setText("Invalid lift ID selected.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    statusLabel.setVisible(true);
                    return;
                }
            }

            // CASE 2: Multiple lifts, none selected
            else {
                // If user has NO preference checked → allow generic
                if (noPreferenceCheckBox.isSelected()) {

                    // expandedRental.getLiftType() is short-form (e.g. "26s")
                    String shortLiftType = expandedRental.getLiftType();

                    String standardizedType =
                            Config.LIFT_BUTTON_TEXT_MAP.get(shortLiftType);

                    if (standardizedType == null) {
                        statusLabel.setText("Unable to determine lift type for service.");
                        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        statusLabel.setVisible(true);
                        return;
                    }

                    Integer genericLiftId =
                            Config.LIFT_TYPE_MAP.get(standardizedType);

                    if (genericLiftId == null) {
                        statusLabel.setText("No generic lift ID found for selected lift type.");
                        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        statusLabel.setVisible(true);
                        return;
                    }

                    oldLiftId = genericLiftId;
                }

                // CASE 3: Multiple lifts, no selection, no preference NOT checked → FAIL
                else {
                    statusLabel.setText("Please select a specific lift or choose 'No Preference'.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    statusLabel.setVisible(true);
                    return;
                }
            }
        }
    
        // 5️⃣ Contacts
        Contact selectedOrdering = orderedByBox.getValue();
        Contact selectedSite = siteContactBox.getValue();
    
        Long orderingId = null;
        Long siteId = null;
    
        String orderedByName = orderedByField.getText().trim();
        String orderedByPhone = orderedByPhoneField.getText().replaceAll("\\D", "").trim();
    
        String siteContactName = siteContactField.getText().trim();
        String siteContactPhone = siteContactPhoneField.getText().replaceAll("\\D", "").trim();
    
        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD)) {
    
            // Ordering contact
            if (!orderedByName.isEmpty() && !orderedByPhone.isEmpty()) {
                if (selectedOrdering != null) {
                    orderingId = selectedOrdering.contactId;
                } else {
                    orderingId = insertContactInDB(connection, orderedByName, orderedByPhone, true);
                }
            }
    
            // Site contact
            if (!siteContactName.isEmpty() && !siteContactPhone.isEmpty()) {
                if (selectedSite != null) {
                    siteId = selectedSite.contactId;
                } else {
                    siteId = insertContactInDB(connection, siteContactName, siteContactPhone, false);
                }
            }
    
            // 6️⃣ Checkboxes and text fields
            int chargeDeliveryTrip = chargeDeliveryTripCheckBox.isSelected() ? 1 : 0;
            String notes = notesField.getText();
            String reason = reasonField.getText();
    
            reason = (reason == null || reason.isBlank()) ? null : reason;
            notes = (notes == null || notes.isBlank()) ? null : notes;
    
            // 7️⃣ Previous service
            
            Integer previousServiceId = expandedRental.getLatestServiceId();
    
            // 8️⃣ New rental order ID if moving to a new site
            Integer newRentalOrderId = null;
            boolean isMoveAndNewSite = "Move".equalsIgnoreCase(serviceType) && !sameSiteBox.isSelected();
    
            if (isMoveAndNewSite) {
                // Query current rental_order to reuse its values
                String selectOldOrder = "SELECT customer_id, order_date, delivery_date, call_off_date, " +
                        "order_status, is_invoice_created, is_contract_created, single_item_order, po_number, unit_number, latitude, longitude " +
                        "FROM rental_orders WHERE rental_order_id = ?";
                int oldRentalOrderId = expandedRental.getRentalOrderId();
                String customerId;
                java.sql.Date orderDate, deliveryDate, callOffDate;
                double deliveryCost, totalCost, latitude, longitude;
                String orderStatus, poNumber, unitNumber;
                int isInvoiceCreated, isContractCreated, singleItemOrder;
    
                try (PreparedStatement oldOrderStmt = connection.prepareStatement(selectOldOrder)) {
                    oldOrderStmt.setInt(1, oldRentalOrderId);
                    try (ResultSet rs = oldOrderStmt.executeQuery()) {
                        if (rs.next()) {
                            customerId = rs.getString("customer_id");
                            orderDate = rs.getDate("order_date");
                            deliveryDate = rs.getDate("delivery_date");
                            callOffDate = rs.getDate("call_off_date");
                            orderStatus = rs.getString("order_status");
                            isInvoiceCreated = rs.getInt("is_invoice_created");
                            isContractCreated = rs.getInt("is_contract_created");
                            singleItemOrder = rs.getInt("single_item_order");
                            poNumber = rs.getString("po_number");
                            unitNumber = rs.getString("unit_number");
                            latitude = rs.getDouble("latitude");
                            longitude = rs.getDouble("longitude");
                        } else throw new SQLException("Old rental order not found");
                    }
                }
    
                // Insert new rental_order
                String insertRentalOrder = """
                    INSERT INTO rental_orders (customer_id, order_date, delivery_date, call_off_date,
                    order_status, is_invoice_created, is_contract_created, single_item_order, po_number, unit_number, latitude, longitude,
                    site_name, street_address, city)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
    
                try (PreparedStatement newOrderStmt = connection.prepareStatement(insertRentalOrder, Statement.RETURN_GENERATED_KEYS)) {
                    newOrderStmt.setString(1, customerId);
                    newOrderStmt.setDate(2, orderDate);
                    newOrderStmt.setDate(3, deliveryDate);
                    newOrderStmt.setDate(4, callOffDate);
                    newOrderStmt.setString(5, orderStatus);
                    newOrderStmt.setInt(6, isInvoiceCreated);
                    newOrderStmt.setInt(7, isContractCreated);
                    newOrderStmt.setInt(8, singleItemOrder);
                    newOrderStmt.setString(9, poNumber);
                    newOrderStmt.setString(10, unitNumber);
                    newOrderStmt.setDouble(11, latitude);
                    newOrderStmt.setDouble(12, longitude);
                    newOrderStmt.setString(13, newSiteField.getText());
                    newOrderStmt.setString(14, newAddressField.getText());
                    newOrderStmt.setString(15, expandedRental.getCity()); // assuming city available
                    newOrderStmt.executeUpdate();
    
                    try (ResultSet generatedKeys = newOrderStmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            newRentalOrderId = generatedKeys.getInt(1);
                        }
                    }
                }
            }
    
            // Insert into services
            String insertServiceSQL = """
                INSERT INTO services (
                    service_type, service_date, time, ordered_contact_id, site_contact_id,
                    rental_item_id, service_order_date, reason, notes,
                    billable, previous_service_id, new_lift_id, new_rental_order_id, old_lift_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
    
            int newServiceId = -1;
    
            try (PreparedStatement stmt = connection.prepareStatement(insertServiceSQL, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, serviceType);
                stmt.setDate(2, sqlServiceDate);
                stmt.setString(3, timeText);
                if (orderingId != null) stmt.setLong(4, orderingId); else stmt.setNull(4, Types.BIGINT);
                if (siteId != null) stmt.setLong(5, siteId); else stmt.setNull(5, Types.BIGINT);
                stmt.setLong(6, rentalItemId);
                stmt.setDate(7, sqlServiceOrderDate);
                if (reason != null) stmt.setString(8, reason); else stmt.setNull(8, Types.VARCHAR);
                if (notes != null) stmt.setString(9, notes); else stmt.setNull(9, Types.VARCHAR);
                stmt.setInt(10, chargeDeliveryTrip);
                if (previousServiceId != null && previousServiceId > 0) stmt.setInt(11, previousServiceId); else stmt.setNull(11, Types.INTEGER);
                if (liftTypeMapped != null) stmt.setInt(12, liftTypeMapped); else stmt.setNull(12, Types.INTEGER);
                if (newRentalOrderId != null) stmt.setInt(13, newRentalOrderId); else stmt.setNull(13, Types.INTEGER);
                stmt.setInt(14, oldLiftId);

                int rowsInserted = stmt.executeUpdate();
                if (rowsInserted > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            newServiceId = generatedKeys.getInt(1);
                            System.out.println("Inserted new service with ID: " + newServiceId);
                        }
                    }
                }
            }
    
            // 10️⃣ Update rental_items.last_service_id
            if (newServiceId > 0) {
                String updateRentalItemSQL = "UPDATE rental_items SET last_service_id = ? WHERE rental_item_id = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateRentalItemSQL)) {
                    updateStmt.setInt(1, newServiceId);
                    updateStmt.setInt(2, rentalItemId);
                    updateStmt.executeUpdate();
                }
            }

            statusLabel.setText("Service successfully scheduled!");
            statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            statusLabel.setVisible(true);
            freezePage();   // <--- your freeze method
            return;         // prevent red error state
    
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setVisible(true);
            statusLabel.setText("Error scheduling service. Please try again.");
            statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        }

    }
    
    // Utility method to insert contact using existing connection
    private Long insertContactInDB(Connection connection, String name, String number, boolean isOrderingContact) throws SQLException {
        Long newContactId = null;
        String query = "INSERT INTO contacts (customer_id, first_name, phone_number, is_ordering_contact, is_site_contact) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, expandedRental.getCustomerId());
            stmt.setString(2, name);
            stmt.setString(3, number);
            stmt.setBoolean(4, isOrderingContact);
            stmt.setBoolean(5, !isOrderingContact);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) newContactId = keys.getLong(1);
            }
        }
        return newContactId;
    }
    
    
    
    public LocalDate getSelectedDate (ToggleGroup toggleGroup) {
    	System.out.println("get selected date called");
    	LocalDate date = LocalDate.now();
    	// Ensure a ToggleButton is selected
    	ToggleButton selectedWeekdayButton = (ToggleButton) toggleGroup.getSelectedToggle();
    	System.out.println("Selected weekday button is: " + selectedWeekdayButton);
    	if (selectedWeekdayButton == null) {
        	statusLabel.setText("Please select a weekday."); // Show error message
        	statusLabel.setTextFill(Color.RED);
        	statusLabel.setVisible(true);
        	return null; // Exit the method early
    	}


// Get the text of the selected weekday button (e.g., "12/17")
    	String selectedWeekdayText = selectedWeekdayButton.getText().trim();


// Split the text
    	String[] parts = selectedWeekdayText.split("/");
    	if (parts.length != 2) {
        	statusLabel.setText("Invalid date format. Use MM/DD format.");
        	statusLabel.setTextFill(Color.RED);
        	statusLabel.setVisible(true);
        	return null; // Exit early
    	}


// Parse month and day safely
    	int month, day;
    	try {
        	month = Integer.parseInt(parts[0].trim());
        	day = Integer.parseInt(parts[1].trim());
    	} catch (NumberFormatException e) {
        	statusLabel.setText("Invalid date input. Use numeric MM/DD format.");
        	statusLabel.setTextFill(Color.RED);
        	statusLabel.setVisible(true);
        	return null;
    	}


    	date = resolveDateWithYear(month, day);


// Check if the date falls on a weekend
    	if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
        	statusLabel.setText("Selected date cannot be on a weekend.");
        	statusLabel.setTextFill(Color.RED);
        	statusLabel.setVisible(true);
        	return null;
    	}
    	return date;
	}

    private LocalDate resolveDateWithYear(int month, int day) {
		LocalDate today = LocalDate.now();
		int currentYear = today.getYear();
	
		LocalDate selectedDateThisYear = LocalDate.of(currentYear, month, day);
		LocalDate selectedDateNextYear = selectedDateThisYear.plusYears(1);
	
		return Math.abs(ChronoUnit.DAYS.between(today, selectedDateThisYear)) <= 
			   Math.abs(ChronoUnit.DAYS.between(today, selectedDateNextYear)) 
			   ? selectedDateThisYear 
			   : selectedDateNextYear;
	}

    private void freezePage() {

        // Disable all toggle groups
        if (serviceToggleGroup != null)
            serviceToggleGroup.getToggles().forEach(t -> ((Node) t).setDisable(true));

        if (serviceTimeToggleGroup != null)
            serviceTimeToggleGroup.getToggles().forEach(t -> ((Node) t).setDisable(true));

        if (liftTypeToggleGroup != null)
            liftTypeToggleGroup.getToggles().forEach(t -> ((Node) t).setDisable(true));

        if (weeksToggleGroup != null)
            weeksToggleGroup.getToggles().forEach(t -> ((Node) t).setDisable(true));

        // Disable individual controls
        changeOutButton.setDisable(true);
        serviceChangeOutButton.setDisable(true);
        serviceButton.setDisable(true);
        moveButton.setDisable(true);

        datePicker.setDisable(true);
        openCalendar.setDisable(true);
        serviceHourComboBox.setDisable(true);

        orderedByBox.setDisable(true);
        orderedByField.setDisable(true);
        orderedByPhoneField.setDisable(true);

        siteContactBox.setDisable(true);
        siteContactField.setDisable(true);
        siteContactPhoneField.setDisable(true);

        notesButton.setDisable(true);
        notesField.setDisable(true);

        chargeDeliveryTripCheckBox.setDisable(true);
        reasonField.setDisable(true);

        newAddressField.setDisable(true);
        newSiteField.setDisable(true);
        sameSiteBox.setDisable(true);

        // Lift options
        twelveMastButton.setDisable(true);
        liftTypeTilePane.setDisable(true);
        specificLiftHBox.setDisable(true);
        noPreferenceCheckBox.setDisable(true);

        // Labels don’t need disabling, but we gray them out for clarity
        specificLiftLabel.setOpacity(0.6);
        noPreferenceLabel.setOpacity(0.6);
        // Disable schedule button itself
        scheduleButton.setDisable(true);

        // ⛔ Everything except BACK is frozen
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

    private Image recolorImage(Image original, Color newColor) {
        int width = (int) original.getWidth();
        int height = (int) original.getHeight();
        WritableImage recolored = new WritableImage(width, height);
        PixelReader reader = original.getPixelReader();
        PixelWriter writer = recolored.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixel = reader.getColor(x, y);
                if (pixel.getOpacity() == 0.0) {
                    writer.setColor(x, y, Color.TRANSPARENT);
                } else {
                    // Replace all visible pixels with new color, keeping original opacity
                    writer.setColor(x, y, new Color(
                        newColor.getRed(), newColor.getGreen(), newColor.getBlue(), pixel.getOpacity()
                    ));
                }
            }
        }

        return recolored;
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
        

    @Override
    public double getTotalHeight() { return originalScissorHeight; }
}

package com.MaxHighReach;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.sql.Connection;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.itextpdf.kernel.pdf.PdfName.Event;

public class ExpandController extends BaseController {

    private ObservableList<Customer> customers = FXCollections.observableArrayList();
    private ObservableList<Lift> lifts = FXCollections.observableArrayList();
    private Customer selectedCustomer;
    private Lift selectedLift;
    private boolean dateSelected = false;
    private int rentalOrderId;
    private CustomerRental currentCustomerRental;
    private boolean noCallOffMemory = false;

    @FXML
    private Label tableViewTitle;
    @FXML
    private TableView<CustomerRental> scheduledRentalsTableView;  // TableView for rentals
    @FXML
    private TableColumn<CustomerRental, String> customerIdColumn;  // Column for Customer ID
    @FXML
    private TableColumn<CustomerRental, String> rentalDateColumn;   // Column for Rental Date
    @FXML
    private TableColumn<CustomerRental, String> liftTypeColumn;     // Column for Lift Type
    @FXML
    private TableColumn<CustomerRental, String> deliveryTimeColumn;  // Column for Delivery Time

    private ObservableList<CustomerRental> rentalsList = FXCollections.observableArrayList(); // List to hold rentals

    @FXML
    private TextField customerNameField;
    @FXML
    private TextField contractNumberField;
    @FXML
    private VBox statusPane;
    @FXML
    private TextField orderedByField;
    @FXML
    private TextField orderedByPhoneField;
    @FXML
    private ComboBox orderedByBox;
    private Map<String, String> orderingContactsPhoneNumbers = new HashMap<>();
    private String selectedOrderingContactId;
    @FXML
    private Button autoTermButton;
    private final Tooltip autoTermTooltip = new Tooltip("Auto-term");
    @FXML
    private DatePicker datePickerDel; // For the calendar view
    @FXML
    private DatePicker datePickerEnd;
    @FXML
    private TilePane weekViewTilePane; // To show the week view
    @FXML
    private Label[] dayLabels; // Array to hold the day labels for week view

    private boolean isCalendarExpanded = false;
    @FXML
    private TilePane deliveryWeeksRowTilePane;
    @FXML
    private TilePane callOffWeeksRowTilePane;
    @FXML
    private Rectangle calendarCoverDel;
    @FXML
    private Rectangle calendarCoverEnd;
    @FXML
    private TilePane liftTypeTilePane;  // TilePane containing the lift type toggle buttons
    @FXML
    private ToggleButton twelveMastButton;
    @FXML
    private TilePane deliveryTimeTilePane;  // TilePane for delivery time toggle buttons

    private final ToggleGroup statusToggleGroup = new ToggleGroup();
    private ToggleGroup liftTypeToggleGroup;  // To ensure only one lift type can be selected at a time
    private ToggleGroup deliveryTimeToggleGroup;  // To ensure only one delivery time can be selected at a time
    private ToggleGroup weeksRowToggleGroup;
    private ToggleGroup weeksRowToggleGroupAT = new ToggleGroup();

    @FXML
    private ToggleButton deliveryTime8To10Button; // Reference to the "8-10" toggle button
    @FXML
    public Button openCalendarDel;
    @FXML
    private Button openCalendarEnd;
    @FXML
    private ToggleButton customButton;
    @FXML
    private ComboBox<String> hourComboBox;
 //   @FXML
 //   private ComboBox<String> ampmComboBox; // Reference to the AM/PM ComboBox
    @FXML
    private TextField siteField;
    @FXML
    private TextField addressField;
    @FXML
    private ComboBox<String> suggestionsBox;
    @FXML
    private TextField siteContactField;
    @FXML
    private ComboBox siteContactBox;
    @FXML
    private TextField siteContactPhoneField;
    private Map<String, String> siteContactsPhoneNumbers = new HashMap<>();
    private String selectedSiteContactId;
    @FXML
    private Label POLabel;
    @FXML
    private TextField POField;
    @FXML
    private Button locationNotesButton;
    private final Tooltip locationNotesTooltip = new Tooltip("Location notes");
    @FXML
    private Label locationNotesLabel;
    @FXML
    private TextField locationNotesField;
    @FXML
    private Button preTripInstructionsButton;
    private final Tooltip preTripInstructionsTooltip = new Tooltip("Pre-trip instructions");
    @FXML
    private Label preTripInstructionsLabel;
    @FXML
    private TextField preTripInstructionsField;
    @FXML
    private Label serialNumberLabel;
    @FXML
    private TextField serialNumberField;
    @FXML
    private HBox invoiceBox;
    @FXML
    private Button switchInvoiceButton;
    @FXML
    private VBox switchInvoiceLabelBox;
    @FXML
    private Button updateRentalButton;
    @FXML
    private Label statusLabel; // Reference to the status label

    private boolean suggestionMuter = false;// Flag to track rotation state
    private int addressTypeCounter = 0;
    private ObservableList<String> addressSuggestions = FXCollections.observableArrayList();
    private OkHttpClient client = new OkHttpClient();

    private CustomerRental expandedRental;


    public void initialize() {
        lifts = MaxReachPro.getLifts();
        customers = MaxReachPro.getCustomers();
        setRentalDate();

        customerNameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                // Remove focus from the text field
                customerNameField.getParent().requestFocus();
            }
        });

        customerNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Call your existing autofill logic
            autoFillCustomerName(newValue);

            // Check if there's text in the field and update the style accordingly
            if (newValue.isEmpty()) {
                // No text, show the underline
                customerNameField.getStyleClass().remove("has-text");
            } else {
                // Text is present, hide the underline
                if (!customerNameField.getStyleClass().contains("has-text")) {
                    customerNameField.getStyleClass().add("has-text");
                }
            }
        });

        contractNumberField.setEditable(false); // Prevents user input.
        contractNumberField.setFocusTraversable(false); // Disables focus and hides the cursor.

        contractNumberField.addEventFilter(MouseEvent.ANY, javafx.event.Event::consume); // Blocks mouse interaction.
        contractNumberField.addEventFilter(KeyEvent.ANY, javafx.event.Event::consume); // Blocks keyboard interaction.

        for (javafx.scene.Node node : statusPane.getChildren()) {
            if (node instanceof HBox) {
                for (Node hboxNode : ((HBox) node).getChildren()) {
                    if (hboxNode instanceof ToggleButton) {
                        ToggleButton toggleButton = (ToggleButton) hboxNode;
                        toggleButton.setToggleGroup(statusToggleGroup);
                        String buttonText = toggleButton.getText();
                        toggleButton.setOnAction(event -> {
                            expandedRental.setStatus(buttonText);
                            if (buttonText.equals("Upcoming") || buttonText.equals("Active")) {
                                if (!noCallOffMemory) {
                                    if (!expandedRental.isAutoTerm()) {
                                        System.out.println("Made it inside of this if statement. Here's where i'd like it to add dormant style.");
                                        updateWeekdayToggleButtons(callOffWeeksRowTilePane, null);
                                        expandedRental.setCallOffDate(null);
                                        weeksRowToggleGroupAT.selectToggle(null);
                                    }
                                }
                            } else {
                                System.out.println("about to switch noCallOffMemory to false and it's currently: " + noCallOffMemory);
                                if (noCallOffMemory) {
                                    LocalDate assumedCallOffDate = getAssumedCallOffDate();
                                    noCallOffMemory = false;
                                    updateWeekdayToggleButtons(callOffWeeksRowTilePane, assumedCallOffDate);
                                    expandedRental.setCallOffDate(getAssumedCallOffDate().toString());
                                }
                            }
                        });
                    }
                }
            }
        }

        orderedByBox.setOnAction(event -> handleContactSelection(orderedByBox, true));
        siteContactBox.setOnAction(event -> handleContactSelection(siteContactBox, false));

        orderedByBox.setPrefWidth(1);
        orderedByBox.setMinWidth(1);
        orderedByBox.setMaxWidth(1);

        createCustomTooltip(autoTermButton, 38, 10, autoTermTooltip);

        weeksRowToggleGroup = new ToggleGroup();  // Create the ToggleGroup for weeks
        liftTypeToggleGroup = new ToggleGroup();  // Create the ToggleGroup for lift types
        deliveryTimeToggleGroup = new ToggleGroup();  // Create the ToggleGroup for delivery times

        // Load the calendar icon image
        Image calendarImage = new Image(getClass().getResourceAsStream("/images/calendar.png"));
        ImageView calendarIcon = new ImageView(calendarImage);
        calendarIcon.setFitHeight(20);
        calendarIcon.setFitWidth(20);
        // Optionally, set the calendar button's graphic if you have a button for it.
        // calendarButton.setGraphic(calendarIcon);

        setupDatePicker(datePickerDel, deliveryWeeksRowTilePane, "Del", calendarCoverDel);

        // Set up the DatePicker for Call Off side with associated TilePane and Calendar Cover
        setupDatePicker(datePickerEnd, callOffWeeksRowTilePane, "End", calendarCoverEnd);

        ToggleGroup deliveryWeeksToggleGroup = new ToggleGroup();
        ToggleGroup callOffWeeksToggleGroup = new ToggleGroup();
        initializeWeekRowToggleButtons(deliveryWeeksRowTilePane, deliveryWeeksToggleGroup);
        initializeWeekRowToggleButtons(callOffWeeksRowTilePane, callOffWeeksToggleGroup);

        for (javafx.scene.Node node : liftTypeTilePane.getChildren()) {
            if (node instanceof ToggleButton) {
                ToggleButton toggleButton = (ToggleButton) node;
                toggleButton.setToggleGroup(liftTypeToggleGroup);  // Add each ToggleButton to the ToggleGroup
            }
        }

        prepareLiftTypeButtons();

        // Set up ToggleButtons for delivery time
        for (javafx.scene.Node node : deliveryTimeTilePane.getChildren()) {
            if (node instanceof ToggleButton) {
                ToggleButton toggleButton = (ToggleButton) node;
                toggleButton.setToggleGroup(deliveryTimeToggleGroup);  // Add each ToggleButton to the ToggleGroup

                // Hide the custom ComboBoxes when a delivery time button is clicked
                toggleButton.setOnAction(event -> {
                    if (toggleButton != customButton) {
                        hourComboBox.setVisible(false);
  //                      ampmComboBox.setVisible(false);
                        customButton.setSelected(false); // Unselect custom button
                    }
                });
            }
        }

        // Pre-select the "8-10" delivery time toggle button
        deliveryTimeToggleGroup.selectToggle(deliveryTime8To10Button);

        // Populate hour and AM/PM ComboBoxes
      //  for (int i = 1; i <= 12; i++) {
        hourComboBox.getItems().addAll("6", "7", "8", "9", "10", "11", "12", "1", "2", "3", "4"); // Add hours 1-12 to the ComboBox
 //       }
 //       ampmComboBox.getItems().addAll("am", "pm"); // Add AM and PM to the ComboBox

        customButton.setOnAction(event -> {
            boolean isSelected = customButton.isSelected();
            hourComboBox.setVisible(isSelected);
  //          ampmComboBox.setVisible(isSelected);
            if (!isSelected) {
                hourComboBox.getSelectionModel().clearSelection();
  //              ampmComboBox.getSelectionModel().clearSelection();
            }
        });

        siteField.textProperty().addListener((observable, oldValue, newValue) -> {
                        // Check if there's text in the field and update the style accordingly
            if (newValue.isEmpty()) {
                siteField.getStyleClass().remove("has-text");
                siteField.getStyleClass().add("empty-unfocused");
            } else {
                siteField.getStyleClass().remove("empty-unfocused");
                if (!siteField.getStyleClass().contains("has-text")) {
                    siteField.getStyleClass().add("has-text");
                }
            }
        });

        // Handle focus events for addressField
        siteField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                siteField.getStyleClass().remove("empty-unfocused"); // Remove empty style when focused
            } else if (siteField.getText().isEmpty()) {
                siteField.getStyleClass().add("empty-unfocused"); // Add empty style when focus is lost
            }
        });

        suggestionsBox.setPrefWidth(1);
        suggestionsBox.setMinWidth(1);
        suggestionsBox.setMaxWidth(1);
        suggestionsBox.setVisibleRowCount(5); // Set the number of visible rows

        // Listener for addressField text changes
        addressField.textProperty().addListener((observable, oldValue, newValue) -> {
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
                addressField.getStyleClass().remove("has-text");
                addressField.getStyleClass().add("empty-unfocused");
             //   suggestionsBox.setVisible(false); // Hide suggestions if empty
            } else {
                addressField.getStyleClass().remove("empty-unfocused");
                if (!addressField.getStyleClass().contains("has-text")) {
                    addressField.getStyleClass().add("has-text");
                }
            }
        });

        // Handle focus events for addressField
        addressField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
         //   suggestionsBox.setVisible(false);
            if (isNowFocused) {
                addressField.getStyleClass().remove("empty-unfocused"); // Remove empty style when focused
                //System.out.println("");
            } else if (addressField.getText().isEmpty()) {
                addressField.getStyleClass().add("empty-unfocused"); // Add empty style when focus is lost
          //      suggestionsBox.setVisible(false); // Hide suggestions when focus is lost and input is empty
            }
        });

        // Listener for the ComboBox to handle selection
        suggestionsBox.setOnAction(e -> {
            System.out.println("Action event on suggestionsBox");
            String selectedSuggestion = suggestionsBox.getValue();
            if (selectedSuggestion != null) {
                System.out.println("Selected suggestion: " + selectedSuggestion);
                String formattedAddress = formatSelectedSuggestion(selectedSuggestion);
                addressField.setText(formattedAddress); // Set selected suggestion in the TextField
                suggestionsBox.getSelectionModel().clearSelection(); // Clear selection to reset ComboBox
             //   suggestionsBox.setVisible(false);
                addressTypeCounter = 0;// Hide suggestions after selection
            }

        });

        // Optional: Set up an additional listener if you want to catch selection via valueProperty
        suggestionsBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                addressField.setText(newVal); // Populate addressField with selected suggestion
             //   suggestionsBox.setVisible(false);
                addressTypeCounter = 0;// Hide suggestions after selection
            }
        });

        // Handle Enter key press in addressField to confirm selection
        addressField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (suggestionsBox.isVisible() && suggestionsBox.getValue() != null) {
                    addressField.setText(suggestionsBox.getValue()); // Set selected suggestion
               //     suggestionsBox.setVisible(false);
                    addressTypeCounter = 0;// Hide suggestions on Enter key
                } else {
                    addressField.getParent().requestFocus(); // Move focus if no selection
                }
            }
        });

        siteField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {

                siteField.getParent().requestFocus(); // Move focus if no selection
            }
        });

        siteContactBox.setPrefWidth(1);
        siteContactBox.setMinWidth(1);
        siteContactBox.setMaxWidth(1);

         POField.textProperty().addListener((observable, oldValue, newValue) -> {
                        // Check if there's text in the field and update the style accordingly
            if (newValue.isEmpty()) {
                POField.getStyleClass().remove("has-text");
                POField.getStyleClass().add("empty-unfocused");
            } else {
                POField.getStyleClass().remove("empty-unfocused");
                if (!POField.getStyleClass().contains("has-text")) {
                    POField.getStyleClass().add("has-text");
                }
            }
        });

        // Handle focus events for addressField
        POField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                POField.getStyleClass().remove("empty-unfocused"); // Remove empty style when focused
            } else if (POField.getText().isEmpty()) {
                POField.getStyleClass().add("empty-unfocused"); // Add empty style when focus is lost
            }
        });

        createCustomTooltip(locationNotesButton, 38, 10, locationNotesTooltip);
        createCustomTooltip(preTripInstructionsButton, 38, 10, preTripInstructionsTooltip);

        setupTextFieldListeners(locationNotesField, locationNotesButton, locationNotesLabel);
        setupTextFieldListeners(preTripInstructionsField, preTripInstructionsButton, preTripInstructionsLabel);

        // Hide the status label initially
       // statusLabel.setVisible(false);

        serialNumberField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().length() > 6) {
                return null; // Reject the change
            }
            return change; // Accept the change
        }));

        if (MaxReachPro.getUser()[0] == "Byron Chilton") {
            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.applyStylesToButtons(liftTypeTilePane);
            buttonGroup.startRandomWalk();
        }

        serialNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Call your existing autofill logic
            autoFillSerialNumber(newValue);

            // Check if there's text in the field and update the style accordingly
            if (newValue.isEmpty()) {
                // No text, show the underline
                serialNumberField.getStyleClass().remove("has-text");
            } else {
                // Text is present, hide the underline
                if (!serialNumberField.getStyleClass().contains("has-text")) {
                    serialNumberField.getStyleClass().add("has-text");
                }
            }
        });


        expandedRental = MaxReachPro.getRentalForExpanding();
        prepareInvoiceArea();
        fillInFields();
    }

    private void fillInFields() {
       customerNameField.setText(expandedRental.getName());
       contractNumberField.setText("P" + String.valueOf(expandedRental.getRentalItemId()));
       System.out.println("Trying to fill out that the status is: " + expandedRental.getStatus());
       updateStatusToggleButtons(expandedRental.getStatus());
       orderedByField.setText(expandedRental.getOrderedByName());
       orderedByPhoneField.setText(expandedRental.getOrderedByPhone());
       if (expandedRental.isAutoTerm()) {
           autoTermButton.getStyleClass().add("schedule-delivery-button-has-value");
       }
       siteField.setText(expandedRental.getAddressBlockOne());
       addressField.setText(expandedRental.getAddressBlockTwo() + ", " + expandedRental.getCity());
       siteContactField.setText(expandedRental.getSiteContactName());
       siteContactPhoneField.setText(expandedRental.getSiteContactPhone());
       POField.setText(expandedRental.getPoNumber());

       if (expandedRental.getLocationNotes() != null) {
           if (!expandedRental.getLocationNotes().isEmpty()) {
               locationNotesField.setText(expandedRental.getLocationNotes());
               locationNotesButton.getStyleClass().add("schedule-delivery-button-has-value");
           }
       }

       if (expandedRental.getPreTripInstructions() != null) {
           if (!expandedRental.getPreTripInstructions().isEmpty()) {
               preTripInstructionsField.setText(expandedRental.getPreTripInstructions());
               preTripInstructionsButton.getStyleClass().add("schedule-delivery-button-has-value");
           }
       }

       updateWeekdayToggleButtons(deliveryWeeksRowTilePane, LocalDate.parse(expandedRental.getDeliveryDate()));
       if (expandedRental.getCallOffDate() == null) {
           updateWeekdayToggleButtons(callOffWeeksRowTilePane, null);
       } else {
           updateWeekdayToggleButtons(callOffWeeksRowTilePane, LocalDate.parse(expandedRental.getCallOffDate()));
       }

       setDeliveryTime(expandedRental.getDeliveryTime());
       setLiftType(expandedRental.getLiftType());
       serialNumberField.setText(expandedRental.getSerialNumber());

    }

    private void prepareInvoiceArea() {
        invoiceBox.setSpacing(0);
        invoiceBox.setAlignment(Pos.CENTER_LEFT);

        String imagePath = "/images/create-invoices.png";
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
        ImageView statusImage = new ImageView(image);
        statusImage.setFitHeight(30);
        statusImage.setPreserveRatio(true);

        Label statusLabel = new Label();
        if (expandedRental.isInvoiceWritten()) {
            Label checkSymbol = new Label("\u2713"); // Unicode checkmark symbol
            checkSymbol.setStyle("-fx-text-fill: green; -fx-font-size: 22px; -fx-padding: 0;");
            statusLabel = new Label(" Has invoice");
            invoiceBox.getChildren().addAll(statusImage, checkSymbol, statusLabel);
        } else {
            Label xSymbol = new Label("\u2717"); // Unicode X symbol
            xSymbol.setStyle("-fx-text-fill: red; -fx-font-size: 22px; -fx-padding: -2;");
            VBox labelBox = new VBox();
            labelBox.setSpacing(-4);
            Label statusLabelTop = new Label("   Needs");
            Label statusLabelBottom = new Label("   Invoice");
            statusLabelTop.setStyle("-fx-font-size: 12; -fx-padding: 0 -2 0 -5;");
            statusLabelBottom.setStyle("-fx-font-size: 12; -fx-padding: 0 -2 0 -5;");
            labelBox.getChildren().addAll(statusLabelTop, statusLabelBottom);
            statusLabelTop.setTranslateX(2);
            invoiceBox.getChildren().addAll(statusImage, xSymbol, labelBox);
        }
        statusLabel.setStyle("-fx-font-size: 12; -fx-padding: 0 -2 0 -5;");
        invoiceBox.setAlignment(Pos.CENTER);

        Label topInvoiceLabel = new Label("Mark as");
        topInvoiceLabel.setStyle("-fx-font-size: 12");
        Label bottomInvoiceLabel = new Label();
        if (expandedRental.isInvoiceWritten()) {
            bottomInvoiceLabel.setText("'Needs Invoice'");
        } else {
            bottomInvoiceLabel.setText("'Has Invoice'");
        }
        switchInvoiceLabelBox.getChildren().addAll(topInvoiceLabel, bottomInvoiceLabel);
        switchInvoiceLabelBox.setSpacing(-4);
        switchInvoiceLabelBox.setAlignment(Pos.CENTER);

        switchInvoiceButton.getStyleClass().remove("button");
        invoiceBox.setOnMouseEntered(event -> {
            switchInvoiceButton.setVisible(true);
            invoiceBox.setVisible(false);
            switchInvoiceButton.setStyle("-fx-background-color: orange;  -fx-background-radius: 5;");
            switchInvoiceButton.setPadding(new Insets(5));
            switchInvoiceButton.setLineSpacing(.5);
            switchInvoiceButton.setAlignment(Pos.CENTER);
            switchInvoiceButton.setWrapText(true);
            switchInvoiceButton.setOnMouseExited(e -> {
                invoiceBox.setVisible(true);
                switchInvoiceButton.setVisible(false);
                switchInvoiceButton.setStyle("-fx-background-color: #F4F4F4");
            });
        });




    }

    private void autoFillSerialNumber(String input) {
        selectedLift = null;

        ObservableList<String> matchingNumbers = FXCollections.observableArrayList();

        for (Lift lift : lifts) {
            if (lift.getSerialNumber().contains(input.toLowerCase())) {
                matchingNumbers.add(lift.getSerialNumber());
            }
        }

        if (matchingNumbers.size() == 1) {
            String matchedSerialNumber = matchingNumbers.get(0);
            serialNumberField.setText(matchedSerialNumber);
            customerNameField.getParent().requestFocus();
        }
    }

    private void autoFillCustomerName(String input) {
        selectedCustomer = null;

        if (input.isEmpty()) {
            orderedByBox.getItems().clear(); // Clear ComboBoxes
            siteContactBox.getItems().clear();
            return;
        }


        ObservableList<String> matchingNames = FXCollections.observableArrayList();


        for (Customer customer : customers) {
            if (customer.getName().toLowerCase().contains(input.toLowerCase())) {
                matchingNames.add(customer.getName());
            }
        }


        if (matchingNames.size() == 1) {
            // Auto-fill if there's a single match
            String matchedName = matchingNames.get(0);
            customerNameField.setText(matchedName);
            loadSelectedCustomerByName(matchedName); // Load customer object


            // Populate ComboBoxes with contacts for the matched customer
            populateComboBoxesForCustomer(selectedCustomer.getCustomerId());
            orderedByField.clear();
            orderedByPhoneField.clear();
            siteContactField.clear();
            siteContactPhoneField.clear();
            locationNotesField.clear();
            preTripInstructionsField.clear();
            locationNotesButton.getStyleClass().remove("schedule-delivery-button-has-value");
            preTripInstructionsButton.getStyleClass().remove("schedule-delivery-button-has-value");

            // Remove focus from the text field after auto-complete
            customerNameField.getParent().requestFocus();
        }
    }


    // Method to load the selected customer based on their name
    private void loadSelectedCustomerByName(String customerName) {
        for (Customer customer : customers) {
            if (customer.getName().equals(customerName)) {
                selectedCustomer = customer;  // Set the selected customer
                orderedByField.clear();
                orderedByPhoneField.clear();
                siteContactField.clear();
                siteContactPhoneField.clear();
                break;
            }
        }
    }



    // Method to populate ComboBoxes with contacts for the selected customer
    private void populateComboBoxesForCustomer(String customerId) {
       ObservableList<String> orderingContacts = FXCollections.observableArrayList();
       ObservableList<String> siteContacts = FXCollections.observableArrayList();

       String query = "SELECT first_name, phone_number, is_ordering_contact, is_site_contact, contact_id FROM contacts WHERE customer_id = ?";


       try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {


           preparedStatement.setString(1, customerId);
           try (ResultSet resultSet = preparedStatement.executeQuery()) {
               while (resultSet.next()) {
                   String contactName = resultSet.getString("first_name");
                   String phoneNumber = resultSet.getString("phone_number");
                   boolean isOrderingContact = resultSet.getBoolean("is_ordering_contact");
                   boolean isSiteContact = resultSet.getBoolean("is_site_contact");
                   String contactId = resultSet.getString("contact_id");


                   // Add to ComboBoxes and store phone numbers in the maps
                   if (isOrderingContact) {
                       orderingContacts.add(contactName);
                       for (Customer customer : customers) {
                            if (customer.getCustomerId().equals(customerId)) {
                                 customer.addOrderingContact(contactName, phoneNumber, contactId);
                                break;
                            }
                       }
                   }
                   if (isSiteContact) {
                       siteContacts.add(contactName);
                       for (Customer customer : customers) {
                          if (customer.getCustomerId().equals(customerId)) {
                               customer.addSiteContact(contactName, phoneNumber, contactId);
                               break;
                          }
                      }
                   }
               }
           }


           // Populate the ComboBoxes
           orderedByBox.setItems(orderingContacts);
           siteContactBox.setItems(siteContacts);


           // Add listeners to handle selection events
           orderedByBox.setOnAction(event -> handleContactSelection(orderedByBox, true)); // true for ordering
           siteContactBox.setOnAction(event -> handleContactSelection(siteContactBox, false)); // false for site


       } catch (SQLException e) {
           System.err.println("Error fetching contacts: " + e.getMessage());
           e.printStackTrace();
       }
    }


    // Method to handle contact selection and populate fields
    private void handleContactSelection(ComboBox<String> comboBox, boolean isOrderingContact) {
        String selectedContactName = comboBox.getValue();

       // Check if the selected contact name is not null
       if (selectedContactName != null) {

           Map<String, String> phoneNumbers = isOrderingContact
                   ? selectedCustomer.getOrderingContactsPhoneNumbers()
                   : selectedCustomer.getSiteContactsPhoneNumbers();
           Map<String, String> contactIds = isOrderingContact
                   ? selectedCustomer.getOrderingContactsIds()
                   : selectedCustomer.getSiteContactsIds();

           if (isOrderingContact) {
               orderedByField.setText(selectedContactName);  // Name
               orderedByPhoneField.setText(phoneNumbers.get(selectedContactName));    // Phone number
               selectedOrderingContactId = contactIds.get(selectedContactName); // Contact ID
           } else {
               siteContactField.setText(selectedContactName);  // Name
               siteContactPhoneField.setText(phoneNumbers.get(selectedContactName));     // Phone number
               selectedSiteContactId = contactIds.get(selectedContactName); // Contact ID
           }

       }

    }

    // Initialize toggle buttons in a given TilePane
    private void initializeWeekRowToggleButtons(TilePane tilePane, ToggleGroup toggleGroup) {
        for (javafx.scene.Node node : tilePane.getChildren()) {
            if (node instanceof ToggleButton) {
                ToggleButton toggleButton = (ToggleButton) node;
                toggleButton.setToggleGroup(toggleGroup); // Add each ToggleButton to the ToggleGroup

                // Add action for each button
                toggleButton.setOnAction(event -> {
                    toggleGroup.selectToggle(toggleButton); // Ensure the selected button is set
                });
            }
        }
    }



    private void setRentalDate(){

    }


    private void updateSuggestions(String input) {
        addressSuggestions.clear(); // Clear previous suggestions

        // Only make the request if the input is sufficiently long
        if (input.length() < 3) {
            suggestionsBox.getItems().setAll(addressSuggestions);
        //    suggestionsBox.setVisible(false);
            return;
        }

        String apiKey = "AIzaSyBN9kWbuL3QuZzONJfWKPX1-o0LG7eNisQ"; // Replace with your actual Google Places API key
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
            //    Platform.runLater(() -> suggestionsBox.setVisible(false)); // Hide suggestions on failure
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    // Parse the JSON response to extract suggestions
                    parseAddressSuggestions(jsonData);

                    // Update the suggestions box on the JavaFX Application Thread
                    Platform.runLater(() -> {
                        suggestionsBox.getItems().setAll(addressSuggestions);
                        if (addressTypeCounter > 3) {
                            suggestionsBox.setVisible(!addressSuggestions.isEmpty()); // Show suggestions if not empty
                            System.out.println("Suggestions Box visibility set to " + suggestionsBox.isVisible());
                            System.out.println("Number of suggestions: " + addressSuggestions.size());
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


    private Tooltip createCustomTooltip(Button button, double xOffset, double yOffset, Tooltip tooltip) {
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setHideDelay(Duration.ZERO);

        button.setOnMouseEntered(event -> {
            // Get the button's screen position
            double buttonX = button.localToScreen(button.getBoundsInLocal()).getMinX();
            double buttonY = button.localToScreen(button.getBoundsInLocal()).getMaxY();
            // Show tooltip with an offset relative to the button
            tooltip.show(button, buttonX + xOffset, buttonY + yOffset);
        });

        button.setOnMouseExited(event -> tooltip.hide());

        return tooltip;
    }

    private void updateStatusToggleButtons(String status) {
        for (Toggle toggle : statusToggleGroup.getToggles()) {
            if (toggle instanceof ToggleButton) {
                ToggleButton button = (ToggleButton) toggle;
                if (button.getText().equals(status)) {
                    statusToggleGroup.selectToggle(button);
                    return;
                }
            }
        }
    }

    // Method to update weekday toggle buttons for a specific TilePane and date
    private void updateWeekdayToggleButtons(TilePane tilePane, LocalDate date) {
        if (date == null) {
            noCallOffMemory = true;
            // Default to delivery date if no date is provided
            date = LocalDate.parse(expandedRental.getDeliveryDate());
        }

        // Determine the start of the week (Monday)
        LocalDate startOfWeek = date.with(java.time.temporal.ChronoField.DAY_OF_WEEK, 1);

        // Determine the suffix for button lookup (Del or End)
        String suffix = (tilePane == deliveryWeeksRowTilePane) ? "Del" : "End";

        // Lookup buttons in the TilePane
        ToggleButton monButton = (ToggleButton) tilePane.lookup("#monButton" + suffix);
        ToggleButton tueButton = (ToggleButton) tilePane.lookup("#tueButton" + suffix);
        ToggleButton wedButton = (ToggleButton) tilePane.lookup("#wedButton" + suffix);
        ToggleButton thuButton = (ToggleButton) tilePane.lookup("#thuButton" + suffix);
        ToggleButton friButton = (ToggleButton) tilePane.lookup("#friButton" + suffix);

        // Array of buttons for easier iteration
        ToggleButton[] buttons = {monButton, tueButton, wedButton, thuButton, friButton};

        // Update buttons with corresponding dates
        for (int i = 0; i < buttons.length; i++) {
            LocalDate buttonDate = startOfWeek.plusDays(i); // Calculate the date for this button


            // Set button text to date in M/d format
            if (buttons[i] != null) {
                buttons[i].setText(buttonDate.format(DateTimeFormatter.ofPattern("M/d")));

                // Highlight the button if it matches the provided date

                if (noCallOffMemory) {
                    buttons[i].getStyleClass().remove("lift-type-button-stopped");
                    buttons[i].getStyleClass().remove("lift-type-button-stopped");
                    buttons[i].getStyleClass().add("lift-type-button-dormant");
                    openCalendarEnd.getStyleClass().removeAll("schedule-delivery-button");
                    openCalendarEnd.getStyleClass().add("schedule-delivery-button-dormant");
                    openCalendarEnd.setOnAction(null);
                } else {
                    buttons[i].getStyleClass().remove("lift-type-button-dormant");
                    buttons[i].getStyleClass().remove("lift-type-button-dormant");
                    buttons[i].getStyleClass().add("lift-type-button-stopped");
                    openCalendarEnd.getStyleClass().add("schedule-delivery-button");
                    openCalendarEnd.getStyleClass().removeAll("schedule-delivery-button-dormant");
                    openCalendarEnd.setOnAction(this::handleOpenCalendar);
                    buttons[i].setSelected(buttonDate.equals(date));
                }
            }

            if (tilePane == deliveryWeeksRowTilePane) {
                buttons[i].setToggleGroup(weeksRowToggleGroup);
            } else {
                buttons[i].setToggleGroup(weeksRowToggleGroupAT);
            }
        }
    }


    // Method to get the correct TilePane based on the DatePicker
    private TilePane getTilePane(DatePicker datePicker) {
        if (datePicker == datePickerDel) {
            return deliveryWeeksRowTilePane; // For Delivery DatePicker
        } else if (datePicker == datePickerEnd) {
            return callOffWeeksRowTilePane; // For Call Off DatePicker
        }
        return null;
    }

    private LocalDate getAssumedCallOffDate() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        if (now.isBefore(Config.CUT_OFF_TIME)) {
            return getPreviousBusinessDay(today);
        } else {
            if (isBusinessDay(today)) {
                return today;
            } else {
                return getNextBusinessDay(today);
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

    private LocalDate getPreviousBusinessDay(LocalDate startDate) {
        LocalDate previousDate = startDate.minusDays(1);
        while (!isBusinessDay(previousDate)) {
            previousDate = previousDate.minusDays(1);
        }
        return previousDate;
    }

    private void setDeliveryTime (String time) {
        for (Toggle toggle : deliveryTimeToggleGroup.getToggles()) {
            if (toggle instanceof ToggleButton) {
                ToggleButton button = (ToggleButton) toggle;
                if (button.getText().equals(time)) {
                    deliveryTimeToggleGroup.selectToggle(button);
                    return;
                }
            }
        }
        deliveryTimeToggleGroup.selectToggle(customButton);
        hourComboBox.setVisible(true);
        for (String hour : hourComboBox.getItems()) {
            if (hour.equals(expandedRental.getDeliveryTime())) {
                hourComboBox.getSelectionModel().select(hour);
                break;
            }
        }
    }

    public int getLiftIdFromType(String liftType) {
        return Config.LIFT_TYPE_MAP.getOrDefault(liftType, -1);
    }

    private void setLiftType(String liftType) {

        // Find the corresponding button text
        String buttonText = Config.LIFT_BUTTON_TEXT_MAP.getOrDefault(liftType, null);
        if (buttonText == null) {
            System.err.println("No matching button text for lift type: " + liftType);
            return;
        }

        Platform.runLater(() -> {
            // Iterate through the toggles to match the button text
            for (Toggle toggle : liftTypeToggleGroup.getToggles()) {
                if (toggle instanceof ToggleButton) {
                    ToggleButton button = (ToggleButton) toggle;
                    if (button.getText().equals(buttonText)) {
                        liftTypeToggleGroup.selectToggle(button); // Ensure the selected button is set
                        button.setSelected(true);
                    } else {
                        button.setSelected(false);
                    }
                }
            }
        });
    }

    @FXML
    public void handleOpenCalendar(ActionEvent event) {
        Button sourceButton = (Button) event.getSource(); // Get the source of the event

        // Check which button was clicked (Delivery or End)
        if (sourceButton == openCalendarDel) {
            if (!isCalendarExpanded) {
                openCalendar(datePickerDel); // Open the Delivery DatePicker
            } else {
                closeCalendar(datePickerDel); // Close the Delivery DatePicker
            }
        } else if (sourceButton == openCalendarEnd) {
            if (!isCalendarExpanded) {
                openCalendar(datePickerEnd); // Open the Call Off DatePicker
            } else {
                closeCalendar(datePickerEnd); // Close the Call Off DatePicker
            }
        }
    }




    // Method to open the calendar for the specific DatePicker
    public void openCalendar(DatePicker datePicker) {
        datePicker.show();
        isCalendarExpanded = true;
        datePicker.requestFocus(); // Focus on the DatePicker


        // When a date is selected, update the buttons and close the calendar
        datePicker.setOnAction(event -> {
            LocalDate selectedDate = datePicker.getValue();
            if (selectedDate != null) {
                dateSelected = true;
                updateWeekdayToggleButtons(getTilePane(datePicker), selectedDate);
                isCalendarExpanded = false;
            }
        });


        // Hide the calendar when focus is lost (if no date is selected)
        datePicker.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && !dateSelected) {
                closeCalendar(datePicker);
            }
        });
    }


    // Method to close the calendar for the specific DatePicker
    private void closeCalendar(DatePicker datePicker) {
        isCalendarExpanded = false;
        // Optionally, show a cover for the calendar
        if (datePicker == datePickerDel) {
            calendarCoverDel.setVisible(true);
        } else if (datePicker == datePickerEnd) {
            calendarCoverEnd.setVisible(true);
        }
    }

    // Method to setup each DatePicker with the relevant parameters
    private void setupDatePicker(DatePicker datePicker, TilePane weeksRowTilePane, String suffix, Rectangle calendarCover) {
        // Event listener for DatePicker to show the calendar when focused
        datePicker.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                openCalendar(datePicker); // Call openCalendar when DatePicker is focused
            } else if (!dateSelected) {
                closeCalendar(datePicker); // Call closeCalendar when focus is lost
            }
        });


        // Disable weekends for both DatePickers (Delivery and Call Off)
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && (item.getDayOfWeek() == DayOfWeek.SATURDAY || item.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                    setDisable(true); // Disable weekends
                    setStyle("-fx-background-color: lightgrey;"); // Optional: change background color for disabled dates
                }
            }
        });
    }



    private Toggle getNextToggle(Toggle currentToggle) {
        Toggle nextToggle = null;

        if (currentToggle == null) {
            return liftTypeToggleGroup.getToggles().get(0); // If none is selected, return the first
        }

        int currentIndex = liftTypeToggleGroup.getToggles().indexOf(currentToggle);
        int nextIndex = (currentIndex + 1) % liftTypeToggleGroup.getToggles().size(); // Wrap around
        nextToggle = liftTypeToggleGroup.getToggles().get(nextIndex);

        return nextToggle;
    }

    @FXML
    private void handleAutoTerm(){
        if (expandedRental.isAutoTerm()) {
            autoTermButton.getStyleClass().remove("schedule-delivery-button-has-value");
            expandedRental.setAutoTerm(false);
            if (expandedRental.getStatus().equals("Upcoming") || expandedRental.getStatus().equals("Active")) {
                updateWeekdayToggleButtons(callOffWeeksRowTilePane, null);
            }
        } else {
            autoTermButton.getStyleClass().add("schedule-delivery-button-has-value");
            expandedRental.setAutoTerm(true);
            if (expandedRental.getStatus().equals("Upcoming") || expandedRental.getStatus().equals("Active")) {
                LocalDate assumedCallOffDate = getAssumedCallOffDate();
                noCallOffMemory = false;
                updateWeekdayToggleButtons(callOffWeeksRowTilePane, assumedCallOffDate);
                expandedRental.setCallOffDate(getAssumedCallOffDate().toString());
            }
        }
    }

    @FXML
    private void handleLocationNotes(){
        toggleDedicatedField(locationNotesButton, locationNotesLabel, locationNotesField);
        POLabel.setVisible(false);
    }

    @FXML
    private void handlePreTripInstructions(){
        toggleDedicatedField(preTripInstructionsButton, preTripInstructionsLabel, preTripInstructionsField);
        POLabel.setVisible(false);
    }

    private void toggleDedicatedField(Button button, Label label, TextField textField){
        boolean isDedicatedFieldVisible = textField.isVisible();

        POLabel.setVisible(isDedicatedFieldVisible);
        POField.setVisible(isDedicatedFieldVisible);
        locationNotesButton.setVisible(isDedicatedFieldVisible && button != locationNotesButton);
        preTripInstructionsButton.setVisible(isDedicatedFieldVisible && button != preTripInstructionsButton);

        label.setVisible(!isDedicatedFieldVisible);
        textField.setVisible(!isDedicatedFieldVisible);

        if (!isDedicatedFieldVisible){
           // button.setLayoutX(14);
            textField.requestFocus();
            if (!textField.getText().isEmpty()) {
                textField.positionCaret(textField.getText().length());
            }
        } else {
           // button.setLayoutX(button == locationNotesButton ? 207 : 258);
            if (!textField.getText().isEmpty()) {
                button.getStyleClass().add("schedule-delivery-button-has-value");
            } else {
                button.getStyleClass().remove("schedule-delivery-button-has-value");
            }
            preTripInstructionsButton.setVisible(true);
            locationNotesButton.setVisible(true);
        }
    }

    @FXML
    private void handleSwitchInvoice() {
        invoiceBox.getChildren().clear();
        String imagePath = "/images/create-invoices.png";
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
        ImageView statusImage = new ImageView(image);
        statusImage.setFitHeight(30);
        statusImage.setPreserveRatio(true);

        Label statusLabel = new Label();

        Label topInvoiceLabel = new Label("Mark as");
        topInvoiceLabel.setStyle("-fx-font-size: 12");
        Label bottomInvoiceLabel = new Label();

        if (expandedRental.isInvoiceWritten()) {
            Label xSymbol = new Label("\u2717"); // Unicode X symbol
            xSymbol.setStyle("-fx-text-fill: red; -fx-font-size: 22px; -fx-padding: -2;");
            VBox labelBox = new VBox();
            labelBox.setSpacing(-4);
            Label statusLabelTop = new Label("   Needs");
            Label statusLabelBottom = new Label("   Invoice");
            statusLabelTop.setStyle("-fx-font-size: 12; -fx-padding: 0 -2 0 -5;");
            statusLabelBottom.setStyle("-fx-font-size: 12; -fx-padding: 0 -2 0 -5;");
            labelBox.getChildren().addAll(statusLabelTop, statusLabelBottom);
            statusLabelTop.setTranslateX(2);
            invoiceBox.getChildren().addAll(statusImage, xSymbol, labelBox);
            invoiceBox.setAlignment(Pos.CENTER);
            bottomInvoiceLabel.setText("'Has Invoice'");
        } else {
            Label checkSymbol = new Label("\u2713"); // Unicode checkmark symbol
            checkSymbol.setStyle("-fx-text-fill: green; -fx-font-size: 22px; -fx-padding: 0;");
            statusLabel = new Label(" Has invoice");
            invoiceBox.getChildren().addAll(statusImage, checkSymbol, statusLabel);
            bottomInvoiceLabel.setText("'Needs Invoice'");
        }
        statusLabel.setStyle("-fx-font-size: 12; -fx-padding: 0 -2 0 -5; -fx-spacing: -4;");
        switchInvoiceLabelBox.getChildren().clear();
        switchInvoiceLabelBox.getChildren().addAll(topInvoiceLabel, bottomInvoiceLabel);
        switchInvoiceLabelBox.setSpacing(-4);
        switchInvoiceLabelBox.setAlignment(Pos.CENTER);
        expandedRental.setInvoiceWritten(!expandedRental.isInvoiceWritten());
    }

    private void prepareLiftTypeButtons(){
        for (Toggle toggle : liftTypeToggleGroup.getToggles()) {
            if (toggle instanceof ToggleButton) {
                ToggleButton toggleButton = (ToggleButton) toggle;
                toggleButton.getStyleClass().add("lift-type-button-stopped");
            }
        }
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
    private void handleUpdateRental() {
       System.out.println("handleUpdateRental called");
       // Chunk of code for getting page settings
       String deliveryDate = getDateForDB(weeksRowToggleGroup);
       String callOffDate = "";
       if (!noCallOffMemory) {
           System.out.println("boolean noCallOffMemory remembers that there's a call off date");
           callOffDate = getDateForDB(weeksRowToggleGroupAT);
           System.out.println("call off date derived from buttons is: " + callOffDate);
       }
       String deliveryTime;
       if (customButton.isSelected()) {
           String selectedHour = hourComboBox.getSelectionModel().getSelectedItem();
           if (selectedHour == null /* || selectedAmPm == null */) {
               statusLabel.setText("Please select a custom delivery time."); // Show error message
               statusLabel.setTextFill(Color.RED); // Set the text color to red
               statusLabel.setVisible(true); // Make the status label visible
               return; // Exit the method early
           }
           deliveryTime = selectedHour /* + " " + selectedAmPm */; // Construct delivery time string
       } else {
           ToggleButton selectedDeliveryTimeButton = (ToggleButton) deliveryTimeToggleGroup.getSelectedToggle();
           if (selectedDeliveryTimeButton == null) {
               statusLabel.setText("Please select a delivery time."); // Show error message
               statusLabel.setTextFill(Color.RED); // Set the text color to red
               statusLabel.setVisible(true); // Make the status label visible
               return; // Exit the method early
           }
           deliveryTime = selectedDeliveryTimeButton.getText(); // Get the selected delivery time
       }

       int rentalItemId = expandedRental.getRentalItemId();
       String customerName = customerNameField.getText();
       String status = expandedRental.getStatus();
       String orderedBy = orderedByField.getText() == null ? "" : orderedByField.getText();
       String orderedByPhone = orderedByPhoneField.getText() == null ? "" : orderedByPhoneField.getText();
       String site = siteField.getText() == null ? "" : siteField.getText();
       String address = addressField.getText();
       String[] addressParts = getAddressParts(address);
       String streetAddress = addressParts[0];
       String city = addressParts[1];
       String siteContact = siteContactField.getText() == null ? "" : siteContactField.getText();
       String siteContactPhone = siteContactPhoneField.getText() == null ? "" : siteContactPhoneField.getText();
       String poNumber = POField.getText();
       String locationNotes = locationNotesField.getText();
       String preTripInstructions = preTripInstructionsField.getText();
       int invoiceWritten = expandedRental.isInvoiceWritten() ? 1 : 0 ;
       int autoTerm = expandedRental.isAutoTerm() ? 1 : 0 ;

       // Note: going to comment out addedLifts centric code but anticipate needing it for the add lifts scene

       // Process addedLifts in reverse order
       //while (!addedLifts.isEmpty()) {
           // Get the last element in addedLifts
       //    String latestLift = addedLifts.get(addedLifts.size() - 1);


           // Match the ToggleGroup to the corresponding button text
        //   for (Toggle toggle : liftTypeToggleGroup.getToggles()) {
        //       if (toggle instanceof ToggleButton) {
        //           ToggleButton button = (ToggleButton) toggle;
        //           if (button.getText().equals(latestLift)) {
        //               liftTypeToggleGroup.selectToggle(button);
         //              break;
        //           }
         //      }
         //  }

           // Determine if the current lift is the "base" lift
       //    boolean isBaseLift = addedLifts.size() == 1;
       //    System.out.println("about to call rentalItemSQLCalls and isBaseLift = " + isBaseLift);
           // Call rentalItemSQLCalls with the isBaseLift flag
           rentalItemSQLCalls(true, deliveryDate, callOffDate, deliveryTime, rentalItemId, customerName, orderedBy,
                   orderedByPhone, site, streetAddress, city, siteContact, siteContactPhone, poNumber, locationNotes,
                   preTripInstructions, address, invoiceWritten, autoTerm, status);

           // Remove the last element from addedLifts
        //   addedLifts.remove(addedLifts.size() - 1);
      // }

       // Re-add a single element to addedLifts based on the rental's lift type
      // String newLift = Config.LIFT_BUTTON_TEXT_MAP.getOrDefault(expandedRental.getLiftType(), "1");
       //addedLifts.add(newLift);
    }



    private void rentalItemSQLCalls(boolean isBaseLift, String deliveryDate, String callOffDate, String deliveryTime, int rentalItemId,
                                    String customerName, String orderedBy, String orderedByPhone, String site, String streetAddress,
                                    String city, String siteContact, String siteContactPhone, String poNumber, String locationNotes,
                                    String preTripInstructions, String address, int invoiceWritten, int autoTerm, String status){
        System.out.println("deliveryDate var is: " + deliveryDate);
        statusLabel.setText("");
        int rentalOrderId = expandedRental.getRentalOrderId();

        String liftType = liftTypeToggleGroup.getSelectedToggle() != null ? ((ToggleButton) liftTypeToggleGroup.getSelectedToggle()).getText() : Config.LIFT_BUTTON_TEXT_MAP.getOrDefault(expandedRental.getLiftType(), "");

        String checkOrdersTableQuery = """
                SELECT 
                    ri.*, 
                    ro.customer_id, ro.po_number, ro.site_name, ro.street_address, ro.city, 
                    oc.first_name AS ordered_contact_first_name, 
                    oc.phone_number AS ordered_contact_phone_number, 
                    sc.first_name AS site_contact_first_name, 
                    sc.phone_number AS site_contact_phone_number,
                    l.lift_id, l.serial_number
                FROM rental_items ri 
                INNER JOIN rental_orders ro ON ri.rental_order_id = ro.rental_order_id 
                INNER JOIN lifts l ON ri.lift_id = l.lift_id
                LEFT JOIN contacts oc ON ri.ordered_contact_id = oc.contact_id 
                LEFT JOIN contacts sc ON ri.site_contact_id = sc.contact_id 
                WHERE ri.rental_item_id = ?
                """;

        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
                PreparedStatement preparedStatement = connection.prepareStatement(checkOrdersTableQuery)) {

                preparedStatement.setInt(1, rentalItemId);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    String customerId = safeGetString(resultSet, "customer_id");
                    String dbPoNumber = safeGetString(resultSet, "po_number");
                    String dbSiteName = safeGetString(resultSet, "site_name");
                    String dbStreetAddress = safeGetString(resultSet, "street_address");
                    String dbCity = safeGetString(resultSet, "city");
                    String dbFullAddress = (dbStreetAddress + ", " + dbCity).replace(" CO", "");
                    String dbStatus = safeGetString(resultSet, "item_status");
                    String dbOrderedBy = safeGetString(resultSet, "ordered_contact_first_name");
                    String dbOrderedByPhone = safeGetString(resultSet, "ordered_contact_phone_number");
                    String dbSiteContact = safeGetString(resultSet, "site_contact_first_name");
                    String dbSiteContactPhone = safeGetString(resultSet, "site_contact_phone_number");
                    String dbLocationNotes = safeGetString(resultSet, "location_notes");
                    String dbPreTripInstructions = safeGetString(resultSet, "pre_trip_instructions");
                    String dbDeliveryDate = resultSet.getDate("item_delivery_date").toLocalDate()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    Date medCallOffDate = resultSet.getDate("Item_call_off_date");
                    String dbCallOffDate = "";
                    if (medCallOffDate != null) {
                        dbCallOffDate = !noCallOffMemory ? resultSet.getDate("item_call_off_date").toLocalDate()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null;
                    }
                    String dbDeliveryTime = safeGetString(resultSet, "delivery_time");
                    int dbIsInvoiceWritten = resultSet.getInt("invoice_composed");
                    int dbAutoTerm = resultSet.getInt("auto_term");
                    String dbSerialNumber = safeGetString(resultSet, "serial_number");

                    if (selectedCustomer != null) {
                        if (!customerId.equals(selectedCustomer.getCustomerId())) {
                            statusLabel.setText("Customer ID mismatch");
                            statusLabel.setVisible(true);
                        }
                    }

                    boolean customerMatch = true;
                    boolean anyCustomer = false;
                    if (selectedCustomer != null) {
                        System.out.println(String.valueOf(selectedCustomer.getCustomerId() + " " + selectedCustomer.getName()));
                        if (!selectedCustomer.getCustomerId().equals(customerId)) {
                            customerMatch = false;
                        }
                    } else {
                        customerMatch = false;
                        System.out.println(String.valueOf(expandedRental.getCustomerId() + " " + expandedRental.getName()));
                        for (Customer customer : customers) {
                            if (customer.getCustomerName().equals(customerName)) {
                                customerMatch = true;
                                customer = selectedCustomer;
                                anyCustomer = true;
                            }
                        }
                        if (!anyCustomer) {
                            statusLabel.setText("New customer additions currently only supported in Quickbooks");
                            statusLabel.setVisible(true);
                            return;
                        }
                    }

                    if (!dbPoNumber.equals(poNumber) || !dbSiteName.equals(site) || !dbFullAddress.equals(address) || !customerMatch) {

                        String createRentalOrderQuery = """
                                INSERT INTO rental_orders (customer_id, po_number, site_name, street_address, city, order_date, delivery_date)
                                VALUES (?, ?, ?, ?, ?, ?, ?)
                                """;
                                try (PreparedStatement createOrderStmt = connection.prepareStatement(createRentalOrderQuery, Statement.RETURN_GENERATED_KEYS)) {
                                    createOrderStmt.setString(1, selectedCustomer.getCustomerId());
                                    createOrderStmt.setString(2, poNumber);
                                    createOrderStmt.setString(3, site);
                                    createOrderStmt.setString(4, streetAddress);
                                    createOrderStmt.setString(5, city);
                                    createOrderStmt.setDate(6, Date.valueOf(LocalDate.now()));
                                    createOrderStmt.setString(7, deliveryDate);

                                    createOrderStmt.executeUpdate();

                                    try (ResultSet generatedKeys = createOrderStmt.getGeneratedKeys()) {
                                        if (generatedKeys.next()) {
                                            rentalOrderId = generatedKeys.getInt(1);
                                            expandedRental.setRentalOrderId(rentalOrderId);
                                            System.out.println("Generated rental order ID: " + rentalOrderId);

                                            statusLabel.setText("Order updated. ");
                                            statusLabel.setVisible(true);

                                            if (isBaseLift) {
                                                String updateRentalItemQuery = """
                                                        UPDATE rental_items
                                                        SET rental_order_id = ?
                                                        WHERE rental_item_id = ?
                                                        """;
                                                try (PreparedStatement updateRentalItemStmt = connection.prepareStatement(updateRentalItemQuery)) {
                                                    updateRentalItemStmt.setInt(1, rentalOrderId);
                                                    updateRentalItemStmt.setInt(2, rentalItemId);
                                                    updateRentalItemStmt.executeUpdate();
                                                } catch (SQLException e) {
                                                    e.printStackTrace();
                                                    statusLabel.setText("Error updating rental item: " + e.getMessage());
                                                    statusLabel.setVisible(true);
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                    statusLabel.setText("Error creating rental order: " + e.getMessage());
                                    statusLabel.setVisible(true);
                                    return;
                                }

                    } else {
                        System.out.println("passed the order components match check");
                    }

                    if (isBaseLift) {
                        // just for the base, otherwise insert a new item with the current order id
                        if (!dbOrderedBy.equals(orderedBy) || !dbOrderedByPhone.equals(orderedByPhone) || !dbSiteContact.equals(siteContact)
                                || !dbSiteContactPhone.equals(siteContactPhone) || !dbLocationNotes.equals(locationNotes)
                                || !dbPreTripInstructions.equals(preTripInstructions) || !dbDeliveryTime.equals(deliveryTime)
                                || !dbDeliveryDate.equals(deliveryDate) || !dbCallOffDate.equals(callOffDate)
                                || dbIsInvoiceWritten != invoiceWritten || dbAutoTerm != autoTerm || !dbStatus.equals(status)) {

                            // Log specific mismatches
                           if (!dbOrderedBy.equals(orderedBy)) {
                               System.out.println("Mismatch: dbOrderedBy (" + dbOrderedBy + ") != orderedBy (" + orderedBy + ")");
                           }
                           if (!dbOrderedByPhone.equals(orderedByPhone)) {
                               System.out.println("Mismatch: dbOrderedByPhone (" + dbOrderedByPhone + ") != orderedByPhone (" + orderedByPhone + ")");
                           }
                           if (!dbSiteContact.equals(siteContact)) {
                               System.out.println("Mismatch: dbSiteContact (" + dbSiteContact + ") != siteContact (" + siteContact + ")");
                           }
                           if (!dbSiteContactPhone.equals(siteContactPhone)) {
                               System.out.println("Mismatch: dbSiteContactPhone (" + dbSiteContactPhone + ") != siteContactPhone (" + siteContactPhone + ")");
                           }
                           if (!dbLocationNotes.equals(locationNotes)) {
                               System.out.println("Mismatch: dbLocationNotes (" + dbLocationNotes + ") != locationNotes (" + locationNotes + ")");
                           }
                           if (!dbPreTripInstructions.equals(preTripInstructions)) {
                               System.out.println("Mismatch: dbPreTripInstructions (" + dbPreTripInstructions + ") != preTripInstructions (" + preTripInstructions + ")");
                           }
                           if (!dbDeliveryDate.equals(deliveryDate)) {
                               System.out.println("Mismatch: dbDeliveryDate (" + dbDeliveryDate + ") != deliveryDate (" + deliveryDate + ")");
                           }
                           if (!dbCallOffDate.equals(callOffDate)) {
                               System.out.println("Mismatch: dbCallOffDate (" + dbCallOffDate + ") != callOffDate (" + callOffDate + ")");
                           }
                           if (!dbDeliveryTime.equals(deliveryTime)) {
                               System.out.println("Mismatch: dbDeliveryTime (" + dbDeliveryTime + ") != deliveryTime (" + deliveryTime + ")");
                           }


                            String updateRentalItemQuery = """
                            
                            UPDATE rental_items
                            SET rental_order_id = ?,
                                ordered_contact_id = ?,
                                site_contact_id = ?,
                                lift_id = ?,
                                item_delivery_date = ?,
                                item_call_off_date = ?,
                                delivery_time = ?,
                                location_notes = ?,
                                pre_trip_instructions = ?,
                                item_status = ?,
                                invoice_composed = ?,
                                auto_term = ?    
                            WHERE rental_item_id = ?
                            """;

                           System.out.println("About to engage updateRentalItemStatement and deliveryDate is: " +
                                   deliveryDate + ", and callOffDate is: " + callOffDate + ", and noCallOffMemory is: " +
                                   noCallOffMemory);

                            try (PreparedStatement updateRentalItemStmt = connection.prepareStatement(updateRentalItemQuery)) {
                                updateRentalItemStmt.setInt(1, rentalOrderId);
                                updateRentalItemStmt.setString(2, selectedOrderingContactId);
                                updateRentalItemStmt.setString(3, selectedSiteContactId);
                                updateRentalItemStmt.setInt(4, getLiftIdFromType(liftType)); // Adjust index if necessary
                                updateRentalItemStmt.setDate(5, java.sql.Date.valueOf(deliveryDate));
                                if (callOffDate != null && callOffDate != "") {
                                    updateRentalItemStmt.setDate(6, java.sql.Date.valueOf(callOffDate));
                                } else {
                                    updateRentalItemStmt.setNull(6, Types.VARCHAR);
                                }
                                updateRentalItemStmt.setString(7, deliveryTime);
                                updateRentalItemStmt.setString(8, locationNotes);
                                updateRentalItemStmt.setString(9, preTripInstructions);
                                updateRentalItemStmt.setString(10, status);
                                updateRentalItemStmt.setInt(11, invoiceWritten);
                                updateRentalItemStmt.setInt(12, autoTerm);
                                updateRentalItemStmt.setInt(13, rentalItemId); // Match the rental_item_id

                                updateRentalItemStmt.executeUpdate();

                                statusLabel.setText("Item updated. " + statusLabel.getText());
                                statusLabel.setVisible(true);
                            } catch (SQLException e) {
                                e.printStackTrace();
                                statusLabel.setText("Error updating rental item: " + e.getMessage());
                                statusLabel.setVisible(true);
                                return;
                            }

                        } else {
                            System.out.println("passed the item components match check");
                        }
                        String serialNumber = serialNumberField.getText();
                        if (!dbSerialNumber.equals(serialNumber)) {
                            updateSerialNumberInDB(rentalItemId, serialNumber);
                            expandedRental.setSerialNumber(serialNumber);
                        }
                    }

                }

            } catch (SQLException e) {
                e.printStackTrace();
                statusLabel.setText("Error checking orders table: " + e.getMessage());
                statusLabel.setVisible(true);
                return;
            }
        System.out.println("End of checkpoint 1");

        if (!isBaseLift) {
            // Insert a whole new rental item if not baseLift
            String createRentalItemQuery = """
                    INSERT INTO rental_items (rental_order_id, lift_id, ordered_contact_id, site_contact_id,
                          item_delivery_date, item_call_off_date, delivery_time, customer_ref_number, location_notes,
                          pre_trip_instructions, item_order_date, item_status, invoice_composed, auto_term)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;

            try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
                 PreparedStatement preparedStatement = connection.prepareStatement(createRentalItemQuery)) {

                // Set the parameters for the prepared statement
                preparedStatement.setInt(1, rentalOrderId); // rental_order_id
                preparedStatement.setInt(2, getLiftIdFromType(liftType)); // lift_id
                preparedStatement.setString(3, selectedOrderingContactId); // ordered_contact_id
                preparedStatement.setString(4, selectedSiteContactId); // site_contact_id
                preparedStatement.setString(5, deliveryDate); // item_delivery_date
                preparedStatement.setString(6, callOffDate);
                preparedStatement.setString(7, deliveryTime); // delivery_time
                preparedStatement.setString(8, poNumber); // customer_ref_number
                preparedStatement.setString(9, locationNotes); // location_notes
                preparedStatement.setString(10, preTripInstructions); // pre_trip_instructions
                preparedStatement.setDate(11, new java.sql.Date(System.currentTimeMillis()));
                preparedStatement.setString(12, status);
                preparedStatement.setInt(13, invoiceWritten);
                preparedStatement.setInt(14, autoTerm);

                // Execute the query
                preparedStatement.executeUpdate();
                System.out.println("New rental item inserted successfully.");

                statusLabel.setText("New item inserted. " + statusLabel.getText());
                statusLabel.setVisible(true);
            } catch (SQLException e) {
                e.printStackTrace();
                statusLabel.setText("Error inserting new rental item: " + e.getMessage());
                statusLabel.setVisible(true);
                return;
            }

        }

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

    private String[] getAddressParts(String address) {
        String[] parts = address.split(",");

        if (parts.length < 2){
            throw new IllegalArgumentException("address must contain both a street and city separated by a comma");
        }

        String street = parts[0].trim();
        String city = parts[1].trim();

        return new String[]{street, city};
    }

    private void updateSerialNumberInDB(int rentalItemId, String serialNumber) {
       if (!checkSerialNumberExists(serialNumber)) {
           System.out.println("The provided serial number does not exist in the database.");
           return;
       }

       String updateQuery = """
          UPDATE rental_items
          SET lift_id = (
              SELECT lift_id
              FROM lifts
              WHERE serial_number = ?
              LIMIT 1        
          )
          WHERE rental_item_id = ?;
       """;

       try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
            PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {

           // Set the parameters for the query
           preparedStatement.setString(1, serialNumber);
           preparedStatement.setInt(2, rentalItemId);

           // Execute the update
           int rowsAffected = preparedStatement.executeUpdate();
           if (rowsAffected > 0) {
               statusLabel.setText("Serial updated. " + statusLabel.getText());
               System.out.println("Rental item updated successfully.");
           } else {
               System.out.println("No matching rental item found or serial number does not exist.");
           }

       } catch (SQLException e) {
           System.err.println("Error while updating the rental item: " + e.getMessage());
           e.printStackTrace();
       }
    }

    private boolean checkSerialNumberExists(String serialNumber) {
        String checkQuery = "SELECT COUNT(*) FROM lifts WHERE serial_number = ?;";
        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             PreparedStatement preparedStatement = connection.prepareStatement(checkQuery)) {
            preparedStatement.setString(1, serialNumber);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String safeGetString(ResultSet resultSet, String columnName) throws SQLException {
        String value = resultSet.getString(columnName);
        return (value == null) ? "" : value;
    }

    @FXML
    public void handleBack() {
        try {
            MaxReachPro.goBack("/fxml/expand.fxml");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double getTotalHeight() {
        return 222;
    }



}

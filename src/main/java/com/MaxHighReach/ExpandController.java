package com.MaxHighReach;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.TilePane;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpandController extends BaseController {

    private ObservableList<Customer> customers = FXCollections.observableArrayList();
    private Customer selectedCustomer;
    private boolean dateSelected = false;
    private int rentalOrderId;
    private CustomerRental currentCustomerRental;

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
    private TextField rentalDateField; // Hidden text field for rental date
    @FXML
    private DatePicker datePicker; // For the calendar view
    @FXML
    private TilePane weekViewTilePane; // To show the week view
    @FXML
    private Label[] dayLabels; // Array to hold the day labels for week view

    private boolean isCalendarExpanded = false;
    @FXML
    private TilePane weeksRowTilePane;
    @FXML
    private Rectangle calendarCover;
    @FXML
    private TilePane liftTypeTilePane;  // TilePane containing the lift type toggle buttons
    @FXML
    private ToggleButton twelveMastButton;
    @FXML
    private Button plusButton;
    @FXML
    private Line liftTypeUnderLine;
    private int liftCount = 0;
    private List<String> addedLifts = new ArrayList<>();
    @FXML
    private Button xButton;
    @FXML
    private Label liftCountLabel;
    @FXML
    private TilePane deliveryTimeTilePane;  // TilePane for delivery time toggle buttons

    private ToggleGroup liftTypeToggleGroup;  // To ensure only one lift type can be selected at a time
    private ToggleGroup deliveryTimeToggleGroup;  // To ensure only one delivery time can be selected at a time
    private ToggleGroup weeksRowToggleGroup;


    @FXML
    private ToggleButton deliveryTime8To10Button; // Reference to the "8-10" toggle button
    @FXML
    public Button calendarButton;
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
    private Button updateRentalButton;
    @FXML
    private Label statusLabel; // Reference to the status label

    private Timeline rotationTimeline; // Timeline for rotating highlight
    private boolean isRotating = false;
    private boolean suggestionMuter = false;// Flag to track rotation state
    private int addressTypeCounter = 0;
    private ObservableList<String> addressSuggestions = FXCollections.observableArrayList();
    private OkHttpClient client = new OkHttpClient();

    private CustomerRental expandedRental;


    public void initialize() {
        loadCustomers();
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

        // Event listener for DatePicker
        datePicker.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                openCalendar(); // Call openCalendar when DatePicker is focused
            } else if (!dateSelected) {
                closeCalendar(); // Call closeCalendar when focus is lost
            }
        });

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

        // Initialize weeksRowTilePane toggle buttons
        for (javafx.scene.Node node : weeksRowTilePane.getChildren()) {
            if (node instanceof ToggleButton) {
                ToggleButton toggleButton = (ToggleButton) node;
                toggleButton.setToggleGroup(weeksRowToggleGroup);  // Add each ToggleButton to the ToggleGroup

                // Add action for each button
                toggleButton.setOnAction(event -> {
                    weeksRowToggleGroup.selectToggle(toggleButton); // Ensure the selected button is set
                });
            }
        }

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


        if (MaxReachPro.getUser()[0] == "Byron Chilton") {
            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.applyStylesToButtons(liftTypeTilePane);
            buttonGroup.startRandomWalk();
        }

        expandedRental = MaxReachPro.getRentalForExpanding();
        fillInFields();
    }

    private void loadCustomers() {
        String query = "SELECT customer_id, customer_name, email FROM customers";

        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String customerId = resultSet.getString("customer_id");
                String customer_name = resultSet.getString("customer_name");
                String email = resultSet.getString("email");

                // Add to the customer list
                customers.add(new Customer(customerId, customer_name, email));
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading customers: " + e.getMessage());
            statusLabel.setVisible(true);
        }
    }

    private void fillInFields() {
        customerNameField.setText(expandedRental.getName());
        orderedByField.setText(expandedRental.getOrderedByName());
        orderedByPhoneField.setText(expandedRental.getOrderedByPhone());
        siteField.setText(expandedRental.getAddressBlockOne());
        addressField.setText(expandedRental.getAddressBlockTwo() + ", " + expandedRental.getCity());
        siteContactField.setText(expandedRental.getSiteContactName());
        siteContactPhoneField.setText(expandedRental.getSiteContactPhone());
        POField.setText(expandedRental.getPoNumber());
        if (expandedRental.getLocationNotes() != null) {
            locationNotesField.setText(expandedRental.getLocationNotes());
            locationNotesButton.getStyleClass().add("schedule-delivery-button-has-value");
        }
        if (expandedRental.getPreTripInstructions() != null) {
            preTripInstructionsField.setText(expandedRental.getPreTripInstructions());
            preTripInstructionsButton.getStyleClass().add("schedule-delivery-button-has-value");
        }
        updateWeekdayToggleButtons(LocalDate.parse(expandedRental.getDeliveryDate()));
        setDeliveryTime(expandedRental.getDeliveryTime());
        setLiftType(expandedRental.getLiftType(), 1);
        plusButton.setVisible(true);
        liftCountLabel.setVisible(true);
        liftCount++;
        liftCountLabel.setText(String.valueOf(liftCount));
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


    private void setRentalDate(){

    }


    @FXML
    private void handlePlus(){
        ToggleButton selectedLiftTypeButton = (ToggleButton) liftTypeToggleGroup.getSelectedToggle();
        if (selectedLiftTypeButton != null) {
            String liftType = selectedLiftTypeButton.getText();
            addedLifts.add(liftType);
            {
                statusLabel.setText(addedLifts.toString());
                statusLabel.setVisible(true);
            }
            liftCount++;
        }
        liftCountLabel.setVisible(true);
        xButton.setVisible(true);
        liftCountLabel.setText(String.valueOf(liftCount));
        liftTypeToggleGroup.selectToggle(twelveMastButton);
        setLiftType(expandedRental.getLiftType(), 0);
        startHighlightRotation();
        rotationTimeline.play();
        isRotating = true;
        prepareLiftTypeButtons();
        plusButton.setVisible(false);
    }

    @FXML
    private void handleX(){
        liftCount--;
        if (addedLifts.size() > 0) {
            addedLifts.remove(addedLifts.size() - 1);
        }
            {
                statusLabel.setText(addedLifts.toString());
                statusLabel.setVisible(true);
            }
        liftCountLabel.setText(String.valueOf(liftCount));
        if (liftCount == 0){
            liftCountLabel.setVisible(false);
        }
        if (liftCount == 1) {
            xButton.setVisible(false);
        }
        if (isRotating){
            plusButton.setVisible(false);
        }

    }


    private void updateSuggestions(String input) {
        System.out.println("updateSuggestions called");
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

    private void updateWeekdayToggleButtons(LocalDate date) {
        LocalDate startOfWeek = date.with(java.time.temporal.ChronoField.DAY_OF_WEEK, 1); // Monday

        // Update the weekday buttons with the corresponding dates
        ToggleButton monButton = (ToggleButton) weeksRowTilePane.lookup("#monButton");
        ToggleButton tueButton = (ToggleButton) weeksRowTilePane.lookup("#tueButton");
        ToggleButton wedButton = (ToggleButton) weeksRowTilePane.lookup("#wedButton");
        ToggleButton thuButton = (ToggleButton) weeksRowTilePane.lookup("#thuButton");
        ToggleButton friButton = (ToggleButton) weeksRowTilePane.lookup("#friButton");

        ToggleButton[] buttons = {monButton, tueButton, wedButton, thuButton, friButton};

        for (int i = 0; i < buttons.length; i++) {
            LocalDate buttonDate = startOfWeek.plusDays(i); // Calculate the date for this button
            buttons[i].setText(buttonDate.format(DateTimeFormatter.ofPattern("M/d"))); // Set text to M/d format

            // Select the button representing the nextWeekday
            if (buttonDate.equals(date)) {
                buttons[i].setSelected(true);
            } else {
                buttons[i].setSelected(false);
            }
        }
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

    private void setLiftType(String liftType, int direction) {

        // Find the corresponding button text
        String buttonText = Config.LIFT_BUTTON_TEXT_MAP.getOrDefault(liftType, null);
        if (buttonText == null) {
            System.err.println("No matching button text for lift type: " + liftType);
            return;
        }

        if (direction == 1) {
            liftTypeUnderLine.setVisible(true);
        } else {
            liftTypeUnderLine.setVisible(false);
        }

        Platform.runLater(() -> {
            // Iterate through the toggles to match the button text
            for (Toggle toggle : liftTypeToggleGroup.getToggles()) {
                if (toggle instanceof ToggleButton) {
                    ToggleButton button = (ToggleButton) toggle;
                    if (direction == 0) {
                        button.setVisible(true);
                    }
                    if (button.getText().equals(buttonText)) {
                        double offsetX = 0;
                        double offsetY = 0;
                        if (buttonText == "12' Mast") {

                        } else if (buttonText == "19' Slim" ) {
                            offsetX = -59;
                        } else if (buttonText == "26' Slim") {
                            offsetX = -118;
                        } else if (buttonText == "26'") {
                            offsetX = -177;
                        } else if (buttonText == "32'") {
                            offsetX = 0;
                            offsetY = -22;
                        } else if (buttonText == "40'") {
                            offsetX = -59;
                            offsetY = -22;
                        } else if (buttonText == "33' RT") {
                            offsetX = -118;
                            offsetY = -22;
                        } else if (buttonText == "45' Boom") {
                            offsetX = -160;
                            offsetY = -22;
                        }
                        offsetX = offsetX * direction;
                        offsetY = offsetY * direction;
                        button.translateXProperty().set(offsetX);
                        button.translateYProperty().set(offsetY);
                        button.setOnAction(event ->{
                        if (isRotating) {
                            rotationTimeline.stop();
                            isRotating = false; // Set isRotating to false
                        }

                            button.getStyleClass().removeAll("lift-type-button-rotating");
                            button.getStyleClass().add("lift-type-button-stopped");
                            button.setSelected(true);
                            liftTypeToggleGroup.selectToggle(button); // Ensure the selected button is set

                        plusButton.setVisible(true);
                        });

                    } else {
                        if (direction == 1) {
                            button.setVisible(false);
                        }
                    }
                }
            }
        });

        if (direction == 1) {
            addedLifts.add(buttonText);
            {
                statusLabel.setText(addedLifts.toString());
                statusLabel.setVisible(true);
            }
        }
    }

    @FXML
    public void handleOpenCalendar() {
        if (!isCalendarExpanded) {
            openCalendar(); // Show the calendar
        } else {
            closeCalendar(); // Hide the calendar
        }
         // Toggle the state
    }

    public void openCalendar() {
        datePicker.show();
        isCalendarExpanded = true;
        // Hide the calendarCover when the DatePicker is opened
        /*calendarCover.setVisible(false);
        for (Toggle toggle : weeksRowToggleGroup.getToggles()) {
            if (toggle instanceof ToggleButton) {
                ((ToggleButton) toggle).setVisible(false); // Hide each ToggleButton
            }
        }*/

        // Show the DatePicker when the calendar button is clicked
        //dateSelected = false;
        //datePicker.setVisible(true);
        datePicker.requestFocus(); // Focus on the DatePicker

        // When a date is selected, update the hidden buttons and reset the DatePicker
        datePicker.setOnAction(event -> {
            LocalDate selectedDate = datePicker.getValue();
            if (selectedDate != null){
                dateSelected = true;
                updateWeekdayToggleButtons(selectedDate);
                isCalendarExpanded = false;
            }

        });

        // Optionally hide the DatePicker if the user clicks outside or cancels the operation
        datePicker.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                if (!dateSelected) {
                    closeCalendar();
                }
            }
        });
    }


    private void closeCalendar() {
        isCalendarExpanded = false;
        calendarCover.setVisible(true);
        for (Toggle toggle : weeksRowToggleGroup.getToggles()) {
            if (toggle instanceof ToggleButton) {
                ((ToggleButton) toggle).setVisible(true); // Hide each ToggleButton
            }
        }
    }


    private void startHighlightRotation() {
        for (javafx.scene.Node node : liftTypeTilePane.getChildren()) {
            node.getStyleClass().removeAll("lift-type-button-stopped");
            node.getStyleClass().add("lift-type-button-rotating");
        }

        rotationTimeline = new Timeline();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1.5), event -> {
            Toggle selectedToggle = liftTypeToggleGroup.getSelectedToggle();
            if (!isRotating) {
                System.out.println("not rotatting within a keyFrame called.");
                rotationTimeline.stop();
                isRotating = false; // Set isRotating to false
                for (javafx.scene.Node node : liftTypeTilePane.getChildren()) {
                    node.getStyleClass().removeAll("lift-type-button-rotating");
                    node.getStyleClass().add("lift-type-button-stopped");
                    node.setStyle("-fx-background-color: orange");
                }
            }
            Toggle nextToggle = getNextToggle(selectedToggle);
            liftTypeToggleGroup.selectToggle(nextToggle);
        });

        rotationTimeline.getKeyFrames().add(keyFrame);
        rotationTimeline.setCycleCount(Timeline.INDEFINITE); // Set to repeat indefinitely
        rotationTimeline.play();
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

    private void prepareLiftTypeButtons(){
        for (Toggle toggle : liftTypeToggleGroup.getToggles()) {
            if (toggle instanceof ToggleButton) {
                ToggleButton toggleButton = (ToggleButton) toggle;
                toggleButton.getStyleClass().removeAll("lift-type-button-stopped");
                toggleButton.getStyleClass().add("lift-type-button-rotating");
                toggleButton.setOnAction(event ->{
                    if (isRotating) {
                        rotationTimeline.stop();
                        isRotating = false; // Set isRotating to false
                    }
                    toggleButton.getStyleClass().removeAll("lift-type-button-rotating");
                    toggleButton.getStyleClass().add("lift-type-button-stopped");
                    liftTypeToggleGroup.selectToggle(toggleButton); // Ensure the selected button is set
                    plusButton.setVisible(true);
                });
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
        // chunk of code for getting page settings
        ToggleButton selectedWeekdayButton = (ToggleButton) weeksRowToggleGroup.getSelectedToggle();
            if (selectedWeekdayButton == null) {
                statusLabel.setText("Please select a weekday."); // Show error message
                statusLabel.setTextFill(Color.RED);
                statusLabel.setVisible(true);
                return; // Exit the method early
            }
        String selectedWeekdayText = selectedWeekdayButton.getText();
        LocalDate selectedDate = LocalDate.now(); // Initialize to today
        int currentYear = selectedDate.getYear(); // Get the current year
        String[] parts = selectedWeekdayText.split("/");
        int month = Integer.parseInt(parts[0]); // Get the month
        int day = Integer.parseInt(parts[1]); // Get the day
        selectedDate = LocalDate.of(currentYear, month, day); // Creates a LocalDate object
        if (selectedDate.isBefore(LocalDate.now())) { // If the selected date is before today
            selectedDate = selectedDate.plusYears(1); // Move to next year
        }
        if (selectedDate.getDayOfWeek() == DayOfWeek.SATURDAY || selectedDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            statusLabel.setText("Selected date cannot be on a weekend."); // Show error message
            statusLabel.setTextFill(Color.RED);
            statusLabel.setVisible(true);
            return; // Exit the method early
        }
        String deliveryDate = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // For database
        String deliveryTime;
        if (customButton.isSelected()) {
            String selectedHour = hourComboBox.getSelectionModel().getSelectedItem();
            if (selectedHour == null /*|| selectedAmPm == null*/) {
                statusLabel.setText("Please select a custom delivery time."); // Show error message
                statusLabel.setTextFill(Color.RED); // Set the text color to red
                statusLabel.setVisible(true); // Make the status label visible
                return; // Exit the method early
            }
            deliveryTime = selectedHour/* + " " + selectedAmPm*/; // Construct delivery time string
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
        String orderedBy = orderedByField.getText();
        String orderedByPhone = orderedByPhoneField.getText();
        String site = siteField.getText();
        String address = addressField.getText();
        String addressParts[] = getAddressParts(address);
        String streetAddress = addressParts[0];
        String city = addressParts[1];
        String siteContact = siteContactField.getText();
        String siteContactPhone = siteContactPhoneField.getText();
        String poNumber = POField.getText();
        String locationNotes = locationNotesField.getText();
        String preTripInstructions = preTripInstructionsField.getText();


        // Process addedLifts in reverse order
        while (!addedLifts.isEmpty()) {
            // Get the last element in addedLifts
            String latestLift = addedLifts.get(addedLifts.size() - 1);

            // Match the ToggleGroup to the corresponding button text
            for (Toggle toggle : liftTypeToggleGroup.getToggles()) {
                if (toggle instanceof ToggleButton) {
                    ToggleButton button = (ToggleButton) toggle;
                    if (button.getText().equals(latestLift)) {
                        liftTypeToggleGroup.selectToggle(button);
                        break;
                    }
                }
            }

            // Determine if the current lift is the "base" lift
            boolean isBaseLift = addedLifts.size() == 1;

            // Call handleUpdateRental with the isBaseLift flag
            rentalItemSQLCalls(isBaseLift, deliveryDate, deliveryTime, rentalItemId, customerName, orderedBy,
                    orderedByPhone, site, streetAddress, city, siteContact, siteContactPhone, poNumber, locationNotes,
                    preTripInstructions, address);

            // Remove the last element from addedLifts
            addedLifts.remove(addedLifts.size() - 1);
        }

        // Re-add a single element to addedLifts based on the rental's lift type
        String newLift = Config.LIFT_BUTTON_TEXT_MAP.getOrDefault(expandedRental.getLiftType(), "1");
        addedLifts.add(newLift);
    }


    private void rentalItemSQLCalls(boolean isBaseLift, String deliveryDate, String deliveryTime, int rentalItemId,
                                    String customerName, String orderedBy, String orderedByPhone, String site, String streetAddress,
                                    String city, String siteContact, String siteContactPhone, String poNumber, String locationNotes,
                                    String preTripInstructions, String address){

        int rentalOrderId = expandedRental.getRentalOrderId();

        String liftType = liftTypeToggleGroup.getSelectedToggle() != null ? ((ToggleButton) liftTypeToggleGroup.getSelectedToggle()).getText() : Config.LIFT_BUTTON_TEXT_MAP.getOrDefault(expandedRental.getLiftType(), "");

        String checkOrdersTableQuery = """
                SELECT 
                    ri.*, 
                    ro.customer_id, ro.po_number, ro.site_name, ro.street_address, ro.city, 
                    oc.first_name AS ordered_contact_first_name, 
                    oc.phone_number AS ordered_contact_phone_number, 
                    sc.first_name AS site_contact_first_name, 
                    sc.phone_number AS site_contact_phone_number 
                FROM rental_items ri 
                INNER JOIN rental_orders ro ON ri.rental_order_id = ro.rental_order_id 
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

                    String dbOrderedBy = safeGetString(resultSet, "ordered_contact_first_name");
                    String dbOrderedByPhone = safeGetString(resultSet, "ordered_contact_phone_number");
                    String dbSiteContact = safeGetString(resultSet, "site_contact_first_name");
                    String dbSiteContactPhone = safeGetString(resultSet, "site_contact_phone_number");
                    String dbLocationNotes = safeGetString(resultSet, "location_notes");
                    String dbPreTripInstructions = safeGetString(resultSet, "pre_trip_instructions");
                    String dbDeliveryTime = safeGetString(resultSet, "delivery_time");

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
                        statusLabel.setText("Order details mismatch");
                        statusLabel.setVisible(true);

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

                    }

                    if (isBaseLift) {
                        // just for the base, otherwise insert a new item with the current order id
                        if (!dbOrderedBy.equals(orderedBy) || !dbOrderedByPhone.equals(orderedByPhone) || !dbSiteContact.equals(siteContact)
                                || !dbSiteContactPhone.equals(siteContactPhone) || !dbLocationNotes.equals(locationNotes)
                                || !dbPreTripInstructions.equals(preTripInstructions) || !dbDeliveryTime.equals(deliveryTime)) {
                            statusLabel.setText("Item mismatch");

                            String updateRentalItemQuery = """
                            
                            UPDATE rental_items
                            SET rental_order_id = ?,
                                ordered_contact_id = ?,
                                site_contact_id = ?,
                                lift_id = ?,
                                delivery_time = ?,
                                location_notes = ?,
                                pre_trip_instructions = ?
                            WHERE rental_item_id = ?
                            """;


                            try (PreparedStatement updateRentalItemStmt = connection.prepareStatement(updateRentalItemQuery)) {
                                updateRentalItemStmt.setInt(1, rentalOrderId);
                                updateRentalItemStmt.setString(2, selectedOrderingContactId);
                                updateRentalItemStmt.setString(3, selectedSiteContactId);
                                updateRentalItemStmt.setInt(4, getLiftIdFromType(liftType)); // Adjust index if necessary
                                updateRentalItemStmt.setString(5, deliveryTime);
                                updateRentalItemStmt.setString(6, locationNotes);
                                updateRentalItemStmt.setString(7, preTripInstructions);
                                updateRentalItemStmt.setInt(8, rentalItemId); // Match the rental_item_id

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
                statusLabel.setText("Error checking orders table: " + e.getMessage());
                statusLabel.setVisible(true);
                return;
            }
        System.out.println("End of checkpoint 1");

        if (!isBaseLift) {
            // Insert a whole new rental item if not baseLift
            String createRentalItemQuery = """
                    INSERT INTO rental_items (rental_order_id, lift_id, ordered_contact_id, site_contact_id, item_delivery_date, delivery_time, customer_ref_number, location_notes, pre_trip_instructions, item_order_date)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;

            try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
                 PreparedStatement preparedStatement = connection.prepareStatement(createRentalItemQuery)) {

                // Set the parameters for the prepared statement
                preparedStatement.setInt(1, rentalOrderId); // rental_order_id
                preparedStatement.setInt(2, getLiftIdFromType(liftType)); // lift_id
                preparedStatement.setString(3, selectedOrderingContactId); // ordered_contact_id
                preparedStatement.setString(4, selectedSiteContactId); // site_contact_id
                preparedStatement.setString(5, deliveryDate); // item_delivery_date
                preparedStatement.setString(6, deliveryTime); // delivery_time
                preparedStatement.setString(7, poNumber); // customer_ref_number
                preparedStatement.setString(8, locationNotes); // location_notes
                preparedStatement.setString(9, preTripInstructions); // pre_trip_instructions
                preparedStatement.setDate(10, new java.sql.Date(System.currentTimeMillis()));

                // Execute the query
                preparedStatement.executeUpdate();
                System.out.println("New rental item inserted successfully.");

            } catch (SQLException e) {
                e.printStackTrace();
                statusLabel.setText("Error inserting new rental item: " + e.getMessage());
                statusLabel.setVisible(true);
                return;
            }
        }

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
        return 350;
    }



}

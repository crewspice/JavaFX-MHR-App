package com.MaxHighReach;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToggleButton;
import javafx.util.Duration;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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

public class ScheduleDeliveryController extends BaseController {

    private static final double INITIAL_SCISSOR_LIFT_HEIGHT = 263; // Initial height of the scissor lift
    private static final double INITIAL_TABLE_HEIGHT = 50; // Initial height of the TableView (height of one row)
    private static final double ROW_HEIGHT = 25; // Height of each row in the TableView
    private double currentHeight = INITIAL_SCISSOR_LIFT_HEIGHT; // Track the current height of the scissor lift
    private int scheduledCounter = 0;

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
    private Button scheduleDeliveryButton;
    @FXML
    private Label statusLabel; // Reference to the status label

    private Timeline rotationTimeline; // Timeline for rotating highlight
    private boolean isRotating = false;
    private boolean suggestionMuter = false;// Flag to track rotation state
    private int addressTypeCounter = 0;
    private ObservableList<String> addressSuggestions = FXCollections.observableArrayList();
    private OkHttpClient client = new OkHttpClient();

    // Initialize method to set up ToggleButtons and the ToggleGroup
    @FXML
    public void initialize() {
        loadCustomers();
        setDefaultRentalDate();

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

        customerNameField.focusedProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue) {
                handleCustomerNameFieldUnfocus();
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

        // Initialize TableView columns
        customerIdColumn.setCellValueFactory(cellData -> cellData.getValue().customerIdProperty());
        rentalDateColumn.setCellValueFactory(cellData -> cellData.getValue().orderDateProperty());
        liftTypeColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        deliveryTimeColumn.setCellValueFactory(cellData -> cellData.getValue().deliveryTimeProperty()); // Bind delivery time

        // Initially load scheduled rentals for the current session
        scheduledRentalsTableView.setItems(rentalsList); // Bind the rentalsList to the TableView

        // Adjust the TableView height to fit one empty row initially
        adjustTableViewHeight(); // Set initial height of the TableView

        // Hide the status label initially
        statusLabel.setVisible(false);

        // Start rotating highlight for lift type toggle buttons
        startHighlightRotation();

        if (MaxReachPro.getUser()[0] == "Byron Chilton") {
            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.applyStylesToButtons(liftTypeTilePane);
            buttonGroup.startRandomWalk();
        }

    }


// Method to load customers from the SQL database
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

    private void autoFillCustomerName(String input) {
        if (input.isEmpty()) {
            selectedCustomer = null; // Clear the selected customer if the input is empty
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

    private void handleCustomerNameFieldUnfocus() {
        String input = customerNameField.getText();
        boolean isValidCustomer = false;


        if (!input.isEmpty()) {
            for (Customer customer : customers) {
                if (customer.getName().equalsIgnoreCase(input)) {
                    isValidCustomer = true;
                    break;
                }
            }


            if (!isValidCustomer && input != "") {
                scheduleDeliveryButton.setText("Add New Customer: " + input);
            } else {
                scheduleDeliveryButton.setText("Schedule Delivery");
            }
        }
    }



    private boolean isCustomerValid(String customerId) {
       System.out.println("checking customer " + customerId); // Check if any Customer in the customers list has a matching customerId
        for (Customer customer : customers) {
            if (customer.getCustomerId().equals(customerId)) {
                return true; // Found a matching customerId
            }
        }
        return false; // No matching customerId found
    }

    private boolean isPhoneNumberValid(String phoneNumber){
        phoneNumber = phoneNumber.trim(); // Remove leading and trailing whitespace
        String digitsOnly = phoneNumber.replaceAll("[^0-9]", ""); // Remove all non-numeric characters
        return digitsOnly.length() == 10;
    }

    private void setDefaultRentalDate() {
        // Set rental date to the next weekday (Mon-Fri)
        LocalDate today = LocalDate.now();
        LocalDate nextWeekday = today.plusDays(1);
        while (nextWeekday.getDayOfWeek().getValue() > 5) { // Skip Saturday and Sunday
            nextWeekday = nextWeekday.plusDays(1);
        }

        // Update the ToggleButton texts and select the nextWeekday
        updateWeekdayToggleButtons(nextWeekday);
    }

    // Method to update the ToggleButtons based on the next weekday
    private void updateWeekdayToggleButtons(LocalDate nextWeekday) {
        LocalDate startOfWeek = nextWeekday.with(java.time.temporal.ChronoField.DAY_OF_WEEK, 1); // Monday

        // Update the weekday buttons with the corresponding dates
        ToggleButton monButton = (ToggleButton) weeksRowTilePane.lookup("#monButton");
        ToggleButton tueButton = (ToggleButton) weeksRowTilePane.lookup("#tueButton");
        ToggleButton wedButton = (ToggleButton) weeksRowTilePane.lookup("#wedButton");
        ToggleButton thuButton = (ToggleButton) weeksRowTilePane.lookup("#thuButton");
        ToggleButton friButton = (ToggleButton) weeksRowTilePane.lookup("#friButton");

        // Create an array to hold the buttons and their respective dates
        ToggleButton[] buttons = {monButton, tueButton, wedButton, thuButton, friButton};

        for (int i = 0; i < buttons.length; i++) {
            LocalDate buttonDate = startOfWeek.plusDays(i); // Calculate the date for this button
            buttons[i].setText(buttonDate.format(DateTimeFormatter.ofPattern("M/d"))); // Set text to M/d format

            // Select the button representing the nextWeekday
            if (buttonDate.equals(nextWeekday)) {
                buttons[i].setSelected(true);
            } else {
                buttons[i].setSelected(false);
            }
        }
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
        isRotating = true; // Set isRotating to true

        rotationTimeline = new Timeline();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1.5), event -> {
            Toggle selectedToggle = liftTypeToggleGroup.getSelectedToggle();
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
    private void handlePlus(){
        liftCount++;
        ToggleButton selectedLiftTypeButton = (ToggleButton) liftTypeToggleGroup.getSelectedToggle();
        String liftType = selectedLiftTypeButton.getText();
        addedLifts.add(liftType);
        liftCountLabel.setVisible(true);
        xButton.setVisible(true);
        liftCountLabel.setText(String.valueOf(liftCount));
        liftTypeToggleGroup.selectToggle(twelveMastButton);
        rotationTimeline.play();
        isRotating = true;
        prepareLiftTypeButtons();
        plusButton.setVisible(false);
    }

    @FXML
    private void handleX(){
        liftCount--;
        addedLifts.remove(addedLifts.size() - 1);
        liftCountLabel.setText(String.valueOf(liftCount));
        if (liftCount == 0){
            liftCountLabel.setVisible(false);
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

    // Method to adjust the height of the TableView based on the number of entries
    private void adjustTableViewHeight() {
        double newHeight = INITIAL_TABLE_HEIGHT + (ROW_HEIGHT * Math.max(0, rentalsList.size()));
        scheduledRentalsTableView.setPrefHeight(newHeight);
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

    @FXML
    public void handleOpenCalendar() {
        if (!isCalendarExpanded) {
            openCalendar(); // Show the calendar
        } else {
            closeCalendar(); // Hide the calendar
        }
         // Toggle the state
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

    private void setupTextFieldListeners(TextField textField, Button button, Label label) {
        textField.setOnAction(e -> toggleDedicatedField(button, label, textField));
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                toggleDedicatedField(button, label, textField);
            }
        });
    }

    @FXML
    public void handleScheduleDelivery() {
        if (!scheduleDeliveryButton.getText().equals("Schedule Delivery")){
            handleAddNewCustomer();
            return;
        }

        ToggleButton selectedLiftTypeButton = (ToggleButton) liftTypeToggleGroup.getSelectedToggle();
            if (selectedLiftTypeButton == null && liftCount == 0) {
                statusLabel.setText("Please select a lift type."); // Show error message
                statusLabel.setTextFill(Color.RED);
                statusLabel.setVisible(true);
                return; // Exit the method early
            }


        try {
            String customerId = selectedCustomer.getCustomerId(); // Get customer ID as String
            String customerName = selectedCustomer.getName(); // Get customer name
            // Check if the customerId is in the customers list
            if (!isCustomerValid(customerId)) {
                statusLabel.setText("Invalid customer ID. Please select a valid customer."); // Show error message
                statusLabel.setTextFill(Color.RED);
                statusLabel.setVisible(true);
                return; // Exit the method early
            }

            ToggleButton selectedWeekdayButton = (ToggleButton) weeksRowToggleGroup.getSelectedToggle();
            if (selectedWeekdayButton == null) {
                statusLabel.setText("Please select a weekday."); // Show error message
                statusLabel.setTextFill(Color.RED);
                statusLabel.setVisible(true);
                return; // Exit the method early
            }

            // Get the text of the selected weekday button (e.g., "10/1")
            String selectedWeekdayText = selectedWeekdayButton.getText();
            LocalDate selectedDate = LocalDate.now(); // Initialize to today
            int currentYear = selectedDate.getYear(); // Get the current year

            // Split the text to get month and day
            String[] parts = selectedWeekdayText.split("/");
            int month = Integer.parseInt(parts[0]); // Get the month
            int day = Integer.parseInt(parts[1]); // Get the day

            // Construct the LocalDate with the current month and day
            selectedDate = LocalDate.of(currentYear, month, day); // Creates a LocalDate object

            // Determine the appropriate year based on the selected date
            if (selectedDate.isBefore(LocalDate.now())) { // If the selected date is before today
                selectedDate = selectedDate.plusYears(1); // Move to next year
            }

            // Check if the selected date falls on a weekend and handle it if needed
            if (selectedDate.getDayOfWeek() == DayOfWeek.SATURDAY || selectedDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                statusLabel.setText("Selected date cannot be on a weekend."); // Show error message
                statusLabel.setTextFill(Color.RED);
                statusLabel.setVisible(true);
                return; // Exit the method early
            }

            // Format the date to "MMM-dd"
            String deliveryDate = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // For database
            String formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("MMM-dd")); // For display



             // Get the text of the selected button (lift type)

            // Check if the custom delivery time is selected
            String deliveryTime;
            if (customButton.isSelected()) {
                String selectedHour = hourComboBox.getSelectionModel().getSelectedItem();
   //             String selectedAmPm = ampmComboBox.getSelectionModel().getSelectedItem();
                if (selectedHour == null /*|| selectedAmPm == null*/) {
                    // Update the status label for input error
                    statusLabel.setText("Please select a custom delivery time."); // Show error message
                    statusLabel.setTextFill(Color.RED); // Set the text color to red
                    statusLabel.setVisible(true); // Make the status label visible
                    return; // Exit the method early
                }
                deliveryTime = selectedHour/* + " " + selectedAmPm*/; // Construct delivery time string
            } else {
                // Get the selected ToggleButton for delivery time
                ToggleButton selectedDeliveryTimeButton = (ToggleButton) deliveryTimeToggleGroup.getSelectedToggle();
                if (selectedDeliveryTimeButton == null) {
                    // Update the status label for input error
                    statusLabel.setText("Please select a delivery time."); // Show error message
                    statusLabel.setTextFill(Color.RED); // Set the text color to red
                    statusLabel.setVisible(true); // Make the status label visible
                    return; // Exit the method early
                }
                deliveryTime = selectedDeliveryTimeButton.getText(); // Get the selected delivery time
            }

            LocalDate today = LocalDate.now(); // Get the current date
            String orderDate = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            String site = siteField.getText();

            String address = addressField.getText();
            String addressParts[] = getAddressParts(address); // Format the address for the database
            String streetAddress = addressParts[0]; // Get the street address
            String city = addressParts[1]; // Get the city

            String po = POField.getText();

            currentCustomerRental = new CustomerRental(customerId, customerName, formattedDate, deliveryTime, "", "Upcoming", "99999", rentalsList.size() + 1);
            currentCustomerRental.setAddressBlockTwo(streetAddress);
            currentCustomerRental.setAddressBlockThree(city);
            boolean rentalOrderScheduled = false;
            if (insertRentalOrder(customerId, deliveryDate, orderDate, site, streetAddress, city, po)) {
                rentalOrderScheduled = true;
                // Update the status label for successful scheduling
                System.out.println("Rental order scheduled successfully!"); // For debugging
                statusLabel.setText("Rental order scheduled successfully!"); // Show success message
                statusLabel.setTextFill(Color.GREEN); // Set the text color to green
                statusLabel.setVisible(true); // Make the status label visible

                // Add the newly scheduled rental to the rentalsList for this session
                currentCustomerRental.setRentalOrderId(rentalOrderId); // Set the rental_order_id


                // Reset fields after successful scheduling
            } else {
                statusLabel.setText("Failed to schedule a new Rental Order."); // Show error message
                    statusLabel.setTextFill(Color.RED); // Set the text color to red
                    statusLabel.setVisible(true); // Make the status label visible
                    return;
            }
            if (!isRotating){
                addedLifts.add(selectedLiftTypeButton.getText());
                liftCount++;
            }
            System.out.println("Lift count: " + liftCount);
            System.out.println("Size of addedLifts is: " + addedLifts.size());
            System.out.println("Scheduled counter before iteration: " + scheduledCounter);
            boolean insertedRentalItem = false;
            for (int i = 0; i < liftCount; i++){
                System.out.println("iteration number: " + i);
                String liftType =  addedLifts.get(i); // Get the text of the selected button
                int liftId = getLiftIdFromType(liftType);
                currentCustomerRental.setLiftType(liftType);
                currentCustomerRental.setLiftId(liftId);

                if (insertRentalItem(rentalOrderId, liftId, currentCustomerRental.getOrderDate(), deliveryDate, deliveryTime, po) && rentalOrderScheduled) {
                    // Update the status label for successful scheduling
                    System.out.println("Rental item created successfully!"); // For debugging
                    statusLabel.setText("Rental item created successfully!"); // Show success message
                    statusLabel.setTextFill(Color.GREEN); // Set the text color to green
                    statusLabel.setVisible(true); // Make the status label visible


                    rentalsList.add(currentCustomerRental);


                    scheduledCounter++;

                    adjustTableViewHeight(); // Adjust the TableView height after adding a new entry

                    // Animate the scissor lift down by decrementing its height
                    //currentHeight -= 50; // Decrease height
                    //MaxReachPro.getScissorLift().animateTransition(currentHeight + ROW_HEIGHT, currentHeight); // Animate the lift

                    adjustTableViewHeight(); // Adjust the TableView height after adding a new entry
                    // Reset fields after successful scheduling
                    insertedRentalItem = true;
                } else {
                    System.out.println("Failed to insert rental item for liftId: " + liftId); // For debugging
                    // Update the status label for rental failure
                    statusLabel.setText("Failed to create the rental item. Please try again."); // Show error message
                    statusLabel.setTextFill(Color.RED); // Set the text color to red
                    statusLabel.setVisible(true); // Make the status label visible
                }
            }
            if (insertedRentalItem){
                MaxReachPro.getScissorLift().animateTransition(this.getTotalHeight());
                resetFields();
                scheduledRentalsTableView.setVisible(true);
                tableViewTitle.setVisible(true);
            }
        } catch (Exception e) {
            // Update the status label for input error
            statusLabel.setText("Invalid input: " + e.getMessage()); // Show error message
            statusLabel.setTextFill(Color.RED); // Set the text color to red
            statusLabel.setVisible(true); // Make the status label visible
        }

    }

    private boolean insertRentalOrder(String customerId, String deliveryDate, String orderDate, String site, String streetAddress, String city, String po) {
        String query = "INSERT INTO rental_orders (customer_id, delivery_date, order_date, site_name, street_address, city, po_number) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             PreparedStatement preparedStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, customerId); // Change to setString
            preparedStatement.setString(2, deliveryDate);
            preparedStatement.setString(3, orderDate);
            preparedStatement.setString(4, site);
            preparedStatement.setString(5, streetAddress);
            preparedStatement.setString(6, city);
            preparedStatement.setString(7, po);

            if (preparedStatement.executeUpdate() > 0) {
                // Get the generated rental_order_id
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        rentalOrderId = generatedKeys.getInt(1);
                        currentCustomerRental.setRentalOrderId(rentalOrderId);// Retrieve the rental_order_id
                        System.out.println("Generated rental_order_id: " + rentalOrderId);  // For debugging
                        currentCustomerRental.setOrderDate(orderDate); // Set the order date
                    }
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean insertRentalItem(int localRentalOrderId, int liftId, String orderDate, String deliveryDate, String deliveryTime, String  po) {
        // First section prepares contact vars
        String orderedByNameValue = orderedByField.getText();
        String orderedByNumberValue = orderedByPhoneField.getText();
        String siteContactNameValue = siteContactField.getText();
        String siteContactNumberValue = siteContactPhoneField.getText();
        boolean haveOrderedBy = false;
        boolean haveSiteContact = false;
        String orderedByContactId = "";
        String siteContactId = "";
        if (isPhoneNumberValid(orderedByNumberValue) && !orderedByNameValue.isEmpty()) {
            String cleanedNumber = orderedByNumberValue.replaceAll("\\D", "");
            if (selectedCustomer.isOrderingContactExtant(orderedByNameValue, cleanedNumber)) {
                orderedByContactId = selectedCustomer.getOrderingContactId(orderedByNameValue);
            } else {
                orderedByContactId = insertContactInDB(orderedByNameValue, cleanedNumber, true);
            }
            haveOrderedBy = true;
        }
        if (isPhoneNumberValid(siteContactNumberValue) && !siteContactNameValue.isEmpty()) {
            String cleanedNumber = siteContactNumberValue.replaceAll("\\D", "");
            if (selectedCustomer.isSiteContactExtant(siteContactNameValue, cleanedNumber)) {
                siteContactId = selectedCustomer.getSiteContactId(siteContactNameValue);
            } else {
                siteContactId = insertContactInDB(siteContactNameValue, cleanedNumber, false);
            }
            haveSiteContact = true;
        }


        System.out.println("Inserting rental item with lift_id: " + liftId);
        String query = "INSERT INTO rental_items (rental_order_id, lift_id, ordered_contact_id, site_contact_id, " +
                "item_order_date, item_delivery_date, delivery_time, customer_ref_number, location_notes, " +
                "pre_trip_instructions) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, localRentalOrderId);
            preparedStatement.setInt(2, liftId);
            if (haveOrderedBy) {
                preparedStatement.setString(3, orderedByContactId);
            } else {
                preparedStatement.setNull(3, Types.VARCHAR);
            }
            if (haveSiteContact) {
                preparedStatement.setString(4, siteContactId);
            } else {
                preparedStatement.setNull(4, Types.VARCHAR);
            }
            preparedStatement.setString(5, orderDate);
            preparedStatement.setString(6, deliveryDate);
            preparedStatement.setString(7, deliveryTime);
            preparedStatement.setString(8, po);
            if (locationNotesButton.getStyleClass().contains("schedule-delivery-button-has-value")) {
                preparedStatement.setString(9, locationNotesField.getText());
            } else {
                preparedStatement.setNull(9, Types.VARCHAR);
            }
            if (preTripInstructionsButton.getStyleClass().contains("schedule-delivery-button-has-value")) {
                preparedStatement.setString(10, preTripInstructionsField.getText());
            } else {
                preparedStatement.setNull(10, Types.VARCHAR);
            }

            // Execute the update
            int rowsAffected = preparedStatement.executeUpdate();

            // Check if any rows were affected, indicating a successful insert
            return rowsAffected > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String insertContactInDB(String name, String number, boolean isOrderingContact){
        System.out.println("Inserting contact in DB with name: " + name + " and number: " + number);
        String newContactId = null;
        String query = "INSERT INTO contacts (customer_id, first_name, phone_number, is_ordering_contact, is_site_contact) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             PreparedStatement preparedStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, selectedCustomer.getCustomerId());
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, number);
            preparedStatement.setBoolean(4, isOrderingContact);
            preparedStatement.setBoolean(5, !isOrderingContact);

            if (preparedStatement.executeUpdate() > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newContactId = generatedKeys.getString(1);
                        System.out.println("Generated contact_id: " + newContactId);  // For debugging
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newContactId;
    }

    public int getLiftIdFromType(String liftType) {
        return Config.LIFT_TYPE_MAP.getOrDefault(liftType, -1);
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

    private void handleAddNewCustomer(){
        statusLabel.setText("New customer additions currently only supported in Quickbooks");
        statusLabel.setTextFill(Color.RED);
        statusLabel.setVisible(true);
    }

    // Utility method to reset fields
    private void resetFields() {
        customerNameField.clear();
        orderedByField.clear();
        orderedByPhoneField.clear();
        liftTypeToggleGroup.selectToggle(twelveMastButton); // Deselect any selected lift type button
        deliveryTimeToggleGroup.selectToggle(deliveryTime8To10Button); // Re-select the "8-10" delivery time toggle button
        statusLabel.setText(""); // Clear the status label
        statusLabel.setVisible(false); // Hide the status label
        hourComboBox.setVisible(false); // Hide hour ComboBox
        customButton.setSelected(false);
        closeCalendar();
        datePicker.setValue(null);
        dateSelected = false;// Unselect custom button
        setDefaultRentalDate();
        suggestionsBox.setVisible(false);
        suggestionsBox.getItems().clear();// Restart rotation highlight
        siteField.clear();
        addressField.clear();
        siteContactField.clear();
        siteContactPhoneField.clear();
        POField.clear();
        locationNotesField.clear();
        preTripInstructionsField.clear();
        locationNotesButton.getStyleClass().remove("schedule-delivery-button-has-value");
        preTripInstructionsButton.getStyleClass().remove("schedule-delivery-button-has-value");
        rotationTimeline.play();
        isRotating = true;
        prepareLiftTypeButtons();
        liftCount = 0;
        addedLifts.clear();
        plusButton.setVisible(false);
        liftCountLabel.setVisible(false);
        xButton.setVisible(false);
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

    @FXML
    public void handleBack() {
        try {
            MaxReachPro.goBack("/fxml/schedule_delivery.fxml");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double getTotalHeight() {
        if (scheduledCounter == 0){
            return 358;
        } else {
            return 358 - 20 - (ROW_HEIGHT * scheduledCounter);
        }
    }
}
package com.MaxHighReach;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToggleButton;
import javafx.util.Duration;

import java.sql.ResultSet;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class ScheduleDeliveryController extends BaseController {

    private static final double INITIAL_SCISSOR_LIFT_HEIGHT = 247; // Initial height of the scissor lift
    private static final double INITIAL_TABLE_HEIGHT = 50; // Initial height of the TableView (height of one row)
    private static final double ROW_HEIGHT = 25; // Height of each row in the TableView
    private double currentHeight = INITIAL_TABLE_HEIGHT; // Track the current height of the scissor lift

    private ObservableList<Customer> customers = FXCollections.observableArrayList();
    private Customer selectedCustomer;
    private boolean dateSelected = false;

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
    private TilePane deliveryTimeTilePane;  // TilePane for delivery time toggle buttons

    private ToggleGroup liftTypeToggleGroup;  // To ensure only one lift type can be selected at a time
    private ToggleGroup deliveryTimeToggleGroup;  // To ensure only one delivery time can be selected at a time
    private ToggleGroup weeksRowToggleGroup;

    @FXML
    private ToggleButton twelveMastButton;
    @FXML
    private ToggleButton deliveryTime8To10Button; // Reference to the "8-10" toggle button
    @FXML
    public Button calendarButton;
    @FXML
    private ToggleButton customButton;
    @FXML
    private ComboBox<String> hourComboBox;
    @FXML
    private ComboBox<String> ampmComboBox; // Reference to the AM/PM ComboBox
    @FXML
    private Label statusLabel; // Reference to the status label

    private Timeline rotationTimeline; // Timeline for rotating highlight
    private boolean isRotating = false; // Flag to track rotation state

    // Initialize method to set up ToggleButtons and the ToggleGroup
    @FXML
    public void initialize() {
        loadCustomers();
        setDefaultRentalDate();

        customerNameField.textProperty().addListener((obs, oldText, newText) -> {
            autoFillCustomerName(newText);
        });

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

        // Set up ToggleButtons for lift type
        for (javafx.scene.Node node : liftTypeTilePane.getChildren()) {
            node.getStyleClass().add("lift-type-button-rotating");
            if (node instanceof ToggleButton) {
                ToggleButton toggleButton = (ToggleButton) node;
                toggleButton.setToggleGroup(liftTypeToggleGroup);  // Add each ToggleButton to the ToggleGroup

                // Add event handler to stop rotation when a button is clicked
                toggleButton.setOnAction(event -> {
                    if (isRotating) {
                        rotationTimeline.stop();
                        isRotating = false; // Set isRotating to false
                    }
                    toggleButton.getStyleClass().removeAll("lift-type-button-rotating");
                    toggleButton.getStyleClass().add("lift-type-button-stopped");
                    liftTypeToggleGroup.selectToggle(toggleButton); // Ensure the selected button is set
                });
            }
        }

        // Set up ToggleButtons for delivery time
        for (javafx.scene.Node node : deliveryTimeTilePane.getChildren()) {
            if (node instanceof ToggleButton) {
                ToggleButton toggleButton = (ToggleButton) node;
                toggleButton.setToggleGroup(deliveryTimeToggleGroup);  // Add each ToggleButton to the ToggleGroup

                // Hide the custom ComboBoxes when a delivery time button is clicked
                toggleButton.setOnAction(event -> {
                    if (toggleButton != customButton) {
                        hourComboBox.setVisible(false);
                        ampmComboBox.setVisible(false);
                        customButton.setSelected(false); // Unselect custom button
                    }
                });
            }
        }

        // Pre-select the "8-10" delivery time toggle button
        deliveryTimeToggleGroup.selectToggle(deliveryTime8To10Button);

        // Populate hour and AM/PM ComboBoxes
        for (int i = 1; i <= 12; i++) {
            hourComboBox.getItems().add(String.valueOf(i+":00")); // Add hours 1-12 to the ComboBox
        }
        ampmComboBox.getItems().addAll("am", "pm"); // Add AM and PM to the ComboBox

        customButton.setOnAction(event -> {
            boolean isSelected = customButton.isSelected();
            hourComboBox.setVisible(isSelected);
            ampmComboBox.setVisible(isSelected);
            if (!isSelected) {
                hourComboBox.getSelectionModel().clearSelection();
                ampmComboBox.getSelectionModel().clearSelection();
            }
        });

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
    }


// Method to load customers from the SQL database
    private void loadCustomers() {
        String query = "SELECT customer_id, name, email FROM customers";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/practice_db", "root", "SQL3225422!a");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String customerId = resultSet.getString("customer_id");
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");

                // Add to the customer list
                customers.add(new Customer(customerId, name, email));
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading customers: " + e.getMessage());
            statusLabel.setVisible(true);
        }
    }

    // Method to provide auto-fill suggestions for customer names
    private void autoFillCustomerName(String input) {
        if (input.isEmpty()) {
            selectedCustomer = null;  // Clear the selected customer if the input is empty
            return;
        }

        ObservableList<String> matchingNames = FXCollections.observableArrayList();

        for (Customer customer : customers) {
            if (customer.getName().toLowerCase().contains(input.toLowerCase())) {
                matchingNames.add(customer.getName());
            }
        }

        if (matchingNames.size() == 1) {
            customerNameField.setText(matchingNames.get(0));  // Auto-fill if there's a single match
            loadSelectedCustomerByName(matchingNames.get(0));  // Load customer object
        }
    }

    // Method to load the selected customer based on their name
    private void loadSelectedCustomerByName(String customerName) {
        for (Customer customer : customers) {
            if (customer.getName().equals(customerName)) {
                selectedCustomer = customer;  // Set the selected customer
                break;
            }
        }
    }

    private boolean isCustomerValid(String customerId) {
       System.out.println("checking customer " + customerId); // Check if any Customer in the customers list has a matching customerId
        for (Customer customer : customers) {
            System.out.println(customer.getCustomerId());
            if (customer.getName().equals(customerId)) {
                return true; // Found a matching customerId
            }
        }
        return false; // No matching customerId found
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
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), event -> {
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

    // Method to adjust the height of the TableView based on the number of entries
    private void adjustTableViewHeight() {
        double newHeight = INITIAL_TABLE_HEIGHT + (ROW_HEIGHT * Math.max(0, rentalsList.size()));
        scheduledRentalsTableView.setPrefHeight(newHeight);
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
    public void handleScheduleDelivery() {
        try {
            String customerId = customerNameField.getText(); // Get customer ID as String

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
            String startDate = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // For database
            String formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("MMM-dd")); // For display

            ToggleButton selectedLiftTypeButton = (ToggleButton) liftTypeToggleGroup.getSelectedToggle();
            if (selectedLiftTypeButton == null) {
                statusLabel.setText("Please select a lift type."); // Show error message
                statusLabel.setTextFill(Color.RED);
                statusLabel.setVisible(true);
                return; // Exit the method early
            }

            String liftType = selectedLiftTypeButton.getText();  // Get the text of the selected button (lift type)

            // Check if the custom delivery time is selected
            String deliveryTime;
            if (customButton.isSelected()) {
                String selectedHour = hourComboBox.getSelectionModel().getSelectedItem();
                String selectedAmPm = ampmComboBox.getSelectionModel().getSelectedItem();
                if (selectedHour == null || selectedAmPm == null) {
                    // Update the status label for input error
                    statusLabel.setText("Please select a custom delivery time."); // Show error message
                    statusLabel.setTextFill(Color.RED); // Set the text color to red
                    statusLabel.setVisible(true); // Make the status label visible
                    return; // Exit the method early
                }
                deliveryTime = selectedHour + " " + selectedAmPm; // Construct delivery time string
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

            // Insert rental and update the UI
            if (insertRental(customerId, startDate, liftType, deliveryTime)) {
                // Update the status label for successful scheduling
                statusLabel.setText("Rental scheduled successfully!"); // Show success message
                statusLabel.setTextFill(Color.GREEN); // Set the text color to green
                statusLabel.setVisible(true); // Make the status label visible

                // Add the newly scheduled rental to the rentalsList for this session
                rentalsList.add(new CustomerRental(customerId, liftType, formattedDate, deliveryTime, "", "Scheduled", 99999, rentalsList.size() + 1));

                // Animate the scissor lift down by decrementing its height
                currentHeight -= 50; // Decrease height
                MaxReachPro.getScissorLift().animateTransition(currentHeight + ROW_HEIGHT, currentHeight); // Animate the lift

                adjustTableViewHeight(); // Adjust the TableView height after adding a new entry

                // Reset fields after successful scheduling
                resetFields();
            } else {
                // Update the status label for rental failure
                statusLabel.setText("Failed to schedule the rental. Please try again."); // Show error message
                statusLabel.setTextFill(Color.RED); // Set the text color to red
                statusLabel.setVisible(true); // Make the status label visible
            }

        } catch (Exception e) {
            // Update the status label for input error
            statusLabel.setText("Invalid input: " + e.getMessage()); // Show error message
            statusLabel.setTextFill(Color.RED); // Set the text color to red
            statusLabel.setVisible(true); // Make the status label visible
        }
    }


    private boolean insertRental(String customer_name, String rentalDate, String liftType, String deliveryTime) {
        String query = "INSERT INTO rentals (customer_name, rental_date, lift_type, delivery_time) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/practice_db", "root", "SQL3225422!a");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, customer_name); // Change to setString
            preparedStatement.setString(2, rentalDate);
            preparedStatement.setString(3, liftType);
            preparedStatement.setString(4, deliveryTime);

            preparedStatement.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // Utility method to reset fields
    private void resetFields() {
        customerNameField.clear();
        liftTypeToggleGroup.selectToggle(null); // Deselect any selected lift type button
        deliveryTimeToggleGroup.selectToggle(deliveryTime8To10Button); // Re-select the "8-10" delivery time toggle button
        statusLabel.setText(""); // Clear the status label
        statusLabel.setVisible(false); // Hide the status label
        hourComboBox.setVisible(false); // Hide hour ComboBox
        ampmComboBox.setVisible(false); // Hide AM/PM ComboBox
        customButton.setSelected(false);
        closeCalendar();
        datePicker.setValue(null);
        dateSelected = false;// Unselect custom button
        startHighlightRotation();
        setDefaultRentalDate();// Restart rotation highlight
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
        return INITIAL_SCISSOR_LIFT_HEIGHT; // Use the class-level variable for total height
    }
}

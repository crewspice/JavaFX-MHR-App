package com.MaxHighReach;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToggleButton;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class ScheduleDeliveryController extends BaseController {

    private static final double INITIAL_SCISSOR_LIFT_HEIGHT = 247; // Initial height of the scissor lift
    private static final double INITIAL_TABLE_HEIGHT = 50; // Initial height of the TableView (height of one row)
    private static final double ROW_HEIGHT = 25; // Height of each row in the TableView
    private double currentHeight = INITIAL_TABLE_HEIGHT; // Track the current height of the scissor lift

    @FXML
    private TableView<CustomerOrder> scheduledRentalsTableView;  // TableView for rentals
    @FXML
    private TableColumn<CustomerOrder, Integer> customerIdColumn;  // Column for Customer ID
    @FXML
    private TableColumn<CustomerOrder, String> rentalDateColumn;   // Column for Rental Date
    @FXML
    private TableColumn<CustomerOrder, String> liftTypeColumn;     // Column for Lift Type
    @FXML
    private TableColumn<CustomerOrder, String> deliveryTimeColumn;  // Column for Delivery Time

    private ObservableList<CustomerOrder> rentalsList = FXCollections.observableArrayList(); // List to hold rentals

    @FXML
    private TextField customerIdField;

    @FXML
    private TextField rentalDateField;

    @FXML
    private TilePane liftTypeTilePane;  // TilePane containing the lift type toggle buttons
    @FXML
    private TilePane deliveryTimeTilePane;  // TilePane for delivery time toggle buttons

    private ToggleGroup liftTypeToggleGroup;  // To ensure only one lift type can be selected at a time
    private ToggleGroup deliveryTimeToggleGroup;  // To ensure only one delivery time can be selected at a time

    @FXML
    private ToggleButton deliveryTime8To10Button; // Reference to the "8-10" toggle button
    @FXML
    private Label statusLabel; // Reference to the status label

    private Timeline rotationTimeline; // Timeline for rotating highlight

    // Initialize method to set up ToggleButtons and the ToggleGroup
    @FXML
    public void initialize() {
        liftTypeToggleGroup = new ToggleGroup();  // Create the ToggleGroup for lift types
        deliveryTimeToggleGroup = new ToggleGroup();  // Create the ToggleGroup for delivery times

        // Set up ToggleButtons for lift type
        for (javafx.scene.Node node : liftTypeTilePane.getChildren()) {
            if (node instanceof ToggleButton) {
                ToggleButton toggleButton = (ToggleButton) node;
                toggleButton.setToggleGroup(liftTypeToggleGroup);  // Add each ToggleButton to the ToggleGroup

                // Add event handler to stop rotation when a button is clicked
                toggleButton.setOnAction(event -> {
                    rotationTimeline.stop(); // Stop the rotation timeline
                    liftTypeToggleGroup.selectToggle(toggleButton); // Ensure the selected button is set
                });
            }
        }

        // Set up ToggleButtons for delivery time
        for (javafx.scene.Node node : deliveryTimeTilePane.getChildren()) {
            if (node instanceof ToggleButton) {
                ToggleButton toggleButton = (ToggleButton) node;
                toggleButton.setToggleGroup(deliveryTimeToggleGroup);  // Add each ToggleButton to the ToggleGroup
            }
        }

        // Pre-select the "8-10" delivery time toggle button
        deliveryTimeToggleGroup.selectToggle(deliveryTime8To10Button);

        // Initialize TableView columns
        customerIdColumn.setCellValueFactory(cellData -> cellData.getValue().customerIdProperty().asObject());
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

    private void startHighlightRotation() {
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
        // Calculate new height based on the formula: initial height + height of one extra row
        double newHeight = INITIAL_TABLE_HEIGHT + (ROW_HEIGHT * Math.max(0, rentalsList.size())); // Use Math.max to ensure at least one empty row
        scheduledRentalsTableView.setPrefHeight(newHeight); // Set the new height
    }

    // Handle "Schedule" button click
    @FXML
    public void handleScheduleDelivery() {
        try {
            int customerId = Integer.parseInt(customerIdField.getText());
            String rentalDate = rentalDateField.getText();

            // Get the selected ToggleButton for lift type
            ToggleButton selectedLiftTypeButton = (ToggleButton) liftTypeToggleGroup.getSelectedToggle();
            if (selectedLiftTypeButton == null) {
                // Update the status label for input error
                statusLabel.setText("Please select a lift type."); // Show error message
                statusLabel.setTextFill(Color.RED); // Set the text color to red
                statusLabel.setVisible(true); // Make the status label visible
                return; // Exit the method early
            }

            String liftType = selectedLiftTypeButton.getText();  // Get the text of the selected button (lift type)

            // Get the selected ToggleButton for delivery time
            ToggleButton selectedDeliveryTimeButton = (ToggleButton) deliveryTimeToggleGroup.getSelectedToggle();
            if (selectedDeliveryTimeButton == null) {
                // Update the status label for input error
                statusLabel.setText("Please select a delivery time."); // Show error message
                statusLabel.setTextFill(Color.RED); // Set the text color to red
                statusLabel.setVisible(true); // Make the status label visible
                return; // Exit the method early
            }

            String deliveryTime = selectedDeliveryTimeButton.getText(); // Get the selected delivery time

            // Insert rental and update the UI
            if (insertRental(customerId, rentalDate, liftType, deliveryTime)) {
                // Update the status label for successful scheduling
                statusLabel.setText("Rental scheduled successfully!"); // Show success message
                statusLabel.setTextFill(Color.GREEN); // Set the text color to green
                statusLabel.setVisible(true); // Make the status label visible

                // Add the newly scheduled rental to the rentalsList for this session
                rentalsList.add(new CustomerOrder(customerId, liftType, rentalDate, deliveryTime, "", "Scheduled", 99999, rentalsList.size() + 1));

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

    // Method to insert a new rental into the database
    private boolean insertRental(int customerId, String rentalDate, String liftType, String deliveryTime) {
        String query = "INSERT INTO rentals (customer_id, rental_date, lift_type, delivery_time) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/practice_db", "root", "SQL3225422!a");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, customerId);
            preparedStatement.setString(2, rentalDate);
            preparedStatement.setString(3, liftType);  // Insert the lift type
            preparedStatement.setString(4, deliveryTime); // Insert the delivery time

            preparedStatement.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Utility method to reset fields
    private void resetFields() {
        customerIdField.clear();
        rentalDateField.clear();
        liftTypeToggleGroup.selectToggle(null); // Deselect any selected lift type button
        deliveryTimeToggleGroup.selectToggle(deliveryTime8To10Button); // Re-select the "8-10" delivery time toggle button
        statusLabel.setText(""); // Clear the status label
        statusLabel.setVisible(false); // Hide the status label
        startHighlightRotation();
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

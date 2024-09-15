package com.MaxHighReach;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class ScheduleDeliveryController extends BaseController {

    @FXML
    private TextField customerIdField;

    @FXML
    private TextField orderDateField;

    @FXML
    private TextField amountField;

    // Handle "Schedule" button click
    @FXML
    public void handleScheduleDelivery() {
        try {
            int customerId = Integer.parseInt(customerIdField.getText());
            String orderDate = orderDateField.getText();
            double amount = Double.parseDouble(amountField.getText());

            if (insertOrder(customerId, orderDate, amount)) {
                showAlert(AlertType.INFORMATION, "Order Scheduled", "The order has been successfully scheduled!");
            } else {
                showAlert(AlertType.ERROR, "Order Failed", "Failed to schedule the order. Please try again.");
            }

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Input Error", "Invalid input: " + e.getMessage());
        }
    }

    // Method to insert a new order into the database
    private boolean insertOrder(int customerId, String orderDate, double amount) {
        String query = "INSERT INTO orders (customer_id, order_date, amount) VALUES (?, ?, ?)";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/practice_db", "root", "SQL3225422!a");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, customerId);
            preparedStatement.setString(2, orderDate);
            preparedStatement.setDouble(3, amount);

            preparedStatement.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Utility method to show alerts
    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleBack(ActionEvent event) {
        try {
            MaxReachPro.goBack("/fxml/schedule_delivery.fxml");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double getTotalHeight() {
        return 200;
    }
}

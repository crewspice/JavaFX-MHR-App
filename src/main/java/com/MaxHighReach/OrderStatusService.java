package com.MaxHighReach;

import com.MaxHighReach.utils.StatusNodeFactory;

import javafx.scene.Node;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class OrderStatusService {

    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/practice_db";
    private static final String DATABASE_USER = "root";
    private static final String DATABASE_PASSWORD = "SQL3225422!a";

    public void updateOrderStatusInDB(int orderId, Node statusNode) {
        String newStatus = getStatusFromNode(statusNode);
        String updateQuery = "UPDATE orders SET status = ? WHERE order_id = ?"; // Fixed the column reference to order_id
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

            statement.setString(1, newStatus);
            statement.setInt(2, orderId);
            statement.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String determineNewStatus(CustomerOrder order) {
        // Example logic to determine new status based on order's current status
        String currentStatus = getStatusFromNode(order.getStatus());
        if ("Upcoming".equals(currentStatus)) {
            return "Active";
        } else if ("Active".equals(currentStatus)) {
            return "Ended";
        }
        return currentStatus; // Return the current status if no change is needed
    }

    // New method to get the status as a string from a Node (assuming some representation is used)
    public String getStatusFromNode(Node statusNode) {
        return StatusNodeFactory.getStatusFromNode(statusNode); // Delegate to StatusNodeFactory
    }
}

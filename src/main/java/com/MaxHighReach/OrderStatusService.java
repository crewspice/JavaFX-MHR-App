package com.MaxHighReach;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class OrderStatusService {

    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/practice_db";
    private static final String DATABASE_USER = "root";
    private static final String DATABASE_PASSWORD = "SQL3225422!a";

    public void updateOrderStatusInDB(int orderId, String newStatus) {
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

    public String determineNewStatus(CustomerRental order) {
        // Example logic to determine new status based on order's current status
        String currentStatus = order.getStatus();
        if ("Upcoming".equals(currentStatus)) {
            return "Active";
        } else if ("Active".equals(currentStatus)) {
            return "Ended";
        }
        return currentStatus; // Return the current status if no change is needed
    }
}

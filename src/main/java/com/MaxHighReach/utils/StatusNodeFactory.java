package com.MaxHighReach.utils;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class StatusNodeFactory {

    // Existing createStatusNode method
    public static Node createStatusNode(String status) {
        switch (status) {
            case "Upcoming":
                return new Circle(10, Color.BLUE);
            case "Active":
                return new Circle(10, Color.GREEN);
            case "Ended":
                return new Circle(10, Color.RED);
            default:
                return new Circle(10, Color.GRAY); // Default for unknown status
        }
    }

    // New method to create status node with Color and String
    public static Node createStatusNode(Color color, String status) {
        Circle circle = new Circle(10, color);
        Label label = new Label(status);
        circle.setUserData(label); // Store the status as user data if needed
        return circle;
    }

    // New method to get status from Node
    public static String getStatusFromNode(Node statusNode) {
        if (statusNode instanceof Circle) {
            Circle circle = (Circle) statusNode;
            Color color = (Color) circle.getFill();

            // Map colors back to status strings
            if (Color.BLUE.equals(color)) {
                return "Upcoming";
            } else if (Color.GREEN.equals(color)) {
                return "Active";
            } else if (Color.RED.equals(color)) {
                return "Ended";
            } else if (Color.GRAY.equals(color)) {
                return "Unknown";
            }
        }

        // Handle other node types (e.g., Label) if necessary
        if (statusNode instanceof Label) {
            return ((Label) statusNode).getText();
        }

        // Return unknown if the node type or color is not recognized
        return "Unknown";
    }
}

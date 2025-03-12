package com.MaxHighReach;

import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;

public class PopupCard extends VBox {
    private boolean isDriveTimePopup = false;

    // Constructor for rental details
    public PopupCard(Rental rental, double x, double y) {
        System.out.println("PopupCard constructor called for: " + rental.getName());

        setSpacing(10);
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-radius: 10; -fx-padding: 10;");

        // Add a subtle shadow for effect
        setEffect(new DropShadow(10, Color.GRAY));

        // Ensure proper clipping to match rounded corners
        Rectangle clip = new Rectangle(220, 250);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        setClip(clip);

        // Rental Info
        Label title = new Label(rental.getName());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label address = new Label("Address: " + rental.getAddressBlockOne());
        Label price = new Label("Price: $" + rental.getAddressBlockTwo());
        Label size = new Label("Size: " + rental.getCity());

        // Latitude and Longitude
        Label latitude = new Label("Latitude: " + rental.getLatitude());
        Label longitude = new Label("Longitude: " + rental.getLongitude());

        getChildren().addAll(title, address, price, size, latitude, longitude);

        // Set position (Adjust for better placement)
        setLayoutX(x + 10);
        setLayoutY(y - 50);
    }

    // Constructor for drive time popups
    public PopupCard(String driveTimeMessage, double x, double y) {
        setSpacing(10);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: lightyellow; -fx-border-color: black; -fx-border-radius: 10; -fx-padding: 10;");

        setEffect(new DropShadow(10, Color.DARKGRAY));

        // Ensure proper clipping to match rounded corners
        Rectangle clip = new Rectangle(200, 50);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        setClip(clip);

        // Drive time info
        Label driveTimeLabel = new Label(driveTimeMessage);
        driveTimeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        getChildren().add(driveTimeLabel);

        // Set flag and position
        this.isDriveTimePopup = true;
        setLayoutX(x + 30);
        setLayoutY(y + 30);
    }

    // Getter to check if it's a drive time popup
    public boolean isDriveTimePopup() {
        return isDriveTimePopup;
    }

    // Setter for drive time popups
    public void setDriveTimePopup(boolean isDriveTimePopup) {
        this.isDriveTimePopup = isDriveTimePopup;
    }
}

package com.MaxHighReach;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;

public class PopupCard extends VBox {
    private boolean isDriveTimePopup = false;
    private MapController mapController;
    private String primaryColor = Config.getPrimaryColor();
    private String secondaryColor = Config.getSecondaryColor();

    // Constructor for rental details
    public PopupCard(MapController mapController, Rental rental, double x, double y) {
        this.mapController = mapController;
    
        setSpacing(10);
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-radius: 10; -fx-padding: 10;");
        
        // Add a subtle shadow for effect
        setEffect(new DropShadow(10, Color.GRAY));
    
        // Rental Info
        Label title = new Label(rental.getName());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label address = new Label("Address: " + rental.getAddressBlockOne());
        Label price = new Label("Price: $" + rental.getAddressBlockTwo());
        Label size = new Label("Size: " + rental.getCity());
        Label latitude = new Label("Latitude: " + rental.getLatitude());
        Label longitude = new Label("Longitude: " + rental.getLongitude());
    
        // VBox for buttons
        VBox buttonContainer = new VBox(5);
        buttonContainer.setAlignment(Pos.CENTER);
    
        // Route names
        String[] routeNames = { "route1", "route2", "route3", "route4", "route5" };
        
        // Track first empty route
        String firstEmptyRoute = null;
        
        for (String routeName : routeNames) {
            List<Rental> routeStops = mapController.getRouteStops(routeName);
    
            // If route is empty and we haven't assigned an empty route yet, store it
            if (routeStops.isEmpty() && firstEmptyRoute == null) {
                firstEmptyRoute = routeName;
            }
            
            // Only create buttons for non-empty routes
            if (!routeStops.isEmpty()) {
                buttonContainer.getChildren().add(createRouteButton(routeName, rental));
            }
        }
    
        // Add a button for the first empty route
        if (firstEmptyRoute != null) {
            buttonContainer.getChildren().add(createRouteButton(firstEmptyRoute, rental));
        }
    
        // Dynamically adjust popup size
        double baseHeight = 180; // Minimum popup height (adjust as needed)
        double buttonHeight = 30; // Approximate height per button
        double newHeight = baseHeight + (buttonContainer.getChildren().size() * buttonHeight);
        
        // Set clipping for rounded corners
        Rectangle clip = new Rectangle(220, newHeight);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        setClip(clip);
    
        // Apply updated height
        setMinHeight(newHeight);
        setMaxHeight(newHeight);
    
        // Add everything to the popup
        getChildren().addAll(title, address, price, size, latitude, longitude, buttonContainer);
    
        // Set position (Adjust for better placement)
        setLayoutX(x + 10);
        setLayoutY(y - 50);
    }

    // // Constructor for drive time popups
    // public PopupCard(String driveTimeMessage, double x, double y) {
    //     setSpacing(10);
    //     setAlignment(Pos.CENTER);
    //     setStyle("-fx-background-color: lightyellow; -fx-border-color: black; -fx-border-radius: 10; -fx-padding: 10;");

    //     setEffect(new DropShadow(10, Color.DARKGRAY));

    //     // Ensure proper clipping to match rounded corners
    //     Rectangle clip = new Rectangle(320, 50);
    //     clip.setArcWidth(20);
    //     clip.setArcHeight(20);
    //     setClip(clip);

    //     // Drive time info
    //     Label driveTimeLabel = new Label(driveTimeMessage);
    //     driveTimeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

    //     getChildren().add(driveTimeLabel);

    //     // Set flag and position
    //     this.isDriveTimePopup = true;
    //     setLayoutX(30);
    //     setLayoutY(720);
    // }

    // Getter to check if it's a drive time popup
    public boolean isDriveTimePopup() {
        return isDriveTimePopup;
    }

    // Setter for drive time popups
    public void setDriveTimePopup(boolean isDriveTimePopup) {
        this.isDriveTimePopup = isDriveTimePopup;
    }

    private Button createRouteButton(String routeName, Rental rental) {
        Button routeButton = new Button("+ to " + routeName.replace("route", "Route "));
        routeButton.setAlignment(Pos.CENTER);

        // Set button size
        routeButton.setPrefWidth(120);
        routeButton.setPrefHeight(25);
        
        // Fetch route colors from mapController
        String[] colors = mapController.getRouteColors(routeName);
        String primaryColor = colors[0];
        String secondaryColor = colors[1];
    
        // Apply gradient background
        LinearGradient gradient = new LinearGradient(
            0, 0, 1, 0, // Horizontal gradient
            true, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web(colors[0])),        
            new Stop(0.011, Color.web(colors[0]).interpolate(Color.web(colors[1]), 0.1)), 
            new Stop(0.061, Color.web(colors[0]).interpolate(Color.web(colors[1]), 0.38)), 
            new Stop(0.1, Color.web(colors[0]).interpolate(Color.web(colors[1]), 0.95)), 
            new Stop(0.5, Color.web(colors[1])),
            new Stop(0.9, Color.web(colors[0]).interpolate(Color.web(colors[1]), 0.95)), 
            new Stop(0.939, Color.web(colors[0]).interpolate(Color.web(colors[1]), 0.38)), 
            new Stop(0.989, Color.web(colors[0]).interpolate(Color.web(colors[1]), 0.1)),
            new Stop(1.0, Color.web(colors[0]))        
        );
    
        routeButton.getStyleClass().removeAll(routeButton.getStyleClass());
        routeButton.setBackground(new Background(
            new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)
        ));
        routeButton.setTextFill(Color.web(colors[2]));
    
        // Set action to add rental to the selected route
        routeButton.setOnAction(event -> mapController.addStopToRoute(routeName, rental));
    
        return routeButton;
    }

}

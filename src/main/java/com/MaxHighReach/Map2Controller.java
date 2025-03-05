package com.MaxHighReach;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import java.util.List;
import java.util.Random;

public class Map2Controller {

    @FXML
    private AnchorPane map2Pane; // The pane that holds the lavender area

    private Rectangle transitioner = new Rectangle(); // Rectangle to animate
    private Rectangle mapArea = new Rectangle(); // The area where the map (dots) will be placed

    // Latitude and Longitude bounds for the geographical area
    private double LAT_MIN = 39.391122;
    private double LAT_MAX = 40.1234847;
    private double LON_MIN = -105.57661;
    private double LON_MAX = -104.4526;

    // Number of random dots to generate
    private static final int NUM_DOTS = 8;

    // Compression ratio to fit the map into a smaller area within the transitioner
    private static final double COMPRESSION_RATIO_X = 0.7; // Compress the map horizontally
    private static final double COMPRESSION_RATIO_Y = 0.7; // Compress the map vertically

    // Offsets for the entire map area (move Y down by 150, X to the left by 50)
    private static final double OFFSET_X = -50; // Move left by 50 units
    private static final double OFFSET_Y = 150; // Move down by 150 units

    private List<Rental> rentalsForCharting;


    @FXML
    private void initialize() {
        // Set the background color of the map2Pane to transparent for now
        map2Pane.setStyle("-fx-background-color: transparent;");
        map2Pane.setPrefSize(480, 760);

        // Set up the transitioner (a lavender-colored rectangle)
        transitioner.setX(-100); // Set initial position X (out of view)
        transitioner.setY(20); // Set position Y (start from top)
        transitioner.setWidth(1); // Initial width (start with 1px width)
        transitioner.setHeight(760); // Full height
        transitioner.setFill(Color.LAVENDER); // Set color of the rectangle

        // Add the transitioner rectangle to map2Pane
        map2Pane.getChildren().add(transitioner);

        // Define the total duration of the animation (e.g., 1 second)
        double totalDurationInSeconds = 1.0;

        // Create a Timeline to animate both width and height of the transitioner smoothly
        Timeline timeline = new Timeline();

        // Animate the width and height properties simultaneously from their initial to final values
        double targetWidth = 480;
        double targetHeight = 760;

        // Single KeyFrame to animate both width and height
        KeyFrame keyFrame = new KeyFrame(
            Duration.seconds(totalDurationInSeconds),
            new javafx.animation.KeyValue(transitioner.widthProperty(), targetWidth),
            new javafx.animation.KeyValue(transitioner.heightProperty(), targetHeight)
        );

        // Add the KeyFrame to the timeline
        timeline.getKeyFrames().add(keyFrame);

        // Set the timeline to loop the animation only once
        timeline.setCycleCount(1);

        // Add an event handler to configure the map and generate dots after the animation finishes
        timeline.setOnFinished(event -> {
            configureMapArea();
            loadRentalData();
            plotRentalLocations();
        });

        // Start the animation
        timeline.play();
    }

    private void loadRentalData() {
        // SQL query to get rental data with the new filters
        String query = """
            SELECT ro.customer_id, c.customer_name, ri.item_delivery_date, ri.item_call_off_date, ro.po_number,
                ordered_contacts.first_name AS ordered_contact_name, ordered_contacts.phone_number AS ordered_contact_phone,
                ri.auto_term, ro.site_name, ro.street_address, ro.city, ri.rental_item_id,
                l.serial_number, ro.single_item_order, ri.rental_order_id, ro.longitude, ro.latitude,
                site_contacts.first_name AS site_contact_name, site_contacts.phone_number AS site_contact_phone
            FROM customers c
            JOIN rental_orders ro ON c.customer_id = ro.customer_id
            JOIN rental_items ri ON ro.rental_order_id = ri.rental_order_id
            JOIN lifts l ON ri.lift_id = l.lift_id
            LEFT JOIN contacts AS ordered_contacts ON ri.ordered_contact_id = ordered_contacts.contact_id
            LEFT JOIN contacts AS site_contacts ON ri.site_contact_id = site_contacts.contact_id
            WHERE (ri.item_status = 'Called Off')
            OR (ri.item_status = 'Upcoming' AND ri.item_delivery_date BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL 1 DAY)
            AND ro.latitude > 0
            AND ro.longitude < 0
        """;
    
        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery()) {
    
            rentalsForCharting = new ArrayList<>();
    
            // Process each result row
            while (rs.next()) {
                String customerId = rs.getString("customer_id");
                String name = rs.getString("customer_name");
                String deliveryDate = rs.getString("item_delivery_date");
                String callOffDate = rs.getString("item_call_off_date");
                String poNumber = rs.getString("po_number");
                String orderedByName = rs.getString("ordered_contact_name");
                String orderedByPhone = rs.getString("ordered_contact_phone");
                boolean autoTerm = rs.getBoolean("auto_term");
                String addressBlockOne = rs.getString("site_name");
                String addressBlockTwo = rs.getString("street_address");
                String addressBlockThree = rs.getString("city");
                int rentalItemId = rs.getInt("rental_item_id");
                String serialNumber = rs.getString("serial_number");
                boolean singleItemOrder = rs.getBoolean("single_item_order");
                int rentalOrderId = rs.getInt("rental_order_id");
                String siteContactName = rs.getString("site_contact_name");
                String siteContactPhone = rs.getString("site_contact_phone");
                double latitude = rs.getDouble("latitude");
                double longitude = rs.getDouble("longitude");
    
                // Create Rental objects for each row and add them to the list
                rentalsForCharting.add(new Rental(customerId, name, deliveryDate, callOffDate, poNumber,
                        orderedByName, orderedByPhone, autoTerm, addressBlockOne, addressBlockTwo,
                        addressBlockThree, rentalItemId, serialNumber, singleItemOrder, rentalOrderId,
                        siteContactName, siteContactPhone, latitude, longitude));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading rental data", e);
        }
    }
    


    // Configure the mapArea to fit inside the transitioner after animation
    private void configureMapArea() {
        // Set up the mapArea (the area for the map and dots)
        mapArea.setX(transitioner.getX() + 100 + OFFSET_X); // Apply the X offset
        mapArea.setY(transitioner.getY() + OFFSET_Y); // Apply the Y offset
        mapArea.setWidth(transitioner.getWidth() * COMPRESSION_RATIO_X); // Apply compression ratio for width
        mapArea.setHeight(transitioner.getHeight() * COMPRESSION_RATIO_Y); // Apply compression ratio for height
        mapArea.setFill(Color.TRANSPARENT); // Transparent to show dots only
        mapArea.setStroke(Color.BLACK); // Apply border to mapArea
        mapArea.setStrokeWidth(1); // Thin border

        // Add mapArea to the map2Pane
        map2Pane.getChildren().add(mapArea);
    }

    private void plotRentalLocations() {
        for (Rental rental : rentalsForCharting) {
            double x = mapLongitudeToX(rental.getLongitude());
            double y = mapLatitudeToY(rental.getLatitude());
    
            if (rental.getLongitude() < 0 && rental.getLatitude() > 0) {
                System.out.println("Plotting Rental: (" + rental.getLongitude() + ", " + rental.getLatitude() +
                ") -> Mapped to X=" + x + " Y=" + y);

                Circle dot = new Circle(x, y, 5);
                dot.setFill(Color.BLUE); // Different color for rental locations
                map2Pane.getChildren().add(dot);
            }          
        }
    }    

    private double mapLongitudeToX(double lon) {
        double relativeX = (lon - LON_MIN) / (LON_MAX - LON_MIN);
        return mapArea.getX() + relativeX * mapArea.getWidth();
    }
    
    private double mapLatitudeToY(double lat) {
        double relativeY = (LAT_MAX - lat) / (LAT_MAX - LAT_MIN); // Invert Y axis
        return mapArea.getY() + relativeY * mapArea.getHeight();
    }    

    @FXML
    private void resetStage() {
        MaxReachPro maxReachPro = new MaxReachPro();
        maxReachPro.resetStage();
    }
}

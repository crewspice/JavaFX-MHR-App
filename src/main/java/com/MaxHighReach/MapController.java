package com.MaxHighReach;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import javafx.application.Platform;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.json.JSONArray;
import org.json.JSONObject;

public class MapController {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Button closeButton;

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
    private static final double COMPRESSION_RATIO_X = 0.86; // Compress the map horizontally
    private static final double COMPRESSION_RATIO_Y = 0.7; // Compress the map vertically

    // Offsets for the entire map area (move Y down by 150, X to the left by 50)
    private static final double OFFSET_X = -55; // Move left by 50 units
    private static final double OFFSET_Y = 150; // Move down by 150 units

    private List<Rental> rentalsForCharting;

    // Track the alst clicked rental and popup
    private Rental lastClickedRental = null;
    private PopupCard lastPopup = null;


    @FXML
    private void initialize() {
        anchorPane.setStyle("-fx-background-color: transparent;");
        anchorPane.setPrefSize(480, 600);

        transitioner.setX(-2);
        transitioner.setY(20);
        transitioner.setWidth(1);
        transitioner.setHeight(760);
        transitioner.setFill(Color.web("#F4F4F4"));
        transitioner.toBack();

        anchorPane.getChildren().add(transitioner);

        double totalDurationInSeconds = 1.0;
        double targetWidth = 480;
        double targetHeight = 760;

        Timeline timeline = new Timeline(new KeyFrame(
            Duration.seconds(totalDurationInSeconds),
            new javafx.animation.KeyValue(transitioner.widthProperty(), targetWidth),
            new javafx.animation.KeyValue(transitioner.heightProperty(), targetHeight)
        ));
        
        timeline.setCycleCount(1);
        timeline.setOnFinished(event -> {
            configureMapArea();  // Still runs after animation
            plotRentalLocations(); // Now only runs AFTER rental data is loaded
            closeButton.toFront();
            closeButton.setVisible(true);
        });

        // Start loading rental data in a background thread
        CompletableFuture.runAsync(() -> {
            loadRentalData(); // Runs immediately without blocking UI
        }).thenRun(() -> Platform.runLater(() -> plotRentalLocations())); // Ensures plotRentalLocations() runs on the JavaFX thread

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
        mapArea.setX(transitioner.getX() + 80 + OFFSET_X); // Apply the X offset
        mapArea.setY(transitioner.getY() + OFFSET_Y); // Apply the Y offset
        mapArea.setWidth(transitioner.getWidth() * COMPRESSION_RATIO_X); // Apply compression ratio for width
        mapArea.setHeight(transitioner.getHeight() * COMPRESSION_RATIO_Y); // Apply compression ratio for height
        mapArea.setFill(Color.TRANSPARENT); // Transparent to show dots only
        mapArea.setStroke(Color.BLACK); // Apply border to mapArea
        mapArea.setStrokeWidth(1); // Thin border

        // Add mapArea to the map2Pane
        anchorPane.getChildren().add(mapArea);
    }

    private void plotRentalLocations() {
        for (Rental rental : rentalsForCharting) {
            double x = mapLongitudeToX(rental.getLongitude());
            double y = mapLatitudeToY(rental.getLatitude());
    
            if (rental.getLongitude() < 0 && rental.getLatitude() > 0) {
                System.out.println("Plotting Rental: (" + rental.getLongitude() + ", " + rental.getLatitude() +
                    ") -> Mapped to X=" + x + " Y=" + y);
    
                Circle dot = new Circle(x, y, 5);
                dot.setFill(Color.web(Config.getPrimaryColor())); 
                anchorPane.getChildren().add(dot);
    
                // Handle click to show rental details
                dot.setOnMouseClicked(event -> {
                    System.out.println("dot clicked");
                    // Remove any existing drive time popup
                    anchorPane.getChildren().removeIf(node -> node instanceof PopupCard && ((PopupCard) node).isDriveTimePopup());
                
                    // // Remove previous rental popup (if exists)
                    if (lastPopup == null) {
                        System.out.println("lastPopup is null, nothing to remove.");
                    } else {
                        System.out.println("Removing previous popup...");
                        if (anchorPane.getChildren().contains(lastPopup)) {
                            System.out.println("Popup exists in AnchorPane, removing...");
                            anchorPane.getChildren().remove(lastPopup);
                        } else {
                            System.out.println("Popup was already removed.");
                        }
                    }
                    
                
                    // Create and add new popup for the clicked rental
                    PopupCard popup = new PopupCard(rental, x, y);
                    anchorPane.getChildren().add(popup);
                    System.out.println("Rental popup displayed for: " + rental.getName()); // Debugging line

                
                    // Check if a previous rental was selected
                    if (lastClickedRental != null && lastClickedRental != rental) {
                        // Fetch addresses
                        String address1 = lastClickedRental.getAddressBlockTwo() + ", " + lastClickedRental.getCity();
                        String address2 = rental.getAddressBlockTwo() + ", " + rental.getCity();

                        // Fetch drive time asynchronously
                        new Thread(() -> {
                            int driveTime = getDriveTime(address1, address2); // Now uses the Routes API
                            Platform.runLater(() -> {
                                if (driveTime > 0) {
                                    // Create and display the drive time popup
                                    PopupCard driveTimePopup = new PopupCard("Drive Time: " + driveTime + " mins", x + 30, y + 30);
                                    driveTimePopup.setDriveTimePopup(true); // Mark as drive time popup
                                    anchorPane.getChildren().add(driveTimePopup);
                                }
                            });
                        }).start();
                    }

                
                    // Update last clicked rental and popup
                    lastClickedRental = rental;
                    lastPopup = popup;
                
                    // Hide popup when clicking outside
                    anchorPane.setOnMouseClicked(e -> {
                        if (!popup.getBoundsInParent().contains(e.getX(), e.getY())) {
                            anchorPane.getChildren().remove(popup);
                            lastClickedRental = null;
                            lastPopup = null;
                        }
                    });
                
                    event.consume(); // Prevent event from bubbling
                });
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

    public static int getDriveTime(String origin, String destination) {
        // URL for Routes API (POST request)
        String url = "https://routes.googleapis.com/directions/v2:computeRoutes";
        
        // Prepare JSON body for the request
        String requestBody = "{\n" +
                "  \"origin\": {\n" +
                "    \"location\": {\n" +
                "      \"latLng\": {\n" +
                "        \"latitude\": 37.419734,  // Replace with actual latitude of origin\n" +
                "        \"longitude\": -122.0827784  // Replace with actual longitude of origin\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"destination\": {\n" +
                "    \"location\": {\n" +
                "      \"latLng\": {\n" +
                "        \"latitude\": 37.417670,  // Replace with actual latitude of destination\n" +
                "        \"longitude\": -122.079595  // Replace with actual longitude of destination\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"travelMode\": \"DRIVE\",\n" +
                "  \"routingPreference\": \"TRAFFIC_AWARE\",\n" +
                "  \"languageCode\": \"en-US\",\n" +
                "  \"units\": \"IMPERIAL\"\n" +
                "}";
        
        // Execute the request
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .addHeader("X-Goog-Api-Key", Config.GOOGLE_KEY)
                .addHeader("X-Goog-FieldMask", "routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline")
                .build();
        
        OkHttpClient client = new OkHttpClient();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseData = response.body().string();
                JSONObject json = new JSONObject(responseData);
                String status = json.optString("status", "ERROR");

                System.out.println("API Response Status: " + status);

                if ("OK".equals(status)) {
                    JSONArray routes = json.getJSONArray("routes");
                    if (routes.length() > 0) {
                        JSONObject route = routes.getJSONObject(0);
                        int durationInSeconds = route.getJSONObject("duration").getInt("value");
                        int durationInMinutes = durationInSeconds / 60;

                        System.out.println("Drive time: " + durationInMinutes + " minutes");
                        return durationInMinutes;
                    } else {
                        System.out.println("No route found between addresses.");
                    }
                }
            } else {
                System.out.println("API request failed with status: " + response.code());
            }
        } catch (IOException e) {
            System.out.println("Request failed: " + e.getMessage());
            e.printStackTrace();
        }

        return -1; // Return -1 to indicate failure 
    }


    @FXML
    private void resetStage() {
        MaxReachPro.getInstance().collapseStage();
    }
} 
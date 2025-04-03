package com.MaxHighReach;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.effect.*;
import javafx.util.Duration;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.json.JSONArray;
import org.json.JSONObject;
//import com.google.maps.routing.v2.*;


public class MapController {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Button closeButton;

    private Rectangle transitioner = new Rectangle(); // Rectangle to animate
    private Rectangle mapArea = new Rectangle(); // The area where the map (dots) will be placed
    private Pane mapContainer;
    private ImageView metroMapView;
    private double zoomFactor = 1.0;
    private Circle centerpointDot;

    //
    private double[] mapBounds = {40.814076, -104.377137, 39.391122, -105.468393};
    private double[] visibleBounds = {0, 0, 0, 0};
    private double mouseX, mouseY;

    // Number of random dots to generate
    private static final int NUM_DOTS = 8;

    // Compression ratio to fit the map into a smaller area within the transitioner
    private static final double COMPRESSION_RATIO_X = 1; // Compress the map horizontally
    private static final double COMPRESSION_RATIO_Y = 1; // Compress the map vertically

    // Offsets for the entire map area (move Y down by 150, X to the left by 50)
    private static final double OFFSET_X = 0; // Move left by 50 units
    private static final double OFFSET_Y = 0; // Move down by 150 units

    private List<Rental> rentalsForCharting;
    private List<String> storedEncodedPolylines = new ArrayList<>();
    private List<String[]> storedColorsList = new ArrayList<>();


    // Track the alst clicked rental and popup
    private Rental lastClickedRental = null;
    private PopupCard lastPopup = null;

    private List<StackPane> routeVBoxPanes = new ArrayList<>();
    private List<StackPane> routeHBoxPanes = new ArrayList<>();
    private List<VBox> routeVBoxes = new ArrayList<>();
    private List<HBox> routeHBoxes = new ArrayList<>();
    private List<Region> routeRegions = new ArrayList<>();
    private List<List<Rental>> routeStops = new ArrayList<>();
    private Map<String, List<Rental>> routes = new HashMap<>();
    private Map<String, VBox> routeBoxes = new HashMap<>();

    private Map<String, String> routeAssignments = new HashMap<>();
    public List<Rental> latestRouteEdited = null;

    // Define your route lists (ensure these are populated somewhere, otherwise they will be null)
    private List<Rental> routeOneStops = new ArrayList<>();
    private List<Rental> routeTwoStops = new ArrayList<>();
    private List<Rental> routeThreeStops = new ArrayList<>();
    private List<Rental> routeFourStops = new ArrayList<>();
    private List<Rental> routeFiveStops = new ArrayList<>();

    // Initialize routes map (you can do this in a constructor or any other initialization block)
    {
        routes.put("routeOne", routeOneStops);
        routes.put("routeTwo", routeTwoStops);
        routes.put("routeThree", routeThreeStops);
        routes.put("routeFour", routeFourStops);
        routes.put("routeFive", routeFiveStops);
    }

    // Initialize routeBoxes map (same here, ensure the VBox components are defined before using)
    private VBox routeOneBox, routeTwoBox, routeThreeBox, routeFourBox, routeFiveBox;

    {
        routeBoxes.put("routeOne", routeOneBox);
        routeBoxes.put("routeTwo", routeTwoBox);
        routeBoxes.put("routeThree", routeThreeBox);
        routeBoxes.put("routeFour", routeFourBox);
        routeBoxes.put("routeFive", routeFiveBox);
    }
   
    private int cardHeightUnit = 55;
    private int cardWidthUnit = 100;

    @FXML
    private void initialize() {
        int lilAdjuster = 9;
    
        anchorPane.setStyle("-fx-background-color: transparent;");
        anchorPane.setPrefSize((Config.WINDOW_HEIGHT + lilAdjuster) / 2, Config.WINDOW_HEIGHT + lilAdjuster);
        anchorPane.setMaxWidth(Config.WINDOW_HEIGHT / 2);
        anchorPane.setMaxHeight(Config.WINDOW_HEIGHT);
    
        // Create the main transitioner rectangle
        Rectangle transitioner = new Rectangle();
        transitioner.setX(-2);
        transitioner.setY(5);
        transitioner.setWidth(1);
        transitioner.setHeight(Config.WINDOW_HEIGHT + lilAdjuster);
        transitioner.setFill(Color.web("#F4F4F4"));
        transitioner.toBack();
        anchorPane.getChildren().addAll(transitioner);
    
        // Timeline animation for transitioner
        double totalDurationInSeconds = 1.0;
        double targetWidth = (Config.WINDOW_HEIGHT + lilAdjuster) / 2;
        double targetHeight = Config.WINDOW_HEIGHT;
    
        Timeline timeline = new Timeline(new KeyFrame(
            Duration.seconds(totalDurationInSeconds),
            new javafx.animation.KeyValue(transitioner.widthProperty(), targetWidth)
        ));
        timeline.setCycleCount(1);
        timeline.setOnFinished(event -> {
            // Ensure setupMetroMap() runs before plotRentalLocations()
            setupMetroMap();
            // Start loading rental data after setupMetroMap() finishes
            loadRentalDataAsync();  // We load data asynchronously
        });
    
        timeline.play();
    
        // Dynamically instantiate routes based on Config.NUMBER_OF_TRUCKS
        int numberOfRoutes = Config.NUMBER_OF_TRUCKS;
        for (int i = 0; i < numberOfRoutes; i++) {
            StackPane routeVBoxPane = new StackPane();
            StackPane routeHBoxPane = new StackPane();
            VBox routeVBox = new VBox();
            HBox routeHBox = new HBox();
            List<Rental> routeRentalStops = new ArrayList<>();
    
            // Add the route components to their respective lists
            routeVBoxPanes.add(routeVBoxPane);
            routeHBoxPanes.add(routeHBoxPane);
            routeVBoxes.add(routeVBox);
            routeHBoxes.add(routeHBox);
            routeRegions.add(routeVBox);
            routeStops.add(routeRentalStops);
    
            // Optionally add each route to the routes map and routeBoxes map for later use
            routes.put("route" + (i + 1), routeRentalStops);
            routeBoxes.put("route" + (i + 1), routeVBox);
        }
    }
    
    // New async method to load rental data
    private void loadRentalDataAsync() {
        CompletableFuture.runAsync(() -> {
            loadRentalData();  // Load rental data in the background
        }).thenRun(() -> Platform.runLater(() -> {
            // Once data is loaded, plotRentalLocations() will be triggered here
            plotRentalLocations();
            loadACenterPointDeleteLater();
        }));
    }    

    private void loadACenterPointDeleteLater() {
        centerpointDot = new Circle(anchorPane.getWidth() / 2, anchorPane.getHeight() / 2, 5);
        System.out.println("centerpointDot will be drawn at: (" + (anchorPane.getWidth() / 2) + ", " + (anchorPane.getHeight() / 2) + ")");
        centerpointDot.setFill(Color.CYAN);
        anchorPane.getChildren().add(centerpointDot);
        centerpointDot.toFront();
    }


    private void loadRentalData() {
        // SQL query to get rental data with the new filters
        String query = """
            SELECT ro.customer_id, c.customer_name, ri.item_delivery_date, ri.item_call_off_date, ro.po_number,
                ordered_contacts.first_name AS ordered_contact_name, ordered_contacts.phone_number AS ordered_contact_phone,
                ri.auto_term, ro.site_name, ro.street_address, ro.city, ri.rental_item_id, l.lift_type,
                l.serial_number, ro.single_item_order, ri.rental_order_id, ro.longitude, ro.latitude,
                site_contacts.first_name AS site_contact_name, site_contacts.phone_number AS site_contact_phone
            FROM customers c
            JOIN rental_orders ro ON c.customer_id = ro.customer_id
            JOIN rental_items ri ON ro.rental_order_id = ri.rental_order_id
            JOIN lifts l ON ri.lift_id = l.lift_id
            LEFT JOIN contacts AS ordered_contacts ON ri.ordered_contact_id = ordered_contacts.contact_id
            LEFT JOIN contacts AS site_contacts ON ri.site_contact_id = site_contacts.contact_id
            WHERE (ri.item_status = 'Called Off')
            OR (ri.item_status = 'Upcoming')
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
                String liftType = rs.getString("lift_type");
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
                        siteContactName, siteContactPhone, latitude, longitude, liftType));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading rental data", e);
        }
    }
    


    // Shared logic for creating rental location dots
    private void createRentalDot(double x, double y, Rental rental) {
        Circle dot = new Circle(x, y, 5);
        dot.setFill(Color.web(Config.getPrimaryColor())); 

        // Apply stroke for the under-effect based on the primary color
        String strokeUnderneath = Config.COLOR_TEXT_MAP.get(Config.getPrimaryColor()) == 1 ? Config.getTertiaryColor() : "WHITE";
        dot.setStroke(Color.web(strokeUnderneath));
        dot.setStrokeWidth(2);

        mapContainer.getChildren().add(dot);

        final double finalX = x > 150 ? x - 205 : x;
        final double finalY = y > 410 ? y - 150 : y;

        // Handle click to show rental details
        dot.setOnMouseClicked(event -> {
            // Remove any existing drive time popup
            anchorPane.getChildren().removeIf(node -> node instanceof PopupCard && ((PopupCard) node).isDriveTimePopup());

            // Remove previous rental popup (if exists)
            if (lastPopup != null && anchorPane.getChildren().contains(lastPopup)) {
                anchorPane.getChildren().remove(lastPopup);
            }

            lastClickedRental = rental;

            // Create and add new popup for the clicked rental
            PopupCard popup = new PopupCard(this, rental, finalX, finalY);
            anchorPane.getChildren().add(popup);
            System.out.println("Rental popup displayed for: " + rental.getName()); // Debugging line
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

    private void plotRentalLocations() {
        for (Rental rental : rentalsForCharting) {
            double x = mapLongitudeToX(rental.getLongitude());
            double y = mapLatitudeToY(rental.getLatitude());

            if (rental.getLongitude() < 0 && rental.getLatitude() > 0) {
                createRentalDot(x, y, rental);
            }
        }
    }

    // This method is used to update the position of all dots (rental locations)
    private void updateRentalLocations() {
        mapContainer.getChildren().removeIf(node -> node instanceof Circle); // Remove old dots before re-plotting
        
        for (Rental rental : rentalsForCharting) {
            double x = mapLongitudeToX(rental.getLongitude());
            double y = mapLatitudeToY(rental.getLatitude());

            if (rental.getLongitude() < 0 && rental.getLatitude() > 0) {
                createRentalDot(x, y, rental);
            }
        }
    }



    private void drawRoutePolyline(String encodedPolyline, String[] colors) {
        List<double[]> polylinePoints = decodePolyline(encodedPolyline);
    
        // **1️⃣ First glow (larger gray glow)**
        Polyline glowPolyline1 = new Polyline();
        glowPolyline1.setStroke(Color.web(Config.getTertiaryColor(), .3));  // Larger gray glow
        glowPolyline1.setStrokeWidth(11);  // Larger width for strong glow
        glowPolyline1.setOpacity(0.5);  // Slight transparency for the glow
        glowPolyline1.setEffect(new GaussianBlur(15));  // Larger blur for stronger glow
    
        // **2️⃣ Second glow (shorter gradient glow)**
        Polyline glowPolyline2 = new Polyline();
        glowPolyline2.setStroke(Color.web(colors[0]));  // Shorter glow with colors[0]
        glowPolyline2.setStrokeWidth(9);  // Slightly smaller width
        glowPolyline2.setOpacity(1);  // Some transparency
        glowPolyline2.setEffect(new GaussianBlur(8));  // Smaller blur for a subtler glow
    
        // **3️⃣ Main polyline with gradient**
        Polyline mainPolyline = new Polyline();
        mainPolyline.setStrokeWidth(4);  // Standard polyline width
        mainPolyline.setStroke(Color.web(colors[1]));  // Apply gradient to the main line
    
        // **4️⃣ Add points to all polylines**
        for (double[] point : polylinePoints) {
            double x = mapLongitudeToX(point[1]);
            double y = mapLatitudeToY(point[0]);
            glowPolyline1.getPoints().addAll(x, y); // Add to first glow (larger, gray)
            glowPolyline2.getPoints().addAll(x, y); // Add to second glow (shorter, gradient)
            mainPolyline.getPoints().addAll(x, y);  // Add to main polyline
        }
    
        // **5️⃣ Add all polylines to the map container**
        mapContainer.getChildren().add(glowPolyline1);  // Add larger gray glow first (behind)
        mapContainer.getChildren().add(glowPolyline2);  // Add smaller gradient glow second (middle)
        mapContainer.getChildren().add(mainPolyline);  // Add main polyline last (on top)
    
        // Ensure proper layering
        mainPolyline.toBack();
        glowPolyline1.toBack();  // Glow1 at the very back
        glowPolyline2.toBack();  // Glow2 behind the main line
        metroMapView.toBack();   // Ensure map is behind everything else
    }
    
    

    private void updateRoutePolylines() {
        // Remove existing polylines before re-plotting
        mapContainer.getChildren().removeIf(node -> node instanceof Polyline);
    
        for (int i = 0; i < storedEncodedPolylines.size(); i++) {
            String encodedPolyline = storedEncodedPolylines.get(i);
            String[] colors = storedColorsList.get(i); // Retrieve associated colors
            drawRoutePolyline(encodedPolyline, colors);
        }
    }
    

    private void setupMetroMap() {
        System.out.println("setupMetroMap triggered");
        try {
            // Load the original metro map image
            Image metroImage = new Image(getClass().getResourceAsStream("/images/stadia_map_z8_size400_800.png"));
            if (metroImage.isError()) {
                throw new RuntimeException("Error loading metro map image.");
            }
    
            // Convert Image to WritableImage for pixel manipulation
            int width = (int) metroImage.getWidth();
            int height = (int) metroImage.getHeight();
            WritableImage recoloredImage = new WritableImage(width, height);
            PixelReader pixelReader = metroImage.getPixelReader();
            PixelWriter pixelWriter = recoloredImage.getPixelWriter();
    
            // 🎨 Get user’s theme color with 50% transparency
            Color baseColor = Color.web(Config.getPrimaryColor()); 
            Color translucentColor = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 0.25);  // 50% opacity
    
            // Loop through each pixel
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color pixelColor = pixelReader.getColor(x, y);
    
                    // If pixel is black, recolor it with the user's theme and 50% transparency
                    if (pixelColor.equals(Color.BLACK)) {
                        pixelWriter.setColor(x, y, translucentColor);
                    } else {
                        pixelWriter.setColor(x, y, pixelColor); // Preserve other colors and transparency
                    }
                }
            }
    
            // Use the modified image
            metroMapView = new ImageView(recoloredImage);
            metroMapView.setPreserveRatio(true);
            metroMapView.setSmooth(true);
            metroMapView.setCache(true);
    
            System.out.println("Made it to defining a mapContainer");

            // Wrap in a Pane for movement
            mapContainer = new Pane(metroMapView);
            mapContainer.setPrefSize(anchorPane.getWidth(), anchorPane.getHeight());
    
            // Clip the image within anchorPane bounds
            Rectangle clipRect = new Rectangle(anchorPane.getWidth(), anchorPane.getHeight() - 2);
            clipRect.setY(5);
            mapContainer.setClip(clipRect);
    
            // Initial centering of the map
            double startX = (anchorPane.getWidth() - metroImage.getWidth()) / 2;
            double startY = (anchorPane.getHeight() - metroImage.getHeight()) / 2;
            metroMapView.setTranslateX(startX);
            metroMapView.setTranslateY(startY);
    
            // Enable panning with bounds
            mapContainer.setOnMousePressed(event -> {
                mapContainer.setUserData(new double[]{event.getSceneX(), event.getSceneY(), metroMapView.getTranslateX(), metroMapView.getTranslateY()});
            });
    
            mapContainer.setOnMouseDragged(event -> {
                double[] data = (double[]) mapContainer.getUserData();
                if (data != null) {
                    double startXPos = data[0];
                    double startYPos = data[1];
                    double startImgX = data[2];
                    double startImgY = data[3];
    
                    double deltaX = event.getSceneX() - startXPos;
                    double deltaY = event.getSceneY() - startYPos;
    
                    double newX = startImgX + deltaX;
                    double newY = startImgY + deltaY;
    
                    double minX = anchorPane.getWidth() - metroImage.getWidth();
                    double maxX = 0;
                    double minY = anchorPane.getHeight() - metroImage.getHeight();
                    double maxY = 0;
    
                    // Apply bounds
                    metroMapView.setTranslateX(Math.max(minX, Math.min(maxX, newX)));
                    metroMapView.setTranslateY(Math.max(minY, Math.min(maxY, newY)));
    
                    updateVisibleMapBounds(metroMapView);
                    updateRentalLocations();
                    updateRoutePolylines();
                }
            });
    
            updateVisibleMapBounds(metroMapView);
    
            // Add map to UI
            anchorPane.getChildren().add(mapContainer);
            mapArea.toFront();
            if (centerpointDot != null) {
                centerpointDot.toFront();
            }
    
            System.out.println("Metro map successfully loaded with dynamic theme color and 50% opacity.");
        } catch (Exception e) {
            System.err.println("Failed to load metro map: " + e.getMessage());
        }
    }


    private void updateVisibleMapBounds(ImageView metroMapView) {
        double latMax = mapBounds[0]; // Top-left latitude
        double lonMin = mapBounds[3]; // Top-left longitude
        double latMin = mapBounds[2]; // Bottom-right latitude
        double lonMax = mapBounds[1]; // Bottom-right longitude
    
        // Get translation values (panning offsets)
        double translateX = metroMapView.getTranslateX();
        double translateY = metroMapView.getTranslateY();
    
        // Get image size (full map)
        double imageWidth = metroMapView.getImage().getWidth();
        double imageHeight = metroMapView.getImage().getHeight();
    
        // Get viewport (visible area)
        double viewportWidth = anchorPane.getWidth();
        double viewportHeight = anchorPane.getHeight();
    
        // Determine the visible pixel range
        double xStart = Math.max(0, -translateX);
        double xEnd = Math.min(imageWidth, xStart + viewportWidth);
        double yStart = Math.max(0, -translateY);
        double yEnd = Math.min(imageHeight, yStart + viewportHeight);
    
        // Convert pixel coordinates to geo-coordinates
        visibleBounds[3] = lonMin + (xStart / imageWidth) * (lonMax - lonMin);
        visibleBounds[1] = lonMin + (xEnd / imageWidth) * (lonMax - lonMin);
        visibleBounds[0] = latMax - (yStart / imageHeight) * (latMax - latMin);
        visibleBounds[2] = latMax - (yEnd / imageHeight) * (latMax - latMin);
    
        // System.out.println("New visible bounds are: "
        // + "Top: " + visibleBounds[0]
        // + ", Right: " + visibleBounds[1]
        // + ", Bottom: " + visibleBounds[2]
        // + ", Left: " + visibleBounds[3]);
    }
       
    
    
        
    

    // -------------   Methods for fully synchronized visual revamp    ------------- //
    // ----------------------------------------------------------------------------- //
    // event sequence after a zoom:
    //      - dot and polyline transition, new tile rendering async, <-- old tile 
    //        timeout to blank if new tile rendering exceeds limit
    //             ~ everything map related -> centered alignments
    //             ~ an underlying scroll pane which grows and shrinks symmetrically from center,
    //                  adds and drops tiles, always contains all fleet data
    //             ~ relative mouse position makes a (2x2?) matrix to transform ALL x, y points
    //


































    // ----------   Code unrelated to fully synchronized visual revamp    ---------- //
    // ----------------------------------------------------------------------------- //
    // i could see us wanting a base scene for routes so let's consider exporting to 
    // their own class


    public double mapLongitudeToX(double lon) {
        double visibleMinX = visibleBounds[3];
        double visibleMaxX = visibleBounds[1];

        // Extract bounds for readability
        double lonMin = mapBounds[3]; // LON_MIN (top-left)
        double lonMax = mapBounds[1]; // LON_MAX (bottom-right)
        
        // Ensure the longitude is within bounds
        if (lon < lonMin || lon > lonMax) {
            return 0;
        }
    
        // Compute relative position (0 to 1)
        double relativeX = (lon - visibleMinX) / (visibleMaxX - visibleMinX);
    
        
        // Map to screen range
        return relativeX * (Config.WINDOW_HEIGHT / 2);
    }
    
    public double mapLatitudeToY(double lat) {
        double visibleMinY = visibleBounds[2]; // Bottom bound of the visible area
        double visibleMaxY = visibleBounds[0]; // Top bound of the visible area
    
        // Extract bounds for readability
        double latMax = mapBounds[0]; // LAT_MAX (top-left)
        double latMin = mapBounds[2]; // LAT_MIN (bottom-right)
    
        // Ensure the latitude is within bounds
        if (lat < latMin || lat > latMax) {
            return 0;
        }

        double observedOffset = .12;
        lat += observedOffset;
    
        // Compute relative position (inverted Y-axis)
        double relativeY = (visibleMaxY - lat) / (visibleMaxY - visibleMinY);

        
    
        // Map to screen range
        return relativeY * Config.WINDOW_HEIGHT;
    }
    
    // Configure the mapArea to fit inside the transitioner after animation
    private void configureMapArea() {
        // Set up the mapArea (the area for the map and dots)
        mapArea.setX(transitioner.getX() + OFFSET_X); // Apply the X offset
        mapArea.setY(transitioner.getY() + OFFSET_Y); // Apply the Y offset
        mapArea.setWidth(transitioner.getWidth() * COMPRESSION_RATIO_X); // Apply compression ratio for width
        mapArea.setHeight(transitioner.getHeight() * COMPRESSION_RATIO_Y); // Apply compression ratio for height
        mapArea.setFill(Color.TRANSPARENT); // Transparent to show dots only
        mapArea.setStroke(Color.BLACK); // Apply border to mapArea
        mapArea.setStrokeWidth(1); // Thin border

        // Add mapArea to the map2Pane
        anchorPane.getChildren().add(mapArea);
    }

    
    public void addStopToRoute(String routeSignifier, Rental rental) {
        System.out.println("Add stop to route called");
        System.out.println("Route signifier: " + routeSignifier);

        String matchedRoute = null;
        
        if (routeSignifier == null) {
            matchedRoute = getARouteNoPreference();
        } else {

            // Check if routeSignifier is a driver’s initials
            for (String[] employee : Config.EMPLOYEES) {
                
                System.out.println("employee[1] is: " + employee[1] + ", and employee[2] is: " 
                    + employee[2]);

                if (routeSignifier.equals(employee[1]) || routeSignifier.equals(employee[2])) {
                    System.out.println("Matched with employee: " + Arrays.toString(employee));

                    // Print routeAssignments before checking for an existing route
                    System.out.println("Current route assignments: " + routeAssignments);

                    // Find an existing route assigned to this driver
                    for (Map.Entry<String, String> entry : routeAssignments.entrySet()) {
                        System.out.println("Checking route: " + entry.getKey() + " assigned to: " + entry.getValue());
                        if (entry.getValue() != null && entry.getValue().equals(routeSignifier)) {
                            matchedRoute = entry.getKey();
                            System.out.println("Found existing route: " + matchedRoute);
                            break;
                        }
                    }

                    // If no assigned route found, assign a new one
                    if (matchedRoute == null) {
                        matchedRoute = "Route " + (routeAssignments.size() + 1);
                        System.out.println("No existing route found, creating new: " + matchedRoute);
                        routes.put(matchedRoute, new ArrayList<>());
                        routeAssignments.put(matchedRoute, routeSignifier);
                    }
                    break; // Exit the employee loop since we found a match
                }
                System.out.println("No match. Moving on");
            }

            System.out.println("We passed employees check for a route signifier");

            // Final check if route was assigned
            if (matchedRoute == null) {
                System.out.println("ERROR: No matching route found for " + routeSignifier);
            } else {
                System.out.println("Final assigned route: " + matchedRoute);
            }

            // If not a driver initial, treat it as a route name directly
            if (matchedRoute == null && routes.containsKey(routeSignifier)) {
                matchedRoute = routeSignifier;
            }
        }
        System.out.println("chose a route and it's: " + matchedRoute);

        // Add stop to the selected route
        routes.get(matchedRoute).add(rental);
        latestRouteEdited = routes.get(matchedRoute);

        // Update UI for the route
        updateRoutePane(matchedRoute, rental);


    }

    private void updateRoutePane(String routeName, Rental rental) {
        System.out.println("update route vbox called, route name is: '" + routeName + 
            "', and rental is: " + rental);
        int routeIndex = getRouteIndex(routeName);
        StackPane routeVBoxPane = routeVBoxPanes.get(routeIndex);
        System.out.println("returned routeVBoxPane is: " + routeVBoxPane);
        StackPane routeHBoxPane = routeHBoxPanes.get(routeIndex);
        Region routeRegion = routeRegions.get(routeIndex);
        VBox routeVBox = routeVBoxes.get(routeIndex);
        HBox routeHBox = routeHBoxes.get(routeIndex);
        String[] colors = getRouteColors(routeName);
        List<Rental> route = routes.get(routeName);
        System.out.println("routeRegion is: " + routeRegion);
        int routeSize = route.size();

        if (routeSize == 1) {
            if (routeVBox != null && routeHBox != null) {
                System.out.println("the route box colors are: " + colors[0] + " and " + colors[1]);
            
                // Default to VBox visible
                routeVBox.setVisible(true);
                routeHBox.setVisible(false);
                System.out.println("routeVBox visible: " + routeVBox.isVisible());
                System.out.println("routeHBox visible: " + routeHBox.isVisible());


                // dynamic logic later
                Random random = new Random();
                double randomX = (200 - 50) * random.nextDouble(); // Random between 50 and 200
                double randomY = 100 + (700 - 100) * random.nextDouble(); // Random between 100 and 700
                routeVBoxPane.setLayoutX(randomX);
                routeVBoxPane.setLayoutY(randomY);
                routeHBoxPane.setLayoutX(randomX + 15);
                routeHBoxPane.setLayoutY(randomY + 15/*region is hiding*/);
            
                // Create a styled HBox chunk for the stop
                System.out.println("made it back from the hbox constructor");
        
                routeVBox.setSpacing(0);
                routeHBox.setSpacing(0);
            
                routeVBoxPane.getChildren().add(routeVBox);
                routeHBoxPane.getChildren().add(routeHBox);

                routeVBoxPane.setStyle("-fx-background-color: " + colors[0] + ";");
                routeHBoxPane.setStyle("-fx-background-color: " + colors[0] + ";");


                // **Filled square (existing one)**
                Rectangle filledSquareV = new Rectangle(7, 7); // 12x12 square
                Rectangle filledSquareH = new Rectangle(7, 7); // 12x12 square
                filledSquareV.setFill(Color.web(colors[1])); // Fill color
                filledSquareH.setFill(Color.web(colors[1])); // Fill color
                filledSquareV.setTranslateX(3);
                filledSquareH.setTranslateX(3);
                filledSquareV.setTranslateY(3);
                filledSquareH.setTranslateY(3);


                // **Outlined frame (slightly larger square)**
                Rectangle outlineSquareV = new Rectangle(12, 12); // Slightly bigger to act as a frame
                Rectangle outlineSquareH = new Rectangle(12, 12); // Slightly bigger to act as a frame
                outlineSquareV.setFill(Color.TRANSPARENT); // No fill, just an outline
                outlineSquareH.setFill(Color.TRANSPARENT); // No fill, just an outline
                outlineSquareV.setStroke(Color.TRANSPARENT); // Same color as the filled square
                outlineSquareH.setStroke(Color.TRANSPARENT); // Same color as the filled square
                outlineSquareV.setStrokeWidth(1); // Thickness of the outline
                outlineSquareH.setStrokeWidth(1); // Thickness of the outline


                Rectangle rightArrowOutlineV = new Rectangle(12, 12);
                Rectangle rightArrowOutlineH = new Rectangle(12, 12);
                rightArrowOutlineV.setFill(Color.TRANSPARENT); // No fill, just an outline
                rightArrowOutlineH.setFill(Color.TRANSPARENT); // No fill, just an outline
                rightArrowOutlineV.setStroke(Color.TRANSPARENT); // Same color as the filled square
                rightArrowOutlineH.setStroke(Color.TRANSPARENT); // Same color as the filled square
                rightArrowOutlineV.setStrokeWidth(1); // Thickness of the outline
                rightArrowOutlineH.setStrokeWidth(1); // Thickness of the outline
                rightArrowOutlineV.setTranslateX(12);
                rightArrowOutlineH.setTranslateX(12);


                Line rightArrowTopV = new Line(3.0,2.0, 10.0, 5.0);
                Line rightArrowTopH = new Line(3.0,2.0, 10.0, 5.0);
                Line rightArrowBottomV = new Line(10.0, 6.0, 3.0, 9.0);
                Line rightArrowBottomH = new Line(10.0, 6.0, 3.0, 9.0);
                rightArrowTopV.setStroke(Color.web(colors[1]));
                rightArrowTopH.setStroke(Color.web(colors[1]));
                rightArrowBottomV.setStroke(Color.web(colors[1]));
                rightArrowBottomH.setStroke(Color.web(colors[1]));
                rightArrowTopV.setStrokeWidth(2);
                rightArrowTopH.setStrokeWidth(2);
                rightArrowBottomV.setStrokeWidth(2);
                rightArrowBottomH.setStrokeWidth(2);
                rightArrowTopV.setTranslateX(14);
                rightArrowTopH.setTranslateX(14);
                rightArrowBottomV.setTranslateX(14);
                rightArrowBottomH.setTranslateX(14);
                rightArrowTopV.setTranslateY(2);
                rightArrowTopH.setTranslateY(2);
                rightArrowBottomV.setTranslateY(5);
                rightArrowBottomH.setTranslateY(5);

                Rectangle downArrowOutlineV = new Rectangle(12, 12);
                Rectangle downArrowOutlineH = new Rectangle(12, 12);
                downArrowOutlineV.setFill(Color.TRANSPARENT); // No fill, just an outline
                downArrowOutlineH.setFill(Color.TRANSPARENT); // No fill, just an outline
                downArrowOutlineV.setStroke(Color.TRANSPARENT); // Same color as the filled square
                downArrowOutlineH.setStroke(Color.TRANSPARENT); // Same color as the filled square
                downArrowOutlineV.setStrokeWidth(1); // Thickness of the outline
                downArrowOutlineH.setStrokeWidth(1); // Thickness of the outline
                downArrowOutlineV.setTranslateY(12);
                downArrowOutlineH.setTranslateY(12);


                Line downArrowLeftV = new Line(2.0,3.0, 5.0, 10.0);
                Line downArrowLeftH = new Line(2.0,3.0, 5.0, 10.0);
                Line downArrowRightV = new Line(6.0, 10.0, 9.0, 3.0);
                Line downArrowRightH = new Line(6.0, 10.0, 9.0, 3.0);
                downArrowLeftV.setStroke(Color.web(colors[1]));
                downArrowLeftH.setStroke(Color.web(colors[1]));
                downArrowRightV.setStroke(Color.web(colors[1]));
                downArrowRightH.setStroke(Color.web(colors[1]));
                downArrowLeftV.setStrokeWidth(2);
                downArrowLeftH.setStrokeWidth(2);
                downArrowRightV.setStrokeWidth(2);
                downArrowRightH.setStrokeWidth(2);
                downArrowLeftV.setTranslateY(14);
                downArrowLeftH.setTranslateY(14);
                downArrowRightV.setTranslateY(14);
                downArrowRightH.setTranslateY(14);
                downArrowLeftV.setTranslateX(2);
                downArrowLeftH.setTranslateX(2);
                downArrowRightV.setTranslateX(5);
                downArrowRightH.setTranslateX(5);

                // **Grouping both squares in a StackPane**
                StackPane squareContainerV = new StackPane(outlineSquareV, filledSquareV, rightArrowOutlineV, 
                                                            rightArrowTopV, rightArrowBottomV, downArrowLeftV,
                                                            downArrowOutlineV, downArrowRightV);
                StackPane squareContainerH = new StackPane(outlineSquareH, filledSquareH, rightArrowOutlineH, 
                                                            rightArrowTopH, rightArrowBottomH, downArrowLeftH,
                                                            downArrowOutlineH, downArrowRightH);
                StackPane.setAlignment(squareContainerV, Pos.TOP_LEFT); // Align to top-left
                StackPane.setAlignment(squareContainerH, Pos.TOP_LEFT); // Align to top-left
                StackPane.setAlignment(outlineSquareV, Pos.TOP_LEFT);
                StackPane.setAlignment(outlineSquareH, Pos.TOP_LEFT);
                StackPane.setAlignment(filledSquareV, Pos.TOP_LEFT);
                StackPane.setAlignment(filledSquareH, Pos.TOP_LEFT);
                StackPane.setAlignment(rightArrowOutlineV, Pos.TOP_LEFT);
                StackPane.setAlignment(rightArrowOutlineH, Pos.TOP_LEFT);
                StackPane.setAlignment(rightArrowTopV, Pos.TOP_LEFT);
                StackPane.setAlignment(rightArrowTopH, Pos.TOP_LEFT);
                StackPane.setAlignment(rightArrowBottomV, Pos.TOP_LEFT);
                StackPane.setAlignment(rightArrowBottomH, Pos.TOP_LEFT);
                StackPane.setAlignment(downArrowOutlineV, Pos.TOP_LEFT);
                StackPane.setAlignment(downArrowOutlineH, Pos.TOP_LEFT);
                StackPane.setAlignment(downArrowLeftV, Pos.TOP_LEFT);
                StackPane.setAlignment(downArrowLeftH, Pos.TOP_LEFT);
                StackPane.setAlignment(downArrowRightV, Pos.TOP_LEFT);
                StackPane.setAlignment(downArrowRightH, Pos.TOP_LEFT);

                Color defaultColor = Color.web(colors[1]);
                Color hoverColor = Color.web(colors[2]);

                Shape[] squareGroupV = {filledSquareV};
                Shape[] squareGroupH = {filledSquareH};
                Shape[] rightGroupV = {rightArrowTopV, rightArrowBottomV};
                Shape[] rightGroupH = {rightArrowTopH, rightArrowBottomH};
                Shape[] downGroupV = {downArrowLeftV, downArrowRightV};
                Shape[] downGroupH = {downArrowLeftH, downArrowRightH};


                addHoverEffectToRouteCard(filledSquareV, squareGroupV,  defaultColor, hoverColor);
                addHoverEffectToRouteCard(filledSquareH, squareGroupH,  defaultColor, hoverColor);
                addHoverEffectToRouteCard(rightArrowTopV, rightGroupV, defaultColor, hoverColor);
                addHoverEffectToRouteCard(rightArrowTopH, rightGroupH, defaultColor, hoverColor);
                addHoverEffectToRouteCard(rightArrowBottomV, rightGroupV, defaultColor, hoverColor);
                addHoverEffectToRouteCard(rightArrowBottomH, rightGroupH, defaultColor, hoverColor);
                addHoverEffectToRouteCard(downArrowLeftV, downGroupV, defaultColor, hoverColor);
                addHoverEffectToRouteCard(downArrowLeftH, downGroupH, defaultColor, hoverColor);
                addHoverEffectToRouteCard(downArrowRightV, downGroupV, defaultColor, hoverColor);
                addHoverEffectToRouteCard(downArrowRightH, downGroupH, defaultColor, hoverColor);
                addHoverEffectToRouteCard(outlineSquareV, squareGroupV, defaultColor, hoverColor);
                addHoverEffectToRouteCard(outlineSquareH, squareGroupH, defaultColor, hoverColor);
                addHoverEffectToRouteCard(rightArrowOutlineV, rightGroupV, defaultColor, hoverColor);
                addHoverEffectToRouteCard(rightArrowOutlineH, rightGroupH, defaultColor, hoverColor);
                addHoverEffectToRouteCard(downArrowOutlineV, downGroupV, defaultColor, hoverColor);
                addHoverEffectToRouteCard(downArrowOutlineH, downGroupH, defaultColor, hoverColor);


                for (Shape arrowV : rightGroupV) {
                    arrowV.setOnMouseClicked(event -> toggleRouteLayout(routeName, rental, "horizontal"));
                }
                for (Shape arrowH : downGroupH) {
                    arrowH.setOnMouseClicked(event -> toggleRouteLayout(routeName, rental, "vertical"));
                }
                rightArrowOutlineV.setOnMouseClicked(event -> toggleRouteLayout(routeName, rental, "horizontal"));
                downArrowOutlineH.setOnMouseClicked(event -> toggleRouteLayout(routeName, rental, "vertical"));


                // Group filledSquareGroup = new Group(outlineSquare, filledSquare);
                // Group rightArrowGroup = new Group(rightArrowOutline, rightArrowTop, rightArrowBottom);
                // Group downArrowGroup = new Group(downArrowOutline, downArrowLeft, downArrowRight);

                // // **Add hover effect to each group**
                // addHoverEffectToRouteCard(filledSquareGroup, defaultColor, hoverColor);
                // addHoverEffectToRouteCard(rightArrowGroup, defaultColor, hoverColor);
                // addHoverEffectToRouteCard(downArrowGroup, defaultColor, hoverColor);

                Image originalFlatbedImage = new Image(getClass().getResourceAsStream("/images/flatbed-even.png"));

                int width= (int) originalFlatbedImage.getWidth();
                int height = (int) originalFlatbedImage.getHeight();

                PixelReader pixelReader = originalFlatbedImage.getPixelReader();
                WritableImage modifiedImage = new WritableImage(width, height);
                PixelWriter pixelWriter = modifiedImage.getPixelWriter();

                Color targetColor = Color.web(colors[1]);
                
                // Iterate over every pixel
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        Color pixelColor = pixelReader.getColor(x, y);
                        
                        // Check if pixel is black (or near black to account for aliasing)
                        if (pixelColor.getRed() < 0.1 && pixelColor.getGreen() < 0.1 && pixelColor.getBlue() < 0.1) {
                            pixelWriter.setColor(x, y, targetColor); // Replace black with target color
                        } else {
                            pixelWriter.setColor(x, y, pixelColor); // Keep other colors the same
                        }
                    }
                }

                ImageView imageView = new ImageView(originalFlatbedImage);
                imageView.setFitWidth(30);
                imageView.setFitHeight(22);
                imageView.setTranslateX(6);
                imageView.setTranslateY(1);


                routeVBoxPane.getChildren().add(imageView);
                routeHBoxPane.getChildren().add(imageView);
                //modifiedImage.setAlignment(Pos.CENTER);
                StackPane.setAlignment(imageView, Pos.BOTTOM_LEFT);

                // Add click event handler to the inner square
                filledSquareV.setOnMouseClicked(event -> {
                    System.out.println("Square clicked!"); // Prints a message when clicked
                });
                filledSquareH.setOnMouseClicked(event -> {
                    System.out.println("Square clicked!"); // Prints a message when clicked
                });

                // Add the grouped squares to the routeVBoxPane
                routeVBoxPane.getChildren().addAll(squareContainerV);
                routeHBoxPane.getChildren().addAll(squareContainerH);

                anchorPane.getChildren().add(routeVBoxPane);
                // as a default, don't add

                routeVBoxPane.setOnMousePressed(event -> {
                    // Store initial position of the mouse when pressed
                    routeVBoxPane.setUserData(new double[]{event.getSceneX(), event.getSceneY()});
                });
                routeHBoxPane.setOnMousePressed(event -> {
                    // Store initial position of the mouse when pressed
                    routeHBoxPane.setUserData(new double[]{event.getSceneX(), event.getSceneY()});
                });
                
                routeVBoxPane.setOnMouseDragged(event -> {
                    double deltaX = event.getSceneX() - ((double[]) routeVBoxPane.getUserData())[0];
                    double deltaY = event.getSceneY() - ((double[]) routeVBoxPane.getUserData())[1];
                
                    double newX = routeVBoxPane.getLayoutX() + deltaX;
                    double newY = routeVBoxPane.getLayoutY() + deltaY;
                
                    // Define boundaries
                    double minX = 0; // Adjust according to your left boundary
                    double maxX = anchorPane.getWidth() - routeVBoxPane.getWidth() - 2; // Prevent from moving out on the right
                    double minY = 20; // Top boundary
                    double maxY = anchorPane.getHeight() - routeVBoxPane.getHeight(); // Bottom boundary
                
                    // Apply boundary constraints
                    routeVBoxPane.setLayoutX(Math.max(minX, Math.min(maxX, newX)));
                    routeVBoxPane.setLayoutY(Math.max(minY, Math.min(maxY, newY)));
                
                    // Update the starting mouse position for the next drag event
                    routeVBoxPane.setUserData(new double[]{event.getSceneX(), event.getSceneY()});
                });

                routeHBoxPane.setOnMouseDragged(event -> {
                    double deltaX = event.getSceneX() - ((double[]) routeHBoxPane.getUserData())[0];
                    double deltaY = event.getSceneY() - ((double[]) routeHBoxPane.getUserData())[1];
                
                    double newX = routeHBoxPane.getLayoutX() + deltaX;
                    double newY = routeHBoxPane.getLayoutY() + deltaY;
                
                    // Define boundaries
                    double minX = 0; // Adjust according to your left boundary
                    double maxX = anchorPane.getWidth() - routeHBoxPane.getWidth() - 2; // Prevent from moving out on the right
                    double minY = 20; // Top boundary
                    double maxY = anchorPane.getHeight() - routeHBoxPane.getHeight(); // Bottom boundary
                
                    // Apply boundary constraints
                    routeHBoxPane.setLayoutX(Math.max(minX, Math.min(maxX, newX)));
                    routeHBoxPane.setLayoutY(Math.max(minY, Math.min(maxY, newY)));
                
                    // Update the starting mouse position for the next drag event
                    routeHBoxPane.setUserData(new double[]{event.getSceneX(), event.getSceneY()});
                });
            }
        } else {
            System.out.println("Beginning of button sequencing logic");
        
            Rental firstRental = route.get(route.size() - 2);
            Rental secondRental = route.get(route.size() - 1);
        
            // Check if firstRental and secondRental are being fetched correctly
            System.out.println("First Rental: " + firstRental.getName() + ", " + firstRental.getLatitude() + ", " + firstRental.getLongitude());
            System.out.println("Second Rental: " + secondRental.getName() + ", " + secondRental.getLatitude() + ", " + secondRental.getLongitude());
        
            String[] googleResults = {"unknown", "unknown"};
            try {
                googleResults = getGoogleRoute(firstRental.getLatitude(), firstRental.getLongitude(),
                                                secondRental.getLatitude(), secondRental.getLongitude());
                // Check googleResults after fetching
                //System.out.println("Google Results: " + googleResults[0] + ", " + googleResults[1]);
            } catch (Exception e) {
                System.out.println("Error while fetching Google route: " + e.getMessage());
            }
        
            // Check if the intermediary VBox is created correctly
            Region intermediaryRegionV = createStopIntermediary(googleResults[0], colors, "vertical");
            Region intermediaryRegionH = createStopIntermediary(googleResults[0], colors, "horizontal");
            VBox intermediaryV = (VBox) intermediaryRegionV;
            HBox intermediaryH = (HBox) intermediaryRegionH;

            
            if (intermediaryV != null) {
                System.out.println("Intermediary VBox created successfully.");
            } else {
                System.out.println("Failed to create Intermediary VBox.");
            }
            if (intermediaryH != null) {
                System.out.println("Intermediary VBox created successfully.");
            } else {
                System.out.println("Failed to create Intermediary VBox.");
            }

            int singleDigitSpacerV = isSingleDigitMinutes(googleResults[0]) ? 10 : 0;
            int singleDigitSpacerH = isSingleDigitMinutes(googleResults[0]) ? 10 : 0;


            routeVBoxPane.getChildren().add(intermediaryV);
            routeHBoxPane.getChildren().add(intermediaryH);
            intermediaryV.setTranslateY(((routeSize - 1) * cardHeightUnit) - 13 + singleDigitSpacerV);
            intermediaryH.setTranslateY(1);
            intermediaryV.setTranslateX(1);
            intermediaryH.setTranslateX(((routeSize - 1) * cardWidthUnit) - 23 + singleDigitSpacerH);


            System.out.println("made it to just before drawing polyline from button sequencing");
        
            drawRoutePolyline(googleResults[1], colors);
            storedEncodedPolylines.add(googleResults[1]);
            storedColorsList.add(colors);
        
            System.out.println("made it to right after drawing polyline from button sequencing");
        
        
            routeVBoxPane.toFront();
            routeVBox.toFront();
            System.out.println("routeVBoxPane children: " + routeHBoxPane.getChildren());
            System.out.println("routeVBoxPane children: " + routeVBoxPane.getChildren());

        }
        
        StackPane rentalChunkV = createRentalChunk(rental, colors, "vertical");
        StackPane rentalChunkH = createRentalChunk(rental, colors, "horizontal");
        routeVBox.getChildren().add(rentalChunkV);
        routeHBox.getChildren().add(rentalChunkH);

        rentalChunkH.setTranslateY(20);
        rentalChunkV.setTranslateY(-8);

        routeVBox.toBack();

        routeVBox.setAlignment(Pos.BOTTOM_RIGHT);
        routeHBox.setAlignment(Pos.BOTTOM_RIGHT);
        routeVBox.setTranslateY(-5);
        routeHBox.setTranslateX(-5);

        routeVBoxPane.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Check for double click
                double x = event.getX(); // X relative to routeBox
                double y = event.getY(); // Y relative to routeBox
                if (x > 15 && y > 14) {
                    System.out.println("Double-clicked at: X=" + x + ", Y=" + y);
                    StackPane cover = createCardOptionsCover(rental);
                    routeVBoxPane.getChildren().add(cover);
                    StackPane.setAlignment(cover, Pos.TOP_LEFT);
                    cover.setTranslateX(12);
                    int yOffset = (closestMultiple((y - 14), cardHeightUnit) * cardHeightUnit) + 14;
                    System.out.println(yOffset);
                    cover.setTranslateY(yOffset);
                }
            }
        });

        routeVBoxPane.setPrefWidth(112);
        routeVBoxPane.setPrefHeight((routeSize * cardHeightUnit) + 28);
        
        routeHBoxPane.setPrefHeight(85);
        routeHBoxPane.setPrefWidth((routeSize * cardWidthUnit) + 20);


    }


    // Creates a visually distinct "chunk" for each Rental stop
    private StackPane createRentalChunk(Rental rental, String[] colors, String orientation) {
        VBox labelBox = new VBox(0); // Vertical box with no spacing between elements
        StackPane rentalChunk = new StackPane(labelBox);
        rentalChunk.setAlignment(Pos.TOP_RIGHT);
        labelBox.setPadding(new Insets(0, 10, 0, 10)); // Top, Right, Bottom, Left padding for VBox container
        labelBox.setAlignment(Pos.CENTER); // Center the content inside the VBox
        labelBox.setMaxWidth(cardWidthUnit); // Maximum width (increased from 70px to 95px)
        labelBox.setMinWidth(cardWidthUnit);
        labelBox.setMinHeight(cardHeightUnit);
        labelBox.setMaxHeight(cardHeightUnit);

        // Define the gradient between colors[1] and colors[0]
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

        // Set the background gradient
        labelBox.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));

        // Set a border
        labelBox.setStyle("-fx-padding: 5;");

        // Character limit for truncation (increased to fit the new width)
        int charLimit = 14; // Adjusted for 95px width

        // Create labels with truncation and set padding for left/right buffer
        Label nameLabel = new Label(truncateText(rental.getName(), charLimit));
        nameLabel.setStyle("-fx-text-fill: " + colors[2] + "; -fx-font-weight: bold;");
        nameLabel.setMaxWidth(95); // Updated width limit
        nameLabel.setPadding(new Insets(0, 4, 0, 4)); // Add buffer on left/right

        Label address2 = new Label(truncateText(rental.getAddressBlockTwo(), charLimit));
        address2.setStyle("-fx-text-fill: " + colors[2] + ";");
        address2.setMaxWidth(95);
        address2.setPadding(new Insets(0, 4, 0, 4)); // Add buffer on left/right

        Label address3 = new Label(truncateText(rental.getAddressBlockThree(), 7));
        address3.setStyle("-fx-text-fill: " + colors[2] + ";");
        address3.setMaxWidth(95);
        address3.setPadding(new Insets(0, 4, 0, 4)); // Add buffer on left/right

        Label liftType = new Label(rental.getLiftType());
        rentalChunk.getChildren().add(liftType);
        liftType.setAlignment(Pos.BOTTOM_RIGHT);
        liftType.setStyle("-fx-font-weight: bold;");
        liftType.setTranslateX(-8);
        StackPane.setAlignment(liftType, Pos.BOTTOM_RIGHT);
        if (orientation.equals("horizontal")) {
            liftType.setTranslateY(-28);
        }

        // Add labels to the VBox
        labelBox.getChildren().addAll(nameLabel, address2, address3);

        return rentalChunk;
    }

    // Utility method to truncate text
    private String truncateText(String text, int limit) {
        if (text.length() > limit) {
            return text.substring(0, limit) + "..."; // Append "..." if exceeding limit
        }
        return text;
    }

    private Region createStopIntermediary(String driveTimeStr, String[] colors, String orientation) {

        // Extract and convert drive time
        driveTimeStr = driveTimeStr.replace("s", "");
        int driveTimeInSeconds = Integer.parseInt(driveTimeStr);
        int driveTimeInMinutes = (int) Math.round(driveTimeInSeconds / 60.0);
        
        // Convert to a string to separate each digit
        String timeNumber = String.valueOf(driveTimeInMinutes); // Just the number
        
        String timeUnit = "m"; // Just "m"
    
        // Create a container for alignment
        StackPane container = new StackPane();
        container.setMinWidth(10); // Keeps it within the leftmost 10px
        container.setAlignment(Pos.CENTER_LEFT); // Centers text inside the container
    
        // Create "m" label (smaller font)
        Label unitLabel = new Label(timeUnit);
        unitLabel.setTranslateY(1);
        unitLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + colors[1] + ";");
        

        if (orientation.equals("vertical")){
            
            VBox intermediary = new VBox(-6);
            VBox textBox = new VBox(-13); // Holds the digits and "m" in a vertical stack
            textBox.setAlignment(Pos.CENTER);
            textBox.setMinWidth(12);
            textBox.setMaxWidth(12);
            textBox.setTranslateX(1);
        
            // Add each digit as a separate label
            for (char digit : timeNumber.toCharArray()) {
                Label digitLabel = new Label(String.valueOf(digit));
                digitLabel.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: " + colors[1] + ";");
                textBox.getChildren().add(digitLabel);
            }

            textBox.getChildren().add(unitLabel); // Add "m" at the bottom
        
            // Add textBox to container
            container.getChildren().add(textBox);
            
            // Add container to intermediary VBox
            intermediary.getChildren().add(container);
        
            return intermediary;


        } else if (orientation.equals("horizontal")) {
            HBox intermediary = new HBox(-6);
            HBox textBox = new HBox(-1); // Holds the digits and "m" in a vertical stack
            textBox.setAlignment(Pos.CENTER);
            textBox.setMinHeight(12);
            textBox.setMaxHeight(12);
            textBox.setTranslateY(-37);
            textBox.setTranslateX(21);
        
            // Add each digit as a separate label
            for (char digit : timeNumber.toCharArray()) {
                Label digitLabel = new Label(String.valueOf(digit));
                digitLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + colors[1] + ";");
                textBox.getChildren().add(digitLabel);
            }

            textBox.getChildren().add(unitLabel); // Add "m" at the bottom
        
            // Add textBox to container
            container.getChildren().add(textBox);
            
            // Add container to intermediary VBox
            intermediary.getChildren().add(container);
        
            return intermediary;
        } else {
            System.out.println("orientation invalid");
            return null;
        }
    }
    
    private StackPane createCardOptionsCover(Rental rental) {
        // Create the cover rectangle
        Rectangle cover = new Rectangle(cardWidthUnit, cardHeightUnit);
        cover.setFill(Color.web("#F4F4F4"));
        cover.setOpacity(0.75); // Adjust opacity to make it slightly transparent
    
        // Load the delete icon from resources
        Image deleteImage = new Image(getClass().getResource("/images/delete.png").toExternalForm());
        ImageView deleteIcon = new ImageView(deleteImage);
        deleteIcon.setFitWidth(30);  // Adjust size as needed
        deleteIcon.setFitHeight(30);
        
        // Add a glow effect
        Glow glow = new Glow(0.8);  // Adjust intensity (0 to 1)
        deleteIcon.setEffect(glow);
    
        // StackPane to center the image on the rectangle
        StackPane stack = new StackPane(cover, deleteIcon);
        stack.setAlignment(Pos.CENTER);
    
        return stack;
    }
    
    
    private void toggleRouteLayout(String routeName, Rental rental, String orientation) {
        System.out.println("toggle route layout clicked");
        String cleanedName = routeName.replaceFirst("(?i)route", "");
        int routeIndex = -1;
        try {
            String numericPart = routeName.replaceAll("[^0-9]", "");
            if (!numericPart.isEmpty()) {
                routeIndex = Integer.parseInt(numericPart);
            } else {
                routeIndex = wordToNumber(cleanedName);
            }
            routeIndex -=1;
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            System.err.println("Error processing routeName: " + routeName);
        }
        
        if (routeIndex >= 0 && routeIndex < routeRegions.size()) {
            switch (orientation) {
                case "horizontal":
                    routeRegions.set(routeIndex, routeHBoxes.get(routeIndex));
                    routeVBoxes.get(routeIndex).setVisible(false);
                    routeHBoxes.get(routeIndex).setVisible(true);
                    System.out.println("routeVBox visible: " + routeVBoxes.get(routeIndex));
                    System.out.println("routeHBox visible: " + routeHBoxes.get(routeIndex));
                    anchorPane.getChildren().remove(routeVBoxPanes.get(routeIndex));
                    anchorPane.getChildren().add(routeHBoxPanes.get(routeIndex));
                    break;
                case "vertical":
                    routeRegions.set(routeIndex, routeVBoxes.get(routeIndex));
                    routeHBoxes.get(routeIndex).setVisible(false);
                    routeVBoxes.get(routeIndex).setVisible(true);
                    System.out.println("routeVBox visible: " + routeVBoxes.get(routeIndex));
                    System.out.println("routeHBox visible: " + routeHBoxes.get(routeIndex));
                    anchorPane.getChildren().remove(routeHBoxPanes.get(routeIndex));
                    anchorPane.getChildren().add(routeVBoxPanes.get(routeIndex));
                    break;
                default:
                    System.err.println("Invalid orientation: " + orientation);
            }
        } else {
            System.err.println("Invalid routeIndex: " + routeIndex);
        }
        

        
    }
    
    
    

    public List<Rental> getRouteStops(String routeName) {
        switch (routeName) {
            case "routeOne": return routeOneStops;
            case "routeTwo": return routeTwoStops;
            case "routeThree": return routeThreeStops;
            case "routeFour": return routeFourStops;
            case "routeFive": return routeFiveStops;
            default: return null;
        }
    }
    
    // Helper method to convert word numbers to integers
    private int wordToNumber(String word) {
        Map<String, Integer> wordMap = Map.of(
            "one", 1, "two", 2, "three", 3, "four", 4, "five", 5,
            "six", 6, "seven", 7, "eight", 8, "nine", 9, "ten", 10
        );
    
        int num = wordMap.getOrDefault(word.toLowerCase(), -1); // Convert to lowercase before lookup
        
        if (num == -1) {
            System.err.println("Warning: Could not map word '" + word + "' to a number.");
        }
        
        return num;
    }
    
    

    private VBox getRouteVBox(String routeName) {
        switch (routeName) {
            case "routeOne": return routeOneBox;
            case "routeTwo": return routeTwoBox;
            case "routeThree": return routeThreeBox;
            case "routeFour": return routeFourBox;
            case "routeFive": return routeFiveBox;
            default: return null;
        }
    }
    

    public int getRouteIndex(String routeName) {
        String cleanedName = routeName.replaceFirst("(?i)route", "");

        try {
            // Extract numeric part
            String numericPart = routeName.replaceAll("[^0-9]", "");
            
            // If numeric part is empty, attempt to map from word numbers
            int routeIndex;
            if (!numericPart.isEmpty()) {
                routeIndex = Integer.parseInt(numericPart);
            } else {
                routeIndex = wordToNumber(cleanedName);
            }
    
            // Convert to zero-based index
            routeIndex -= 1;
            System.out.println("routeName was: " + routeName + ", and routeIndex derived: " + routeIndex);
    
            return routeIndex;
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            System.err.println("Error processing routeName: " + routeName);
            return -1;
        }
    }

    public String[] getRouteColors(String routeName) {
        switch (routeName) {
            case "routeOne": return new String[] {Config.getPrimaryColor(), Config.getSecondaryColor(), 
                                Config.COLOR_TEXT_MAP.get(Config.getSecondaryColor()) == 1 ? Config.getTertiaryColor() : "#ffffff"};
            case "routeTwo": return new String[] {Config.getTertiaryColor(), Config.getPrimaryColor(),
                                Config.COLOR_TEXT_MAP.get(Config.getPrimaryColor()) == 1 ? Config.getTertiaryColor() : "#ffffff"};
            case "routeThree": return new String[] {Config.getSecondaryColor(), Config.getTertiaryColor(), "#ffffff"};
            case "routeFour": return new String[] {Config.getPrimaryColor(), "#ffffff", Config.getTertiaryColor()};
            case "routeFive": return new String[] {Config.getPrimaryColor(), Config.getSecondaryColor(), 
                                Config.COLOR_TEXT_MAP.get(Config.getSecondaryColor()) == 1 ? Config.getTertiaryColor() : "#ffffff"};
            default: return new String[] {Config.getPrimaryColor(), Config.getSecondaryColor(), 
                                Config.COLOR_TEXT_MAP.get(Config.getSecondaryColor()) == 1 ? Config.getTertiaryColor() : "#ffffff"};
        }
    }


    private String[] getGoogleRoute(double originLat, double originLong, double destinationLat, double destinationLong) throws Exception {
        // Construct JSON request body
        String requestBody = String.format(
            "{ \"origin\": { \"location\": { \"latLng\": { \"latitude\": %f, \"longitude\": %f } } }, " +
            "\"destination\": { \"location\": { \"latLng\": { \"latitude\": %f, \"longitude\": %f } } }, " +
            "\"travelMode\": \"DRIVE\", " +
            "\"polylineQuality\": \"OVERVIEW\", " +
            "\"routingPreference\": \"TRAFFIC_AWARE\", " +
            "\"departureTime\": \"2025-10-23T15:00:00Z\" }",
            originLat, originLong, destinationLat, destinationLong
        );
    
        // Set up HTTP connection
        URL url = new URL("https://routes.googleapis.com/directions/v2:computeRoutes");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setRequestProperty("X-Goog-Api-Key", Config.GOOGLE_KEY);
        connection.setRequestProperty("X-Goog-FieldMask", "routes.duration,routes.polyline");
    
        // Convert JSON body to bytes
        byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
    
        // Explicitly set Content-Length header
        connection.setRequestProperty("Content-Length", String.valueOf(input.length));
    
        // Enable sending request body
        connection.setDoOutput(true);
    
        // Send JSON request body
        try (OutputStream os = connection.getOutputStream()) {
            os.write(input);
        }
    
        // Get response code
        int responseCode = connection.getResponseCode();
    
        // Handle non-200 response
        if (responseCode != 200) {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder errorResponse = new StringBuilder();
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorResponse.append(errorLine);
            }
            errorReader.close();
            throw new Exception("HTTP request failed with response code: " + responseCode);
        }
    
        // Read response
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
    
        // Parse the response JSON
        JSONObject jsonResponse = new JSONObject(response.toString());
        if (!jsonResponse.has("routes") || jsonResponse.getJSONArray("routes").isEmpty()) {
            throw new Exception("No routes found in response");
        }
    
        JSONObject route = jsonResponse.getJSONArray("routes").getJSONObject(0);
        if (!route.has("duration")) {
            throw new Exception("Duration not found in response");
        }
    
        String durationString = route.getString("duration"); // e.g., "1441s"
        String polylineString = route.getJSONObject("polyline").getString("encodedPolyline");
    
        // Convert duration to minutes and seconds
        int durationInSeconds = Integer.parseInt(durationString.replace("s", ""));
        int minutes = durationInSeconds / 60;
        int seconds = durationInSeconds % 60;
    

        return new String[]{durationString, polylineString};
    }

    private boolean isSingleDigitMinutes(String durationString) {
        // Remove the 's' character and convert to integer (assuming format like "1441s")
        int durationInSeconds = Integer.parseInt(durationString.replace("s", ""));
        
        // Convert seconds to minutes
        int minutes = durationInSeconds / 60;
        
        // Check if minutes are less than 10
        return minutes < 10;
    }
   

    private List<double[]> decodePolyline(String encoded) {
        List<double[]> polylinePoints = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lon = 0;
    
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1F) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
    
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1F) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlon = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lon += dlon;
    
            double latitude = lat / 1E5;
            double longitude = lon / 1E5;
            polylinePoints.add(new double[]{latitude, longitude});
        }
    
        return polylinePoints;
    }

    
    private String getARouteNoPreference() {
        System.out.println("get a route called");

        // Find the first empty route
        for (Map.Entry<String, List<Rental>> entry : routes.entrySet()) {
            if (entry.getValue().isEmpty()) {
                return entry.getKey();
            }
        }
    
        // Use the last edited route
        if (latestRouteEdited != null) {
            for (Map.Entry<String, List<Rental>> entry : routes.entrySet()) {
                if (entry.getValue() == latestRouteEdited) {
                    return entry.getKey();
                }
            }
        }
    
        // Default to Route 1
        return "routeOne";
    }
  
    private void addHoverEffectToRouteCard(Shape shape, Shape[] shapesToChange, Color defaultColor, Color hoverColor) {
        shape.setOnMouseEntered(e -> {
            for (Shape s : shapesToChange) {
                s.setStroke(hoverColor); // Change outline color
                if (s instanceof Rectangle && ((Rectangle) s).getFill() != Color.TRANSPARENT) {
                    ((Rectangle) s).setFill(hoverColor); // Change fill color
                    ((Rectangle) s).setStrokeWidth(1);
                }
            }
        });
    
        shape.setOnMouseExited(e -> {
            for (Shape s : shapesToChange) {
                s.setStroke(defaultColor); // Revert outline color
                if (s instanceof Rectangle && ((Rectangle) s).getFill() != Color.TRANSPARENT) {
                    ((Rectangle) s).setFill(defaultColor); // Revert fill color
                    ((Rectangle) s).setStrokeWidth(1); // Reset stroke width
                }
            }
        });
    }

    private int closestMultiple(double value, int multiple) {
        int result = (int) (value / multiple);
        
        // Debug print statements
        System.out.println("Input value: " + value);
        System.out.println("Multiple: " + multiple);
        System.out.println("Value / Multiple: " + (value / multiple));
        System.out.println("Casted to int: " + (int) (value / multiple));
        System.out.println("Final result (closest multiple): " + result);
    
        return result;
    }
    
    
    @FXML
    private void resetStage() {
        MaxReachPro.getInstance().collapseStage();
    }
} 
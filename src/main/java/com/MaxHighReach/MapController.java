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
    private double[] truck25;
    private double[] truck06;
    private double[] truck08;
    private double[] truck16;
    private double[] truck20;
    private Random random = new Random();

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


    // New trials for syncing up route polylines in deletions
   // private Map<String, List<Polyline[]>> polylines = new HashMap<>();
    private Map<String, List<String>> encodedPolylines = new HashMap<>();

    private Map<String, String> routeAssignments = new HashMap<>();
    public List<Rental> latestRouteEdited = null;
    private StackPane lastCardCover = null;
    private Rectangle lastSideBarCover = null;
    private Region lastCoveredPane = null;


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
    
        // Instantiate route panes based on Config.NUMBER_OF_TRUCKS
        for (int i = 0; i < Config.NUMBER_OF_TRUCKS; i++) {
            final int index = i;
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
            encodedPolylines.put("route" + (i + 1), new ArrayList<>());


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
                routeHBoxPane.setLayoutX(Math.max(minX, Math.min(maxX, newX)));
                routeHBoxPane.setLayoutY(Math.max(minY, Math.min(maxY, newY)));

            
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
                routeVBoxPane.setLayoutX(Math.max(minX, Math.min(maxX, newX)));
                routeVBoxPane.setLayoutY(Math.max(minY, Math.min(maxY, newY)));

            
                // Update the starting mouse position for the next drag event
                routeHBoxPane.setUserData(new double[]{event.getSceneX(), event.getSceneY()});
            });

            String routeKey = getRouteNameFromIndex(i);

            routeVBoxPane.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) { // Check for double click
                    double x = Math.floor(event.getX() * 10) / 10.0; // X relative to routeBox
                    double y = Math.floor(event.getY() * 10) / 10.0; // Y relative to routeBox
    
                    removeCardCovers();
                    
                    if (x > 15 && y > 14) {
                        int multiple = closestMultiple(y - 14, cardHeightUnit);
                        int routeSize = routes.get(routeKey).size();
                        if (multiple + 1 <= routeSize) {
                            int yOffset = (multiple * (cardHeightUnit)) - ((routeSize - 1) * 27);
                            lastCardCover = createCardOptionsCover(routes.get(routeKey).get(multiple), multiple, x, y, index);
                            routeVBoxPane.getChildren().add(lastCardCover);
                            lastCoveredPane = routeVBoxPane;
                            StackPane.setAlignment(lastCardCover, Pos.BOTTOM_LEFT);
                            lastCardCover.setTranslateX(5);
                            lastCardCover.setTranslateY(yOffset);
                        }
                    }
                }
            });

            routeHBoxPane.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) { // Check for double click
                    System.out.println("card covered triggered");
                    double x = Math.floor(event.getX() * 10) / 10.0; // X relative to routeBox
                    double y = Math.floor(event.getY() * 10) / 10.0; // Y relative to routeBox
    
                    removeCardCovers();
                    
                    if (x > 15 && y > 14) {
                        int multiple = closestMultiple(x - 14, cardWidthUnit);
                        int routeSize = routes.get(routeKey).size();
                        if (multiple + 1 <= routeSize) {
                            int xOffset = multiple * (cardWidthUnit - 2) - ((routeSize) * 20) - ((routeSize - 1) * 22) + 10 + ((3 - routeSize) * 5);
                            lastCardCover = createCardOptionsCover(routes.get(routeKey).get(multiple), multiple, x, y, index);
                            routeHBoxPane.getChildren().add(lastCardCover);
                            lastCoveredPane = routeHBoxPane;
                            StackPane.setAlignment(lastCardCover, Pos.BOTTOM_LEFT);
                            lastCardCover.setTranslateY(5);
                            lastCardCover.setTranslateX(xOffset);
                        }
                    }
                }
            });

            routeVBox.setAlignment(Pos.BOTTOM_RIGHT);
            routeHBox.setAlignment(Pos.BOTTOM_RIGHT);
            routeVBox.setTranslateY(-5);
            routeHBox.setTranslateX(-5);
    
            routeVBoxPane.setPickOnBounds(false);
            routeHBoxPane.setPickOnBounds(false);


        }
    }
    
    // New async method to load rental data
    private void loadRentalDataAsync() {
        CompletableFuture.runAsync(() -> {
            loadRentalData();  // Load rental data in the background
        }).thenRun(() -> Platform.runLater(() -> {
            // Once data is loaded, plotRentalLocations() will be triggered here
            plotRentalLocations();
            loadTruck();
        }));
    }    


    private void loadTruck() {
        try {
            truck25 = FleetAPIClient.getTruckCoordsByName("2025");
            truck16 = randomizeCoords(new double[]{Config.SHOP_LAT, Config.SHOP_LON});
            truck08 = randomizeCoords(truck25);
            truck06 = randomizeCoords(truck25);
            truck20 = randomizeCoords(truck25);
            updateTrucks();
        } catch (IOException e) {
            e.printStackTrace(); // or log the error / show a message to the user
        }
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






    private Polyline[] drawRoutePolyline(String encodedPolyline, String[] colors) {
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
    
        return new Polyline[]{mainPolyline, glowPolyline1, glowPolyline2};
    }


    


    private void updateRoutePolylines() {
        // Remove existing polylines before re-plotting
        mapContainer.getChildren().removeIf(node -> node instanceof Polyline);
      //  polylines.clear();
    
        // for (int i = 0; i < storedEncodedPolylines.size(); i++) {
        //     String encodedPolyline = storedEncodedPolylines.get(i);
        //     String[] colors = storedColorsList.get(i); // Retrieve associated colors
        //     drawRoutePolyline(encodedPolyline, colors);
        // }
    
        for (Map.Entry<String, List<String>> entry : encodedPolylines.entrySet()) {
            List<String> routeSegments = entry.getValue();
            String[] colors = getRouteColors(entry.getKey());
            for (String segmentPolyline : routeSegments) {
                drawRoutePolyline(segmentPolyline, colors);
            }
        }
    
    }

    private void updateTrucks() {
        mapContainer.getChildren().removeIf(node ->
            node instanceof Circle && Color.CYAN.equals(((Circle) node).getFill())
        );
        plotTruck(truck25);
        plotTruck(truck06);
        plotTruck(truck08);
        plotTruck(truck16);
        plotTruck(truck20);
    }

    private void plotTruck(double[] coords) {
        if (coords == null) return;
        Circle truckDot = new Circle(mapLongitudeToX(coords[1]), mapLatitudeToY(coords[0]), 5);
        truckDot.setFill(Color.CYAN);
        mapContainer.getChildren().add(truckDot);
        truckDot.toFront();
    }
    


    private void setupMetroMap() {
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
                removeCardCovers();
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
                    updateTrucks();
                }
            });
    
            updateVisibleMapBounds(metroMapView);
    
            // Add map to UI
            anchorPane.getChildren().add(mapContainer);
            mapArea.toFront();
            updateTrucks();
    
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
        String matchedRoute = null;
        int index = 99;
        if (routeSignifier == null) {
            String[] route = getARouteNoPreference();
            matchedRoute = route[0];
            index = Integer.parseInt(route[1]);
        } else {
            // Check if routeSignifier is a driver’s initials
            for (String[] employee : Config.EMPLOYEES) {
                if (routeSignifier.equals(employee[1]) || routeSignifier.equals(employee[2])) {
                    // Print routeAssignments before checking for an existing route
                    int counter = 0;
                    // Find an existing route assigned to this driver
                    for (Map.Entry<String, String> entry : routeAssignments.entrySet()) {
                        if (entry.getValue() != null && entry.getValue().equals(routeSignifier)) {
                            matchedRoute = entry.getKey();
                            index = counter;
                            break;
                        }
                        counter++;
                    }

                    // If no assigned route found, assign a new one
                    if (matchedRoute == null) {
                        matchedRoute = "Route " + (routeAssignments.size() + 1);
                        index = routes.size();
                        routes.put(matchedRoute, new ArrayList<>());
                        routeAssignments.put(matchedRoute, routeSignifier);
                    }
                    break; // Exit the employee loop since we found a match
                }
            }
            // If not a driver initial, treat it as a route name directly
            if (matchedRoute == null && routes.containsKey(routeSignifier)) {
                matchedRoute = routeSignifier;
                index = wordToNumber(matchedRoute.replace("route","")) - 1;
            }
        }

        // Add stop to the selected route
        routes.get(matchedRoute).add(rental);
        latestRouteEdited = routes.get(matchedRoute);
        // Update UI for the route
        updateRoutePane(matchedRoute, rental, "insertion", 99, index);
    }

    private void addTruckToRoute(String routeSignifier, double[] truck) {
        String matchedRoute = null;
        int index = 0;

        // If not a driver initial, treat it as a route name directly
        if (matchedRoute == null && routes.containsKey(routeSignifier)) {
            matchedRoute = routeSignifier;
            index = wordToNumber(matchedRoute.replace("route","")) - 1;
        }

        String city = getCityFromCoordinates(truck[0], truck[1]);

        Rental truckLocation = new Rental(null, "", null,
            null, null, null, null, false,
            null, "truck's location", city, 0,
            null, false, 0, null, null, truck[0],
            truck[1], "");

        
    }


    private void removeStopFromRoute(Rental rental, int closestMultiple, int routeIndex) {
        System.out.println("**    removeStopFromRoute called with:" +
            "\n- closestMultiple: " + closestMultiple +
            "\n- routeIndex: " + routeIndex + " **");
            // Get route key from index
        String routeKey = getRouteNameFromIndex(routeIndex);


        List<Rental> stopList;
        if (routes.get(routeKey) != null) {
            stopList = routes.get(routeKey);
        } else {
            System.err.println("stopList is null");
            return;
        }

        if (closestMultiple < 0 || closestMultiple >= stopList.size()) {
            System.out.println("closestMultiple out of bounds. Exiting method.");
            return;
        }
    
        Rental removed = stopList.remove(closestMultiple);
    


    
        removeCardCovers();


        updateRoutePane(routeKey, rental, "deletion", closestMultiple, routeIndex);


    }
    

    private void updateRoutePane(String routeName, Rental rental, String orientation,
                                int closestMultiple, int index) {


        System.out.println("Updating route pane with: " +
            "\n- routeName: " + routeName +
            "\n- rental: " + rental +
            "\n- orientation: " + orientation +
            "\n- closestMultiple: " + closestMultiple +
            "\n- index: " + index + 
            "-------------------------");


        int tempIndex = index;
        if (orientation.equals("insertion")) {
            tempIndex = getRouteIndex(routeName);
        }
        final int routeIndex = tempIndex;
        // ^ maybe not the optimal way but i think this is needed since this method
        // contains itself in a setOnAction response
        
        StackPane routeVBoxPane = routeVBoxPanes.get(routeIndex);
        StackPane routeHBoxPane = routeHBoxPanes.get(routeIndex);
        Region routeRegion = routeRegions.get(routeIndex);
        VBox routeVBox = routeVBoxes.get(routeIndex);
        HBox routeHBox = routeHBoxes.get(routeIndex);
        String[] colors = getRouteColors(routeName);
        List<Rental> route = routes.get(routeName);
        int routeSize = route.size();


        if (orientation.equals("insertion")) {
            if (routeSize == 1) {
                if (routeVBox != null && routeHBox != null) {
                
                    // Default to VBox visible
                    routeVBox.setVisible(true);
                    routeHBox.setVisible(false);




                    // dynamic logic later
                    Random random = new Random();
                    double randomX = (200 - 50) * random.nextDouble(); // Random between 50 and 200
                    double randomY = 100 + (700 - 100) * random.nextDouble(); // Random between 100 and 700
                    routeVBoxPane.setLayoutX(randomX);
                    routeVBoxPane.setLayoutY(randomY);
                    routeHBoxPane.setLayoutX(randomX);
                    routeHBoxPane.setLayoutY(randomY);
                
                    // Create a styled HBox chunk for the stop
            
                    routeVBox.setSpacing(0);
                    routeHBox.setSpacing(0);
                
                    routeVBoxPane.getChildren().add(routeVBox);
                    routeHBoxPane.getChildren().add(routeHBox);


                    routeVBoxPane.setStyle("-fx-background-color: " + colors[0] + ";");
                    routeHBoxPane.setStyle("-fx-background-color: " + colors[0] + ";");




                    // **Filled square (existing one)**
                    // Rectangle filledSquareV = new Rectangle(7, 7); // 12x12 square
                    // Rectangle filledSquareH = new Rectangle(7, 7); // 12x12 square
                    // filledSquareV.setFill(Color.web(colors[1])); // Fill color
                    // filledSquareH.setFill(Color.web(colors[1])); // Fill color
                    // filledSquareV.setTranslateX(3);
                    // filledSquareH.setTranslateX(3);
                    // filledSquareV.setTranslateY(3);
                    // filledSquareH.setTranslateY(3);




                    // **Outlined frame (slightly larger square)**
                    // Rectangle outlineSquareV = new Rectangle(12, 12); // Slightly bigger to act as a frame
                    // Rectangle outlineSquareH = new Rectangle(12, 12); // Slightly bigger to act as a frame
                    // outlineSquareV.setFill(Color.TRANSPARENT); // No fill, just an outline
                    // outlineSquareH.setFill(Color.TRANSPARENT); // No fill, just an outline
                    // outlineSquareV.setStroke(Color.TRANSPARENT); // Same color as the filled square
                    // outlineSquareH.setStroke(Color.TRANSPARENT); // Same color as the filled square
                    // outlineSquareV.setStrokeWidth(1); // Thickness of the outline
                    // outlineSquareH.setStrokeWidth(1); // Thickness of the outline

                    Rectangle rightArrowOutlineV = new Rectangle(12, 12);
                    // Rectangle rightArrowOutlineH = new Rectangle(12, 12);
                    rightArrowOutlineV.setFill(Color.TRANSPARENT); // No fill, just an outline
                    // rightArrowOutlineH.setFill(Color.TRANSPARENT); // No fill, just an outline
                    rightArrowOutlineV.setStroke(Color.TRANSPARENT); // Same color as the filled square
                    // rightArrowOutlineH.setStroke(Color.TRANSPARENT); // Same color as the filled square
                    rightArrowOutlineV.setStrokeWidth(1); // Thickness of the outline
                    // rightArrowOutlineH.setStrokeWidth(1); // Thickness of the outline
                    rightArrowOutlineV.setTranslateX(1);
                    // rightArrowOutlineH.setTranslateX(12);

                    Line rightArrowTopV = new Line(3.0,2.0, 10.0, 5.0);
                    // Line rightArrowTopH = new Line(3.0,2.0, 10.0, 5.0);
                    Line rightArrowBottomV = new Line(10.0, 6.0, 3.0, 9.0);
                    // Line rightArrowBottomH = new Line(10.0, 6.0, 3.0, 9.0);
                    rightArrowTopV.setStroke(Color.web(colors[1]));
                    // rightArrowTopH.setStroke(Color.web(colors[1]));
                    rightArrowBottomV.setStroke(Color.web(colors[1]));
                    // rightArrowBottomH.setStroke(Color.web(colors[1]));
                    rightArrowTopV.setStrokeWidth(2);
                    // rightArrowTopH.setStrokeWidth(2);
                    rightArrowBottomV.setStrokeWidth(2);
                    // rightArrowBottomH.setStrokeWidth(2);
                    rightArrowTopV.setTranslateX(2);
                    // rightArrowTopH.setTranslateX(14);
                    rightArrowBottomV.setTranslateX(2);
                    // rightArrowBottomH.setTranslateX(14);
                    rightArrowTopV.setTranslateY(2);
                    // rightArrowTopH.setTranslateY(2);
                    rightArrowBottomV.setTranslateY(5);
                    // rightArrowBottomH.setTranslateY(5);


                    // Rectangle downArrowOutlineV = new Rectangle(12, 12);
                    Rectangle downArrowOutlineH = new Rectangle(12, 12);
                    // downArrowOutlineV.setFill(Color.TRANSPARENT); // No fill, just an outline
                    downArrowOutlineH.setFill(Color.TRANSPARENT); // No fill, just an outline
                    // downArrowOutlineV.setStroke(Color.TRANSPARENT); // Same color as the filled square
                    downArrowOutlineH.setStroke(Color.TRANSPARENT); // Same color as the filled square
                    // downArrowOutlineV.setStrokeWidth(1); // Thickness of the outline
                    downArrowOutlineH.setStrokeWidth(1); // Thickness of the outline
                    // downArrowOutlineV.setTranslateY(12);
                    downArrowOutlineH.setTranslateY(1);




                    // Line downArrowLeftV = new Line(2.0,3.0, 5.0, 10.0);
                    Line downArrowLeftH = new Line(2.0,3.0, 5.0, 10.0);
                    // Line downArrowRightV = new Line(6.0, 10.0, 9.0, 3.0);
                    Line downArrowRightH = new Line(6.0, 10.0, 9.0, 3.0);
                    // downArrowLeftV.setStroke(Color.web(colors[1]));
                    downArrowLeftH.setStroke(Color.web(colors[1]));
                    // downArrowRightV.setStroke(Color.web(colors[1]));
                    downArrowRightH.setStroke(Color.web(colors[1]));
                    // downArrowLeftV.setStrokeWidth(2);
                    downArrowLeftH.setStrokeWidth(2);
                    // downArrowRightV.setStrokeWidth(2);
                    downArrowRightH.setStrokeWidth(2);
                    // downArrowLeftV.setTranslateY(14);
                    downArrowLeftH.setTranslateY(2);
                    // downArrowRightV.setTranslateY(14);
                    downArrowRightH.setTranslateY(2);
                    // downArrowLeftV.setTranslateX(2);
                    downArrowLeftH.setTranslateX(2);
                    // downArrowRightV.setTranslateX(5);
                    downArrowRightH.setTranslateX(5);


                    // **Grouping both squares in a StackPane**
                    StackPane squareContainerV = new StackPane(/*outlineSquareV, filledSquareV,*/ rightArrowOutlineV, 
                                                                rightArrowTopV, rightArrowBottomV/*, downArrowLeftV,
                                                                downArrowOutlineV, downArrowRightV*/);
                    StackPane squareContainerH = new StackPane(/*outlineSquareH, filledSquareH, rightArrowOutlineH, 
                                                                rightArrowTopH, rightArrowBottomH,*/ downArrowLeftH,
                                                                downArrowOutlineH, downArrowRightH);
                    StackPane.setAlignment(squareContainerV, Pos.TOP_LEFT); // Align to top-left
                    StackPane.setAlignment(squareContainerH, Pos.TOP_LEFT); // Align to top-left
                    // StackPane.setAlignment(outlineSquareV, Pos.TOP_LEFT);
                    // StackPane.setAlignment(outlineSquareH, Pos.TOP_LEFT);
                    // StackPane.setAlignment(filledSquareV, Pos.TOP_LEFT);
                    // StackPane.setAlignment(filledSquareH, Pos.TOP_LEFT);
                    StackPane.setAlignment(rightArrowOutlineV, Pos.TOP_LEFT);
                    // StackPane.setAlignment(rightArrowOutlineH, Pos.TOP_LEFT);
                    StackPane.setAlignment(rightArrowTopV, Pos.TOP_LEFT);
                    // StackPane.setAlignment(rightArrowTopH, Pos.TOP_LEFT);
                    StackPane.setAlignment(rightArrowBottomV, Pos.TOP_LEFT);
                    // StackPane.setAlignment(rightArrowBottomH, Pos.TOP_LEFT);
                    // StackPane.setAlignment(downArrowOutlineV, Pos.TOP_LEFT);
                    StackPane.setAlignment(downArrowOutlineH, Pos.TOP_LEFT);
                    // StackPane.setAlignment(downArrowLeftV, Pos.TOP_LEFT);
                    StackPane.setAlignment(downArrowLeftH, Pos.TOP_LEFT);
                    // StackPane.setAlignment(downArrowRightV, Pos.TOP_LEFT);
                    StackPane.setAlignment(downArrowRightH, Pos.TOP_LEFT);


                    Color defaultColor = Color.web(colors[1]);
                    Color hoverColor = routeName == "routeTwo" ? Color.web("#FFFFFF") : Color.web(colors[2]);


                    // Shape[] squareGroupV = {filledSquareV};
                    // Shape[] squareGroupH = {filledSquareH};
                    Shape[] rightGroupV = {rightArrowTopV, rightArrowBottomV};
                    Shape[] downGroupH = {downArrowLeftH, downArrowRightH};

                    // addHoverEffectToRouteCard(/*filledSquareV, squareGroupV,  */defaultColor, hoverColor);
                    // addHoverEffectToRouteCard(/*filledSquareH, squareGroupH,  */defaultColor, hoverColor);
                    addHoverEffectToRouteCard(rightArrowTopV, rightGroupV, defaultColor, hoverColor);
                    addHoverEffectToRouteCard(rightArrowBottomV, rightGroupV, defaultColor, hoverColor);
                    addHoverEffectToRouteCard(downArrowLeftH, downGroupH, defaultColor, hoverColor);
                    addHoverEffectToRouteCard(downArrowRightH, downGroupH, defaultColor, hoverColor);
                    // addHoverEffectToRouteCard(/*outlineSquareV, squareGroupV,*/ defaultColor, hoverColor);
                    // addHoverEffectToRouteCard(/*outlineSquareH, squareGroupH, */defaultColor, hoverColor);
                    addHoverEffectToRouteCard(rightArrowOutlineV, rightGroupV, defaultColor, hoverColor);
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


                    Image originalFlatbedImage = new Image(getClass().getResourceAsStream("/images/truck-face.png"));

                    int width = (int) originalFlatbedImage.getWidth();
                    int height = (int) originalFlatbedImage.getHeight();
                    
                    PixelReader pixelReader = originalFlatbedImage.getPixelReader();
                    WritableImage modifiedImage = new WritableImage(width, height);
                    PixelWriter pixelWriter = modifiedImage.getPixelWriter();
                    
                    Color contentColor = Color.web(colors[1]); // black fill (already used in GIMP)
                    Color outlineColor = Color.BLACK; // Replace this with your desired outline color
                    
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            Color pixelColor = pixelReader.getColor(x, y);
                    
                            // Preserve full transparency
                            if (pixelColor.getOpacity() == 0.0) {
                                pixelWriter.setColor(x, y, pixelColor);
                                continue;
                            }
                    
                            // If pixel is near black, keep it as content color
                            if (isNearBlack(pixelColor)) {
                                pixelWriter.setColor(x, y, contentColor);
                            }
                    
                            // If pixel is near gray (outline), recolor to outlineColor
                            else if (isNearGray(pixelColor)) {
                                Color recolored = new Color(
                                    outlineColor.getRed(),
                                    outlineColor.getGreen(),
                                    outlineColor.getBlue(),
                                    pixelColor.getOpacity() // preserve original alpha
                                );
                                pixelWriter.setColor(x, y, recolored);
                            }
                    
                            // Otherwise, keep the pixel as-is
                            else {
                                pixelWriter.setColor(x, y, pixelColor);
                            }
                        }
                    }
                    
                    ImageView imageView = new ImageView(modifiedImage);
                    
                    imageView.setFitWidth(11);
                    imageView.setFitHeight(11);
                    imageView.setTranslateX(13);
                    imageView.setTranslateY(-cardHeightUnit);




                    routeVBoxPane.getChildren().add(imageView);
                    routeHBoxPane.getChildren().add(imageView);
                    //modifiedImage.setAlignment(Pos.CENTER);
                    StackPane.setAlignment(imageView, Pos.BOTTOM_LEFT);


                    // Add click event handler to the inner square
                    // filledSquareV.setOnMouseClicked(event -> {
                    // });
                    // filledSquareH.setOnMouseClicked(event -> {
                    // });


                    // Add the grouped squares to the routeVBoxPane
                    routeVBoxPane.getChildren().addAll(squareContainerV);
                    routeHBoxPane.getChildren().addAll(squareContainerH);

                    Label truckIdLabelV = new Label(" ? ");
                    truckIdLabelV.setTranslateY(2);
                    truckIdLabelV.setTranslateX(0);
                    truckIdLabelV.setTextFill(Color.web(colors[1]));
                    truckIdLabelV.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                    
                    Color baseColor = Color.web(colors[1]);
                    
                    truckIdLabelV.setOnMouseEntered(e -> truckIdLabelV.setTextFill(hoverColor));
                    truckIdLabelV.setOnMouseExited(e -> truckIdLabelV.setTextFill(baseColor));
                    
                    // 👇 Add the click handler
                    truckIdLabelV.setOnMouseClicked(e -> handleTruckExpander(routeVBoxPane, colors, routeName));
                    
                    routeVBoxPane.getChildren().add(truckIdLabelV);
                    StackPane.setAlignment(truckIdLabelV, Pos.BOTTOM_LEFT);
                    
                    anchorPane.getChildren().add(routeVBoxPane);
                    // as a default, don't add routeHBoxPane

                }
            } else {
                if (closestMultiple == 99) {
                    Rental firstRental = route.get(route.size() - 2);
                    Rental secondRental = route.get(route.size() - 1);
                
                    String[] googleResults = {"unknown", "unknown"};
                    try {
                        googleResults = getGoogleRoute(firstRental.getLatitude(), firstRental.getLongitude(),
                                                        secondRental.getLatitude(), secondRental.getLongitude());
                        // Check googleResults after fetching
                        //System.out.println("Google Results: " + googleResults[0] + ", " + googleResults[1]);
                    } catch (Exception e) {
                    }
                
                    // Check if the intermediary VBox is created correctly
                    Region intermediaryRegionV = createStopIntermediary(googleResults[0], colors, "vertical");
                    Region intermediaryRegionH = createStopIntermediary(googleResults[0], colors, "horizontal");
                    VBox intermediaryV = (VBox) intermediaryRegionV;
                    HBox intermediaryH = (HBox) intermediaryRegionH;

                    int singleDigitSpacerV = isSingleDigitMinutes(googleResults[0]) ? 10 : 0;
                    int singleDigitSpacerH = isSingleDigitMinutes(googleResults[0]) ? 10 : 0;

                    routeVBoxPane.getChildren().add(intermediaryV);
                    routeHBoxPane.getChildren().add(intermediaryH);
                    intermediaryV.setTranslateY(((routeSize - 1) * cardHeightUnit) - 13 + singleDigitSpacerV);
                    intermediaryH.setTranslateY(8);
                    intermediaryV.setTranslateX(-1);
                    intermediaryH.setTranslateX(((routeSize - 1) * cardWidthUnit) - 27 + singleDigitSpacerH);

                    intermediaryV.setClip(new Rectangle(15, 45));

                    //drawRoutePolyline(googleResults[1], colors);
                    encodedPolylines.get("route" + (index + 1)).add(googleResults[1]);
                    //  storedEncodedPolylines.add(googleResults[1]);
                    updateRoutePolylines();
                
                    routeVBoxPane.toFront();
                    routeVBox.toFront();
                } else {

                }

            }


            StackPane rentalChunkV = createRentalChunk(rental, colors, "vertical");
            StackPane rentalChunkH = createRentalChunk(rental, colors, "horizontal");
            routeVBox.getChildren().add(rentalChunkV);
            routeHBox.getChildren().add(rentalChunkH);
    
            rentalChunkH.setTranslateY(14);
            rentalChunkH.setTranslateX(5);
            rentalChunkV.setTranslateY(3);
    
        } else if (orientation.equals("deletion")) {
            
            System.out.println("routeHBoxPane children before a deletion is: " + routeHBoxPane.getChildren());
            
            if (routeSize == 0) {
                routeVBox.setVisible(false);
                routeHBox.setVisible(false);
                routeVBoxPane.getChildren().removeAll(routeVBoxPane.getChildren());
                routeHBoxPane.getChildren().removeAll(routeHBoxPane.getChildren());
                anchorPane.getChildren().remove(routeVBoxPane);
                anchorPane.getChildren().remove(routeHBoxPane);
            } else {
                if (routeSize == 1) {
                    routeVBoxPane.getChildren().remove(3);
                    routeHBoxPane.getChildren().remove(3);
                    encodedPolylines.get("route" + (index + 1)).remove(0);
                    // no longer need for any int's
                } else if (closestMultiple == 0) {
                    routeVBoxPane.getChildren().remove(3);
                    routeHBoxPane.getChildren().remove(3);
                    for (int i = 3; i < routeVBoxPane.getChildren().size(); i++) {
                        VBox vbox = (VBox) routeVBoxPane.getChildren().get(i);
                        double currentY = vbox.getTranslateY();
                        vbox.setTranslateY(currentY - cardHeightUnit);
                    }
                    for (int i = 3; i < routeHBoxPane.getChildren().size(); i++) {
                        HBox hbox = (HBox) routeHBoxPane.getChildren().get(i);
                        double currentX = hbox.getTranslateX();
                        hbox.setTranslateX(currentX - cardWidthUnit);
                    }
                    encodedPolylines.get("route" + (index + 1)).remove(0);
                    // shift up other int's
                } else if (closestMultiple == routeSize) {
                    routeVBoxPane.getChildren().remove(closestMultiple + 2);
                    routeHBoxPane.getChildren().remove(closestMultiple + 2);
                    encodedPolylines.get("route" + (index + 1)).remove(closestMultiple - 1);
                    // no shifting int's
                } else {
                    routeVBoxPane.getChildren().remove(closestMultiple + 2);
                    routeHBoxPane.getChildren().remove(closestMultiple + 2);
                    // recalculating: 
                    //      - removing at index closestMultiple + 3 too
                    // shifting int's also
                    for (int i = closestMultiple + 2; i < routeVBoxPane.getChildren().size(); i++) {
                        VBox vbox = (VBox) routeVBoxPane.getChildren().get(i);
                        double currentY = vbox.getTranslateY();
                        vbox.setTranslateY(currentY - cardHeightUnit);
                    }
                    for (int i = closestMultiple + 2; i < routeHBoxPane.getChildren().size(); i++) {
                        HBox hbox = (HBox) routeHBoxPane.getChildren().get(i);
                        double currentX = hbox.getTranslateX();
                        hbox.setTranslateX(currentX - cardWidthUnit);
                    }

                    Rental newLinkStart = route.get(closestMultiple - 1);
                    Rental newLinkEnd = route.get(closestMultiple);
                    String [] newLink = {"unknown", "unknown"};
                    try {   
                        newLink = getGoogleRoute(newLinkStart.getLatitude(), 
                                newLinkStart.getLongitude(), newLinkEnd.getLatitude(),
                                newLinkEnd.getLongitude());
                    } catch (Exception e) {
                    }
                    Region newIntV = createStopIntermediary(newLink[0], colors, "vertical");
                    Region newIntH = createStopIntermediary(newLink[0], colors, "horizontal");
                    VBox newIntVBox = (VBox) newIntV;
                    HBox newIntHBox = (HBox) newIntH;
                    int singleDigitSpacerV = isSingleDigitMinutes(newLink[0]) ? 10 : 0;
                    int singleDigitSpacerH = isSingleDigitMinutes(newLink[0]) ? 10 : 0;
                    newIntVBox.setTranslateY(((closestMultiple) * cardHeightUnit) - 13 + singleDigitSpacerV);
                    newIntHBox.setTranslateY(1);
                    newIntVBox.setTranslateX(1);
                    newIntHBox.setTranslateX((closestMultiple * cardWidthUnit) - 23 + singleDigitSpacerH);
                    routeVBoxPane.getChildren().set(closestMultiple + 2, newIntVBox);
                    routeHBoxPane.getChildren().set(closestMultiple + 2, newIntHBox);

                    encodedPolylines.get("route" + (index + 1)).remove(closestMultiple - 1);
                  
                    encodedPolylines.get("route" + (index + 1)).set(closestMultiple - 1, newLink[1]);
                    //  VBox vbox = (VBox) routeVBoxPane.getChildren().get(closestMultiple + 1);




                }
                
            updateRoutePolylines();
            }
            routeVBox.getChildren().remove(closestMultiple);
            routeHBox.getChildren().remove(closestMultiple);
        }
        
        routeVBox.toBack();
        routeHBox.toBack();

        routeVBoxPane.setPrefWidth(112);
        routeVBoxPane.setPrefHeight((routeSize * cardHeightUnit) + 1);
        
        routeHBoxPane.setPrefHeight(69);
        routeHBoxPane.setPrefWidth((routeSize * cardWidthUnit) - 6);


        routeVBoxPane.setClip(new Rectangle(112, routeSize * cardHeightUnit + 28));
        routeHBoxPane.setClip(new Rectangle((routeSize * cardWidthUnit) + 20, 85));
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

        double endX = orientation.equals("vertical") ? 0 : 1;
        double endY = orientation.equals("vertical") ? 1 : 0;

        // Define the gradient between colors[1] and colors[0]
        LinearGradient gradient = new LinearGradient(
            0, 0, endX, endY, // Horizontal gradient
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
        liftType.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colors[2] + ";");
        liftType.setTranslateX(-8);
        StackPane.setAlignment(liftType, Pos.BOTTOM_RIGHT);
        if (orientation.equals("horizontal")) {
            liftType.setTranslateY(-17);
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
            return null;
        }
    }
    
    private StackPane createCardOptionsCover(Rental rental, int closestMultiple, double x, double y, int routeIndex) {
        // Create the cover rectangle
        Rectangle cover = new Rectangle(cardWidthUnit - 14, cardHeightUnit);
        cover.setFill(Color.web("#F4F4F4"));
        cover.setOpacity(0.75); // Adjust opacity to make it slightly transparent
    
        // Load the delete icon from resources
        Image deleteImage = new Image(getClass().getResource("/images/delete.png").toExternalForm());
        ImageView deleteIcon = new ImageView(deleteImage);
        deleteIcon.setFitWidth(30);  // Adjust size as needed
        deleteIcon.setFitHeight(30);
        
        deleteIcon.setOnMouseClicked(event -> {
            removeStopFromRoute(rental, closestMultiple, routeIndex);
        });
        deleteIcon.setPickOnBounds(true);
        deleteIcon.setMouseTransparent(false);


        // Add a glow effect
        Glow glow = new Glow(0.8);  // Adjust intensity (0 to 1)
        deleteIcon.setEffect(glow);
    
        // StackPane to center the image on the rectangle
        StackPane stack = new StackPane(cover, deleteIcon/*,
             new Label(String.valueOf(closestMultiple) + " & x,y: " + x + "," + y)*/);
        stack.setAlignment(Pos.CENTER);
        stack.setFocusTraversable(true);
        //stack.setClip(new Rectangle(cardWidthUnit +1000, cardHeightUnit + 1000));
        stack.setPickOnBounds(false);
        return stack;
    }


    private void removeCardCovers() {
        if (lastCardCover != null && lastCardCover.getParent() != null) {
            Pane parentPane = (Pane) lastCardCover.getParent();
            parentPane.getChildren().remove(lastCardCover);
            
            System.out.println("Removed lastCardCover from:");
            for (Node child : parentPane.getChildren()) {
                System.out.println(" - " + child);
            }
        }
        lastCardCover = null;
        lastCoveredPane = null;
    
        if (lastSideBarCover != null && lastSideBarCover.getParent() != null) {
            Pane parentPane = (Pane) lastSideBarCover.getParent();
            parentPane.getChildren().remove(lastSideBarCover);
            
            // Remove the last VBox if present
            for (int i = parentPane.getChildren().size() - 1; i >= 0; i--) {
                Node child = parentPane.getChildren().get(i);
                if (child instanceof VBox) {
                    parentPane.getChildren().remove(i);
                    break;
                }
            }
    
            System.out.println("Removed lastSideBarCover and last VBox from:");
            for (Node child : parentPane.getChildren()) {
                System.out.println(" - " + child);
            }
        }
        lastSideBarCover = null;
    }
    
    


    private void handleDeleteStop(Rental rental, int closestMultiple, int routeIndex) {
    }
    
    
    private void toggleRouteLayout(String routeName, Rental rental, String orientation) {
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
                    anchorPane.getChildren().remove(routeVBoxPanes.get(routeIndex));
                    anchorPane.getChildren().add(routeHBoxPanes.get(routeIndex));
                    routeHBoxPanes.get(routeIndex).setLayoutX(Math.min(routeHBoxPanes.get(routeIndex).getLayoutX(), 400 - (routes.get(routeName).size() + cardWidthUnit)));
                    break;
                case "vertical":
                    routeRegions.set(routeIndex, routeVBoxes.get(routeIndex));
                    routeHBoxes.get(routeIndex).setVisible(false);
                    routeVBoxes.get(routeIndex).setVisible(true);
                    anchorPane.getChildren().remove(routeHBoxPanes.get(routeIndex));
                    anchorPane.getChildren().add(routeVBoxPanes.get(routeIndex));
                    break;
                default:
            }
        } else {
        }
        
        removeCardCovers();
        
    }
    
    
    private void handleTruckExpander(StackPane routePane, String[] colors, String routeName) {
        removeCardCovers();
        String hoverColor = routeName.equals("routeTwo") ? "#FFFFFF" : colors[2];
        // Count only VBox children
        long routeSize = routePane.getChildren().stream()
            .filter(node -> node instanceof VBox)
            .count();
    
        int height = (int) (routeSize * cardHeightUnit) + 1;
    
        // Sidebar Rectangle
        lastSideBarCover = new Rectangle(11, height);
        lastSideBarCover.setFill(Color.web(colors[0]));
        lastSideBarCover.setTranslateX(1);
        routePane.getChildren().add(lastSideBarCover);
        StackPane.setAlignment(lastSideBarCover, Pos.CENTER_LEFT);
    
        // Sidebar VBox with labels
        VBox labelColumn = new VBox();
        labelColumn.setPrefWidth(11);
        labelColumn.setPrefHeight(height);
        labelColumn.setTranslateX(1);
        labelColumn.setAlignment(Pos.TOP_CENTER);
    
        // Dynamic vertical spacing
        labelColumn.setSpacing(-6 + ((routeSize - 1) * 13));
    
        String[] labelTexts = {"06", "08", "16", "20", "25"};
        for (String text : labelTexts) {
            Label label = new Label(text);
            label.setStyle("-fx-font-size: 10px; -fx-text-fill: " + colors[1] + "; -fx-font-weight: bold");
            label.setTranslateX(-cardWidthUnit / 2 - 1);
    
            // Hover effect
            label.setOnMouseEntered(e -> label.setStyle("-fx-font-size: 10px; -fx-text-fill: " + hoverColor + "; -fx-font-weight: bold"));
            label.setOnMouseExited(e -> label.setStyle("-fx-font-size: 10px; -fx-text-fill: " + colors[1] + "; -fx-font-weight: bold"));
    
            // Click action
            label.setOnMouseClicked(e -> handleTruckAssignment(routePane, text));
    
            labelColumn.getChildren().add(label);
        }
    
        routePane.getChildren().add(labelColumn);
        StackPane.setAlignment(labelColumn, Pos.CENTER_LEFT);
    }
    
    
    private void handleTruckAssignment(StackPane routePane, String labelString) {
        for (Node node : routePane.getChildren()) {
            if (node instanceof Label label) {
                label.setText(labelString);
                label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
                label.setScaleX(0.75);
                label.setTranslateX(-1);
                break;
            }
        }
        removeCardCovers();
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
    
            return routeIndex;
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            System.err.println("Error processing routeName: " + routeName);
            return -1;
        }
    }


    private String getRouteNameFromIndex(int index){
        String routeKey = switch (index) {
            case 0 -> "routeOne";
            case 1 -> "routeTwo";
            case 2 -> "routeThree";
            case 3 -> "routeFour";
            case 4 -> "routeFive";
            default -> null;
        };
        return routeKey;
    }


    public String[] getRouteColors(String routeName) {
        String primary = Config.getPrimaryColor();
        String secondary = Config.getSecondaryColor();
        String tertiary = Config.getTertiaryColor();
    
        switch (routeName) {
            case "route1":
            case "routeOne":
                return new String[]{
                    primary,
                    secondary,
                    Config.COLOR_TEXT_MAP.getOrDefault(secondary, 0) == 1 ? tertiary : "#ffffff"
                };
    
            case "route2":
            case "routeTwo":
                return new String[]{
                    tertiary,
                    primary,
                    Config.COLOR_TEXT_MAP.getOrDefault(primary, 0) == 1 ? tertiary : "#ffffff"
                };
    
            case "route3":
            case "routeThree":
                return new String[]{
                    secondary,
                    tertiary,
                    "#ffffff"
                };
    
            case "route4":
            case "routeFour":
                return new String[]{
                    primary,
                    "#ffffff",
                    tertiary
                };
    
            case "route5":
            case "routeFive":
                return new String[]{
                    primary,
                    secondary,
                    Config.COLOR_TEXT_MAP.getOrDefault(secondary, 0) == 1 ? tertiary : "#ffffff"
                };
    
            default:
                // Fallback route colors
                return new String[]{
                    primary,
                    secondary,
                    Config.COLOR_TEXT_MAP.getOrDefault(secondary, 0) == 1 ? tertiary : "#ffffff"
                };
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

    private String getCityFromCoordinates(double lat, double lon) {
        String cityName = "Unknown";
        try {
            String urlString = String.format(
                "https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&key=%s",
                lat, lon, Config.GOOGLE_KEY
            );

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
            );
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(response.toString());
            JSONArray results = json.getJSONArray("results");

            if (results.length() > 0) {
                JSONArray addressComponents = results.getJSONObject(0).getJSONArray("address_components");

                for (int i = 0; i < addressComponents.length(); i++) {
                    JSONObject component = addressComponents.getJSONObject(i);
                    JSONArray types = component.getJSONArray("types");

                    for (int j = 0; j < types.length(); j++) {
                        if (types.getString(j).equals("locality")) {
                            cityName = component.getString("long_name");
                            break;
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return cityName;
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


    
    private String[] getARouteNoPreference() {


        // Find the first empty route
        int index = 0;
        for (Map.Entry<String, List<Rental>> entry : routes.entrySet()) {
            if (entry.getValue().isEmpty()) {
                return new String[] { entry.getKey(), String.valueOf(index) };
            }
            index++;
        }
    
        index = 0;
        // Use the last edited route
        if (latestRouteEdited != null) {
            for (Map.Entry<String, List<Rental>> entry : routes.entrySet()) {
                if (entry.getValue() == latestRouteEdited) {
                    return new String[] { entry.getKey(), String.valueOf(index) };
                }
            }
            index++;
        }
    
        // Default to Route 1
        return new String[]{ "route1", "0" };
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
        
        return result;
    }

    private double[] randomizeCoords(double[] baseCoords) {
        double latOffset = -0.3 + 0.6 * random.nextDouble();
        double lonOffset = -0.3 + 0.6 * random.nextDouble();
        return new double[]{baseCoords[0] + latOffset, baseCoords[1] + lonOffset};
    }
    
    private boolean isNearGray(Color color) {
        double r = color.getRed();
        double g = color.getGreen();
        double b = color.getBlue();
        double avg = (r + g + b) / 3.0;
        return Math.abs(r - avg) < 0.05 && Math.abs(g - avg) < 0.05 && Math.abs(b - avg) < 0.05;
    }
    
    private boolean isNearBlack(Color color) {
        return color.getRed() < 0.15 && color.getGreen() < 0.15 && color.getBlue() < 0.15;
    }
    
    @FXML
    private void resetStage() {
        MaxReachPro.getInstance().collapseStage();
    }
} 
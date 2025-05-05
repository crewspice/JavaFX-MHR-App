package com.MaxHighReach;


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
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
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

    private Map<String, Circle> pinpointersV = new HashMap<>();
    private Map<String, Circle> innerPinpointersV = new HashMap<>();
    private Map<String, Label> driverLabelsV = new HashMap<>();
    private Map<String, Circle> pinpointersH = new HashMap<>();
    private Map<String, Circle> innerPinpointersH = new HashMap<>();
    private Map<String, Label> driverLabelsH = new HashMap<>();

    private Map<String, String> routeAssignments = new HashMap<>();
    private Map<String, String> truckAssignments = new HashMap<>();
    private Map<String, double[]> truckCoords = new HashMap<>();
    public List<Rental> latestRouteEdited = null;
    private StackPane lastCardCover = null;
    private Rectangle lastSideBarCover = null;
    private Region lastCoveredPane = null;
    private Circle lastPinpointer = null;
    private Label lastDriverLabel = null;
    private Map<String, Circle> trucks = new HashMap<>();

    private double[] truck25;
    private double[] truck06;
    private double[] truck08;
    private double[] truck16;
    private double[] truck20;

    // // Define your route lists (ensure these are populated somewhere, otherwise they will be null)
    // private List<Rental> routeOneStops = new ArrayList<>();
    // private List<Rental> routeTwoStops = new ArrayList<>();
    // private List<Rental> routeThreeStops = new ArrayList<>();
    // private List<Rental> routeFourStops = new ArrayList<>();
    // private List<Rental> routeFiveStops = new ArrayList<>();

    // // Initialize routes map (you can do this in a constructor or any other initialization block)
    // {
    //     routes.put("route1", routeOneStops);
    //     routes.put("route2", routeTwoStops);
    //     routes.put("route3", routeThreeStops);
    //     routes.put("route4", routeFourStops);
    //     routes.put("route5", routeFiveStops);
    // }

    // Initialize routeBoxes map (same here, ensure the VBox components are defined before using)
    private VBox routeOneBox, routeTwoBox, routeThreeBox, routeFourBox, routeFiveBox;

    {
        routeBoxes.put("routeOne", routeOneBox);
        routeBoxes.put("routeTwo", routeTwoBox);
        routeBoxes.put("routeThree", routeThreeBox);
        routeBoxes.put("routeFour", routeFourBox);
        routeBoxes.put("routeFive", routeFiveBox);
    }

    {
        truckCoords.put("06", truck06);
        truckCoords.put("08", truck08);
        truckCoords.put("16", truck16);
        truckCoords.put("20", truck20);
        truckCoords.put("25", truck25);
    }
   
    private int cardHeightUnit = 55;
    private int cardWidthUnit = 100;



    private Timeline progressTicker;
    private Button progressStarterButtonDeleteLater;

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
            progressStarterButtonDeleteLater.toFront();
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
            Circle pinpointerV = new Circle(8);
            Circle pinpointerH = new Circle(8);
            Circle innerPinpointerV = new Circle(6);
            Circle innerPinpointerH = new Circle(6);
            Label driverLabelV = new Label("L");
            Label driverLabelH = new Label("H");
    
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
            pinpointersV.put("route" + (i + 1), pinpointerV);
            pinpointersH.put("route" + (i + 1), pinpointerH);
            innerPinpointersV.put("route" + (i + 1), innerPinpointerV);
            innerPinpointersH.put("route" + (i + 1), innerPinpointerH);
            driverLabelsV.put("route" + (i + 1), driverLabelV);
            driverLabelsH.put("route" + (i + 1), driverLabelH);


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
                
         //       System.out.println(">> Clicked on: " + event.getTarget());
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
                
                Node clickedNode = event.getPickResult().getIntersectedNode();
                if (clickedNode != null) {
                    System.out.println("Clicked node: " + clickedNode);
                    System.out.println("Node ID: " + clickedNode.getId());
                    System.out.println("Node class: " + clickedNode.getClass().getSimpleName());
                }

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

        progressStarterButtonDeleteLater = new Button("start progressions");
        anchorPane.getChildren().add(progressStarterButtonDeleteLater);
        progressStarterButtonDeleteLater.setOnAction(e -> {
            startAllTruckProgress();
            System.out.println("progress starter clicked");
        });
        progressStarterButtonDeleteLater.setTranslateY(20);
    
    }
    
    // New async method to load rental data
    private void loadRentalDataAsync() {
        CompletableFuture.runAsync(() -> {
            // Only background work here
            loadRentalDataFromAPI();
        }).thenRun(() -> {
            // All UI updates happen together here
            Platform.runLater(() -> {
                plotRentalLocations();
                updateTruckCoordinates();
                updateTrucks();
                reflectRoutingData();
                updateRoutePolylines();
            });
        });
    } 


    private void loadTruckFromDirectCall() {
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

    public void updateTruckCoordinates() {
        try {
            URL url = new URL("http://api.maxhighreach.com:8080/routes/fleet");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
    
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
    
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
    
            JSONArray fleetArray = new JSONArray(response.toString());
    
            for (int i = 0; i < fleetArray.length(); i++) {
                JSONObject truck = fleetArray.getJSONObject(i);
                String name = truck.getString("name");
                double lat = truck.getDouble("lat");
                double lng = truck.getDouble("lng");
    
                switch (name) {
                    case "2025":
                        truck25 = new double[] { lat, lng };
                        break;
                    case "2006":
                        truck06 = new double[] { lat, lng };
                        break;
                    case "2008":
                        truck08 = new double[] { lat, lng };
                        break;
                    case "2016":
                        truck16 = new double[] { lat, lng };
                        break;
                    case "2020":
                        truck20 = new double[] { lat, lng };
                        break;
                }
            }
    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    private void loadRentalDataFromAPI() {
        String apiUrl = "http://api.maxhighreach.com:8080/routes/stops";

        try {
            // Call the API
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            reader.close();

            // Parse JSON and extract list of IDs
            JsonArray jsonArray = JsonParser.parseString(responseBuilder.toString()).getAsJsonArray();
            List<Integer> apiIds = new ArrayList<>();
            for (JsonElement element : jsonArray) {
                JsonObject obj = element.getAsJsonObject();
                apiIds.add(obj.get("id").getAsInt());
            }

            if (apiIds.isEmpty()) {
                System.out.println("No stops received from API.");
                rentalsForCharting = new ArrayList<>();
                return;
            }

            // SQL query that filters by rental_item_id from API
            StringBuilder placeholders = new StringBuilder("?");
            for (int i = 1; i < apiIds.size(); i++) {
                placeholders.append(",?");
            }

            String query = """
                SELECT ro.customer_id, c.customer_name, ri.item_delivery_date, ri.item_call_off_date, ro.po_number,
                    ordered_contacts.first_name AS ordered_contact_name, ordered_contacts.phone_number AS ordered_contact_phone,
                    ri.auto_term, ro.site_name, ro.street_address, ro.city, ri.rental_item_id, l.lift_type,
                    l.serial_number, ro.single_item_order, ri.rental_order_id, ro.longitude, ro.latitude,
                    site_contacts.first_name AS site_contact_name, site_contacts.phone_number AS site_contact_phone,
                    ri.driver, ri.driver_number, ri.driver_initial, ri.delivery_truck, ri.pick_up_truck
                FROM customers c
                JOIN rental_orders ro ON c.customer_id = ro.customer_id
                JOIN rental_items ri ON ro.rental_order_id = ri.rental_order_id
                JOIN lifts l ON ri.lift_id = l.lift_id
                LEFT JOIN contacts AS ordered_contacts ON ri.ordered_contact_id = ordered_contacts.contact_id
                LEFT JOIN contacts AS site_contacts ON ri.site_contact_id = site_contacts.contact_id
                WHERE ri.rental_item_id IN (%s)
            """.formatted(placeholders);

            try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                for (int i = 0; i < apiIds.size(); i++) {
                    preparedStatement.setInt(i + 1, apiIds.get(i));
                }

                try (ResultSet rs = preparedStatement.executeQuery()) {
                    rentalsForCharting = new ArrayList<>();

                    while (rs.next()) {
                        Rental rental = new Rental(
                            rs.getString("customer_id"),
                            rs.getString("customer_name"),
                            rs.getString("item_delivery_date"),
                            rs.getString("item_call_off_date"),
                            rs.getString("po_number"),
                            rs.getString("ordered_contact_name"),
                            rs.getString("ordered_contact_phone"),
                            rs.getBoolean("auto_term"),
                            rs.getString("site_name"),
                            rs.getString("street_address"),
                            rs.getString("city"),
                            rs.getInt("rental_item_id"),
                            rs.getString("serial_number"),
                            rs.getBoolean("single_item_order"),
                            rs.getInt("rental_order_id"),
                            rs.getString("site_contact_name"),
                            rs.getString("site_contact_phone"),
                            rs.getDouble("latitude"),
                            rs.getDouble("longitude"),
                            rs.getString("lift_type")
                        );
                        rental.setDriver(rs.getString("driver"));
                        rental.setDriverInitial(rs.getString("driver_initial"));
                        rental.setDriverNumber(rs.getInt("driver_number"));
                        rental.setDeliveryTruck(rs.getString("delivery_truck"));
                        rental.setPickUpTruck(rs.getString("pick_up_truck"));
                        rentalsForCharting.add(rental);
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load rental data from API guide", e);
        }
    }



    private void loadRentalDataFromSQL() {
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
    
    private void syncRoutingToDB() {
        String queryUpdateWithTruck = """
            UPDATE rental_items
            SET driver_initial = ?, driver_number = ?, driver = ?, delivery_truck = ?
            WHERE rental_item_id = ?
        """;
    
        String queryUpdateWithoutTruck = """
            UPDATE rental_items
            SET driver_initial = ?, driver_number = ?, driver = ?, delivery_truck = NULL
            WHERE rental_item_id = ?
        """;
    
        String queryClearUnassigned = """
            UPDATE rental_items
            SET driver_initial = NULL, driver_number = 0, driver = NULL, delivery_truck = NULL
            WHERE rental_item_id NOT IN (%s)
        """;
    
        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             PreparedStatement psWithTruck = connection.prepareStatement(queryUpdateWithTruck);
             PreparedStatement psWithoutTruck = connection.prepareStatement(queryUpdateWithoutTruck)) {
    
            connection.setAutoCommit(false);
            List<Integer> allAssignedIds = new ArrayList<>();
    
            List<String> withTruckBatchPreview = new ArrayList<>();
            List<String> withoutTruckBatchPreview = new ArrayList<>();
    
            for (Map.Entry<String, List<Rental>> entry : routes.entrySet()) {
                String routeKey = entry.getKey();
                List<Rental> rentals = entry.getValue();
                String driverInitial = routeAssignments.get(routeKey); 
                boolean hasTruck = false;
                String assignedTruckId = null;
    
                for (Map.Entry<String, String> truckEntry : truckAssignments.entrySet()) {
                    if (truckEntry.getValue().equals(routeKey)) {
                        hasTruck = true;
                        assignedTruckId = truckEntry.getKey();
                        break;
                    }
                }
    
    
                String truckString = assignedTruckId;
                int offset = hasTruck ? 0 : 1;
    
                for (int i = 0; i < rentals.size(); i++) {
                    Rental rental = rentals.get(i);
                    int rentalId = rental.getRentalItemId();
    
                    // 🚫 Skip "ghost" entries where rentalItemId == 0
                    if (rentalId == 0) continue;
    
                    int driverNumber = i + offset;
                    String driverFull = (driverInitial != null) ? driverInitial + driverNumber : null;
    
                    if (hasTruck) {
                        psWithTruck.setString(1, driverInitial);
                        psWithTruck.setInt(2, driverNumber);
                        psWithTruck.setString(3, driverFull);
                        psWithTruck.setString(4, truckString);
                        psWithTruck.setInt(5, rentalId);
                        psWithTruck.addBatch();
    
                        withTruckBatchPreview.add(
                            String.format("(%s, %d, %s, %s, %d)", driverInitial, driverNumber, driverFull, truckString, rentalId)
                        );
                    } else {
                        psWithoutTruck.setString(1, driverInitial);
                        psWithoutTruck.setInt(2, driverNumber);
                        psWithoutTruck.setString(3, driverFull);
                        psWithoutTruck.setInt(4, rentalId);
                        psWithoutTruck.addBatch();
    
                        withoutTruckBatchPreview.add(
                            String.format("(%s, %d, %s, %d)", driverInitial, driverNumber, driverFull, rentalId)
                        );
                    }
    
                    allAssignedIds.add(rentalId);
                }
            }
    
            // Preview batches
            // System.out.println("\n🟢 WITH TRUCK Batch Preview:");
            // for (String row : withTruckBatchPreview) {
            //     System.out.println(row);
            // }
    
            // System.out.println("\n🟡 WITHOUT TRUCK Batch Preview:");
            // for (String row : withoutTruckBatchPreview) {
            //     System.out.println(row);
            // }
    
            // Execute batches
            psWithTruck.executeBatch();
            psWithoutTruck.executeBatch();
    
            // Clear unassigned
            if (!allAssignedIds.isEmpty()) {
                String inClause = allAssignedIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                String finalClearQuery = String.format(queryClearUnassigned, inClause);
    
                try (Statement clearStatement = connection.createStatement()) {
                    clearStatement.executeUpdate(finalClearQuery);
                }
            }
    
            connection.commit();
    
        } catch (SQLException e) {
            e.printStackTrace(); // or use a logger
        }
    }
    
    
    
    
    private void reflectRoutingData() {
        if (rentalsForCharting == null || rentalsForCharting.isEmpty()) {
            return;
        }
    
        // Step 1: Filter out rentals where driverInitial is null or driverNumber is 0
        List<Rental> validRentals = rentalsForCharting.stream()
                .filter(rental -> rental.getDriverInitial() != null && rental.getDriverNumber() != 0)
                .toList();
    
        if (validRentals.isEmpty()) {
            return;
        }
    
        // Step 2: Group valid rentals by driverInitial (A, B, I, etc.)
        Map<String, List<Rental>> groupedByDriverInitial = validRentals.stream()
                .collect(Collectors.groupingBy(Rental::getDriverInitial));
    
        // Step 3: Sort each group by driverNumber
        for (Map.Entry<String, List<Rental>> entry : groupedByDriverInitial.entrySet()) {
            List<Rental> rentals = entry.getValue();
            rentals.sort(Comparator.comparingInt(Rental::getDriverNumber));
        }
    
        // Step 4: Create routes and populate the global routes map
        for (Map.Entry<String, List<Rental>> entry : groupedByDriverInitial.entrySet()) {
            String routeKey = getARouteNoPreference()[0]; // Get route key (customizable)
            routes.put(routeKey, entry.getValue());
            routeAssignments.put(routeKey, entry.getKey());
            int routeIndex = getRouteIndex(routeKey);
            StackPane vBoxPane = routeVBoxPanes.get(routeIndex);
            StackPane hBoxPane = routeHBoxPanes.get(routeIndex);

            // Process the rentals and update the route pane
            for (Rental rental : entry.getValue()) {
                int closestMultiple = entry.getValue().indexOf(rental);
                updateRoutePane(routeKey, rental, "insertion", closestMultiple, routeIndex, "program");
                // routeVBoxPanes.get(routeIndex).setVisible(true);
                // routeVBoxPanes.get(routeIndex).toFront();
            }
    
            // After all the updates, check if any rental in the route has a non-null deliveryTruck
            String truckSignifier = entry.getValue().stream()
                    .filter(rental -> rental.getDeliveryTruck() != null)
                    .map(Rental::getDeliveryTruck)
                    .findFirst()  // If multiple trucks are non-null, we pick the first one
                    .orElse(null);  // If no trucks are found, we use null
    
            // If a deliveryTruck is found, call addTruckToRoute
            if (truckSignifier != null) {
                // Print the route key and truck signifier before calling addTruckToRoute
                addTruckToRoute(routeKey, truckSignifier, "program");
                VBox vBox = (VBox) vBoxPane.getChildren().get(0);
                HBox hBox = (HBox) hBoxPane.getChildren().get(0);
                StackPane vBoxTruckOuterPane = (StackPane) vBox.getChildren().get(0);
                StackPane hBoxTruckOuterPane = (StackPane) hBox.getChildren().get(0);
                StackPane vBoxTruckPane = (StackPane) vBoxTruckOuterPane.getChildren().get(0);
                StackPane hBoxTruckPane = (StackPane) hBoxTruckOuterPane.getChildren().get(0);
                for (Node node : vBoxTruckPane.getChildren()) {
                    if (node instanceof Label) {
                        ((Label) node).setText(truckSignifier);
                        break; // assuming only one label
                    }
                }
                for (Node node : hBoxTruckPane.getChildren()) {
                    if (node instanceof Label) {
                        ((Label) node).setText(truckSignifier);
                        break; // assuming only one label
                    }
                }
            }

            driverLabelsV.get(routeKey).setText(entry.getKey());
            driverLabelsH.get(routeKey).setText(entry.getKey());
        }

        // Step 5: Done
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
        plotTruck(truck25, "25");
        plotTruck(truck06, "06");
        plotTruck(truck08, "08");
        plotTruck(truck16, "16");
        plotTruck(truck20, "20");
    }

    private void plotTruck(double[] coords, String truckName) {
        if (coords == null) return;
        Circle truckDot = new Circle(mapLongitudeToX(coords[1]), mapLatitudeToY(coords[0]), 5);
        if (trucks.containsKey(truckName)) {
            trucks.remove(truckName);
        }
        truckDot.setFill(Color.CYAN);
        trucks.put(truckName, truckDot);
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
        
    public void addStopToRoute(String routeSignifier, Rental rental) {
        System.out.println("-- addStopToRoute called with routeKey == " + routeSignifier);
        System.out.println("-- for reference, routes is: " + routes);
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
                        matchedRoute = "Route " + (routes.size() + 1);
                        index = routes.size();
                        routes.put(matchedRoute, new ArrayList<>());
                    //    routeAssignments.put(matchedRoute, routeSignifier);
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
        updateRoutePane(matchedRoute, rental, "insertion", 99, index, "user");
        System.out.println("added stop to route and routeAssignments is: " + routeAssignments);
    }

    private void addTruckToRoute(String routeSignifier, String truckSignifier, String agent) {
        double[] truck = getTruckByNumber(truckSignifier);
        String matchedRoute = null;
        int index = 0;

        // If not a driver initial, treat it as a route name directly
        if (matchedRoute == null && routes.containsKey(routeSignifier)) {
            matchedRoute = routeSignifier;
            index = wordToNumber(matchedRoute.replace("route","")) - 1;
        }

        String city = getCityFromCoordinates(truck[0], truck[1]);

        Rental truckLocation = new Rental(null, "'" + truckSignifier, null,
            null, null, null, null, false,
            "", city, "", 0,
            null, false, 0, null, null, truck[0],
            truck[1], "");

        routes.get(matchedRoute).add(0, truckLocation);
        truckAssignments.put(truckSignifier, routeSignifier);
        updateRoutePane(matchedRoute, truckLocation, "insertion-truck", 0, index, agent);
    }


    private void removeFromRoute(Rental rental, int closestMultiple, int routeIndex) {
        // System.out.println("**    removeFromRoute called with:" +
        //     "\n- closestMultiple: " + closestMultiple +
        //     "\n- routeIndex: " + routeIndex + " **");
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
        updateRoutePane(routeKey, rental, "deletion", closestMultiple, routeIndex, "user");
    }
    

    private void updateRoutePane(String routeName, Rental rental, String orientation,
                                int closestMultiple, int index, String agent) {

        /*
        System.out.println("Updating route pane with: " +
            "\n- routeName: " + routeName +
            "\n- rental: " + rental +
            "\n- orientation: " + orientation +
            "\n- closestMultiple: " + closestMultiple +
            "\n- index: " + index + 
            "-------------------------");
        */

        int tempIndex = index;
        if (orientation.equals("insertion") && agent.equals("user")) {
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
        int routeSize;
        if (agent.equals("program")) {
            routeSize = closestMultiple + 1;
        } else {
            routeSize = route.size();
        }
        String numeralRouteName = "route" + String.valueOf(routeIndex + 1);
        Circle pinpointerV = pinpointersV.get(numeralRouteName);
        Circle pinpointerH = pinpointersH.get(numeralRouteName);
        Circle innerPinpointerV = innerPinpointersV.get(numeralRouteName);
        Circle innerPinpointerH = innerPinpointersH.get(numeralRouteName);
        Label driverLabelV = driverLabelsV.get(numeralRouteName);
        Label driverLabelH = driverLabelsH.get(numeralRouteName);
        boolean hasTruckAssigned = truckAssignments.containsValue(routeName);

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
                
                    if (!routeVBoxPane.getChildren().contains(routeVBox)) {
                        routeVBoxPane.getChildren().add(routeVBox);
                    }
                    if (!routeHBoxPane.getChildren().contains(routeHBox)) {
                        routeHBoxPane.getChildren().add(routeHBox);
                    }
                    routeVBox.setPickOnBounds(false);

                    routeVBoxPane.setStyle("-fx-background-color: " + colors[0] + ";");
                    routeHBoxPane.setStyle("-fx-background-color: " + colors[0] + ";");

                    Rectangle rightArrowOutlineV = new Rectangle(12, 12);
                    rightArrowOutlineV.setFill(Color.TRANSPARENT); // No fill, just an outline
                    rightArrowOutlineV.setStroke(Color.TRANSPARENT); // Same color as the filled square
                    rightArrowOutlineV.setStrokeWidth(1); // Thickness of the outline
                    rightArrowOutlineV.setTranslateX(1);

                    Line rightArrowTopV = new Line(3.0,2.0, 10.0, 5.0);
                    Line rightArrowBottomV = new Line(10.0, 6.0, 3.0, 9.0);
                    rightArrowTopV.setStroke(Color.web(colors[1]));
                    rightArrowBottomV.setStroke(Color.web(colors[1]));
                    rightArrowTopV.setStrokeWidth(2);
                    rightArrowBottomV.setStrokeWidth(2);
                    rightArrowTopV.setTranslateX(2);
                    rightArrowBottomV.setTranslateX(2);
                    rightArrowTopV.setTranslateY(2);
                    rightArrowBottomV.setTranslateY(5);

                    Rectangle downArrowOutlineH = new Rectangle(12, 12);
                    downArrowOutlineH.setFill(Color.TRANSPARENT); // No fill, just an outline
                    downArrowOutlineH.setStroke(Color.TRANSPARENT); // Same color as the filled square
                    downArrowOutlineH.setStrokeWidth(1); // Thickness of the outline
                    downArrowOutlineH.setTranslateY(1);

                    Line downArrowLeftH = new Line(2.0,3.0, 5.0, 10.0);
                    Line downArrowRightH = new Line(6.0, 10.0, 9.0, 3.0);
                    downArrowLeftH.setStroke(Color.web(colors[1]));
                    downArrowRightH.setStroke(Color.web(colors[1]));
                    downArrowLeftH.setStrokeWidth(2);
                    downArrowRightH.setStrokeWidth(2);
                    downArrowLeftH.setTranslateY(2);
                    downArrowRightH.setTranslateY(2);
                    downArrowLeftH.setTranslateX(2);
                    downArrowRightH.setTranslateX(5);

                    // **Grouping both squares in a StackPane**
                    StackPane squareContainerV = new StackPane(rightArrowOutlineV, rightArrowTopV, rightArrowBottomV);
                    StackPane squareContainerH = new StackPane(downArrowLeftH, downArrowOutlineH, downArrowRightH);
                    StackPane.setAlignment(squareContainerV, Pos.TOP_LEFT); // Align to top-left
                    StackPane.setAlignment(squareContainerH, Pos.TOP_LEFT); // Align to top-left
                    StackPane.setAlignment(rightArrowOutlineV, Pos.TOP_LEFT);
                    StackPane.setAlignment(rightArrowTopV, Pos.TOP_LEFT);
                    StackPane.setAlignment(rightArrowBottomV, Pos.TOP_LEFT);
                    StackPane.setAlignment(downArrowOutlineH, Pos.TOP_LEFT);
                    StackPane.setAlignment(downArrowLeftH, Pos.TOP_LEFT);
                    StackPane.setAlignment(downArrowRightH, Pos.TOP_LEFT);

                    Color defaultColor = Color.web(colors[1]);
                    Color hoverColor = routeName == "route2" || routeName == "routeTwo" ? Color.web("#FFFFFF") : Color.web(colors[2]);

                    Shape[] rightGroupV = {rightArrowTopV, rightArrowBottomV};
                    Shape[] downGroupH = {downArrowLeftH, downArrowRightH};

                    addHoverEffectToRouteCard(rightArrowTopV, rightGroupV, defaultColor, hoverColor);
                    addHoverEffectToRouteCard(rightArrowBottomV, rightGroupV, defaultColor, hoverColor);
                    addHoverEffectToRouteCard(downArrowLeftH, downGroupH, defaultColor, hoverColor);
                    addHoverEffectToRouteCard(downArrowRightH, downGroupH, defaultColor, hoverColor);
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

                    // Load original truck image (black base)
                    Image originalFlatbedImage = new Image(
                        getClass().getClassLoader().getResourceAsStream("images/truck-face.png")
                    );

                    Color outlineColor = Color.BLACK;
                    Image normalImage = recolorImage(originalFlatbedImage, defaultColor, outlineColor);
                    Image hoverImage = recolorImage(originalFlatbedImage, hoverColor, outlineColor);

                    // === VERTICAL TRUCK ===
                    ImageView imageViewV = new ImageView(normalImage);
                    imageViewV.setFitWidth(15);
                    imageViewV.setFitHeight(14);
                    imageViewV.setTranslateX(-1);
                    imageViewV.setTranslateY(-1);
                    imageViewV.setPickOnBounds(true);

                    imageViewV.setOnMouseEntered(e -> {
                        imageViewV.setImage(hoverImage);
                    });
                    imageViewV.setOnMouseExited(e -> {
                        imageViewV.setImage(normalImage);
                    });
                    imageViewV.setOnMouseClicked(e -> handleTruckExpander(routeName, colors, "vertical"));

                    // === HORIZONTAL TRUCK ===
                    ImageView imageViewH = new ImageView(normalImage);
                    imageViewH.setFitWidth(14);
                    imageViewH.setFitHeight(13);
                    imageViewH.setTranslateX(-2);
                    imageViewH.setTranslateY(1);
                    imageViewH.setPickOnBounds(true);

                    imageViewH.setOnMouseEntered(e -> {
                        imageViewH.setImage(hoverImage);
                    });
                    imageViewH.setOnMouseExited(e -> {
                        imageViewH.setImage(normalImage);
                    });
                    imageViewH.setOnMouseClicked(e -> handleTruckExpander(routeName, colors, "horizontal"));

                    // Add to layout containers
                    squareContainerV.getChildren().add(imageViewV);
                    StackPane.setAlignment(imageViewV, Pos.BOTTOM_LEFT);
                    imageViewV.toFront();

                    squareContainerH.getChildren().add(imageViewH);
                    StackPane.setAlignment(imageViewH, Pos.TOP_RIGHT);
                    imageViewH.toFront();

                    routeVBoxPane.getChildren().addAll(squareContainerV);
                    routeHBoxPane.getChildren().addAll(squareContainerH);

                    anchorPane.getChildren().add(routeVBoxPane);


                }
            } else {
                Rental firstRental = route.get(routeSize - 2);
                Rental secondRental = route.get(routeSize - 1);
            
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

                int singleDigitSpacerV = isSingleDigitMinutes(googleResults[0]) ? 7 : 0;
                int singleDigitSpacerH = isSingleDigitMinutes(googleResults[0]) ? 5 : 0;

                // Insert intermediaryV before the first Circle in routeVBoxPane, or at the end
                ObservableList<Node> vboxChildren = routeVBoxPane.getChildren();
                int vInsertIndex = vboxChildren.size(); // Default to end
                for (int i = 0; i < vboxChildren.size(); i++) {
                    if (vboxChildren.get(i) instanceof Circle) {
                        vInsertIndex = i;
                        break;
                    }
                }
                vboxChildren.add(vInsertIndex, intermediaryV);

                // Insert intermediaryH before the first Circle in routeHBoxPane, or at the end
                ObservableList<Node> hboxChildren = routeHBoxPane.getChildren();
                int hInsertIndex = hboxChildren.size(); // Default to end
                for (int i = 0; i < hboxChildren.size(); i++) {
                    if (hboxChildren.get(i) instanceof Circle) {
                        hInsertIndex = i;
                        break;
                    }
                }
                hboxChildren.add(hInsertIndex, intermediaryH);

                intermediaryV.setTranslateY(((routeSize - 1) * cardHeightUnit) - 26 + singleDigitSpacerV);
                intermediaryH.setTranslateY(8);
                intermediaryV.setTranslateX(-1);
                intermediaryH.setTranslateX(((routeSize - 1) * cardWidthUnit) - 27 + singleDigitSpacerH);

                intermediaryV.setClip(new Rectangle(15, 45));
            //    intermediaryH.setClip(new Rectangle(45, 30));

                //drawRoutePolyline(googleResults[1], colors);
                encodedPolylines.get("route" + (index + 1)).add(googleResults[1]);
                //  storedEncodedPolylines.add(googleResults[1]);
                updateRoutePolylines();
                routeVBoxPane.toFront();
                routeVBox.toFront();
            }

            StackPane rentalChunkV = createRentalChunk(rental, colors, "vertical", routeName, closestMultiple);
            StackPane rentalChunkH = createRentalChunk(rental, colors, "horizontal", routeName, closestMultiple);
            routeVBox.getChildren().add(rentalChunkV);
            routeHBox.getChildren().add(rentalChunkH);
            routeVBoxPane.setPickOnBounds(false);
            routeHBoxPane.setPickOnBounds(false);
            routeVBox.setPickOnBounds(false);
            routeHBox.setPickOnBounds(false);
            rentalChunkV.setPickOnBounds(false);
            rentalChunkH.setTranslateY(14);
            rentalChunkH.setTranslateX(5);
            rentalChunkV.setTranslateY(3);
    
        } else if (orientation.equals("deletion")) {
            int accountForTruck = hasTruckAssigned ? -1 : 0;

            // System.out.println("***///  truckAssignments: " + truckAssignments + "///***"); 
            // System.out.println("📦 routeVBoxPane children BEFORE deletion:");
            // ObservableList<Node> children = routeVBoxPane.getChildren();
            
            // // Print each child
            // for (int i = 0; i < children.size(); i++) {
            //     System.out.println("    [" + i + "] " + children.get(i));
            // }
            
            // // Section break
            // System.out.println("\n──────────── Grouped by Type ────────────");
            
            // // Group by class simple name
            // Map<String, List<Node>> grouped = new TreeMap<>();
            // for (Node child : children) {
            //     String type = child.getClass().getSimpleName();
            //     grouped.computeIfAbsent(type, k -> new ArrayList<>()).add(child);
            // }
            
            // // Print each group
            // for (String type : grouped.keySet()) {
            //     System.out.println("• " + type + "s:");
            //     for (Node child : grouped.get(type)) {
            //         System.out.println("    - " + child);
            //     }
            // }
            
            // System.out.println("closestMultiple is: " + closestMultiple);
            // System.out.println("routeSize is:" + routeSize);
            // System.out.println("routeName is: " + routeName);
            // System.out.println("does truckAssignments contain routeName?: " + 
            //     truckAssignments.containsValue(routeName));

            if (routeSize == 0) {
                routeVBox.setVisible(false);
                routeHBox.setVisible(false);
                routeVBoxPane.getChildren().removeAll(routeVBoxPane.getChildren());
                routeHBoxPane.getChildren().removeAll(routeHBoxPane.getChildren());
                anchorPane.getChildren().remove(routeVBoxPane);
                anchorPane.getChildren().remove(routeHBoxPane);
            } else {
                if (routeSize == 1) {
                    routeVBoxPane.getChildren().remove(2);
                    routeHBoxPane.getChildren().remove(2);
                    encodedPolylines.get("route" + (index + 1)).remove(0);
                    // no longer need for any int's
                } else if (closestMultiple == 0) {
                    routeVBoxPane.getChildren().remove(2);
                    routeHBoxPane.getChildren().remove(2);
                    
                    for (int i = 2; i < routeVBoxPane.getChildren().size(); i++) {
                        Node node = routeVBoxPane.getChildren().get(i);
                        if (node instanceof VBox) {
                            VBox vbox = (VBox) node;
                            double currentY = vbox.getTranslateY();
                            vbox.setTranslateY(currentY - cardHeightUnit);
                        }
                    }
                    
                    for (int i = 2; i < routeHBoxPane.getChildren().size(); i++) {
                        Node node = routeHBoxPane.getChildren().get(i);
                        if (node instanceof HBox) {
                            HBox hbox = (HBox) node;
                            double currentX = hbox.getTranslateX();
                            hbox.setTranslateX(currentX - cardWidthUnit);
                        }
                    }
                    
                    encodedPolylines.get("route" + (index + 1)).remove(0);
                    
                    // shift up other int's
                } else if (closestMultiple == routeSize) {
                    //                ***  closestMultiple == routeSize
                    // Remove the last VBox from routeVBoxPane
                    for (int i = routeVBoxPane.getChildren().size() - 1; i >= 0; i--) {
                        Node node = routeVBoxPane.getChildren().get(i);
                        if (node instanceof VBox) {
                            routeVBoxPane.getChildren().remove(i);
                            break;
                        }
                    }

                    // Remove the last HBox from routeHBoxPane
                    for (int i = routeHBoxPane.getChildren().size() - 1; i >= 0; i--) {
                        Node node = routeHBoxPane.getChildren().get(i);
                        if (node instanceof HBox) {
                            routeHBoxPane.getChildren().remove(i);
                            break;
                        }
                    }

                    encodedPolylines.get("route" + (index + 1)).remove(closestMultiple - 1);
                    // no shifting int's
                } else {
                    routeVBoxPane.getChildren().remove(closestMultiple + 1);
                    routeHBoxPane.getChildren().remove(closestMultiple + 1);
                    // recalculating: 
                    //      - removing at index closestMultiple + 3 too
                    // shifting int's also
                    for (int i = closestMultiple + 1; i < routeVBoxPane.getChildren().size(); i++) {
                        Node node = routeVBoxPane.getChildren().get(i);
                        if (node instanceof VBox) {
                            VBox vbox = (VBox) node;
                            double currentY = vbox.getTranslateY();
                            vbox.setTranslateY(currentY - cardHeightUnit);
                        }
                    }
                    
                    for (int i = closestMultiple + 1; i < routeHBoxPane.getChildren().size(); i++) {
                        Node node = routeHBoxPane.getChildren().get(i);
                        if (node instanceof HBox) {
                            HBox hbox = (HBox) node;
                            double currentX = hbox.getTranslateX();
                            hbox.setTranslateX(currentX - cardWidthUnit);
                        }
                    }

                    Rental newLinkStart = route.get(closestMultiple - 1/* + accountForTruck*/);
                    Rental newLinkEnd = route.get(closestMultiple/* + accountForTruck*/);
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
                    int singleDigitSpacerV = isSingleDigitMinutes(newLink[0]) ? 7 : 0;
                    int singleDigitSpacerH = isSingleDigitMinutes(newLink[0]) ? 10 : 0;
                    newIntVBox.setTranslateY(((closestMultiple) * cardHeightUnit) - 26 + singleDigitSpacerV);
                    newIntHBox.setTranslateY(1);
                    newIntVBox.setTranslateX(-1);
                    newIntHBox.setTranslateX((closestMultiple * cardWidthUnit) - 23 + singleDigitSpacerH);
                    routeVBoxPane.getChildren().set(closestMultiple + 1, newIntVBox);
                    routeHBoxPane.getChildren().set(closestMultiple + 1, newIntHBox);

                    encodedPolylines.get("route" + (index + 1)).remove(closestMultiple - 1/* + accountForTruck*/);
                  
                    encodedPolylines.get("route" + (index + 1)).set(closestMultiple - 1/* + accountForTruck*/, newLink[1]);
                    //  VBox vbox = (VBox) routeVBoxPane.getChildren().get(closestMultiple + 1);

                    newIntVBox.setClip(new Rectangle(15, 45));
                }
                
            updateRoutePolylines();
            }
            routeVBox.getChildren().remove(closestMultiple);
            routeHBox.getChildren().remove(closestMultiple);
        

            // System.out.println("📦 routeVBoxPane children AFTER deletion:");
            // ObservableList<Node> children2 = routeVBoxPane.getChildren();

            // // Print each child
            // for (int i = 0; i < children2.size(); i++) {
            //     System.out.println("    [" + i + "] " + children2.get(i));
            // }

            // // Section break
            // System.out.println("\n──────────── Grouped by Type ────────────");

            // // Group by class simple name
            // Map<String, List<Node>> grouped2 = new TreeMap<>();
            // for (Node child : children2) {
            //     String type = child.getClass().getSimpleName();
            //     grouped2.computeIfAbsent(type, k -> new ArrayList<>()).add(child);
            // }

            // // Print each group
            // for (String type : grouped2.keySet()) {
            //     System.out.println("• " + type + "s:");
            //     for (Node child : grouped2.get(type)) {
            //         System.out.println("    - " + child);
            //     }
            // }

           // System.out.println("routeName: " + routeName);
            // delete truck assignment in memory and assignment ui elements
            // TODO: drop driver assignment as well
            // System.out.println("truckAssignments is: " + truckAssignments);
            if (closestMultiple == 0 && truckAssignments.containsValue(routeName)) {
                System.out.println("Going to try to wipe off pinpointers");
                for (Map.Entry<String, String> entry : truckAssignments.entrySet()) {
                    if (entry.getValue().equals(routeName)) {
                        System.out.println("found a match and it's routeName: " + routeName);
                        truckAssignments.remove(entry.getKey());
                        routeVBoxPane.getChildren().remove(pinpointersV.get(numeralRouteName));
                        routeHBoxPane.getChildren().remove(pinpointersH.get(numeralRouteName));
                        routeVBoxPane.getChildren().remove(innerPinpointersV.get(numeralRouteName));
                        routeHBoxPane.getChildren().remove(innerPinpointersH.get(numeralRouteName));
                        routeVBoxPane.getChildren().remove(driverLabelsV.get(numeralRouteName));
                        routeHBoxPane.getChildren().remove(driverLabelsH.get(numeralRouteName));
                        break;
                    }
                }
            }
        } else if (orientation.equals("insertion-truck")) {
            Rental firstRental = route.get(0);
            Rental secondRental = route.get(1);
        
            for (int i = 2; i < routeVBoxPane.getChildren().size(); i++) {
                Node node = routeVBoxPane.getChildren().get(i);
                if (node instanceof VBox) {
                    VBox vbox = (VBox) node;
                    double currentY = vbox.getTranslateY();
                    vbox.setTranslateY(currentY + cardHeightUnit);
                }
            }
            
            for (int i = 2; i < routeHBoxPane.getChildren().size(); i++) {
                Node node = routeHBoxPane.getChildren().get(i);
                if (node instanceof HBox) {
                    HBox hbox = (HBox) node;
                    double currentX = hbox.getTranslateX();
                    hbox.setTranslateX(currentX + cardWidthUnit);
                }
            }            

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

            int singleDigitSpacerV = isSingleDigitMinutes(googleResults[0]) ? 7 : 0;
            int singleDigitSpacerH = isSingleDigitMinutes(googleResults[0]) ? 10 : 0;

            routeVBoxPane.getChildren().add(2, intermediaryV);
            routeHBoxPane.getChildren().add(2, intermediaryH);

            // System.out.println("routeVBoxPane children:");
            // for (Node node : routeVBoxPane.getChildren()) {
            //     System.out.println(" - " + node);
            // }
            
            // System.out.println("routeHBoxPane children:");
            // for (Node node : routeHBoxPane.getChildren()) {
            //     System.out.println(" - " + node);
            // }       

            intermediaryV.setTranslateY((cardHeightUnit) - 26 + singleDigitSpacerV);
            intermediaryH.setTranslateY(8);
            intermediaryV.setTranslateX(-1);
            intermediaryH.setTranslateX((cardWidthUnit) - 27 + singleDigitSpacerH);

            intermediaryV.setClip(new Rectangle(15, 45));

            //drawRoutePolyline(googleResults[1], colors);
            encodedPolylines.get("route" + (index + 1)).add(0, googleResults[1]);
            //  storedEncodedPolylines.add(googleResults[1]);
            updateRoutePolylines();
        
            routeVBoxPane.toFront();
            routeVBox.toFront();
            intermediaryH.toFront();

            StackPane rentalChunkV = createRentalChunk(rental, colors, "vertical-truck", routeName, closestMultiple);
            StackPane rentalChunkH = createRentalChunk(rental, colors, "horizontal-truck", routeName, closestMultiple);
            routeVBox.getChildren().add(0, rentalChunkV);
            routeHBox.getChildren().add(0, rentalChunkH);
    
            rentalChunkH.setTranslateY(14);
            rentalChunkH.setTranslateX(5);
            rentalChunkV.setTranslateY(3);

            // V variant
            pinpointerV.setFill(Color.web(colors[0]));
            pinpointerV.setTranslateX(-20);
            pinpointerV.setOnMouseClicked(event -> {
                removeCardCovers();
                System.out.println("pinpointer registered a click");
                handleDriverExpander(routeName, numeralRouteName, colors, "vertical");
                event.consume();
            });

            innerPinpointerV.setFill(Color.web(colors[1]));
            innerPinpointerV.setTranslateX(-20);
            innerPinpointerV.setMouseTransparent(true);

            driverLabelV.setTextFill(Color.web(colors[2]));
            driverLabelV.setStyle("-fx-font-weight: bold");
            driverLabelV.setTranslateX(-20);
            driverLabelV.setMouseTransparent(true);

            routeVBoxPane.getChildren().addAll(pinpointerV, innerPinpointerV, driverLabelV);

            // H variant
            pinpointerH.setFill(Color.web(colors[0]));
            pinpointerH.setTranslateY(20);
            pinpointerH.setOnMouseClicked(event -> {
                removeCardCovers();
                System.out.println("pinpointer registered a click");
                handleDriverExpander(routeName, numeralRouteName, colors, "horizontal");
                event.consume();
            });

            innerPinpointerH.setFill(Color.web(colors[1]));
            innerPinpointerH.setTranslateY(20);
            innerPinpointerH.setMouseTransparent(true);

            driverLabelH.setTextFill(Color.web(colors[2]));
            driverLabelH.setStyle("-fx-font-weight: bold");
            driverLabelH.setTranslateY(20);
            driverLabelH.setMouseTransparent(true);

            routeHBoxPane.getChildren().addAll(pinpointerH, innerPinpointerH, driverLabelH);
        }
        
        routeVBox.toBack();
        routeHBox.toBack();

        routeSize = route.size();

        routeVBoxPane.setPrefWidth(112);
        routeVBoxPane.setPrefHeight((routeSize * cardHeightUnit) + 1);
        
        routeHBoxPane.setPrefHeight(69);
        routeHBoxPane.setPrefWidth((routeSize * cardWidthUnit) - 6);

        int timelineOffsetY = -((routeSize - 1) * (cardHeightUnit / 2)) + 11;
        pinpointerV.setTranslateY(timelineOffsetY);
        innerPinpointerV.setTranslateY(timelineOffsetY);
        driverLabelV.setTranslateY(timelineOffsetY);

        int timelineOffsetX = -((routeSize - 1) * (cardWidthUnit / 2)) - 27;
        pinpointerH.setTranslateX(timelineOffsetX);
        innerPinpointerH.setTranslateX(timelineOffsetX);
        driverLabelH.setTranslateX(timelineOffsetX);

        
        routeVBoxPane.setClip(new Rectangle(112, routeSize * cardHeightUnit + 28));
        routeHBoxPane.setClip(new Rectangle((routeSize * cardWidthUnit) + 20, 85));
    
        if (agent.equals("user")) {
            syncRoutingToDB();
        }
    }

    // Creates a visually distinct "chunk" for each Rental stop
    private StackPane createRentalChunk(Rental rental, String[] colors, String orientation, String routeName, int closestMultiple) {
        VBox labelBox = new VBox(0); // Vertical box with no spacing between elements
        StackPane truckPane = new StackPane();
        StackPane rentalChunk = orientation.equals("horizontal") || orientation.equals("vertical") ?
             new StackPane(labelBox) : new StackPane(truckPane);
        rentalChunk.setAlignment(Pos.TOP_RIGHT);
        for (Region node : Arrays.asList(labelBox, truckPane)) {
            node.setPadding(new Insets(0, 10, 0, 10));
            node.setMaxWidth(cardWidthUnit);
            node.setMinWidth(cardWidthUnit);
            node.setMinHeight(cardHeightUnit);
            node.setMaxHeight(cardHeightUnit);
        
            if (node instanceof VBox vbox) {
                vbox.setAlignment(Pos.CENTER);
            } else if (node instanceof StackPane stackPane) {
                stackPane.setAlignment(Pos.CENTER);
            }
        }

        double endX = (orientation.equals("vertical") || orientation.equals("vertical-truck")) ? 0 : 1;
        double endY = (orientation.equals("vertical") || orientation.equals("vertical-truck")) ? 1 : 0;
        
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

        if (orientation.equals("vertical") || orientation.equals("horizontal")) {
            // Set the background gradient
            labelBox.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));

            // Set a border
            labelBox.setStyle("-fx-padding: 5;");

            // Character limit for truncation (increased to fit the new width)
            int charLimit = 14; // Adjusted for 95px width

            // Create labels with truncation and set padding for left/right buffer
            Label nameLabel = new Label(truncateText(rental.getName(), charLimit));
 //           System.out.println("the truck rental getName was: " + rental.getName());
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
            if (orientation.equals("horizontal") || orientation.equals("horizontal-truck")) {
                liftType.setTranslateY(-17);
            }

            // Add labels to the VBox
          //  if (orientation.equals("horizontal-truck") || orientation.equals("vertical-truck")) {
            labelBox.getChildren().addAll(nameLabel, address2, address3);

        } else if (orientation.equals("horizontal-truck") || orientation.equals("vertical-truck")) {
            truckPane.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));

            Image originalFlatbedImage = new Image(getClass().getResourceAsStream("/images/flatbed.png"));

            int width = (int) originalFlatbedImage.getWidth();
            int height = (int) originalFlatbedImage.getHeight();
            
            PixelReader pixelReader = originalFlatbedImage.getPixelReader();
            WritableImage modifiedImage = new WritableImage(width, height);
            PixelWriter pixelWriter = modifiedImage.getPixelWriter();
            
            Color contentColor = Color.web(colors[2]); 
            
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
            
                    // Otherwise, keep the pixel as-is
                    else {
                        pixelWriter.setColor(x, y, pixelColor);
                    }
                }
            }
            
            ImageView imageView = new ImageView(modifiedImage);
            
            imageView.setFitWidth(82);
            imageView.setFitHeight(25);
            // imageView.setTranslateX(13);
            imageView.setTranslateY(8);
            truckPane.getChildren().add(imageView);
            //modifiedImage.setAlignment(Pos.CENTER);
            StackPane.setAlignment(imageView, Pos.TOP_CENTER);

            int timelineCapRange = cardWidthUnit / 2 - 10;
            Circle leftTimelineCap = new Circle(4, Color.web(colors[0]));
            Circle rightTimelineCap = new Circle(4, Color.web(colors[0]));
            leftTimelineCap.setTranslateX(-timelineCapRange);
            rightTimelineCap.setTranslateX(timelineCapRange);
            leftTimelineCap.setTranslateY(14);
            rightTimelineCap.setTranslateY(14);
            Line timeline = new Line(-timelineCapRange, 14, timelineCapRange, 14);
            timeline.setTranslateY(14);
            timeline.setStroke(Color.web(colors[0]));
            Label truckLabel = new Label("'##");
            truckLabel.setTranslateX(timelineCapRange - 9);
            truckLabel.setTranslateY(-12);
            truckLabel.setTextFill(contentColor);
            truckLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 9px");
            DropShadow glow = new DropShadow();
            glow.setColor(Color.web(colors[1]));
            glow.setRadius(8);
            glow.setSpread(0.9);
            glow.setOffsetX(0);
            glow.setOffsetY(0);
         //   truckLabel.setEffect(glow);
            rentalChunk.setPickOnBounds(false);
            truckPane.getChildren().addAll(leftTimelineCap, rightTimelineCap, timeline, truckLabel);
        }
        return rentalChunk;
    }

    private Region createStopIntermediary(String driveTimeStr, String[] colors, String orientation) {
        // Extract and convert drive time
        driveTimeStr = driveTimeStr.replace("s", "");
        int driveTimeInSeconds = Integer.parseInt(driveTimeStr);
        int driveTimeInMinutes = (int) Math.round(driveTimeInSeconds / 60.0);
    
        String timeNumber = String.valueOf(driveTimeInMinutes);
        String timeUnit = "m";
    
        StackPane container = new StackPane();
        container.setMinWidth(10);
        container.setAlignment(Pos.CENTER_LEFT);
    
        Label unitLabel = new Label(timeUnit);
        unitLabel.setTranslateY(1);
        unitLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + colors[1] + ";");
    
        if (orientation.equals("vertical")) {
            VBox intermediary = new VBox(-6) {
                @Override
                public boolean contains(double x, double y) {
                    return x >= 0 && x <= 15 && y >= 0 && y <= 45;
                }
            };
    
            VBox textBox = new VBox(-13);
            textBox.setAlignment(Pos.CENTER);
            textBox.setMinWidth(12);
            textBox.setMaxWidth(12);
            textBox.setTranslateX(1);
    
            for (char digit : timeNumber.toCharArray()) {
                Label digitLabel = new Label(String.valueOf(digit));
                digitLabel.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: " + colors[1] + ";");
                textBox.getChildren().add(digitLabel);
            }
    
            textBox.getChildren().add(unitLabel);
            container.getChildren().add(textBox);
            intermediary.getChildren().add(container);
    
          //  intermediary.setClip(new Rectangle(15, 45));
            intermediary.setPickOnBounds(false);
            return intermediary;
    
        } else if (orientation.equals("horizontal")) {
            HBox intermediary = new HBox(-6) {
                @Override
                public boolean contains(double x, double y) {
                    return x >= 0 && x <= 45 && y >= 0 && y <= 30;
                }
            };
    
            HBox textBox = new HBox(-1);
            textBox.setAlignment(Pos.CENTER);
            textBox.setMinHeight(17);
            textBox.setMaxHeight(17);
            textBox.setTranslateY(-37);
            textBox.setTranslateX(21);
    
            for (char digit : timeNumber.toCharArray()) {
                Label digitLabel = new Label(String.valueOf(digit));
                digitLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + colors[1] + ";");
                textBox.getChildren().add(digitLabel);
            }
    
            textBox.getChildren().add(unitLabel);
            container.getChildren().add(textBox);
            intermediary.getChildren().add(container);
    
            //intermediary.setClip(new Rectangle(45, 30));
            intermediary.setPickOnBounds(false);
            return intermediary;
        }
    
        return null;
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
            removeFromRoute(rental, closestMultiple, routeIndex);
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
            
            // System.out.println("Removed lastCardCover from:");
            // for (Node child : parentPane.getChildren()) {
            //     System.out.println(" - " + child);
            // }
        }
        lastCardCover = null;
        lastCoveredPane = null;
    
        if (lastSideBarCover != null && lastSideBarCover.getParent() != null) {
            Pane parentPane = (Pane) lastSideBarCover.getParent();
            parentPane.getChildren().remove(lastSideBarCover);
            
            // Remove the last VBox if present
            for (int i = parentPane.getChildren().size() - 1; i >= 0; i--) {
                Node child = parentPane.getChildren().get(i);
                if (child instanceof VBox || child instanceof HBox) {
                    parentPane.getChildren().remove(i);
                    break;
                }
            }
    
            // System.out.println("Removed lastSideBarCover and last VBox from:");
            // for (Node child : parentPane.getChildren()) {
            //     System.out.println(" - " + child);
            // }
        }
        lastSideBarCover = null;
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
        }
        removeCardCovers();
    }
    
    
    private void handleTruckExpander(String routeName, String[] colors, String orientation) {
        removeCardCovers();
        String hoverColor = routeName.equals("route2") ? "#FFFFFF" : colors[2];
        // Count only VBox children
        StackPane vboxPane = routeVBoxPanes.get(getRouteIndex(routeName));
        StackPane hboxPane = routeHBoxPanes.get(getRouteIndex(routeName));
        long routeSize = calculateRouteSize(vboxPane);
        int height = (int) (routeSize * cardHeightUnit) + 1;
        int width = (int) (routeSize * cardWidthUnit) - 1;
        System.out.println("variable width in handleTruckExpander is: " + width);
        if (orientation.equals("vertical")) {
            lastSideBarCover = makeSideBarCover(11, height, colors);
            vboxPane.getChildren().add(lastSideBarCover);
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
                label.setOnMouseEntered(e -> {
                    label.setStyle("-fx-font-size: 10px; -fx-text-fill: " + hoverColor + "; -fx-font-weight: bold");
                });
                label.setOnMouseExited(e -> label.setStyle("-fx-font-size: 10px; -fx-text-fill: " + colors[1] + "; -fx-font-weight: bold"));
        
                // Click action
                label.setOnMouseClicked(e -> handleAssignTruck(vboxPane, text, routeName));
        
                labelColumn.getChildren().add(label);
            }
        
            vboxPane.getChildren().add(labelColumn);
            StackPane.setAlignment(labelColumn, Pos.CENTER_LEFT);
        } else if (orientation.equals("horizontal")) {
            lastSideBarCover = makeSideBarCover(width, 14, colors);
            hboxPane.getChildren().add(lastSideBarCover);
            StackPane.setAlignment(lastSideBarCover, Pos.TOP_CENTER);
        
            HBox labelRow = new HBox();
            labelRow.setPrefWidth(width);
            labelRow.setPrefHeight(14);
            labelRow.setTranslateX(1 + (cardWidthUnit / 2));
            labelRow.setTranslateY(-3);
            labelRow.setAlignment(Pos.TOP_CENTER);
        
            // Dynamic vertical spacing
            labelRow.setSpacing((2 + (routeSize - 1) * 15));
        
            String[] labelTexts = {"06", "08", "16", "20", "25"};
            for (String text : labelTexts) {
                Label label = new Label(text);
                label.setStyle("-fx-font-size: 14px; -fx-text-fill: " + colors[1] + "; -fx-font-weight: bold");
                label.setTranslateX(-cardWidthUnit / 2 - 1);
        
                // Hover effect
                label.setOnMouseEntered(e -> {
                    label.setStyle("-fx-font-size: 14px; -fx-text-fill: " + hoverColor + "; -fx-font-weight: bold");
                });
                label.setOnMouseExited(e -> label.setStyle("-fx-font-size: 14px; -fx-text-fill: " + colors[1] + "; -fx-font-weight: bold"));
        
                // Click action
                label.setOnMouseClicked(e -> handleAssignTruck(hboxPane, text, routeName));
        
                labelRow.getChildren().add(label);
            }
        
            hboxPane.getChildren().add(labelRow);
            StackPane.setAlignment(labelRow, Pos.TOP_CENTER);
        }
        
    }
    
    
    private void handleAssignTruck(StackPane routePane, String labelString, String routeName) {
        if (!routePane.getChildren().isEmpty()) {
            Node firstChild = routePane.getChildren().get(0);
            if (firstChild instanceof Parent parent) {
                for (Node node : parent.getChildrenUnmodifiable()) {
                    if (node instanceof Label label) {
                        label.setText(labelString);
                        label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
                        label.setScaleX(0.75);
                        label.setTranslateX(-1);
                        break;
                    }
                }
            }
        }


        for (Map.Entry<String, String> entry : truckAssignments.entrySet()) {

        }

        removeCardCovers();
        if (truckAssignments.containsValue(routeName)) {    
            removeFromRoute(routes.get(routeName).get(0), 0, getRouteIndex(routeName));
        } 

        addTruckToRoute(routeName, labelString, "user");

        Node cardsNode = routePane.getChildren().get(0);
        if (cardsNode instanceof VBox || cardsNode instanceof HBox) {
            Pane cardStack = (Pane) cardsNode;
            System.out.println("Found Pane (VBox/HBox) as cardsNode");
        
            if (!cardStack.getChildren().isEmpty() && cardStack.getChildren().get(0) instanceof StackPane) {
                StackPane truckStack = (StackPane) cardStack.getChildren().get(0);
                System.out.println("Found StackPane as first child of cardStack");
        
                for (Node child : truckStack.getChildren()) {
                    if (child instanceof StackPane) {
                        StackPane innerStack = (StackPane) child;
                        System.out.println("Found inner StackPane");
        
                        for (Node innerChild : innerStack.getChildren()) {
                            if (innerChild instanceof Label) {
                                System.out.println("Found Label. Setting text to: " + labelString);
                                ((Label) innerChild).setText(labelString);
                                return; // Exit once we've updated one label
                            }
                        }
                    }
                }
            } else {
                System.out.println("First child of cardStack is not a StackPane or is empty");
            }
        } else {
            System.out.println("cardsNode is not a VBox or HBox");
        }
        
        syncRoutingToDB();
    }

    private void handleDriverExpander(String routeName, String numeralRouteName, String[] colors, String orientation) {
        removeCardCovers();
        String hoverColor = routeName.equals("route2") ? "#FFFFFF" : colors[2];

        StackPane vboxPane = routeVBoxPanes.get(getRouteIndex(routeName));
        StackPane hboxPane = routeHBoxPanes.get(getRouteIndex(routeName));
        long routeSize = calculateRouteSize(vboxPane);
        int height = (int) (routeSize * cardHeightUnit) + 1;
        int width = (int) (routeSize * cardWidthUnit) - 1;
        if (orientation.equals("vertical")) {
            lastSideBarCover = makeSideBarCover(11, height, colors);
            vboxPane.getChildren().add(lastSideBarCover);
            StackPane.setAlignment(lastSideBarCover, Pos.CENTER_LEFT);
        
        
            // Sidebar VBox with labels
            VBox labelColumn = new VBox();
            labelColumn.setPrefWidth(11);
            labelColumn.setPrefHeight(height);
            labelColumn.setTranslateX(1);
            labelColumn.setAlignment(Pos.TOP_CENTER);
        
            // Dynamic vertical spacing
            labelColumn.setSpacing(-9 + ((routeSize - 1) * 9));
        
            for (String[] driver : Config.DRIVERS) {
                String initials = driver[2]; // Use the 3rd value
            
                Label label = new Label(initials);
                label.setStyle("-fx-font-size: 10px; -fx-text-fill: " + colors[1] + "; -fx-font-weight: bold");
                label.setTranslateX(-cardWidthUnit / 2 - 1);
            
                // Hover effect
                label.setOnMouseEntered(e -> label.setStyle("-fx-font-size: 10px; -fx-text-fill: " + hoverColor + "; -fx-font-weight: bold"));
                label.setOnMouseExited(e -> label.setStyle("-fx-font-size: 10px; -fx-text-fill: " + colors[1] + "; -fx-font-weight: bold"));
            
                // Click action
                label.setOnMouseClicked(e -> handleAssignDriver(vboxPane, initials, numeralRouteName));
            
                labelColumn.getChildren().add(label);
            }
    
            vboxPane.getChildren().add(labelColumn);
            StackPane.setAlignment(labelColumn, Pos.CENTER_LEFT);
        
            System.out.println("finished the vertical part of handledriverexpander");
        
        } else if (orientation.equals("horizontal")) {
            lastSideBarCover = makeSideBarCover(width, 14, colors);
            hboxPane.getChildren().add(lastSideBarCover);
            StackPane.setAlignment(lastSideBarCover, Pos.TOP_CENTER);
       
         
            HBox labelRow = new HBox();
            labelRow.setPrefWidth(width);
            labelRow.setPrefHeight(14);
            labelRow.setTranslateX(1 + (cardWidthUnit / 2));
            labelRow.setTranslateY(-3);
            labelRow.setAlignment(Pos.TOP_CENTER);
        
            // Dynamic vertical spacing
            labelRow.setSpacing(-4 + ((routeSize - 1) * 15));
        
            for (String[] driver : Config.DRIVERS) {
                String initials = driver[2]; // Use the 3rd value
            
                Label label = new Label(initials);
                label.setStyle("-fx-font-size: 14px; -fx-text-fill: " + colors[1] + "; -fx-font-weight: bold");
                label.setTranslateX(-cardWidthUnit / 2 - 1);
            
                // Hover effect
                label.setOnMouseEntered(e -> label.setStyle("-fx-font-size: 14px; -fx-text-fill: " + hoverColor + "; -fx-font-weight: bold"));
                label.setOnMouseExited(e -> label.setStyle("-fx-font-size: 14px; -fx-text-fill: " + colors[1] + "; -fx-font-weight: bold"));
            
                // Click action
                label.setOnMouseClicked(e -> handleAssignDriver(vboxPane, initials, numeralRouteName));
            
                labelRow.getChildren().add(label);
            }
    
            hboxPane.getChildren().add(labelRow);
            StackPane.setAlignment(labelRow, Pos.TOP_CENTER);
        
       
        }
        

        
    }

    /*                                    /*
     *              PROGRESS               *
    /*                                     */

    private int DURATION_MINUTES = 1;
    private int PROGRESS_RANGE_PIXELS = 50;

    private void startAllTruckProgress() {
        for (Map.Entry<String, String> entry : truckAssignments.entrySet()) {
            startTruckProgress(entry.getValue());
        }
    }
    
    private void startTruckProgress(String routeKey) {
        int routeIndex = getRouteIndex(routeKey);
        String numeralRouteName = "route" + (routeIndex + 1);

        int steps = DURATION_MINUTES * 60; // 1 step per second
        double stepPixels = PROGRESS_RANGE_PIXELS / (double) steps;

        Circle pinV = pinpointersV.get(numeralRouteName);
        Circle innerPinV = innerPinpointersV.get(numeralRouteName);
        Label labelV = driverLabelsV.get(numeralRouteName);

        Circle pinH = pinpointersH.get(numeralRouteName);
        Circle innerPinH = innerPinpointersH.get(numeralRouteName);
        Label labelH = driverLabelsH.get(numeralRouteName);

        // ----------------------------
        // Animate route card progress
        // ----------------------------
        final double[] progress = {0};

        Timeline cardTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (progress[0] >= PROGRESS_RANGE_PIXELS) return;

            pinV.setTranslateX(pinV.getTranslateX() + stepPixels);
            innerPinV.setTranslateX(innerPinV.getTranslateX() + stepPixels);
            labelV.setTranslateX(labelV.getTranslateX() + stepPixels);

            pinH.setTranslateX(pinH.getTranslateX() + stepPixels);
            innerPinH.setTranslateX(innerPinH.getTranslateX() + stepPixels);
            labelH.setTranslateX(labelH.getTranslateX() + stepPixels);

            progress[0] += stepPixels;
        }));
        cardTimeline.setCycleCount(steps);
        cardTimeline.play();

        // ----------------------------
        // Animate map truck movement
        // ----------------------------

        Optional<String> assignedTruck = truckAssignments.entrySet().stream()
            .filter(entry -> entry.getValue().equals(routeKey))
            .map(Map.Entry::getKey)
            .findFirst();

        if (assignedTruck.isEmpty()) return;

        String truckName = assignedTruck.get();
        Circle truckCircle = trucks.get(truckName);
        if (truckCircle == null) return;

        List<String> routeOfPolylines = encodedPolylines.get(numeralRouteName);
        if (routeOfPolylines == null || routeOfPolylines.isEmpty()) return;

        String encoded = routeOfPolylines.get(0);
        List<double[]> decodedPoints = decodePolyline(encoded);

        List<Point2D> screenPoints = decodedPoints.stream()
            .map(latlon -> new Point2D(
                mapLongitudeToX(latlon[1]),
                mapLatitudeToY(latlon[0])
            ))
            .collect(Collectors.toList());

        if (screenPoints.size() < 2) return;

        final int totalFrames = DURATION_MINUTES * 60;
        final int totalSegments = screenPoints.size() - 1;
        final double framesPerSegment = (double) totalFrames / totalSegments;
        final double[] currentFrame = {0};

        // Optionally: reset truck to starting point
        Point2D first = screenPoints.get(0);
        truckCircle.setCenterX(first.getX());
        truckCircle.setCenterY(first.getY());

        Timeline mapTimeline = new Timeline(new KeyFrame(Duration.seconds(1.0 / 60), e -> {
            int segmentIndex = (int)(currentFrame[0] / framesPerSegment);
            if (segmentIndex >= totalSegments) return;

            Point2D start = screenPoints.get(segmentIndex);
            Point2D end = screenPoints.get(segmentIndex + 1);
            double segmentProgress = (currentFrame[0] % framesPerSegment) / framesPerSegment;

            double interpX = start.getX() + (end.getX() - start.getX()) * segmentProgress;
            double interpY = start.getY() + (end.getY() - start.getY()) * segmentProgress;

            truckCircle.setCenterX(interpX);
            truckCircle.setCenterY(interpY);

            currentFrame[0]++;
        }));

        mapTimeline.setCycleCount(totalFrames);
        mapTimeline.play();
    }


    

    /*                                    /*
     *              UTILITIES              *
    /*                                     */


    private void handleAssignDriver(StackPane routePane, String labelString, String routeName) {
        driverLabelsV.forEach((key, label) -> {
            if (!key.equals(routeName) && label.getText().equals(labelString)) {
                label.setText("");
            }
        });
        driverLabelsH.forEach((key, label) -> {
            if (!key.equals(routeName) && label.getText().equals(labelString)) {
                label.setText("");
            }
        });
        driverLabelsV.get(routeName).setText(labelString);
        driverLabelsH.get(routeName).setText(labelString);
        routeAssignments.entrySet().removeIf(entry -> entry.getValue().equals(labelString));
        routeAssignments.put(routeName, labelString);
        removeCardCovers();
        syncRoutingToDB();
    }
    

    private Rectangle makeSideBarCover(int width, int height, String[] colors){
        Rectangle rect = new Rectangle(width, height);
        rect.setFill(Color.web(colors[0]));
        rect.setTranslateX(1);

        return rect;
    }

    private long calculateRouteSize(Pane routePane) {
        return routePane.getChildren().stream()
            .filter(node -> node instanceof VBox)
            .count();
    }
    
    public List<Rental> getRouteStops(String routeName) {
        return routes.getOrDefault(routeName, null);
    }
    
    
    // Helper method to convert word numbers to integers
    private int wordToNumber(String word) {
        Map<String, Integer> wordMap = Map.of(
            "one", 1, "two", 2, "three", 3, "four", 4, "five", 5,
            "six", 6, "seven", 7, "eight", 8, "nine", 9, "ten", 10
        );
    
        // First try to parse it as an integer
        try {
            return Integer.parseInt(word);
        } catch (NumberFormatException e) {
            // Not a numeric string, fall back to word map
        }
    
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
            case 0 -> "route1";
            case 1 -> "route2";
            case 2 -> "route3";
            case 3 -> "route4";
            case 4 -> "route5";
            default -> null;
        };
        return routeKey;
    }

    private double[] getTruckByNumber(String number) {
        try {
            Field field = getClass().getDeclaredField("truck" + number);
            field.setAccessible(true);
            return (double[]) field.get(this);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
        return minutes < 11;
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

    private Rental createNewRentalFromId(int rentalId){
        return new Rental(
            null, "'" + rentalId, null, null, null, null, null, false,
            "", "", "", 0,
            null, false, 0, null, null,
            0.0, 0.0, ""
        );
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

    private Image recolorImage(Image source, Color contentColor, Color outlineColor) {
        int width = (int) source.getWidth();
        int height = (int) source.getHeight();
    
        WritableImage newImage = new WritableImage(width, height);
        PixelReader reader = source.getPixelReader();
        PixelWriter writer = newImage.getPixelWriter();
    
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixelColor = reader.getColor(x, y);
    
                if (pixelColor.getOpacity() == 0.0) {
                    writer.setColor(x, y, pixelColor);
                } else if (isNearBlack(pixelColor)) {
                    writer.setColor(x, y, contentColor);
                } else if (isNearGray(pixelColor)) {
                    writer.setColor(x, y, new Color(
                        outlineColor.getRed(),
                        outlineColor.getGreen(),
                        outlineColor.getBlue(),
                        pixelColor.getOpacity()
                    ));
                } else {
                    writer.setColor(x, y, pixelColor);
                }
            }
        }
    
        return newImage;
    }

    // Utility method to truncate text
    private String truncateText(String text, int limit) {
        if (text.length() > limit) {
            return text.substring(0, limit) + "..."; // Append "..." if exceeding limit
        }
        return text;
    }
    
    @FXML
    private void resetStage() {
        MaxReachPro.getInstance().collapseStage();
    }
} 
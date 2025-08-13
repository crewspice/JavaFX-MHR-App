package com.MaxHighReach;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;
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
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.effect.*;
import javafx.util.Duration;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;
//import com.google.maps.routing.v2.*;

public class MapController {

    @FXML
    public AnchorPane anchorPane;

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
    private double lastDragX = -1;
    private double lastDragY = -1;
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
    private PopupDisc lastPopup = null;

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
    private Map<String, List<List<double[]>>> decodedPolylines = new HashMap<>();
    private Map<String, Circle> pinpointersV = new HashMap<>();
    private Map<String, Circle> innerPinpointersV = new HashMap<>();
    private Map<String, Label> driverLabelsV = new HashMap<>();
    private Map<String, Circle> pinpointersH = new HashMap<>();
    private Map<String, Circle> innerPinpointersH = new HashMap<>();
    private Map<String, Label> driverLabelsH = new HashMap<>();
    public Map<String, String> routeAssignments = new HashMap<>();
    public Map<String, String> truckAssignments = new HashMap<>();
    private Map<String, double[]> truckCoords = new HashMap<>();
    private Map<String, DoubleProperty> truckProgress = new HashMap<>();
    private Set<StackPane> activeAnimations = new HashSet<>();
    private Map<String, List<Integer>> intervals = new HashMap<>();
    public List<Rental> latestRouteEdited = null;
    private StackPane lastCardCover = null;
    private Rectangle lastSideBarCover = null;
    private Region lastCoveredPane = null;
    private Circle lastPinpointer = null;
    private Label lastDriverLabel = null;
    private Map<String, Circle> trucks = new HashMap<>();
    private Map<String, Double> truckTranslateX = new HashMap<>();
    private Map<String, Double> truckTranslateY = new HashMap<>();
    private Map<String, Timeline> truckTimelines = new HashMap<>();
    private Map<String, List<double[]>> truckPolylineSteps = new HashMap<>();
    private Map<String, List<Double>> truckSegmentLengths = new HashMap<>();
    private Map<String, StackPane> truckPanes = new HashMap<>();
    private Map<String, List<String>> inventories = new HashMap<>();
    private Map<String, StackPane> pictoralTrucksV = new HashMap<>();
    private Map<String, StackPane> pictoralTrucksH = new HashMap<>();

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
    private String[] truckNames = {"25", "06", "08", "16", "20"};
    private Timeline progressTicker;
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();


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
            List<Integer> intervalList = new ArrayList<>();
            Circle pinpointerV = new Circle(8);
            Circle pinpointerH = new Circle(8);
            Circle innerPinpointerV = new Circle(6);
            Circle innerPinpointerH = new Circle(6);
            Label driverLabelV = new Label("?");
            Label driverLabelH = new Label("?");
            StackPane pictoralTruckV = new StackPane();
            StackPane pictoralTruckH = new StackPane();
    
            // Add the route components to their respective lists
            routeVBoxPanes.add(routeVBoxPane);
            routeHBoxPanes.add(routeHBoxPane);
            routeVBoxes.add(routeVBox);
            routeHBoxes.add(routeHBox);
            routeRegions.add(routeVBox);
            routeStops.add(routeRentalStops);
    
            routes.put("route" + (i + 1), routeRentalStops);
            intervals.put("route" + (i + 1), intervalList);
            routeBoxes.put("route" + (i + 1), routeVBox);
            decodedPolylines.put("route" + (i + 1), new ArrayList<>());
            pinpointersV.put("route" + (i + 1), pinpointerV);
            pinpointersH.put("route" + (i + 1), pinpointerH);
            innerPinpointersV.put("route" + (i + 1), innerPinpointerV);
            innerPinpointersH.put("route" + (i + 1), innerPinpointerH);
            driverLabelsV.put("route" + (i + 1), driverLabelV);
            driverLabelsH.put("route" + (i + 1), driverLabelH);
            inventories.put("route" + (i + 1), new ArrayList<>());
            pictoralTrucksV.put("route" + (i + 1), pictoralTruckV);
            pictoralTrucksH.put("route" + (i + 1), pictoralTruckH);


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
                            lastCardCover = createCardOptionsCover(routes.get(routeKey).get(multiple), multiple, x, y, index, "vertical");
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


                if (event.getClickCount() == 2) { // Check for double click
                    double x = Math.floor(event.getX() * 10) / 10.0; // X relative to routeBox
                    double y = Math.floor(event.getY() * 10) / 10.0; // Y relative to routeBox
    
                    removeCardCovers();
                    
                    if (x > 15 && y > 14) {
                        int multiple = closestMultiple(x - 14, cardWidthUnit);
                        int routeSize = routes.get(routeKey).size();
                        if (multiple + 1 <= routeSize) {
                            int xOffset = multiple * (cardWidthUnit - 2) - ((routeSize) * 20) - ((routeSize - 1) * 22) + 10 + ((3 - routeSize) * 5);
                            lastCardCover = createCardOptionsCover(routes.get(routeKey).get(multiple), multiple, x, y, index, "horizontal");
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
            

            // Create the truck circle
            Circle truckDot = new Circle(8); // radius 8
            truckDot.setTranslateX(-5);
            truckDot.setTranslateY(-5);
            truckDot.setFill(Color.TRANSPARENT);

            // Create a tight, non-obstructive StackPane
            StackPane truckPane = new StackPane(truckDot);
            truckPane.setPickOnBounds(false); // only respond to mouse events on visible content
            truckPane.setMouseTransparent(true); // let mouse events pass through to map
            truckPane.setMinSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);
            truckPane.setPrefSize(10, 10); // match circle diameter
            truckPane.setMaxSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);

            // Track
            trucks.put(truckNames[i], truckDot);
            truckPanes.put(truckNames[i], truckPane);
            truckTranslateX.put(truckNames[i], 0.0);
            truckTranslateY.put(truckNames[i], 0.0);
            truckProgress.put(truckNames[i], new SimpleDoubleProperty(0.0));

            pictoralTruckV.setMaxHeight(25);
            pictoralTruckV.setMaxWidth(82);
            pictoralTruckH.setMaxHeight(25);
            pictoralTruckH.setMaxWidth(82);
        }
    
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
                updateTrucks(true);
                reflectRoutingData();
                updateRoutePolylines();
                setupLiveDriveProgressions();
            });
        });
    } 




    /*                                    /*
     *               incoming              *
     *                  &                  *
     *               outgoing              *
     *                 DATA                *
    /*                                     */




/*
    private void loadTruckFromDirectCall() {
        try {
            truck25 = FleetAPIClient.getTruckCoordsByName("2025");
            truck16 = randomizeCoords(new double[]{Config.SHOP_LAT, Config.SHOP_LON});
            truck08 = randomizeCoords(truck25);
            truck06 = randomizeCoords(truck25);
            truck20 = randomizeCoords(truck25);
            System.out.println("truckCoords is: " + truckCoords);




            updateTrucks();
        } catch (IOException e) {
            e.printStackTrace(); // or log the error / show a message to the user
        }
    }
*/



    public void updateTruckCoordinates() {
        try {
            URL url = new URL("http://5.78.73.173:8080/routes/fleet");
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
    
                // Normalize keys to match truckCoords' keys
                switch (name) {
                    case "2025":
                        truckCoords.put("25", new double[]{lat, lng});
                        break;
                    case "2006":
                        truckCoords.put("06", new double[]{lat, lng});
                        break;
                    case "2008":
                        truckCoords.put("08", new double[]{lat, lng});
                        break;
                    case "2016":
                        truckCoords.put("16", new double[]{lat, lng});
                        break;
                    case "2020":
                        truckCoords.put("20", new double[]{lat, lng});
                        break;
                }
            }
       
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    private void loadRentalDataFromAPI() {
        String apiUrl = "http://5.78.73.173:8080/routes/stops";

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

            // OBFUSCATE_OFF
            String query = """
                SELECT ro.customer_id, c.customer_name, ri.item_delivery_date, ri.item_call_off_date, ro.po_number,
                    ordered_contacts.first_name AS ordered_contact_name, ordered_contacts.phone_number AS ordered_contact_phone,
                    ri.auto_term, ro.site_name, ro.street_address, ro.city, ri.rental_item_id, ri.item_status, l.lift_type,
                    l.serial_number, ro.single_item_order, ri.rental_order_id, ro.longitude, ro.latitude,
                    site_contacts.first_name AS site_contact_name, site_contacts.phone_number AS site_contact_phone,
                    ri.driver, ri.driver_number, ri.driver_initial, ri.delivery_truck, ri.pick_up_truck, ri.delivery_time, 
                    ri.invoice_composed
                FROM customers c
                JOIN rental_orders ro ON c.customer_id = ro.customer_id
                JOIN rental_items ri ON ro.rental_order_id = ri.rental_order_id
                JOIN lifts l ON ri.lift_id = l.lift_id
                LEFT JOIN contacts AS ordered_contacts ON ri.ordered_contact_id = ordered_contacts.contact_id
                LEFT JOIN contacts AS site_contacts ON ri.site_contact_id = site_contacts.contact_id
                WHERE ri.rental_item_id IN (%s)
            """.formatted(placeholders);
            // OBFUSCATE_ON


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
                            rs.getString("lift_type"),
                            rs.getString("item_status")
                        );
                        rental.setDriver(rs.getString("driver"));
                        rental.setDriverInitial(rs.getString("driver_initial"));
                        rental.setDriverNumber(rs.getInt("driver_number"));
                        rental.setDeliveryTruck(rs.getString("delivery_truck"));
                        rental.setPickUpTruck(rs.getString("pick_up_truck"));
                        rental.setDeliveryTime(rs.getString("delivery_time"));
                        rental.setInvoiceComposed(rs.getBoolean("invoice_composed"));
                        rental.decapitalizeLiftType();
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
        // OBFUSCATE_OFF
        String query = """
            SELECT ro.customer_id, c.customer_name, ri.item_delivery_date, ri.item_call_off_date, ro.po_number,
                ordered_contacts.first_name AS ordered_contact_name, ordered_contacts.phone_number AS ordered_contact_phone,
                ri.auto_term, ro.site_name, ro.street_address, ro.city, ri.rental_item_id, l.lift_type,
                l.serial_number, ro.single_item_order, ri.rental_order_id, ro.longitude, ro.latitude, ri.item_status,
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
        // OBFUSCATE_ON
    
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
                String status = rs.getString("item_status");
    
                // Create Rental objects for each row and add them to the list
                rentalsForCharting.add(new Rental(customerId, name, deliveryDate, callOffDate, poNumber,
                        orderedByName, orderedByPhone, autoTerm, addressBlockOne, addressBlockTwo,
                        addressBlockThree, rentalItemId, serialNumber, singleItemOrder, rentalOrderId,
                        siteContactName, siteContactPhone, latitude, longitude, liftType, status));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading rental data", e);
        }
    }
    
    private void syncRoutingToDB() {
        // OBFUSCATE_OFF
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
        // OBFUSCATE_ON
    
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
        sendRoutingToRedisAPI();
    }
    
    private void sendRoutingToRedisAPI() {   
        try {
            List<RoutingRental> routingItems = new ArrayList<>();
            for (Map.Entry<String, List<Rental>> entry : routes.entrySet()) {
                String routeKey = entry.getKey();
                List<Rental> rentals = entry.getValue();
                String driverInitial = routeAssignments.get(routeKey);   
                String assignedTruck = truckAssignments.entrySet().stream()
                        .filter(e -> e.getValue().equals(routeKey))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null); 
                int offset = (assignedTruck != null) ? 0 : 1;
                for (int i = 0; i < rentals.size(); i++) {
                    Rental rental = rentals.get(i);
                    if (rental.getRentalItemId() == 0) {
                        continue;
                    }
                    int driverNumber = i + offset;
                    RoutingRental dto = new RoutingRental(
                            rental.getRentalItemId(),
                            "unknown",
                            "unknwon",
                            0.0,
                            0.0,
                            driverInitial,
                            driverNumber,
                            assignedTruck
                    );
                    routingItems.add(dto);
                }
            }   
            ObjectMapper mapper = new ObjectMapper();
            String requestBody = mapper.writeValueAsString(routingItems);
            //System.out.println("Serialized JSON payload: " + requestBody);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://5.78.73.173:8080/routes/update"))  // Fixed URI
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
   
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            System.err.println("Exception occurred while sending routing data:");
            e.printStackTrace();
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
            String[] route = getARouteNoPreference();
            String routeKey = route[0]; // Get route key (customizable)
            int routeIndex = Integer.valueOf(route[1]);
            String[] colors = getRouteColors(routeKey);
            routes.put(routeKey, entry.getValue());
            routeAssignments.put(routeKey, entry.getKey());
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

                configureTruckDot(entry.getKey(), truckSignifier, colors);

            }
            driverLabelsV.get(routeKey).setText(entry.getKey());
            driverLabelsH.get(routeKey).setText(entry.getKey());
        }

    }


    private List<Rental> findCompletedStops() {
        List<Rental> completedStops = new ArrayList<>();
    
        // Collect all rental_item_ids from routes
        List<Integer> allItemIds = routes.values().stream()
            .flatMap(List::stream)
            .map(Rental::getRentalItemId)
            .distinct()
            .toList();
    
        if (allItemIds.isEmpty()) return completedStops;
    
        // Create a comma-separated list of placeholders (e.g., ?, ?, ?, ...)
        String placeholders = allItemIds.stream().map(id -> "?").collect(Collectors.joining(", "));
    
        String query = "SELECT rental_item_id, item_status FROM rental_items WHERE rental_item_id IN (" + placeholders + ")";
    
        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             PreparedStatement ps = connection.prepareStatement(query)) {
    
            // Fill placeholders
            for (int i = 0; i < allItemIds.size(); i++) {
                ps.setInt(i + 1, allItemIds.get(i));
            }
    
            // Execute query and build status map
            ResultSet rs = ps.executeQuery();
            Map<Integer, String> statusMap = new HashMap<>();
            while (rs.next()) {
                int id = rs.getInt("rental_item_id");
                String status = rs.getString("item_status");
                statusMap.put(id, status);
            }
    
            // Find rentals with invalid statuses
            for (List<Rental> rentalList : routes.values()) {
                for (Rental rental : rentalList) {
                    int id = rental.getRentalItemId();
                    String status = statusMap.get(id);
                    if (status != null && !(status.equals("Upcoming") || status.equals("Called Off"))) {
                        completedStops.add(rental);
                    }
                }
            }
    
        } catch (SQLException e) {
            throw new RuntimeException("Error checking completed rentals", e);
        }
    
        return completedStops;
    }
    


    /*                                    /*
     *               MAPPING               *
    /*                                     */
    
    // Returns a Shape for a rental dot, agnostic of position/event handling
    private Shape createRentalDot(Rental rental, String status) {
        Shape dotShape;

        if ("Called Off".equals(status)) {
            // Create an octagon (stop-sign style)
            double radius = 7;
            Polygon octagon = new Polygon();
            for (int i = 0; i < 8; i++) {
                double angle = Math.toRadians(45 * i + 22.5); // flat top offset
                double px = radius * Math.cos(angle);
                double py = radius * Math.sin(angle);
                octagon.getPoints().addAll(px, py);
            }
            dotShape = octagon;
            dotShape.setFill(Color.RED);
        } else {
            Circle circle = new Circle(0, 0, 7); // center at 0,0, position later
            circle.setFill(Color.web(Config.getPrimaryColor()));
            dotShape = circle;
        }

        // Stroke logic
        String strokeUnderneath = Config.COLOR_TEXT_MAP.get(Config.getPrimaryColor()) == 1
            ? Config.getTertiaryColor()
            : "WHITE";
        dotShape.setStroke(Color.web(strokeUnderneath));
        dotShape.setStrokeWidth(2);

        return dotShape;
    }

    // Handles coordinate placement, adding to mapContainer, and event logic
    private void createMapDot(double x, double y, Rental rental, String status) {
        Shape dotShape = createRentalDot(rental, status);

        // Apply position
        dotShape.setTranslateX(x);
        dotShape.setTranslateY(y);

        mapContainer.getChildren().add(dotShape);

        // Adjusted popup positioning
        // final double finalX = x > 150 ? x - 205 : x;
        // final double finalY = y > 410 ? y - 150 : y;

        // Mouse click behavior
        dotShape.setOnMouseClicked(event -> {
            anchorPane.getChildren().removeIf(node -> node instanceof PopupDisc);
            if (lastPopup != null && anchorPane.getChildren().contains(lastPopup)) {
                anchorPane.getChildren().remove(lastPopup);
            }

            lastClickedRental = rental;
            PopupDisc disc = new PopupDisc(this, rental, x, y);
            anchorPane.getChildren().add(disc);
            lastPopup = disc;

            anchorPane.setOnMouseClicked(e -> {
                if (!disc.getBoundsInParent().contains(e.getX(), e.getY())) {
                    anchorPane.getChildren().remove(disc);
                    lastClickedRental = null;
                    lastPopup = null;
                }
            });

            event.consume();
        });
    }


    private void plotRentalLocations() {
        for (Rental rental : rentalsForCharting) {
            double x = mapLongitudeToX(rental.getLongitude());
            double y = mapLatitudeToY(rental.getLatitude());

            if (rental.getLongitude() < 0 && rental.getLatitude() > 0) {
                createMapDot(x, y, rental, rental.getStatus());
            }
        }
    }




    // This method is used to update the position of all dots (rental locations)
    private void updateRentalLocations() {
        mapContainer.getChildren().removeIf(node -> node instanceof Circle || node instanceof Polygon);
                
        for (Rental rental : rentalsForCharting) {
            double x = mapLongitudeToX(rental.getLongitude());
            double y = mapLatitudeToY(rental.getLatitude());


            if (rental.getLongitude() < 0 && rental.getLatitude() > 0) {
                createMapDot(x, y, rental, rental.getStatus());
            }
        }
    }


    private Polyline[] drawRoutePolyline(List<double[]> polylinePoints, String[] colors) {
        // 1️⃣ First glow (larger gray glow)
        Polyline glowPolyline1 = new Polyline();
        glowPolyline1.setStroke(Color.web(Config.getTertiaryColor(), .3));
        glowPolyline1.setStrokeWidth(11);
        glowPolyline1.setOpacity(0.5);
        glowPolyline1.setEffect(new GaussianBlur(15));

        // 2️⃣ Second glow (shorter gradient glow)
        Polyline glowPolyline2 = new Polyline();
        glowPolyline2.setStroke(Color.web(colors[0]));
        glowPolyline2.setStrokeWidth(9);
        glowPolyline2.setOpacity(1);
        glowPolyline2.setEffect(new GaussianBlur(8));

        // 3️⃣ Main polyline with gradient
        Polyline mainPolyline = new Polyline();
        mainPolyline.setStrokeWidth(4);
        mainPolyline.setStroke(Color.web(colors[1]));

        // 4️⃣ Add points to all polylines
        for (double[] point : polylinePoints) {
            double x = mapLongitudeToX(point[1]);
            double y = mapLatitudeToY(point[0]);

            glowPolyline1.getPoints().addAll(x, y);
            glowPolyline2.getPoints().addAll(x, y);
            mainPolyline.getPoints().addAll(x, y);
        }

        // 5️⃣ Add to map
        mapContainer.getChildren().addAll(glowPolyline1, glowPolyline2, mainPolyline);

        // Layering
        mainPolyline.toBack();
        glowPolyline1.toBack();
        glowPolyline2.toBack();
        metroMapView.toBack();

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
    
        for (Map.Entry<String, List<List<double[]>>> entry : decodedPolylines.entrySet()) {
            List<List<double[]>> routeSegments = entry.getValue();
            String[] colors = getRouteColors(entry.getKey());

            for (List<double[]> segmentPoints : routeSegments) {
                drawRoutePolyline(segmentPoints, colors);
            }
        }

    
    }

    public void updateTrucks(boolean absoluteMode) {
        for (Map.Entry<String, StackPane> entry : truckPanes.entrySet()) {
            String truckId = entry.getKey();
            StackPane truckPane = entry.getValue();
            if (absoluteMode) {
                double[] coords = truckCoords.get(truckId);
                if (coords == null) continue;
        
                double targetX = mapLongitudeToTranslateX(coords[1]);
                double targetY = mapLatitudeToTranslateY(coords[0]);
                truckTranslateX.put(truckId, targetX);
                truckTranslateY.put(truckId, targetY);
                truckPane.setTranslateX(targetX);
                truckPane.setTranslateY(targetY);
            } else {
                double[] logicalVector = {truckTranslateX.get(truckId), truckTranslateY.get(truckId)};
                if (logicalVector == null) logicalVector = new double[]{0, 0};
    
                truckPane.setTranslateX(logicalVector[0]);
                truckPane.setTranslateY(logicalVector[1]);
            }
        }
    }
    
    
    private void setupMetroMap() {
        try {
            // Load the original metro map image
            Image metroImage = new Image(getClass().getResourceAsStream("/images/stadia_map_z8_size400_800_vertical_tweaks.png"));
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
    
            // MOUSE PRESSED: Initialize starting positions and reset last drag
            mapContainer.setOnMousePressed(event -> {
                mapContainer.setUserData(new double[]{
                    event.getSceneX(),
                    event.getSceneY(),
                    metroMapView.getTranslateX(),
                    metroMapView.getTranslateY()
                });
                lastDragX = event.getSceneX();
                lastDragY = event.getSceneY();
                removeCardCovers();
                for (Timeline timeline : truckTimelines.values()) {
                    timeline.pause();  // Or .stop() if you want to hard stop
                }
                for (Map.Entry<String, StackPane> entry : truckPanes.entrySet()) {
                    String id = entry.getKey();
                    StackPane truckPane = entry.getValue();
                    truckTranslateX.put(id, truckPane.getTranslateX());
                    truckTranslateY.put(id, truckPane.getTranslateY());
                }
            });

            // MOUSE DRAGGED: Calculate deltas based on last drag, not initial press
            mapContainer.setOnMouseDragged(event -> {
                double[] data = (double[]) mapContainer.getUserData();
                if (data != null) {
                    double startXPos = data[0];
                    double startYPos = data[1];
                    double startImgX = data[2];
                    double startImgY = data[3];

                    // System.out.println("Truck Translation States:");
                    // for (String truckId : trucks.keySet()) {
                    //     double tx = truckTranslateX.getOrDefault(truckId, 0.0);
                    //     double ty = truckTranslateY.getOrDefault(truckId, 0.0);
                    //     System.out.printf("Truck ID: %s — translateX: %.2f, translateY: %.2f%n", truckId, tx, ty);
                    // }
                    // System.out.println(); // Line break

                    // Compute delta first
                    double deltaX = event.getSceneX() - lastDragX;
                    double deltaY = event.getSceneY() - lastDragY;
                    
                    // Enhanced log output including last drag point and deltas
                    // System.out.printf(
                    //     "DRAG START DATA — startXPos: %.2f, startYPos: %.2f, startImgX: %.2f, startImgY: %.2f%n" +
                    //     "                  lastDragX: %.2f, lastDragY: %.2f, deltaX: %.2f, deltaY: %.2f%n" +
                    //     "                  eventX: %.2f, eventY: %.2f%n%n",
                    //     startXPos, startYPos, startImgX, startImgY,
                    //     lastDragX, lastDragY, deltaX, deltaY,
                    //     event.getSceneX(), event.getSceneY()
                    // );                    

                    // Update last drag point
                    lastDragX = event.getSceneX();
                    lastDragY = event.getSceneY();

                    double currentX = metroMapView.getTranslateX();
                    double currentY = metroMapView.getTranslateY();
                    double newX = currentX + deltaX;
                    double newY = currentY + deltaY;

                    double minX = anchorPane.getWidth() - metroImage.getWidth();
                    double maxX = 0;
                    double minY = anchorPane.getHeight() - metroImage.getHeight();
                    double maxY = 0;
                    // System.out.println("minX: " + minX + " & minY: " + minY);

                    // Apply bounds
                    metroMapView.setTranslateX(Math.max(minX, Math.min(maxX, newX)));
                    metroMapView.setTranslateY(Math.max(minY, Math.min(maxY, newY)));

                    // Update logical translation for all trucks
                    for (String truckId : trucks.keySet()) {

            

                        boolean allowX =
                        ((startXPos - lastDragX) > startImgX || deltaX < 0) &&
                        ((lastDragX - startXPos + startImgX) > minX || deltaX > 0);
                        
                        if (allowX) {
                            double oldTranslateX = truckTranslateX.getOrDefault(truckId, 0.0);
                            truckTranslateX.put(truckId, oldTranslateX + deltaX);
                        }
                        
                        boolean allowY =
                            ((startYPos - lastDragY) > startImgY || deltaY < 0) &&
                            ((lastDragY - startYPos + startImgY) > minY || deltaY > 0);
                        
                        if (allowY) {
                            double oldTranslateY = truckTranslateY.getOrDefault(truckId, 0.0);
                            truckTranslateY.put(truckId, oldTranslateY + deltaY);
                        }
                    


                    }

                    updateVisibleMapBounds(metroMapView);
                    updateRentalLocations();
                    updateRoutePolylines();
                    updateTrucks(false); // relative mode
                }
            });

            mapContainer.setOnMouseReleased(event -> {
                for (Map.Entry<String, Timeline> entry : truckTimelines.entrySet()) {
                    String truckId = entry.getKey();
                    Timeline timeline = entry.getValue();
                    StackPane truckPane = truckPanes.get(truckId);
                    List<double[]> steps = truckPolylineSteps.get(truckId);
            
                    if (truckPane == null || steps == null || steps.isEmpty()) continue;
            
                    // Find step with minimum distance from truck's current position
                    double truckX = truckPane.getTranslateX();
                    double truckY = truckPane.getTranslateY();
            
                    int closestIndex = 0;
                    double minDistance = Double.MAX_VALUE;
                    for (int i = 0; i < steps.size(); i++) {
                        double[] latLon = steps.get(i);
                        double x = mapLongitudeToTranslateX(latLon[1]);
                        double y = mapLatitudeToTranslateY(latLon[0]);
                        double distance = Math.hypot(truckX - x, truckY - y);
                        if (distance < minDistance) {
                            minDistance = distance;
                            closestIndex = i;
                        }
                    }
            
                    // Rebuild timeline from the closest index
                    Timeline resumedTimeline = new Timeline();
                    for (int i = closestIndex; i < steps.size(); i++) {
                        double[] latLon = steps.get(i);
                        KeyFrame frame = new KeyFrame(
                            Duration.seconds((i - closestIndex) * 1),
                            new KeyValue(truckPane.translateXProperty(), mapLongitudeToTranslateX(latLon[1])),
                            new KeyValue(truckPane.translateYProperty(), mapLatitudeToTranslateY(latLon[0]))
                        );
                        resumedTimeline.getKeyFrames().add(frame);
                    }
            
                    resumedTimeline.setOnFinished(e2 -> {
                        activeAnimations.remove(truckPane);
                        truckTranslateX.put(truckPane.getId(), truckPane.getTranslateX());
                        truckTranslateY.put(truckPane.getId(), truckPane.getTranslateY());
                    });
            
                    // Replace old timeline and play
                    truckTimelines.put(truckId, resumedTimeline);
                    activeAnimations.add(truckPane);
                    resumedTimeline.play();
                }
            });
              
            
            
            updateVisibleMapBounds(metroMapView);

    
            // Add map to UI
            anchorPane.getChildren().add(mapContainer);
            mapArea.toFront();
            updateTrucks(true);

            
            for (StackPane truckPane : truckPanes.values()) {
                mapContainer.getChildren().add(truckPane);
            }
    
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

    private void configureTruckDot(String driverInitial, String truckId, String[] colors) {
        System.out.printf("▶ Configuring truck dot: driverInitial=%s, truckId=%s%n", driverInitial, truckId);

        StackPane truckPane = truckPanes.get(truckId);
        if (truckPane == null) {
            System.out.println("❌ No StackPane found for truckId=" + truckId);
            return;
        }
        System.out.println("✔ Found truckPane: " + truckPane);

        if (truckPane.getChildren().isEmpty() || !(truckPane.getChildren().get(0) instanceof Circle)) {
            System.out.println("❌ First child is missing or not a Circle.");
            return;
        }

        Circle truck = (Circle) truckPane.getChildren().get(0);
        truck.setFill(Color.web(colors[0]));
        System.out.println("✔ Outer truck circle color set to " + colors[0]);

        Circle innerTruck = new Circle(6);
        innerTruck.setFill(Color.web(colors[1]));
        innerTruck.setTranslateX(-5);
        innerTruck.setTranslateY(-5);
        truckPane.getChildren().add(innerTruck);
        System.out.println("✔ Inner truck circle added with color " + colors[1]);

        Label driverLabel = new Label(driverInitial);
        driverLabel.setTextFill(Color.web(colors[2]));
        driverLabel.setTranslateX(-5);
        driverLabel.setTranslateY(-5);
        driverLabel.setStyle("-fx-font-weight: bold");
        driverLabel.setMinWidth(Label.USE_PREF_SIZE);
        driverLabel.setPrefWidth(Label.USE_COMPUTED_SIZE);
        driverLabel.setMaxWidth(Double.MAX_VALUE);
        driverLabel.setWrapText(false);
        driverLabel.setEllipsisString(null);
        truckPane.getChildren().add(driverLabel);
        System.out.println("✔ Driver label added with initial '" + driverInitial + "' and color " + colors[2]);

        System.out.println("✅ Finished configuring truck dot for truckId=" + truckId);
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








































    /*                                    /*
     *             ROUTE CARDS             *
     *            ROUTE EDITING            *
    /*                                     */




        
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
    
        if (!rental.isSingleItemOrder()) {
            int rentalOrderIdToMatch = rental.getRentalOrderId();
            for (Rental r : rentalsForCharting) {
                if (r.getRentalOrderId() == rentalOrderIdToMatch) {
                    if (r != rental) {
                        routes.get(matchedRoute).add(r);
                        updateRoutePane(matchedRoute, r, "insertion", 99, index, "user");
                    }
                }
            }   
        }
    
    }








    private void addTruckToRoute(String routeSignifier, String truckSignifier, String agent) {
        double[] truck = truckCoords.get(truckSignifier);
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
            truck[1], "", "");


        routes.get(matchedRoute).add(0, truckLocation);
        truckAssignments.put(truckSignifier, routeSignifier);
        
        updateRoutePane(matchedRoute, truckLocation, "insertion-truck", 0, index, agent);
        
    }




    private void removeFromRoute(Rental rental, int closestMultiple, int routeIndex) {
        System.out.println("**    removeFromRoute called with:" +
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
        
        StackPane routeVBoxPane = routeVBoxPanes.get(routeIndex);

        updateRoutePane(routeKey, rental, "deletion", closestMultiple, routeIndex, "user");
    }
    

    private void editRouteInventory(Rental rental, int index, int routeKey) {
        printRouteVBoxPaneSummary();
        StackPane vboxPane = routeVBoxPanes.get(routeKey);
        StackPane hboxPane = routeHBoxPanes.get(routeKey);
    
        if (vboxPane == null) {
            System.out.println("vboxPane is null for routeKey: " + routeKey);
            return;
        }
    
        if (hboxPane == null) {
            System.out.println("hboxPane is null for routeKey: " + routeKey);
            return;
        }
    
    
        if (!vboxPane.getChildren().isEmpty()) {
            Node firstChild = vboxPane.getChildren().get(0);
    
            if (firstChild instanceof Parent parent) {
    
                int routeIndex = 1;
                for (Node node : parent.getChildrenUnmodifiable()) {
                    System.out.println(index++ + ". " + describeNode(node));
                }
    
                Map<String, List<Node>> grouped = new LinkedHashMap<>();
                for (Node node : parent.getChildrenUnmodifiable()) {
                    String key = node.getClass().getSimpleName();
                    grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(node);
                }
    
                for (Map.Entry<String, List<Node>> entry : grouped.entrySet()) {
                    System.out.println("\n" + entry.getKey() + "s:");
                    for (Node node : entry.getValue()) {
                        System.out.println(" - " + describeNode(node));
                    }
                }
            } else {
            }
        } else {
        }
    }
    

    private String describeNode(Node node) {
        String id = node.getId() != null ? "id='" + node.getId() + "'" : "no id";
        String type = node.getClass().getSimpleName();
        String text = "";

        // Try to extract meaningful text content
        if (node instanceof Labeled labeled) {
            text = ", text='" + labeled.getText() + "'";
        } else if (node instanceof TextInputControl input) {
            text = ", text='" + input.getText() + "'";
        }

        return type + " (" + id + text + ")";
    }


    private void printRouteVBoxPaneSummary() {
        System.out.println("\n--- routeVBoxPanes summary ---");
        for (int i = 0; i < routeVBoxPanes.size(); i++) {
            StackPane pane = routeVBoxPanes.get(i);
            System.out.println("Index " + i + ": StackPane with " + pane.getChildren().size() + " children");
    
            for (int j = 0; j < pane.getChildren().size(); j++) {
                Node child = pane.getChildren().get(j);
                System.out.println("  " + (j + 1) + ". " + describeNode(child));
            }
        }
        System.out.println("--- End summary ---\n");
    }
        
    


    private void updateRoutePane(String routeName, Rental rental, String orientation,
                                int closestMultiple, int index, String agent) {

        StackPane routeHBoxPane2 = routeHBoxPanes.get(index);

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
        List<Integer> intervalList = intervals.get(routeName);
        List<List<double[]>> polylineList = decodedPolylines.get(routeName);
        StackPane pictoralTruckV = pictoralTrucksV.get(numeralRouteName);
        StackPane pictoralTruckH = pictoralTrucksH.get(numeralRouteName);
        
/*
        System.out.println("Updating route pane with: " +
            "\n- routeName: " + routeName +
            "\n- rental: " + rental +
            "\n- orientation: " + orientation +
            "\n- closestMultiple: " + closestMultiple +
            "\n- index: " + index + 
            "\n- routeSize " + routeSize +
            "-------------------------");

        // Print all child nodes, preserving order
        System.out.println("Children (pre-update) of routeHBoxPane (index " + index + "):");
        for (Node child : routeHBoxPane2.getChildren()) {
            System.out.println(" - " + child);
        }  */

        List<Node> vInts = routeVBoxPane.getChildren().stream()
            .filter(node -> node.getClass().getName().contains("MapController$"))
            .collect(Collectors.toList());

        Map<Integer, Integer> paneIndexToVIntIndex = new LinkedHashMap<>();

      //  System.out.println("vInts contents (pre-update):");
        for (int vIntIdx = 0; vIntIdx < vInts.size(); vIntIdx++) {
            Node n = vInts.get(vIntIdx);
            
            // Find the index of this node in the full children list
            int paneIndex = routeVBoxPane.getChildren().indexOf(n);
            
            // Map full-list index → vInts index
            paneIndexToVIntIndex.put(paneIndex, vIntIdx);

            // Collect label text
            // StringBuilder labelText = new StringBuilder();
            // collectLabelText(n, labelText);
            
            // System.out.println(" - " + n + "   text: " + labelText + "   paneIndex=" + paneIndex + ", vIntIndex=" + vIntIdx);
        }

        List<Node> hInts = routeHBoxPane.getChildren().stream()
            .filter(node -> node.getClass().getName().contains("MapController$"))
            .collect(Collectors.toList());

        Map<Integer, Integer> paneIndexToHIntIndex = new LinkedHashMap<>();

  //  System.out.println("hInts contents (pre-update):");
    for (int hIntIdx = 0; hIntIdx < hInts.size(); hIntIdx++) {
        Node n = hInts.get(hIntIdx);
        int paneIndex = routeHBoxPane.getChildren().indexOf(n);
        paneIndexToHIntIndex.put(paneIndex, hIntIdx);
/*
        // Collect label text
        StringBuilder labelText = new StringBuilder();
        collectLabelText(n, labelText);

        // Get translateX
        double tx = n.getTranslateX();

        // Try to get alignment if parent is a StackPane
        Pos alignment = null;
        if (n.getParent() instanceof StackPane) {
            alignment = StackPane.getAlignment(n);
        }

        System.out.println(" - " + n
            + "   text: " + labelText
            + "   translateX: " + tx);   */
    }


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
                    Color hoverColor;
                    if (routeName.equals("route2")) {
                        hoverColor = Color.web("#FFFFFF");
                    } else {
                        hoverColor = Color.web(colors[2]);
                    }


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
            
                RouteInfo googleRoute = new RouteInfo(0, 0, null);
                try {
                    googleRoute = getGoogleRoute(firstRental.getLatitude(), firstRental.getLongitude(),
                                        secondRental.getLatitude(), secondRental.getLongitude());
                } catch (Exception e) {
                }

                int driveTimeInMinutes = (int) Math.round(googleRoute.getDurationSeconds() / 60.0);
                
                intervalList.add(driveTimeInMinutes);


                // Check if the intermediary VBox is created correctly
                Region intermediaryRegionV = createStopIntermediary(driveTimeInMinutes, colors, "vertical");
                Region intermediaryRegionH = createStopIntermediary(driveTimeInMinutes, colors, "horizontal");
                VBox intermediaryV = (VBox) intermediaryRegionV;
                HBox intermediaryH = (HBox) intermediaryRegionH;

                int singleDigitSpacerV = driveTimeInMinutes < 11 ? 7 : 0;
                int singleDigitSpacerH = driveTimeInMinutes < 11 ? 5 : 0;

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
                StackPane.setAlignment(intermediaryH, Pos.TOP_LEFT);

                intermediaryV.setTranslateY(((routeSize - 1) * cardHeightUnit) - 26 + singleDigitSpacerV);
                intermediaryH.setTranslateY(9);
                intermediaryV.setTranslateX(-1);
                intermediaryH.setTranslateX(((routeSize - 1) * cardWidthUnit) - 29 + singleDigitSpacerH);

                intermediaryV.setClip(new Rectangle(15, 45));
            //    intermediaryH.setClip(new Rectangle(45, 30));
                //drawRoutePolyline(googleResults[1], colors);
                polylineList.add(googleRoute.getPolylinePoints());
                //  storedEncodedPolylines.add(googleResults[1]);
                updateRoutePolylines();
                routeVBoxPane.toFront();
                routeVBox.toFront();

            }




            System.out.println("[DEBUG] Creating vertical chunk for: " + rental);
            StackPane rentalChunkV = createRentalChunk(rental, colors, "vertical", routeName, closestMultiple);
            System.out.println("[DEBUG] Created vertical chunk: " + rentalChunkV);
            StackPane rentalChunkH = createRentalChunk(rental, colors, "horizontal", routeName, closestMultiple);

            System.out.println("[DEBUG] routeVBox children before add: " + routeVBox.getChildren().size());
            routeVBox.getChildren().add(rentalChunkV);
            System.out.println("[DEBUG] routeVBox children after add: " + routeVBox.getChildren().size());
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

            ////////////// Print System for child/parent shifting ////////////////
     //      System.out.println("***///  truckAssignments: " + truckAssignments + "///***"); 
     /*       System.out.println("📦 routeVBoxPane children BEFORE deletion:");
            ObservableList<Node> children = routeVBoxPane.getChildren();
            // Print each child
            for (int i = 0; i < children.size(); i++) {
                System.out.println("    [" + i + "] " + children.get(i));
            }
            // Section break
            System.out.println("\n──────────── Grouped by Type ────────────");
            // Group by class simple name
            Map<String, List<Node>> grouped = new TreeMap<>();
            for (Node child : children) {
                String type = child.getClass().getSimpleName();
                grouped.computeIfAbsent(type, k -> new ArrayList<>()).add(child);
            }
            // Print each group
            for (String type : grouped.keySet()) {
                System.out.println("• " + type + "s:");
                for (Node child : grouped.get(type)) {
                    System.out.println("    - " + child);
                }
            }
            System.out.println("closestMultiple is: " + closestMultiple);
            System.out.println("routeSize is:" + routeSize);
            System.out.println("routeName is: " + routeName);
            System.out.println("does truckAssignments contain routeName?: " + 
                truckAssignments.containsValue(routeName));  */
            /////////////////////////////////////////////////////////////




   
            if (routeSize == 0) {
                routeVBox.setVisible(false);
                routeHBox.setVisible(false);
                routeVBoxPane.getChildren().removeAll(routeVBoxPane.getChildren());
                routeHBoxPane.getChildren().removeAll(routeHBoxPane.getChildren());
                anchorPane.getChildren().remove(routeVBoxPane);
                anchorPane.getChildren().remove(routeHBoxPane);
            } else {
                if (routeSize == 1) {
                    routeVBoxPane.getChildren().remove(vInts.get(0));
                    routeHBoxPane.getChildren().remove(hInts.get(0));
                    polylineList.remove(0);
                    intervalList.remove(0);
                    // no longer need for any int's
                } else if (closestMultiple == 0) {
                    routeVBoxPane.getChildren().remove(vInts.get(0));
                    routeHBoxPane.getChildren().remove(hInts.get(0));
                    intervalList.remove(0);
                    
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
                    
                    polylineList.remove(0);
                    
                    // shift up other int's
                } else if (closestMultiple == routeSize) {
                    //                ***  closestMultiple == routeSize
                    // Remove the last VBox from routeVBoxPane
                    if (!vInts.isEmpty()) {
                        Node lastVBoxNode = vInts.get(vInts.size() - 1);
                        routeVBoxPane.getChildren().remove(lastVBoxNode);
                    }


                    if (!hInts.isEmpty()) {
                        Node lastHBoxNode = hInts.get(hInts.size() - 1);
                        routeHBoxPane.getChildren().remove(lastHBoxNode);
                    }


                    polylineList.remove(closestMultiple - 1);
                    intervalList.remove(closestMultiple - 1);
                    
                    // no shifting int's
                } else {
                    int targetVIntIndex1 = closestMultiple - 1;
                    int targetVIntIndex2 = closestMultiple; // the next one

                    int targetVFullIndex1 = -1;
                    int targetVFullIndex2 = -1;

                    for (Map.Entry<Integer, Integer> entry : paneIndexToVIntIndex.entrySet()) {
                        int fullIndex = entry.getKey();
                        int vIntIndex = entry.getValue();

                        if (vIntIndex == targetVIntIndex1) {
                            targetVFullIndex1 = fullIndex;
                        }
                        if (vIntIndex == targetVIntIndex2) {
                            targetVFullIndex2 = fullIndex;
                        }
                    }

                    if (targetVFullIndex1 == -1 || targetVFullIndex2 == -1) {
                        System.out.println("No matching full index found for one or both vInt indices.");
                    } else {

                        // Debug: Show the whole paneIndexToVIntIndex map
                    //    System.out.println("paneIndexToVIntIndex contents:");
                        // paneIndexToVIntIndex.forEach((fullIndex, vIntIndex) ->
                        //     System.out.println("  fullIndex=" + fullIndex + ", vIntIndex=" + vIntIndex)
                        // );

                        int startVIntShiftIndex = closestMultiple;

                    //    System.out.println("\nFiltering for vIntIndex >= " + startVIntShiftIndex + "...");

                        // Collect full indices of all vInts from startVIntShiftIndex to end
                        List<Integer> fullVIndicesToShift = paneIndexToVIntIndex.entrySet().stream()
                            .filter(e -> e.getValue() >= startVIntShiftIndex)
                      //      .peek(e -> System.out.println("  Keeping entry: fullIndex=" + e.getKey() + ", vIntIndex=" + e.getValue()))
                            .map(Map.Entry::getKey)
                            .sorted()
                            .collect(Collectors.toList());

                    //    System.out.println("\nFull indices to shift: " + fullIndicesToShift);

                        
                        for (int fullIndex : fullVIndicesToShift) {
                            Node nodeV = routeVBoxPane.getChildren().get(fullIndex);
                            if (nodeV instanceof VBox) {
                      //          System.out.println("Made it to a shift for fullIndex: " + fullIndex); 
                                VBox vbox = (VBox) nodeV;
                                double currentY = vbox.getTranslateY();
                                vbox.setTranslateY(currentY - cardHeightUnit);
                            }
                            
                        }

                        // Remove higher index first to avoid shifting problems
                        if (targetVFullIndex1 > targetVFullIndex2) {
                            routeVBoxPane.getChildren().remove(targetVFullIndex1);
                            routeVBoxPane.getChildren().remove(targetVFullIndex2);
                        } else {
                            routeVBoxPane.getChildren().remove(targetVFullIndex2);
                            routeVBoxPane.getChildren().remove(targetVFullIndex1);
                        }

                    }

                    int targetHIntIndex1 = closestMultiple - 1;
                    int targetHIntIndex2 = closestMultiple; // the next one

                    int targetHFullIndex1 = -1;
                    int targetHFullIndex2 = -1;

                    for (Map.Entry<Integer, Integer> entry : paneIndexToHIntIndex.entrySet()) {
                        int fullIndex = entry.getKey();
                        int hIntIndex = entry.getValue();

                        if (hIntIndex == targetHIntIndex1) {
                            targetHFullIndex1 = fullIndex;
                        }
                        if (hIntIndex == targetHIntIndex2) {
                            targetHFullIndex2 = fullIndex;
                        }
                    }

                    if (targetHFullIndex1 == -1 || targetHFullIndex2 == -1) {
                        System.out.println("No matching full index found for one or both vInt indices.");
                    } else {

                        //Debug: Show the whole paneIndexToVIntIndex map
                    //    System.out.println("paneIndexToHIntIndex contents:");
                    //     paneIndexToVIntIndex.forEach((fullIndex, hIntIndex) ->
                    //         System.out.println("  fullIndex=" + fullIndex + ", hIntIndex=" + hIntIndex)
                    //     );

                        int startHIntShiftIndex = closestMultiple;

                     //   System.out.println("\nFiltering for hIntIndex >= " + startHIntShiftIndex + "...");

                        // Collect full indices of all vInts from startVIntShiftIndex to end
                        List<Integer> fullHIndicesToShift = paneIndexToHIntIndex.entrySet().stream()
                            .filter(e -> e.getValue() >= startHIntShiftIndex)
                      //      .peek(e -> System.out.println("  Keeping entry: fullIndex=" + e.getKey() + ", hIntIndex=" + e.getValue()))
                            .map(Map.Entry::getKey)
                            .sorted()
                            .collect(Collectors.toList());

                   //     System.out.println("\nFull indices to shift: " + fullHIndicesToShift);

                        
                        for (int fullIndex : fullHIndicesToShift) {
                           
                            Node nodeH = routeHBoxPane.getChildren().get(fullIndex);
                            if (nodeH instanceof HBox) {
                        //        System.out.println("Made it to a shift for fullIndex: " + fullIndex); 
                                HBox hbox = (HBox) nodeH;
                                double currentX = hbox.getTranslateX();
                                hbox.setTranslateX(currentX - cardWidthUnit);
                            }
                        }

                        // Remove higher index first to avoid shifting problems
                        if (targetHFullIndex1 > targetHFullIndex2) {
                            routeHBoxPane.getChildren().remove(targetHFullIndex1);
                            routeHBoxPane.getChildren().remove(targetHFullIndex2);
                        } else {
                            routeHBoxPane.getChildren().remove(targetHFullIndex2);
                            routeHBoxPane.getChildren().remove(targetHFullIndex1);
                        }

                    }

                    Rental newLinkStart = route.get(closestMultiple - 1/* + accountForTruck*/);
                    Rental newLinkEnd = route.get(closestMultiple/* + accountForTruck*/);
      
                    RouteInfo newLink = new RouteInfo(0, 0, null);
                    try {
                        newLink = getGoogleRoute(newLinkStart.getLatitude(), 
                                newLinkStart.getLongitude(), newLinkEnd.getLatitude(),
                                newLinkEnd.getLongitude());
                    } catch (Exception e) {
                    }

                    int driveTimeInMinutes = (int) Math.round(newLink.getDurationSeconds() / 60.0);
                    Region newIntV = createStopIntermediary(driveTimeInMinutes, colors, "vertical");
                    Region newIntH = createStopIntermediary(driveTimeInMinutes, colors, "horizontal");
                    VBox newIntVBox = (VBox) newIntV;
                    HBox newIntHBox = (HBox) newIntH;
                    int singleDigitSpacerV = driveTimeInMinutes < 11 ? 7 : 0;
                    int singleDigitSpacerH = driveTimeInMinutes < 11 ? 0 : 10;
                    newIntVBox.setTranslateY(((closestMultiple) * cardHeightUnit) - 26 + singleDigitSpacerV);
                    newIntHBox.setTranslateY(9);
                    newIntVBox.setTranslateX(-1);
                    newIntHBox.setTranslateX(((closestMultiple) * cardWidthUnit) - 36 + singleDigitSpacerH);
                    routeVBoxPane.getChildren().add(targetVFullIndex1, newIntVBox);
                    routeHBoxPane.getChildren().add(targetHFullIndex1, newIntHBox);
                    StackPane.setAlignment(newIntHBox, Pos.TOP_LEFT);
                    polylineList.remove(closestMultiple - 1/* + accountForTruck*/);
                    polylineList.set(closestMultiple - 1/* + accountForTruck*/, newLink.getPolylinePoints());
                    intervalList.remove(closestMultiple - 1);
                    intervalList.set(closestMultiple - 1, driveTimeInMinutes);
                    newIntVBox.setClip(new Rectangle(15, 45));
                }
            updateRoutePolylines();
            }
            routeVBox.getChildren().remove(closestMultiple);
            routeHBox.getChildren().remove(closestMultiple);




            ////////////// Print System for child/parent shifting ////////////////
    /*      System.out.println("📦 routeVBoxPane children AFTER deletion:");
            ObservableList<Node> children2 = routeVBoxPane.getChildren();
            // Print each child
            for (int i = 0; i < children2.size(); i++) {
                System.out.println("    [" + i + "] " + children2.get(i));
            }
            // Section break
            System.out.println("\n──────────── Grouped by Type ────────────");
            // Group by class simple name
            Map<String, List<Node>> grouped2 = new TreeMap<>();
            for (Node child : children2) {
                String type = child.getClass().getSimpleName();
                grouped2.computeIfAbsent(type, k -> new ArrayList<>()).add(child);
            }
            // Print each group
            for (String type : grouped2.keySet()) {
                System.out.println("• " + type + "s:");
                for (Node child : grouped2.get(type)) {
                    System.out.println("    - " + child);
                }
            } */
            ///////////////////////////////////////////////////


            // delete truck assignment in memory and assignment ui elements
            // TODO: drop driver assignment as well
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
                        routeVBoxPane.getChildren().remove(pictoralTruckV);
                        routeHBoxPane.getChildren().remove(pictoralTruckH);
                        routeVBoxPane.getChildren().removeIf(node -> node instanceof Rectangle);
                        routeHBoxPane.getChildren().removeIf(node -> node instanceof Rectangle);
                     
                
                        StackPane pane = truckPanes.get(entry.getKey());
                        Circle truckCircle = (Circle) pane.getChildren().get(0);
                        pane.getChildren().clear();
                        truckCircle.setFill(Color.TRANSPARENT);
                        pane.getChildren().add(truckCircle);
                        routeAssignments.remove(routeName);
                        driverLabelsV.get(numeralRouteName).setText("?");
                        driverLabelsH.get(numeralRouteName).setText("?");
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


            RouteInfo googleRoute = new RouteInfo(0, 0, null);
            try {
                googleRoute = getGoogleRoute(firstRental.getLatitude(), firstRental.getLongitude(),
                                    secondRental.getLatitude(), secondRental.getLongitude());
            } catch (Exception e) {
            }
            int driveTimeInSeconds = googleRoute.getDurationSeconds();
            int driveTimeInMinutes = (int) Math.round(driveTimeInSeconds / 60.0);

            // Check if the intermediary VBox is created correctly
            Region intermediaryRegionV = createStopIntermediary(driveTimeInMinutes, colors, "vertical");
            Region intermediaryRegionH = createStopIntermediary(driveTimeInMinutes, colors, "horizontal");
            VBox intermediaryV = (VBox) intermediaryRegionV;
            HBox intermediaryH = (HBox) intermediaryRegionH;
            int singleDigitSpacerV = driveTimeInMinutes < 11 ? 7 : 0;
            int singleDigitSpacerH = driveTimeInMinutes < 11 ? 10 : 0;

            routeVBoxPane.getChildren().add(2, intermediaryV);
            routeHBoxPane.getChildren().add(0, intermediaryH);
            StackPane.setAlignment(intermediaryH, Pos.TOP_LEFT);
            /* 
            System.out.println("routeVBoxPane children:");
            for (Node node : routeVBoxPane.getChildren()) {
                System.out.println(" - " + node);
            }   */
            
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
            polylineList.add(0, googleRoute.getPolylinePoints());
            intervalList.add(0, driveTimeInMinutes);
            //  storedEncodedPolylines.add(googleResults[1]);
            updateRoutePolylines();
        
            routeVBoxPane.toFront();
            routeVBox.toFront();
         //   intermediaryH.toFront();

            StackPane rentalChunkV = createRentalChunk(rental, colors, "vertical-truck", routeName, closestMultiple);
            StackPane rentalChunkH = createRentalChunk(rental, colors, "horizontal-truck", routeName, closestMultiple);
            routeVBox.getChildren().add(0, rentalChunkV);
            routeHBox.getChildren().add(0, rentalChunkH);
    
            rentalChunkH.setTranslateY(14);
            rentalChunkH.setTranslateX(5);
            rentalChunkV.setTranslateY(3);


            // V variant
            pinpointerV.setFill(Color.web(colors[0]));
            pinpointerV.setTranslateX(-22);
            pinpointerV.setOnMouseEntered(event -> {
                innerPinpointerV.setFill(Color.web(colors[0]));
                if (routeName.equals("route2")) driverLabelV.setTextFill(Color.WHITE);
                else if (routeName.equals("route3")) driverLabelV.setTextFill(Color.web(colors[1]));
            });
            pinpointerV.setOnMouseExited(event -> {
                innerPinpointerV.setFill(Color.web(colors[1]));
                driverLabelV.setTextFill(Color.web(colors[2]));
            });
            pinpointerV.setOnMouseClicked(event -> {
                removeCardCovers();
                handleDriverExpander(routeName, numeralRouteName, colors, "vertical");
                event.consume();
            });


            innerPinpointerV.setFill(Color.web(colors[1]));
            innerPinpointerV.setTranslateX(-22);
            innerPinpointerV.setMouseTransparent(true);
            driverLabelV.setTextFill(Color.web(colors[2]));
            driverLabelV.setStyle("-fx-font-weight: bold");
            driverLabelV.setTranslateX(-22);
            driverLabelV.setMouseTransparent(true);


            Rectangle truckCatcherV = new Rectangle(85, 26);
            truckCatcherV.setFill(Color.TRANSPARENT);
            truckCatcherV.setTranslateX(17);
            truckCatcherV.setTranslateY(5);
            StackPane.setAlignment(truckCatcherV, Pos.TOP_LEFT);

            // new image views
            Image originalFlatbedImage = new Image(getClass().getResourceAsStream("/images/flatbed.png"));
            Image infoFlatbedImage = new Image(getClass().getResourceAsStream("/images/flatbed-info.png"));
            Image modifiedBase = changePixelColor(originalFlatbedImage, colors);
            Image modifiedHover = changePixelColor(infoFlatbedImage, colors);
            
            ImageView imageViewV = new ImageView(modifiedBase);
            imageViewV.setFitWidth(98);
            imageViewV.setFitHeight(25);
            imageViewV.setMouseTransparent(true);
            
            pictoralTruckV.getChildren().removeAll();
            pictoralTruckV.getChildren().add(imageViewV);
            pictoralTruckV.setMouseTransparent(true);
            pictoralTruckV.setTranslateX(13);
            pictoralTruckV.setTranslateY(6);
            // pictoralTruckV.setMaxHeight(25);
            // pictoralTruckV.setMaxWidth(82);
            StackPane.setAlignment(pictoralTruckV, Pos.TOP_LEFT);

            Rectangle truckCatcherH = new Rectangle(85, 26);
            truckCatcherH.setFill(Color.TRANSPARENT);
            truckCatcherH.setTranslateX(0);
            truckCatcherH.setTranslateY(0);
            StackPane.setAlignment(truckCatcherH, Pos.CENTER_LEFT);


            ImageView imageViewH = new ImageView(modifiedBase);
            imageViewH.setFitWidth(82);
            imageViewH.setFitHeight(25);
            imageViewH.setTranslateY(-2);
            imageViewH.setTranslateX(10);
            imageViewH.setMouseTransparent(true);
            StackPane.setAlignment(imageViewH, Pos.CENTER_LEFT);

            pictoralTruckH.getChildren().removeAll();
            pictoralTruckH.getChildren().add(imageViewH);
            pictoralTruckH.setMouseTransparent(true);
            StackPane.setAlignment(pictoralTruckH, Pos.CENTER_LEFT);

            // truckCatcherV.setOnMouseEntered(e -> showDetailedTruck(pictoralTruckV, imageViewV, modifiedHover, routeName));
            // truckCatcherV.setOnMouseExited(e -> showDefaultTruck(pictoralTruckV, imageViewV, modifiedBase, routeName));


            routeVBoxPane.getChildren().addAll(truckCatcherV, pictoralTruckV, pinpointerV, innerPinpointerV, driverLabelV);


            // H variant
            pinpointerH.setFill(Color.web(colors[0]));
            pinpointerH.setTranslateY(20);
            pinpointerH.setOnMouseEntered(event -> {
                innerPinpointerH.setFill(Color.web(colors[0]));
                if (routeName.equals("route2")) driverLabelH.setTextFill(Color.WHITE);
                else if (routeName.equals("route3")) driverLabelH.setTextFill(Color.web(colors[1]));
            });
            pinpointerH.setOnMouseExited(event -> {
                innerPinpointerH.setFill(Color.web(colors[1]));
                driverLabelH.setTextFill(Color.web(colors[2]));
            });
            pinpointerH.setOnMouseClicked(event -> {
                removeCardCovers();
                System.out.println("pinpointer registered a click");
                handleDriverExpander(routeName, numeralRouteName, colors, "horizontal");
                event.consume();
            });

            innerPinpointerH.setFill(Color.web(colors[1]));
            innerPinpointerH.setTranslateY(20);
            innerPinpointerH.setMouseTransparent(true);
            StackPane.setAlignment(innerPinpointerH, Pos.CENTER_LEFT);
            StackPane.setAlignment(pinpointerH, Pos.CENTER_LEFT);
            driverLabelH.setTextFill(Color.web(colors[2]));
            driverLabelH.setStyle("-fx-font-weight: bold");
            driverLabelH.setTranslateY(20);
            driverLabelH.setTranslateX(15);
            driverLabelH.setMouseTransparent(true);
            StackPane.setAlignment(driverLabelH, Pos.CENTER_LEFT);


            // new image views

            routeHBoxPane.getChildren().addAll(truckCatcherH, pictoralTruckH, pinpointerH, innerPinpointerH, driverLabelH);
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
/*
        hInts = routeHBoxPane.getChildren().stream()
            .filter(node -> node.getClass().getName().contains("MapController$"))
            .collect(Collectors.toList());

        System.out.println("hInts contents (post-update):");
        for (Node n : hInts) {
            StringBuilder labelText = new StringBuilder();

            // Walk the hierarchy recursively to collect label texts
            collectLabelText(n, labelText);

            // Get translateX
            double tx = n.getTranslateX();

            // Try to get alignment if parent is a StackPane
            Pos alignment = null;
            if (n.getParent() instanceof StackPane) {
                alignment = StackPane.getAlignment(n);
            }

            System.out.println(" - " + n 
                + "   text: " + labelText
                + "   translateX: " + tx);
        }


        // Print all child nodes, preserving order
        System.out.println("Children (post-update) of routeHBoxPane (index " + index + "):");
        for (Node child : routeHBoxPane2.getChildren()) {
            System.out.println(" - " + child);
        }
*/
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
            int charLimit = orientation.equals("vertical") ? 20 : 17; // Adjusted for 95px width
            int sidePadding = orientation.equals("vertical") ? -2 : 1;

            // Create labels with width-based truncation and set padding for left/right buffer
            Font boldFont = Font.font("System", FontWeight.BOLD, 12);
            Font regularFont = Font.font("System", 12);
            double maxLabelWidth = orientation.equals("vertical") ? 101 : 86;
            double cityLineDeduction = orientation.equals("verical") ? 26 : 20;

            // Name label (bold)
            String rawName = Config.CUSTOMER_NAME_MAP.getOrDefault(rental.getName(), rental.getName());
            String name = rawName.replace(".", "");
            Label nameLabel = new Label(truncateTextToWidth(name, maxLabelWidth - cityLineDeduction, boldFont));
            nameLabel.setFont(boldFont);
            nameLabel.setStyle("-fx-text-fill: " + colors[2] + ";");
            nameLabel.setMaxWidth(maxLabelWidth);
            nameLabel.setPadding(new Insets(0, sidePadding, 0, sidePadding));

            // Address block two (regular)
            Label address2 = new Label(truncateTextToWidth(rental.getAddressBlockTwo(), maxLabelWidth, regularFont));
            address2.setFont(regularFont);
            address2.setStyle("-fx-text-fill: " + colors[2] + ";");
            address2.setMaxWidth(maxLabelWidth);
            address2.setPadding(new Insets(0, sidePadding - 2, 0, sidePadding));

            // Address block three (regular, conditional padding/limit preserved)
            Label address3 = new Label(truncateTextToWidth(rental.getAddressBlockThree(), maxLabelWidth - cityLineDeduction, regularFont));
            address3.setFont(regularFont);
            address3.setStyle("-fx-text-fill: " + colors[2] + ";");
            address3.setMaxWidth(maxLabelWidth);
            address3.setPadding(new Insets(0, sidePadding, 0, sidePadding));

            if (orientation.equals("horizontal")) {
                nameLabel.setTranslateX(-2);
                address2.setTranslateX(-2);
                address3.setTranslateX(-2);
            }

            Label liftType = new Label(rental.getLiftType());
            rentalChunk.getChildren().add(liftType);
            liftType.setAlignment(Pos.BOTTOM_RIGHT);
            liftType.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colors[2] + ";");
            int liftTypeTranslateX = orientation.equals("vertical") ? -3 : -8;
            liftType.setTranslateX(liftTypeTranslateX);
            StackPane.setAlignment(liftType, Pos.BOTTOM_RIGHT);
            if (orientation.equals("horizontal") || orientation.equals("horizontal-truck")) {
                liftType.setTranslateY(-17);
            }

            // Add labels to the VBox
          //  if (orientation.equals("horizontal-truck") || orientation.equals("vertical-truck")) {
            labelBox.getChildren().addAll(nameLabel, address2, address3);

            Shape dot = createRentalDot(rental, rental.getStatus());
            rentalChunk.getChildren().add(dot);
            StackPane.setAlignment(dot, Pos.TOP_RIGHT);
            dot.setTranslateX(orientation.equals("vertical") ? -2 : -7);
            dot.setTranslateY(orientation.equals("vertical") ? 5 : 3);

        } else if (orientation.equals("horizontal-truck") || orientation.equals("vertical-truck")) {
            truckPane.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));
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
            truckLabel.setTranslateX(orientation.equals("horizontal-truck") ? timelineCapRange - 8 : timelineCapRange - 3);
            truckLabel.setTranslateY(orientation.equals("horizontal-truck") ? -14 : -12);
            Color contentColor = Color.web(colors[2]); 
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


    private Region createStopIntermediary(int driveTimeInMinutes, String[] colors, String orientation) {
    
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
    
    
    private StackPane createCardOptionsCover(Rental rental, int closestMultiple, double x, double y, int routeIndex, String orientation) {
        double buttonCoverHeight;
        double buttonCoverWidth;
        double centerX;
        double centerY;
        double leftCardCenterX;
        double rightCardCenterX;

        if (orientation.equals("vertical")) {
            buttonCoverHeight = cardHeightUnit - 8;
            buttonCoverWidth = cardWidthUnit / 2;
            centerX = 1;
            centerY = -4;
            leftCardCenterX = (-cardWidthUnit / 4) + 1;
            rightCardCenterX = (cardWidthUnit / 4) + 1;
        } else {
            buttonCoverHeight = cardHeightUnit;
            buttonCoverWidth = (cardWidthUnit / 2) - 8;
            centerX =  -3;
            centerY = 2;
            leftCardCenterX = (-cardWidthUnit) / 4;
            rightCardCenterX = ((cardWidthUnit - 16) / 4) - 3; 
        }
        
        
        // Create the cover rectangle
        Rectangle deleteCover = new Rectangle(buttonCoverWidth, buttonCoverHeight);
        deleteCover.setFill(Color.web("#F4F4F4"));
        deleteCover.setOpacity(0.75); // Adjust opacity to make it slightly transparent
        deleteCover.setTranslateX(rightCardCenterX);
        deleteCover.setTranslateY(centerY);

        // Load the delete icon from resources
        Image deleteImage = new Image(getClass().getResource("/images/delete.png").toExternalForm());
        ImageView deleteIcon = new ImageView(deleteImage);
        deleteIcon.setFitWidth(30);  // Adjust size as needed
        deleteIcon.setFitHeight(30);
        deleteIcon.setTranslateX(rightCardCenterX);
        deleteIcon.setTranslateY(centerY);
        
        deleteIcon.setOnMouseClicked(event -> {
            removeFromRoute(rental, closestMultiple, routeIndex);
        });
        deleteIcon.setPickOnBounds(true);
        deleteIcon.setMouseTransparent(false);

        // Create a thin vertical line as a visual separator
        Line separatorLine = new Line(0, 0, 0, buttonCoverHeight);
        separatorLine.setStroke(Color.BLACK);
        separatorLine.setStrokeWidth(1);
        separatorLine.setTranslateX(centerX);  // Centered between the covers
        separatorLine.setTranslateY(centerY);

        Rectangle expandCover = new Rectangle(buttonCoverWidth, buttonCoverHeight);
        expandCover.setFill(Color.web("#F4F4F4"));
        expandCover.setOpacity(0.75);
        expandCover.setTranslateX(leftCardCenterX);
        expandCover.setTranslateY(centerY);

        Image expandImage = new Image(getClass().getResource("/images/expand.png").toExternalForm());
        ImageView expandIcon = new ImageView(expandImage);
        expandIcon.setFitWidth(30);
        expandIcon.setFitHeight(30);
        expandIcon.setTranslateX(leftCardCenterX);
        expandIcon.setTranslateY(centerY);
        expandIcon.setPickOnBounds(true);

        expandIcon.setOnMouseClicked(event -> {
            try {
                BaseController active = MaxReachPro.getCurrentController();
                ObservableList<Rental> currentList = (active != null) ? active.getActiveRentalList() : null;
                removeCardCovers();
                MaxReachPro.setRentalForExpanding(rental, currentList);
                MaxReachPro.loadScene("/fxml/expand.fxml");
            } catch (Exception e) {
                e.printStackTrace();
            }     
        });

        // Add a glow effect
        Glow glow = new Glow(0.8);  // Adjust intensity (0 to 1)
        deleteIcon.setEffect(glow);
        expandIcon.setEffect(glow);
    
        // StackPane to center the image on the rectangle
        StackPane stack = new StackPane(deleteCover, deleteIcon, separatorLine, expandCover, expandIcon/*,
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
                label.setOnMouseClicked(e -> handleAssignTruck(vboxPane, hboxPane, text, routeName));
        
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
                label.setOnMouseClicked(e -> handleAssignTruck(hboxPane, vboxPane, text, routeName));
        
                labelRow.getChildren().add(label);
            }
        
            hboxPane.getChildren().add(labelRow);
            StackPane.setAlignment(labelRow, Pos.TOP_CENTER);
        }
        
    }
    
    
    private void handleAssignTruck(StackPane routePane, StackPane routePane2, String labelString, String routeName) {
        removeCardCovers();
        if (truckAssignments.containsValue(routeName)) {
            removeFromRoute(routes.get(routeName).get(0), 0, getRouteIndex(routeName));
        }
        addTruckToRoute(routeName, labelString, "user");
        updateInnerTruckLabel(routePane, labelString);
        updateInnerTruckLabel(routePane2, labelString);
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
                label.setOnMouseClicked(e -> handleAssignDriver(vboxPane, initials, numeralRouteName, colors));
            
                labelColumn.getChildren().add(label);
            }
    
            vboxPane.getChildren().add(labelColumn);
            StackPane.setAlignment(labelColumn, Pos.CENTER_LEFT);
        
        //    System.out.println("finished the vertical part of handledriverexpander");
        
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
                label.setOnMouseClicked(e -> handleAssignDriver(vboxPane, initials, numeralRouteName, colors));
            
                labelRow.getChildren().add(label);
            }
    
            hboxPane.getChildren().add(labelRow);
            StackPane.setAlignment(labelRow, Pos.TOP_CENTER);
        
       
        }
        
    }

    private void updateInnerTruckLabel(StackPane parentPane, String labelText) {
        if (parentPane.getChildren().isEmpty()) return;

        Node cardsNode = parentPane.getChildren().get(0);
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
                                System.out.println("Found Label. Setting text to: " + labelText);
                                ((Label) innerChild).setText(labelText);
                                return;
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
    }


    private void handleAssignDriver(StackPane routePane, String labelString, String routeName, String[] colors) {
        String truckId = getTruckIdForRoute(routeName);
        configureTruckDot(labelString, truckId, colors);
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


    private void showDefaultTruck(StackPane pane, ImageView imageView, Image flatbedImage, String routeName) {
        imageView.setImage(flatbedImage);
    }

    private void showDetailedTruck(StackPane pane, ImageView imageView, Image flatbedInfoImage, String routeName) {
        imageView.setImage(flatbedInfoImage);
        System.out.println("showDetailedTruck triggered");

    }


    

    /*                                    /*
     *              PROGRESS               *
    /*                                     */
    private void setupLiveDriveProgressions() {
        Map<String, Double> lastProportions = new HashMap<>();
    
        Runnable task = () -> Platform.runLater(() -> {
            try {
                Map<String, Double> currentProportions = getProgressReport("geometric");
                // === NEW: Handle completed stops first ===
                List<Rental> completedRentals = findCompletedStops();
                List<Rental> rentalsToRemoveFromCharting = new ArrayList<>();
                
                for (Rental rental : completedRentals) {
                    for (Map.Entry<String, List<Rental>> entry : routes.entrySet()) {
                        List<Rental> rentals = entry.getValue();
                        int index = rentals.indexOf(rental);
                        if (index != -1) {
                            int routeIndex = getRouteIndex(entry.getKey());
                            removeFromRoute(rental, index, routeIndex);
                            if (rental.getStatus().equals("Upcoming")) {
                            } else if (rental.getStatus().equals("Called Off")) {
                                editVisualCargo(rental, routeIndex, "picking-up");
                            }
                            break; // Rental is only in one route
                        }
                    }
                    rentalsToRemoveFromCharting.add(rental);
                }
                
                loadRentalDataFromAPI();

                // ✅ Safely remove afterward
                for (Rental rentalToRemove : rentalsToRemoveFromCharting) {
                    int idToRemove = rentalToRemove.getRentalItemId();

                    Rental match = null;
                    for (Rental rental : rentalsForCharting) {
                        if (rental.getRentalItemId() == idToRemove) {
                            match = rental;
                            break;
                        }
                    }

                    if (match != null) {
                        rentalsForCharting.remove(match);
                    }
                }


                updateRentalLocations();
    
                // === Animate progress ===
                for (Map.Entry<String, Double> entry : currentProportions.entrySet()) {
                    String routeKey = entry.getKey();
                    double current = entry.getValue();
                    double previous = lastProportions.getOrDefault(routeKey, current);
    
                    double visualCompletionRatioV = 15.0 / 17.0;
                    double prevScaledV = Math.min(previous / visualCompletionRatioV, 1.0);
                    double currScaledV = Math.min(current / visualCompletionRatioV, 1.0);
                    double visualCompletionRatioH = 15.0 / 22.0;
                    double prevScaledH = Math.min(previous / visualCompletionRatioH, 1.0);
                    double currScaledH = Math.min(current / visualCompletionRatioH, 1.0);
    
                    double prevXV = -22 + prevScaledV * 55;
                    double currXV = -22 + currScaledV * 55;
                    double prevXH = 15 + prevScaledH * 55;
                    double currXH = 15 + currScaledH * 55;
    
                    Timeline timelineV = new Timeline(
                            new KeyFrame(Duration.ZERO, new KeyValue(pinpointersV.get(routeKey).translateXProperty(), prevXV)),
                            new KeyFrame(Duration.minutes(1), new KeyValue(pinpointersV.get(routeKey).translateXProperty(), currXV))
                    );
                    timelineV.play();

                    Timeline timelineH = new Timeline(
                            new KeyFrame(Duration.ZERO, new KeyValue(pinpointersH.get(routeKey).translateXProperty(), prevXH)),
                            new KeyFrame(Duration.minutes(1), new KeyValue(pinpointersH.get(routeKey).translateXProperty(), currXH))
                    );
                    timelineH.play();
    
                    Timeline innerPinpointerTimelineV = new Timeline(
                            new KeyFrame(Duration.ZERO, new KeyValue(innerPinpointersV.get(routeKey).translateXProperty(), prevXV)),
                            new KeyFrame(Duration.minutes(1), new KeyValue(innerPinpointersV.get(routeKey).translateXProperty(), currXV))
                    );
                    innerPinpointerTimelineV.play();

                    Timeline innerPinpointerTimelineH = new Timeline(
                            new KeyFrame(Duration.ZERO, new KeyValue(innerPinpointersH.get(routeKey).translateXProperty(), prevXH)),
                            new KeyFrame(Duration.minutes(1), new KeyValue(innerPinpointersH.get(routeKey).translateXProperty(), currXH + 4))
                    );
                    innerPinpointerTimelineH.play();
    
                    Timeline driverLabelTimelineV = new Timeline(
                            new KeyFrame(Duration.ZERO, new KeyValue(driverLabelsV.get(routeKey).translateXProperty(), prevXV)),
                            new KeyFrame(Duration.minutes(1), new KeyValue(driverLabelsV.get(routeKey).translateXProperty(), currXV))
                    );
                    driverLabelTimelineV.play();

                    Timeline driverLabelTimelineH = new Timeline(
                            new KeyFrame(Duration.ZERO, new KeyValue(driverLabelsH.get(routeKey).translateXProperty(), prevXH)),
                            new KeyFrame(Duration.minutes(1), new KeyValue(driverLabelsH.get(routeKey).translateXProperty(), currXH + 6))
                    );
                    driverLabelTimelineH.play();
    
                    lastProportions.put(routeKey, current);
    
                    Optional<String> assignedTruck = truckAssignments.entrySet().stream()
                        .filter(e -> e.getValue().equals(routeKey))
                        .map(Map.Entry::getKey)
                        .findFirst();
    
                    if (assignedTruck.isEmpty()) return;
    
                    if (decodedPolylines.containsKey(routeKey) && trucks.containsKey(assignedTruck.get())) {
                        List<double[]> decodedPolyline = decodedPolylines.get(routeKey).get(0);
                        StackPane truckPane = truckPanes.get(assignedTruck.get());
                        animateTruckAlongPolyline(assignedTruck.get(), decodedPolyline, previous, /*Math.random()*/current, truckPane);
                    }
                }
    
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    
        scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.MINUTES);
        System.out.println("");
    }
    


    public void animateTruckAlongPolyline(
        String truckId,
        List<double[]> decodedPolyline,
        double oldProportion,
        double newProportion,
        StackPane truckPane
    ) {
        if (decodedPolyline == null || decodedPolyline.size() < 2) return;
    
        // Precompute segment lengths if not stored
        List<Double> segmentLengths = truckSegmentLengths.computeIfAbsent(truckId, k -> {
            List<Double> lengths = new ArrayList<>();
            for (int i = 0; i < decodedPolyline.size() - 1; i++) {
                double[] p1 = decodedPolyline.get(i);
                double[] p2 = decodedPolyline.get(i + 1);
                lengths.add(haversine(p1[0], p1[1], p2[0], p2[1]));
            }
            return lengths;
        });
    
        // Compute intermediate lat/lon steps
        List<double[]> steps = new ArrayList<>();
        int stepCount = 60;
        for (int i = 0; i <= stepCount; i++) {
            double t = (double) i / stepCount;
            double proportion = oldProportion + t * (newProportion - oldProportion);
            steps.add(getPointAtProportion(decodedPolyline, segmentLengths, proportion));
        }
        truckPolylineSteps.put(truckId, steps);
    
        // Cancel previous timeline if it exists
        Timeline existing = truckTimelines.get(truckId);
        if (existing != null) existing.stop();
    
        Timeline timeline = new Timeline();
        for (int i = 0; i < steps.size(); i++) {
            double[] latLon = steps.get(i);
            KeyFrame frame = new KeyFrame(
                Duration.seconds(i * 1), // 1 second per step, you can adjust
                new KeyValue(truckPane.translateXProperty(), mapLongitudeToTranslateX(latLon[1])),
                new KeyValue(truckPane.translateYProperty(), mapLatitudeToTranslateY(latLon[0]))
            );
            timeline.getKeyFrames().add(frame);
        }
    
        timeline.setOnFinished(e -> {
            activeAnimations.remove(truckPane);
            truckTranslateX.put(truckPane.getId(), truckPane.getTranslateX());
            truckTranslateY.put(truckPane.getId(), truckPane.getTranslateY());
        });
    
        truckTimelines.put(truckId, timeline);
        activeAnimations.add(truckPane);
        timeline.play();
    }
    
    
    
    private double[] getPointAtDistance(
        List<double[]> decodedPolyline,
        List<Double> segmentLengths,
        double targetDistance) {
        double traveled = 0.0;
        for (int i = 0; i < segmentLengths.size(); i++) {
            double segLen = segmentLengths.get(i);
            if (traveled + segLen >= targetDistance) {
                double ratio = (targetDistance - traveled) / segLen;
                double[] from = decodedPolyline.get(i);
                double[] to = decodedPolyline.get(i + 1);
                double lat = from[0] + ratio * (to[0] - from[0]);
                double lon = from[1] + ratio * (to[1] - from[1]);
                return new double[]{lat, lon};
            }
            traveled += segLen;
        }
        return decodedPolyline.get(decodedPolyline.size() - 1);
    }
    
    private double[] getPointAtProportion(
        List<double[]> decodedPolyline,
        List<Double> segmentLengths,
        double proportion
    ) {
        double totalLength = segmentLengths.stream().mapToDouble(Double::doubleValue).sum();
        double targetDistance = proportion * totalLength;
        return getPointAtDistance(decodedPolyline, segmentLengths, targetDistance);
    }
    

    // Hybrid inputs: real driver progress and linear ETA simulation
    public Map<String, Double> getProgressReport(String type) {
        Map<String, Double> proportions = new HashMap<>();

        try {
            new URL("http://5.78.73.173:8080/routes/fleet/refresh").openConnection().getInputStream().close();
            updateTruckCoordinates();

            URL url = new URL("http://5.78.73.173:8080/routes/fetch");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            JsonNode root = new ObjectMapper().readTree(conn.getInputStream());
            conn.disconnect();

            Map<Integer, JsonNode> stopIdToNode = new HashMap<>();
            for (JsonNode routeNode : root) {
                JsonNode stops = routeNode.get("stops");
                if (stops != null && stops.isArray()) {
                    for (JsonNode stop : stops) {
                        if (stop.has("id")) stopIdToNode.put(stop.get("id").asInt(), stop);
                    }
                }
            }

            for (Map.Entry<String, List<Rental>> entry : routes.entrySet()) {
                String key = entry.getKey();
                String truckId = truckAssignments.entrySet().stream()
                    .filter(e -> e.getValue().equals(key))
                    .map(Map.Entry::getKey)
                    .findFirst().orElse(null);

                if (truckId == null) continue;
                List<Rental> rentalList = entry.getValue();
                if (rentalList == null || rentalList.isEmpty()) continue;

                Rental rentalToUse = rentalList.stream()
                    .filter(r -> r.getRentalItemId() != 0)
                    .findFirst().orElse(null);
                if (rentalToUse == null) continue;

                JsonNode stopMatch = stopIdToNode.get(rentalToUse.getRentalItemId());
                if (stopMatch == null || !stopMatch.has("departedTime") || stopMatch.get("departedTime").isNull()) continue;

                String departedTimeString = stopMatch.get("departedTime").asText();
                LocalDateTime departedTimeLocal = LocalDateTime.parse(departedTimeString);
                ZonedDateTime departedZoned = departedTimeLocal.atZone(ZoneId.of("America/Denver"));
                ZonedDateTime nowZoned = ZonedDateTime.now(ZoneId.of("America/Denver"));
                long elapsedMinutes = java.time.Duration.between(departedZoned, nowZoned).toMinutes();

                List<Integer> etaList = intervals.get(key);
                if (etaList == null || etaList.isEmpty()) continue;
                int etaMinutes = etaList.get(0);

                double elapsedProportion = Math.min((double) elapsedMinutes / etaMinutes, 1.0);

                double[] truckLocation = truckCoords.get(truckId);
                if (truckLocation == null || truckLocation[0] == 0 || truckLocation[1] == 0) continue;

                double gpsProportion;

                if (type.equalsIgnoreCase("geometric")) {
                    List<List<double[]>> routeSegments = decodedPolylines.get(key);
                    if (routeSegments == null || routeSegments.isEmpty()) continue;

                    List<double[]> activePolyline = routeSegments.get(0);
                    gpsProportion = estimatePolylineProgress(activePolyline, truckLocation);

                } else { // fallback to ETA-based
                    double[] destination = {rentalToUse.getLatitude(), rentalToUse.getLongitude()};
                    RouteInfo googleRoute = getGoogleRoute(truckLocation[0], truckLocation[1], destination[0], destination[1]);
                    int remainingSeconds = googleRoute.getDurationSeconds();
                    gpsProportion = 1.0 - Math.min((double) remainingSeconds / (etaMinutes * 60), 1.0);
                }

                double faster = Math.max(elapsedProportion, gpsProportion);
                double slower = Math.min(elapsedProportion, gpsProportion);
                double dynamicProportion = 0.3 * faster + 0.7 * slower;

                proportions.put(key, dynamicProportion);
            }

        } catch (Exception e) {
            System.out.println("🔥 Exception in getProgressReport: " + e);
            e.printStackTrace();
        }

        return proportions;
    }

    
    private void editVisualCargo(Rental rental, int routeIndex, String orientation) {
        if (!"picking-up".equals(orientation)) {
            return;
        }

        String routeKey = "route" + (routeIndex + 1);

        StackPane pictoralTruckV = pictoralTrucksV.get(routeKey);
        StackPane pictoralTruckH = pictoralTrucksH.get(routeKey);

        if (pictoralTruckV == null || pictoralTruckH == null) {
            return;
        }

        if (pictoralTruckV.getChildren().isEmpty() || pictoralTruckH.getChildren().isEmpty()) {
            return;
        }

        ImageView baseImageViewV = (ImageView) pictoralTruckV.getChildren().get(0);
        ImageView baseImageViewH = (ImageView) pictoralTruckH.getChildren().get(0);

        pictoralTruckV.getChildren().clear();
        pictoralTruckH.getChildren().clear();

        pictoralTruckV.getChildren().add(baseImageViewV);
        pictoralTruckH.getChildren().add(baseImageViewH);

        // Add cargo to route inventory
        List<String> cargoList = inventories.computeIfAbsent(routeKey, k -> new ArrayList<>());
        String cargoName = rental.getLiftType();
        cargoList.add(cargoName);

        int counter = 1;
        String[] colors = getRouteColors(routeKey);
        Color highlightColor = Color.web(colors[2]);

        for (String item : cargoList) {
            String imageName = item + "-peek.png";

            InputStream imageStreamV = getClass().getClassLoader().getResourceAsStream("images/" + imageName);

            if (imageStreamV != null) {
                Image imageV = new Image(imageStreamV);
                Image imageRecoloredV = recolorImage(imageV, highlightColor, null);

                // Vertical peek
                ImageView imageViewV = new ImageView(imageRecoloredV);
                imageViewV.setFitWidth(20);
                imageViewV.setFitHeight(27);
                imageViewV.setTranslateX(-59 + (counter * 19));
                pictoralTruckV.getChildren().add(imageViewV);

                // Horizontal peek
                ImageView imageViewH = new ImageView(imageRecoloredV);
                imageViewH.setFitWidth(27);
                imageViewH.setFitHeight(20);
                imageViewH.setTranslateY(-25 + (counter * 19));
                pictoralTruckH.getChildren().add(imageViewH);
            }

            counter++;
        }
    }



    /*                                    /*
     *              UTILITIES              *
    /*                                     */

    // debugging delete
    private void collectLabelText(Node node, StringBuilder sb) {
        if (node instanceof Label) {
            sb.append(((Label) node).getText());
        } else if (node instanceof Pane) {
            for (Node child : ((Pane) node).getChildren()) {
                collectLabelText(child, sb);
            }
        }
    }


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


    public double mapLongitudeToTranslateX(double lon) {
        double visibleMinX = visibleBounds[3];
        double visibleMaxX = visibleBounds[1];
    
        double relativeX = (lon - visibleMinX) / (visibleMaxX - visibleMinX);
        return relativeX * (Config.WINDOW_HEIGHT / 2); // or WIDTH if your map is wider
    }
    
    public double mapLatitudeToTranslateY(double lat) {
        double visibleMinY = visibleBounds[2];
        double visibleMaxY = visibleBounds[0];
    
        double observedOffset = 0.12;
        lat += observedOffset;
    
        double relativeY = (visibleMaxY - lat) / (visibleMaxY - visibleMinY);
        return relativeY * Config.WINDOW_HEIGHT;
    }


    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
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
                    secondary,
                    interpolateColorHex(Color.web(primary), Color.web(tertiary), .5),
                    "#ffffff"
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

    private String interpolateColorHex(Color c1, Color c2, double factor) {
        double r = c1.getRed() * (1 - factor) + c2.getRed() * factor;
        double g = c1.getGreen() * (1 - factor) + c2.getGreen() * factor;
        double b = c1.getBlue() * (1 - factor) + c2.getBlue() * factor;
        double a = c1.getOpacity() * (1 - factor) + c2.getOpacity() * factor;
    
        int ri = (int) Math.round(r * 255);
        int gi = (int) Math.round(g * 255);
        int bi = (int) Math.round(b * 255);
        int ai = (int) Math.round(a * 255);
    
        // If opacity is 1.0, return #RRGGBB; else include alpha
        if (ai == 255) {
            return String.format("#%02X%02X%02X", ri, gi, bi);
        } else {
            return String.format("#%02X%02X%02X%02X", ai, ri, gi, bi);
        }
    }
    

    public String getTruckIdForRoute(String routeName) {
        for (Map.Entry<String, String> entry : truckAssignments.entrySet()) {
            if (entry.getValue().equals(routeName)) {
                return entry.getKey(); // This is the truck ID
            }
        }
        return null; // Not found
    }    


    private RouteInfo getGoogleRoute(double originLat, double originLong, double destinationLat, double destinationLong) throws Exception {
        if (originLat == destinationLat && originLong == destinationLong) {
            System.out.println("[DEBUG] Same coordinates — returning placeholder route");
            List<double[]> placeholderPolyline = List.of(
                new double[]{originLat, originLong},
                new double[]{originLat, originLong}
            );
            return new RouteInfo(0, 0.0, placeholderPolyline);
        }
       
        // Construct JSON request body
        String requestBody = String.format(
            "{ \"origin\": { \"location\": { \"latLng\": { \"latitude\": %f, \"longitude\": %f } } }, " +
            "\"destination\": { \"location\": { \"latLng\": { \"latitude\": %f, \"longitude\": %f } } }, " +
            "\"travelMode\": \"DRIVE\", " +
            "\"polylineQuality\": \"OVERVIEW\", " +
            "\"routingPreference\": \"TRAFFIC_UNAWARE\" }",
            originLat, originLong, destinationLat, destinationLong
        );


        // Set up HTTP connection
        URL url = new URL("https://routes.googleapis.com/directions/v2:computeRoutes");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setRequestProperty("X-Goog-Api-Key", Config.GOOGLE_KEY);
        connection.setRequestProperty("X-Goog-FieldMask", "routes.duration,routes.polyline,routes.distanceMeters");

        byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
        connection.setRequestProperty("Content-Length", String.valueOf(input.length));
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(input);
        }

        int responseCode = connection.getResponseCode();
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

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        JSONObject jsonResponse = new JSONObject(response.toString());
        if (!jsonResponse.has("routes") || jsonResponse.getJSONArray("routes").isEmpty()) {
            throw new Exception("No routes found in response");
        }

        JSONObject route = jsonResponse.getJSONArray("routes").getJSONObject(0);
        if (!route.has("duration")) {
            throw new Exception("Duration not found in response");
        }

        String durationString = route.getString("duration"); // e.g., "1441s"
        int durationSeconds = Integer.parseInt(durationString.replace("s", ""));

        int distanceMeters = route.getInt("distanceMeters");
        double distanceMiles = distanceMeters / 1609.34;

        String encodedPolyline = route.getJSONObject("polyline").getString("encodedPolyline");
        List<double[]> polylinePoints = decodePolyline(encodedPolyline);

        return new RouteInfo(durationSeconds, distanceMiles, polylinePoints);
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


    private void animateTranslateX(Node node, double fromX, double toX) {
        TranslateTransition transition = new TranslateTransition(Duration.seconds(2), node);
        transition.setFromX(fromX);
        transition.setToX(toX);
        transition.setInterpolator(Interpolator.EASE_BOTH);
        transition.play();
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
        // Try routes in explicit order route1 to route5
        for (int i = 1; i <= 5; i++) {
            String routeKey = "route" + i;
            List<Rental> routeList = routes.get(routeKey);

            if (routeList != null && routeList.isEmpty()) {
                return new String[]{ routeKey, String.valueOf(i - 1) };
            }
        }

        // Look for latestRouteEdited
        if (latestRouteEdited != null) {
            int index = 0;
            for (int i = 1; i <= 5; i++) {
                String routeKey = "route" + i;
                List<Rental> routeList = routes.get(routeKey);
                if (routeList == latestRouteEdited) {
                    return new String[]{ routeKey, String.valueOf(index) };
                }
                index++;
            }
        }

        // Final fallback
        return new String[]{ "route1", "0" };
    }



    private double estimatePolylineProgress(List<double[]> polyline, double[] truckLocation) {
        if (polyline == null || polyline.size() < 2) return 0.0;

        double minDistance = Double.MAX_VALUE;
        int closestIndex = 0;

        // Find closest point on the polyline
        for (int i = 0; i < polyline.size(); i++) {
            double[] pt = polyline.get(i);
            double dist = haversine(truckLocation[0], truckLocation[1], pt[0], pt[1]);
            if (dist < minDistance) {
                minDistance = dist;
                closestIndex = i;
            }
        }

        // Sum up the total length
        double totalLength = 0.0;
        for (int i = 1; i < polyline.size(); i++) {
            totalLength += haversine(polyline.get(i - 1)[0], polyline.get(i - 1)[1], polyline.get(i)[0], polyline.get(i)[1]);
        }

        // Sum up the length to the closest point
        double traveledLength = 0.0;
        for (int i = 1; i <= closestIndex; i++) {
            traveledLength += haversine(polyline.get(i - 1)[0], polyline.get(i - 1)[1], polyline.get(i)[0], polyline.get(i)[1]);
        }

        return totalLength == 0 ? 0.0 : Math.min(traveledLength / totalLength, 1.0);
    }






    private Rental createNewRentalFromId(int rentalId){
        return new Rental(
            null, "'" + rentalId, null, null, null, null, null, false,
            "", "", "", 0,
            null, false, 0, null, null,
            0.0, 0.0, "", ""
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


    private Image changePixelColor(Image originalImage, String[] colors) {
        int width = (int) originalImage.getWidth();
        int height = (int) originalImage.getHeight();


        PixelReader pixelReader = originalImage.getPixelReader();
        WritableImage modifiedImage = new WritableImage(width, height);
        PixelWriter pixelWriter = modifiedImage.getPixelWriter();


        Color contentColor = Color.web(colors[2]);


        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixelColor = pixelReader.getColor(x, y);


                // Preserve transparency
                if (pixelColor.getOpacity() == 0.0) {
                    pixelWriter.setColor(x, y, pixelColor);
                    continue;
                }


                // Replace near-black with content color
                if (isNearBlack(pixelColor)) {
                    pixelWriter.setColor(x, y, contentColor);
                } else {
                    pixelWriter.setColor(x, y, pixelColor);
                }
            }
        }


        return modifiedImage;
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
    private String truncateTextToWidth(String text, double maxWidth, Font font) {
        if (text == null || text.isEmpty()) return "";
   
        Text helper = new Text();
        helper.setFont(font);
   
        String ellipsis = "...";
        helper.setText(ellipsis);
        double ellipsisWidth = helper.getLayoutBounds().getWidth();
   
        for (int i = 1; i <= text.length(); i++) {
            String substr = text.substring(0, i);
            helper.setText(substr);
            double textWidth = helper.getLayoutBounds().getWidth();
   
            if (textWidth + ellipsisWidth >= maxWidth) {
                return substr.substring(0, Math.max(0, i - 1)) + ellipsis;
            }
        }
        return text;
    }

    private void removeIntermediaryNode(Pane pane, Class<? extends Pane> type, String which, int routeSize) {
        List<Node> matching = new ArrayList<>();

        // Collect only anonymous inner class instances (e.g., MapController$1)
        for (Node child : pane.getChildren()) {
            Class<?> clazz = child.getClass();
            if (type.isInstance(child) && clazz.getName().contains("MapController$")) {
                matching.add(child);
            }
        }

        if (matching.isEmpty()) return;

        Node toRemove = null;

        switch (which) {
            case "first" -> toRemove = matching.get(0);
            case "last" -> toRemove = matching.get(matching.size() - 1);
            case "second" -> {
                if (matching.size() >= 2) toRemove = matching.get(1);
            }
            case "routeSizeMinus1" -> {
                int index = Math.min(matching.size() - 1, matching.size() - (routeSize - 1));
                if (index >= 0) toRemove = matching.get(index);
            }
            default -> System.out.println("Unknown removal spec: " + which);
        }

        if (toRemove != null) {
            pane.getChildren().remove(toRemove);
        }
    }


    @FXML
    private void resetStage() {
        MaxReachPro.getInstance().collapseStage();
    }
}    
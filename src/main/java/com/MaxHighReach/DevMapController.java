package com.MaxHighReach;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.application.Platform;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import org.json.JSONArray;
import org.json.JSONObject;
//import com.google.maps.routing.v2.*;

public class DevMapController {

    @FXML
    public AnchorPane anchorPane;

    @FXML
    private Button closeButton;

    private Rectangle mapArea = new Rectangle(); // The area where the map (dots) will be placed
    private Pane mapContainer;
    private ImageView metroMapView;
    private Map<String, double[]> truckCoords = new HashMap<>();
    private Map<String, TruckMarker> trucks = new HashMap<>();

    //
    private double[] mapBounds = {40.814076, -104.377137, 39.391122, -105.468393};
    private double[] visibleBounds = {0, 0, 0, 0};
    private double hqLat = 39.79503384433233;
    private double hqLon = -104.93205143793766;    
    private double lastDragX = -1;
    private double lastDragY = -1;

    private static class TruckMarker {
        Circle dot;
        javafx.scene.text.Text label;
        String status;
    }    

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
            setupMetroMap();
            startFleetPolling();
        });
        
    
        timeline.play();
        System.out.println("finished dev map constructor");


        for (int i = 0; i < Config.NUMBER_OF_TRUCKS; i++) {
            
        }

    }

    private void startFleetPolling() {

        Timeline t = new Timeline(
            new KeyFrame(Duration.seconds(0), e -> loadFleetAsync()),
            new KeyFrame(Duration.minutes(1))
        );
    
        t.setCycleCount(Timeline.INDEFINITE);
        t.play();
    }

    private void loadFleetAsync() {

        CompletableFuture.runAsync(() -> {
            updateTruckCoordinates();
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
            URL url = new URL("http://5.78.73.173:8080/routes/fleet-state");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            JSONObject fleetObj = new JSONObject(response.toString());
            for (String key : fleetObj.keySet()) {

                JSONObject truck = fleetObj.getJSONObject(key);
            
                String name = truck.getString("name");
                double lat  = truck.getDouble("lat");
                double lng  = truck.getDouble("lng");
            
                String status = truck.optString("status", null);
            
                // Only mutate plain data here
                truckCoords.put(name, new double[]{lat, lng});
            
                // store status only – no FX objects here
                TruckMarker m = trucks.get(name);
                if (m != null) {
                    m.status = status;
                }
            }
            
            

            Platform.runLater(this::updateTruckMarkers);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    

    /*                                    /*
     *               MAPPING               *
    /*                                     */
    
   
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


                    updateVisibleMapBounds(metroMapView);
                    updateTruckMarkers();
                }
            });

            
            
            updateVisibleMapBounds(metroMapView);

    
            // Add map to UI
            anchorPane.getChildren().add(mapContainer);
            mapArea.toFront();
            

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

    private TruckMarker createOrGetTruckMarker(String id) {

        TruckMarker m = trucks.get(id);
        if (m != null) return m;
    
        TruckMarker marker = new TruckMarker();
    
        Circle c = new Circle(4, Color.ORANGE);
        c.setStroke(Color.BLACK);
        c.setStrokeWidth(1);
    
        javafx.scene.text.Text t = new javafx.scene.text.Text(id);
        t.setFill(Color.BLACK);
        t.setStyle("-fx-font-size: 9px; -fx-font-weight: bold;");
    
        marker.dot = c;
        marker.label = t;
    
        trucks.put(id, marker);
    
        mapContainer.getChildren().addAll(c, t);
    
        return marker;
    }
    

    private void updateTruckMarkers() {

        if (mapContainer == null) return;
    
        for (Map.Entry<String, double[]> e : truckCoords.entrySet()) {
    
            String id = e.getKey();
            double lat = e.getValue()[0];
            double lon = e.getValue()[1];
    
            TruckMarker m = createOrGetTruckMarker(id);
    
            double x = mapLongitudeToX(lon);
            double y = mapLatitudeToY(lat);
    
            m.dot.setCenterX(x);
            m.dot.setCenterY(y);
    
            m.label.setX(x + 6);
            m.label.setY(y - 6);
    
            // ✅ color here (on FX thread)
            m.dot.setFill(colorForStatus(m.status));
        }
    }
    
    


    /*                                    /*
     *              UTILITIES              *
    /*                                     */


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

    private Color colorForStatus(String status) {

        if (status == null) return Color.GRAY;
    
        switch (status) {
            case "Moving":
                return Color.LIMEGREEN;
            case "Stopped":
                return Color.RED;
            case "Idle":
                return Color.GOLD;
            default:
                return Color.GRAY;
        }
    }
    

    @FXML
    private void resetStage() {
        MaxReachPro.getInstance().collapseStage();
    }
}    


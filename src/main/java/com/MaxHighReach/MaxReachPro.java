package com.MaxHighReach;


import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.StageStyle;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;


public class MaxReachPro extends Application {

    private static MaxReachPro instance;
    private static StackPane mainPane;
    private static StackPane mapPane;
    private static HBox rootLayout;
    private static StackPane layeredRoot;
    private static Scene scene;
    private static Rectangle topBar;
    private static Rectangle closeRect;
    private static Text closeSymbol;
    private static Rectangle minimizeRect;
    private static Text minimizeSymbol;
    private static Rectangle collapseRect;
    private static Text collapseSymbol;
    private static Rectangle cornerCoverInMiddle1;
    private static Rectangle cornerCoverInMiddle2;
    private static ScissorLift scissorLift;
    private static boolean isFirstScene = true;
    private static Map<String, String> sceneHierarchy = new HashMap<>();
    private static Stage primaryStage = new Stage();
    private static String[] user;
    private static BaseController currentController;
    private static Rental rentalForExpanding;
    private static String currentScenePath;
    private static String sceneBeforeExpandName;
    private static String filterFromActivityScene;
    private static String selectedViewSetting;
    private static String selectedStatusSetting;
    private static LocalDate activityDateSelected1;
    private static LocalDate activityDateSelected2;
    private static String selectedCustomerName;
    private static String selectedDriverName;
    private static ObservableList<Rental> scheduledRentalsList = FXCollections.observableArrayList();
    private static ObservableList<Customer> customers = FXCollections.observableArrayList();
    private static ObservableList<Lift> lifts = FXCollections.observableArrayList();
    private static double xOffset = 0;
    private static double yOffset = 0;
    private static final double SCISSOR_DRAW_HEIGHT = 50;
    private static final double SCISSOR_INITIAL_HEIGHT = Config.SCISSOR_LIFT_INITIAL_HEIGHT;

    @Override
    public void start(Stage stage) throws Exception {
        instance = this;
        this.primaryStage = stage;
        primaryStage.setResizable(false);
        primaryStage.initStyle(StageStyle.TRANSPARENT); // Makes the entire window transparent!

        // Define scene hierarchy
        sceneHierarchy.put("/fxml/home.fxml", "/fxml/login.fxml");
        sceneHierarchy.put("/fxml/smm_tax.fxml", "/fxml/home.fxml");
        sceneHierarchy.put("/fxml/activity.fxml", "/fxml/home.fxml");
        sceneHierarchy.put("/fxml/schedule_delivery.fxml", "/fxml/home.fxml");
        sceneHierarchy.put("/fxml/sync_with_qb.fxml", "/fxml/home.fxml");
        sceneHierarchy.put("/fxml/utilization.fxml", "/fxml/home.fxml");
        sceneHierarchy.put("/fxml/expand_imaginary.fxml", "/fxml/utilization.fxml");

        // Load the initial scene
        // Now includes calls to makeMainPane() and makeMapPane()
        loadScene("/fxml/login.fxml");

        rootLayout = new HBox(mainPane, mapPane);
        rootLayout.setStyle("-fx-background-color: transparent;");
        rootLayout.setHgrow(mapPane, Priority.ALWAYS);

        Rectangle[] derivedRects = makeTopBar();
        topBar = derivedRects[0];

        setupTopBarButtons();

        // Create StackPane to layer dragArea above rootLayout
        layeredRoot = new StackPane(rootLayout, topBar, derivedRects[1], derivedRects[2], 
                                        closeRect, closeSymbol, minimizeRect, minimizeSymbol, 
                                        collapseRect, collapseSymbol);
        layeredRoot.setStyle("-fx-background-color: transparent");
        layeredRoot.setAlignment(Pos.TOP_LEFT);

        // Create and set scene
        scene = new Scene(layeredRoot, Config.WINDOW_WIDTH + (Config.WINDOW_HEIGHT / 2), Config.WINDOW_HEIGHT + 15);
        scene.setFill(Color.TRANSPARENT); // Makes the scene transparent
        scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());


        // Set application title and show
        stage.setTitle("MaxReachPro");
        stage.setScene(scene);
        stage.show();

        // Animate scissor lift transition
        scissorLift.animateTransition(SCISSOR_INITIAL_HEIGHT);
    }

    public void expandStage() {
        System.out.println("Expand stage triggered");

        if (mapPane.getChildren().isEmpty()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/map.fxml"));
                Parent mapContent = loader.load();
                mapPane.getChildren().add(mapContent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    
        // Expand the mapPane
        mapPane.setMinWidth(Config.WINDOW_HEIGHT / 2);
        mapPane.setMaxWidth(Config.WINDOW_HEIGHT / 2);
        HBox.setHgrow(mapPane, Priority.ALWAYS);

        double originalWidth = topBar.getWidth();

        // Calculate the new X position by adding half of the window height from the current position
        double newWidth = originalWidth + (Config.WINDOW_HEIGHT / 2) + 2.5;
        // Assuming closeRect, closeSymbol, minimizeRect, and minimizeSymbol are defined somewhere

        // Define the original and new positions for the translations
        double closeRectOriginalX = closeRect.getLayoutX();
        double closeRectNewX = closeRectOriginalX + newWidth - 35;  // Adjust this to the desired translation value

        double closeSymbolOriginalX = closeSymbol.getLayoutX();
        double closeSymbolNewX = closeSymbolOriginalX + newWidth - 26;  // Same as above

        double minimizeRectOriginalX = minimizeRect.getLayoutX();
        double minimizeRectNewX = minimizeRectOriginalX + newWidth - 69;  // Adjust accordingly

        double minimizeSymbolOriginalX = minimizeSymbol.getLayoutX();
        double minimizeSymbolNewX = minimizeSymbolOriginalX + newWidth - 61;  // Adjust accordingly

        double collapseRectOriginalX = collapseRect.getLayoutX();
        double collapseRectNewX = collapseRectOriginalX + newWidth - 104;

        double collapseSymbolOriginalX = collapseSymbol.getLayoutX();
        double collapseSymbolNewX = collapseSymbolOriginalX + newWidth - 98;

        // Create the Timeline with both width and translation animations
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(topBar.widthProperty(), originalWidth, Interpolator.EASE_BOTH),
                new KeyValue(closeRect.translateXProperty(), 285, Interpolator.EASE_BOTH),
                new KeyValue(closeSymbol.translateXProperty(), 295, Interpolator.EASE_BOTH),
                new KeyValue(minimizeRect.translateXProperty(), 248, Interpolator.EASE_BOTH),
                new KeyValue(minimizeSymbol.translateXProperty(), 256, Interpolator.EASE_BOTH),
                new KeyValue(collapseRect.translateXProperty(), 210, Interpolator.EASE_BOTH),
                new KeyValue(collapseSymbol.translateXProperty(), 216, Interpolator.EASE_BOTH)
            ),
            new KeyFrame(Duration.millis(1075), 
                new KeyValue(topBar.widthProperty(), newWidth, Interpolator.EASE_BOTH),
                new KeyValue(closeRect.translateXProperty(), closeRectNewX, Interpolator.EASE_BOTH),
                new KeyValue(closeSymbol.translateXProperty(), closeSymbolNewX, Interpolator.EASE_BOTH),
                new KeyValue(minimizeRect.translateXProperty(), minimizeRectNewX, Interpolator.EASE_BOTH),
                new KeyValue(minimizeSymbol.translateXProperty(), minimizeSymbolNewX, Interpolator.EASE_BOTH),
                new KeyValue(collapseRect.translateXProperty(), collapseRectNewX, Interpolator.EASE_BOTH),
                new KeyValue(collapseSymbol.translateXProperty(), collapseSymbolNewX, Interpolator.EASE_BOTH)
            )
        );

        timeline.setOnFinished(event -> {
            collapseRect.setVisible(true);
            collapseSymbol.setVisible(true);
        });

        // Play the timeline animation
        timeline.play();


        cornerCoverInMiddle1.setFill(Color.web("#F4F4F4"));
        cornerCoverInMiddle2.setFill(Color.web("#F4F4F4"));

    }

    public static void collapseStage() {
        mapPane.setMinWidth(0);
        mapPane.setMaxWidth(0);
        mapPane.getChildren().removeAll(mapPane.getChildren());

        double originalWidth = topBar.getWidth();

        // Calculate the reduced width for collapse (restore the original width)
        double newWidth = originalWidth - (Config.WINDOW_HEIGHT / 2) - 2.5;
        
        // Create the Timeline with both width and translation animations for collapsing
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(topBar.widthProperty(), originalWidth, Interpolator.EASE_BOTH),
                new KeyValue(closeRect.translateXProperty(), 705, Interpolator.EASE_BOTH),
                new KeyValue(closeSymbol.translateXProperty(), 714, Interpolator.EASE_BOTH),
                new KeyValue(minimizeRect.translateXProperty(), 671, Interpolator.EASE_BOTH),
                new KeyValue(minimizeSymbol.translateXProperty(), 680, Interpolator.EASE_BOTH),
                new KeyValue(collapseRect.translateXProperty(), 637, Interpolator.EASE_BOTH),
                new KeyValue(collapseSymbol.translateXProperty(), 643, Interpolator.EASE_BOTH)
            ),
            new KeyFrame(Duration.millis(1075), 
                new KeyValue(topBar.widthProperty(), newWidth, Interpolator.EASE_BOTH),
                new KeyValue(closeRect.translateXProperty(), 285, Interpolator.EASE_BOTH),
                new KeyValue(closeSymbol.translateXProperty(), 295, Interpolator.EASE_BOTH),
                new KeyValue(minimizeRect.translateXProperty(), 251, Interpolator.EASE_BOTH),
                new KeyValue(minimizeSymbol.translateXProperty(), 259, Interpolator.EASE_BOTH),
                new KeyValue(collapseRect.translateXProperty(), 210, Interpolator.EASE_BOTH),
                new KeyValue(collapseSymbol.translateXProperty(), 216, Interpolator.EASE_BOTH)
            )
        );
    
        collapseRect.setVisible(false);
        collapseSymbol.setVisible(false);
    
        // Play the timeline animation
        timeline.play();
    
        cornerCoverInMiddle1.setFill(Color.TRANSPARENT);  // Optionally hide the cover after collapsing
        cornerCoverInMiddle2.setFill(Color.TRANSPARENT);  // Optionally hide the cover after collapsing

        closeRect.toFront();
    }

    public static void loadScene(String fxmlPath) throws Exception {
        if (currentScenePath != null && fxmlPath.equals("/fxml/expand.fxml")) {
            sceneBeforeExpandName = currentScenePath;
        }
    
        if (mainPane != null) {
            if (mainPane.getChildren().size() > 1) {
                Parent currentRoot = (Parent) mainPane.getChildren().get(1);
                BaseController currentController = (BaseController) currentRoot.getProperties().get("controller");
                if (currentController != null) {
                    currentController.cleanup();
                }
                mainPane.getChildren().remove(1);
            }
        }
    
        FXMLLoader loader = new FXMLLoader(MaxReachPro.class.getResource(fxmlPath));
        Parent newRoot = loader.load();
    
        BaseController newController = loader.getController();
        double newHeight = newController.getTotalHeight();
    
        newRoot.getProperties().put("controller", newController);
        newController.setFXMLPath(fxmlPath);
        currentScenePath = fxmlPath;
        currentController = newController;
        StackPane.setAlignment(newRoot, javafx.geometry.Pos.TOP_LEFT);
    
        if (isFirstScene) {
            MaxReachPro instance = getInstance();
            mainPane = instance.makeMainPane(newRoot);
            mapPane = instance.makeMapPane();
            isFirstScene = false;
        } else {
            scissorLift.animateTransition(newHeight);
        }
    
        if (mainPane.getChildren().contains(newRoot)) {
            mainPane.getChildren().remove(newRoot);
        }
    
        mainPane.getChildren().add(newRoot);
    }
    
    

    public static StackPane makeMainPane(Parent root) {
        mainPane = new StackPane(root);

        double insetMargin = 0;
        Rectangle backgroundRect = new Rectangle(
            Config.WINDOW_WIDTH - 2, 
            Config.WINDOW_HEIGHT + 20
        );
        backgroundRect.setFill(Color.web("#F4F4F4"));
        backgroundRect.setArcWidth(45); // Rounded corners (optional)
        backgroundRect.setArcHeight(45);

        Pane lavenderPane = new Pane(backgroundRect);
        backgroundRect.setTranslateY(-25);
        lavenderPane.setPickOnBounds(false); // Allow clicks to pass through
        
        // Create scissor lift layer
        AnchorPane scissorLiftPane = new AnchorPane();
        scissorLift = new ScissorLift(SCISSOR_DRAW_HEIGHT);
        AnchorPane.setBottomAnchor(scissorLift, 0.0);
        AnchorPane.setLeftAnchor(scissorLift, 0.0);
        AnchorPane.setRightAnchor(scissorLift, 0.0);
        scissorLiftPane.getChildren().add(scissorLift);
        scissorLiftPane.setStyle("-fx-background-color: transparent;");

        // Set scissorLiftPane to not intercept mouse events
        scissorLiftPane.setPickOnBounds(false);

        // Add lavenderPane (which now includes scissorLiftPane and topBar) to the main layout
        lavenderPane.getChildren().add(scissorLiftPane);

        mainPane.getChildren().add(lavenderPane);
        mainPane.setMinWidth(Config.WINDOW_WIDTH);
        mainPane.setMaxWidth(Config.WINDOW_WIDTH);
        mainPane.setStyle("-fx-background-color: transparent;");
        mainPane.setTranslateY(20);

        return mainPane;
    }

    public static StackPane makeMapPane() {
        mapPane = new StackPane();
        mapPane.setMinWidth(30);  // Start hidden
        mapPane.setMaxWidth(30);
        mapPane.setTranslateX(0);
        mapPane.setStyle("-fx-background-color: transparent;");
        
        return mapPane;
    }

    public static Rectangle[] makeTopBar() {
        // 🔹 CREATE GLOBAL DRAG AREA OVER BOTH PANES
        Rectangle dragArea = new Rectangle(Config.WINDOW_WIDTH / 4, Config.WINDOW_HEIGHT);
        dragArea.setWidth(Config.WINDOW_WIDTH - 2);
        dragArea.setHeight(21);
        dragArea.setFill(new LinearGradient(
            0, 0, 1, 0,  // Start (left) to End (right)
            true, CycleMethod.NO_CYCLE, 
            new Stop(0, Color.web(Config.getSecondaryColor())),  // Left color
            new Stop(1, Color.web(Config.getPrimaryColor()))           // Right color
        ));            
        dragArea.setMouseTransparent(false);

        // dragArea.setTranslateX(-Config.WINDOW_WIDTH);
        // dragArea.setLayoutY(0);

        // Round the top two corners only
        dragArea.setArcWidth(20);
        dragArea.setArcHeight(20);

        // 🔹 Enable Dragging (Same across both regions)
        dragArea.setOnMousePressed(event -> {
            System.out.println("Pressed in DragArea");
        });

        dragArea.setOnMouseDragged(event -> {
            System.out.println("Dragging in DragArea");
        });

        dragArea.setOnMousePressed(event -> {
            xOffset = primaryStage.getX() - event.getScreenX();
            yOffset = primaryStage.getY() - event.getScreenY();
        });

        dragArea.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() + xOffset);
            primaryStage.setY(event.getScreenY() + yOffset);
        });

        cornerCoverInMiddle1 = new Rectangle(17, 15);
        cornerCoverInMiddle1.setTranslateX(Config.WINDOW_WIDTH - 21);
        cornerCoverInMiddle1.setTranslateY(Config.WINDOW_HEIGHT + 6);
        cornerCoverInMiddle1.setFill(Color.web("TRANSPARENT"));

        cornerCoverInMiddle2 = new Rectangle(9, 40);
        cornerCoverInMiddle2.setTranslateX(Config.WINDOW_WIDTH - 11);
        cornerCoverInMiddle2.setTranslateY(Config.WINDOW_HEIGHT - 6);
        cornerCoverInMiddle2.setFill(Color.web("TRANSPARENT"));

        return new Rectangle[]{dragArea, cornerCoverInMiddle1, cornerCoverInMiddle2};
    }

    public static void setupTopBarButtons() {
        // Close Button
        closeRect = new Rectangle(30, 15, Color.web("#F4F4F4"));
        closeRect.setArcWidth(40);
        closeRect.setArcHeight(40);
        closeRect.setTranslateX(Config.WINDOW_WIDTH - 35);
        closeRect.setTranslateY(3);
    
        closeSymbol = new Text("X");
        closeSymbol.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        closeSymbol.setFill(Color.web(Config.getPrimaryColor()));
        closeSymbol.setTranslateX(closeRect.getTranslateX() + 10);
        closeSymbol.setTranslateY(closeRect.getTranslateY() - 1);
    
        // Minimize Button (Placed to the left of Close Button)
        minimizeRect = new Rectangle(30, 15, Color.web("#F4F4F4"));
        minimizeRect.setArcWidth(40);
        minimizeRect.setArcHeight(40);
        minimizeRect.setTranslateX(closeRect.getTranslateX() - 35); // Move left by 35 pixels
        minimizeRect.setTranslateY(closeRect.getTranslateY());
    
        minimizeSymbol = new Text("\u2014");
        minimizeSymbol.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        minimizeSymbol.setFill(Color.web(Config.getPrimaryColor()));
        minimizeSymbol.setTranslateX(minimizeRect.getTranslateX() + 8);
        minimizeSymbol.setTranslateY(minimizeRect.getTranslateY() - 2);
    
        // Collapse Button (Placed to the left of Minimize Button)
        collapseRect = new Rectangle(30, 15, Color.web("#F4F4F4"));
        collapseRect.setArcWidth(40);
        collapseRect.setArcHeight(40);
        collapseRect.setTranslateX(minimizeRect.getTranslateX() - 35);
        collapseRect.setTranslateY(closeRect.getTranslateY());
        collapseRect.setVisible(false);
    
        collapseSymbol = new Text("\u2190");
        DropShadow shadow = new DropShadow();
        shadow.setRadius(1); // Remove blur by setting radius to 0
        shadow.setOffsetX(1); // Control horizontal shadow displacement
        shadow.setOffsetY(1); // Control vertical shadow displacement
        shadow.setColor(Color.web(Config.getPrimaryColor())); // Set the shadow color
        collapseSymbol.setEffect(shadow);
        collapseSymbol.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 18));
        collapseSymbol.setFill(Color.web(Config.getPrimaryColor()));
        collapseSymbol.setTranslateX(collapseRect.getTranslateX() + 6);
        collapseSymbol.setTranslateY(collapseRect.getTranslateY() - 4);
        collapseSymbol.setVisible(false);

        // Hover Effects (Both Rectangle & Text)
        EventHandler<MouseEvent> hoverEnter = event -> {
            if (event.getSource() == minimizeRect || event.getSource() == minimizeSymbol) {
                minimizeRect.setFill(Color.web(Config.getTertiaryColor()));
                minimizeSymbol.setFill(Color.web(Config.getSecondaryColor()));
            } else if (event.getSource() == closeRect || event.getSource() == closeSymbol) {
                closeRect.setFill(Color.web(Config.getTertiaryColor()));
                closeSymbol.setFill(Color.web(Config.getSecondaryColor()));
            } else if (event.getSource() == collapseRect || event.getSource() == collapseSymbol) {
                collapseRect.setFill(Color.web(Config.getTertiaryColor()));
                collapseSymbol.setFill(Color.web(Config.getSecondaryColor()));
            }
        };
    
        EventHandler<MouseEvent> hoverExit = event -> {
            if (event.getSource() == minimizeRect || event.getSource() == minimizeSymbol) {
                minimizeRect.setFill(Color.web("#F4F4F4"));
                minimizeSymbol.setFill(Color.web(Config.getPrimaryColor())); // Reset symbol color
            } else if (event.getSource() == closeRect || event.getSource() == closeSymbol) {
                closeRect.setFill(Color.web("#F4F4F4"));
                closeSymbol.setFill(Color.web(Config.getPrimaryColor())); // Reset symbol color
            } else if (event.getSource() == collapseRect || event.getSource() == collapseSymbol) {
                collapseRect.setFill(Color.web("#F4F4F4"));
                collapseSymbol.setFill(Color.web(Config.getPrimaryColor())); // Reset symbol color
            }
        };
    
        // Apply Hover Effects
        minimizeRect.setOnMouseEntered(hoverEnter);
        minimizeRect.setOnMouseExited(hoverExit);
        minimizeSymbol.setOnMouseEntered(hoverEnter);
        minimizeSymbol.setOnMouseExited(hoverExit);
    
        closeRect.setOnMouseEntered(hoverEnter);
        closeRect.setOnMouseExited(hoverExit);
        closeSymbol.setOnMouseEntered(hoverEnter);
        closeSymbol.setOnMouseExited(hoverExit);
    
        collapseRect.setOnMouseEntered(hoverEnter);
        collapseRect.setOnMouseExited(hoverExit);
        collapseSymbol.setOnMouseEntered(hoverEnter);
        collapseSymbol.setOnMouseExited(hoverExit);
    
        // Click Handler for Collapse Button (Prints a message when clicked)
        collapseRect.setOnMouseClicked(event -> {
            collapseStage();
        });
    
        collapseSymbol.setOnMouseClicked(event -> {
            collapseStage();
        });
    
        // Other existing button handlers
        EventHandler<MouseEvent> minimizeHandler = event -> {
            Stage stage = (Stage) topBar.getScene().getWindow();
            stage.setIconified(true);
        };
    
        EventHandler<MouseEvent> closeHandler = event -> {
            Stage stage = (Stage) topBar.getScene().getWindow();
            stage.close();
        };
    
        minimizeRect.setOnMouseClicked(minimizeHandler);
        minimizeSymbol.setOnMouseClicked(minimizeHandler);
    
        closeRect.setOnMouseClicked(closeHandler);
        closeSymbol.setOnMouseClicked(closeHandler);
    }
    


    public static MaxReachPro getInstance() {
        return instance;
    }

    public static BaseController getCurrentController() {
        return currentController;
    }

    public static ScissorLift getScissorLift() {
        return scissorLift;
    }


    public static void setUser(String[] userInfo) {
        user = userInfo;
        System.out.println("User set: " + userInfo[0] + " (" + userInfo[1] + ")");
    }


    public static String[] getUser() {
        if (user == null) {
            return new String[]{"", "", ""};
        }
        return user;
    }

    public static void updateColorCSS(String colorPrimary, String colorSecondary) {
        // If the colors are null, use the default colors from Config
        if (colorPrimary == null) {
            colorPrimary = Config.getPrimaryColor();
        }
    
        if (colorSecondary == null) {
            colorSecondary = Config.getSecondaryColor();
        }
    
        // Ensure the scene is not null before proceeding
        if (scene == null) return;
    
        // Get the previous colors from Config
        String oldPrimaryColor = Config.getPreviousPrimaryColor();
        String oldSecondaryColor = Config.getPreviousSecondaryColor();
    
        // Load the CSS file as a string
        String css = loadCssFile("/styles/styles.css");
    
        // Replace old colors with the new ones
        css = css.replace("orange", colorPrimary)
                .replace("#FFDEAD", colorSecondary);

        // Set up best text colors for the background colors
        int textColorCode = Config.COLOR_TEXT_MAP.getOrDefault(colorPrimary, 0);
        if (textColorCode == 2 || (textColorCode == 0 && checkTooDark(colorPrimary))) {
            css = checkTooDark(colorPrimary) ? css.replace("#000000", "white") : css;
        }
        // Save the updated CSS string for applying
        String updatedCss = css;
    
        // Clear existing stylesheets from the scene
        ObservableList<String> stylesheets = scene.getStylesheets();
        stylesheets.clear();
    
        // Create a temporary CSS file with updated styles (in-memory update)
        File tempCssFile = createTempCssFile(updatedCss);
    
        // Apply the new stylesheet from the temporary CSS file
        scene.getStylesheets().add(tempCssFile.toURI().toString());
    
    }

    public static void updateTopBarColors() {
        if (topBar != null) {
            topBar.setFill(new LinearGradient(
                    0, 0, 1, 0, // Start (left) to End (right)
                    true, CycleMethod.NO_CYCLE, 
                    new Stop(0, Color.web(Config.getSecondaryColor())), // Left color
                    new Stop(1, Color.web(Config.getPrimaryColor()))    // Right color
            ));
        }

        // Update close and minimize symbols with the primary color
        if (closeSymbol != null) {
            closeSymbol.setFill(Color.web(Config.getPrimaryColor()));
        }
        if (minimizeSymbol != null) {
            minimizeSymbol.setFill(Color.web(Config.getPrimaryColor()));
        }    
    }
    
    public static boolean checkTooDark(String colorHex) {
        // Ensure the color starts with '#'
        if (!colorHex.startsWith("#") || colorHex.length() != 7) {
            throw new IllegalArgumentException("Invalid color format. Use #RRGGBB.");
        }

        // Parse RGB values from the hex string
        int red = Integer.parseInt(colorHex.substring(1, 3), 16);
        int green = Integer.parseInt(colorHex.substring(3, 5), 16);
        int blue = Integer.parseInt(colorHex.substring(5, 7), 16);

        // Calculate luminance using the standard formula
        double luminance = (0.299 * red) + (0.587 * green) + (0.114 * blue);

        // Return true if the color is too dark (threshold = 128)
        return luminance < 128;
    }


    public static void setRentalForExpanding(Rental rental, ObservableList<Rental> list) {
        if (list != null && !list.isEmpty()) {
            scheduledRentalsList = list;
        } else {
            scheduledRentalsList = null;
        }        
        rentalForExpanding = rental;
    }


    public static Rental getRentalForExpanding() {
        return rentalForExpanding;
    }


    public static String getSceneBeforeExpandName(){
        System.out.println("Getting the scene before expanding: " + sceneBeforeExpandName);
        return sceneBeforeExpandName;
    }

    public static ObservableList<Rental> getScheduleDeliveryTableViewSet() {
        return scheduledRentalsList;
    }


    public static void setSelectedViewSetting(String view) {selectedViewSetting = view;}


    public static String getSelectedViewSetting() {return selectedViewSetting; }


    public static void setSelectedStatusSetting(String view ) {selectedStatusSetting = view;}


    public static String getSelectedStatusSetting() {return selectedStatusSetting; }


    public static void setSelectedDriverSetting(String driver) {selectedDriverName = driver;}


    public static String getSelectedDriverSetting() {return selectedDriverName;}


    public static void setActivityDateSelected1(LocalDate date) {activityDateSelected1 = date;}


    public static LocalDate getActivityDateSelected1() {return activityDateSelected1; }


    public static void setActivityDateSelected2(LocalDate date) {activityDateSelected2 = date;}


    public static LocalDate getActivityDateSelected2() {return activityDateSelected2; }


    public static void setSelectedCustomerName(String name) {selectedCustomerName = name; }


    public static String getSelectedCustomerName() {return selectedCustomerName;}


    public static void setSelectedDriverName(String name) {selectedDriverName = name; }


    public static String getSelectedDriverName() {return selectedDriverName; }


    public static void loadLifts () {
        String query = "SELECT lift_id, lift_type, serial_number, model, generic FROM lifts";


        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {


            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String liftID = resultSet.getString("lift_id");
                String liftType = resultSet.getString("lift_type");
                String serialNumber = resultSet.getString("serial_number") != null ? resultSet.getString("serial_number") : "";
                String model = resultSet.getString("model");


                // Add to the customer list
                Lift lift = new Lift(liftID, liftType);
                lift.setSerialNumber(serialNumber);
                lift.setModel(model);
                lifts.add(lift);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }


    public static ObservableList<Lift> getLifts () {
        if (lifts.isEmpty()) {
            loadLifts();
        }
        return lifts;
    }


    public static void loadCustomers() {
        String query = "SELECT customer_id, customer_name, email FROM customers";


        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {


            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String customerId = resultSet.getString("customer_id");
                String customer_name = resultSet.getString("customer_name");
                String email = resultSet.getString("email");


                // Add to the customer list
                customers.add(new Customer(customerId, customer_name, email));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }


    public static ObservableList<Customer> getCustomers () {
        if (customers.isEmpty()) {
            loadCustomers();
        }
        return customers;
    }

    // This method loads the CSS file as a string (helper method)
    private static String loadCssFile(String path) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(MaxReachPro.class.getResourceAsStream(path)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    // This method creates a temporary CSS file with the updated CSS content
    private static File createTempCssFile(String cssContent) {
        try {
            // Create a temporary file to hold the updated CSS
            File tempFile = File.createTempFile("styles", ".css");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                writer.write(cssContent);
            }
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static void goBack(String previousScene) throws Exception {
        if (sceneHierarchy.containsKey(previousScene)) {
            String parentScene = sceneHierarchy.get(previousScene);
            loadScene(parentScene);
        } else {
            System.out.println("No parent scene found for " + previousScene);
        }
    }

    public static void goBackFromExpand() throws Exception {
        System.out.println("🔙 goBackFromExpand called. Returning to: " + sceneBeforeExpandName);
        if (sceneBeforeExpandName != null) {
            loadScene(sceneBeforeExpandName);
        } else {
            System.out.println("⚠️ No previous scene stored!");
        }
    }
   

    
}













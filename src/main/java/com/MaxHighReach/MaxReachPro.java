package com.MaxHighReach;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class MaxReachPro extends Application {

    private static StackPane mainLayout;
    private static ScissorLift scissorLift;
    private static boolean isFirstScene = true;
    private static Map<String, String> sceneHierarchy = new HashMap<>();
    private static Stage primaryStage;
    private static String[] user;
    private static CustomerRental rentalForExpanding;
    private static String filterFromActivityScene;
    private static String selectedViewSetting;
    private static String selectedStatusSetting;
    private static LocalDate activityDateSelected1;
    private static LocalDate activityDateSelected2;
    private static String selectedCustomerName;
    private static String selectedDriverName;
    private static ObservableList<Customer> customers = FXCollections.observableArrayList();
    private static ObservableList<Lift> lifts = FXCollections.observableArrayList();

    private static final double SCISSOR_DRAW_HEIGHT = 50;
    private static final double SCISSOR_INITIAL_HEIGHT = Config.SCISSOR_LIFT_INITIAL_HEIGHT;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setResizable(false);
        mainLayout = new StackPane();

        // Add the diagonal shifting gradient background
        StackPane animatedBackground = createDiagonalShiftingGradient();
      //  mainLayout.getChildren().add(animatedBackground);

        // Add the scissor lift layer
        AnchorPane scissorLiftPane = new AnchorPane();
        scissorLift = new ScissorLift(SCISSOR_DRAW_HEIGHT);
        AnchorPane.setBottomAnchor(scissorLift, 0.0);
        AnchorPane.setLeftAnchor(scissorLift, 0.0);
        AnchorPane.setRightAnchor(scissorLift, 0.0);
        scissorLiftPane.getChildren().add(scissorLift);
        mainLayout.getChildren().add(scissorLiftPane);

        // Define scene hierarchy
        sceneHierarchy.put("/fxml/home.fxml", "/fxml/login.fxml");
        sceneHierarchy.put("/fxml/smm_tax.fxml", "/fxml/home.fxml");
        sceneHierarchy.put("/fxml/activity.fxml", "/fxml/home.fxml");
        sceneHierarchy.put("/fxml/schedule_delivery.fxml", "/fxml/home.fxml");
        sceneHierarchy.put("/fxml/sync_with_qb.fxml", "/fxml/home.fxml");
        sceneHierarchy.put("/fxml/expand.fxml", "/fxml/activity.fxml");
        sceneHierarchy.put("/fxml/utilization.fxml", "/fxml/home.fxml");

        // Load the initial scene
        loadScene("/fxml/login.fxml");

        Scene scene = new Scene(mainLayout, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());

        stage.setTitle("MaxReachPro");
        stage.setScene(scene);
        stage.show();

        scissorLift.animateTransition(SCISSOR_INITIAL_HEIGHT);
    }

   private StackPane createDiagonalShiftingGradient() {
        StackPane backgroundPane = new StackPane();

        // Create a Rectangle for the gradient
        Rectangle gradientRect = new Rectangle(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);

        // Define the initial gradient stops
        Stop[] stops = new Stop[]{
                new Stop(0, Color.ORANGE),
                new Stop(0.5, Color.web("#FFDEAD")), // Navajo White
                new Stop(1, Color.HOTPINK)
        };

        // Mutable array for transitioning stops
        double[] offsets = {0.0, 0.5, 1.0}; // Initial offsets

        LinearGradient initialGradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.REPEAT, stops
        );

        gradientRect.setFill(initialGradient);

        // Animation Timeline for smooth transitions
        Timeline gradientAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        // Shift each offset slightly for smooth transition
                        for (int i = 0; i < offsets.length; i++) {
                            offsets[i] += 0.002; // Smoothly increment offset
                            if (offsets[i] > 1.0) offsets[i] -= 1.0; // Wrap around
                        }

                        // Update gradient stops with new offsets
                        Stop[] updatedStops = new Stop[stops.length];
                        for (int i = 0; i < stops.length; i++) {
                            updatedStops[i] = new Stop(offsets[i], stops[i].getColor());
                        }

                        // Apply new gradient
                        LinearGradient updatedGradient = new LinearGradient(
                                0, 0, 1, 1, true, CycleMethod.REPEAT, updatedStops
                        );
                        gradientRect.setFill(updatedGradient);
                    }
                }),
                new KeyFrame(Duration.millis(100)) // Smooth update interval
        );

        gradientAnimation.setCycleCount(Timeline.INDEFINITE);
        gradientAnimation.play();

        backgroundPane.getChildren().add(gradientRect);
        return backgroundPane;
    }


    public static void loadScene(String fxmlPath) throws Exception {

        // Check if the mainLayout has a previous root node
        if (mainLayout.getChildren().size() > 1) {
            Parent currentRoot = (Parent) mainLayout.getChildren().get(1);
            BaseController currentController = (BaseController) currentRoot.getProperties().get("controller");
            if (currentController != null) {
                currentController.cleanup();
            }

        }

        if (mainLayout.getChildren().size() > 1) {
            mainLayout.getChildren().remove(1);
        }

        FXMLLoader loader = new FXMLLoader(MaxReachPro.class.getResource(fxmlPath));
        Parent newRoot = loader.load();
        BaseController newController = loader.getController();
        double newHeight = newController.getTotalHeight();

        newRoot.getProperties().put("controller", newController);
        newController.setFXMLPath(fxmlPath);
        StackPane.setAlignment(newRoot, javafx.geometry.Pos.TOP_LEFT);

        if (isFirstScene) {
            mainLayout.getChildren().add(newRoot);
            isFirstScene = false;
        } else {
            scissorLift.animateTransition(newHeight);
            mainLayout.getChildren().add(newRoot);
        }

        System.out.println("Scene loaded: " + fxmlPath + " | New scene height: " + newHeight);
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

    public static void setRentalForExpanding(CustomerRental rental) {
        rentalForExpanding = rental;
    }

    public static CustomerRental getRentalForExpanding() {
        return rentalForExpanding;
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


    public static void goBack(String previousScene) throws Exception {
        if (sceneHierarchy.containsKey(previousScene)) {
            String parentScene = sceneHierarchy.get(previousScene);
            loadScene(parentScene);
        } else {
            System.out.println("No parent scene found for " + previousScene);
        }
    }
}




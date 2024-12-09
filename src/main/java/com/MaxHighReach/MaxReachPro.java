package com.MaxHighReach;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

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

    private static final double SCISSOR_DRAW_HEIGHT = 50;
    private static final double SCISSOR_INITIAL_HEIGHT = Config.SCISSOR_LIFT_INITIAL_HEIGHT;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

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
        sceneHierarchy.put("/fxml/db.fxml", "/fxml/home.fxml");
        sceneHierarchy.put("/fxml/schedule_delivery.fxml", "/fxml/home.fxml");
        sceneHierarchy.put("/fxml/compose_invoices.fxml", "/fxml/db.fxml");
        sceneHierarchy.put("/fxml/sync_with_qb.fxml", "/fxml/home.fxml");
        sceneHierarchy.put("/fxml/expand.fxml", "/fxml/db.fxml");

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
        double currentHeight = 0;

        // Check if the mainLayout has a previous root node
        if (mainLayout.getChildren().size() > 1) {
            Parent currentRoot = (Parent) mainLayout.getChildren().get(1);
            BaseController currentController = (BaseController) currentRoot.getProperties().get("controller");

            if (currentController != null) {
                currentHeight = currentController.getTotalHeight();
            } else {
                System.out.println("Current controller is null.");
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

    public static void goBack(String previousScene) throws Exception {
        if (sceneHierarchy.containsKey(previousScene)) {
            String parentScene = sceneHierarchy.get(previousScene);
            loadScene(parentScene);
        } else {
            System.out.println("No parent scene found for " + previousScene);
        }
    }
}




package com.MaxHighReach;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class MaxReachPro extends Application {

    private static StackPane mainLayout;
    private static ScissorLift scissorLift;
    private static boolean isFirstScene = true;
    private static Map<String, String> sceneHierarchy = new HashMap<>();
    private static Stage primaryStage;

    private static double SCISSOR_DRAW_HEIGHT = 50;
    private static double SCISSOR_INITIAL_HEIGHT = 350;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        mainLayout = new StackPane();
        AnchorPane scissorLiftPane = new AnchorPane();

        scissorLift = new ScissorLift(SCISSOR_DRAW_HEIGHT);
        AnchorPane.setBottomAnchor(scissorLift, 0.0);
        AnchorPane.setLeftAnchor(scissorLift, 0.0);
        AnchorPane.setRightAnchor(scissorLift, 0.0);

      //  Polygon flourish = createPolygonFlourish();
       // mainLayout.getChildren().add(flourish); // Add to the layout
      //  flourish.setTranslateX(50); // Position it with an X offset
      //  flourish.setTranslateY(50);

        scissorLiftPane.getChildren().add(scissorLift);
        mainLayout.getChildren().add(scissorLiftPane);

        // Define scene hierarchy
        sceneHierarchy.put("/fxml/home.fxml", "/fxml/login.fxml");
        sceneHierarchy.put("/fxml/smm_tax.fxml", "/fxml/home.fxml");
        sceneHierarchy.put("/fxml/db.fxml", "/fxml/home.fxml");
        sceneHierarchy.put("/fxml/schedule_delivery.fxml", "/fxml/home.fxml");
        sceneHierarchy.put("/fxml/create_invoices.fxml", "/fxml/db.fxml");

        loadScene("/fxml/login.fxml");

        Scene scene = new Scene(mainLayout, AppConstants.WINDOW_WIDTH, AppConstants.WINDOW_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());

        stage.setTitle("MaxReachPro");
        stage.setScene(scene);
        stage.show();

        System.out.println("Application started with primary stage size: " + stage.getWidth() + "x" + stage.getHeight());

        scissorLift.animateTransition(SCISSOR_DRAW_HEIGHT, SCISSOR_INITIAL_HEIGHT);
    }

    public static void loadScene(String fxmlPath) throws Exception {
        System.out.println("Loading scene: " + fxmlPath);
        double currentHeight = 0;

        // Reset the scissor lift height if we're returning to the home scene
        if ("/fxml/home.fxml".equals(fxmlPath)) {
            currentHeight = SCISSOR_INITIAL_HEIGHT; // Reset height to initial value
            scissorLift.animateTransition(currentHeight, currentHeight); // Ensure the lift is at initial height
        }

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
            scissorLift.animateTransition(currentHeight, newHeight);
            mainLayout.getChildren().add(newRoot);
        }

        System.out.println("Scene loaded: " + fxmlPath + " | New scene height: " + newHeight);
    }


    public static ScissorLift getScissorLift() {
        return scissorLift;
    }


    public static void goBack(String previousScene) throws Exception {
        if (sceneHierarchy.containsKey(previousScene)) {
            String parentScene = sceneHierarchy.get(previousScene);
            loadScene(parentScene);
        } else {
            System.out.println("No parent scene found for " + previousScene);
        }
    }

    public static double getScissorInitialHeight() {
        return SCISSOR_INITIAL_HEIGHT;
    }
}
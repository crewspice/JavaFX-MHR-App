package com.MaxHighReach;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class HomeController extends BaseController {

    @FXML
    private Button smmTaxButton;

    @FXML
    private Button syncWithQBButton;

    @FXML
    private Button sourceCodeButton;

    @FXML
    private Button backButton;

    @FXML
    private Rectangle dragArea;

    @FXML
    private Label sourceCodeLabel;

    @FXML
    private AnchorPane anchorPane;

    private ScissorLift scissorLift;

    @FXML
    private HBox utilizationBox; 


    private List<Stage> stages = new ArrayList<>();

    private String toggleMap2 = "1";
    private Stage firstStage2 = new Stage();
    private Stage secondStage2 = new Stage();
    private Scene firstScene2;
    private Scene secondScene2;

    @FXML
    public void initialize() {
        System.out.println("checkpoint 1");
        super.initialize(dragArea);
        animateSourceCodeLabel();
        updateUtilizationLabels(utilizationBox);
        // clear these to opt out of saving the most recent activity view settings
        MaxReachPro.setSelectedStatusSetting(null);
        MaxReachPro.setSelectedDriverName(null);
        MaxReachPro.setSelectedCustomerName(null);
        MaxReachPro.setActivityDateSelected2(null);
        MaxReachPro.setActivityDateSelected1(null);
        MaxReachPro.setSelectedViewSetting(null);
    }

    @Override
    public double getTotalHeight() {
        boolean hardCode = true;
        if (hardCode) {
            return 547;
        } else {
            double totalHeight = 0;

            for (Node node : anchorPane.getChildren()) {
                if (node instanceof Region) {
                    totalHeight += ((Region) node).getHeight();
                }
            }
            return totalHeight;
        }
    }

    @FXML
    private void handleSMMTax(ActionEvent event) {
        try {
            MaxReachPro.loadScene("/fxml/smm_tax.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSyncWithQB() {
        try {
            MaxReachPro.loadScene("/fxml/sync_with_qb.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleActivity(ActionEvent event) {
        try {
            MaxReachPro.loadScene("/fxml/activity.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleScheduleDelivery() {
        try {
            MaxReachPro.loadScene("/fxml/schedule_delivery.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUtilization() {
        try {
            MaxReachPro.loadScene("/fxml/utilization.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMap() {
        MaxReachPro.getInstance().expandStage();
    }

    @FXML
    private void handleSourceCodeClick(MouseEvent event) {
        try {
            URI uri = new URI("https://github.com/crewspice/Max-High-Reach/tree/main");
            Desktop.getDesktop().browse(uri);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBack(ActionEvent event) {
        try {
            MaxReachPro.goBack("/fxml/home.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void animateSourceCodeLabel() {
        double startY = sourceCodeLabel.getLayoutY();
        double endY = startY - 53;

        double startRotation = -16;
        double endRotation = -19;

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new javafx.animation.KeyValue(sourceCodeLabel.layoutYProperty(), startY),
                new javafx.animation.KeyValue(sourceCodeLabel.rotateProperty(), startRotation)
            ),
            new KeyFrame(Duration.seconds(1),
                new javafx.animation.KeyValue(sourceCodeLabel.layoutYProperty(), endY),
                new javafx.animation.KeyValue(sourceCodeLabel.rotateProperty(), endRotation)
            )
        );

        timeline.setCycleCount(1);
        timeline.play();
    }

    private int countRowsByStatus(String status) {
        String query = """
            SELECT COUNT(*) AS total
            FROM rental_items ri
            JOIN rental_orders ro ON ri.rental_order_id = ro.rental_order_id
            WHERE ri.item_status = ?
              AND ro.customer_id <> ?
        """;
    
        int count = 0;
    
        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
    
            preparedStatement.setString(1, status);
            preparedStatement.setString(2, Config.A_TEST_CUSTOMER_ID);
    
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    count = resultSet.getInt("total");
                }
            }
    
        } catch (SQLException e) {
            System.err.println("ERROR: SQL exception for status " + status + ": " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    
        return count;
    }
    
    
    
    private void updateUtilizationLabels(Pane statusPane) {
        if (statusPane == null) {
            System.out.println("ERROR: statusPane is null!");
            return;
        }
        statusPane.getChildren().clear();

        int calledOffCount = countRowsByStatus("Called Off");
        int upcomingCount = countRowsByStatus("Upcoming");
        int activeCount = countRowsByStatus("Active");  // <-- new status

        // Create shapes
        Shape calledOffIcon = createRentalDot(null, "Called Off");
        Shape upcomingIcon = createRentalDot(null, "Upcoming");
        Shape activeIcon = createRentalDot(null, "Active");  // <-- new status

        // Position icons
        calledOffIcon.setLayoutX(30);
        calledOffIcon.setLayoutY(20);
        upcomingIcon.setLayoutX(100);
        upcomingIcon.setLayoutY(20);
        activeIcon.setLayoutX(170);  // <-- spaced to the right
        activeIcon.setLayoutY(20);

        // Create labels
        Label calledOffLabel = new Label(String.valueOf(calledOffCount));
        calledOffLabel.setLayoutX(23);
        calledOffLabel.setLayoutY(40);

        Label upcomingLabel = new Label(String.valueOf(upcomingCount));
        upcomingLabel.setLayoutX(93);
        upcomingLabel.setLayoutY(40);

        Label activeLabel = new Label(String.valueOf(activeCount));  // <-- new status
        activeLabel.setLayoutX(163);
        activeLabel.setLayoutY(40);

        // Add to pane
        statusPane.getChildren().addAll(calledOffIcon, calledOffLabel, upcomingIcon, upcomingLabel, activeIcon, activeLabel);
    }

    private Shape createRentalDot(Rental rental, String status) {
        Shape dotShape;
    
        if ("Called Off".equals(status)) {
            // Red octagon
            double radius = 7;
            Polygon octagon = new Polygon();
            for (int i = 0; i < 8; i++) {
                double angle = Math.toRadians(45 * i + 22.5);
                double px = radius * Math.cos(angle);
                double py = radius * Math.sin(angle);
                octagon.getPoints().addAll(px, py);
            }
            dotShape = octagon;
            dotShape.setFill(Color.RED);
    
        } else if ("Upcoming".equals(status)) {
            // Original primary color circle
            Circle circle = new Circle(0, 0, 7);
            circle.setFill(Color.web(Config.getPrimaryColor()));
            dotShape = circle;
    
        } else if ("Active".equals(status)) {
            // New green circle
            Circle circle = new Circle(0, 0, 7);
            circle.setFill(Color.GREEN);
            dotShape = circle;
    
        } else {
            // Default fallback
            Circle circle = new Circle(0, 0, 7);
            circle.setFill(Color.GRAY);
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
    
}
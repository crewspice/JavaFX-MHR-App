package com.MaxHighReach;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.control.ScrollPane;  // Use JavaFX ScrollPane
import javafx.util.Duration;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

public class HomeController extends BaseController {

    @FXML
    private Button smmButton;

    @FXML
    private Button syncWithQBButton;

    @FXML
    private Button sourceCodeButton;

    @FXML
    private Button backButton;

    @FXML
    private Label sourceCodeLabel;

    @FXML
    private AnchorPane anchorPane;

    private ScissorLift scissorLift;

    @FXML
    private Label utilizationLabel;

    @FXML
    public void initialize() {
        animateSourceCodeLabel();
        utilizationLabel.setText(utilizationLabel.getText() + countUniqueLiftIds());

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

    private int countUniqueLiftIds() {
        String query = "SELECT DISTINCT lift_id FROM rental_items WHERE item_status = 'active'";
        int uniqueCount = 0;

        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int liftId = resultSet.getInt("lift_id");
                if (liftId < 1001 || liftId > 1008) {
                    uniqueCount++;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error counting unique lift IDs: " + e.getMessage());
            e.printStackTrace();
            return -1; // Error indicator
        }

        return uniqueCount;
    }





}



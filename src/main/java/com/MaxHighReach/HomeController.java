package com.MaxHighReach;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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
    public void initialize() {

       /* // Initialize and add the ScissorLift to the AnchorPane
        scissorLift = new ScissorLift();
        anchorPane.getChildren().add(scissorLift);

        // Ensure wheel1 is on top and clickable
        scissorLift.getWheel1().toFront();

        // Debug event handling on the AnchorPane
        anchorPane.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            System.out.println("AnchorPane click detected: " + event);
            // Uncomment the following line to consume events at the AnchorPane level:
            // event.consume();
        }); */

        animateSourceCodeLabel();
    }

    @Override
    public double getTotalHeight() {
        boolean hardCode = true;
        if (hardCode) {
            return 500;
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
        System.out.println("SMM Task button clicked");
        try {
            MaxReachPro.loadScene("/fxml/smm_tax.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSyncWithQB() {
        System.out.println("Sync with QB button clicked");
        try {
            MaxReachPro.loadScene("/fxml/sync_with_qb.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDBScene(ActionEvent event) {
        System.out.println("Database button clicked");
        try {
            MaxReachPro.loadScene("/fxml/db.fxml");
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
        System.out.println("Back button clicked on HomeController");
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
}

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
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
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
    private Button smmButton;

    @FXML
    private Button syncWithQBButton;

    @FXML
    private Button sourceCodeButton;

    @FXML
    private Button backButton;

    @FXML
    private Rectangle dragArea2;

    @FXML
    private Label sourceCodeLabel;

    @FXML
    private AnchorPane anchorPane;

    private ScissorLift scissorLift;

    @FXML
    private Label utilizationLabel;

    private List<Stage> stages = new ArrayList<>();

    private String toggleMap2 = "1";
    private Stage firstStage2 = new Stage();
    private Stage secondStage2 = new Stage();
    private Scene firstScene2;
    private Scene secondScene2;

    @FXML
    public void initialize() {
        System.out.println("checkpoint 1");
        super.initialize(dragArea2);
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
    private void handleMap() {
        Platform.runLater(() -> {
            try {
                closeAllWindows(); // Close all other windows

                // Load the FXML for the map scene
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/map.fxml"));
                Parent mapRoot = loader.load();
                Scene mapScene = new Scene(mapRoot, 800, 800);

                // Create a new Stage for the map scene
                Stage mapStage = new Stage();
                mapStage.setScene(mapScene);
                mapStage.initStyle(StageStyle.UNDECORATED);
                mapStage.setAlwaysOnTop(true);

                // Get the primary stage center
                Stage primaryStage = (Stage) anchorPane.getScene().getWindow();
                double centerX = primaryStage.getX() + primaryStage.getWidth() / 2;
                double centerY = primaryStage.getY() + primaryStage.getHeight() / 2;

                // Create a Timeline to animate the width and other properties
                Timeline timeline = new Timeline();
                timeline.setCycleCount(1);  // Only play through once

                // Loop to create and animate stages
                for (int i = 0; i < 100; i++) {
                    final int frame = i;

                    // Keyframe to animate the current stage properties
                    KeyFrame keyFrame = new KeyFrame(Duration.millis(frame * 100),
                            event -> {
                                if (frame > 0) {
                                    // Close the previous stage if not the first frame
                                    Stage previousStage = stages.get(frame - 1);
                                    if (previousStage != null) {
                                        previousStage.close();
                                    }
                                }

                                // Create a new stage with the updated width
                                Stage newStage = new Stage();
                                newStage.setScene(mapScene);
                                newStage.initStyle(StageStyle.UNDECORATED);
                                newStage.setAlwaysOnTop(true);

                                // Set the properties for the new stage
                                newStage.setWidth((frame + 1) * 8); // Increasing width
                                newStage.setHeight(800);
                                newStage.setX(centerX - (frame * 4)); // Adjust position slightly for each frame
                                newStage.setY(centerY - 400);
                                newStage.show();

                                // Store the new stage in the list (to manage future closures)
                                stages.add(newStage);
                            }
                    );
                    timeline.getKeyFrames().add(keyFrame);
                }

                // Start the animation
                timeline.play();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleMap2 () {
        MaxReachPro maxReachPro = new MaxReachPro();
        maxReachPro.expandStage();

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

    // Method to close all open windows
    private void closeAllWindows() {
        if (!stages.isEmpty()) {
            stages.forEach(stage -> {
                if (stage != null && stage.isShowing()) {
                    stage.close(); // Close previous windows if they're still open
                }
            });
            stages.clear(); // Clear the list of stages to start fresh
        }
    }

    // Method to open a new window with specific FXML
    private void openWindow(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

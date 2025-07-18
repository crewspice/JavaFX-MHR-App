package com.MaxHighReach;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public abstract class BaseController {

    @FXML
    protected AnchorPane anchorPane;

    @FXML
    protected Button backButton;

    private double xOffset = 0;
    private double yOffset = 0;

    private String fxmlPath;
    private String parentFxmlPath;

    private Text minimizeSymbol;
    private Text closeSymbol;
    private Rectangle dragArea;

    @FXML
    public void initialize(Rectangle dragArea) {
        this.dragArea = dragArea;
        System.out.println("Initializing BaseController...");
    
        if (backButton != null) {
            backButton.setOnAction(getBackHandler());
        }
        
    }
    

    @FXML
    public abstract double getTotalHeight();

    public void setFXMLPath(String fxmlPath) {
        this.fxmlPath = fxmlPath;
    }

    public String getFXMLPath() {
        return fxmlPath;
    }

    public void setParentFxmlPath(String parentFxmlPath) {
        this.parentFxmlPath = parentFxmlPath;
    }

    public String getParentFxmlPath() {
        return parentFxmlPath;
    }

    public void cleanup() {
        if (anchorPane != null) {
            clearNode(anchorPane);
            anchorPane.getChildren().clear();
        }
        customCleanup();
    }

    private void clearNode(Node node) {
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                clearNode(child);
            }
        }
        if (node instanceof Control control) {
            // can set more cleanup down this avenue
        }
    }

    protected void customCleanup() {
        // By default, do nothin. Each controller can override as needed
    }

    @FXML
    protected void handleClose(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void handleMinimize(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    protected void handlePressMin(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        System.out.println("Minimize mouse pressed");
    }

    protected EventHandler<ActionEvent> getBackHandler() {
        return this::handleBack;
    }    

    @FXML
    protected void handleBack(ActionEvent event) {
        try {
            MaxReachPro.goBack(fxmlPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void expandTopBar() {
        if (dragArea != null) {
            // Get the current X position of the drag area
            double originalWidth = dragArea.getWidth();

            // Calculate the new X position by adding half of the window height from the current position
            double newWidth = originalWidth + (Config.WINDOW_HEIGHT / 2);

            Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(dragArea.widthProperty(), originalWidth, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(400), 
                new KeyValue(dragArea.widthProperty(), newWidth, Interpolator.EASE_BOTH))
            );

            timeline.play();
        } else {
            System.out.println("Drag area is null, cannot expand top bar.");
        }
    }

    public ObservableList<Rental> getActiveRentalList() {
        return null;  // default implementation
    }

}
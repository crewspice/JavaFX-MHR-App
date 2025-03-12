package com.MaxHighReach;

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
            backButton.setOnAction(this::handleBack);
        }
    
        if (dragArea != null) {
            System.out.println("Drag area2 found, setting up listeners...");
    
            dragArea.setWidth(Config.WINDOW_WIDTH - 2);
            dragArea.setHeight(21);
            dragArea.setFill(new LinearGradient(
                0, 0, 1, 0,  // Start (left) to End (right)
                true, CycleMethod.NO_CYCLE, 
                new Stop(0, Color.web(Config.getSecondaryColor())),  // Left color
                new Stop(1, Color.web(Config.getPrimaryColor()))           // Right color
            ));            
            dragArea.setMouseTransparent(false);
    
            // Round the top two corners only
            dragArea.setArcWidth(20);
            dragArea.setArcHeight(20);
    
            // Close Button
            Rectangle closeRect = new Rectangle(30, 15, Color.web("#F4F4F4"));
            closeRect.setArcWidth(40);
            closeRect.setArcHeight(40);
            closeRect.setTranslateX(Config.WINDOW_WIDTH - 35);
            closeRect.setTranslateY(-17);
    
            closeSymbol = new Text("X");
            closeSymbol.setFont(Font.font("Arial", FontWeight.BOLD, 15));
            closeSymbol.setFill(Color.web(Config.getPrimaryColor()));
            closeSymbol.setTranslateX(closeRect.getTranslateX() + 10);
            closeSymbol.setTranslateY(closeRect.getTranslateY() + 13);
    
            // Minimize Button (Placed to the left of Close Button)
            Rectangle minimizeRect = new Rectangle(30, 15, Color.web("#F4F4F4"));
            minimizeRect.setArcWidth(40);
            minimizeRect.setArcHeight(40);
            minimizeRect.setTranslateX(closeRect.getTranslateX() - 35); // Move left by 35 pixels
            minimizeRect.setTranslateY(closeRect.getTranslateY());
    
            minimizeSymbol = new Text("—");
            minimizeSymbol.setFont(Font.font("Arial", FontWeight.BOLD, 15));
            minimizeSymbol.setFill(Color.web(Config.getPrimaryColor()));
            minimizeSymbol.setTranslateX(minimizeRect.getTranslateX() + 8);
            minimizeSymbol.setTranslateY(minimizeRect.getTranslateY() + 11);
    
            // Click handlers
            EventHandler<MouseEvent> minimizeHandler = event -> {
                Stage stage = (Stage) dragArea.getScene().getWindow();
                stage.setIconified(true);
            };
    
            EventHandler<MouseEvent> closeHandler = event -> {
                Stage stage = (Stage) dragArea.getScene().getWindow();
                stage.close();
            };
    
            minimizeRect.setOnMouseClicked(minimizeHandler);
            minimizeSymbol.setOnMouseClicked(minimizeHandler);
    
            closeRect.setOnMouseClicked(closeHandler);
            closeSymbol.setOnMouseClicked(closeHandler);
    
            // Hover Effects (Both Rectangle & Text)
            EventHandler<MouseEvent> hoverEnter = event -> {
                if (event.getSource() == minimizeRect || event.getSource() == minimizeSymbol) {
                    minimizeRect.setFill(Color.BLACK);
                    minimizeSymbol.setFill(Color.web(Config.getSecondaryColor()));
                } else if (event.getSource() == closeRect || event.getSource() == closeSymbol) {
                    closeRect.setFill(Color.BLACK);
                    closeSymbol.setFill(Color.web(Config.getSecondaryColor()));
                }
            };

            EventHandler<MouseEvent> hoverExit = event -> {
                if (event.getSource() == minimizeRect || event.getSource() == minimizeSymbol) {
                    minimizeRect.setFill(Color.web("#F4F4F4"));
                    minimizeSymbol.setFill(Color.web(Config.getPrimaryColor())); // Reset symbol color
                } else if (event.getSource() == closeRect || event.getSource() == closeSymbol) {
                    closeRect.setFill(Color.web("#F4F4F4"));
                    closeSymbol.setFill(Color.web(Config.getPrimaryColor())); // Reset symbol color
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

    
            // Dragging functionality
            dragArea.setOnMousePressed(event -> {
                Stage stage = (Stage) dragArea.getScene().getWindow();
                xOffset = stage.getX() - event.getScreenX();
                yOffset = stage.getY() - event.getScreenY();
            });
    
            dragArea.setOnMouseDragged(event -> {
                Stage stage = (Stage) dragArea.getScene().getWindow();
                stage.setX(event.getScreenX() + xOffset);
                stage.setY(event.getScreenY() + yOffset);
            });
    
            dragArea.setTranslateY(-20);
            dragArea.toFront();
    
            // Add buttons and symbols to the scene
            ((Pane) dragArea.getParent()).getChildren().addAll(minimizeRect, closeRect, minimizeSymbol, closeSymbol);
        } else {
            System.out.println("Drag area2 not found!");
        }
    }
    
    // Method to refresh UI elements with the current primary and secondary colors
    public void refreshUIElements() {
        if (dragArea != null) {
            dragArea.setFill(new LinearGradient(
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

    @FXML
    protected void handleBack(ActionEvent event) {
        try {
            MaxReachPro.goBack(fxmlPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
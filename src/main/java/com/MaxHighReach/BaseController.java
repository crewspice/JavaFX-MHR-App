package com.MaxHighReach;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public abstract class BaseController {

    @FXML
    protected AnchorPane anchorPane;

    @FXML
    protected Button backButton;

    @FXML
    protected Button closeButton;

    @FXML
    protected Button minimizeButton;

    // @FXML
    // protected Rectangle dragArea; // The draggable area for moving the window

    private double xOffset = 0;
    private double yOffset = 0;

    private String fxmlPath;
    private String parentFxmlPath;

    @FXML
    public void initialize(Rectangle dragArea2) {
        System.out.println("Initializing BaseController...");

        if (backButton != null) {
            backButton.setOnAction(this::handleBack);
        }

        // if (dragArea != null) {
        //     System.out.println("Drag area found, setting up listeners...");
            
        //     dragArea.setOnMousePressed(event -> {
        //         Stage stage = (Stage) dragArea.getScene().getWindow();
        //         xOffset = stage.getX() - event.getScreenX();
        //         yOffset = stage.getY() - event.getScreenY();
        //     });

        //     dragArea.setOnMouseDragged(event -> {
        //         Stage stage = (Stage) dragArea.getScene().getWindow();
        //         stage.setX(event.getScreenX() + xOffset);
        //         stage.setY(event.getScreenY() + yOffset);
        //     });

        //     dragArea.toFront();
        // } else {
        //     System.out.println("Drag area not found!");
        // }

        if (dragArea2 != null) {
            System.out.println("Drag area2 found, setting up listeners...");
            
            dragArea2.setOnMousePressed(event -> {
                Stage stage = (Stage) dragArea2.getScene().getWindow();
                xOffset = stage.getX() - event.getScreenX();
                yOffset = stage.getY() - event.getScreenY();
            });

            dragArea2.setOnMouseDragged(event -> {
                Stage stage = (Stage) dragArea2.getScene().getWindow();
                stage.setX(event.getScreenX() + xOffset);
                stage.setY(event.getScreenY() + yOffset);
            });

            dragArea2.toFront();
        } else {
            System.out.println("Drag area2 not found!");
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

    // @FXML
    // protected void handleMousePressed(MouseEvent event) {
    //     Stage stage = (Stage) dragArea.getScene().getWindow();
    //     xOffset = stage.getX() - event.getScreenX();
    //     yOffset = stage.getY() - event.getScreenY();
    // }

    // @FXML
    // private void handleMouseDragged(MouseEvent event) {
    //     Stage stage = (Stage) dragArea.getScene().getWindow();
    //     stage.setX(event.getScreenX() + xOffset);
    //     stage.setY(event.getScreenY() + yOffset);
    // }

    @FXML
    protected void handleBack(ActionEvent event) {
        try {
            MaxReachPro.goBack(fxmlPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
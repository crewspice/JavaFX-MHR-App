package com.MaxHighReach;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.layout.AnchorPane;

public abstract class BaseController {

    @FXML
    protected AnchorPane anchorPane;

    @FXML
    protected Button backButton;

    private String fxmlPath;
    private String parentFxmlPath;

    @FXML
    public void initialize() {
        if (backButton != null) {
            backButton.setOnAction(this::handleBack);
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
    protected void handleBack(ActionEvent event) {
        try {
            MaxReachPro.goBack(fxmlPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
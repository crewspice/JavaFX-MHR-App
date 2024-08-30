package com.MaxHighReach;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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

    @FXML
    protected void handleBack(ActionEvent event) {
        try {
            MaxReachPro.goBack(fxmlPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
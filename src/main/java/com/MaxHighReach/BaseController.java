package com.MaxHighReach;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

import javafx.event.ActionEvent;

public abstract class BaseController {

    @FXML
    protected AnchorPane anchorPane;
    private String fxmlPath;
    private String parentFxmlPath;

    @FXML
    public abstract double getTotalHeight();

    public void setFXMLPath(String fxmlPath) {
        this.fxmlPath = fxmlPath;
    }

    @FXML
    public abstract void handleBack(ActionEvent event);

    public String getFXMLPath() {
        return fxmlPath;
    }

    public void setParentFxmlPath(String parentFxmlPath) {
        this.parentFxmlPath = parentFxmlPath;
    }

    public String getParentFxmlPath() {
        return parentFxmlPath;
    }
}

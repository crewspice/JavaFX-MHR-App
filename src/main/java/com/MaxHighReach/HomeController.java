package com.MaxHighReach;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;

public class HomeController extends BaseController {

    @FXML
    private Button smmButton;

    @FXML
    private Button backButton;

    @FXML
    public void initialize() {
        System.out.println("HomeController initialized");
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
    public void handleBack(ActionEvent event) {
        System.out.println("Back button clicked on HomeController");
        try {
            MaxReachPro.goBack("/fxml/home.fxml"); // Use the goBack method for back navigation
        } catch (Exception e) {
            e.printStackTrace(); // Make sure to handle exceptions properly
        }
    }

}

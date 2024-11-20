package com.MaxHighReach;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LoginController extends BaseController {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TextField nameField;

    @FXML
    private Button loginButton;

    @FXML
    private ListView<String> suggestionsList;

    private static final List<String> EMPLOYEE_NAMES = Arrays.asList(
        "Ken Mulberry", "Sandy Mulberry", "Byron Chilton", "Jackson Cline",
        "John Wright", "Isaiah Sabala", "Kaleb Streit", "Adrian Barraza"
    );

    @FXML
    public void initialize() {
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateSuggestions(newValue);
        });

        // Handle Tab key to cycle through suggestions
        suggestionsList.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                selectSuggestion();
            } else if (event.getCode() == KeyCode.TAB) {
                handleTabKey(event);
            }
        });

        suggestionsList.setOnMouseClicked(event -> {
            selectSuggestion();
        });

        // Focus event handler for the name field to ensure it behaves correctly
        nameField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused && suggestionsList.isVisible()) {
                suggestionsList.requestFocus();
            }
        });
    }

    @Override
    public double getTotalHeight() {
        return Config.SCISSOR_LIFT_INITIAL_HEIGHT;
    }

    private void handleTabKey(KeyEvent event) {
        if (suggestionsList.isVisible()) {
            int index = suggestionsList.getSelectionModel().getSelectedIndex();
            if (index == -1) {
                index = 0;
            } else {
                index = (index + 1) % suggestionsList.getItems().size();
            }
            suggestionsList.getSelectionModel().select(index);
            suggestionsList.scrollTo(index);
            event.consume();  // Consume the event to prevent default tabbing behavior
        }
    }

    private void updateSuggestions(String query) {
        suggestionsList.getItems().clear();

        if (query != null && !query.isEmpty()) {
            List<String> filteredNames = EMPLOYEE_NAMES.stream()
                .filter(name -> name.toLowerCase().startsWith(query.toLowerCase()) ||
                                name.split(" ")[1].toLowerCase().startsWith(query.toLowerCase()))
                .collect(Collectors.toList());

            if (!filteredNames.isEmpty()) {
                suggestionsList.getItems().addAll(filteredNames);
                suggestionsList.setVisible(true);
                suggestionsList.setPrefHeight(Math.min(filteredNames.size() * 24, 240));
                nameField.requestFocus();
            } else {
                suggestionsList.setVisible(false);
            }
        } else {
            suggestionsList.setVisible(false);
        }
    }

    private void selectSuggestion() {
        String selectedName = suggestionsList.getSelectionModel().getSelectedItem();
        if (selectedName != null) {
            nameField.setText(selectedName);
            suggestionsList.setVisible(false);
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String enteredName = nameField.getText().trim();  // Get and trim the input

        // Set user if a match is found in the array
        for (String[] employee : Config.EMPLOYEES) {
            if (employee[0].equals(enteredName)) {
                MaxReachPro.setUser(employee);  // Set the user if a match is found
                break;  // Exit the loop once the user is set
            }
        }

        try {
            MaxReachPro.loadScene("/fxml/home.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    @Override
    protected void handleBack(ActionEvent event) {
        // Do nothing for LoginController
    }
}

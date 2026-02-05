package com.MaxHighReach;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.Group;

import java.awt.Desktop.Action;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LoginController extends BaseController {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TextField nameField;

    @FXML
    private Button loginButton;

    @FXML
    private Rectangle dragArea;

    @FXML
    private ListView<String> suggestionsList;

    @FXML
    private Button paintbrushButton;

    @FXML
    public void initialize() {
        super.initialize(dragArea);
        System.out.println("Controller class: " + this.getClass());

        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateSuggestions(newValue);
            Config.setUser(null);
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

        setupPaintbrushButton();
        new Thread(() -> MaxReachPro.loadCustomers(false)).start();

    }
    
    private void drawColorSettingElements() {
        // Hexagon properties
        double hexSize = 6; // Radius of hexagon
        double centerX = -6;
        double centerY = 161;
        double hexSpacing = hexSize * 1.8; // Adjust for even spacing

        // Define a list of the indices that need the 600 Y-offset
        Set<Integer> yOffsetIndices = new HashSet<>(Arrays.asList(
            3, 4, 5, 6, 9, 10, 11, 12, 13, 16, 17, 20, 21,
            22, 23, 27, 28, 30, 31, 34, 35, 37, 38, 39, 40, 41,
            45, 46, 47, 48
        ));
        List<Integer> yOffsetIndicesList = new ArrayList<>(yOffsetIndices);

        for (int i = 1; i <= 30; i++) {
            String color = Config.ALL_COLORS.get(i + 29);
            int colorIndex = yOffsetIndicesList.get(i - 1);

            
            // Determine the X and Y positions based on the index
            double offsetX = (colorIndex % 7) * hexSpacing;  // Positioning horizontally
            double offsetY = (colorIndex / 7) * (hexSize * 1.5); // Positioning vertically

            if (colorIndex == 21 || colorIndex == 28 || colorIndex == 35) {
                offsetY -= (hexSize * 1.5);
                offsetX += 75;
            }


            // Create hexagon for each color with the adjusted Y offset
            Polygon hex = createHexagon(centerX + offsetX, centerY + offsetY, hexSize, color, colorIndex);
            int finalIndex = colorIndex;  // Capture the current index for lambda
            hex.setOnMouseClicked(e -> {
                Config.setPrimaryColor(color);
                MaxReachPro.getInstance().updateColorCSS(color, null);
                MaxReachPro.getInstance().updateTopBarColors();
                MaxReachPro.getInstance().getScissorLift().updateColors(Color.web(Config.getPrimaryColor()), Color.web(Config.getSecondaryColor()));
                String[] user = Config.getUser();
                if (user != null) {
                    updateColorInDB(Config.getUser()[0], Config.getPrimaryColor());
                }
                /*  System.out.println("colorIndex: " + colorIndex + 
                    " -> offsetX: (" + colorIndex + " % 7) * " + hexSpacing + " = " + offsetX + 
                    ", offsetY: (" + colorIndex + " / 7) * (" + hexSize + " * 1.5) = " + offsetY);
                */
            });

            // Add hexagon to the AnchorPane
            anchorPane.getChildren().add(hex);
            
        }
    }

    private void drawColorSettingElementsForSecondWheel() {
        // Hexagon properties
        double hexSize = 6; // Radius of hexagon
        double centerX = 229;
        double centerY = 161;
        double hexSpacing = hexSize * 1.8; // Adjust for even spacing

        // Define a list of the indices that need the 600 Y-offset
        Set<Integer> yOffsetIndices = new HashSet<>(Arrays.asList(
            3, 4, 5, 6, 9, 10, 11, 12, 13, 16, 17, 20, 21,
            22, 23, 27, 28, 30, 31, 34, 35, 37, 38, 39, 40, 41,
            45, 46, 47, 48
        ));
        List<Integer> yOffsetIndicesList = new ArrayList<>(yOffsetIndices);


        for (int i = 1; i <= 30; i++) {
            String color = Config.ALL_COLORS.get(i - 1);
            int colorIndex = yOffsetIndicesList.get(i - 1);
            
            // Determine the X and Y positions based on the index
            double offsetX = (colorIndex % 7) * hexSpacing;  // Positioning horizontally
            double offsetY = (colorIndex / 7) * (hexSize * 1.5); // Positioning vertically

            if (colorIndex == 21 || colorIndex == 28 || colorIndex == 35) {
                offsetY -= (hexSize * 1.5);
                offsetX += 75;
            }


            // Create hexagon for each color with the adjusted Y offset
            Polygon hex = createHexagon(centerX + offsetX, centerY + offsetY, hexSize, color, colorIndex);
            int finalIndex = colorIndex;  // Capture the current index for lambda
            hex.setOnMouseClicked(e -> {
                Config.setSecondaryColor(color);
                MaxReachPro.getInstance().updateColorCSS(null, color);
                MaxReachPro.getInstance().updateTopBarColors();
                MaxReachPro.getInstance().getScissorLift().updateColors(Color.web(Config.getPrimaryColor()), Color.web(Config.getSecondaryColor()));
                /*  System.out.println("colorIndex: " + colorIndex + 
                    " -> offsetX: (" + colorIndex + " % 7) * " + hexSpacing + " = " + offsetX + 
                    ", offsetY: (" + colorIndex + " / 7) * (" + hexSize + " * 1.5) = " + offsetY);
                */
            });

            // Add hexagon to the AnchorPane
            anchorPane.getChildren().add(hex);
            
        }
    }


    

    /**
     * Generates hexagonal tiles in a circular layout.
     * Uses axial coordinates (q, r) for the tiles.
     */
    private List<Hex> generateHexTiles(int radius) {
        List<Hex> hexes = new ArrayList<>();
        for (int q = -radius; q <= radius; q++) {
            for (int r = Math.max(-radius, -q - radius); r <= Math.min(radius, -q + radius); r++) {
                hexes.add(new Hex(q, r));
            }
        }
        return hexes;
    }

    /**
     * Assigns a color based on the hex position (could be modified as needed).
     */
    private String getColorForHex(Hex hex) {
        // Simple color assignment based on position, can be expanded
        if (hex.q % 2 == 0 && hex.r % 2 == 0) {
            return Config.ORANGE;
        } else if (hex.q % 2 != 0 && hex.r % 2 == 0) {
            return Config.SKY_BLUE;
        } else {
            return Config.LIME_GREEN;
        }
    }

    /**
     * Creates a hexagon shape centered at (x, y).
     */
    private Polygon createHexagon(double x, double y, double size, String color, int colorIndex) {
        double angle = Math.PI / 3; // 60 degrees (rad) for each side of the hexagon
        Polygon hex = new Polygon();
        
        // Adjust the angle to make the hexagon point upward
        double offsetAngle = Math.PI / 6; // Rotate by 30 degrees (offset for upward point)
    
        // Formalize the offset for alternating sequences of 6 colors
        if ((colorIndex >= 8 && colorIndex <= 14) || (colorIndex >= 22 && colorIndex <= 28) ||
            (colorIndex >= 36 && colorIndex <= 42) || (colorIndex >= 50 && colorIndex <= 56)) {
            x += (size / 2) + 3; // Apply offset of size/2 + 8 for these ranges
        }

        // Apply y offset of 200 for specified indices
        if (colorIndex == 3 || colorIndex == 4 || colorIndex == 5 || colorIndex == 6 ||
            colorIndex == 9 || colorIndex == 10 || colorIndex == 11 || colorIndex == 12 || colorIndex == 13 ||
            colorIndex == 16 || colorIndex == 17 || colorIndex == 20 || colorIndex == 21 ||
            colorIndex == 22 || colorIndex == 23 || colorIndex == 27 || colorIndex == 28 ||
            colorIndex == 30 || colorIndex == 31 || colorIndex == 34 || colorIndex == 35 ||
            colorIndex == 38 || colorIndex == 39 || colorIndex == 40 || colorIndex == 41 || colorIndex == 37 ||
            colorIndex == 45 || colorIndex == 46 || colorIndex == 47 || colorIndex == 48) {
            y += 600; // Apply y offset of 200 for these indices
        }
    
        for (int i = 0; i < 6; i++) {
            double px = x + size * Math.cos(i * angle + offsetAngle);
            double py = y + size * Math.sin(i * angle + offsetAngle);
            
            hex.getPoints().addAll(px, py);
        }
    
        hex.setFill(Color.web(color));
        hex.setStroke(Color.BLACK); // Optional: add a border for clarity
        return hex;
    }
     

    // Inner Hex class to hold coordinate data (q, r)
    static class Hex {
        int q, r, s;
        public Hex(int q, int r) {
            this.q = q;
            this.r = r;
            this.s = -q - r; // Calculates s automatically
        }
    }

    private void updateColorInDB(String userName, String color) {
        color = color.replaceFirst("^#", "");
        System.out.println("Starting updateColorInDB method: Updating color for user '" + userName + "' to '" + color + "' in the database.");
    
        String query = "UPDATE users SET color = ? WHERE name = ?";  // Adjust table name if needed
    
        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
    
            // Debugging prints
            System.out.println("Color: " + color + ", User: " + userName);
    
            // Set color and user as parameters for the query
            preparedStatement.setString(1, color);
            preparedStatement.setString(2, userName);
    
            int rowsUpdated = preparedStatement.executeUpdate();
            System.out.println("Rows updated: " + rowsUpdated); // Check if rows are affected
    
            if (rowsUpdated > 0) {
                System.out.println("User color updated successfully.");
            } else {
                System.out.println("No user found with the name '" + userName + "' or color is already set.");
            }
        } catch (SQLException e) {
            System.out.println("SQL error while updating color for user: " + userName);
            e.printStackTrace();
        }
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
            List<String> filteredNames = Arrays.stream(Config.EMPLOYEES)  // Stream the 2D array
            .map(entry -> entry[0])  // Get the name (first element of each sub-array)
            .filter(name -> name.toLowerCase().startsWith(query.toLowerCase()) ||
                            name.split(" ")[1].toLowerCase().startsWith(query.toLowerCase()))
            .collect(Collectors.toList());  // Collect to a list

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
            Config.setUser(selectedName);
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        System.out.println(
            MaxReachPro.class.getResource("/fxml/home.fxml")
        );
        
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

    private void setupPaintbrushButton() {
        ImageView paintbrushIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/paintbrush.png")));
        
        paintbrushIcon.setFitWidth(20);
        paintbrushIcon.setFitHeight(20);

        paintbrushButton.setGraphic(paintbrushIcon);
        paintbrushButton.setLayoutX(14);
        paintbrushButton.setLayoutY(550);

        paintbrushButton.setOnAction(null);
        paintbrushButton.setOnAction(event -> {
            drawColorSettingElements();
            drawColorSettingElementsForSecondWheel();
            paintbrushButton.setVisible(false);
        });
    }

    @FXML
    private void handlePaintbrush(ActionEvent event) {

    }

    @FXML
    @Override
    protected void handleBack(ActionEvent event) {
        // Do nothing for LoginController
    }
}

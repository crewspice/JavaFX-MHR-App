package com.MaxHighReach;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

import static java.lang.Integer.parseInt;

public class DBColumnFactory {

    private TableColumn<CustomerRental, String> customerAndAddressColumn = new TableColumn<>();
    private TableColumn<CustomerRental, Boolean> statusColumn = new TableColumn<>();
    private TableColumn<CustomerRental, String> serialNumberColumn = new TableColumn<>();
    private TableColumn<CustomerRental, String> deliveryDateColumn = new TableColumn<>();
    private TableColumn<CustomerRental, String> deliveryTimeColumn = new TableColumn<>();
    private TableColumn<CustomerRental, String> driverColumn = new TableColumn<>();
    private Button updateRentalButton;
    private String lastActionType = "";
    private TableView<CustomerRental> dbTableView;
    private Map<String, List<CustomerRental>> groupedRentals = new HashMap<>();
    private Map<String, Integer> driverSequenceMap = new HashMap<>();
    private boolean shouldShowCheckboxes = false;
    private ObservableList<String> driverInitials = FXCollections.observableArrayList("A", "J", "I", "B", "JC", "K");
    private String driverComboBoxOpenOrClosed = "";
    private Label globalLiftTypeLabel;


    public DBColumnFactory(Button button, TableView<CustomerRental> tableView, Map<String, List<CustomerRental>> rentalsMap, Map<String, Integer> driverMap){
        updateRentalButton = button;
        dbTableView = tableView;
        groupedRentals = rentalsMap;
        driverSequenceMap = driverMap;


        customerAndAddressColumn.setCellFactory(column -> new TableCell<CustomerRental, String>() {
            private final Label nameLabel = new Label();       // Customer name
            private final Label addressBlockOneLabel = new Label();  // Address block one
            private final Label addressBlockTwoLabel = new Label();  // Address block two
            private final Label addressBlockThreeLabel = new Label();  // Address block three
            private final VBox contentVBox = new VBox(nameLabel, addressBlockOneLabel, addressBlockTwoLabel, addressBlockThreeLabel); // Contains all address lines
            private final Label liftTypeLabel = new Label();   // Lift type label with shadow
            private final StackPane overlayPane = new StackPane(contentVBox, liftTypeLabel); // Overlay for layout
            private final DropShadow glowEffect = new DropShadow(); // Glow effect for lift type

            {
                // Configure the VBox for customer name and address lines
                contentVBox.setAlignment(Pos.TOP_LEFT);
                contentVBox.setPadding(new Insets(0)); // Optional padding for better spacing
                contentVBox.setSpacing(-2); // Space between address lines
                contentVBox.setFillWidth(true); // Ensures labels take up full width of VBox

                // Configure each label in the VBox
                nameLabel.setStyle("-fx-font-weight: bold;");
                addressBlockOneLabel.setStyle("-fx-font-weight: normal;");
                addressBlockTwoLabel.setStyle("-fx-font-weight: normal;");
                addressBlockThreeLabel.setStyle("-fx-font-weight: normal;");

                // Apply fixed height matching the row height for consistent layout
                contentVBox.setMinHeight(AppConstants.DB_ROW_HEIGHT);
                contentVBox.setMaxHeight(AppConstants.DB_ROW_HEIGHT);

                // Configure the liftTypeLabel with drop shadow and style
                glowEffect.setRadius(10);
                glowEffect.setSpread(0.5);
                liftTypeLabel.setEffect(glowEffect);
                liftTypeLabel.getStyleClass().add("lift-type-in-corner");
                liftTypeLabel.setStyle("-fx-font-weight: bold;");
                liftTypeLabel.setFont(Font.font("Patrick Hand"));

                globalLiftTypeLabel = liftTypeLabel;


                // Set DropShadow animation for liftTypeLabel
                Timeline glowTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO,
                        new KeyValue(glowEffect.radiusProperty(), 10),
                        new KeyValue(glowEffect.colorProperty(), Color.web("#FFDEAD", 0.5))
                    ),
                    new KeyFrame(Duration.seconds(2),
                        new KeyValue(glowEffect.radiusProperty(), 10),
                        new KeyValue(glowEffect.colorProperty(), Color.web("#FF7F00", 1.0))
                    )
                );
                glowTimeline.setCycleCount(Timeline.INDEFINITE);
                glowTimeline.setAutoReverse(true);
                glowTimeline.play();

                // Align liftTypeLabel to the bottom-right corner
                StackPane.setAlignment(liftTypeLabel, Pos.BOTTOM_RIGHT);
                StackPane.setAlignment(contentVBox, Pos.CENTER_LEFT);
                StackPane.setMargin(liftTypeLabel, new Insets(0, 5, 8, 0)); // Adjust padding if necessary
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null) {
                    setGraphic(null); // Clear for empty cells
                    setMinHeight(AppConstants.DB_ROW_HEIGHT_EMPTY);
                    setMaxHeight(AppConstants.DB_ROW_HEIGHT_EMPTY);
                    setPrefHeight(AppConstants.DB_ROW_HEIGHT_EMPTY);
                } else {
                    setMinHeight(AppConstants.DB_ROW_HEIGHT);
                    setMaxHeight(AppConstants.DB_ROW_HEIGHT);
                    setPrefHeight(AppConstants.DB_ROW_HEIGHT);
                    CustomerRental rental = getTableRow().getItem();
                    if (rental != null) {
                        // Set the text for each line in the content VBox
                        nameLabel.setText(rental.getName());
                        addressBlockOneLabel.setText(rental.getAddressBlockOne());
                        addressBlockTwoLabel.setText(rental.getAddressBlockTwo());
                        addressBlockThreeLabel.setText(rental.getAddressBlockThree());

                        // Set the lift type text
                        String liftType = rental.getShortLiftType();
                        liftTypeLabel.setText(liftType != null ? liftType : "");
                        liftTypeLabel.setTranslateY(7);

                        // Set the overlay pane as the graphic for the cell
                        setGraphic(overlayPane);
                    }
                }
            }
        });











        customerAndAddressColumn.setPrefWidth(123);

        statusColumn.setCellValueFactory(cellData -> {
            CustomerRental rental = cellData.getValue();
            String status = rental.getStatus();
            return new SimpleBooleanProperty("Active".equals(status)); // Still retains the same logic for Active status
        });

        statusColumn.setCellFactory(column -> new TableCell<CustomerRental, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setMinHeight(AppConstants.DB_ROW_HEIGHT_EMPTY);
                    setMaxHeight(AppConstants.DB_ROW_HEIGHT_EMPTY);
                    setPrefHeight(AppConstants.DB_ROW_HEIGHT_EMPTY);
                } else {
                    setMinHeight(AppConstants.DB_ROW_HEIGHT);
                    setMaxHeight(AppConstants.DB_ROW_HEIGHT);
                    setPrefHeight(AppConstants.DB_ROW_HEIGHT);
                    CustomerRental rental = getTableView().getItems().get(getIndex());

                    // Check the conditions for displaying the checkbox based on lastActionType
                    boolean shouldShow = false;

                    if ("calling-off".equals(lastActionType) && "Active".equals(rental.getStatus())) {
                        shouldShow = true;
                    } else if ("dropping-off".equals(lastActionType) && "Upcoming".equals(rental.getStatus())) {
                        shouldShow = true;
                    } else if ("picking-up".equals(lastActionType) && "Called Off".equals(rental.getStatus())) {
                        shouldShow = true;
                    } else if ("creating-invoices".equals(lastActionType) &&
                               ("Called Off".equals(rental.getStatus()) || "Ended".equals(rental.getStatus()))) {
                        shouldShow = true;
                    } else if ("creating-contracts".equals(lastActionType)) {
                        shouldShow = true;
                    }

                    if (shouldShow) {
                        CheckBox checkBox = new CheckBox();
                        checkBox.setSelected(rental.isSelected()); // Assuming you have a method isSelected
                        checkBox.setOnAction(e -> {
                            handleSelection(checkBox.isSelected(), getIndex(), lastActionType); // Pass the last action type
                            rental.setSelected(checkBox.isSelected());
                        });

                        // Center the checkbox using StackPane
                        StackPane stackPane = new StackPane(checkBox);
                        stackPane.setAlignment(Pos.CENTER);
                        setGraphic(stackPane);
                    } else {
                        // If not showing checkboxes, display the circle
                        Circle circle = new Circle(8);
                        String status = rental.getStatus();
                        Tooltip tooltip = new Tooltip(status);
                        tooltip.setShowDelay(Duration.ZERO); // Set tooltip to appear instantly

                        if (status.equals("Upcoming")) {
                            circle.setFill(Color.BLACK);
                            tooltip.setStyle("-fx-background-color: #FF8C00; -fx-text-fill: white;"); // Tooltip style
                        } else if (status.equals("Active")) {
                            circle.setFill(Color.GREEN);
                            tooltip.setStyle("-fx-background-color: green; -fx-text-fill: white;"); // Tooltip style
                        } else if (status.equals("Called Off")) {
                            circle.setFill(Color.RED);
                            tooltip.setStyle("-fx-background-color: red; -fx-text-fill: white;"); // Tooltip style
                        } else if (status.equals("Picked Up")) {
                            circle.setFill(Color.web("#C0C0C0")); // Dark orange
                            tooltip.setStyle("-fx-background-color: #C0C0C0; -fx-text-fill: black;"); // Tooltip style
                        }

                        Tooltip.install(circle, tooltip); // Install the tooltip on the circle

                        // Center the circle using StackPane
                        StackPane stackPane = new StackPane(circle);
                        stackPane.setAlignment(Pos.CENTER);
                        setGraphic(stackPane);
                    }
                }
            }
        });
        statusColumn.setPrefWidth(24);

    }


    public TableColumn<CustomerRental, String> getSerialNumberColumn(){
        serialNumberColumn.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        serialNumberColumn.setCellFactory(column -> new TableCell<CustomerRental, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);  // Clear text for empty cells
                    setGraphic(null);  // Clear graphic for empty cells
                    setMinHeight(AppConstants.DB_ROW_HEIGHT_EMPTY);
                    setMaxHeight(AppConstants.DB_ROW_HEIGHT_EMPTY);
                    setPrefHeight(AppConstants.DB_ROW_HEIGHT_EMPTY);
                } else {
                    setMinHeight(AppConstants.DB_ROW_HEIGHT);
                    setMaxHeight(AppConstants.DB_ROW_HEIGHT);
                    setPrefHeight(AppConstants.DB_ROW_HEIGHT);

                    // Get the serial number from the CustomerRental
                    CustomerRental rental = getTableView().getItems().get(getIndex());
                    String serialNumber = rental.getSerialNumber(); // Get serial number

                    // Create a VBox to hold each character as a Label
                    VBox vBox = new VBox();
                    vBox.setAlignment(Pos.CENTER); // Center align the VBox

                    // Create a Label for the '#' character
                    Label hashLabel = new Label("#");
                    hashLabel.setStyle("-fx-font-size: 9.5;"); // Set the style for the hash character
                    vBox.getChildren().add(hashLabel); // Add the hash label to the VBox

                    // Create a Label for each character in the serial number
                    for (int i = 0; i < serialNumber.length(); i++) {
                        char c = serialNumber.charAt(i);
                        Label charLabel = new Label(String.valueOf(c));
                        charLabel.setStyle("-fx-font-size: 9; -fx-padding: 0;"); // Adjust style as needed
                        vBox.getChildren().add(charLabel); // Add each character label to the VBox
                    }

                    vBox.setSpacing(-5.5); // Set the spacing between characters
                    // Set the VBox as the graphic for the cell
                    setGraphic(vBox); // Set the VBox as the graphic for the cell
                }
            }
});




        serialNumberColumn.setPrefWidth(10);
        return serialNumberColumn;
    }

    public TableColumn<CustomerRental, String> getDeliveryDateColumn(){
        deliveryDateColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryDate"));

        deliveryDateColumn.setCellFactory(column -> new TableCell<CustomerRental, String>() {
            private final Label ddLabel = new Label();
            private final Label m1Label = new Label();
            private final Label m2Label = new Label();
            private final Label m3Label = new Label();
            private final VBox contentVBox = new VBox(ddLabel, m1Label, m2Label, m3Label);
            private final StackPane overLayPane = new StackPane(contentVBox);

            {
                contentVBox.setAlignment(Pos.CENTER);
                contentVBox.setPadding(new Insets(0, 5, 2, 5));
                contentVBox.setSpacing(-2);

                ddLabel.setStyle("-fx-font-weight: bold;");
                m1Label.setStyle("-fx-font-weight: normal;");
                m2Label.setStyle("-fx-font-weight: normal;");
                m3Label.setStyle("-fx-font-weight: normal;");

                contentVBox.setMinHeight(AppConstants.DB_ROW_HEIGHT);
                contentVBox.setMaxHeight(AppConstants.DB_ROW_HEIGHT);

                StackPane.setAlignment(contentVBox, Pos.CENTER);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setMinHeight(AppConstants.DB_ROW_HEIGHT_EMPTY);
                    setMaxHeight(AppConstants.DB_ROW_HEIGHT_EMPTY);
                    setPrefHeight(AppConstants.DB_ROW_HEIGHT_EMPTY);
                } else {
                    setMinHeight(AppConstants.DB_ROW_HEIGHT);
                    setMaxHeight(AppConstants.DB_ROW_HEIGHT);
                    setPrefHeight(AppConstants.DB_ROW_HEIGHT);

                    CustomerRental rental = getTableView().getItems().get(getIndex());
                    String deliveryDate = rental.getDeliveryDate();

                    if (deliveryDate != null && deliveryDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        String[] dateParts = deliveryDate.split("-");
                        String day = String.valueOf(Integer.parseInt(dateParts[2])); // Remove leading zero
                        int month = Integer.parseInt(dateParts[1]);

                        // Append ordinal suffix for day
                        String suffix;
                        int dayInt = Integer.parseInt(day);
                        if (dayInt >= 11 && dayInt <= 13) {
                            suffix = "th";
                        } else if (dayInt % 10 == 1) {
                            suffix = "st";
                        } else if (dayInt % 10 == 2) {
                            suffix = "nd";
                        } else if (dayInt % 10 == 3) {
                            suffix = "rd";
                        } else {
                            suffix = "th";
                        }

                        // Set text for day with suffix
                        ddLabel.setText(day + suffix);

                        // Set month labels as three-letter abbreviation
                        String monthAbbreviation = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase();
                        m1Label.setText(String.valueOf(monthAbbreviation.charAt(0)));
                        m2Label.setText(String.valueOf(monthAbbreviation.charAt(1)));
                        m3Label.setText(String.valueOf(monthAbbreviation.charAt(2)));

                        setGraphic(overLayPane);
                    }
                }
            }
        });




        deliveryDateColumn.setPrefWidth(40);
        return deliveryDateColumn;
    }

    public TableColumn<CustomerRental, String> getDeliveryTimeColumn(){
        deliveryTimeColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryTime"));


        deliveryTimeColumn.setCellFactory(column -> new TableCell<CustomerRental, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);  // Clear text for empty cells
                    setGraphic(null);  // Clear graphic for empty cells
                    setMinHeight(AppConstants.DB_ROW_HEIGHT_EMPTY);
                    setMaxHeight(AppConstants.DB_ROW_HEIGHT_EMPTY);
                    setPrefHeight(AppConstants.DB_ROW_HEIGHT_EMPTY);
                } else {
                    setMinHeight(AppConstants.DB_ROW_HEIGHT);
                    setMaxHeight(AppConstants.DB_ROW_HEIGHT);
                    setPrefHeight(AppConstants.DB_ROW_HEIGHT);

                    // Get delivery time from the CustomerRental
                    CustomerRental rental = getTableView().getItems().get(getIndex());
                    String time = rental.getDeliveryTime(); // Get delivery time

                    // Create a Pane to hold individual character labels
                    Pane pane = new Pane();
                    double labelHeight = 10; // Set height for character labels
                    double labelWidth = 10; // Set width for character labels

                    // Create labels for each character in the delivery time
                    for (int i = 0; i < time.length(); i++) {
                        char c = time.charAt(i);
                        Label charLabel = new Label(String.valueOf(c));
                        charLabel.setStyle("-fx-font-size: 10; -fx-padding: 0; -fx-margin: 0;"); // Remove padding and margin

                        // Set the layout position for each label, allowing significant overlap
                        charLabel.setLayoutX(i * 2); // Adjusting label width for closer overlap
                        charLabel.setLayoutY(0); // Align them to the top

                        // Set explicit dimensions
                        charLabel.setMinHeight(labelHeight);
                        charLabel.setMaxHeight(labelHeight);
                        charLabel.setPrefHeight(labelHeight);
                        charLabel.setMinWidth(labelWidth);
                        charLabel.setMaxWidth(labelWidth);
                        charLabel.setPrefWidth(labelWidth);

                        pane.getChildren().add(charLabel); // Add the label to the pane
                    }

                    // Adjust the height of the pane if needed
                    pane.setMinHeight(labelHeight);
                    pane.setMaxHeight(labelHeight);
                    pane.setPrefHeight(labelHeight);

                    setGraphic(pane); // Set the Pane as the graphic for the cell
                }
            }
        });










        return deliveryTimeColumn;
    }

    public void setClosedDriverColumn(){
        driverColumn.setCellFactory(column -> new TableCell<CustomerRental, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                // Check if the cell is empty or the item is null
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setMinHeight(AppConstants.DB_ROW_HEIGHT_EMPTY);
                    setMaxHeight(AppConstants.DB_ROW_HEIGHT_EMPTY);
                    setPrefHeight(AppConstants.DB_ROW_HEIGHT_EMPTY);
                } else {
                    setMinHeight(AppConstants.DB_ROW_HEIGHT);
                    setMaxHeight(AppConstants.DB_ROW_HEIGHT);
                    setPrefHeight(AppConstants.DB_ROW_HEIGHT);

                    if ("x".equals(item)) {
                        displayDriverIconOnly(); // Show only the driver icon when value is "x"
                    } else {
                        displayDriverWithIcon(item); // Show initials and icon for assigned drivers
                    }
                }
            }

            // Method to display only the driver icon
            private void displayDriverIconOnly() {
                VBox vBox = new VBox();
                vBox.setAlignment(Pos.BOTTOM_CENTER);


                try {
                    String imagePath = "/images/driver-icon.png";
                    Image image = new Image(getClass().getResourceAsStream(imagePath));
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(19);  // Set smaller size
                    imageView.setFitHeight(19);  // Set smaller size

                    vBox.getChildren().add(imageView);
                    setGraphic(vBox);
                } catch (Exception e) {
                    e.printStackTrace();  // Print the stack trace for debugging
                    setGraphic(null); // Set graphic to null in case of an error
                }
            }

            // Method to display driver initials and icon
            private void displayDriverWithIcon(String driverInitials) {
                HBox hBox = new HBox();
                hBox.setSpacing(1);  // Space between initials and the icon
                hBox.setAlignment(Pos.BOTTOM_CENTER);  // Align icon at the bottom

                // Create a label for driver initials
                Label initialsLabel = new Label(driverInitials);
                initialsLabel.setTextFill(Color.BLACK); // Ensure text is visible
                initialsLabel.setTranslateX(3);

                // Create an ImageView for the driver icon
                try {
                    String imagePath = "/images/driver-icon.png";
                    Image image = new Image(getClass().getResourceAsStream(imagePath));
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(19);  // Smaller image (24px - 5px)
                    imageView.setFitHeight(19);
                    imageView.setTranslateX(3);

                    hBox.getChildren().addAll(initialsLabel, imageView);  // Add both to VBox
                    setGraphic(hBox);
                } catch (Exception e) {
                    e.printStackTrace(); // Print the stack trace for debugging
                    setText(driverInitials); // Fallback to just initials
                    setTextFill(Color.BLACK); // Ensure text is visible
                    setGraphic(null); // Remove any graphic in case of an error
                }
            }
        });




        driverComboBoxOpenOrClosed = "closed";
    }

    public void setOpenDriverColumn(){
        driverColumn.setCellFactory(column -> new TableCell<CustomerRental, String>() {
            private final ComboBox<String> comboBox = new ComboBox<>();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getItem() == null) {
                    setText(null);
                    setGraphic(null);
                    setMinHeight(AppConstants.DB_ROW_HEIGHT_EMPTY);
                    setMaxHeight(AppConstants.DB_ROW_HEIGHT_EMPTY);
                    setPrefHeight(AppConstants.DB_ROW_HEIGHT_EMPTY);
                    return;
                }
                setMinHeight(AppConstants.DB_ROW_HEIGHT);
                setMaxHeight(AppConstants.DB_ROW_HEIGHT);
                setPrefHeight(AppConstants.DB_ROW_HEIGHT);

                CustomerRental currentRental = dbTableView.getItems().get(getIndex());
                System.out.println("Updating cell for rental: " + currentRental);

                Set<String> potentialDrivers = calculatePotentialDrivers(currentRental);
                comboBox.getItems().setAll(potentialDrivers);
                comboBox.getSelectionModel().select(currentRental.getDriver());

                comboBox.setOnAction(event -> {
                    String selectedDriver = comboBox.getValue();
                    System.out.println("Driver Selection: " + selectedDriver);
                    if (selectedDriver != null) {
                        updateGroupedRentals(selectedDriver, currentRental);
                        commitEdit(selectedDriver);
                        dbTableView.refresh();
                    }
                });

                // Pre-select driver initials
                VBox vBox = new VBox(comboBox);
                vBox.setSpacing(5);
                vBox.setAlignment(Pos.BOTTOM_CENTER);
                setGraphic(vBox);  // Display combo box in VBox
            }

            @Override
            public void commitEdit(String newValue) {
                super.commitEdit(newValue);
                if (getTableRow() != null && getTableRow().getItem() != null) {
                    CustomerRental rental = getTableRow().getItem();
                    if (rental != null) {
                        rental.setDriver(newValue);  // Update driver in rental
                        rental.setupDriverCompositionalParts();
                        String newDriverInitial = rental.getDriver();
                        System.out.println("Committed Edit: New Driver for Rental: " + newValue);
                        updateDriverInDatabase(rental.getRentalItemId(), newDriverInitial);
                    }
                }
            }
        });
        driverComboBoxOpenOrClosed = "open";
    }

    public TableColumn<CustomerRental, String> getDriverColumn(){
        if (driverComboBoxOpenOrClosed == "") { // if the driver column is still default uninstantiated
            driverColumn.setCellValueFactory(new PropertyValueFactory<>("driver"));
            driverColumn.setPrefWidth(63);  // minimum is 63 for those combo boxes
            setClosedDriverColumn();
        }
        System.out.println("Getting driver column for status: " + driverComboBoxOpenOrClosed);
        return driverColumn;
    }


    public TableColumn<CustomerRental, String> getAddressColumn(){

        return customerAndAddressColumn;

    }


    public TableColumn<CustomerRental, Boolean> getStatusColumn(){


        return statusColumn;
    }

    public void setLiftTypeLabel(Label liftTypeLabel){
        globalLiftTypeLabel = liftTypeLabel;
    }

    public Label getLiftTypeLabel(){
        return globalLiftTypeLabel;
    }

    private void handleSelection(boolean isSelected, int index, String actionType) {
        dbTableView.getItems().get(index).setSelected(isSelected);

        // Show the update button only if at least one row is selected
        boolean anySelected = dbTableView.getItems().stream().anyMatch(CustomerRental::isSelected);
        updateRentalButton.setVisible(anySelected);

    }

    public void showSelectableCheckboxes(boolean visible, String actionType) {
        lastActionType = actionType;
        boolean shouldShow = "dropping-off".equals(actionType) ||
                "calling-off".equals(actionType) ||
                "picking-up".equals(actionType) ||
                "creating-invoices".equals(actionType) ||
                "creating-contracts".equals(actionType);

        this.shouldShowCheckboxes = shouldShow && visible; // Set the class-level variable
        dbTableView.refresh();
    }

    public void resetCheckboxes() {
        // Deselect all checkboxes in the table
        for (CustomerRental order : dbTableView.getItems()) {
            order.setSelected(false);
        }
        dbTableView.refresh(); // Refresh the table view to update the checkbox states
    }

    private Set<String> calculatePotentialDrivers(CustomerRental rental) {
        // Initialize a set to hold potential drivers.
        Set<String> potentialDrivers = new HashSet<>();

        // Add the "x" option for clearing the driver.
        potentialDrivers.add("x");

        // Get the current driver assigned to this rental.
        String currentDriver = rental.getDriver();
        String currentDriverInitial = currentDriver.replaceAll("[^A-Za-z]", "");
        String currentDriverNumber = currentDriver.replaceAll("[^0-9]", "");

        driverSequenceMap.clear();

        // Count existing drivers by their initials.
        for (CustomerRental r : dbTableView.getItems()) {
            String driver = r.getDriver();

            if (driver != null && !driver.equals("x")) {
                // Extract letters (initials) part only, assuming initials are the letters at the start
                String initial = driver.replaceAll("[^A-Za-z]", "");

                // Increment the sequence for the extracted initial
                int newSequence = driverSequenceMap.getOrDefault(initial, 0) + 1;
                driverSequenceMap.put(initial, newSequence);
            }
        }

        // Add potential drivers based on the initials and count
        for (String driverInitial : driverInitials) {
            // Count of existing drivers for this initial
            int count = driverSequenceMap.getOrDefault(driverInitial, 0);

            // Adding the current driver without increment
            if (driverInitial.equals(currentDriverInitial)) {
                for (int i = 1; i <= count; i++) {
                    String potentialDriver = driverInitial + i; // Append the count
                    potentialDrivers.add(potentialDriver);
                }
            } else {
                // Adding initial with a sequence number
                for (int i = 1; i <= count + 1; i++) {
                    String potentialDriver = driverInitial + i; // Append the count
                    potentialDrivers.add(potentialDriver);
                }

                // If there are no existing rentals for this initial, we still want to add the first option
                if (count == 0) {
                    potentialDrivers.add(driverInitial + "1");
                }
            }
        }

        return potentialDrivers;
    }

    private void updateGroupedRentals(String driverValue, CustomerRental rental) {
        // Extract letters (initials) and numbers (sequence) from the new driver value
        System.out.println("New Driver Value: " + driverValue);

        String newInitial = driverValue.replaceAll("[^A-Za-z]", "");  // Extract letters only
        String newStringSeqNumber = driverValue.replaceAll("[^0-9]", "");  // Extract digits only

        System.out.println("New Initial: " + newInitial);
        System.out.println("New Sequence Number: " + newStringSeqNumber);

        // Get the current driver assigned to this rental
        String currentDriver = rental.getDriver();
        System.out.println("Old Driver Value: " + currentDriver);

        // Extract initials and sequence from the old driver value
        String oldInitial = currentDriver.replaceAll("[^A-Za-z]", "");  // Extract letters only
        String oldSeqNumber = currentDriver.replaceAll("[^0-9]", "");  // Extract digits only

        System.out.println("Old Initial: " + oldInitial);
        System.out.println("Old Sequence Number: " + oldSeqNumber);

        // Convert sequence numbers to integers for comparisons, if needed

        int newIntSeqNum = newStringSeqNumber.isEmpty() ? 0 : parseInt(newStringSeqNumber);
        int oldIntSeqNum = oldSeqNumber.isEmpty() ? 0 : parseInt(oldSeqNumber);

        // Adjust sequence for rentals with the new initial
        List<CustomerRental> newRentals = groupedRentals.get(newInitial);

        if (!driverValue.equals("x")) {
            if (newRentals != null) {
                System.out.println("Derived newRentals for the new initial '" + newInitial + "': " + newRentals);
                System.out.println("Processing rentals with new initial: " + newInitial);
                System.out.println("Current driver sequence: " + driverSequenceMap.get(newInitial));

                // Increment sequence numbers for rentals with the new initial that are higher
                for (CustomerRental r : newRentals) {
                    System.out.println("Checking rental: " + r);

                    String sequenceStringOfRentalBeingChecked = String.valueOf(r.getSequenceNumber());
                    int sequenceIntOfRentalBeingChecked = r.getSequenceNumber();
                    System.out.println("Sequence number of rental being checked: " + sequenceStringOfRentalBeingChecked);

                    if (sequenceIntOfRentalBeingChecked >= newIntSeqNum) {
                        System.out.println("Updating rental from new initial: " + r);
                        int newSequenceInt = sequenceIntOfRentalBeingChecked + 1;
                        String newSequenceString = String.valueOf(newSequenceInt);
                        r.setDriver(newInitial + newSequenceString); // Update driver value
                        System.out.println("Set driver to: " + r.getDriver());
                    } else {
                        System.out.println("Skipping rental (below the sequence of the new rental value): " + r);
                    }
                }
            } else {
                System.out.println("No rentals found with initial: " + newInitial);
            }
        }

        // Adjust sequence for rentals with the old initial
        List<CustomerRental> currentRentals = groupedRentals.get(oldInitial);
        if (currentRentals != null) {
            System.out.println("Derived currentRentals for the old initial '" + oldInitial + "': " + currentRentals);
            System.out.println("Processing rentals with old initial: " + oldInitial);
            System.out.println("Current driver sequence: " + driverSequenceMap.get(oldInitial));

            // Decrement sequence numbers for rentals with the old initial that are higher
            for (CustomerRental r : currentRentals) {
                System.out.println("Checking rental: " + r);

                String sequenceStringOfRentalBeingChecked = String.valueOf(r.getSequenceNumber());
                int sequenceIntOfRentalBeingChecked = r.getSequenceNumber();
                System.out.println("Sequence number of rental being checked: " + sequenceStringOfRentalBeingChecked);

                if (sequenceIntOfRentalBeingChecked > oldIntSeqNum) {
                    System.out.println("Updating rental from old initial: " + r);
                    int newSequenceInt = sequenceIntOfRentalBeingChecked - 1;
                    String newSequenceString = String.valueOf(newSequenceInt);
                    r.setDriver(oldInitial + newSequenceString); // Update driver value
                    System.out.println("Set driver to: " + r.getDriver());
                }
            }
            // Remove the rental from the old initial list
            currentRentals.remove(rental);
            System.out.println("Removed rental: " + rental + " from old initial: " + oldInitial);

            // If there are no more rentals for the old initial, remove it from the map
            if (currentRentals.isEmpty()) {
                groupedRentals.remove(oldInitial);
                System.out.println("Removed old initial: " + oldInitial + " from groupedRentals.");
            }
        }

        // Now, set the new driver for the rental
        rental.setDriver(driverValue); // Set the new driver value
        System.out.println("Set new driver for rental: " + rental + " to: " + driverValue);

        // Update the sequence number of the rental
        if (newIntSeqNum != 0) {
            rental.setSequenceNumber(newIntSeqNum); // Set the new sequence number
            System.out.println("Set sequence number for rental: " + rental + " to: " + newIntSeqNum);
        }

        // Add the rental to the new driver's list
        groupedRentals.computeIfAbsent(newInitial, k -> new ArrayList<>()).add(rental);
        System.out.println("Added rental: " + rental + " to new initial: " + newInitial);
    }


    private void updateDriverInDatabase(int rentalItemId, String newDriver) {
        String updateQuery = "UPDATE rental_items SET driver = ? WHERE rental_item_id = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/practice_db", "root", "SQL3225422!a");
             PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {

            preparedStatement.setString(1, newDriver);
            preparedStatement.setInt(2, rentalItemId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initialsFallbackFailsafe() {
        // Iterate through all the rentals in the ordersList and remove numeric suffixes from drivers
        for (CustomerRental rental : dbTableView.getItems()) {
            String driver = rental.getDriver();

            // Check if the driver has a number suffix, and if so, strip it off
            String[] parts = driver.split("(?<=\\D)(?=\\d)");  // Split at the point where a letter meets a number
            if (parts.length > 1) {
                // If there is a number part, we want to keep only the initials
                String driverInitials = parts[0]; // Keep only the non-numeric part (initials)
                rental.setDriver(driverInitials);  // Update the in-memory object

                // Update the driver initials in the database
                updateDriverInDatabase(rental.getRentalItemId(), driverInitials);
            }
        }
    }

}

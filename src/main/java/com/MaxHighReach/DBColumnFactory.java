package com.MaxHighReach;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.File;


import static java.lang.Integer.parseInt;

import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.utils.PdfMerger;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;

public class DBColumnFactory {

    private final TableColumn<CustomerRental, String> customerAndAddressColumn = new TableColumn<>();
    private final TableColumn<CustomerRental, Boolean> statusColumn = new TableColumn<>();
    private final TableColumn<CustomerRental, String> serialNumberColumn = new TableColumn<>();
    private final TableColumn<CustomerRental, String> deliveryDateColumn = new TableColumn<>();
    private final TableColumn<CustomerRental, String> deliveryTimeColumn = new TableColumn<>();
    private final TableColumn<CustomerRental, String> driverColumn = new TableColumn<>();
    private final TableColumn<CustomerRental, String> selectColumn = new TableColumn<>();
    private final TableColumn<CustomerRental, String> invoiceColumn = new TableColumn<>();
    private final Button updateRentalButton;
    private TextField serialNumberField;
    private final Button batchButton;
    private String batchSwitcher;
    private final Button secondInProcessButton;
    private String lastActionType = "";
    private final TableView<CustomerRental> dbTableView;
    private Map<String, List<CustomerRental>> groupedRentals = new HashMap<>();
    private Map<String, Integer> driverSequenceMap = new HashMap<>();
    private boolean shouldShowCheckboxes = false;
    private final ObservableList<String> driverInitials = FXCollections.observableArrayList("A", "J", "I", "B", "JC", "K");
    private String driverComboBoxOpenOrClosed = "";
    private Label globalLiftTypeLabel;

    public DBColumnFactory(Button button, TextField textField, TableView<CustomerRental> tableView, Map<String,
            List<CustomerRental>> rentalsMap, Map<String, Integer> driverMap, Button button2, String buttonString, Button button3) {
        this.updateRentalButton = button;
        this.serialNumberField = textField;
        this.batchButton = button2;
        this.batchSwitcher = buttonString;
        this.secondInProcessButton = button3;
        this.dbTableView = tableView;
        this.groupedRentals = rentalsMap;
        this.driverSequenceMap = driverMap;

        initializeColumns();
    }

    // New constructr with only the TableView argument
    public DBColumnFactory(TableView<CustomerRental> tableView, Button button) {
        this.dbTableView = tableView;
        this.updateRentalButton = button;
        batchButton = new Button();
        secondInProcessButton = new Button();
        initializeColumns();
    }

    // Method to initialize columns and other properties
    private void initializeColumns() {
        customerAndAddressColumn.setCellFactory(column -> new TableCell<CustomerRental, String>() {
            private final Label nameLabel = new Label();
            private final Label addressBlockOneLabel = new Label();
            private final Label addressBlockTwoLabel = new Label();
            private final Label addressBlockThreeLabel = new Label();
            private final VBox contentVBox = new VBox(nameLabel, addressBlockOneLabel, addressBlockTwoLabel, addressBlockThreeLabel);
            private final Label liftTypeLabel = new Label();
            private final StackPane overlayPane = new StackPane(contentVBox, liftTypeLabel);
            private final DropShadow glowEffect = new DropShadow();

            {
                contentVBox.setAlignment(Pos.TOP_LEFT);
                contentVBox.setPadding(new Insets(0));
                contentVBox.setSpacing(-2);
                contentVBox.setFillWidth(true);

                nameLabel.setStyle("-fx-font-weight: bold;");
                addressBlockOneLabel.setStyle("-fx-font-weight: normal;");
                addressBlockTwoLabel.setStyle("-fx-font-weight: normal;");
                addressBlockThreeLabel.setStyle("-fx-font-weight: normal;");

                contentVBox.setMinHeight(Config.DB_ROW_HEIGHT);
                contentVBox.setMaxHeight(Config.DB_ROW_HEIGHT);

                glowEffect.setRadius(10);
                glowEffect.setSpread(0.5);
                liftTypeLabel.setEffect(glowEffect);
                liftTypeLabel.getStyleClass().add("lift-type-in-corner");
                liftTypeLabel.setStyle("-fx-font-weight: bold;");
                liftTypeLabel.setFont(Font.font("Patrick Hand"));

                globalLiftTypeLabel = liftTypeLabel;

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

                StackPane.setAlignment(liftTypeLabel, Pos.BOTTOM_RIGHT);
                StackPane.setAlignment(contentVBox, Pos.CENTER_LEFT);
                StackPane.setMargin(liftTypeLabel, new Insets(0, 5, 8, 0));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null) {
                    setGraphic(null);
                    setMinHeight(Config.DB_ROW_HEIGHT_EMPTY);
                    setMaxHeight(Config.DB_ROW_HEIGHT_EMPTY);
                    setPrefHeight(Config.DB_ROW_HEIGHT_EMPTY);
                } else {
                    setMinHeight(Config.DB_ROW_HEIGHT);
                    setMaxHeight(Config.DB_ROW_HEIGHT);
                    setPrefHeight(Config.DB_ROW_HEIGHT);
                    CustomerRental rental = getTableRow().getItem();
                    if (rental != null) {
                        nameLabel.setText(rental.getName());
                        addressBlockOneLabel.setText(rental.getAddressBlockOne());
                        addressBlockTwoLabel.setText(rental.getAddressBlockTwo());
                        addressBlockThreeLabel.setText(rental.getCity());

                        String liftType = rental.getShortLiftType();
                        liftTypeLabel.setText(liftType != null ? liftType : "");
                        liftTypeLabel.setTranslateY(7);

                        setGraphic(overlayPane);
                    }
                }
            }
        });

        customerAndAddressColumn.setPrefWidth(123);

        statusColumn.setCellValueFactory(cellData -> {
            CustomerRental rental = cellData.getValue();
            String status = rental.getStatus();
            return new SimpleBooleanProperty("Active".equals(status));
        });

        statusColumn.setCellFactory(column -> new TableCell<CustomerRental, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setMinHeight(Config.DB_ROW_HEIGHT_EMPTY);
                    setMaxHeight(Config.DB_ROW_HEIGHT_EMPTY);
                    setPrefHeight(Config.DB_ROW_HEIGHT_EMPTY);
                } else {
                    setMinHeight(Config.DB_ROW_HEIGHT);
                    setMaxHeight(Config.DB_ROW_HEIGHT);
                    setPrefHeight(Config.DB_ROW_HEIGHT);
                    CustomerRental rental = getTableView().getItems().get(getIndex());

                    boolean shouldShow = false;
                    boolean shouldShowExpandIcons = false;

                    if ("calling-off".equals(lastActionType) && "Active".equals(rental.getStatus())) {
                        shouldShow = true;
                    } else if ("dropping-off".equals(lastActionType) && "Upcoming".equals(rental.getStatus())) {
                        shouldShow = true;
                    } else if ("picking-up".equals(lastActionType) && "Called Off".equals(rental.getStatus())) {
                        shouldShow = true;
                    } else if ("composing-invoices".equals(lastActionType) &&
                               ("Called Off".equals(rental.getStatus()) || "Ended".equals(rental.getStatus()))) {
                        shouldShow = true;
                    } else if ("composing-contracts".equals(lastActionType)) {
                        shouldShow = true;
                    } else if ("deleting".equals(lastActionType)) {
                        shouldShow = true;
                    } else if ("expanding".equals(lastActionType)) {
                        shouldShowExpandIcons = true;
                    }

                    if (shouldShow) {
                        CheckBox checkBox = new CheckBox();
                        checkBox.setSelected(rental.isSelected());
                        checkBox.setOnAction(e -> {
                            handleSelection(checkBox.isSelected(), getIndex(), lastActionType);
                            rental.setSelected(checkBox.isSelected());
                        });

                        StackPane stackPane = new StackPane(checkBox);
                        stackPane.setAlignment(Pos.CENTER);
                        setGraphic(stackPane);
                    } else if (shouldShowExpandIcons) {
                        Button button = new Button();
                        ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/images/small-expand.png")));
                        imageView.setFitWidth(13);
                        imageView.setFitHeight(13);
                        button.setGraphic(imageView);
                        button.getStyleClass().add("expand-button");
                        int rowIndex = getIndex();
                        button.setOnAction(event -> handleExpandSelection(rowIndex));
                        StackPane stackPane = new StackPane(button);
                        stackPane.setAlignment(Pos.CENTER);
                        setGraphic(stackPane);
                    } else {
                        Circle circle = new Circle(8);
                        String status = rental.getStatus();
                        Tooltip tooltip = new Tooltip(status);
                        tooltip.setShowDelay(Duration.ZERO);

                        if (status.equals("Upcoming")) {
                            if (MaxReachPro.getUser()[0] == "Sandy Mulberry") {
                                circle.setFill(Color.web("#F4F471"));
                                tooltip.setStyle("-fx-background-color: #F4F471; -fx-text-fill: black;");
                            } else {
                                circle.setFill(Color.ORANGE);
                                tooltip.setStyle("-fx-background-color: orange; -fx-text-fill: black;");
                            }
                        } else if (status.equals("Active")) {
                            circle.setFill(Color.GREEN);
                            tooltip.setStyle("-fx-background-color: green; -fx-text-fill: white;");
                        } else if (status.equals("Called Off")) {
                            circle.setFill(Color.RED);
                            tooltip.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                        } else if (status.equals("Picked Up")) {
                            circle.setFill(Color.BLACK);
                            tooltip.setStyle("-fx-background-color: black; -fx-text-fill: white;");
                        }

                        Tooltip.install(circle, tooltip);

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
                    setMinHeight(Config.DB_ROW_HEIGHT_EMPTY);
                    setMaxHeight(Config.DB_ROW_HEIGHT_EMPTY);
                    setPrefHeight(Config.DB_ROW_HEIGHT_EMPTY);
                } else {
                    setMinHeight(Config.DB_ROW_HEIGHT);
                    setMaxHeight(Config.DB_ROW_HEIGHT);
                    setPrefHeight(Config.DB_ROW_HEIGHT);

                    // Get the serial number from the CustomerRental
                    CustomerRental rental = getTableView().getItems().get(getIndex());
                    if (rental.getLiftId() == 1008) {
                        rental.setSerialNumber("45");
                    } else if (rental.getLiftId() == 1007) {
                        rental.setSerialNumber("33");
                    }
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

                contentVBox.setMinHeight(Config.DB_ROW_HEIGHT);
                contentVBox.setMaxHeight(Config.DB_ROW_HEIGHT);

                StackPane.setAlignment(contentVBox, Pos.CENTER);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setMinHeight(Config.DB_ROW_HEIGHT_EMPTY);
                    setMaxHeight(Config.DB_ROW_HEIGHT_EMPTY);
                    setPrefHeight(Config.DB_ROW_HEIGHT_EMPTY);
                } else {
                    setMinHeight(Config.DB_ROW_HEIGHT);
                    setMaxHeight(Config.DB_ROW_HEIGHT);
                    setPrefHeight(Config.DB_ROW_HEIGHT);

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

        deliveryDateColumn.setPrefWidth(45);
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
                    setMinHeight(Config.DB_ROW_HEIGHT_EMPTY);
                    setMaxHeight(Config.DB_ROW_HEIGHT_EMPTY);
                    setPrefHeight(Config.DB_ROW_HEIGHT_EMPTY);
                } else {
                    setMinHeight(Config.DB_ROW_HEIGHT);
                    setMaxHeight(Config.DB_ROW_HEIGHT);
                    setPrefHeight(Config.DB_ROW_HEIGHT);


                    // Get delivery time from the CustomerRental
                    CustomerRental rental = getTableView().getItems().get(getIndex());
                    String time = rental.getDeliveryTime(); // Get delivery time


                    // Create a VBox to hold each character as a Label
                    VBox vBox = new VBox();
                    vBox.setAlignment(Pos.CENTER); // Center align the VBox


                    // Create a Label for each character in the delivery time
                    for (int i = 0; i < time.length(); i++) {
                        char c = time.charAt(i);
                        String cc = String.valueOf(c);
                        if (cc == "-") {
                            cc = "|";
                        }
                        Label charLabel = new Label(cc);
                        charLabel.setStyle("-fx-font-size: 10; -fx-padding: 1.5;"); // Adjust style as needed
                        vBox.getChildren().add(charLabel); // Add each character label to the VBox
                    }


                    vBox.setSpacing(-5.5); // Set the spacing between characters
                    setGraphic(vBox); // Set the VBox as the graphic for the cell
                }
            }
        });

        deliveryTimeColumn.setPrefWidth(11);
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
                    setMinHeight(Config.DB_ROW_HEIGHT_EMPTY);
                    setMaxHeight(Config.DB_ROW_HEIGHT_EMPTY);
                    setPrefHeight(Config.DB_ROW_HEIGHT_EMPTY);
                } else {
                    setMinHeight(Config.DB_ROW_HEIGHT);
                    setMaxHeight(Config.DB_ROW_HEIGHT);
                    setPrefHeight(Config.DB_ROW_HEIGHT);

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
                    setMinHeight(Config.DB_ROW_HEIGHT_EMPTY);
                    setMaxHeight(Config.DB_ROW_HEIGHT_EMPTY);
                    setPrefHeight(Config.DB_ROW_HEIGHT_EMPTY);
                    return;
                }
                setMinHeight(Config.DB_ROW_HEIGHT);
                setMaxHeight(Config.DB_ROW_HEIGHT);
                setPrefHeight(Config.DB_ROW_HEIGHT);

                CustomerRental currentRental = dbTableView.getItems().get(getIndex());

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

    public TableColumn<CustomerRental, String> getSelectColumn(){
        selectColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryTime"));

        selectColumn.setCellFactory(column -> new TableCell<CustomerRental, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setMinHeight(Config.DB_ROW_HEIGHT_EMPTY);
                    setMaxHeight(Config.DB_ROW_HEIGHT_EMPTY);
                    setPrefHeight(Config.DB_ROW_HEIGHT_EMPTY);
                } else {
                    setMinHeight(Config.DB_ROW_HEIGHT);
                    setMaxHeight(Config.DB_ROW_HEIGHT);
                    setPrefHeight(Config.DB_ROW_HEIGHT);
                    CustomerRental rental = getTableView().getItems().get(getIndex());

                    CheckBox checkBox = new CheckBox();
                    checkBox.setSelected(rental.isSelected());
                    checkBox.setOnAction(e -> {
                        handleSelection(checkBox.isSelected(), getIndex(), "Compose Invoices");
                        rental.setSelected(checkBox.isSelected());
                    });

                    StackPane stackPane = new StackPane(checkBox);
                    stackPane.setAlignment(Pos.CENTER);
                    setGraphic(stackPane);

                }
            }
        });

        selectColumn.setPrefWidth(22);
        return selectColumn;
    }

    public TableColumn<CustomerRental, String> getInvoiceColumn(){
        invoiceColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryTime"));

        invoiceColumn.setCellFactory(column -> new TableCell<CustomerRental, String>() {


            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);


                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setMinHeight(Config.DB_ROW_HEIGHT_EMPTY);
                    setMaxHeight(Config.DB_ROW_HEIGHT_EMPTY);
                    setPrefHeight(Config.DB_ROW_HEIGHT_EMPTY);
                } else {
                    setMinHeight(Config.DB_ROW_HEIGHT);
                    setMaxHeight(Config.DB_ROW_HEIGHT);
                    setPrefHeight(Config.DB_ROW_HEIGHT);


                    CustomerRental rental = getTableView().getItems().get(getIndex());
                    VBox vBox = new VBox();
                    vBox.setSpacing(-3);
                    vBox.setAlignment(Pos.BOTTOM_CENTER);


                    // Top section handling (optional "Send to QuickBooks" image)
                    if (rental.isWritingInvoice()) {
                        ImageView sendImage = createImageView("/images/send-to-quickbooks.png", 34);
                        Tooltip tooltip = new Tooltip("Ready to Send");
                        tooltip.setShowDelay(Duration.ZERO);
                        Tooltip.install(sendImage, tooltip);
                        vBox.getChildren().add(createHBox(sendImage));
                    }


                    // Bottom section handling (invoice status and check/X icon)
                    HBox bottomBox = createBottomPane(rental.isInvoiceWritten());
                    vBox.getChildren().add(bottomBox);


                    setGraphic(vBox);
                }
            }


            // Helper method for creating the top HBox with an image
            private HBox createHBox(Node node) {
                HBox hBox = new HBox();
                hBox.getChildren().add(node);
                hBox.setAlignment(Pos.CENTER);
                return hBox;
            }


            // Helper method for creating the bottom pane with the image and status
            private HBox createBottomPane(boolean isInvoiceWritten) {
                HBox hBox = new HBox();
                hBox.setSpacing(0);
                hBox.setAlignment(Pos.CENTER_LEFT);


                String imagePath = "/images/create-invoices.png";
                ImageView statusImage = createImageView(imagePath, 27);


                Label statusLabel;
                if (isInvoiceWritten) {
                    Label checkSymbol = new Label("\u2713"); // Unicode checkmark symbol
                    checkSymbol.setStyle("-fx-text-fill: green; -fx-font-size: 18px; -fx-padding: 0;");
                    statusLabel = new Label(" Has\n invoice");
                    hBox.getChildren().addAll(statusImage, checkSymbol, statusLabel);
                } else {
                    Label xSymbol = new Label("\u2717"); // Unicode X symbol
                    xSymbol.setStyle("-fx-text-fill: red; -fx-font-size: 18px; -fx-padding: -2;");
                    statusLabel = new Label("  Needs\n  invoice");
                    hBox.getChildren().addAll(statusImage, xSymbol, statusLabel);
                }


                statusLabel.setStyle("-fx-font-size: 9; -fx-padding: 0;");
                return hBox;
            }


            // Helper method to create an ImageView from an image path
            private ImageView createImageView(String path, int fitHeight) {
                Image image = new Image(getClass().getResourceAsStream(path));
                ImageView imageView = new ImageView(image);
                imageView.setFitHeight(fitHeight);
                imageView.setPreserveRatio(true);
                return imageView;
            }
        });


        invoiceColumn.setPrefWidth(80); // Adjust width as needed
        return invoiceColumn;
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

        if (actionType.equals("composing-contracts")) {
            batchButton.setText("Batch Contracts");
            batchButton.setVisible(anySelected);
            updateRentalButton.setVisible(false);

            if (anySelected) {
                batchButton.setOnAction(event -> {
                    ObservableList<CustomerRental> selectedRentals = dbTableView.getItems().filtered(CustomerRental::isSelected);

                    if (selectedRentals.isEmpty()) {
                        System.out.println("No rentals selected. Cannot compose contracts.");
                        return;
                    }

                    String sourceFile = "C:/Users/maxhi/OneDrive/Documents/Quickbooks/QBProgram Development/Composing Contracts/contract template.pdf";
                    List<String> createdPdfFiles = new ArrayList<>();

                    for (CustomerRental rental : selectedRentals) {
                        String outputFile = "C:/Users/maxhi/OneDrive/Documents/Quickbooks/QBProgram Development/Composing Contracts/contract_" + rental.getRentalItemId() + ".pdf";

                        try {
                            // Open the source PDF
                            PdfDocument pdfDoc = new PdfDocument(new PdfReader(sourceFile), new PdfWriter(outputFile));
                            Document document = new Document(pdfDoc);

                            // Get page 1 of the PDF
                            PdfCanvas canvas = new PdfCanvas(pdfDoc.getPage(1));

                            // Add text to specific coordinates
                            canvas.beginText();
                            canvas.setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(), 12);

                            String dateString = rental.getDeliveryDate();
                            LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM'.' d");
                            String formattedDate = date.format(formatter);
                            int day = date.getDayOfMonth();
                            String suffix = getDaySuffix(day);
                            String formattedDeliveryDate = formattedDate + suffix;
                            canvas.setTextMatrix(435, 711); // Delivery Date
                            canvas.showText(formattedDeliveryDate);

                            // Additional fields
                            canvas.setTextMatrix(449, 747); // Delivery Time
                            canvas.showText("P" + rental.getRentalItemId());
                            canvas.setTextMatrix(355, 631); // Address Block One
                            canvas.showText(rental.getAddressBlockOne());
                            canvas.setTextMatrix(346, 613); // Address Block Two
                            canvas.showText(rental.getAddressBlockTwo());
                            canvas.setTextMatrix(364, 595); // Address Block Three
                            canvas.showText(rental.getCity());

                            if (rental.getSiteContactName() != null) {
                                canvas.setTextMatrix(371, 577); // Address Block Four
                                canvas.showText(rental.getSiteContactName());
                                canvas.setTextMatrix(454, 577); // Address Block Five
                                canvas.showText(formatPhoneNumber(rental.getSiteContactPhone()));
                            }

                            if (rental.getOrderedByName() != null) {
                                canvas.setTextMatrix(119, 559); // Address Block Four
                                canvas.showText(rental.getOrderedByName());
                                canvas.setTextMatrix(76, 577); // Address Block Five
                                canvas.showText(formatPhoneNumber(rental.getOrderedByPhone()));
                            }

                            canvas.setTextMatrix(194, 559); // PO Number
                            canvas.showText(rental.getPoNumber());
                            canvas.setTextMatrix(43, 523);
                            canvas.showText(rental.getLocationNotes());
                            canvas.setTextMatrix(81, 630); // Name
                            canvas.showText(rental.getName());
                            canvas.setTextMatrix(43, 652);
                            canvas.showText(rental.getPreTripInstructions());
                            canvas.setTextMatrix(99, 481); // Lift Type
                            canvas.showText(rental.getLiftType());

                            canvas.endText();

                            // Close the document
                            document.close();

                            // Track the generated PDF file
                            createdPdfFiles.add(outputFile);

                            System.out.println("Contract created: " + outputFile);
                            secondInProcessButton.setVisible(true);
                        } catch (Exception e) {
                            System.out.println("Error creating contract for rental ID " + rental.getRentalItemId() + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    }

                    // Merge individual PDFs into one file
                    if (!createdPdfFiles.isEmpty()) {
                        String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        String finalOutputFile = "C:/Users/maxhi/OneDrive/Documents/Quickbooks/QBProgram Development/Composing Contracts/contracts_" + todayDate + ".pdf";

                        try {
                            PdfDocument finalPdfDoc = new PdfDocument(new PdfWriter(finalOutputFile));
                            PdfMerger merger = new PdfMerger(finalPdfDoc);

                            for (String pdfFile : createdPdfFiles) {
                                PdfDocument docToMerge = new PdfDocument(new PdfReader(pdfFile));
                                merger.merge(docToMerge, 1, docToMerge.getNumberOfPages());
                                docToMerge.close();
                            }

                            finalPdfDoc.close();

                            System.out.println("All contracts merged into: " + finalOutputFile);

                            // Clean up individual PDFs
                            for (String pdfFile : createdPdfFiles) {
                                File file = new File(pdfFile);
                                if (file.exists() && file.delete()) {
                                    System.out.println("Deleted temporary file: " + pdfFile);
                                }
                            }

                            System.out.println("Temporary individual contract files deleted.");
                        } catch (Exception e) {
                            System.out.println("Error merging PDFs: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("No contracts were created, so no merge occurred.");
                    }

                    System.out.println("Batch contracts processing completed.");
                });
            }
        }

        if (actionType.equals("composing-invoices")) {
            batchButton.setText("Batch Invoices");
            batchButton.setVisible(anySelected);
            updateRentalButton.setVisible(false);
            if (anySelected) {
                batchButton.setText("Batch Invoices");
                batchButton.setVisible(true);
                updateRentalButton.setVisible(false);

                batchButton.setOnAction(event -> {
                    ObservableList<CustomerRental> selectedRentals = dbTableView.getItems().filtered(CustomerRental::isSelected);

                    if (selectedRentals.isEmpty()) {
                        System.out.println("No rentals selected. Cannot compose invoices.");
                        return;
                    }

                    clearAllComposingInvoiceInDB();

                    // Loop through each selected rental and flag it in the database
                    boolean anyUpdated = false;
                    for (CustomerRental rental : selectedRentals) {
                        System.out.println("Processing rental item ID: " + rental.getRentalItemId());
                        boolean updateSuccess = flagComposingInvoiceInDB(rental.getRentalItemId());

                        if (updateSuccess) {
                            anyUpdated = true;
                            rental.setWritingInvoice(true);
                        } else {
                            System.out.println("Failed to update rental item ID: " + rental.getRentalItemId());
                        }
                    }

                    // Show confirmation if any updates were successful
                    if (anyUpdated) {
                        System.out.println("At least one rental item was updated successfully.");

                        // Path to the Python script
                        String scriptPath = "C:\\Users\\maxhi\\OneDrive\\Documents\\Quickbooks\\QBProgram Development\\Invoice Creating\\.venv\\Scripts\\make_invoices_from_queue.py";

                        // Execute the Python script directly without passing data
                        executePythonScript(scriptPath);

                        prepareSecondaryButtonForInvoices();
                        secondInProcessButton.setVisible(true);
                        runSDKTool();
                    } else {
                        System.out.println("No rental items were updated.");
                    }

                    resetCheckboxes();
                    dbTableView.refresh();
                });

                secondInProcessButton.setOnAction(event -> {
                    ObservableList<CustomerRental> selectedRentals = dbTableView.getItems().filtered(CustomerRental::isSelected);

                    if (selectedRentals.isEmpty()) {
                        System.out.println("No rentals selected. Cannot compose invoices.");
                        return;
                    }

                    clearAllComposingInvoiceInDB();

                    // Loop through each selected rental and flag it in the database
                    boolean anyUpdated = false;
                    for (CustomerRental rental : selectedRentals) {
                        System.out.println("Processing rental item ID: " + rental.getRentalItemId());
                        boolean updateSuccess = flagComposingInvoiceInDB(rental.getRentalItemId());

                        if (updateSuccess) {
                            anyUpdated = true;
                            rental.setWritingInvoice(true);
                        } else {
                            System.out.println("Failed to update rental item ID: " + rental.getRentalItemId());
                        }
                    }

                    // Show confirmation if any updates were successful
                    if (anyUpdated) {
                        System.out.println("At least one rental item was updated successfully.");

                        // Path to the Python script
                        String scriptPath = "C:\\Users\\maxhi\\OneDrive\\Documents\\Quickbooks\\QBProgram Development\\Invoice Creating\\.venv\\Scripts\\make_invoices_from_queue.py";

                        // Execute the Python script directly without passing data
                        executePythonScript(scriptPath);

                        prepareSecondaryButtonForInvoices();
                        secondInProcessButton.setVisible(true);
                        runSDKTool();
                    } else {
                        System.out.println("No rental items were updated.");
                    }

                    resetCheckboxes();
                    dbTableView.refresh();
                });
            }

        }
    }


    private void handleExpandSelection(int index) {
        MaxReachPro.setRentalForExpanding(dbTableView.getItems().get(index));
        try {
            MaxReachPro.loadScene("/fxml/expand.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSelectableCheckboxes(boolean visible, String actionType) {
        lastActionType = actionType;
        boolean shouldShow = "dropping-off".equals(actionType) ||
                "calling-off".equals(actionType) ||
                "picking-up".equals(actionType) ||
                "composing-invoices".equals(actionType) ||
                "composing-contracts".equals(actionType) ||
                "deleting".equals(actionType);

        this.shouldShowCheckboxes = shouldShow && visible; // Set the class-level variable
        dbTableView.refresh();
    }

    public void showExpandIcons(boolean visible) {
        if (visible) {
            lastActionType = "expanding";
        } else {
            lastActionType = null;
        }
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
        // Extract initials from the newDriver argument
        String driverInitials = extractDriverInitials(newDriver);

        // Update both the driver and driver_initial columns
        String updateQuery = "UPDATE rental_items SET driver = ?, driver_initial = ? WHERE rental_item_id = ?";

        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {

            // Set parameters for the prepared statement
            preparedStatement.setString(1, newDriver);
            preparedStatement.setString(2, driverInitials);
            preparedStatement.setInt(3, rentalItemId);

            // Execute the update
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String extractDriverInitials(String driverName) {
        // Split the driverName into parts and concatenate the first letter of each part
        StringBuilder initials = new StringBuilder();
        String[] parts = driverName.split("\\s+"); // Split by whitespace
        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(part.charAt(0));
            }
        }
        return initials.toString().toUpperCase(); // Convert to uppercase for consistency
    }

    private String getDaySuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }

    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() != 10) {
            throw new IllegalArgumentException("Input must be a 10-digit number.");
        }

        return "(" + phoneNumber.substring(0, 3) + ")-" + phoneNumber.substring(3, 6) + "-" + phoneNumber.substring(6, 10);
    }

    private void clearAllComposingInvoiceInDB() {
        System.out.println("Attempting to clear all composing invoices in the database.");
        String updateQuery = "UPDATE rental_items SET composing_invoice = 0 WHERE composing_invoice = 1";
        boolean success = false;

        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Update successful for all composing invoices.");
                success = true;
            } else {
                System.out.println("No rows updated for composing invoices.");
            }

        } catch (SQLException e) {
            System.err.println("SQL exception while updating composing invoices: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean flagComposingInvoiceInDB(int rentalItemId) {
        System.out.println("Attempting to update rental item ID: " + rentalItemId);
        String updateQuery = "UPDATE rental_items SET composing_invoice = 1 WHERE rental_item_id = ?";
        boolean success = false;


        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {


            statement.setInt(1, rentalItemId);
            int rowsUpdated = statement.executeUpdate();


            if (rowsUpdated > 0) {
                System.out.println("Update successful for rental item ID: " + rentalItemId);
                success = true;
            } else {
                System.out.println("No rows updated for rental item ID: " + rentalItemId);
            }


        } catch (SQLException e) {
            System.err.println("SQL exception while updating rental item ID " + rentalItemId + ": " + e.getMessage());
            e.printStackTrace();
        }


        return success;
    }

    private void executePythonScript(String scriptPath) {
        try {
            // Prepare the command to execute the Python script
            ProcessBuilder processBuilder = new ProcessBuilder("python", scriptPath);
            processBuilder.redirectErrorStream(true);

            // Start the Python process
            Process process = processBuilder.start();

            // Capture and print output from the Python script
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Python script output: " + line);
                }
            }

            // Wait for the process to finish
            int exitCode = process.waitFor();
            System.out.println("Python script executed with exit code: " + exitCode);

            if (exitCode != 0) {
                System.err.println("Python script returned an error.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runSDKTool() {
        // Debug output path
        System.out.println("SDK Path: " + SDK_PATH);

        File sdkToolFile = new File(SDK_PATH);
        if (!sdkToolFile.exists()) {
            return;
        }


        new Thread(() -> {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(SDK_PATH);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void prepareSecondaryButtonForInvoices(){
        Image image = new Image(getClass().getResourceAsStream("/images/send-to-quickbooks.png"));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(17);
        imageView.setFitWidth(20);

        HBox hbox = new HBox();
        hbox.getChildren().addAll(new Label("Send to Quickbooks  "), imageView);

        secondInProcessButton.setGraphic(hbox);
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

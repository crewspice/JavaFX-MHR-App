package com.MaxHighReach;

import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.control.Tooltip;

import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.sql.*;


import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.element.LineSeparator;

import java.io.FileNotFoundException;
import java.util.*;

public class DBController extends BaseController {

    @FXML
    private Button backButton;
    @FXML
    private Button editDriverButton;
    @FXML
    private Button droppingOffButton;
    @FXML
    private Button callingOffButton;
    @FXML
    private Button pickingUpButton;
    @FXML
    private Button updateRentalButton;
    @FXML
    private Button composeInvoicesButton;
    @FXML
    private Button composeContractsButton;
    @FXML
    private Button refreshDataButton;
    private final Tooltip composeContractsTooltip = new Tooltip("Compose Contracts");
    private final Tooltip assignDriverTooltip = new Tooltip("Assign Driver");
    private final Tooltip droppingOffTooltip = new Tooltip("Record Drop Off");
    private final Tooltip callingOffTooltip = new Tooltip("Record Call Off");
    private final Tooltip pickingUpTooltip = new Tooltip("Record Pick Up");
    private final Tooltip composeInvoicesTooltip = new Tooltip("Compose Invoices");
    private final Tooltip refreshDataTooltip = new Tooltip("Refresh Table");
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TableView<CustomerRental> dbTableView;
    private TableColumn<CustomerRental, Boolean> statusColumn = new TableColumn<>();
    private TableColumn<CustomerRental, String> customerAndAddressColumn = new TableColumn<>();
    private TableColumn<CustomerRental, String> thirdColumn = new TableColumn<>();
    private TableColumn<CustomerRental, String> fourthColumn = new TableColumn<>();
    private TableColumn<CustomerRental, String> fifthColumn = new TableColumn<>();

    @FXML
    private Label loadingLabel;
    @FXML
    private ComboBox<String> filterComboBox;

    @FXML
    private ComboBox<String> filterComboBoxTwo;
    @FXML
    private TableView<CustomerRental> dbTableViewTwo;
    @FXML
    private VBox viewsTilePane;
    @FXML
    private VBox leftSideVboxStatusView;
    private ToggleGroup statusViewToggleGroup = new ToggleGroup();
    @FXML
    private DatePicker rightSideDatePickerOne;
    @FXML
    private DatePicker rightSideDatePickerTwo;
    @FXML
    private VBox rightSideVboxStatusView;
    private VBox latestRightSideVbox;
    private VBox latestLeftSideVbox;
    @FXML
    private VBox leftSideVboxCustomerView;
    @FXML
    private VBox rightSideVboxCustomerView;
    private ToggleGroup customerViewToggleGroup = new ToggleGroup();
    @FXML
    private ComboBox<String> customerComboBox;
    private ObservableList<Customer> customers = FXCollections.observableArrayList();

    private ObservableList<CustomerRental> ordersList = FXCollections.observableArrayList();
    private boolean shouldShowCheckboxes = false;
    private boolean isDriverEditMode = false;
    private String lastActionType;
    private boolean isDroppingOff = false;
    private boolean isPickingUp = false;

    private Timeline fadeOutTimeline;
// globals before the retry
    private Map<String, Integer> driverCounts = new HashMap<>();
    private ObservableList<String> driverInitials = FXCollections.observableArrayList("A", "J", "I", "B", "JC", "K");


// retry globals
    private ObservableList<String> potentialInitials = FXCollections.observableArrayList();
    private ObservableList<String> currentViewInitials = FXCollections.observableArrayList();


    private Map<String, List<CustomerRental>> groupedRentals = new HashMap<>();
    private Map<String, Integer> driverSequenceMap = new HashMap<>();

    private String initialsErrorCode;
    private String initialsErrorPointerKey;

    private javafx.scene.text.Text liftTypeText = new javafx.scene.text.Text();


    @FXML
    public void initialize() {
        super.initialize();


        dbTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        dbTableView.setOnMouseEntered(event -> {
            showScrollBars(dbTableView);
            resetFadeOutTimeline(dbTableView, fadeOutTimeline);
        });

        // Hide scroll bars when mouse exits the TableView
        dbTableView.setOnMouseExited(event -> {
            hideScrollBars(dbTableView);
        });

        // Create the fade out timeline
        fadeOutTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            hideScrollBars(dbTableView);
        }));

        // Show scroll bars when scrolling
        dbTableView.setOnScroll(event -> {
            showScrollBars(dbTableView);
            resetFadeOutTimeline(dbTableView, fadeOutTimeline);
        });


        customerAndAddressColumn.setCellFactory(column -> new TableCell<CustomerRental, String>() {
            private final Label contentLabel = new Label();  // Address and customer label
            private final Label liftTypeLabel = new Label(); // Lift type label
            private final StackPane overlayPane = new StackPane(); // Overlay for layout
            private final DropShadow glowEffect = new DropShadow(); // Glow effect for lift type


            {
                // Configure the glowing effect for the liftTypeLabel
                glowEffect.setRadius(10);
                glowEffect.setSpread(0.5);
                liftTypeLabel.setEffect(glowEffect);
                liftTypeLabel.getStyleClass().add("lift-type-in-corner");
                liftTypeLabel.setStyle("-fx-font-weight: bold;");
                liftTypeLabel.setFont(Font.font("Patrick Hand"));


                // Ensure liftTypeLabel never collapses
                liftTypeLabel.setMinWidth(Region.USE_PREF_SIZE);
                liftTypeLabel.setMinHeight(Region.USE_PREF_SIZE);


                // Create glowing animation for the lift type label
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


                // Configure the overlayPane using StackPane
                overlayPane.getChildren().addAll(contentLabel, liftTypeLabel);


                // Align liftTypeLabel to the bottom-right corner
                StackPane.setAlignment(liftTypeLabel, Pos.BOTTOM_RIGHT);
                StackPane.setAlignment(contentLabel, Pos.CENTER_LEFT);


                // Set a high priority for liftTypeLabel so it is always visible
                StackPane.setMargin(liftTypeLabel, new Insets(0, 5, 5, 0)); // Adjust padding if necessary
            }


            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);


                if (empty || getTableRow() == null) {
                    setGraphic(null); // Clear for empty cells
                } else {
                    CustomerRental rental = getTableRow().getItem();
                    if (rental != null) {
                        // Set the address and customer text
                        contentLabel.setText(
                            rental.getName() + "\n" +
                            rental.getAddressBlockOne() + "\n" +
                            rental.getAddressBlockTwo() + "\n" +
                            rental.getAddressBlockThree()
                        );


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


        thirdColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryDate"));

        thirdColumn.setCellFactory(column -> new TableCell<CustomerRental, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);


                if (empty || item == null) {
                    setText(null);  // Clear text for empty cells
                    setGraphic(null);  // Clear graphic for empty cells
                } else {
                    CustomerRental rental = getTableView().getItems().get(getIndex());
                    LocalDate date = LocalDate.parse(item); // Assuming yyyy-MM-dd format


                    // Format month and day components
                    String month = date.getMonth().toString().substring(0, 3).toUpperCase(); // 3-letter month abbreviation
                    String day = String.format("%02d", date.getDayOfMonth()); // Two-digit day


                    // Create the vertical text with bullet separator
                    String verticalDate = String.join("\n", month.split("")) +
                                          "\n\u2022\n" +  // Unicode bullet separator
                                          String.join("\n", day.split(""));


                    // Set the text for the cell
                    setText(verticalDate);


                    // Adjust line spacing and alignment
                    setStyle("-fx-line-spacing: -6; -fx-alignment: center; -fx-font-size: 11;");
                }
            }
        });

        // Date formatting without leading zeros for month and day (M/d format)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d");

        // Customize deliveryDateColumn to show date in M/d format


        // Reusing the old column for delivery time
        fourthColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryDate"));


        fourthColumn.setCellFactory(column -> new TableCell<CustomerRental, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);


                if (empty || item == null) {
                    setText(null);  // Clear text for empty cells
                    setGraphic(null);  // Clear graphic for empty cells
                } else {
                    CustomerRental rental = getTableView().getItems().get(getIndex());
                    String time = rental.getDeliveryTime(); // Get delivery time


                    // Create a vertical text representation for time
                    String verticalTime = String.join("\n", time.split("")); // Split each character


                    // Use a Label to display the vertical time
                    Label timeLabel = new Label(verticalTime);
                    timeLabel.setStyle("-fx-line-spacing: -6; -fx-font-size: 12;"); // Adjust font size if needed


                    // Create a VBox for vertical centering
                    VBox vBox = new VBox(timeLabel);
                    vBox.setAlignment(Pos.CENTER); // Center align the VBox
                    vBox.setPrefHeight(getHeight()); // Make the VBox the same height as the cell


                    setGraphic(vBox); // Set the VBox as the graphic for the cell
                }
            }
        });


        // Comparator for sorting, if needed
        fourthColumn.setComparator((time1, time2) -> {
            // Custom comparison logic for time can be added here if necessary
            return time1.compareTo(time2);  // Example: simple string comparison
        });


        //  deliveryTimeColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryTime"));
        fifthColumn.setCellValueFactory(new PropertyValueFactory<>("driver"));
        fifthColumn.setCellFactory(column -> new TableCell<CustomerRental, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
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
                    if (!image.isError()) {
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(19);  // Set smaller size
                        imageView.setFitHeight(19);  // Set smaller size

                        vBox.getChildren().add(imageView);

                        setGraphic(vBox);
                    } else {
                        setGraphic(null);
                    }
                } catch (Exception e) {
                    setGraphic(null);
                }
            }

            // Method to display driver initials and icon
            private void displayDriverWithIcon(String driverInitials) {
                VBox vBox = new VBox();
                vBox.setSpacing(5);  // Space between initials and the icon
                vBox.setAlignment(Pos.BOTTOM_CENTER);  // Align icon at the bottom

                // Create a label for driver initials
                Label initialsLabel = new Label(driverInitials);
                initialsLabel.setTextFill(Color.BLACK); // Ensure text is visible

                // Create an ImageView for the driver icon
                try {
                    String imagePath = "/images/driver-icon.png";
                    Image image = new Image(getClass().getResourceAsStream(imagePath));
                    if (!image.isError()) {
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(19);  // Smaller image (24px - 5px)
                        imageView.setFitHeight(19);
                        vBox.getChildren().addAll(initialsLabel, imageView);  // Add both to VBox
                        setGraphic(vBox);
                    } else {
                        setText(driverInitials);
                        setTextFill(Color.BLACK); // Ensure text is visible
                        setGraphic(null);
                    }
                } catch (Exception e) {
                    setText(driverInitials);  // Fallback to just initials
                    setTextFill(Color.BLACK); // Ensure text is visible
                    setGraphic(null);
                }
            }
        });
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
                } else {
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


        // Disable resizing
     //   selectColumn.setResizable(false);
       //idColumn.setResizable(false);
        customerAndAddressColumn.setResizable(false);
      //  nameColumn.setResizable(false);
        fourthColumn.setResizable(false);
        fifthColumn.setResizable(false);
        statusColumn.setResizable(false);

        // Initialize filter combo box
        filterComboBox.setItems(FXCollections.observableArrayList(
                "All Rentals",
                "Today's Rentals",
                "Yesterday's Rentals",
                "Custom Date Range",
                "Ended Rentals"
        ));
        filterComboBox.setValue("All Rentals"); // Default selection
        for (String initial : new String[]{"JD", "AB", "MG", "CN"}) {
                driverCounts.put(initial, 1); // Start with count 1 for each driver
            }


        // Enable table editing
        dbTableView.setEditable(true);



        // Load data initially
        showLoadingMessage(true);
        loadDataAsync("All Rentals");

        // Handle filter changes
        filterComboBox.setOnAction(event -> handleFilterSelection());

        if (updateRentalButton != null) {
            updateRentalButton.setVisible(false); // Start with the button hidden
        } else {
            System.err.println("updateRentalButton is not injected!");
        }
        hideCheckboxes(); // Call the method to hide checkboxes initially

        createCustomTooltip(editDriverButton, 38, 10, assignDriverTooltip);
        createCustomTooltip(droppingOffButton, 38, 10, droppingOffTooltip);
        createCustomTooltip(callingOffButton, 38, 10, callingOffTooltip);
        createCustomTooltip(pickingUpButton, 38, 10, pickingUpTooltip);
        createCustomTooltip(composeInvoicesButton, 38, 10, composeInvoicesTooltip);
        createCustomTooltip(composeContractsButton, 38, 10, composeContractsTooltip);
        createCustomTooltip(refreshDataButton, 38, 10, refreshDataTooltip);

        for (javafx.scene.Node node : leftSideVboxStatusView.getChildren()) {
            node.getStyleClass().add("lift-type-button-rotating");
            if (node instanceof ToggleButton toggleButton) {
                toggleButton.setToggleGroup(statusViewToggleGroup);

                toggleButton.setOnAction(event -> {
                    if (toggleButton.isSelected()) {
                        System.out.println("Selected: " + toggleButton.getText());
                        toggleButton.getStyleClass().removeAll("lift-type-button-rotating");
                        toggleButton.getStyleClass().add("lift-type-button-stopped");
                        statusViewToggleGroup.selectToggle(toggleButton);

                    }
                });
            }
        }

        loadCustomers();
        for (javafx.scene.Node node : rightSideVboxCustomerView.getChildren()) {
            node.getStyleClass().add("lift-type-button-rotating");
            if (node instanceof ToggleButton toggleButton) {
                toggleButton.setToggleGroup(customerViewToggleGroup);

                toggleButton.setOnAction(event -> {
                    if (toggleButton.isSelected()) {
                        System.out.println("Selected: " + toggleButton.getText());
                        toggleButton.getStyleClass().removeAll("lift-type-button-rotating");
                        toggleButton.getStyleClass().add("lift-type-button-stopped");
                        customerViewToggleGroup.selectToggle(toggleButton);

                    }
                });
            }
        }

    }

    // Method to load data asynchronously from the database
    private void loadDataAsync(String filter) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                loadData(filter);
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    showLoadingMessage(false);
                    dbTableView.setItems(ordersList); // Update the table
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showLoadingMessage(false);
                    loadingLabel.setText("Failed to load data.");
                });
            }
        };
        new Thread(task).start();
    }

    private void loadData(String filter) {
        ordersList.clear();
        groupedRentals.clear();
        currentViewInitials.clear();
        String query = "SELECT * FROM customers " +
               "JOIN rental_orders ON customers.customer_id = rental_orders.customer_id " +
               "JOIN rental_items ON rental_orders.rental_order_id = rental_items.rental_order_id";

        dbTableView.getColumns().clear();

        // Modify query based on filter
        switch (filter) {
            case "Today's Rentals":
                query += " WHERE DATE(rental_orders.delivery_date) = CURDATE()"; // Adjusted to order_date
                break;
            case "Yesterday's Rentals":
                query += " WHERE DATE(rental_orders.delivery_date) = CURDATE() - INTERVAL 1 DAY"; // Adjusted to order_date
                RentalTableView caseTableView = new RentalTableView() {
                };
                dbTableView = caseTableView.getTableView();
                break;
            case "Custom Date Range":
                // Implement custom date range logic here if needed
                break;
            case "Ended Rentals":
                query += " WHERE rental_orders.order_status = 'Picked Up'";
                break;
            case "All Rentals":
                // No additional filtering
                dbTableView.getColumns().addAll(statusColumn, customerAndAddressColumn, thirdColumn, fourthColumn, fifthColumn);
                break;
        }



        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/practice_db", "root", "SQL3225422!a");
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String name = resultSet.getString("customer_name");
                String deliveryDate = resultSet.getString("delivery_date");
                String driver = resultSet.getString("driver");
                String status = resultSet.getString("item_status");
                int rental_id = resultSet.getInt("rental_order_id");
                int rental_item_id = resultSet.getInt("rental_item_id");
                String deliveryTime = resultSet.getString("delivery_time"); // Now from rental_items
                String liftType = resultSet.getString("lift_type");
                String siteName = resultSet.getString("site_name");
                String streetAddress = resultSet.getString("street_address");
                String poNumber = resultSet.getString("po_number");

                CustomerRental rental = new CustomerRental("0", name, deliveryDate, deliveryTime, driver != null ? driver : "", status != null ? status : "Unknown", 999999, rental_id);
                rental.setRentalItemId(rental_item_id);
                rental.setLiftType(liftType);
                rental.setAddressBlockOne(siteName);
                rental.setAddressBlockTwo(streetAddress);
                rental.splitAddressBlockTwo();
                rental.setPoNumber(poNumber);
                ordersList.add(rental);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        fixCurrentViewInitials(0);
    }

    private void showScrollBars(TableView<CustomerRental> dbTableView) {
        // Show scroll bars
        setScrollBarVisibility(dbTableView, true);
        resetFadeOutTimeline(dbTableView, fadeOutTimeline); // Reset the fade out timer
    }

    private void hideScrollBars(TableView<CustomerRental> dbTableView) {
        // Hide scroll bars
        setScrollBarVisibility(dbTableView, false);
    }

    private void setScrollBarVisibility(TableView<CustomerRental> dbTableView, boolean isVisible) {
        ScrollBar verticalScrollBar = (ScrollBar) dbTableView.lookup(".scroll-bar:vertical");
        ScrollBar horizontalScrollBar = (ScrollBar) dbTableView.lookup(".scroll-bar:horizontal");

        if (verticalScrollBar != null) {
            verticalScrollBar.setVisible(isVisible);
        }
        if (horizontalScrollBar != null) {
            horizontalScrollBar.setVisible(isVisible);
        }
    }

    private void resetFadeOutTimeline(TableView<CustomerRental> dbTableView, Timeline fadeOutTimeline) {
        // Stop any existing timeline
        fadeOutTimeline.stop();
        // Show scroll bars and restart the fade out timer
        setScrollBarVisibility(dbTableView, true);
        fadeOutTimeline.playFromStart();
    }

    private Tooltip createCustomTooltip(Button button, double xOffset, double yOffset, Tooltip tooltip) {
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setHideDelay(Duration.ZERO);

        button.setOnMouseEntered(event -> {
            // Get the button's screen position
            double buttonX = button.localToScreen(button.getBoundsInLocal()).getMinX();
            double buttonY = button.localToScreen(button.getBoundsInLocal()).getMaxY();
            // Show tooltip with an offset relative to the button
            tooltip.show(button, buttonX + xOffset, buttonY + yOffset);
        });

        button.setOnMouseExited(event -> tooltip.hide());

        return tooltip;
    }



    /**
     * Activates the button's selected state.
     */
    private void visuallySelectSidebarButton(Button button) {
        refreshButtonStyles(button);
        button.getStyleClass().add("sidebar-button-active"); // Apply active class
    }

    /**
     * Deactivates the button's state to inactive.
     */
    private void visuallyUnselectSidebarButton(Button button) {
        refreshButtonStyles(button);
        button.getStyleClass().add("sidebar-button-inactive"); // Apply inactive class
    }

    /**
     * Ensures the button's style is refreshed properly.
     */
    private void refreshButtonStyles(Button button) {
        button.getStyleClass().removeAll("sidebar-button-active", "sidebar-button-inactive");
        button.setDisable(true); // Temporarily disable to force CSS refresh
        button.applyCss();       // Force re-apply CSS
        button.setDisable(false); // Re-enable after refreshing
    }




    private void updateDriverInitials() {
        driverInitials.clear();
        for (Map.Entry<String, Integer> entry : driverCounts.entrySet()) {
            String driver = entry.getKey();
            int count = entry.getValue();
            driverInitials.add(driver + "-" + count);
        }
    }

    // Handle dropdown selection for filtering
    @FXML
    private void handleFilterSelection() {
        String selectedFilter = filterComboBox.getValue();
        showLoadingMessage(true);
        loadDataAsync(selectedFilter);
    }


    @FXML
    private void handleAssignDrivers() {

        if ("assigning-drivers".equals(lastActionType)) {
            lastActionType = null;
            updateRentalButton.setVisible(false); // Call this method to hide checkboxes

            fifthColumn.setCellFactory(column -> new TableCell<CustomerRental, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
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
                        if (!image.isError()) {
                            ImageView imageView = new ImageView(image);
                            imageView.setFitWidth(19);  // Set smaller size
                            imageView.setFitHeight(19);  // Set smaller size

                            vBox.getChildren().add(imageView);

                            setGraphic(vBox);
                        } else {
                            setGraphic(null);
                        }
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }

                // Method to display driver initials and icon
                private void displayDriverWithIcon(String driverInitials) {
                    VBox vBox = new VBox();
                    vBox.setSpacing(5);  // Space between initials and the icon
                    vBox.setAlignment(Pos.BOTTOM_CENTER);  // Align icon at the bottom

                    // Create a label for driver initials
                    Label initialsLabel = new Label(driverInitials);
                    initialsLabel.setTextFill(Color.BLACK); // Ensure text is visible

                    // Create an ImageView for the driver icon
                    try {
                        String imagePath = "/images/driver-icon.png";
                        Image image = new Image(getClass().getResourceAsStream(imagePath));
                        if (!image.isError()) {
                            ImageView imageView = new ImageView(image);
                            imageView.setFitWidth(19);  // Smaller image (24px - 5px)
                            imageView.setFitHeight(19);
                            vBox.getChildren().addAll(initialsLabel, imageView);  // Add both to VBox
                            setGraphic(vBox);
                        } else {
                            setText(driverInitials);
                            setTextFill(Color.BLACK); // Ensure text is visible
                            setGraphic(null);
                        }
                    } catch (Exception e) {
                        setText(driverInitials);  // Fallback to just initials
                        setTextFill(Color.BLACK); // Ensure text is visible
                        setGraphic(null);
                    }
                }
            });
        } else {

            // Switch to driver edit mode with ComboBox
            fifthColumn.setCellFactory(column -> new TableCell<CustomerRental, String>() {
            private final ComboBox<String> comboBox = new ComboBox<>();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getItem() == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

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
                        System.out.println("Committed Edit: New Driver for Rental: " + newValue);
                        updateDriverInDatabase(rental.getRentalItemId(), newValue);
                    }
                }
            }
        });

        isDriverEditMode = true;
        updateRentalButton.setVisible(true);
        System.out.println("Driver assignment mode activated.");
        lastActionType = "assigning-drivers";

        }
    }

    private boolean isInitialsSetValid() {
        groupedRentals.clear();
        currentViewInitials.clear();  // Reset to avoid stale data


        // Populate the groupedRentals map from ordersList
        for (CustomerRental rental : ordersList) {
            String driver = rental.getDriver();
            String[] parts = driver.split("(?<=\\D)(?=\\d)"); // Split into the initial and number parts
            String initial = parts[0];
            int number = parts.length > 1 ? Integer.parseInt(parts[1]) : 0; // Default to 0 if no number present


            groupedRentals.computeIfAbsent(initial, k -> new ArrayList<>()).add(rental);
            currentViewInitials.add(driver);
        }


        // Display the grouped rentals for debugging purposes
        System.out.println("Grouped Rentals:");
        groupedRentals.forEach((initial, rentals) -> {
            System.out.println("Initial: " + initial);
            rentals.forEach(rental ->
                System.out.println("   Rental ID: " + rental.getRentalItemId() + ", Driver: " + rental.getDriver())
            );
        });


        // Create a set of valid initials from the driverInitials list
        Set<String> validInitials = new HashSet<>(driverInitials);
        System.out.println("Valid Initials: " + validInitials);


        Map<String, Set<Integer>> initialIntegerMap = new HashMap<>();


        // Validate each rental's driver value
        for (CustomerRental rental : ordersList) {
            String driver = rental.getDriver();
            System.out.println("Processing Driver: " + driver);


            if (driver.equals("x")) {
                System.out.println("Skipping 'x' (always valid)");
                continue;
            }


            // Split the driver string into initial and number parts
            String[] parts = driver.split("(?<=\\D)(?=\\d)");
            if (parts.length != 2) {
                System.out.println("Invalid Format: " + driver);
                initialsErrorCode = "format";
                initialsErrorPointerKey = parts[0];
                return false;
            }


            String initial = parts[0];
            String numberPart = parts[1];


            System.out.println("Initial: " + initial + ", Number: " + numberPart);


            // Check if the initial is valid
            if (!validInitials.contains(initial)) {
                System.out.println("Invalid Initial: " + initial);
                initialsErrorCode = "out-of-bounds";
                initialsErrorPointerKey = initial;
                return false;
            }


            // Parse and validate the number
            int number;
            try {
                number = Integer.parseInt(numberPart);
                if (number <= 0) {
                    System.out.println("Invalid Number (<= 0): " + number);
                    return false;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid Number Format: " + numberPart);
                return false;
            }


            // Check for duplicate numbers within the same initial group
            Set<Integer> numberSet = initialIntegerMap.computeIfAbsent(initial, k -> new HashSet<>());
            if (numberSet.contains(number)) {
                System.out.println("Duplicate Number for Initial " + initial + ": " + number);
                return false;
            }


            numberSet.add(number);
            System.out.println("Added Number " + number + " to Initial " + initial);
        }


        // Check if all numbers are sequential for each initial
        for (Map.Entry<String, Set<Integer>> entry : initialIntegerMap.entrySet()) {
            String initial = entry.getKey();
            Set<Integer> numberSet = entry.getValue();
            System.out.println("Checking Sequence for Initial: " + initial + ", Numbers: " + numberSet);


            for (int i = 1; i <= numberSet.size(); i++) {
                if (!numberSet.contains(i)) {
                    System.out.println("Non-Sequential Numbers for Initial " + initial);
                    initialsErrorCode = "non-sequential";
                    initialsErrorPointerKey = initial;
                    return false;
                }
            }
        }


        System.out.println("All checks passed.");
        return true; // All checks passed
    }














    private void repairLineOfInitials(String problemInitial) {
        System.out.println("Repairing rentals for initial: " + problemInitial);
        List<CustomerRental> rentalsForDriver = groupedRentals.get(problemInitial);


        if (rentalsForDriver == null || rentalsForDriver.isEmpty()) {
            System.out.println("No rentals found for initial: " + problemInitial);
            return;
        }


        System.out.println("Initials Error Code: " + initialsErrorCode);
        switch (initialsErrorCode) {
            case "format":
                System.out.println("Handling 'format' error for initial: " + problemInitial);
                for (CustomerRental rental : rentalsForDriver) {
                    String driver = rental.getDriver();
                    String[] parts = driver.split("(?<=\\D)(?=\\d)");
                    System.out.println("Processing Driver: " + driver + ", Parts: " + Arrays.toString(parts));


                    if (parts.length == 1) { // No number part
                        int maxNumber = 0;
                        System.out.println("Finding max number for initial: " + problemInitial);


                        for (CustomerRental siblingRental : rentalsForDriver) {
                            String siblingDriver = siblingRental.getDriver();
                            String[] siblingParts = siblingDriver.split("(?<=\\D)(?=\\d)");


                            if (siblingParts.length > 1) {
                                int siblingNumber = Integer.parseInt(siblingParts[1]);
                                maxNumber = Math.max(maxNumber, siblingNumber);
                                System.out.println("Sibling Driver: " + siblingDriver + ", Number: " + siblingNumber);
                            }
                        }


                        String newDriver = parts[0] + (maxNumber + 1);
                        rental.setDriver(newDriver);
                        System.out.println("Updated Driver: " + newDriver);
                        break;
                    }
                }
                break;


            case "out-of-bounds":
                System.out.println("Handling 'out-of-bounds' error for initial: " + problemInitial);
                CustomerRental rental = rentalsForDriver.get(0);
                rental.setDriver("x");
                System.out.println("Set Driver to 'x' for Rental ID: " + rental.getRentalItemId());
                break;


            case "next-case":
                System.out.println("Handling 'next-case'. No changes made.");
                break;


            default:
                System.out.println("Unknown initials error code: " + initialsErrorCode);
                break;
        }
    }


    private void fixCurrentViewInitials(int tryCounter) {
        if (tryCounter >= 10) {
            System.out.println("Reached maximum retry limit (10). Exiting.");
            return;
        } else {
            tryCounter++;
        }
        System.out.println("Try counter: " + tryCounter);


        if (isInitialsSetValid()) {
            System.out.println("Initials set is valid. No further fixes needed.");
            return;
        }


        System.out.println("Initials set is invalid. Repairing initials for: " + initialsErrorPointerKey);
        repairLineOfInitials(initialsErrorPointerKey);

        System.out.println("Rechecking initials set validity...");
        fixCurrentViewInitials(tryCounter); // Retry if still not valid
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

        int newIntSeqNum = newStringSeqNumber.isEmpty() ? 0 : Integer.parseInt(newStringSeqNumber);
        int oldIntSeqNum = oldSeqNumber.isEmpty() ? 0 : Integer.parseInt(oldSeqNumber);

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


    private void initialsFallbackFailsafe() {
        // Iterate through all the rentals in the ordersList and remove numeric suffixes from drivers
        for (CustomerRental rental : ordersList) {
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


    private void hideCheckboxes() {
        // Set the checkboxes to not be visible
       /* selectColumn.setCellFactory(tc -> new TableCell<CustomerRental, Boolean>() {
            private final CheckBox checkBox = new CheckBox();

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    checkBox.setVisible(false);
                    checkBox.setSelected(getTableView().getItems().get(getIndex()).isSelected());
                    setGraphic(checkBox);
                } else {
                    setGraphic(null);
                }
            }
        });*/
        dbTableView.refresh();
    }

   @FXML
    private void handleDroppingOff(ActionEvent event) {
        if ("dropping-off".equals(lastActionType)) { // Use .equals() to compare strings
            lastActionType = null;
            hideCheckboxes();
            showSelectableCheckboxes(false, lastActionType);
            updateRentalButton.setVisible(false); // Call this method to hide checkboxes

            visuallyUnselectSidebarButton(droppingOffButton);
        } else {

            lastActionType = "dropping-off";
            resetCheckboxes();
            showSelectableCheckboxes(true, lastActionType);
            visuallySelectSidebarButton(droppingOffButton);

        }
    }




    @FXML
    private void handleCallingOff(ActionEvent event) {
        if ("calling-off" == lastActionType) {
            lastActionType = null;
            hideCheckboxes();
            showSelectableCheckboxes(false, lastActionType);
            updateRentalButton.setVisible(false);
            System.out.println("Calling Off button pressed again. Resetting action type.");
        } else {
            lastActionType = "calling-off";
            resetCheckboxes();
            showSelectableCheckboxes(true, lastActionType);
            System.out.println("Calling Off button pressed.");
            return;
        }
    }

    @FXML
    private void handlePickingUp(ActionEvent event) {
        if ("picking-up" == lastActionType) {
            lastActionType = null; // Reset the action type if the button is pressed again
            hideCheckboxes();
            showSelectableCheckboxes(false, lastActionType);
            updateRentalButton.setVisible(false);
   //         System.out.println("Picking Up button pressed again. Resetting action type.");
            updateRentalButton.setVisible(false);
        } else {
            lastActionType = "picking-up";
            resetCheckboxes();
            showSelectableCheckboxes(true, lastActionType);
       //     System.out.println("Picking Up button pressed.");
            return; // Exit if no action type is set
        }

    }

    @FXML
    private void handleComposeInvoices(ActionEvent event) {
        System.out.println("Compose Invoices Button pressed. Current lastActionType: " + lastActionType);

        // Check for the current action type
        if ("creating-invoices".equals(lastActionType)) {
            lastActionType = null; // Reset the action type
            hideCheckboxes(); // Hide the checkboxes
            showSelectableCheckboxes(false, lastActionType);
            updateRentalButton.setVisible(false);
            System.out.println("Resetting action type. Checkboxes hidden.");
            return; // Exit the method
        } else {
            lastActionType = "creating-invoices"; // Set the action type
            resetCheckboxes(); // Deselect all checkboxes first
            showSelectableCheckboxes(true, lastActionType); // Show the checkboxes
            System.out.println("Action type set to 'creating-invoices'. Checkboxes shown.");
        }

    }


    @FXML
    private void handleComposeContracts(ActionEvent event) {
        System.out.println("Compose Contracts Button pressed. Current lastActionType: " + lastActionType);

        if ("creating-contracts" == lastActionType) {
            lastActionType = null; // Reset the action type
            hideCheckboxes(); // Hide the checkboxes
            showSelectableCheckboxes(false, lastActionType);
            updateRentalButton.setVisible(false);
            System.out.println("Resetting action type. Checkboxes hidden.");
            return; // Exit the method
        } else {
            lastActionType = "creating-contracts"; // Set the action type
            resetCheckboxes(); // Deselect all checkboxes first
            showSelectableCheckboxes(true, lastActionType); // Show the checkboxes
            System.out.println("Action type set to 'creating-contracts'. Checkboxes shown.");
        }
    }


    private void updateInvoiceFlagInDB(String customerId, boolean isFlagged) {
        String updateQuery = "UPDATE rentals SET is_flagged = ? WHERE customer_id = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/practice_db", "root", "SQL3225422!a");
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

            statement.setBoolean(1, isFlagged); // Convert boolean to int (1 or 0)
            statement.setString(2, customerId);

            int rowsUpdated = statement.executeUpdate();
            System.out.println("Rows updated: " + rowsUpdated);  // Debugging line
            if (rowsUpdated > 0) {
                System.out.println("Invoice flag updated successfully for customer ID " + customerId);
            } else {
                System.out.println("Failed to update invoice flag for customer ID " + customerId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void resetCheckboxes() {
        // Deselect all checkboxes in the table
        for (CustomerRental order : dbTableView.getItems()) {
            order.setSelected(false);
        }
        dbTableView.refresh(); // Refresh the table view to update the checkbox states
    }


    private void showSelectableCheckboxes(boolean visible, String actionType) {
        boolean shouldShowCheckboxes = "dropping-off".equals(actionType) ||
                                       "calling-off".equals(actionType) ||
                                       "picking-up".equals(actionType) ||
                                       "creating-invoices".equals(actionType) ||
                                        "creating-contracts".equals(actionType);

        this.shouldShowCheckboxes = shouldShowCheckboxes && visible; // Set the class-level variable
        dbTableView.refresh(); // Refresh to update cell rendering


     /*   selectColumn.setCellFactory(tc -> new TableCell<CustomerRental, Boolean>() {
            private final CheckBox checkBox = new CheckBox();

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    checkBox.setVisible(shouldShowCheckboxes && visible);
                    checkBox.setSelected(getTableView().getItems().get(getIndex()).isSelected());
                    setGraphic(checkBox);

                    checkBox.setOnAction(e -> {
                        handleSelection(checkBox.isSelected(), getIndex(), actionType);
                        getTableView().getItems().get(getIndex()).setSelected(checkBox.isSelected());
                        System.out.println("Checkbox at index " + getIndex() + " selected: " + checkBox.isSelected());
                    });
                } else {
                    setGraphic(null);
                }
            }
        });*/
    }

    private long getActiveRentalsCount() {
        return dbTableView.getItems().stream()
                .filter(rental -> "Active".equals(rental.getStatus()))
                .count();
    }

    private void handleSelection(boolean isSelected, int index, String actionType) {
        dbTableView.getItems().get(index).setSelected(isSelected);

        // Show the update button only if at least one row is selected
        boolean anySelected = dbTableView.getItems().stream().anyMatch(CustomerRental::isSelected);
        updateRentalButton.setVisible(anySelected);

    }


    @FXML
    private void handleUpdateRental(ActionEvent event) {
        ObservableList<CustomerRental> selectedRentals = dbTableView.getItems().filtered(CustomerRental::isSelected);
        boolean statusUpdated = false;

        // Handle the 'creating-invoices' action type
        if (lastActionType.equals("creating-invoices")) {
            for (CustomerRental order : selectedRentals) {
                if (order.getStatus().equals("Called Off") || order.getStatus().equals("Picked Up")) {
                    order.setFlagged(true);
                    updateInvoiceFlagInDB(order.getCustomerId(), true); // Flag the order for invoicing
                    System.out.println("Order for " + order.getName() + " flagged for invoicing.");
                    statusUpdated = true;
                    checkAndSwitchScene(statusUpdated);
                } else {
                    System.out.println("Order for " + order.getName() + " cannot be flagged; status is not 'Called Off' or 'Picked Up'.");
                }
            }
        } else if (lastActionType.equals("creating-contracts")) {
            // Handle the driver assignment status updates
            for (CustomerRental order : selectedRentals) {

            }
        } else if (lastActionType.equals("assigning-drivers")) {
            handleAssignDrivers();
            statusUpdated = true;
        } else if (lastActionType.equals("dropping-off")) {
            for (CustomerRental order : selectedRentals) {
                // Mark as dropping-off
                String newStatus = "Active";
                System.out.println("Order for " + order.getName() + " status is:" + order.getStatus());
                if (order.getStatus().equals("Upcoming")) {
                    order.setStatus(newStatus);
                    updateRentalStatusInDB(order.getRentalItemId(), newStatus);  // Sync with DB
                    statusUpdated = true;
                    visuallyUnselectSidebarButton(droppingOffButton);
                    System.out.println("Order for " + order.getName() + " marked as Active.");
                }
            }
            droppingOffButton.setStyle("-fx-background-color: transparent");
        } else if (lastActionType.equals("calling-off")) {
            for (CustomerRental order : selectedRentals) {
                String newStatus = "Called Off";
                if (order.getStatus().equals("Active")) {
                    order.setStatus(newStatus);
                    updateRentalStatusInDB(order.getRentalItemId(), newStatus);  // Sync with DB
                    statusUpdated = true;
                    System.out.println("Order for " + order.getName() + " status updated to 'Called Off'.");
                }
            }
        } else if (lastActionType.equals("picking-up")) {
            // Handle the 'picking-up' action type
            for (CustomerRental order : selectedRentals) {
                String newStatus = "Picked Up";  // Set the status for picking-up
                if (order.getStatus().equals("Called Off")) {
                    order.setStatus(newStatus);
                    updateRentalStatusInDB(order.getRentalItemId(), newStatus);  // Sync with DB
                    statusUpdated = true;
                    System.out.println("Order for " + order.getName() + " status updated to 'Picked Up'.");
                }
            }
        } else {
            // Existing logic for other action types
            for (CustomerRental order : selectedRentals) {
                String newStatus = determineNewStatus(order, lastActionType);
                if (!newStatus.equals(order.getStatus())) {
                    order.setStatus(newStatus);
                    updateRentalStatusInDB(order.getRentalItemId(), newStatus);  // Sync with DB
                    statusUpdated = true;
                    System.out.println("Order for " + order.getName() + " status updated to '" + newStatus + "'.");
                }
            }
        }

        if (statusUpdated) {
            lastActionType = null;
            hideCheckboxes(); // If needed, based on logic
            shouldShowCheckboxes = false;
            resetCheckboxes();
            updateRentalButton.setVisible(false);

        }
        dbTableView.refresh();

    }




    private void updateRentalStatusInDB(int rentalItemId, String newStatus) {
        String updateQuery = "UPDATE rental_items SET item_status = ? WHERE rental_item_id = ?"; // Update table name

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/practice_db", "root", "SQL3225422!a");
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

            statement.setString(1, newStatus);
            statement.setInt(2, rentalItemId);

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                // Update success logic
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void checkAndSwitchScene(boolean statusUpdated) {
        if (statusUpdated) {
            try {
                MaxReachPro.loadScene("/fxml/create_invoices.fxml"); // Replace with your scene path
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void highlightRow(CustomerRental order, String color) {
        TableRow<CustomerRental> row = new TableRow<>();
        row.setStyle("-fx-background-color: " + color + ";");
    }

    private String determineNewStatus(CustomerRental order, String actionType) {
        String currentStatus = order.getStatus();
 //       System.out.println("Determining new status for Order ID " + order.getCustomerId() + ": Current Status = " + currentStatus + ", Action Type = " + actionType);

        if (actionType.equals("dropping-off")) {
            if (currentStatus.equals("Upcoming")) {
                return "Active"; // This should work if currentStatus is "Upcoming"
            }
        } else if (actionType.equals("calling-off")) {
            if (currentStatus.equals("Active")) {
                return "Called Off"; // This should work if currentStatus is "Active"
            }
        } else if (actionType.equals("picking-up")){
            if (currentStatus.equals("Called Off")) {
                return "Picked Up";
            }
        }

        // If no status change applies
 //       System.out.println("No status change applied for Order ID " + order.getCustomerId() + ". Returning current status: " + currentStatus);
        return currentStatus;
    }

    private void createContractPDF(CustomerRental rental, String contractPath) {
        try {
            // Create a PdfWriter instance
            PdfWriter writer = new PdfWriter(contractPath);

            // Create a PdfDocument instance
            PdfDocument pdf = new PdfDocument(writer);

            // Create a Document instance
            Document document = new Document(pdf);

            // Load a font
            PdfFont font = PdfFontFactory.createFont();

            // Add title as a Paragraph with text
            Paragraph paragraph = new Paragraph()
                .add(new Text("Rental Agreement").setFont(font).setFontSize(18).setBold())
                .setFixedPosition(50, 750, 500) // Set x, y, and width
                .setFontColor(ColorConstants.BLACK);

            // Add the paragraph to the document
            document.add(paragraph);

            // Add rental details
            String rentalDetails = "Customer Name: " + rental.getName() + "\n" +
                                   "Rental ID: " + rental.getRentalOrderId() + "\n" +
                                   "Status: " + rental.getStatus() + "\n";

            Paragraph detailsParagraph = new Paragraph(rentalDetails)
                .setFixedPosition(50, 700, 500) // Adjust the position as needed
                .setFontColor(ColorConstants.BLACK);

            // Add the details paragraph to the document
            document.add(detailsParagraph);

            // Add a line separator (correct usage of LineSeparator)
            LineSeparator lineSeparator = new LineSeparator(new SolidLine());
            document.add(lineSeparator);

            // Close the document
            document.close();
            System.out.println("Contract PDF created at: " + contractPath);
        } catch (FileNotFoundException e) {
            System.err.println("Error creating PDF: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error while generating contract PDF: " + e.getMessage());
        }
    }


    // Refresh the table view data
    @FXML
    private void handleRefreshData() {
        showLoadingMessage(true);
        loadDataAsync(filterComboBox.getValue());
    }



    // Handle the back button action
    @FXML
    public void handleBack(ActionEvent event) {
        try {
            MaxReachPro.goBack("/fxml/db.fxml");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Show or hide loading message and buttons
    private void showLoadingMessage(boolean isLoading) {
        if (isLoading) {
            loadingLabel.setText("Loading database...");
            loadingLabel.setVisible(true);
            dbTableView.setVisible(false);
            refreshDataButton.setVisible(false);
        } else {
            loadingLabel.setVisible(false);
            dbTableView.setVisible(true);
            refreshDataButton.setVisible(true);
        }
    }

    @Override
    public double getTotalHeight() {
        boolean hardCode = true;
        if (hardCode) {
            return 65;
        } else {
            double totalHeight = 0;

            return totalHeight;
        }
    }

    @FXML
    private void handleViewSelect(ActionEvent event) {
        ToggleButton selectedButton = (ToggleButton) event.getSource();
        String selectedView = selectedButton.getText();
        if (latestLeftSideVbox != null) {
            latestLeftSideVbox.setVisible(false);
        }
        if (latestRightSideVbox != null) {
            latestRightSideVbox.setVisible(false);
        }
        switch (selectedView) {
            case "Interval":
                break;
            case "Customer":
                leftSideVboxCustomerView.setVisible(true);
                latestLeftSideVbox = leftSideVboxCustomerView;
                rightSideVboxCustomerView.setVisible(true);
                latestRightSideVbox = rightSideVboxCustomerView;
                break;
            case "Status":
                leftSideVboxStatusView.setVisible(true);
                latestLeftSideVbox = leftSideVboxStatusView;
                rightSideVboxStatusView.setVisible(true);
                latestRightSideVbox = rightSideVboxStatusView;
                break;
            case "Driver":
                break;
        }
    }

    private void loadCustomers() {
        String query = "SELECT customer_id, customer_name, email FROM customers";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/practice_db", "root", "SQL3225422!a");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String customerId = resultSet.getString("customer_id");
                String customer_name = resultSet.getString("customer_name");
                String email = resultSet.getString("email");

                // Add to the customer list
                customers.add(new Customer(customerId, customer_name, email));
            }

            for (Customer customer : customers) {
                customerComboBox.getItems().add(customer.getCustomerName());
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


}
package com.MaxHighReach;

import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.control.Tooltip;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
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
    private Button backButton, editDriverButton, droppingOffButton, callingOffButton, pickingUpButton, updateRentalButton, createInvoicesButton, refreshButton, createContractsButton;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TableView<CustomerRental> dbTableView;
  //  @FXML
 //   private TableColumn<CustomerRental, Boolean> selectColumn;
    @FXML
    private TableColumn<CustomerRental, String> idColumn;
    @FXML
    private TableColumn<CustomerRental, String> customerAndAddressColumn;
 //   @FXML
 //   private TableColumn<CustomerRental, String> nameColumn;
    @FXML
    private TableColumn<CustomerRental, String> deliveryDateColumn;
  //  @FXML
   // private TableColumn<CustomerRental, String> deliveryTimeColumn;
    @FXML
    private TableColumn<CustomerRental, String> driverColumn;
    @FXML
    private TableColumn<CustomerRental, Boolean> statusColumn;
    @FXML
    private Label loadingLabel;
    @FXML
    private ComboBox<String> filterComboBox;

    private Map<String, Integer> driverCounts = new HashMap<>();
    private ObservableList<CustomerRental> ordersList = FXCollections.observableArrayList();
    private boolean shouldShowCheckboxes = false;
    private ObservableList<String> driverInitials = FXCollections.observableArrayList("A", "J", "I", "B", "JC", "K");
    private boolean isDriverEditMode = false;
    private String lastActionType;
    private boolean isDroppingOff = false;
    private boolean isPickingUp = false;

    private Timeline fadeOutTimeline;

    private ObservableList<String> potentialInitials = FXCollections.observableArrayList();
    private ObservableList<String> currentViewInitials = FXCollections.observableArrayList();


    @FXML
    public void initialize() {
        super.initialize();

       /* dbTableView.setRowFactory(tv -> new TableRow<CustomerRental>() {
            @Override
            protected void updateItem(CustomerRental item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox();
                    vbox.setSpacing(2);  // Space between the two lines

                    // First line (name and rental details)
                    HBox line1 = new HBox();
                    line1.setSpacing(10);  // Adjust spacing between columns
                    Label nameLabel = new Label(item.getName());
                    Label dateLabel = new Label(item.getDeliveryDate());
                    Label timeLabel = new Label(item.getDeliveryTime());
                    Label driverLabel = new Label(item.getDriver());
                    Label statusLabel = new Label(item.getStatus());

                    line1.getChildren().addAll(nameLabel, dateLabel, timeLabel, driverLabel, statusLabel);

                    // Second line (address)
                    HBox line2 = new HBox();
                    String address = item.getAddressBlockTwo(); // Get the address from the item
                    Label addressLabel = new Label("Address: " + (address != null && !address.isEmpty() ? address : "No address available"));
                    line2.getChildren().add(addressLabel);

                    // Add both lines to the vbox
                    vbox.getChildren().addAll(line1, line2);

                    setGraphic(vbox);
                }
            }
        });*/



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



        /*selectColumn.setCellValueFactory(param -> new SimpleBooleanProperty(param.getValue().isSelected()));
        selectColumn.setCellFactory(column -> new TableCell<CustomerRental, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(item != null && item);
                    setGraphic(checkBox);

                    checkBox.setOnAction(e -> {
                        getTableView().getItems().get(getIndex()).setSelected(checkBox.isSelected());
                    });
                }
            }
        }); */

        // Initialize table columns
       // idColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
     //   nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        customerAndAddressColumn.setCellFactory(column -> new TableCell<CustomerRental, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null) {
                    setText(null);
                } else {
                    CustomerRental rental = getTableRow().getItem();
                    if (rental != null) {
                        setText(rental.getName() + "\n" + rental.getAddressBlockOne() + "\n" + rental.getAddressBlockTwo() + "\n" + rental.getAddressBlockThree());
                    }
                }
            }
        });

        // Date formatting without leading zeros for month and day (M/d format)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d");

        // Customize deliveryDateColumn to show date in M/d format

        deliveryDateColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryDate"));

        deliveryDateColumn.setCellFactory(column -> new TableCell<CustomerRental, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);  // Clear text for empty cells
                    setGraphic(null);  // Clear graphic for empty cells
                } else {
                    CustomerRental rental = getTableView().getItems().get(getIndex());
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
                    LocalDate date = LocalDate.parse(item);
                    String time = rental.getDeliveryTime();
                    String formattedDate = date.format(formatter);
                    String liftType = rental.getShortLiftType();

                    // Calculate the length of the time string and create padding spaces
                    int timeLength = time.length();
                    StringBuilder padding = new StringBuilder();
                    // Add spaces based on the length of the time string
                    for (int i = 0; i < Math.max(0, 6 - timeLength); i++) { // Adjust 10 based on your desired alignment
                        padding.append(" ");
                    }
                    String paddingTwo = liftType.length() < 3 ? "   " : "  ";

                    // Set the text to formatted date and time with padding
                    setText(formattedDate + "\n" + padding + time + "\n\n" + paddingTwo + liftType);
                }
            }
        });

        // Comparator for sorting
        deliveryDateColumn.setComparator((date1, date2) -> {
            LocalDate d1 = LocalDate.parse(date1);  // Assuming dates are in yyyy-MM-dd format
            LocalDate d2 = LocalDate.parse(date2);
            return d1.compareTo(d2);  // Ascending order
        });

        //  deliveryTimeColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryTime"));
        driverColumn.setCellValueFactory(new PropertyValueFactory<>("driver"));
        driverColumn.setCellFactory(column -> new TableCell<CustomerRental, String>() {
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
                try {
                    String imagePath = "/images/driver-icon.png";
                    Image image = new Image(getClass().getResourceAsStream(imagePath));
                    if (!image.isError()) {
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(19);  // Set smaller size
                        imageView.setFitHeight(19);  // Set smaller size

                        VBox vBox = new VBox(imageView);
                        vBox.setAlignment(Pos.BOTTOM_CENTER); // Align icon to bottom center
                        vBox.setPrefHeight(50);  // Ensure VBox has some height to work with

                        setGraphic(vBox);
                        setText("x"); // Set text to "x" for visibility if needed
                    } else {
                        setGraphic(null);
                    }
                } catch (Exception e) {
                    setGraphic(null);
                    setText(null); // Clear text in case of an error
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
                            circle.setFill(Color.web("#FF8C00"));
                            tooltip.setStyle("-fx-background-color: #FF8C00; -fx-text-fill: black;"); // Tooltip style
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
        deliveryDateColumn.setResizable(false);
        driverColumn.setResizable(false);
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
        updateDriverInitials();
        // Set the initial cell factory to default mode for the driver column
        driverColumn.setCellFactory(column -> new TableCell<CustomerRental, String>() {
            private final ImageView driverImageView = new ImageView();

            {
                driverImageView.setFitWidth(20); // Set the image size
                driverImageView.setFitHeight(20);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (item.equals("x")) {
                        driverImageView.setImage(new Image("file:C:\\Users\\maxhi\\OneDrive\\Documents\\Quickbooks\\QBProgram Development\\IdeaProjects\\JavaFX-MHR-App\\src\\main\\resources\\images\\driver-icon.png"));
                        setGraphic(driverImageView);
                        setText(null); // No text when displaying the icon
                    } else {
                        String[] parts = item.split("-");
                        String driverInitial = parts[0];
                        if (parts.length > 1) {
                            String driverNumber = String.valueOf(Integer.parseInt(parts[1]));
                            setText(driverInitial + "-" + driverNumber);
                        } else {
                            String driverNumber = "1";
                            setText(driverInitial + "-" + driverNumber);
                        }

                        setGraphic(driverImageView);
                    }
                }
            }

            // Method to display the driver icon only (for "x")
            private void displayDriverIconOnly() {
                try {
                    String imagePath = "/images/driver-icon.png";
                    Image image = new Image(getClass().getResourceAsStream(imagePath));
                    if (!image.isError()) {
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(19);  // 24px minus 5px
                        imageView.setFitHeight(19); // 24px minus 5px
                        setGraphic(imageView);      // Display the smaller image
                    } else {
                        setText("Image error");
                        setGraphic(null);
                    }
                } catch (Exception e) {
                    setText("Image not found");
                    setGraphic(null);
                }
                setText(null);  // No text when showing the icon
            }

            // Method to display initials and driver icon for assigned drivers
            private void displayDriverWithIcon(String driverInitials) {
                VBox vBox = new VBox();
                vBox.setSpacing(5);  // Space between initials and the icon
                vBox.setAlignment(Pos.BOTTOM_CENTER);  // Align icon at the bottom

                // Create a label for driver initials
                Label initialsLabel = new Label(driverInitials);

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
                        setGraphic(null);
                    }
                } catch (Exception e) {
                    setText(driverInitials);  // Fallback to just initials
                    setGraphic(null);
                }
            }
        });



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

        // Set tooltip for Edit Driver Button
        Tooltip editDriverTooltip = createCustomTooltip("Assign Driver", editDriverButton, 38, 10);

        // Tooltip for Dropping Off Button
        Tooltip droppingOffTooltip = createCustomTooltip("Record Drop Off", droppingOffButton, 38, 10);

        // Tooltip for Calling Off Button
        //callingOffButton.setOnAction(this::handleCallingOff);
        Tooltip callingOffTooltip = createCustomTooltip("Record Call Off", callingOffButton, 38, 10);

        // Tooltip for Picking Up Button
        Tooltip pickingUpTooltip = createCustomTooltip("Record Pick Up", pickingUpButton, 38, 10);

        // Tooltip for Create Invoices Button
        Tooltip createInvoicesTooltip = createCustomTooltip("Compose Invoices", createInvoicesButton, 38, 10);

        if (createInvoicesButton != null) {
                // Start with the button hidden
                createInvoicesButton.setOnAction(this::handleCreateInvoices);
            } else {
                System.err.println("createInvoicesButton is not injected!");
            }
        Tooltip createContractsTooltip = createCustomTooltip("Compose Contracts", createContractsButton, 38, 10);

        if (createContractsButton != null) {
                // Start with the button hidden
                createContractsButton.setOnAction(this::handleCreateContracts);
            } else {
                System.err.println("createContractsButton is not injected!");
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
                    refreshButton.setVisible(true);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showLoadingMessage(false);
                    loadingLabel.setText("Failed to load data.");
                    refreshButton.setVisible(true);
                });
            }
        };
        refreshButton.setVisible(false); // Hide refresh while loading
        new Thread(task).start();
    }

    private void loadData(String filter) {
        ordersList.clear();
        String query = "SELECT * FROM customers " +
               "JOIN rental_orders ON customers.customer_id = rental_orders.customer_id " +
               "JOIN rental_items ON rental_orders.rental_order_id = rental_items.rental_order_id";

        // Modify query based on filter
        switch (filter) {
            case "Today's Rentals":
                query += " WHERE DATE(rental_orders.delivery_date) = CURDATE()"; // Adjusted to order_date
                break;
            case "Yesterday's Rentals":
                query += " WHERE DATE(rental_orders.delivery_date) = CURDATE() - INTERVAL 1 DAY"; // Adjusted to order_date
                break;
            case "Custom Date Range":
                // Implement custom date range logic here if needed
                break;
            case "Ended Rentals":
                query += " WHERE rental_orders.order_status = 'Picked Up'";
                break;
            case "All Rentals":
                // No additional filtering
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
        setCurrentViewInitialsJustLoadedPage();
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

    private Tooltip createCustomTooltip(String text, Button button, double xOffset, double yOffset) {
        Tooltip tooltip = new Tooltip(text);
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
            // Exit driver edit mode and revert to displaying driver names or icons
            lastActionType = null;

            driverColumn.setCellFactory(column -> new TableCell<CustomerRental, String>() {
                private final ImageView driverImageView = new ImageView();

                {
                    driverImageView.setFitWidth(20); // Set the image size
                    driverImageView.setFitHeight(20);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        if (item.equals("x")) {
                            driverImageView.setImage(new Image("file:C:\\Users\\maxhi\\OneDrive\\Documents\\Quickbooks\\QBProgram Development\\IdeaProjects\\JavaFX-MHR-App\\src\\main\\resources\\images\\driver-icon.png"));
                            setGraphic(driverImageView);
                            setText(null);
                        } else {
                            String[] parts = item.split("-");
                            String driverInitial = parts[0];
                            if (parts.length > 1) {
                                String driverNumber = String.valueOf(Integer.parseInt(parts[1]));
                                setText(driverInitial + "-" + driverNumber);
                            } else {
                                String driverNumber = "1";
                                setText(driverInitial + "-" + driverNumber);
                            }

                            setGraphic(driverImageView);
                        }
                    }
                }

                private void displayDriverIconOnly() {
                    try {
                        String imagePath = "/images/driver-icon.png";
                        Image image = new Image(getClass().getResourceAsStream(imagePath));
                        if (!image.isError()) {
                            ImageView imageView = new ImageView(image);
                            imageView.setFitWidth(19);  // 24px minus 5px
                            imageView.setFitHeight(19); // 24px minus 5px

                            VBox vBox = new VBox(imageView); // Wrap icon in VBox
                            vBox.setAlignment(Pos.BOTTOM_CENTER); // Align icon to bottom center
                            vBox.setPrefHeight(50);  // Ensure VBox has some height to work with

                            setGraphic(vBox);  // Set VBox as the graphic
                            setText(null);      // No text
                        } else {
                            setText("Image error");
                            setGraphic(null);
                        }
                    } catch (Exception e) {
                        setText("Image not found");
                        setGraphic(null);
                    }
                }

                private void displayDriverWithIcon(String driverInitials) {
                    VBox vBox = new VBox();
                    vBox.setSpacing(5);
                    vBox.setAlignment(Pos.BOTTOM_CENTER);

                    Label initialsLabel = new Label(driverInitials);

                    try {
                        String imagePath = "/images/driver-icon.png";
                        Image image = new Image(getClass().getResourceAsStream(imagePath));
                        if (!image.isError()) {
                            ImageView imageView = new ImageView(image);
                            imageView.setFitWidth(19);
                            imageView.setFitHeight(19);
                            vBox.getChildren().addAll(initialsLabel, imageView);
                            setGraphic(vBox);
                        } else {
                            setText(driverInitials);
                            setGraphic(null);
                        }
                    } catch (Exception e) {
                        setText(driverInitials);
                        setGraphic(null);
                    }
                }
            });

            isDriverEditMode = false;
            updateRentalButton.setVisible(false);
            System.out.println("Driver assignment mode deactivated.");

        } else {
            // Switch to driver edit mode with ComboBox
            driverColumn.setCellFactory(column -> new TableCell<CustomerRental, String>() {
                private final ComboBox<String> comboBox = new ComboBox<>(driverInitials);

                {
                    comboBox.setEditable(true);
                    comboBox.setOnAction(e -> {
                        String selectedDriver = comboBox.getValue();
                        if (selectedDriver != null) {
                            // Update driver counts
                            String[] parts = selectedDriver.split("-");
                            String driverInitial = parts[0];
                            int driverNumber = Integer.parseInt(parts[1]);

                            // Update the count for the selected driver
                            driverCounts.put(driverInitial, driverNumber);

                            // Refresh the driver initials
                            updateDriverInitials();

                            commitEdit(selectedDriver); // Commit the selected driver
                        }
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        comboBox.getSelectionModel().select(item);  // Pre-select driver initials
                        VBox vBox = new VBox();
                        vBox.setSpacing(5);
                        vBox.setAlignment(Pos.BOTTOM_CENTER);

                        vBox.getChildren().addAll(comboBox);
                        setGraphic(vBox);  // Display combo box in VBox
                    }
                }

                @Override
                public void commitEdit(String newValue) {
                    super.commitEdit(newValue);
                    if (getTableRow() != null) {
                        CustomerRental order = getTableRow().getItem();
                        if (order != null) {
                            order.setDriver(newValue);  // Update driver in rental
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


    private void prepareDriverNumbers(ComboBox<String> comboBox, CustomerRental rental) {
        currentViewInitials.clear();
        for (CustomerRental rental : ordersList) {
            currentViewInitials.add(rental.getDriver());
        }
    }

    private void setCurrentViewInitialsJustLoadedPage() {
        currentViewInitials.clear();
        for (CustomerRental rental : ordersList) {
            currentViewInitials.add(rental.getDriver());
        }
        if (isCurrentViewInitialsValid()) {

        } else {
            fixCurrentViewInitials();
        }
    }

    private boolean isCurrentViewInitialsValid() {
        // Set of valid initials from the driverInitials list
        Set<String> validInitials = new HashSet<>(driverInitials);

        // Map to track the sequence of integers for each initial
        Map<String, Set<Integer>> initialIntegerMap = new HashMap<>();

        for (String value : currentViewInitials) {
            if (value.equals("x")) {
                // "x" is always valid
                continue;
            }

            // Split the value into initials and the number part
            String[] parts = value.split("(?<=\\D)(?=\\d)"); // Splits into non-digit and digit parts
            if (parts.length != 2) {
                return false; // Not in the correct format
            }

            String initial = parts[0]; // The alphabetical part
            String numberPart = parts[1]; // The numerical part

            // Check if the initial is in the set of valid initials
            if (!validInitials.contains(initial)) {
                return false; // Invalid initial
            }

            // Check if the number part is a valid positive integer
            int number;
            try {
                number = Integer.parseInt(numberPart);
                if (number <= 0) {
                    return false; // The number must be positive
                }
            } catch (NumberFormatException e) {
                return false; // Not a valid number
            }

            // Ensure sequential and non-duplicate integers for each initial
            Set<Integer> numberSet = initialIntegerMap.computeIfAbsent(initial, k -> new HashSet<>());
            if (numberSet.contains(number)) {
                return false; // Duplicate number for this initial
            }

            numberSet.add(number); // Add the number to the set for this initial
        }

        // After processing all values, ensure that each initial has a sequential series of numbers starting from 1
        for (Map.Entry<String, Set<Integer>> entry : initialIntegerMap.entrySet()) {
            Set<Integer> numberSet = entry.getValue();
            for (int i = 1; i <= numberSet.size(); i++) {
                if (!numberSet.contains(i)) {
                    return false; // The sequence is not continuous
                }
            }
        }

        return true; // All checks passed
    }

    private void fixCurrentViewInitials() {
        // Step 1: Group drivers by their initials (without numbers) and collect their numbers
        Map<String, List<CustomerRental>> groupedRentals = new HashMap<>();

        // Populate the groupedRentals map
        for (CustomerRental rental : ordersList) {
            String driver = rental.getDriver();
            String[] parts = driver.split("(?<=\\D)(?=\\d)"); // Split into the initial and number parts

            String initial = parts[0];
            int number = parts.length > 1 ? Integer.parseInt(parts[1]) : 0; // Default to 0 if no number present

            // Group by initials
            groupedRentals.computeIfAbsent(initial, k -> new ArrayList<>()).add(rental);
        }

        // Step 2: Fix the sequence for each group
        for (Map.Entry<String, List<CustomerRental>> entry : groupedRentals.entrySet()) {
            String initial = entry.getKey();
            List<CustomerRental> rentals = entry.getValue();

            // Sort the rentals by their current number (if any) or default to zero
            rentals.sort(Comparator.comparingInt(rental -> {
                String[] parts = rental.getDriver().split("(?<=\\D)(?=\\d)");
                return parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            }));

            // Step 3: Assign new sequential numbers to each rental
            int currentNumber = 1;
            for (CustomerRental rental : rentals) {
                String newDriver;
                if ("B".equals(initial) && currentNumber == 1) {
                    newDriver = initial + currentNumber; // Assign B1 for the first "B"
                } else {
                    newDriver = initial + currentNumber;
                }
                rental.setDriver(newDriver);  // Update the in-memory object

                // Step 4: Update the SQL database with the new driver value
                updateDriverInDatabase(rental.getRentalItemId(), newDriver);

                currentNumber++; // Increment the number for the next driver
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
        if ("dropping-off" == lastActionType) {
            lastActionType = null;
            hideCheckboxes();
            showSelectableCheckboxes(false, lastActionType);
  //          System.out.println("Picking Up button pressed again. Resetting action type.");
            updateRentalButton.setVisible(false); // Call this method to hide checkboxes
        } else {
            lastActionType = "dropping-off";
            resetCheckboxes();
            showSelectableCheckboxes(true, lastActionType);
  //          System.out.println("Picking Up button pressed.");
            return; // Exit if no action type is set
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
    private void handleCreateInvoices(ActionEvent event) {
        System.out.println("Create Invoices Button pressed. Current lastActionType: " + lastActionType);

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
    private void handleCreateContracts(ActionEvent event) {
        System.out.println("Create Contracts Button pressed. Current lastActionType: " + lastActionType);

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
                    System.out.println("Order for " + order.getName() + " marked as Active.");
                }
            }
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
    private void handleRefresh() {
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
            refreshButton.setVisible(false);
        } else {
            loadingLabel.setVisible(false);
            dbTableView.setVisible(true);
            refreshButton.setVisible(true);
        }
    }

    @Override
    public double getTotalHeight() {
        boolean hardCode = true;
        if (hardCode) {
            return 65;
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
}
package com.MaxHighReach;

import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.control.Tooltip;
import java.lang.String;

import javafx.scene.paint.Color;
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
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

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
    @FXML
    private TextField serialNumberField;
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

    private TableColumn<CustomerRental, String> fifthColumn = new TableColumn<>();




    private DBColumnFactory workingDBColumnFactory;

    @FXML
    private HBox viewsTilePane;
    private ToggleGroup viewsToggleGroup = new ToggleGroup();
    @FXML
    private ToggleButton intervalButton;
    @FXML
    private ToggleButton customerButton;
    @FXML
    private ToggleButton statusButton;
    @FXML
    private ToggleButton driverButton;
    private ToggleButton selectedViewButton;
    private ToggleButton selectedStatusButton;
    @FXML
    private VBox statusesPane;
    @FXML
    private VBox statusesPaneTwo;
    private ToggleGroup statusViewToggleGroup = new ToggleGroup();
    @FXML
    private ToggleButton upcomingButton;
    @FXML
    private ToggleButton activeButton;
    @FXML
    private ToggleButton billableButton;
    @FXML
    private ToggleButton calledOffButton;
    @FXML
    private DatePicker datePickerOne;
    private AtomicBoolean isDatePickerOneExpanded = new AtomicBoolean(false);
    @FXML
    private Label datePickerOneLabel;
    @FXML
    private DatePicker datePickerTwo;
    private AtomicBoolean isDatePickerTwoExpanded = new AtomicBoolean(false);
    @FXML
    private Label datePickerTwoLabel;
    @FXML
    private HBox datePickersPane;
    @FXML
    private Rectangle datePickerOneCover;
    @FXML
    private Button calendarButtonOne;

    @FXML
    private Button calendarButtonTwo;
    private HBox latestRightSideVbox;
    private HBox latestLeftSideVbox;
    @FXML
    private HBox leftSideVboxCustomerView;
    private ToggleGroup customerViewToggleGroup = new ToggleGroup();
    @FXML
    private ComboBox<String> customerComboBox;
    private ObservableList<Customer> customers = FXCollections.observableArrayList();
    @FXML
    private ComboBox<String> driverComboBox;
    @FXML
    private HBox leftSideVboxDriverView;

    private Timeline rotateViewsTimeline;
    private Timeline rotateStatusesTimeLine;
    private boolean areViewsRotating = false;
    private boolean areStatusesRotating = false;

    private ObservableList<CustomerRental> ordersList = FXCollections.observableArrayList();
    private boolean shouldShowCheckboxes = false;
    private boolean isDriverEditMode = false;
    private String lastActionType;
    private boolean isDroppingOff = false;
    private boolean isPickingUp = false;

    private Timeline fadeOutTimeline;
    private Timeline inactivityCheckTimeLine;
    private long lastScrollTime = System.currentTimeMillis();
    private static final long SCROLL_TIMEOUT = 2000; // 5 seconds
    private Glow glowEffect;
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
            fadeOutScrollBars(dbTableView);
        });

        // Create the fade out timeline
        fadeOutTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            fadeOutScrollBars(dbTableView);
        }));

        // Show scroll bars when scrolling
        dbTableView.setOnScroll(event -> {
            lastScrollTime = System.currentTimeMillis();
            showScrollBars(dbTableView);
            resetFadeOutTimeline(dbTableView, fadeOutTimeline);
        });

        inactivityCheckTimeLine = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            if (System.currentTimeMillis() - lastScrollTime > SCROLL_TIMEOUT) {
                fadeOutScrollBars(dbTableView);
            }
        }));
        inactivityCheckTimeLine.setCycleCount(Timeline.INDEFINITE);
        inactivityCheckTimeLine.play();

        glowEffect =  new Glow(1);

        // Enable table editing
        dbTableView.setEditable(true);



        // Load data initially
        loadDataAsync("All Rentals");

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

        for (javafx.scene.Node node : viewsTilePane.getChildren()) {
            node.getStyleClass().add("view-type-button-rotating");
            if (node instanceof ToggleButton toggleButton) {
                toggleButton.setToggleGroup(viewsToggleGroup);

                toggleButton.setOnAction(event -> {

                    if (areViewsRotating) {
                        rotateViewsTimeline.stop();
                        areViewsRotating = false;
                        toggleButton.getStyleClass().removeAll("view-type-button-rotating");
                        toggleButton.getStyleClass().add("view-type-button-stopped");
                        viewsToggleGroup.selectToggle(toggleButton);
                        selectedViewButton = toggleButton;
                        handleViewSelect(event, "collapse");
                    } else if (selectedViewButton == toggleButton) {
                        toggleButton.getStyleClass().removeAll("view-type-button-stopped");
                        toggleButton.getStyleClass().add("view-type-button-rotating");
                        areViewsRotating = true;
                        viewsToggleGroup.selectToggle(intervalButton);
                        handleViewSelect(event,"expand");
                        startHighlightRotation(viewsToggleGroup);
                        loadData("Open Interval");
                    } else {
                        toggleButton.getStyleClass().removeAll("view-type-button-rotating");
                        toggleButton.getStyleClass().add("view-type-button-stopped");
                        selectedViewButton.getStyleClass().removeAll("view-type-button-stopped");
                        selectedViewButton.getStyleClass().add("view-type-button-rotating");
                        selectedViewButton = toggleButton;
                        handleViewSelect(event, "collapse");
                    }
                });
            }
        }
        viewsToggleGroup.selectToggle(intervalButton);
        startHighlightRotation(viewsToggleGroup);


        for (javafx.scene.Node node : statusesPane.getChildren()) {
            node.getStyleClass().add("status-type-button-rotating");
            if (node instanceof ToggleButton toggleButton) {
                toggleButton.setToggleGroup(statusViewToggleGroup);

                toggleButton.setOnAction(event -> {
                    if (toggleButton.isSelected()) {
                        System.out.println("Selected: " + toggleButton.getText());
                        handleViewSettingSelect("Status", toggleButton.getText());
                        toggleButton.getStyleClass().removeAll("status-type-button-rotating");
                        toggleButton.getStyleClass().add("status-type-button-stopped");
                        statusViewToggleGroup.selectToggle(toggleButton);

                    }
                });
            }
        }


        for (javafx.scene.Node node : statusesPaneTwo.getChildren()) {
            node.getStyleClass().add("status-type-button-rotating");
            if (node instanceof ToggleButton toggleButton) {
                toggleButton.setToggleGroup(statusViewToggleGroup);

                toggleButton.setOnAction(event -> {
                    if (toggleButton.isSelected()) {
                        System.out.println("Selected: " + toggleButton.getText());
                        handleViewSettingSelect("Status", toggleButton.getText());
                        toggleButton.getStyleClass().removeAll("status-type-button-rotating");
                        toggleButton.getStyleClass().add("status-type-button-stopped");
                        statusViewToggleGroup.selectToggle(toggleButton);

                    }
                });
            }
        }

        for (Toggle toggle : statusViewToggleGroup.getToggles()) {
            ToggleButton toggleButton = (ToggleButton) toggle;
            toggleButton.setOnAction(event -> {
                if (areStatusesRotating) {
                    rotateStatusesTimeLine.stop();
                    areStatusesRotating = false;
                    toggleButton.getStyleClass().removeAll("status-type-button-rotating");
                    toggleButton.getStyleClass().add("status-type-button-stopped");
                    statusViewToggleGroup.selectToggle(toggleButton);
                    selectedStatusButton = toggleButton;
                    handleViewSettingSelect(selectedViewButton.getText(), toggleButton.getText());
                } else if (selectedStatusButton == toggleButton) {
                    toggleButton.getStyleClass().removeAll("status-type-button-stopped");
                    toggleButton.getStyleClass().add("status-type-button-rotating");
                    areStatusesRotating = true;
                    statusViewToggleGroup.selectToggle((ToggleButton) statusesPane.getChildren().get(0));
                    startHighlightRotation(statusViewToggleGroup);
                    handleViewSettingSelect(selectedViewButton.getText(), null);
                } else {
                    toggleButton.getStyleClass().removeAll("status-type-button-rotating");
                    toggleButton.getStyleClass().add("status-type-button-stopped");
                    selectedStatusButton.getStyleClass().removeAll("status-type-button-stopped");
                    selectedStatusButton.getStyleClass().add("status-type-button-rotating");
                    selectedStatusButton = toggleButton;
                    handleViewSettingSelect(selectedViewButton.getText(), toggleButton.getText());
                }
            });
        }



        /*loadCustomers();
        for (javafx.scene.Node node : statusesPane.getChildren()) {
            node.getStyleClass().add("view-type-button-rotating");
            if (node instanceof ToggleButton toggleButton) {
                toggleButton.setToggleGroup(customerViewToggleGroup);

                toggleButton.setOnAction(event -> {
                    if (toggleButton.isSelected()) {
                        System.out.println("Selected: " + toggleButton.getText());
                        toggleButton.getStyleClass().removeAll("view-type-button-rotating");
                        toggleButton.getStyleClass().add("view-type-button-stopped");
                        customerViewToggleGroup.selectToggle(toggleButton);
                    }
                });
            }
        }*/

        datePickerOne.setOnAction(event -> {
            ToggleButton selectedViewButton = (ToggleButton) viewsToggleGroup.getSelectedToggle();
            if (selectedViewButton != null) {
                handleViewSettingSelect(selectedViewButton.getText(), null);
                }
        });

        datePickerTwo.setOnAction(event -> {
            ToggleButton selectedViewButton = (ToggleButton) viewsToggleGroup.getSelectedToggle();
            if (selectedViewButton != null) {
                handleViewSettingSelect(selectedViewButton.getText(), null);
                }
        });
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
                    dbTableView.setItems(ordersList); // Update the table
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
             //       loadingLabel.setText("Failed to load data.");
                });
            }
        };
        new Thread(task).start();
    }

    private void loadData(String filter) {
        ordersList.clear();
        groupedRentals.clear();
        currentViewInitials.clear();

        String query = "SELECT customers.*, rental_orders.*, rental_items.*, lifts.*" +
               "FROM customers " +
               "JOIN rental_orders ON customers.customer_id = rental_orders.customer_id " +
               "JOIN rental_items ON rental_orders.rental_order_id = rental_items.rental_order_id " +
               "JOIN lifts ON rental_items.lift_id = lifts.lift_id ";


        // This variable tracks if we need to append WHERE or AND
        boolean hasWhereClause = false;

        String customerName = customerComboBox.getValue();
        String date = "";
        String startDate = "";
        if (datePickerOne.getValue() != null) {
            date = datePickerOne.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            startDate = datePickerOne.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        String endDate = "";
        if (datePickerTwo.getValue() != null) {
            endDate = datePickerTwo.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        workingDBColumnFactory = new DBColumnFactory(updateRentalButton, serialNumberField, dbTableView, groupedRentals, driverSequenceMap);

        // Modify query based on filter
        switch (filter) {
            case "Open Interval":
                // No additional filtering
                dbTableView.getColumns().addAll(workingDBColumnFactory.getDeliveryDateColumn(),
                        workingDBColumnFactory.getDriverColumn());
                break;
            case "One Date":
                query += "WHERE DATE(rental_orders.delivery_date) = '" + date + "' ";
                hasWhereClause = true;
                break;
            case "Interval":
                query += (hasWhereClause ? "AND " : "WHERE ") + "rental_orders.delivery_date BETWEEN '" + startDate + "' AND '" + endDate + "' ";
                hasWhereClause = true;
                break;
            case "Interval Active":
                query += (hasWhereClause ? "AND " : "WHERE ") + "rental_items.item_status = 'Active' " +
                         "AND rental_orders.delivery_date BETWEEN '" + startDate + "' AND '" + endDate + "' ";
                hasWhereClause = true;
                break;
            case "Interval Billable":
                query += (hasWhereClause ? "AND " : "WHERE ") + "rental_items.item_status = 'Billable' " +
                         "AND rental_orders.delivery_date BETWEEN '" + startDate + "' AND '" + endDate + "' ";
                hasWhereClause = true;
                break;
            case "Interval Upcoming":
                query += (hasWhereClause ? "AND " : "WHERE ") + "rental_items.item_status = 'Upcoming' " +
                         "AND rental_orders.delivery_date BETWEEN '" + startDate + "' AND '" + endDate + "' ";
                hasWhereClause = true;
                break;
            case "Interval Called Off":
                query += (hasWhereClause ? "AND " : "WHERE ") + "rental_items.item_status = 'Called Off' " +
                         "AND rental_orders.delivery_date BETWEEN '" + startDate + "' AND '" + endDate + "' ";
                hasWhereClause = true;
                break;
            case "Customer Active":
                query += (hasWhereClause ? "AND " : "WHERE ") + "customers.customer_name = '" + customerName + "'" +
                         " AND rental_orders.order_status = 'Active' ";
                hasWhereClause = true;
                break;
            case "Customer Billable":
                query += (hasWhereClause ? "AND " : "WHERE ") + "customers.customer_name = '" + customerName + "'" +
                         " AND rental_orders.order_status = 'Billable' ";
                hasWhereClause = true;
                break;
            case "Customer Upcoming":
                query += (hasWhereClause ? "AND " : "WHERE ") + "customers.customer_name = '" + customerName + "'" +
                         " AND rental_orders.order_status = 'Upcoming' ";
                hasWhereClause = true;
                break;
            case "Customer Called Off":
                query += (hasWhereClause ? "AND " : "WHERE ") + "customers.customer_name = '" + customerName + "'" +
                         " AND rental_orders.order_status = 'Called Off' ";
                hasWhereClause = true;
                break;
            case "Active":
                query += (hasWhereClause ? "AND " : "WHERE ") + "rental_items.item_status = 'Active' ";
                hasWhereClause = true;
                break;
            case "Billable":
                query += (hasWhereClause ? "AND " : "WHERE ") + "rental_items.item_status = 'Billable' ";
                hasWhereClause = true;
                break;
            case "Upcoming":
                query += (hasWhereClause ? "AND " : "WHERE ") + "rental_items.item_status = 'Upcoming' ";
                hasWhereClause = true;
                break;
            case "Called Off":
                query += (hasWhereClause ? "AND " : "WHERE ") + "rental_items.item_status = 'Called Off' ";
                hasWhereClause = true;
                break;
            case "Driver":
               // query += (hasWhereClause ? "AND " : "WHERE ") + "rental_items.driver = '" + driverComboBox.getValue() + "' ";
                break;
            case "Driver One Date":
                query += (hasWhereClause ? "AND " : "WHERE ") + "rental_items.driver = '" + driverComboBox.getValue() + "'" +
                         " AND rental_orders.delivery_date = '" + date + "' ";
                break;
            case "Driver Interval":
                query += (hasWhereClause ? "AND " : "WHERE ") + "rental_items.driver = '" + driverComboBox.getValue() + "'" +
                         " AND rental_orders.delivery_date BETWEEN '" + startDate + "' AND '" + endDate + "' ";
                break;
            case "Ended Rentals":
                query += (hasWhereClause ? "AND " : "WHERE ") + "rental_orders.order_status = 'Picked Up' " +
                         " OR rental_orders.order_status = 'Called Off' ";
                break;
            case "All Rentals":
                // No additional filtering
                dbTableView.getColumns().addAll(workingDBColumnFactory.getStatusColumn(),
                                                    workingDBColumnFactory.getAddressColumn(),
                                                    workingDBColumnFactory.getDeliveryDateColumn(),
                                                    workingDBColumnFactory.getSerialNumberColumn(),
                                                    workingDBColumnFactory.getDriverColumn());
                break;
        }

        // Add LIMIT clause at the end
        query += "LIMIT 100;";

        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
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
                String serialNumber = resultSet.getString("serial_number");
                int liftId = resultSet.getInt("lift_id");
                String liftType = resultSet.getString("lift_type");
                String siteName = resultSet.getString("site_name");
                String streetAddress = resultSet.getString("street_address");
                String poNumber = resultSet.getString("po_number");

                CustomerRental rental = new CustomerRental("0", name, deliveryDate, deliveryTime, driver != null ? driver : "", status != null ? status : "Unknown", "999999", rental_id);
                rental.setRentalItemId(rental_item_id);
                rental.setLiftId(liftId);
                rental.setLiftType(liftType);
                rental.setAddressBlockOne(siteName);
                rental.setAddressBlockTwo(streetAddress);
                rental.splitAddressBlockTwo();
                rental.setPoNumber(poNumber);
                rental.setSerialNumber(serialNumber);
                ordersList.add(rental);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        fixCurrentViewInitials(0);
    }

    private void startHighlightRotation(ToggleGroup toggleGroup) {
        System.out.println("Starting highlight rotation for" + toggleGroup);
        // Start rotation styling for each toggle button in the group
        for (Toggle toggle : toggleGroup.getToggles()) {
            if (toggle instanceof ToggleButton toggleButton) {
                if (toggleGroup == viewsToggleGroup) {
                    toggleButton.getStyleClass().removeAll("view-type-button-stopped");
                    toggleButton.getStyleClass().add("view-type-button-rotating");
                } else {
                    toggleButton.getStyleClass().removeAll("status-type-button-stopped");
                    toggleButton.getStyleClass().add("status-type-button-rotating");
                }

            }
        }

        ToggleButton currentToggle = new ToggleButton();
        boolean statusesTimeLineExistsAlready = true;

        if (toggleGroup == viewsToggleGroup) {
            areViewsRotating = true;
            // Set the initial selected button and configure the timeline for rotation
            selectedViewButton = intervalButton; // Set an initial toggle button
            toggleGroup.selectToggle(selectedViewButton); // Make sure it's selected in the toggle group
            rotateViewsTimeline = new Timeline();
            currentToggle = selectedViewButton; // Store current toggle
        } else {
            areStatusesRotating = true;
            // Set the initial selected button and configure the timeline for rotation
            selectedStatusButton = upcomingButton; // Set an initial toggle button
            toggleGroup.selectToggle(selectedStatusButton); // Make sure it's selected in the toggle group
            if (rotateStatusesTimeLine == null) {
                statusesTimeLineExistsAlready = false;
                 rotateStatusesTimeLine = new Timeline();
            }
            currentToggle = selectedStatusButton; // Store current toggle
        }

        System.out.println("Starting the rotation with button: " + currentToggle.getText());


        // Define the keyframe to toggle through views
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), event -> {
            Toggle nextToggle = getNextToggle(toggleGroup); // Get the next toggle
            // Update the selection in the ToggleGroup
            toggleGroup.selectToggle(nextToggle);
            ToggleButton nextButton = (ToggleButton) nextToggle;
            String groupName = new String("");
            if (toggleGroup == viewsToggleGroup) {
                groupName = "views";
                selectedViewButton = (ToggleButton) nextToggle;
            } else {
                groupName = "statuses";
                selectedStatusButton = (ToggleButton) nextToggle;
            }
            // Update selectedViewButton to the new toggle


      //      ((ToggleButton) nextToggle).getStyleClass().removeAll("view-type-but")

            // Debug output
        });

        if (toggleGroup == viewsToggleGroup) {
            // Set up and start the rotating timeline
            rotateViewsTimeline.getKeyFrames().add(keyFrame);
            rotateViewsTimeline.setCycleCount(Timeline.INDEFINITE);
            rotateViewsTimeline.play();
        } else {
            if (!statusesTimeLineExistsAlready) {
                // Set up and start the rotating timeline
                rotateStatusesTimeLine.getKeyFrames().add(keyFrame);
                rotateStatusesTimeLine.setCycleCount(Timeline.INDEFINITE);
            }
            rotateStatusesTimeLine.play();
        }

    }

    // Returns the next toggle in the sequence based on the current selection
    private ToggleButton getNextToggle(ToggleGroup toggleGroup) {
        if (toggleGroup == viewsToggleGroup) {
            if (selectedViewButton == intervalButton) {
                return customerButton;
            } else if (selectedViewButton == customerButton) {
                return statusButton;
            } else if (selectedViewButton == statusButton) {
                return driverButton;
            } else if (selectedViewButton == driverButton) {
                return intervalButton;
            } else {
                // Return a default button if the current selection is null or unrecognized
                return intervalButton;
            }
        } else {
            if (selectedStatusButton == upcomingButton) {

                return activeButton;
            } else if (selectedStatusButton == activeButton) {
                return calledOffButton;
            } else if (selectedStatusButton == calledOffButton) {
                return billableButton;
            } else if (selectedStatusButton == billableButton) {
                return upcomingButton;
            } else {
                // Return a default button if the current selection is null or unrecognized
                return upcomingButton;
            }
        }
    }



    private void showScrollBars(TableView<CustomerRental> dbTableView) {
        // Show scroll bars
        setScrollBarVisibility(dbTableView, true);
        animateScrollBars(dbTableView);
        resetFadeOutTimeline(dbTableView, fadeOutTimeline); // Reset the fade out timer
    }

    private void animateScrollBars(TableView<CustomerRental> dbTableView) {
        // Assuming your scroll bars are part of the dbTableView's skin
        for (Node node : dbTableView.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar) {
                // Create a glow effect for the scroll bar
                DropShadow glowEffect = new DropShadow();
                glowEffect.setRadius(10);
                glowEffect.setSpread(0.5);
                node.setEffect(glowEffect);

                // Create a glow animation
                Timeline glowTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO,
                        new KeyValue(glowEffect.colorProperty(), Color.web("#FFDEAD", 0.5))
                    ),
                    new KeyFrame(Duration.seconds(1),
                        new KeyValue(glowEffect.colorProperty(), Color.web("#FF7F00", 1.0))
                    )
                );
                glowTimeline.setCycleCount(Timeline.INDEFINITE);
                glowTimeline.setAutoReverse(true);
                glowTimeline.play();
            }
        }
    }


    private void fadeOutScrollBars(TableView<CustomerRental> dbTableView) {
        // Access the vertical and horizontal scroll bars
        ScrollBar verticalScrollBar = (ScrollBar) dbTableView.lookup(".scroll-bar:vertical");
        ScrollBar horizontalScrollBar = (ScrollBar) dbTableView.lookup(".scroll-bar:horizontal");

        // Create FadeTransitions for the vertical and horizontal scroll bars
        FadeTransition fadeOutVertical = new FadeTransition(Duration.seconds(2), verticalScrollBar);
        fadeOutVertical.setFromValue(1.0); // Fully visible
        fadeOutVertical.setToValue(0.0);   // Fully transparent
        fadeOutVertical.setOnFinished(e -> {
            if (verticalScrollBar != null) {
                verticalScrollBar.setVisible(false); // Optionally hide the scroll bar after fading out
            }
        });

        FadeTransition fadeOutHorizontal = new FadeTransition(Duration.seconds(2), horizontalScrollBar);
        fadeOutHorizontal.setFromValue(1.0); // Fully visible
        fadeOutHorizontal.setToValue(0.0);   // Fully transparent
        fadeOutHorizontal.setOnFinished(e -> {
            if (horizontalScrollBar != null) {
                horizontalScrollBar.setVisible(false); // Optionally hide the scroll bar after fading out
            }
        });

        // Start the fade-out transitions
        fadeOutVertical.play();
        fadeOutHorizontal.play();

        // Adjust the glow effect to fade out with the scroll bars
        fadeOutGlowEffect();
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
        if (fadeOutTimeline != null && fadeOutTimeline.getStatus() == Animation.Status.RUNNING) {
            fadeOutTimeline.stop(); // Stop the fade-out timeline
        }

        // Start a new fade-out timeline to trigger after 2 seconds of inactivity
        fadeOutTimeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            fadeOutScrollBars(dbTableView); // Call the fade-out method
        }));
        fadeOutTimeline.setCycleCount(1);
        fadeOutTimeline.play(); // Start the timeline
    }


    private void fadeOutGlowEffect() {
        // Create a Timeline to gradually decrease the glow effect's level
        Timeline glowFade = new Timeline();

        // The initial level of the glow effect
        double startLevel = glowEffect.getLevel();

        // Number of frames for the fade out; adjust for smoothness and speed
        int frames = 20;
        double decrement = startLevel / frames;

        // Create KeyFrames to gradually reduce the glow level
        for (int i = 0; i <= frames; i++) {
            final int frameIndex = i; // Necessary for accessing in the lambda
            KeyFrame keyFrame = new KeyFrame(Duration.millis(100 * i), e -> {
                double newLevel = startLevel - (frameIndex * decrement);
                glowEffect.setLevel(Math.max(newLevel, 0)); // Set the glow level but ensure it doesn't go below 0
            });
            glowFade.getKeyFrames().add(keyFrame);
        }

        // Start the glow fade out animation
        glowFade.play();
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



    @FXML
    private void handleViewAndDriverSelect(){
        if (datePickerOne.getValue() == null && datePickerTwo.getValue() == null) {
            handleViewSettingSelect("Driver", null);
        } else if (datePickerOne.getValue() != null && datePickerTwo.getValue() == null) {
            handleViewSettingSelect("Driver One Date", null);
        } else {
            handleViewSettingSelect("Driver Interval", null);
        }
    }

    @FXML
    private void handleViewAndCustomerSelect(){
        if (datePickerOne.getValue() == null && datePickerTwo.getValue() == null) {
            handleViewSettingSelect("Customer Active", null);
        } else if (datePickerOne.getValue() != null && datePickerTwo.getValue() == null) {
            handleViewSettingSelect("Customer One Date", null);
        } else {
            handleViewSettingSelect("Customer Interval", null);
        }
    }

    @FXML
    private void handleOpenDatePickerOne() {
        toggleDatePicker(datePickerOne, isDatePickerOneExpanded, datePickerOneLabel, true);
    }

    @FXML
    private void handleOpenDatePickerTwo() {
        toggleDatePicker(datePickerTwo, isDatePickerTwoExpanded, datePickerTwoLabel, false);
    }

    private void toggleDatePicker(DatePicker datePicker, AtomicBoolean isDatePickerExpanded, Label label, boolean enableSecondDatePicker) {
        if (!isDatePickerExpanded.get()) {
            openDatePicker(datePicker, isDatePickerExpanded, label, enableSecondDatePicker);
        } else {
            isDatePickerExpanded.set(false); // Hide the calendar
        }
    }

    private void openDatePicker(DatePicker datePicker, AtomicBoolean isDatePickerExpanded, Label label, boolean enableSecondDatePicker) {
        datePicker.show();
        isDatePickerExpanded.set(true);
        datePicker.requestFocus();

        datePicker.setOnAction(event -> {
            LocalDate selectedDate = datePicker.getValue();
            if (selectedDate != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d");
                String formattedDate = selectedDate.format(formatter);
                label.setText(formattedDate);
                isDatePickerExpanded.set(false);

                // Conditionally enable the second date picker and its elements
                if (enableSecondDatePicker) {
                    datePickerTwo.setVisible(true);
                    calendarButtonTwo.setVisible(true);
                    datePickerTwoLabel.setVisible(true);
                }
            }
        });

        datePicker.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                isDatePickerExpanded.set(false);
            }
        });
    }

    @FXML
    private void handleAssignDrivers() {

        if ("assigning-drivers".equals(lastActionType)) {

            isDriverEditMode = false;
            lastActionType = null;
            updateRentalButton.setVisible(false); // Call this method to hide checkboxes
            workingDBColumnFactory.setClosedDriverColumn();

        } else {

            isDriverEditMode = true;
            updateRentalButton.setVisible(true);
            System.out.println("Driver assignment mode activated.");
            lastActionType = "assigning-drivers";
            workingDBColumnFactory.setOpenDriverColumn();

        }

        dbTableView.refresh();
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


    private void hideCheckboxes() {

        dbTableView.refresh();
    }

   @FXML
    private void handleDroppingOff(ActionEvent event) {
        if ("dropping-off".equals(lastActionType)) { // Use .equals() to compare strings
            lastActionType = null;
            hideCheckboxes();
            showSelectableCheckboxes(false, lastActionType);
            workingDBColumnFactory.showSelectableCheckboxes(false, lastActionType);
            updateRentalButton.setVisible(false); // Call this method to hide checkboxes
            serialNumberField.setVisible(false);
            shiftUpdateButtonFull();

            visuallyUnselectSidebarButton(droppingOffButton);
        } else {

            lastActionType = "dropping-off";
            resetCheckboxes();
            showSelectableCheckboxes(true, lastActionType);
            workingDBColumnFactory.showSelectableCheckboxes(true, lastActionType);
            visuallySelectSidebarButton(droppingOffButton);
            serialNumberField.setVisible(true);
            shiftUpdateButtonHalf();

        }
    }




    @FXML
    private void handleCallingOff(ActionEvent event) {
        if ("calling-off" == lastActionType) {
            lastActionType = null;
            hideCheckboxes();
            showSelectableCheckboxes(false, lastActionType);
            workingDBColumnFactory.showSelectableCheckboxes(false, lastActionType);
            updateRentalButton.setVisible(false);
            System.out.println("Calling Off button pressed again. Resetting action type.");
        } else {
            lastActionType = "calling-off";
            resetCheckboxes();
            showSelectableCheckboxes(true, lastActionType);
            workingDBColumnFactory.showSelectableCheckboxes(true, lastActionType);
            System.out.println("Calling Off button pressed.");
            if (serialNumberField.isVisible()) {
                serialNumberField.setVisible(false);
                shiftUpdateButtonFull();
            }
            return;
        }
    }

    @FXML
    private void handlePickingUp(ActionEvent event) {
        if ("picking-up" == lastActionType) {
            lastActionType = null; // Reset the action type if the button is pressed again
            hideCheckboxes();
            showSelectableCheckboxes(false, lastActionType);
            workingDBColumnFactory.showSelectableCheckboxes(false, lastActionType);
   //         System.out.println("Picking Up button pressed again. Resetting action type.");
            updateRentalButton.setVisible(false);
        } else {
            lastActionType = "picking-up";
            resetCheckboxes();
            showSelectableCheckboxes(true, lastActionType);
            workingDBColumnFactory.showSelectableCheckboxes(true, lastActionType);
            if (serialNumberField.isVisible()) {
                serialNumberField.setVisible(false);
                shiftUpdateButtonFull();
            }
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
            workingDBColumnFactory.showSelectableCheckboxes(false, lastActionType);
            updateRentalButton.setVisible(false);
            System.out.println("Resetting action type. Checkboxes hidden.");
            return; // Exit the method
        } else {
            lastActionType = "creating-invoices"; // Set the action type
            resetCheckboxes(); // Deselect all checkboxes first
            showSelectableCheckboxes(true, lastActionType); // Show the checkboxes
            workingDBColumnFactory.showSelectableCheckboxes(true, lastActionType);
            System.out.println("Action type set to 'creating-invoices'. Checkboxes shown.");
            if (serialNumberField.isVisible()) {
                serialNumberField.setVisible(false);
                shiftUpdateButtonFull();
            }
        }

    }


    @FXML
    private void handleComposeContracts(ActionEvent event) {
        System.out.println("Compose Contracts Button pressed. Current lastActionType: " + lastActionType);

        if ("creating-contracts" == lastActionType) {
            lastActionType = null; // Reset the action type
            hideCheckboxes(); // Hide the checkboxes
            showSelectableCheckboxes(false, lastActionType);
            workingDBColumnFactory.showSelectableCheckboxes(false, lastActionType);
            updateRentalButton.setVisible(false);
            System.out.println("Resetting action type. Checkboxes hidden.");
            return; // Exit the method
        } else {
            lastActionType = "creating-contracts"; // Set the action type
            resetCheckboxes(); // Deselect all checkboxes first
            showSelectableCheckboxes(true, lastActionType); // Show the checkboxes
            workingDBColumnFactory.showSelectableCheckboxes(true, lastActionType);
            System.out.println("Action type set to 'creating-contracts'. Checkboxes shown.");
            if (serialNumberField.isVisible()) {
                serialNumberField.setVisible(false);
                shiftUpdateButtonFull();
            }
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

    }

    @FXML
    private void handleUpdateRental(ActionEvent event) {
        ObservableList<CustomerRental> selectedRentals = dbTableView.getItems().filtered(CustomerRental::isSelected);
        boolean statusUpdated = false;
        Date today = new Date(); // Today's date

        // Handle the 'creating-invoices' action type
        if (lastActionType.equals("creating-invoices")) {
            statusUpdated = true;
            checkAndSwitchScene(statusUpdated);
        } else if (lastActionType.equals("creating-contracts")) {
            // Handle the driver assignment status updates
            for (CustomerRental order : selectedRentals) {
                // Logic for contracts if needed
            }
        } else if (lastActionType.equals("assigning-drivers")) {
            handleAssignDrivers();
            statusUpdated = true;
        } else if (lastActionType.equals("dropping-off")) {
            for (CustomerRental order : selectedRentals) {
                String newStatus = "Active";
                System.out.println("Order for " + order.getName() + " status is:" + order.getStatus());
                if (order.getStatus().equals("Upcoming")) {
                    order.setStatus(newStatus);
                    updateRentalStatusInDB(order.getRentalItemId(), newStatus); // Sync with DB
                    updateDateInDB(order.getRentalItemId(), "item_delivery_date", today); // Update delivery date
                    statusUpdated = true;
                    visuallyUnselectSidebarButton(droppingOffButton);
                    System.out.println("Order for " + order.getName() + " marked as Active with today's delivery date.");
                    if (serialNumberField.getText().length() >= 5) {
                    for (CustomerRental rental : selectedRentals) {
                            updateSerialNumberInDB(rental.getRentalItemId(), serialNumberField.getText());
                            rental.setSerialNumber(serialNumberField.getText());
                        }
                    }

                    serialNumberField.setVisible(false);
                    shiftUpdateButtonFull();
                    serialNumberField.clear();
                }
            }
            droppingOffButton.setStyle("-fx-background-color: transparent");
        } else if (lastActionType.equals("calling-off")) {
            for (CustomerRental order : selectedRentals) {
                String newStatus = "Called Off";
                if (order.getStatus().equals("Active")) {
                    order.setStatus(newStatus);
                    updateRentalStatusInDB(order.getRentalItemId(), newStatus); // Sync with DB
                    updateDateInDB(order.getRentalItemId(), "item_call_off_date", today); // Update pickup date
                    statusUpdated = true;
                    System.out.println("Order for " + order.getName() + " status updated to 'Called Off'.");
                }
            }
        } else if (lastActionType.equals("picking-up")) {
            for (CustomerRental order : selectedRentals) {
                String newStatus = "Picked Up"; // Set the status for picking-up
                if (order.getStatus().equals("Called Off")) {
                    order.setStatus(newStatus);
                    updateRentalStatusInDB(order.getRentalItemId(), newStatus); // Sync with DB
                    statusUpdated = true;
                    System.out.println("Order for " + order.getName() + " status updated to 'Picked Up' with today's pickup date.");
                }
            }
        } else {
            // Existing logic for other action types
            for (CustomerRental order : selectedRentals) {
                String newStatus = determineNewStatus(order, lastActionType);
                if (!newStatus.equals(order.getStatus())) {
                    order.setStatus(newStatus);
                    updateRentalStatusInDB(order.getRentalItemId(), newStatus); // Sync with DB
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
            workingDBColumnFactory.resetCheckboxes();
            workingDBColumnFactory.showSelectableCheckboxes(false, lastActionType);
            updateRentalButton.setVisible(false);


        }
        dbTableView.refresh();
    }



    private void updateSerialNumberInDB(int rentalItemId, String serialNumber) {
       if (!checkSerialNumberExists(serialNumber)) {
           System.out.println("The provided serial number does not exist in the database.");
           return;
       }

       String updateQuery = """
          UPDATE rental_items
          SET lift_id = (
              SELECT lift_id
              FROM lifts
              WHERE serial_number = ?
          )
          WHERE rental_item_id = ?;
       """;

       try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
            PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {

           // Set the parameters for the query
           preparedStatement.setString(1, serialNumber);
           preparedStatement.setInt(2, rentalItemId);

           // Execute the update
           int rowsAffected = preparedStatement.executeUpdate();
           if (rowsAffected > 0) {
               System.out.println("Rental item updated successfully.");
           } else {
               System.out.println("No matching rental item found or serial number does not exist.");
           }

       } catch (SQLException e) {
           System.err.println("Error while updating the rental item: " + e.getMessage());
           e.printStackTrace();
       }
    }


    private void updateRentalStatusInDB(int rentalItemId, String newStatus) {
        String updateQuery = "UPDATE rental_items SET item_status = ? WHERE rental_item_id = ?"; // Update table name

        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
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

    private void updateDateInDB(int rentalItemId, String dateColumn, Date date) {
        String updateQuery = "UPDATE rental_items SET " + dateColumn + " = ? WHERE rental_item_id = ?";

        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

            statement.setDate(1, new java.sql.Date(date.getTime()));
            statement.setInt(2, rentalItemId);

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                // Log or success logic
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean checkSerialNumberExists(String serialNumber) {
        String checkQuery = "SELECT COUNT(*) FROM lifts WHERE serial_number = ?;";
        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             PreparedStatement preparedStatement = connection.prepareStatement(checkQuery)) {
            preparedStatement.setString(1, serialNumber);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    private void checkAndSwitchScene(boolean statusUpdated) {
        if (statusUpdated) {
            try {
                MaxReachPro.loadScene("/fxml/compose_invoices.fxml"); // Replace with your scene path
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
        loadDataAsync("All Rentals");
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

    public void shiftUpdateButtonHalf(){
        updateRentalButton.setMinWidth(145);
        updateRentalButton.setMaxWidth(145);
        updateRentalButton.setTranslateX(-23);
    }

    public void shiftUpdateButtonFull(){
        serialNumberField.clear();
        serialNumberField.setPromptText("     Serial Number");
        updateRentalButton.setMinWidth(295);
        updateRentalButton.setMaxWidth(295);
        updateRentalButton.setTranslateX(-100);
    }


    private void handleViewSelect(ActionEvent event, String orientation) {
        datePickerOne.setValue(null);
        datePickerTwo.setValue(null);
        datePickerOneLabel.setText("From:");
        datePickerTwoLabel.setText("To:");

        ToggleButton selectedButton = event.getSource() instanceof ToggleButton ? (ToggleButton) event.getSource() : null;

        if (orientation == "expand") {
            // Unselect the currently selected button
            selectedViewButton.setSelected(false);

            // Show all ToggleButtons in the group
            for (Toggle toggle : viewsToggleGroup.getToggles()) {
                ToggleButton toggleButton = (ToggleButton) toggle;
                toggleButton.setVisible(true);

                // Animate the button sliding back to its original position
                TranslateTransition transition = new TranslateTransition(Duration.millis(300), toggleButton);
                transition.setToX(0); // number equals an offset amount of original
                transition.setToY(0);

                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(300), toggleButton);
                scaleTransition.setToX(1); // number equals a scalar amount of original
                scaleTransition.setToY(1);

                toggleButton.scaleXProperty().addListener((obs, oldVal, newVal) -> {
                    double newFontSize = 14 * newVal.doubleValue();
                    toggleButton.setFont(Font.font(newFontSize));
                });

                scaleTransition.play();
                transition.play();
            }

            // Clear the selectedViewButton reference
            selectedViewButton = null;

            // Hide any VBox currently shown
            if (latestLeftSideVbox != null) {
                latestLeftSideVbox.setVisible(false);
                latestLeftSideVbox = null;
            }
            if (latestRightSideVbox != null) {
                latestRightSideVbox.setVisible(false);
                latestRightSideVbox = null;
            }
            statusesPane.setVisible(false);
            statusesPaneTwo.setVisible(false);
            datePickerOneLabel.setVisible(false);
            datePickerTwoLabel.setVisible(false);
            datePickerOneCover.setVisible(false);
            calendarButtonOne.setVisible(false);
            calendarButtonTwo.setVisible(false);
        } else {
            // New button selection
           // selectedViewButton = selectedButton;
            String selectedView = selectedViewButton.getText();

            // Hide the previously shown VBoxes
            if (latestLeftSideVbox != null) {
                latestLeftSideVbox.setVisible(false);
            }
            if (latestRightSideVbox != null) {
                latestRightSideVbox.setVisible(false);
            }

            // Determine which VBoxes to show based on the selected view
            switch (selectedView) {
                case "Interval":
                    datePickersPane.setVisible(true);
                    latestRightSideVbox = datePickersPane;
                    datePickerOneLabel.setVisible(true);
                    datePickerOneCover.setVisible(true);
                    calendarButtonOne.setVisible(true);
                    statusesPane.setVisible(true);
                    statusesPaneTwo.setVisible(true);
                    statusViewToggleGroup.selectToggle(upcomingButton);
                    startHighlightRotation(statusViewToggleGroup);
                    break;
                case "Customer":
                    leftSideVboxCustomerView.setVisible(true);
                    latestLeftSideVbox = leftSideVboxCustomerView;
                    datePickersPane.setVisible(true);
                    latestRightSideVbox = datePickersPane;
                    datePickerOneLabel.setVisible(true);
                    datePickerOneCover.setVisible(true);
                    calendarButtonOne.setVisible(true);
                    break;
                case "Status":
                    statusesPane.setVisible(true);
                    statusesPaneTwo.setVisible(true);
                    datePickersPane.setVisible(true);
                    latestRightSideVbox = datePickersPane;
                    datePickerOneLabel.setVisible(true);
                    datePickerOneCover.setVisible(true);
                    calendarButtonOne.setVisible(true);
                    statusViewToggleGroup.selectToggle(upcomingButton);
                    startHighlightRotation(statusViewToggleGroup);
                    break;
                case "Driver":
                    leftSideVboxDriverView.setVisible(true);
                    latestLeftSideVbox = leftSideVboxDriverView;
                    datePickersPane.setVisible(true);
                    latestRightSideVbox = datePickersPane;
                    datePickerOneLabel.setVisible(true);
                    datePickerOneCover.setVisible(true);
                    calendarButtonOne.setVisible(true);
                    driverComboBox.setItems(driverInitials);
                    break;
            }

            // Refresh the database view
            dbTableView.refresh();

            // Animate the selected button to slide to the leftmost position
            TranslateTransition transition = new TranslateTransition(Duration.millis(300), selectedViewButton);
            transition.setToX(-selectedViewButton.getLayoutX() - 4); // Adjust this based on the leftmost position
            transition.setToY(-selectedViewButton.getLayoutY());

            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(300), selectedViewButton);
            scaleTransition.setToX(.8);


            /*
            selectedViewButton.scaleXProperty().addListener((obs, oldVal, newVal) -> {
                double newFontSize = 14 * newVal.doubleValue();
                selectedViewButton.setFont(Font.font(newFontSize));
            });*/

            scaleTransition.play();

            transition.play();

            // Hide all other ToggleButtons
            for (Toggle toggle : viewsToggleGroup.getToggles()) {
                ToggleButton toggleButton = (ToggleButton) toggle;
                if (toggleButton != selectedViewButton) {
                    toggleButton.setVisible(false);
                }
            }
        }
    }






    private void loadCustomers() {
        String query = "SELECT customer_id, customer_name, email FROM customers";

        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
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


    private void handleViewSettingSelect (String viewType, String firstSetting) {
        dbTableView.getColumns().clear();
        workingDBColumnFactory = new DBColumnFactory(updateRentalButton, serialNumberField, dbTableView, groupedRentals, driverSequenceMap);
        TableColumn<CustomerRental, String> serialNumberColumn = workingDBColumnFactory.getSerialNumberColumn();
        TableColumn<CustomerRental, Boolean> statusColumn = workingDBColumnFactory.getStatusColumn();
        TableColumn<CustomerRental, String> deliveryDateColumn = workingDBColumnFactory.getDeliveryDateColumn();
        TableColumn<CustomerRental, String> deliveryTimeColumn = workingDBColumnFactory.getDeliveryTimeColumn();
        TableColumn<CustomerRental, String> addressColumn = workingDBColumnFactory.getAddressColumn();
        LocalDate firstDate = datePickerOne.getValue();
        LocalDate secondDate = datePickerTwo.getValue();

        dbTableView.getColumns().addAll(statusColumn, addressColumn);
        switch (viewType) {
            case "Interval":
                if (firstSetting != null) {
                    if (firstDate == null && secondDate == null) {
                        switch (firstSetting) {
                            case "Active":
                                dbTableView.getColumns().add(serialNumberColumn);
                                loadData("Active");
                                break;
                            case "Billable":
                                loadData("Billable");
                                break;
                            case "Upcoming":
                                dbTableView.getColumns().add(deliveryDateColumn);
                                loadData("Upcoming");
                                break;
                            case "Called Off":
                                loadData("Called Off");
                                break;
                            default:
                                loadData("Open Interval");
                                break;
                        }
                    } else if (firstDate != null && secondDate == null) {
                        switch (firstSetting) {
                            case "Active":
                                dbTableView.getColumns().add(serialNumberColumn);
                                loadData("One Date Active");
                                break;
                            case "Billable":
                                loadData("One Date Billable");
                                break;
                            case "Upcoming":
                                dbTableView.getColumns().add(deliveryDateColumn);
                                loadData("One Date Upcoming");
                                break;
                            case "Called Off":
                                loadData("One Date Called Off");
                                break;
                            default:
                                loadData("One Date");
                                break;
                        }
                    } else {
                        switch (firstSetting) {
                            case "Active":
                                dbTableView.getColumns().add(serialNumberColumn);
                                loadData("Interval Active");
                                break;
                            case "Billable":
                                loadData("Interval Billable");
                                break;
                            case "Upcoming":
                                dbTableView.getColumns().add(deliveryDateColumn);
                                loadData("Interval Upcoming");
                                break;
                            case "Called Off":
                                loadData("Interval Called Off");
                                break;
                            default:
                                loadData("Interval");
                                break;
                        }
                    }
                } else {
                    loadData("Open Interval");
                }
                break;


            case "Customer":
                switch (firstSetting) {
                    case "Active":
                        loadData("Customer Active");
                        break;
                    case "Billable":
                        loadData("Customer Billable");
                        break;
                    case "Upcoming":
                        loadData("Customer Upcoming");
                        break;
                    case "Called Off":
                        loadData("Customer Called Off");
                        break;
                    default:
                        loadData("Customer");
                        break;
                }
                break;


            case "Status":
                if (firstSetting != null) {
                    switch (firstSetting) {
                        case "Active":
                            dbTableView.getColumns().add(serialNumberColumn);
                            loadData("Active");
                            break;
                        case "Billable":
                            loadData("Billable");
                            break;
                        case "Upcoming":
                            dbTableView.getColumns().add(deliveryDateColumn);
                            loadData("Upcoming");
                            break;
                        case "Called Off":
                            loadData("Called Off");
                            break;
                        default:
                            loadData("Status");
                            break;
                    }
                } else {
                    loadData("Open Interval");
                }
                break;


            case "Driver":
                dbTableView.getColumns().add(serialNumberColumn);
                dbTableView.getColumns().add(deliveryTimeColumn);
                if (firstDate == null && secondDate == null) {
                    loadData("Driver");
                } else if (firstDate != null && secondDate == null) {
                    loadData("Driver One Date");
                } else {
                    loadData("Driver Interval");
                }
                break;


            default:
                loadData("Unknown View Type");
                break;
        }






        dbTableView.refresh();
    }


}
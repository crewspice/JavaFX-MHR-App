package com.MaxHighReach;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.kernel.utils.PdfMerger;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.control.Tooltip;

import java.awt.*;
import java.io.File;
import java.io.IOException;
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

import javax.swing.*;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.Date;
import java.util.List;
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
    private Button expandButton;
    @FXML
    private Button composeContractsButton;
    @FXML
    private Button refreshDataButton;
    @FXML
    private Button deleteButton;
    @FXML
    private TextField serialNumberField;
    @FXML
    private Button batchButton;
    private String batchSwitcher = null;
    @FXML
    private Button secondInProcessButton;
    @FXML
    private Button openSDKButton;
    private final Tooltip composeContractsTooltip = new Tooltip("Compose Contracts");
    private final Tooltip assignDriverTooltip = new Tooltip("Assign Driver");
    private final Tooltip droppingOffTooltip = new Tooltip("Record Drop Off");
    private final Tooltip callingOffTooltip = new Tooltip("Record Call Off");
    private final Tooltip pickingUpTooltip = new Tooltip("Record Pick Up");
    private final Tooltip composeInvoicesTooltip = new Tooltip("Compose Invoices");
    private final Tooltip expandTooltip = new Tooltip("Expand Rental");
    private final Tooltip refreshDataTooltip = new Tooltip("Refresh Table");
    private final Tooltip deleteTooltip = new Tooltip("Delete Rental");
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TableView<CustomerRental> dbTableView;
    private DBColumnFactory workingDBColumnFactory;
    TableColumn<CustomerRental, String> serialNumberColumn = new TableColumn<>();
    TableColumn<CustomerRental, Boolean> statusColumn = new TableColumn<>();
    TableColumn<CustomerRental, String> deliveryDateColumn = new TableColumn<>();
    TableColumn<CustomerRental, String> deliveryTimeColumn = new TableColumn<>();
    TableColumn<CustomerRental, String> addressColumn = new TableColumn<>();
    TableColumn<CustomerRental, String> invoiceColumn = new TableColumn<>();
    TableColumn<CustomerRental, String> driverColumn = new TableColumn<>();
    @FXML
    private HBox viewsTilePane;
    private ToggleGroup viewsToggleGroup = new ToggleGroup();
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
    private String lastActionType;
    @FXML
    private Rectangle sideBarHighlighter;

    private Timeline fadeOutTimeline;
    private Timeline inactivityCheckTimeLine;
    private long lastScrollTime = System.currentTimeMillis();
    private static final long SCROLL_TIMEOUT = 2000; // 5 seconds
    private Glow glowEffect;
    private Timeline[] glowTimelines = new Timeline[2];
    private boolean timelineFlagger = false;
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

    private String PREFIX = "C:\\Users\\maxhi\\OneDrive\\Documents\\Max High Reach\\MONTH END\\";
    private String SRCDIR = "..\\..\\Quickbooks\\QBProgram Development\\Composing Invoices\\";
    private String INVOICE_QUERY = PREFIX + SRCDIR + "scripts\\invoice_batch.xml";

    @FXML
    public void initialize() {
        super.initialize();

        dbTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        animateScrollBars(dbTableView);

        dbTableView.setOnMouseExited(event -> {
            timelineFlagger = true;
        });

        dbTableView.setOnMouseEntered(event -> {
            timelineFlagger = false;
        });

        /*
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


         */
        // Enable table editing
        dbTableView.setEditable(true);

        workingDBColumnFactory = new DBColumnFactory(updateRentalButton, serialNumberField, dbTableView, groupedRentals,
                driverSequenceMap, batchButton, batchSwitcher, secondInProcessButton);

        serialNumberColumn = workingDBColumnFactory.getSerialNumberColumn();
        statusColumn = workingDBColumnFactory.getStatusColumn();
        deliveryDateColumn = workingDBColumnFactory.getDeliveryDateColumn();
        deliveryTimeColumn = workingDBColumnFactory.getDeliveryTimeColumn();
        addressColumn = workingDBColumnFactory.getAddressColumn();
        invoiceColumn = workingDBColumnFactory.getInvoiceColumn();
        driverColumn = workingDBColumnFactory.getDriverColumn();

        dbTableView.getColumns().addAll(statusColumn, addressColumn, deliveryDateColumn, serialNumberColumn, driverColumn);

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
        createCustomTooltip(expandButton, 38, 10, expandTooltip);
        createCustomTooltip(composeContractsButton, 38, 10, composeContractsTooltip);
        createCustomTooltip(refreshDataButton, 38, 10, refreshDataTooltip);
        createCustomTooltip(deleteButton, 38, 10, deleteTooltip);

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
                        /*if (selectedViewButton != null) {
                            String view = selectedViewButton.getText();
                            switch (view) {
                                case "Status":
                                    dbTableView.getColumns().clear();
                                    dbTableView.getColumns().addAll(statusColumn, addressColumn, deliveryDateColumn, serialNumberColumn, driverColumn);
                                    break;
                                case "Customer":
                                    dbTableView.getColumns().clear();
                                    dbTableView.getColumns().addAll(statusColumn, addressColumn, invoiceColumn);
                                    break;
                                case "Driver":
                                    dbTableView.getColumns().clear();
                                    dbTableView.getColumns().addAll(statusColumn, addressColumn, serialNumberColumn, driverColumn);
                                    break;
                            }
                        }
                        dbTableView.refresh();*/
                    } else if (selectedViewButton == toggleButton) {
                        toggleButton.getStyleClass().removeAll("view-type-button-stopped");
                        toggleButton.getStyleClass().add("view-type-button-rotating");
                        areViewsRotating = true;
                        viewsToggleGroup.selectToggle(statusButton);
                        handleViewSelect(event,"expand");
                        startHighlightRotation(viewsToggleGroup);
                        handleViewSettingSelect("All Rentals", null);
                    } else {
                        toggleButton.getStyleClass().removeAll("view-type-button-rotating");
                        toggleButton.getStyleClass().add("view-type-button-stopped");
                        selectedViewButton.getStyleClass().removeAll("view-type-button-stopped");
                        selectedViewButton.getStyleClass().add("view-type-button-rotating");
                        selectedViewButton = toggleButton;
                        handleViewSelect(event, "collapse");
                        /*if (selectedViewButton != null) {
                            String view = selectedViewButton.getText();
                            switch (view) {
                                case "Status":
                                    dbTableView.getColumns().clear();
                                    dbTableView.getColumns().addAll(statusColumn, addressColumn, deliveryDateColumn, serialNumberColumn, driverColumn);
                                    break;
                                case "Customer":
                                    dbTableView.getColumns().clear();
                                    dbTableView.getColumns().addAll(statusColumn, addressColumn, invoiceColumn);
                                    break;
                                case "Driver":
                                    dbTableView.getColumns().clear();
                                    dbTableView.getColumns().addAll(statusColumn, addressColumn, serialNumberColumn, driverColumn);
                                    break;
                            }
                        }
                        dbTableView.refresh();*/
                    }
                });
            }
        }
        viewsToggleGroup.selectToggle(statusButton);
        startHighlightRotation(viewsToggleGroup);


        for (javafx.scene.Node node : statusesPane.getChildren()) {
            node.getStyleClass().add("status-type-button-rotating");
            if (node instanceof ToggleButton toggleButton) {
                toggleButton.setToggleGroup(statusViewToggleGroup);

                toggleButton.setOnAction(event -> {
                    if (toggleButton.isSelected()) {
                        System.out.println("Selected: " + toggleButton.getText());
                        ToggleButton selectedViewButton = (ToggleButton) viewsToggleGroup.getSelectedToggle();
                        if (selectedViewButton != null) {
                            String view = selectedViewButton.getText();
                            switch (view) {
                                case "Status":
                                    handleSettingInStatusSelect();
                                    break;
                                case "Customer":
                                    handleViewAndCustomerSelect();
                                    break;
                                case "Driver":
                                    handleViewAndDriverSelect();
                                    break;
                            }
                        }
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
                        ToggleButton selectedViewButton = (ToggleButton) viewsToggleGroup.getSelectedToggle();
                        if (selectedViewButton != null) {
                            String view = selectedViewButton.getText();
                            switch (view) {
                                case "Status":
                                    handleSettingInStatusSelect();
                                    break;
                                case "Customer":
                                    handleViewAndCustomerSelect();
                                    break;
                                case "Driver":
                                    handleViewAndDriverSelect();
                                    break;
                            }
                        }
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
                    if (selectedViewButton != null) {
                        String view = selectedViewButton.getText();
                        switch (view) {
                            case "Status":
                                handleSettingInStatusSelect();
                                break;
                            case "Customer":
                                handleViewAndCustomerSelect();
                                break;
                            case "Driver":
                                handleViewAndDriverSelect();
                                break;
                        }
                    }
                } else if (selectedStatusButton == toggleButton) {
                    toggleButton.getStyleClass().removeAll("status-type-button-stopped");
                    toggleButton.getStyleClass().add("status-type-button-rotating");
                    areStatusesRotating = true;
                    statusViewToggleGroup.selectToggle((ToggleButton) statusesPane.getChildren().get(0));
                    startHighlightRotation(statusViewToggleGroup);
                    handleViewSelect(null, "expand");
                } else {
                    toggleButton.getStyleClass().removeAll("status-type-button-rotating");
                    toggleButton.getStyleClass().add("status-type-button-stopped");
                    selectedStatusButton.getStyleClass().removeAll("status-type-button-stopped");
                    selectedStatusButton.getStyleClass().add("status-type-button-rotating");
                    selectedStatusButton = toggleButton;
                    if (selectedViewButton != null) {
                        String view = selectedViewButton.getText();
                        switch (view) {
                            case "Status":
                                handleSettingInStatusSelect();
                                break;
                            case "Customer":
                                handleViewAndCustomerSelect();
                                break;
                            case "Driver":
                                handleViewAndDriverSelect();
                                break;
                        }
                    }
                }
            });
        }

        loadCustomers();

        driverComboBox.setOnAction(event -> {
            handleViewAndDriverSelect();
        });

        customerComboBox.setOnAction(event -> {
            handleViewAndCustomerSelect();
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

        String query = "SELECT customers.*, rental_orders.*, rental_items.*, lifts.*, " +
                "ordered_contacts.first_name AS ordered_contact_name, " +
                "ordered_contacts.phone_number AS ordered_contact_phone, " +
                "site_contacts.first_name AS site_contact_name, " +
                "site_contacts.phone_number AS site_contact_phone " +
                "FROM customers " +
                "JOIN rental_orders ON customers.customer_id = rental_orders.customer_id " +
                "JOIN rental_items ON rental_orders.rental_order_id = rental_items.rental_order_id " +
                "JOIN lifts ON rental_items.lift_id = lifts.lift_id " +
                "LEFT JOIN contacts AS ordered_contacts ON rental_items.ordered_contact_id = ordered_contacts.contact_id " +
                "LEFT JOIN contacts AS site_contacts ON rental_items.site_contact_id = site_contacts.contact_id ";

        String customerName = customerComboBox.getValue();
        String date = "";
        String startDate = "";
        if (datePickerOne.getValue() != null) {
            date = datePickerOne.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            startDate = date;
        }
        String endDate = "";
        if (datePickerTwo.getValue() != null) {
            endDate = datePickerTwo.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        boolean fixInitials = false;

        switch (filter) {
            case "Active":
                query += " WHERE rental_items.item_status = 'Active'";
                break;
            case "Billable":
                query += " WHERE rental_items.item_status IN ('Called Off', 'Ended') AND rental_items.invoice_composed = 0";
                break;
            case "Upcoming":
                query += " WHERE rental_items.item_status = 'Upcoming'";
                break;
            case "Called Off":
                query += " WHERE rental_items.item_status = 'Called Off'";
                fixInitials = true;
                break;
            case "Active One Date":
                query += " WHERE rental_items.item_status = 'Active' AND rental_orders.delivery_date = '" + date + "'";
                break;
            case "Billable One Date":
                query += " WHERE rental_items.item_status IN ('Called Off', 'Ended') AND rental_items.invoice_composed = 0 " +
                         "AND rental_orders.delivery_date = '" + date + "'";
                break;
            case "Upcoming One Date":
                query += " WHERE rental_items.item_status = 'Upcoming' AND rental_orders.delivery_date = '" + date + "'";
                fixInitials = true;
                break;
            case "Called Off One Date":
                query += " WHERE rental_items.item_status = 'Called Off' AND rental_orders.delivery_date = '" + date + "'";
                fixInitials = true;
                break;
            case "Active Interval":
                query += " WHERE rental_items.item_status = 'Active' AND rental_orders.delivery_date BETWEEN '" + startDate + "' AND '" + endDate + "'";
                break;
            case "Billable Interval":
                query += " WHERE rental_items.item_status IN ('Called Off', 'Ended') AND rental_items.invoice_composed = 0 " +
                         "AND rental_orders.delivery_date BETWEEN '" + startDate + "' AND '" + endDate + "'";
                break;
            case "Upcoming Interval":
                query += " WHERE rental_items.item_status = 'Upcoming' AND rental_orders.delivery_date BETWEEN '" + startDate + "' AND '" + endDate + "'";
                break;
            case "Called Off Interval":
                query += " WHERE rental_items.item_status = 'Called Off' AND rental_orders.delivery_date BETWEEN '" + startDate + "' AND '" + endDate + "'";
                break;
            case "Customer":
                query += " WHERE customers.customer_name = '" + customerName + "'";
                break;
            case "Customer One Date":
                query += " WHERE customers.customer_name = '" + customerName + "' AND rental_orders.delivery_date = '" + date + "'";
                break;
            case "Customer Interval":
                query += " WHERE customers.customer_name = '" + customerName + "' AND rental_orders.delivery_date BETWEEN '" + startDate + "' AND '" + endDate + "'";
                break;
            case "Driver":
                query += " WHERE rental_items.driver_initial = '" + driverComboBox.getValue() + "'";
                break;
            case "Driver One Date":
                query += " WHERE rental_items.driver_initial = '" + driverComboBox.getValue() + "' AND rental_orders.delivery_date = '" + date + "'";
                fixInitials = true;
                break;
            case "Driver Interval":
                query += " WHERE rental_items.driver_initial = '" + driverComboBox.getValue() + "' " +
                         "AND rental_orders.delivery_date BETWEEN '" + startDate + "' AND '" + endDate + "'";
                break;
            case "One Date":
                query += " WHERE rental_orders.delivery_date = '" + date + "'";
                fixInitials = true;
                break;
            case "Interval":
                query += " WHERE rental_orders.delivery_date BETWEEN '" + startDate + "' AND '" + endDate + "'";
                break;
            case "All Rentals":
                // No additional filtering needed
                break;
            default:
                throw new IllegalArgumentException("Unknown filter: " + filter);
        }


        // Add LIMIT clause at the end
        query += " LIMIT 100";
        System.out.println(query);

        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String name = resultSet.getString("customer_name");
                String orderedContactName = resultSet.getString("ordered_contact_name");
                String orderedContactPhone = resultSet.getString("ordered_contact_phone");
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
                String cityState = resultSet.getString("city");
                String siteContactName = resultSet.getString("site_contact_name");
                String siteContactPhone = resultSet.getString("site_contact_phone");
                String poNumber = resultSet.getString("po_number");
                String locationNotes = resultSet.getString("location_notes");
                String preTripInstructions = resultSet.getString("pre_trip_instructions");

                CustomerRental rental = new CustomerRental("0", name, deliveryDate, deliveryTime, driver != null ? driver : "", status != null ? status : "Unknown", "999999", rental_id);
                rental.setRentalItemId(rental_item_id);
                rental.setOrderedByName(orderedContactName);
                rental.setOrderedByPhone(orderedContactPhone);
                rental.setLiftId(liftId);
                rental.setLiftType(liftType);
                rental.setAddressBlockOne(siteName);
                rental.setAddressBlockTwo(streetAddress);
                rental.setAddressBlockThree(cityState);
                rental.splitAddressBlockTwo();
                rental.setSiteContactName(siteContactName);
                rental.setSiteContactPhone(siteContactPhone);
                rental.setPoNumber(poNumber);
                rental.setLocationNotes(locationNotes);
                rental.setPreTripInstructions(preTripInstructions);
                rental.setSerialNumber(serialNumber);
                ordersList.add(rental);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (fixInitials) {
            fixCurrentViewInitials(0);
        }

    }


    private void startHighlightRotation(ToggleGroup toggleGroup) {
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
            selectedViewButton = statusButton; // Set an initial toggle button
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

        // Define the keyframe to toggle through views
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1.5), event -> {
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
            if (selectedViewButton == statusButton) {
                return customerButton;
            } else if (selectedViewButton == customerButton) {
                return driverButton;
            } else if (selectedViewButton == driverButton) {
                return statusButton;
            } else {
                // Return a default button if the current selection is null or unrecognized
                return statusButton;
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

    private void animateScrollBars(TableView<CustomerRental> tableView) {
       Platform.runLater(() -> {
           int i = 0;
           // Assuming your scroll bars are part of the dbTableView's skin
           for (Node node : tableView.lookupAll(".scroll-bar")) {
               System.out.println("animating for:" + node);
               if (node instanceof ScrollBar) {
                   // Create a glow effect for the scroll bar
                   DropShadow glowEffect = new DropShadow();
                   glowEffect.setRadius(10);
                   glowEffect.setSpread(0.5);
                   node.setEffect(glowEffect);


                   // Create a glow animation
                   glowTimelines[i] = new Timeline(
                           new KeyFrame(Duration.ZERO,
                                   new KeyValue(glowEffect.colorProperty(),
                                           timelineFlagger ? Color.web("#FFDEAD", 0.5) : Color.web("#FFDEAD", 0.5))
                           ),
                           new KeyFrame(Duration.seconds(1),
                                   new KeyValue(glowEffect.colorProperty(),
                                           timelineFlagger ? Color.web("#000000", 0.0) : Color.web("#FF7F00", 1.0))
                           )
                   );


                   glowTimelines[i].setCycleCount(Timeline.INDEFINITE);
                   glowTimelines[i].setAutoReverse(true);
                   glowTimelines[i].play();
               }
           }
       });
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

    private void loadCustomers() {
        String query = "SELECT customer_name FROM customers";
        ObservableList<String> customers = FXCollections.observableArrayList();

        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                customers.add(resultSet.getString("customer_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        FXCollections.sort(customers, (customer1, customer2) -> customer1.compareTo(customer2));
        customerComboBox.setItems(customers);
    }


    private void shiftSidebarHighlighter(String nextActionType) {
        if (nextActionType == null) {
            sideBarHighlighter.setVisible(false);
            updateHighlighterPosition(lastActionType, -1);
            lastActionType = null;
        } else {
            sideBarHighlighter.setVisible(true);
            if (lastActionType == null) {
                updateHighlighterPosition(nextActionType, 1);
                lastActionType = nextActionType;
            } else {
                lastActionType = null;
                shiftSidebarHighlighter(nextActionType);
            }
        }
    }

    private void updateHighlighterPosition(String actionType, int direction) {
        double downset = 44; // Distance for each sidebar item
        switch (actionType) {
            case "composing-contracts":
                sideBarHighlighter.setTranslateY(downset * 0 * direction);
                break;
            case "assigning-drivers":
                sideBarHighlighter.setTranslateY(downset * 1 * direction);
                sideBarHighlighter.setTranslateX(direction * -1);
                break;
            case "dropping-off":
                sideBarHighlighter.setTranslateY(downset * 2 * direction - 2);
                sideBarHighlighter.setTranslateX(direction * 1);
                break;
            case "calling-off":
                sideBarHighlighter.setTranslateY(downset * 3 * direction - 1);
                sideBarHighlighter.setTranslateX(direction * 0);
                break;
            case "picking-up":
                sideBarHighlighter.setTranslateY(downset * 4 * direction - 1);
                sideBarHighlighter.setTranslateX(direction * 1);
                break;
            case "composing-invoices":
                sideBarHighlighter.setTranslateY(downset * 5 * direction);
                sideBarHighlighter.setTranslateX(direction * 0);
                break;
            case "expanding":
                sideBarHighlighter.setTranslateY(downset * 6 * direction);
                sideBarHighlighter.setTranslateX(direction * 0);
                break;
            case "refreshing-data":
                sideBarHighlighter.setTranslateY(downset * 7 * direction);
                sideBarHighlighter.setTranslateX(direction * -1);
                break;
            case "deleting":
                sideBarHighlighter.setTranslateY(downset * 8 * direction);
                sideBarHighlighter.setTranslateX(direction * 0);
                break;
            default:
                sideBarHighlighter.setVisible(false);
                break;
        }
    }



    @FXML
    private void handleViewAndDriverSelect(){
        if (driverComboBox.getValue() != null) {
            if (datePickerOne.getValue() == null && datePickerTwo.getValue() == null) {
                handleViewSettingSelect("Driver", null);
            } else if (datePickerOne.getValue() != null && datePickerTwo.getValue() == null) {
                handleViewSettingSelect("Driver One Date", null);
            } else {
                handleViewSettingSelect("Driver Interval", null);
            }
        } else {
            if (datePickerOne.getValue() == null && datePickerTwo.getValue() == null) {
                handleViewSettingSelect("All Rentals", null);
            } else if (datePickerOne.getValue() != null && datePickerTwo.getValue() == null) {
                handleViewSettingSelect("One Date", null);
            } else {
                handleViewSettingSelect("Interval", null);
            }
        }
    }

    @FXML
    private void handleViewAndCustomerSelect(){
        if (customerComboBox.getValue() != null) {
            if (datePickerOne.getValue() == null && datePickerTwo.getValue() == null) {
                handleViewSettingSelect("Customer", null);
            } else if (datePickerOne.getValue() != null && datePickerTwo.getValue() == null) {
                handleViewSettingSelect("Customer One Date", null);
            } else {
                handleViewSettingSelect("Customer Interval", null);
            }
        } else {
            if (datePickerOne.getValue() == null && datePickerTwo.getValue() == null) {
                handleViewSettingSelect("All Rentals", null);
            } else if (datePickerOne.getValue() != null && datePickerTwo.getValue() == null) {
                handleViewSettingSelect("One Date", null);
            } else {
                handleViewSettingSelect("Interval", null);
            }
        }
    }

    private void handleSettingInStatusSelect(){
        String status = null;
        if (!areStatusesRotating) {
            ToggleButton selectedStatusButton = (ToggleButton) statusViewToggleGroup.getSelectedToggle();
            status = selectedStatusButton.getText();
        }
        if (datePickerOne.getValue() == null && datePickerTwo.getValue() == null){
            handleViewSettingSelect("Status", status);
        } else if (datePickerOne.getValue() != null && datePickerTwo.getValue() == null) {
            handleViewSettingSelect("Status One Date", status);
        } else {
            handleViewSettingSelect("Status Interval", status);
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
                ToggleButton selectedViewButton = (ToggleButton) viewsToggleGroup.getSelectedToggle();
                if (selectedViewButton != null) {
                    String view = selectedViewButton.getText();
                    switch (view) {
                        case "Status":
                            handleSettingInStatusSelect();
                            System.out.println("Going to handleSettingInStatusSelect from datePickerOne");
                            break;
                        case "Customer":
                            handleViewAndCustomerSelect();
                            System.out.println("Going to handleViewAndCustomerSelect from datePickerOne");
                            break;
                        case "Driver":
                            handleViewAndDriverSelect();
                            System.out.println("Going to handleViewAndDriverSelect from datePickerOne");
                            break;
                    }
                }
            }
        });

        datePicker.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                isDatePickerExpanded.set(false);
            }
        });
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
        if (tryCounter >= 20) {
            System.out.println("Reached maximum retry limit (20). Exiting.");
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
    private void handleAssignDrivers() {
        String actionType = "assigning-drivers";
        if (actionType.equals(lastActionType)) {
            updateRentalButton.setVisible(false); // Hide the update button
            workingDBColumnFactory.setClosedDriverColumn(); // Reset driver column
            shiftSidebarHighlighter(null); // Reset sidebar highlighter
            System.out.println("Driver assignment mode deactivated.");
        } else {
            updateRentalButton.setVisible(true); // Show the update button
            workingDBColumnFactory.setOpenDriverColumn(); // Open driver column for assignment
            shiftSidebarHighlighter(actionType); // Highlight the sidebar for driver assignment
            System.out.println("Driver assignment mode activated.");
        }
        dbTableView.refresh(); // Refresh the table view
    }


   @FXML
    private void handleDroppingOff(ActionEvent event) {
        String actionType = "dropping-off";
        if (actionType.equals(lastActionType)) {
            hideCheckboxes();
            workingDBColumnFactory.showSelectableCheckboxes(false, null);
            updateRentalButton.setVisible(false);
            serialNumberField.setVisible(false);
            shiftSidebarHighlighter(null); // Reset sidebar highlighter
            shiftUpdateButtonFull();
        } else {
            resetCheckboxes();
            workingDBColumnFactory.showSelectableCheckboxes(true, actionType);
            serialNumberField.setVisible(true);
            shiftSidebarHighlighter(actionType); // Update sidebar highlighter
            shiftUpdateButtonHalf();
        }
    }

    @FXML
    private void handleCallingOff(ActionEvent event) {
        String actionType = "calling-off";
        if (actionType.equals(lastActionType)) {
            hideCheckboxes();
            workingDBColumnFactory.showSelectableCheckboxes(false, null);
            updateRentalButton.setVisible(false);
            shiftSidebarHighlighter(null);
        } else {
            resetCheckboxes();
            workingDBColumnFactory.showSelectableCheckboxes(true, actionType);
            serialNumberField.setVisible(false);
            shiftSidebarHighlighter(actionType);
            shiftUpdateButtonFull();
        }
    }

    @FXML
    private void handlePickingUp(ActionEvent event) {
        String actionType = "picking-up";
        if (actionType.equals(lastActionType)) {
            hideCheckboxes();
            workingDBColumnFactory.showSelectableCheckboxes(false, null);
            updateRentalButton.setVisible(false);
            shiftSidebarHighlighter(null);
        } else {
            resetCheckboxes();
            workingDBColumnFactory.showSelectableCheckboxes(true, actionType);
            serialNumberField.setVisible(false);
            shiftSidebarHighlighter(actionType);
            shiftUpdateButtonFull();
        }
    }

    @FXML
    private void handleComposeInvoices(ActionEvent event) {
        String actionType = "composing-invoices";
        if (actionType.equals(lastActionType)) {
            hideCheckboxes();
            workingDBColumnFactory.showSelectableCheckboxes(false, null);
            updateRentalButton.setVisible(false);
            shiftSidebarHighlighter(null);
        } else {
            resetCheckboxes();
            workingDBColumnFactory.showSelectableCheckboxes(true, actionType);
            serialNumberField.setVisible(false);
            shiftSidebarHighlighter(actionType);
            shiftUpdateButtonFull();
        }
    }

    @FXML
    private void handleComposeContracts(ActionEvent event) {
        String actionType = "composing-contracts";
        if (actionType.equals(lastActionType)) {
            shiftSidebarHighlighter(null);
            hideCheckboxes();
            workingDBColumnFactory.showSelectableCheckboxes(false, null);
            updateRentalButton.setVisible(false);
            composeContractsButton.setVisible(false);
            secondInProcessButton.setVisible(false);
            } else {
            shiftSidebarHighlighter(actionType);
            resetCheckboxes();
            workingDBColumnFactory.showSelectableCheckboxes(true, actionType);
            serialNumberField.setVisible(false);
            shiftUpdateButtonFull();
        }
    }

    @FXML
    private void handleExpand(ActionEvent event) {
        String actionType = "expanding";
        if (actionType.equals(lastActionType)) {
            workingDBColumnFactory.showExpandIcons(false);
            shiftSidebarHighlighter(null);
        } else {
            workingDBColumnFactory.showExpandIcons(true);
            serialNumberField.setVisible(false);
            shiftSidebarHighlighter(actionType);
            shiftUpdateButtonFull();
        }
        dbTableView.refresh();
    }



    // Refresh the table view data
    @FXML
    private void handleRefreshData() {
        dbTableView.refresh();
    }


    @FXML
    private void handleDelete(ActionEvent event) {
        String actionType = "deleting";
        if (actionType.equals(lastActionType)) {
            hideCheckboxes();
            workingDBColumnFactory.showSelectableCheckboxes(false, null);
            updateRentalButton.setVisible(false);
            shiftSidebarHighlighter(null); // Reset sidebar highlighter
        } else {
            resetCheckboxes();
            workingDBColumnFactory.showSelectableCheckboxes(true, actionType);
            shiftSidebarHighlighter(actionType); // Update sidebar highlighter
            System.out.println("Delete button pressed.");
            if (serialNumberField.isVisible()) {
                serialNumberField.setVisible(false);
                shiftUpdateButtonFull();
            }
            shiftSidebarHighlighter(actionType);
        }
    }


    private void resetCheckboxes() {
        // Deselect all checkboxes in the table
        for (CustomerRental order : dbTableView.getItems()) {
            order.setSelected(false);
        }
        dbTableView.refresh(); // Refresh the table view to update the checkbox states
    }


    @FXML
    private void handleUpdateRental(ActionEvent event) {
        ObservableList<CustomerRental> selectedRentals = dbTableView.getItems().filtered(CustomerRental::isSelected);
        boolean statusUpdated = false;
        Date today = new Date(); // Today's date

        // Handle the 'creating-invoices' action type
        if (lastActionType.equals("composing-invoices")) {
            statusUpdated = true;
            checkAndSwitchScene(statusUpdated);
        } else if (lastActionType.equals("composing-contracts")) {
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
                    System.out.println("Order for " + order.getName() + " marked as Active with today's delivery date.");
                    if (serialNumberField.getText().length() >= 4) {
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
        } else if (lastActionType.equals("deleting")) {
            List<CustomerRental> itemsToRemove = new ArrayList<>();
            for (CustomerRental order : selectedRentals) {
                deleteRentalFromDB(order.getRentalItemId());
                itemsToRemove.add(order);
                statusUpdated = true;
            }
            ordersList.removeAll(itemsToRemove);
            selectedRentals.removeAll(itemsToRemove);
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


    private void deleteRentalFromDB(int rentalItemId) {
        String deleteQuery = "DELETE FROM rental_items WHERE rental_item_id = ?";


        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             PreparedStatement statement = connection.prepareStatement(deleteQuery)) {


            statement.setInt(1, rentalItemId);
            int rowsDeleted = statement.executeUpdate();


            if (rowsDeleted > 0) {
                System.out.println("Rental item with ID " + rentalItemId + " successfully deleted.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    private void switchToExpandScene() {
        try {
            MaxReachPro.loadScene("/fxml/expand.fxml"); // Replace with your scene path
        } catch (Exception e) {
            e.printStackTrace();
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
            return 155;
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

        composeContractsButton.setVisible(false);
        secondInProcessButton.setVisible(false);
    }


    private void handleViewSelect(ActionEvent event, String orientation) {
        datePickerOne.setValue(null);
        datePickerTwo.setValue(null);
        datePickerOneLabel.setText("From:");
        datePickerTwoLabel.setText("To:");

        ToggleButton selectedButton = event.getSource() instanceof ToggleButton ? (ToggleButton) event.getSource() : (ToggleButton) viewsToggleGroup.getSelectedToggle();

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
                case "Customer":
                    leftSideVboxCustomerView.setVisible(true);
                    latestLeftSideVbox = leftSideVboxCustomerView;
                    datePickersPane.setVisible(true);
                    latestRightSideVbox = datePickersPane;
                    datePickerOneLabel.setVisible(true);
                    datePickerOneCover.setVisible(true);
                    calendarButtonOne.setVisible(true);
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


    private void handleViewSettingSelect (String viewType, String status) {
        dbTableView.getColumns().clear();

        dbTableView.getColumns().addAll(statusColumn, addressColumn);
        switch (viewType) {
             case "Status":
                switch (status) {
                    case "Active":
                        dbTableView.getColumns().add(serialNumberColumn);
                        loadData("Active");
                        break;
                    case "Billable":
                        dbTableView.getColumns().add(invoiceColumn);
                        loadData("Billable");
                        break;
                    case "Upcoming":
                        dbTableView.getColumns().add(deliveryDateColumn);
                        loadData("Upcoming");
                        break;
                    case "Called Off":
                        dbTableView.getColumns().addAll(serialNumberColumn, driverColumn);
                        loadData("Called Off");
                        break;
                    case null:
                        dbTableView.getColumns().addAll(deliveryDateColumn, serialNumberColumn, driverColumn);
                        loadData("All Rentals");
                        break;
                    default:
                        dbTableView.getColumns().addAll(deliveryDateColumn, serialNumberColumn, driverColumn);
                        loadData("All Rentals");
                        break;
                }
                break;
            case "Status One Date":
                switch (status) {
                    case "Active":
                        dbTableView.getColumns().add(serialNumberColumn);
                        loadData("Active One Date");
                        break;
                    case "Billable":
                        dbTableView.getColumns().add(invoiceColumn);
                        loadData("Billable One Date");
                        break;
                    case "Upcoming":
                        dbTableView.getColumns().add(deliveryDateColumn);
                        loadData("Upcoming One Date");
                        break;
                    case "Called Off":
                        dbTableView.getColumns().addAll(serialNumberColumn, driverColumn);
                        loadData("Called Off One Date");
                        break;
                    case null:
                        dbTableView.getColumns().addAll(deliveryDateColumn, serialNumberColumn, driverColumn);
                        loadData("One Date");
                        break;
                    default:
                        dbTableView.getColumns().addAll(deliveryDateColumn, serialNumberColumn, driverColumn);
                        loadData("One Date");
                        break;
                }
                break;
            case "Status Interval":
                switch (status) {
                    case "Active":
                        dbTableView.getColumns().add(serialNumberColumn);
                        loadData("Active Interval");
                        break;
                    case "Billable":
                        dbTableView.getColumns().add(invoiceColumn);
                        loadData("Billable Interval");
                        break;
                    case "Upcoming":
                        dbTableView.getColumns().add(deliveryDateColumn);
                        loadData("Upcoming Interval");
                        break;
                    case "Called Off":
                        dbTableView.getColumns().addAll(serialNumberColumn, driverColumn);
                        loadData("Called Off Interval");
                        break;
                    case null:
                        dbTableView.getColumns().addAll(deliveryDateColumn, serialNumberColumn, driverColumn);
                        loadData("Interval");
                        break;
                    default:
                        dbTableView.getColumns().addAll(deliveryDateColumn, serialNumberColumn, driverColumn);
                        loadData("Interval");
                        break;
                }
                break;
            case "Customer":
                dbTableView.getColumns().add(invoiceColumn);
                loadData("Customer");
                break;
            case "Customer One Date":
                dbTableView.getColumns().add(invoiceColumn);
                loadData("Customer One Date");
                break;
            case "Customer Interval":
                dbTableView.getColumns().add(invoiceColumn);
                loadData("Customer Interval");
                break;
            case "Driver":
                dbTableView.getColumns().addAll(serialNumberColumn, driverColumn);
                loadData("Driver");
                break;
            case "Driver One Date":
                dbTableView.getColumns().addAll(serialNumberColumn, driverColumn);
                loadData("Driver One Date");
                break;
            case "Driver Interval":
                dbTableView.getColumns().addAll(serialNumberColumn, driverColumn);
                loadData("Driver Interval");
                break;
            case "One Date":
                dbTableView.getColumns().addAll(deliveryDateColumn, serialNumberColumn, driverColumn);
                loadData("One Date");
                break;
            case "Interval":
                dbTableView.getColumns().addAll(deliveryDateColumn, serialNumberColumn, driverColumn);
                loadData("Interval");
                break;
            default:
                dbTableView.getColumns().addAll(deliveryDateColumn, serialNumberColumn, driverColumn);
                loadData("All Rentals");
                break;
        }
        dbTableView.refresh();
    }

    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() != 10) {
            throw new IllegalArgumentException("Input must be a 10-digit number.");
        }

        return "(" + phoneNumber.substring(0, 3) + ")-" + phoneNumber.substring(3, 6) + "-" + phoneNumber.substring(6, 10);
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

    @FXML
    private void handleBatchContracts(ActionEvent event) {
        ObservableList<CustomerRental> selectedRentals = dbTableView.getItems().filtered(CustomerRental::isSelected);

        if (selectedRentals.isEmpty()) {
            System.out.println("No rentals selected. Cannot compose contracts.");
            return;
        }

        String sourceFile = "C:/Users/maxhi/OneDrive/Documents/Quickbooks/QBProgram Development/Composing Contracts/contract template.pdf";

        // List to store generated PDF filenames
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

                canvas.setTextMatrix(449, 747); // Delivery Time
                canvas.showText("P" + String.valueOf(rental.getRentalItemId()));

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


        // Now, merge the individual PDFs into one file
        if (!createdPdfFiles.isEmpty()) {
            // Get today's date for naming the final PDF
            String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String finalOutputFile = "C:/Users/maxhi/OneDrive/Documents/Quickbooks/QBProgram Development/Composing Contracts/contracts_" + todayDate + ".pdf";


            try {
                // Create PdfDocument for the final output file
                PdfDocument finalPdfDoc = new PdfDocument(new PdfWriter(finalOutputFile));


                // Create PdfMerger to merge PDFs
                PdfMerger merger = new PdfMerger(finalPdfDoc);


                // Merge each created PDF into the final document
                for (String pdfFile : createdPdfFiles) {
                    PdfDocument docToMerge = new PdfDocument(new PdfReader(pdfFile));
                    merger.merge(docToMerge, 1, docToMerge.getNumberOfPages());
                    docToMerge.close();
                }


                // Close the final merged document
                finalPdfDoc.close();


                System.out.println("All contracts merged into: " + finalOutputFile);


                // Clean up the individual PDFs after merging
                for (String pdfFile : createdPdfFiles) {
                    File file = new File(pdfFile);
                    if (file.exists()) {
                        if (file.delete()) {
                            System.out.println("Deleted temporary file: " + pdfFile);
                        } else {
                            System.out.println("Failed to delete temporary file: " + pdfFile);
                        }
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
    }

    @FXML
    private void handleSecondInProcess(ActionEvent event) {
        // Get today's date to match the final merged PDF filename
        String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String finalOutputFile = "C:/Users/maxhi/OneDrive/Documents/Quickbooks/QBProgram Development/Composing Contracts/contracts_" + todayDate + ".pdf";

        // Check if the generated file exists
        File contractFile = new File(finalOutputFile);
        if (contractFile.exists()) {
            try {
                // Open the file using the default system PDF viewer
                Desktop.getDesktop().open(contractFile);
                System.out.println("Opened contract: " + finalOutputFile);
            } catch (IOException e) {
                System.out.println("Error opening contract file: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Generated contract file not found: " + finalOutputFile);
        }
    }


}
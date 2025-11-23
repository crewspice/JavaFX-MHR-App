package com.MaxHighReach;

import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.utils.PdfMerger;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.String;
import java.nio.file.Paths;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.sql.*;


import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;

import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ActivityController extends BaseController {

    private String IMAGE_PATH_BASE = Paths.get(PathConfig.IMAGES_DIR).toString();
    private String IMAGE_PATH_INV_SUFFIX = "-inv.png";
    @FXML
    private Button backButton;
    @FXML
    private VBox buttonsVBox;
    @FXML
    private Button editDriverButton;
    @FXML
    private Button cancellingButton;
    @FXML
    private Button droppingOffButton;
    @FXML
    private Button callingOffButton;
    @FXML
    private Button schedulingServiceButton;
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
    private final Tooltip cancellingTooltip = new Tooltip("Cancel");    
    private final Tooltip droppingOffTooltip = new Tooltip("Record Drop Off");
    private final Tooltip callingOffTooltip = new Tooltip("Record Call Off");
    private final Tooltip schedulingServiceTooltip = new Tooltip("Schedule Service");
    private final Tooltip pickingUpTooltip = new Tooltip("Record Pick Up");
    private final Tooltip composeInvoicesTooltip = new Tooltip("Compose Invoices");
    private final Tooltip expandTooltip = new Tooltip("Expand Rental");
    private final Tooltip refreshDataTooltip = new Tooltip("Refresh Table");
    private final Tooltip deleteTooltip = new Tooltip("Delete Rental");
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Rectangle dragArea;
    @FXML
    private TableView<Rental> dbTableView;
    private ColumnFactory workingColumnFactory;
    TableColumn<Rental, String> serialNumberColumn = new TableColumn<>();
    TableColumn<Rental, Boolean> statusColumn = new TableColumn<>();
    TableColumn<Rental, String> deliveryDateColumn = new TableColumn<>();
    TableColumn<Rental, String> deliveryTimeColumn = new TableColumn<>();
    TableColumn<Rental, String> callOffDateColumn = new TableColumn<>();
    TableColumn<Rental, String> biModalDateColumn = new TableColumn<>();
    TableColumn<Rental, String> addressColumn = new TableColumn<>();
    TableColumn<Rental, String> invoiceColumn = new TableColumn<>();
    TableColumn<Rental, String> driverColumn = new TableColumn<>();
    private String latestFilter = "All Rentals";
    @FXML
    private HBox viewsTilePane;
    private ToggleGroup viewsToggleGroup = new ToggleGroup();
    private List<ToggleButton> viewsToggleButtons = new ArrayList<>();
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
    private List<ToggleButton> statusesToggleButtons = new ArrayList<>();
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

    private ObservableList<Rental> ordersList = FXCollections.observableArrayList();
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
    private String driverColumnType = "po-number";

// retry globals
    private ObservableList<String> potentialInitials = FXCollections.observableArrayList();
    private ObservableList<String> currentViewInitials = FXCollections.observableArrayList();


    private Map<String, List<Rental>> groupedRentals = new HashMap<>();
    private Map<String, Integer> driverSequenceMap = new HashMap<>();

    private String initialsErrorCode;
    private String initialsErrorPointerKey;

    private javafx.scene.text.Text liftTypeText = new javafx.scene.text.Text();
    private static DateTimeFormatter fromJavaObjectFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        super.initialize(dragArea);

        dbTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        animateScrollBars(dbTableView);

        dbTableView.setOnMouseExited(event -> {
            timelineFlagger = true;
        });

        dbTableView.setOnMouseEntered(event -> {
            timelineFlagger = false;
        });

        dbTableView.setEditable(true);

        workingColumnFactory = new ColumnFactory(updateRentalButton, serialNumberField, dbTableView, groupedRentals,
                driverSequenceMap, batchButton, batchSwitcher, secondInProcessButton, this);
        workingColumnFactory.loadExistingDrivers();

        serialNumberColumn = workingColumnFactory.getSerialNumberColumn();
        statusColumn = workingColumnFactory.getStatusColumn();
        deliveryDateColumn = workingColumnFactory.getDeliveryDateColumn();
        deliveryTimeColumn = workingColumnFactory.getDeliveryTimeColumn();
        callOffDateColumn = workingColumnFactory.getCallOffDateColumn();
        biModalDateColumn = workingColumnFactory.getBiModalDateColumn();
        addressColumn = workingColumnFactory.getAddressColumn();
        invoiceColumn = workingColumnFactory.getInvoiceColumn();
        driverColumn = workingColumnFactory.getDriverColumn(driverColumnType);

        dbTableView.getColumns().addAll(statusColumn, addressColumn, biModalDateColumn, serialNumberColumn, driverColumn);
        biModalDateColumn.setPrefWidth(49);

        updateRentalButton.setVisible(false); // Start with the button hidden

        createCustomTooltip(editDriverButton, 38, 10, assignDriverTooltip);
        createCustomTooltip(cancellingButton, 38, 10, cancellingTooltip);
        createCustomTooltip(droppingOffButton, 38, 10, droppingOffTooltip);
        createCustomTooltip(callingOffButton, 38, 10, callingOffTooltip);
        createCustomTooltip(schedulingServiceButton, 38, 10, schedulingServiceTooltip);
        createCustomTooltip(pickingUpButton, 38, 10, pickingUpTooltip);
        createCustomTooltip(composeInvoicesButton, 38, 10, composeInvoicesTooltip);
        createCustomTooltip(expandButton, 38, 10, expandTooltip);
        createCustomTooltip(composeContractsButton, 38, 10, composeContractsTooltip);
        createCustomTooltip(refreshDataButton, 38, 10, refreshDataTooltip);
        createCustomTooltip(deleteButton, 38, 10, deleteTooltip);

        for (javafx.scene.Node node : viewsTilePane.getChildren()) {
            if (node instanceof ToggleButton toggleButton) {
                toggleButton.setToggleGroup(viewsToggleGroup);
                viewsToggleButtons.add((ToggleButton) node);
                toggleButton.setStyle("-fx-font-size: 15; -fx-padding: 1 0; " +
                    "-fx-alignment: center;");
                toggleButton.setOnAction(event -> {
                    if (selectedViewButton == toggleButton) {
                        viewsToggleGroup.selectToggle(null);
                        handleViewSelect("expand");
              //          toggleButton.setStyle("-fx-font-size: 15; -fx-padding: 1 0; " +
              //              "-fx-alignment: center; -fx-background-color: transparent;");
                    } else {
                        viewsToggleGroup.selectToggle(toggleButton);
                        selectedViewButton = toggleButton;
                        handleViewSelect("collapse");
              //          toggleButton.setStyle("-fx-font-size: 15; -fx-padding: 1 0; " +
              //              "-fx-alignment: center; -fx-background-color: orange;");
                    }
                });
            }
        }
        GradientAnimator.applySequentialGradientAnimationToggles(viewsToggleButtons, 0, "view-type-button-stopped");

        for (javafx.scene.Node node : statusesPane.getChildren()) {
            if (node instanceof ToggleButton toggleButton) {
                toggleButton.setToggleGroup(statusViewToggleGroup);
                statusesToggleButtons.add((ToggleButton) node);
                toggleButton.setStyle("-fx-font-size: 12; -fx-padding: 1 0; -fx-aligment: center");
            }
        }


        for (javafx.scene.Node node : statusesPaneTwo.getChildren()) {
            if (node instanceof ToggleButton toggleButton) {
                toggleButton.setToggleGroup(statusViewToggleGroup);
                statusesToggleButtons.add((ToggleButton) node);
                toggleButton.setStyle("-fx-font-size: 12; -fx-padding: 1 0; -fx-aligment: center");
            }
        }
        Collections.swap(statusesToggleButtons, 1, 2);

        for (Toggle toggle : statusViewToggleGroup.getToggles()) {
            ToggleButton toggleButton = (ToggleButton) toggle;
            toggleButton.setOnAction(event -> {
                GradientAnimator.stopAllAnimations();
                if (selectedStatusButton == toggleButton) {
                    handleViewSelect("expand");
                } else {
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
                clearGradientFromToggleSet(statusesToggleButtons, toggleButton, "view-type-button-stopped");
            });
        }

        customers = MaxReachPro.getCustomers(true);
        ObservableList<String> customerNames = FXCollections.observableArrayList();
        for (Customer customer : customers) {
            customerNames.add(customer.getName());
        }
        FXCollections.sort(customerNames, (name1, name2) -> name1.compareTo(name2));
        customerComboBox.setItems(customerNames);
        String customerName = MaxReachPro.getSelectedCustomerName();
        if (customerName != null && customerName != "") {
            customerComboBox.setValue(customerName);
        }

        driverComboBox.setOnAction(event -> {
            handleViewAndDriverSelect();
        });

        customerComboBox.setOnAction(event -> {
            handleViewAndCustomerSelect();
        });
/*
        String storedViewType = MaxReachPro.getSelectedViewSetting();
        String storedDriver = MaxReachPro.getSelectedDriverName();
        String storedCustomer = MaxReachPro.getSelectedCustomerName();
        LocalDate storedDate1 = MaxReachPro.getActivityDateSelected1();
        LocalDate storedDate2 = MaxReachPro.getActivityDateSelected2();
        String storedStatusSetting = MaxReachPro.getSelectedStatusSetting();
        // load the data as last configured
        if ((storedViewType == null) || (storedViewType == "Driver" && storedDate1 == null && storedDriver == null)
            || (storedViewType == "Customer" && storedDate1 == null && storedCustomer == null)
            || (storedViewType == "Status" && storedDate1 == null && storedViewType == null)) {
            loadDataAsync("All Rentals");
        } else if ((storedViewType == "Driver" && storedDriver == null)
                    || (storedViewType == "Customer" && storedCustomer == null )
                    || (storedViewType == "Status" && storedStatusSetting == null)) {
            if (storedDate1 != null && storedDate2 == null) {
                datePickerOne.setValue(storedDate1);
                handleViewSettingSelect("One Date", null);
            } else if (storedDate2 != null) {
                datePickerOne.setValue(storedDate1);
                datePickerTwo.setValue(storedDate2);
                handleViewSettingSelect("Interval", null);
            }
        } else if (storedViewType.equals("Driver")) {
            selectedViewButton = driverButton;
            Platform.runLater(() -> {
                handleViewSelect("collapse");
            });
            //viewsToggleGroup.selectToggle(driverButton);
            driverComboBox.setValue(storedDriver);
            //TranslateTransition transition = new TranslateTransition(Duration.millis(300), driverButton);
            //transition.setToX(-driverButton.getLayoutX() - 4);
            //transition.play();
             if (storedDate1 != null && storedDate2 == null) {
                datePickerOne.setValue(storedDate1);
                handleViewSettingSelect("Driver One Date", storedDriver);
            } else if (storedDate2 != null) {
                datePickerOne.setValue(storedDate1);
                datePickerTwo.setValue(storedDate2);
                handleViewSettingSelect("Driver Interval", storedDriver);
            } else {
                handleViewSettingSelect("Driver", storedDriver);
            }
        } else if (storedViewType.equals("Customer")) {
            selectedViewButton = customerButton;
            customerComboBox.setValue(storedCustomer);
            Platform.runLater(() -> {
                handleViewSelect("collapse");
            });
            //viewsToggleGroup.selectToggle(customerButton);
            if (storedDate1 != null && storedDate2 == null) {
                datePickerOne.setValue(storedDate1);
                handleViewSettingSelect("Customer One Date", storedCustomer);
            } else if (storedDate2 != null) {
                datePickerOne.setValue(storedDate1);
                datePickerTwo.setValue(storedDate2);
                handleViewSettingSelect("Customer Interval", storedCustomer);
            } else {
                handleViewSettingSelect("Customer", storedCustomer);
            }
        } else if (storedViewType.equals("Status")) {
            selectedViewButton = statusButton;
            Platform.runLater(() -> {
                handleViewSelect("collapse");
            });
            //viewsToggleGroup.selectToggle(statusButton);
            if (storedDate1 != null && storedDate2 == null) {
                handleViewSettingSelect("Status One Date", storedStatusSetting);
            } else if (storedDate2 != null) {
                handleViewSettingSelect("Status Interval", storedStatusSetting);
            } else {
                handleViewSettingSelect("Status", storedStatusSetting);
            }
        } else {  */
            loadDataAsync("All Rentals");
    //    }


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
        System.out.println("filter for the load data is: " + filter);
        ordersList.clear();
        groupedRentals.clear();
        currentViewInitials.clear();
        String filterSuffix = "";

        String query = "SELECT customers.*, rental_orders.*, rental_items.*, lifts.*, " +
                "ordered_contacts.first_name AS ordered_contact_name, " +
                "ordered_contacts.phone_number AS ordered_contact_phone, " +
                "site_contacts.first_name AS site_contact_name, " +
                "site_contacts.phone_number AS site_contact_phone " +
                "FROM customers " +
                "JOIN rental_orders ON customers.customer_id = rental_orders.customer_id " +
                "JOIN rental_items ON rental_orders.rental_order_id = rental_items.rental_order_id " +
                "LEFT JOIN lifts ON rental_items.lift_id = lifts.lift_id " +
                "LEFT JOIN contacts AS ordered_contacts ON rental_items.ordered_contact_id = ordered_contacts.contact_id " +
                "LEFT JOIN contacts AS site_contacts ON rental_items.site_contact_id = site_contacts.contact_id ";

        String customerName = customerComboBox.getValue();
        String date = "";
        String startDate = "";
        if (datePickerOne.getValue() != null) {
            date = datePickerOne.getValue().format(fromJavaObjectFormatter);
            startDate = date;
        }
        String endDate = "";
        if (datePickerTwo.getValue() != null) {
            endDate = datePickerTwo.getValue().format(fromJavaObjectFormatter);
        }

        switch (filter) {
            case "Active":
                filterSuffix = " WHERE rental_items.item_status = 'Active'";
                break;
            case "Billable":
                filterSuffix = " WHERE rental_items.item_status IN ('Called Off', 'Picked Up') AND rental_items.invoice_composed = 0";
                break;
            case "Upcoming":
                filterSuffix = " WHERE rental_items.item_status = 'Upcoming'";
                break;
            case "Called Off":
                filterSuffix = " WHERE rental_items.item_status = 'Called Off'";
                break;
            case "Active One Date":
                filterSuffix = " WHERE rental_items.item_status = 'Active' AND rental_orders.delivery_date = '" + date + "'";
                break;
            case "Billable One Date":
                filterSuffix = " WHERE rental_items.item_status IN ('Called Off', 'Picked Up') AND rental_items.invoice_composed = 0 " +
                         "AND rental_orders.delivery_date = '" + date + "'";
                break;
            case "Upcoming One Date":
                filterSuffix = " WHERE rental_items.item_status = 'Upcoming' AND rental_orders.delivery_date = '" + date + "'";
                break;
            case "Called Off One Date":
                filterSuffix = " WHERE rental_items.item_status = 'Called Off' AND rental_orders.delivery_date = '" + date + "'";
                break;
            case "Active Interval":
                filterSuffix = " WHERE rental_items.item_status = 'Active' AND rental_orders.delivery_date BETWEEN '" + startDate + "' AND '" + endDate + "'";
                break;
            case "Billable Interval":
                filterSuffix = " WHERE rental_items.item_status IN ('Called Off', 'Picked Up') AND rental_items.invoice_composed = 0 " +
                         "AND rental_orders.delivery_date BETWEEN '" + startDate + "' AND '" + endDate + "'";
                break;
            case "Upcoming Interval":
                filterSuffix = " WHERE rental_items.item_status = 'Upcoming' AND rental_orders.delivery_date BETWEEN '" + startDate + "' AND '" + endDate + "'";
                break;
            case "Called Off Interval":
                filterSuffix = " WHERE rental_items.item_status = 'Called Off' AND rental_orders.delivery_date BETWEEN '" + startDate + "' AND '" + endDate + "'";
                break;
            case "Customer":
                filterSuffix = " WHERE customers.customer_name = '" + customerName + "'";
                break;
            case "Customer One Date":
                filterSuffix = " WHERE customers.customer_name = '" + customerName + "' AND rental_orders.delivery_date = '" + date + "'";
                break;
            case "Customer Interval":
                filterSuffix = " WHERE customers.customer_name = '" + customerName + "' AND rental_orders.delivery_date BETWEEN '" + startDate + "' AND '" + endDate + "'";
                break;
            case "Driver":
                filterSuffix = " WHERE rental_items.driver_initial = '" + driverComboBox.getValue() + "'";
                break;
            case "Driver One Date":
                filterSuffix = " WHERE rental_items.driver_initial = '" + driverComboBox.getValue() + "' AND rental_orders.delivery_date = '" + date + "'";
                break;
            case "Driver Interval":
                filterSuffix = " WHERE rental_items.driver_initial = '" + driverComboBox.getValue() + "' " +
                         "AND rental_orders.delivery_date BETWEEN '" + startDate + "' AND '" + endDate + "'";
                break;
            case "One Date":
                filterSuffix = " WHERE rental_orders.delivery_date = '" + date + "'";
                break;
            case "Interval":
                filterSuffix = " WHERE rental_orders.delivery_date BETWEEN '" + startDate + "' AND '" + endDate + "'";
                break;
            case "All Rentals":
                break;
            default:
                throw new IllegalArgumentException("Unknown filter: " + filter);
        }


        // Add LIMIT clause at the end
      //  filterSuffix = " LIMIT 1000";
        System.out.println(query + filterSuffix);

        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query + filterSuffix)) {

            while (resultSet.next()) {
                String customerId = resultSet.getString("customer_id");
                String name = resultSet.getString("customer_name");
                String nameWithCodes = resultSet.getString("name_with_codes");
                String orderedContactName = resultSet.getString("ordered_contact_name");
                String orderedContactPhone = resultSet.getString("ordered_contact_phone");
                String deliveryDate = resultSet.getString("item_delivery_date");
                String callOffDate = resultSet.getString("item_call_off_date");
                int autoTerm = resultSet.getInt("auto_term");
                int rentalDuration = resultSet.getInt("rental_duration");
                String driver = resultSet.getString("driver");
                int driverNumber = resultSet.getInt("driver_number");
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
                double latitude = resultSet.getLong("latitude");
                double longitude = resultSet.getLong("longitude");
                String siteContactName = resultSet.getString("site_contact_name");
                String siteContactPhone = resultSet.getString("site_contact_phone");
                String poNumber = resultSet.getString("po_number");
                String locationNotes = resultSet.getString("location_notes");
                String preTripInstructions = resultSet.getString("pre_trip_instructions");
                int needsInvoice = resultSet.getInt("needs_invoice");
                int isInvoiceWritten = resultSet.getInt("invoice_composed");
                String lastBilledDate = resultSet.getString("last_billed_date");
                int latestServiceId = resultSet.getInt("last_service_id");

                Rental rental = new Rental(customerId, name, deliveryDate, deliveryTime, callOffDate,
                        driver != null ? driver : "", driverNumber, status != null ? status : "Unknown", 
                        poNumber, rental_id, 
                        false, lastBilledDate);
                rental.setRentalItemId(rental_item_id);
                rental.setOrderedByName(orderedContactName);
                rental.setOrderedByPhone(orderedContactPhone);
                rental.setAutoTerm(autoTerm == 1);
                rental.setLiftId(liftId);
                rental.setLiftType(liftType);
                rental.setAddressBlockOne(siteName);
                rental.setAddressBlockTwo(streetAddress);
                rental.setAddressBlockThree(cityState);
                rental.splitAddressBlockTwo();
                rental.setLatitude(latitude);
                rental.setLongitude(longitude);
                rental.setSiteContactName(siteContactName);
                rental.setSiteContactPhone(siteContactPhone);
                rental.setLocationNotes(locationNotes);
                rental.setPreTripInstructions(preTripInstructions);
                rental.setSerialNumber(serialNumber);
                rental.setInvoiceComposed(isInvoiceWritten != 0);
                rental.setNeedsInvoice(needsInvoice == 1);
                rental.setLatestServiceId(latestServiceId);
                boolean deliveryCheck = isWithinBusinessDays(deliveryDate, 40);
                boolean callOffCheck = isWithinBusinessDays(callOffDate, 40);
                boolean lastBilledCheck = isWithinBusinessDays(lastBilledDate, 40);
        
           //     if (deliveryCheck || callOffCheck || lastBilledCheck) {
                    ordersList.add(rental);
           //     }
                
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String servicesQuery = """
            SELECT 
                s.*,
                rental_items.rental_order_id,
                rental_items.lift_id,
                rental_items.customer_ref_number,
                rental_orders.*,
                customers.*,
                lifts.*,
                -- New lift info
                nl.lift_type AS new_lift_type,
                -- New rental order info
                nro.site_name AS new_site_name,
                nro.street_address AS new_street_address,
                nro.city AS new_city,
                nro.latitude AS new_latitude,
                nro.longitude AS new_longitude,
                ordered_contacts.first_name AS ordered_contact_name,
                ordered_contacts.phone_number AS ordered_contact_phone,
                site_contacts.first_name AS site_contact_name,
                site_contacts.phone_number AS site_contact_phone
            FROM services s
            JOIN rental_items ON s.rental_item_id = rental_items.rental_item_id
            JOIN rental_orders ON rental_items.rental_order_id = rental_orders.rental_order_id
            JOIN customers ON rental_orders.customer_id = customers.customer_id
            LEFT JOIN lifts ON rental_items.lift_id = lifts.lift_id
            LEFT JOIN lifts nl ON s.new_lift_id = nl.lift_id
            LEFT JOIN rental_orders nro ON s.new_rental_order_id = nro.rental_order_id
            LEFT JOIN contacts AS ordered_contacts ON s.ordered_contact_id = ordered_contacts.contact_id
            LEFT JOIN contacts AS site_contacts ON s.site_contact_id = site_contacts.contact_id
        """;
        
        
        
        try (Connection conn = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(servicesQuery + filterSuffix)) {

            while (rs.next()) {
                String customerId = rs.getString("customer_id");
                String name = rs.getString("customer_name");
                String serviceDate = rs.getString("service_date");
                String serviceTime = rs.getString("time");
                String serviceDriver = rs.getString("driver");
                int serviceDriverNumber = rs.getInt("driver_number");
                String serviceStatus = rs.getString("service_status");
                String poNumber = rs.getString("customer_ref_number");
                int rentalOrderId = rs.getInt("rental_order_id");
                int rentalItemId = rs.getInt("rental_item_id");
                boolean billable = rs.getInt("billable") == 1; // services will use needsInvoice for "billable" property
                int liftId = rs.getInt("lift_id");
                String liftType = rs.getString("lift_type");
                String serialNumber = rs.getString("serial_number");
                String siteName = rs.getString("site_name");
                String streetAddress = rs.getString("street_address");
                String city = rs.getString("city");
                int serviceId = rs.getInt("service_id");
                String serviceType = rs.getString("service_type");
                String reason = rs.getString("reason");
                int previousServiceId = rs.getInt("previous_service_id");
                int newRentalOrderId = rs.getInt("new_rental_order_id");
                int newLiftId = rs.getInt("new_lift_id");
                String orderedContactName = rs.getString("ordered_contact_name");
                String orderedContactNumber = rs.getString("ordered_contact_phone");
                String siteContactName = rs.getString("site_contact_name");
                String siteContactNumber = rs.getString("site_contact_phone");
                String locationNotes = rs.getString("location_notes");
                String preTripInstructions = rs.getString("pre_trip_instructions");
                String driver = rs.getString("driver");
                String driverInitial = rs.getString("driver_initial");
                int driverNumber = rs.getInt("driver_number");
                String orderDate = rs.getString("order_date");
                boolean singleItemOrder = rs.getInt("single_item_order") == 1;
                double latitude = rs.getLong("latitude");
                double longitude = rs.getLong("longitude");
                String newLiftType = rs.getString("new_lift_type");
                String newSiteName = rs.getString("new_site_name");
                String newStreetAddress = rs.getString("new_street_address");
                String newCity = rs.getString("new_city");
                double newLatitude = rs.getLong("new_latitude");
                double newLongitude = rs.getLong("new_longitude");

                Rental rental = new Rental(customerId, name, serviceDate, serviceTime,
                 "", serviceDriver, serviceDriverNumber, serviceStatus, reason,
                rentalOrderId, billable, "");
                rental.setLiftId(liftId);
                rental.setLiftType(liftType);
                rental.setSerialNumber(serialNumber);
                rental.setAddressBlockOne(siteName);
                rental.setAddressBlockTwo(streetAddress);
                rental.setAddressBlockThree(city);
                rental.setOrderedByName(orderedContactName);
                rental.setOrderedByPhone(orderedContactNumber);
                rental.setSiteContactName(siteContactName);
                rental.setSiteContactPhone(siteContactNumber);
                rental.setRentalItemId(rentalItemId);
                rental.setLocationNotes(locationNotes);
                rental.setPreTripInstructions(preTripInstructions);
                rental.setDriver(driver);
                rental.setDriverInitial(driverInitial);
                rental.setDriverNumber(driverNumber);
                rental.setOrderDate(orderDate);
                rental.setIsSingleItemOrder(singleItemOrder);
                rental.setLatitude(latitude);
                rental.setLongitude(longitude);

                Service service = new Service(serviceId, serviceType, reason, billable,
                     previousServiceId, newRentalOrderId, newLiftId, newLiftType, newSiteName,
                     newStreetAddress, newCity, newLatitude, newLongitude, locationNotes, preTripInstructions);
                rental.setService(service);

                ordersList.add(rental);
            }
        
        } catch (SQLException e) {
            e.printStackTrace();
        }
        

        Comparator<Rental> comparator = Comparator.comparingDouble((Rental item) -> {
            String status = item.getStatus();
            String deliveryDateString = item.getDeliveryDate();
            LocalDate today = LocalDate.now();
            LocalDate nextWorkDay = getNextWorkDay(today);
            LocalDate deliveryDate = parseDate(deliveryDateString);
            String deliveryTime = item.getDeliveryTime();
        
            if (filter != null && filter.startsWith("Driver")) {
                if (item.getDriverNumber() != 0) {
                    switch (status) {
                        case "Upcoming": return -1.0;
                        case "Called Off": return -0.5;
                    }
                }
            }
        
            if ("Upcoming".equals(status)) {
                if (deliveryDate != null && deliveryDate.isEqual(today)) {
                    return 0;
                } else if (deliveryDate != null && deliveryDate.isEqual(nextWorkDay)) {
                    return 1;
                } else if (deliveryDate != null && deliveryDate.isEqual(getNextWorkDay(nextWorkDay))
                           && "Any".equals(deliveryTime)) {
                    return 1.5;
                }
            }
            if ("Called Off".equals(status)) {
                return 2;
            }
            if ("Active".equals(status)) {
                return 3;
            }
            if ("Upcoming".equals(status)) {
                return 5;
            }
        
            return 10; // fallback
        }).thenComparing((Rental a, Rental b) -> {
            LocalDate callOffA = parseDate(a.getCallOffDate());
            LocalDate callOffB = parseDate(b.getCallOffDate());
            LocalDate deliveryA = parseDate(a.getDeliveryDate());
            LocalDate deliveryB = parseDate(b.getDeliveryDate());
        
            if (callOffA != null && callOffB != null) {
                return callOffB.compareTo(callOffA); // latest first
            }
            if (callOffA != null) return -1;
            if (callOffB != null) return 1;
        
            if (deliveryA != null && deliveryB != null) {
                return deliveryB.compareTo(deliveryA); // latest first
            }
            return 0;
        });
        
        
        // Sort the list using the comparator
        FXCollections.sort(ordersList, comparator);
        dbTableView.setItems(ordersList);
        latestFilter = filter;
    }

    private void animateScrollBars(TableView<Rental> tableView) {
       Platform.runLater(() -> {
           int i = 0;
           // Assuming your scroll bars are part of the dbTableView's skin
           for (Node node : tableView.lookupAll(".scroll-bar")) {
               //System.out.println("animating for:" + node);
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
                                           timelineFlagger ? Color.web(Config.getSecondaryColor(), 0.5) : Color.web(Config.getSecondaryColor(), 0.5))
                           ),
                           new KeyFrame(Duration.seconds(1),
                                   new KeyValue(glowEffect.colorProperty(),
                                           timelineFlagger ? Color.web("#000000", 0.0) : Color.web(Config.getPrimaryColor(), 1.0))
                           )
                   );


                   glowTimelines[i].setCycleCount(Timeline.INDEFINITE);
                   glowTimelines[i].setAutoReverse(true);
                   glowTimelines[i].play();
               }
           }
       });
    }



    private void fadeOutScrollBars(TableView<Rental> dbTableView) {
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

    private void setScrollBarVisibility(TableView<Rental> dbTableView, boolean isVisible) {
        ScrollBar verticalScrollBar = (ScrollBar) dbTableView.lookup(".scroll-bar:vertical");
        ScrollBar horizontalScrollBar = (ScrollBar) dbTableView.lookup(".scroll-bar:horizontal");

        if (verticalScrollBar != null) {
            verticalScrollBar.setVisible(isVisible);
        }
        if (horizontalScrollBar != null) {
            horizontalScrollBar.setVisible(isVisible);
        }
    }

    private void resetFadeOutTimeline(TableView<Rental> dbTableView, Timeline fadeOutTimeline) {
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

    public void shiftSidebarHighlighter(String nextActionType) {
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
            case "cancelling":
                sideBarHighlighter.setTranslateY(downset * 2 * direction);
                sideBarHighlighter.setTranslateX(direction * -1);
                break;
            case "dropping-off":
                sideBarHighlighter.setTranslateY(downset * 3 * direction - 2);
                sideBarHighlighter.setTranslateX(direction * 1);
                break;
            case "scheduling-service":
                sideBarHighlighter.setTranslateY(downset * 4 * direction - 1);
                sideBarHighlighter.setTranslateX(direction * 0);
                break;
            case "calling-off":
                sideBarHighlighter.setTranslateY(downset * 5 * direction - 1);
                sideBarHighlighter.setTranslateX(direction * 0);
                break;
            case "picking-up":
                sideBarHighlighter.setTranslateY(downset * 6 * direction - 1);
                sideBarHighlighter.setTranslateX(direction * 1);
                break;
            case "composing-invoices":
                sideBarHighlighter.setTranslateY(downset * 7 * direction);
                sideBarHighlighter.setTranslateX(direction * 0);
                break;
            case "expanding":
                sideBarHighlighter.setTranslateY(downset * 8 * direction);
                sideBarHighlighter.setTranslateX(direction * 0);
                break;
            case "refreshing-data":
                sideBarHighlighter.setTranslateY(downset * 9 * direction);
                sideBarHighlighter.setTranslateX(direction * -1);
                break;
            case "deleting":
                sideBarHighlighter.setTranslateY(downset * 10 * direction);
                sideBarHighlighter.setTranslateX(direction * 0);
                break;
            default:
                sideBarHighlighter.setVisible(false);
                break;
        }
    }

    @FXML
    private void handleButtonMouseEntered(MouseEvent event) {
        Button hoveredButton = (Button) event.getSource();
        String imageUrl = getImageUrl(hoveredButton, true); // true to use inverted image
        updateButtonImage(hoveredButton, imageUrl);
    }

    @FXML
    private void sidebarButtonMouseExited(MouseEvent event) {
        Button hoveredButton = (Button) event.getSource();
        String imageUrl = getImageUrl(hoveredButton, false); // false to use original image
        updateButtonImage(hoveredButton, imageUrl);
    }

    private String getImageUrl(Button button, boolean isInverted) {
        String imageUrl = "";
        if (button == composeContractsButton) {
            imageUrl = isInverted ? IMAGE_PATH_BASE + "create-contracts" + IMAGE_PATH_INV_SUFFIX : IMAGE_PATH_BASE + "create-contracts.png";
        } else if (button == editDriverButton) {
            imageUrl = isInverted ? IMAGE_PATH_BASE + "driver-icon" + IMAGE_PATH_INV_SUFFIX : IMAGE_PATH_BASE + "driver-icon.png";
        } else if (button == cancellingButton) {
            imageUrl = isInverted ? IMAGE_PATH_BASE + "cancelling" + IMAGE_PATH_INV_SUFFIX : IMAGE_PATH_BASE + "cancelling.png";
        } else if (button == droppingOffButton) {
            imageUrl = isInverted ? IMAGE_PATH_BASE + "dropping-off" + IMAGE_PATH_INV_SUFFIX : IMAGE_PATH_BASE + "dropping-off.png";
        } else if (button == schedulingServiceButton) {
            imageUrl = isInverted ? IMAGE_PATH_BASE + "scheduling-service" + IMAGE_PATH_INV_SUFFIX : IMAGE_PATH_BASE + "scheduling-service.png";
        } else if (button == callingOffButton) {
            imageUrl = isInverted ? IMAGE_PATH_BASE + "calling-off" + IMAGE_PATH_INV_SUFFIX : IMAGE_PATH_BASE + "calling-off.png";
        } else if (button == pickingUpButton) {
            imageUrl = isInverted ? IMAGE_PATH_BASE + "picking-up" + IMAGE_PATH_INV_SUFFIX : IMAGE_PATH_BASE + "picking-up.png";
        } else if (button == composeInvoicesButton) {
            imageUrl = isInverted ? IMAGE_PATH_BASE + "create-invoices" + IMAGE_PATH_INV_SUFFIX : IMAGE_PATH_BASE + "create-invoices.png";
        } else if (button == expandButton) {
            imageUrl = isInverted ? IMAGE_PATH_BASE + "expand" + IMAGE_PATH_INV_SUFFIX : IMAGE_PATH_BASE + "expand.png";
        } else if (button == refreshDataButton) {
            imageUrl = isInverted ? IMAGE_PATH_BASE + "refresh" + IMAGE_PATH_INV_SUFFIX : IMAGE_PATH_BASE + "refresh.png";
        } else if (button == deleteButton) {
            imageUrl = isInverted ? IMAGE_PATH_BASE + "delete" + IMAGE_PATH_INV_SUFFIX : IMAGE_PATH_BASE + "delete.png";
        }
        return imageUrl;
    }

    private void updateButtonImage(Button button, String imageUrl) {
        Image image = new Image(getClass().getResource(imageUrl).toExternalForm());
        ImageView imageView = new ImageView(image);
        button.setGraphic(imageView); // Update the button's graphic
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
            MaxReachPro.setSelectedDriverName(driverComboBox.getValue());
        } else {
            if (datePickerOne.getValue() == null && datePickerTwo.getValue() == null) {
                handleViewSettingSelect("All Rentals", null);
            } else if (datePickerOne.getValue() != null && datePickerTwo.getValue() == null) {
                handleViewSettingSelect("One Date", null);
            } else {
                handleViewSettingSelect("Interval", null);
            }
        }
        MaxReachPro.setActivityDateSelected1(datePickerOne.getValue());
        MaxReachPro.setActivityDateSelected2(datePickerTwo.getValue());
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
            MaxReachPro.setSelectedCustomerName(customerComboBox.getValue());
        } else {
            if (datePickerOne.getValue() == null && datePickerTwo.getValue() == null) {
                handleViewSettingSelect("All Rentals", null);
            } else if (datePickerOne.getValue() != null && datePickerTwo.getValue() == null) {
                handleViewSettingSelect("One Date", null);
            } else {
                handleViewSettingSelect("Interval", null);
            }
        }
        MaxReachPro.setActivityDateSelected1(datePickerOne.getValue());
        MaxReachPro.setActivityDateSelected2(datePickerTwo.getValue());
    }

    private void handleSettingInStatusSelect(){
        String status = null;
        if (!areStatusesRotating) {
            ToggleButton selectedStatusButton = (ToggleButton) statusViewToggleGroup.getSelectedToggle();
            status = selectedStatusButton.getText();
            MaxReachPro.setSelectedStatusSetting(status);
        }
        if (datePickerOne.getValue() == null && datePickerTwo.getValue() == null){
            handleViewSettingSelect("Status", status);
        } else if (datePickerOne.getValue() != null && datePickerTwo.getValue() == null) {
            handleViewSettingSelect("Status One Date", status);
        } else {
            handleViewSettingSelect("Status Interval", status);
        }
        MaxReachPro.setActivityDateSelected1(datePickerOne.getValue());
        MaxReachPro.setActivityDateSelected2(datePickerTwo.getValue());
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



    @FXML
    private void handleAssignDrivers() {
        String actionType = "assigning-drivers";
        if (actionType.equals(lastActionType)) {
            updateRentalButton.setVisible(false); // Hide the update button
            workingColumnFactory.setClosedDriverColumn(driverColumnType); // Reset driver column
            shiftSidebarHighlighter(null); // Reset sidebar highlighter
            System.out.println("Driver assignment mode deactivated.");
        } else {
            batchButton.setVisible(false);
            secondInProcessButton.setVisible(false);
            updateRentalButton.setVisible(false); // Show the update button
            workingColumnFactory.setOpenDriverColumn(driverColumnType); // Open driver column for assignment
            shiftSidebarHighlighter(actionType); // Highlight the sidebar for driver assignment
            workingColumnFactory.showSelectableCheckboxes(false, actionType);
            System.out.println("Driver assignment mode activated.");
        }
        dbTableView.refresh(); // Refresh the table view
    }


    @FXML
    private void handleCancelling() {
        String actionType = "cancelling";
        if (actionType.equals(lastActionType)) {
            updateRentalButton.setVisible(false); // Hide the update button
            workingColumnFactory.setClosedDriverColumn(driverColumnType); // Reset driver column
            shiftSidebarHighlighter(null); // Reset sidebar highlighter
            System.out.println("Cancel mode deactivated.");
        } else {
            batchButton.setVisible(false);
            secondInProcessButton.setVisible(false);
            updateRentalButton.setVisible(false); // Show the update button
            workingColumnFactory.setOpenDriverColumn(driverColumnType); // Open driver column for assignment
            shiftSidebarHighlighter(actionType); // Highlight the sidebar for driver assignment
            workingColumnFactory.showSelectableCheckboxes(false, actionType);
            System.out.println("Cancel mode activated.");
        }
        dbTableView.refresh(); // Refresh the table view
    }  


   @FXML
    private void handleDroppingOff(ActionEvent event) {
        String actionType = "dropping-off";
        if (actionType.equals(lastActionType)) {
            workingColumnFactory.showSelectableCheckboxes(false, null);
            updateRentalButton.setVisible(false);
            serialNumberField.setVisible(false);
            shiftSidebarHighlighter(null); // Reset sidebar highlighter
            shiftUpdateButtonFull();
        } else {
            closeDriverBoxesIfOpen();
            resetCheckboxes();
            updateRentalButton.setVisible(false);
            batchButton.setVisible(false);
            workingColumnFactory.showSelectableCheckboxes(true, actionType);
            serialNumberField.setVisible(true);
            shiftSidebarHighlighter(actionType); // Update sidebar highlighter
            shiftUpdateButtonHalf();
        }
    }

    @FXML
    private void handleSchedulingService(ActionEvent event) {
        String actionType = "scheduling-service";
        if (actionType.equals(lastActionType)) {
            workingColumnFactory.showMiniatureIcons(false, actionType);
            shiftSidebarHighlighter(null);
        } else {
            closeDriverBoxesIfOpen();
            batchButton.setVisible(false);
            secondInProcessButton.setVisible(false);
            workingColumnFactory.showMiniatureIcons(true, actionType);
            serialNumberField.setVisible(false);
            shiftSidebarHighlighter(actionType);
            shiftUpdateButtonFull();
        }
    }

    @FXML
    private void handleCallingOff(ActionEvent event) {
        String actionType = "calling-off";
        if (actionType.equals(lastActionType)) {
            workingColumnFactory.showSelectableCheckboxes(false, null);
            updateRentalButton.setVisible(false);
            shiftSidebarHighlighter(null);
        } else {
            batchButton.setVisible(false);
            secondInProcessButton.setVisible(false);
            closeDriverBoxesIfOpen();
            resetCheckboxes();
            updateRentalButton.setVisible(false);
            workingColumnFactory.showSelectableCheckboxes(true, actionType);
            serialNumberField.setVisible(false);
            shiftSidebarHighlighter(actionType);
            shiftUpdateButtonFull();
        }
    }

    @FXML
    private void handlePickingUp(ActionEvent event) {
        String actionType = "picking-up";
        if (actionType.equals(lastActionType)) {
            workingColumnFactory.showSelectableCheckboxes(false, null);
            updateRentalButton.setVisible(false);
            shiftSidebarHighlighter(null);
        } else {
            closeDriverBoxesIfOpen();
            batchButton.setVisible(false);
            secondInProcessButton.setVisible(false);
            resetCheckboxes();
            updateRentalButton.setVisible(false);
            workingColumnFactory.showSelectableCheckboxes(true, actionType);
            serialNumberField.setVisible(false);
            shiftSidebarHighlighter(actionType);
            shiftUpdateButtonFull();
        }
    }

    @FXML
    private void handleComposeInvoices(ActionEvent event) {
        String actionType = "composing-invoices";
        if (actionType.equals(lastActionType)) {
            workingColumnFactory.showSelectableCheckboxes(false, null);
            updateRentalButton.setVisible(false);
            shiftSidebarHighlighter(null);
            batchButton.setVisible(false);
            secondInProcessButton.setVisible(false);
        } else {
            closeDriverBoxesIfOpen();
            resetCheckboxes();
            batchButton.setVisible(false);
            secondInProcessButton.setVisible(false);
            updateRentalButton.setVisible(false);
            workingColumnFactory.showSelectableCheckboxes(true, actionType);
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
            workingColumnFactory.showSelectableCheckboxes(false, null);
            updateRentalButton.setVisible(false);
            batchButton.setVisible(false);
            secondInProcessButton.setVisible(false);
            } else {
            closeDriverBoxesIfOpen();
            shiftSidebarHighlighter(actionType);
            batchButton.setVisible(false);
            secondInProcessButton.setVisible(false);
            resetCheckboxes();
            updateRentalButton.setVisible(false);
            workingColumnFactory.showSelectableCheckboxes(true, actionType);
            serialNumberField.setVisible(false);
            shiftUpdateButtonFull();
        }
    }

    @FXML
    private void handleExpand(ActionEvent event) {
        String actionType = "expanding";
        if (actionType.equals(lastActionType)) {
            workingColumnFactory.showMiniatureIcons(false, actionType);
            shiftSidebarHighlighter(null);
        } else {
            closeDriverBoxesIfOpen();
            batchButton.setVisible(false);
            secondInProcessButton.setVisible(false);
            workingColumnFactory.showMiniatureIcons(true, actionType);
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
        loadData(latestFilter);
        sendPrepareRequest();
    }


    @FXML
    private void handleDelete(ActionEvent event) {
        String actionType = "deleting";
        if (actionType.equals(lastActionType)) {
            workingColumnFactory.showSelectableCheckboxes(false, null);
            updateRentalButton.setVisible(false);
            shiftSidebarHighlighter(null); // Reset sidebar highlighter
        } else {
            closeDriverBoxesIfOpen();
            resetCheckboxes();
            batchButton.setVisible(false);
            secondInProcessButton.setVisible(false);
            updateRentalButton.setVisible(false);
            workingColumnFactory.showSelectableCheckboxes(true, actionType);
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
        for (Rental order : dbTableView.getItems()) {
            order.setSelected(false);
        }
        dbTableView.refresh(); // Refresh the table view to update the checkbox states
    }


    @FXML
    private void handleUpdateRental(ActionEvent event) {
        ObservableList<Rental> selectedRentals = dbTableView.getItems().filtered(Rental::isSelected);
        boolean statusUpdated = false;
        Date today = new Date(); // Today's date

        if (lastActionType.equals("assigning-drivers")) {
            handleAssignDrivers();
            lastActionType = "assigning-drivers"; // correcting for above line which causes it to be set to null
            statusUpdated = true;
        } else if (lastActionType.equals("cancelling")) {
            System.out.println("cancelling");
        } else if (lastActionType.equals("dropping-off")) {
            for (Rental order : selectedRentals) {
                String newStatus = "Active";
                System.out.println("Order for " + order.getName() + " status is:" + order.getStatus());
                if (order.getStatus().equals("Upcoming")) {
                    order.setStatus(newStatus);
                    updateRentalStatusInDB(order.getRentalItemId(), newStatus); // Sync with DB
                    updateDateInDB(order.getRentalItemId(), "item_delivery_date", today); // Update delivery date
                    statusUpdated = true;
                    System.out.println("Order for " + order.getName() + " marked as Active with today's delivery date.");
                    if (serialNumberField.getText().length() >= 4) {
                        for (Rental rental : selectedRentals) {
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
        } else if (lastActionType.equals("scheudling-service")) {
            System.out.println("schedule seravice logic");
        } else if (lastActionType.equals("calling-off")) {
            for (Rental order : selectedRentals) {
                String newStatus = "Called Off";
                if (order.getStatus().equals("Active")) {
                    order.setStatus(newStatus);
                    updateRentalStatusInDB(order.getRentalItemId(), newStatus); // Sync with DB
                    Date rentalEndDate = getCutoffAdjustedDate();
                    updateDateInDB(order.getRentalItemId(), "item_call_off_date", rentalEndDate); // Update pickup date
                    String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(rentalEndDate);
                    order.setCallOffDate(formattedDate);
                    statusUpdated = true;
                    System.out.println("Order for " + order.getName() + " status updated to 'Called Off'.");
                }
            }
        } else if (lastActionType.equals("picking-up")) {
            for (Rental order : selectedRentals) {
                String newStatus = "Picked Up"; // Set the status for picking-up
                if (order.getStatus().equals("Called Off")) {
                    order.setStatus(newStatus);
                    updateRentalStatusInDB(order.getRentalItemId(), newStatus); // Sync with DB
                    statusUpdated = true;
                    System.out.println("Order for " + order.getName() + " status updated to 'Picked Up' with today's pickup date.");
                }
            }
        } else if (lastActionType.equals("deleting")) {
            List<Rental> itemsToRemove = new ArrayList<>();
            for (Rental order : selectedRentals) {
                deleteRentalFromDB(order.getRentalItemId());
                itemsToRemove.add(order);
                statusUpdated = true;
            }
            ordersList.removeAll(itemsToRemove);
            selectedRentals.removeAll(itemsToRemove);
        } else {
            // Existing logic for other action types
            for (Rental order : selectedRentals) {
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
            shiftSidebarHighlighter(null);
            lastActionType = null;
            resetCheckboxes();
            workingColumnFactory.resetCheckboxes();
            workingColumnFactory.showSelectableCheckboxes(false, lastActionType);
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
                LIMIT 1        
            )
            WHERE rental_item_id = ?;
        """;
    
        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
    
            preparedStatement.setString(1, serialNumber);
            preparedStatement.setInt(2, rentalItemId);
    
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 0) {
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

    
    private void sendPrepareRequest() {
        try {
            String endpoint = Config.API_BASE_URL + "/routes/prepare";
            java.net.URL url = new java.net.URL(endpoint);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            // Optional: Body payload
            try (java.io.OutputStream os = conn.getOutputStream()) {
                byte[] input = "{}".getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            System.out.println("POST Response Code: " + responseCode);

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String determineNewStatus(Rental order, String actionType) {
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


    private Date getCutoffAdjustedDate() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();


        // Determine if we should use today's date or the previous business day
        LocalDate adjustedDate = (now.isAfter(Config.CUT_OFF_TIME)) ? today : getPreviousBusinessDay(today);


        // Convert LocalDate to java.util.Date
        return Date.from(adjustedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }


    private LocalDate getPreviousBusinessDay(LocalDate date) {
        LocalDate previousDay = date.minusDays(1);


        // Loop backward until we find a valid business day
        while (previousDay.getDayOfWeek() == DayOfWeek.SATURDAY ||
               previousDay.getDayOfWeek() == DayOfWeek.SUNDAY ||
               Config.isHoliday(previousDay)) {
            previousDay = previousDay.minusDays(1);
        }


        return previousDay;
    }




    // Handle the back button action
    @FXML
    public void handleBack(ActionEvent event) {
        try {
            MaxReachPro.goBack("/fxml/activity.fxml");
            GradientAnimator.initialize();
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

        secondInProcessButton.setVisible(false);
    }

    private void closeDriverBoxesIfOpen(){
        if ("assigning-drivers".equals(lastActionType)) {
            workingColumnFactory.setClosedDriverColumn(driverColumnType);
        }
    }


    private void handleViewSelect(String orientation) {
        datePickerOne.setValue(null);
        datePickerTwo.setValue(null);
        datePickerOneLabel.setText("From:");
        datePickerTwoLabel.setText("To:");

        GradientAnimator.stopAllAnimations();

        if (Objects.equals(orientation, "expand")) {
            statusViewToggleGroup.selectToggle(null);
            // Unselect the currently selected button
            selectedViewButton.setSelected(false);
            GradientAnimator.applySequentialGradientAnimationToggles(viewsToggleButtons, 0, "view-type-button-stopped");

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

            handleViewSettingSelect("All Rentals", null);
            selectedStatusButton = null;
        } else {
            // New button selection
           // selectedViewButton = selectedButton;
            String selectedView = selectedViewButton.getText();
            MaxReachPro.setSelectedViewSetting(selectedView);
            MaxReachPro.setActivityDateSelected1(null);
            MaxReachPro.setActivityDateSelected2(null);
            MaxReachPro.setSelectedCustomerName(null);
            MaxReachPro.setSelectedDriverName(null);
            MaxReachPro.setSelectedStatusSetting(null);
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
                    GradientAnimator.applySequentialGradientAnimationToggles(statusesToggleButtons, 1, "status-type-button-stopped");
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
            selectedViewButton.setVisible(true);
        }
    }


    private void handleViewSettingSelect (String viewType, String status) {
        dbTableView.getColumns().clear();
        resetColumnWidths();
        dbTableView.getColumns().addAll(statusColumn, addressColumn);
        switch (viewType) {
             case "Status":
                switch (status) {
                    case "Active":
                        serialNumberColumn.setPrefWidth(19);
                        dbTableView.getColumns().addAll(deliveryDateColumn, serialNumberColumn, invoiceColumn);
                        loadData("Active");
                        break;
                    case "Billable":
                        addressColumn.setPrefWidth(105);
                        dbTableView.getColumns().addAll(deliveryDateColumn, callOffDateColumn, invoiceColumn);
                        loadData("Billable");
                        break;
                    case "Upcoming":
                        driverColumnType = "po-number";
                        driverColumn = workingColumnFactory.getDriverColumn(driverColumnType);
                        deliveryDateColumn.setPrefWidth(36);
                        dbTableView.getColumns().addAll(deliveryDateColumn, deliveryTimeColumn, driverColumn);
                        loadData("Upcoming");
                        break;
                    case "Called Off":
                        driverColumnType = "interval";
                        driverColumn = workingColumnFactory.getDriverColumn(driverColumnType);
                        serialNumberColumn.setPrefWidth(26);
                        callOffDateColumn.setPrefWidth(33);
                        dbTableView.getColumns().addAll(serialNumberColumn, callOffDateColumn, driverColumn);
                        loadData("Called Off");
                        break;
                    default:
                        driverColumnType = "po-number";
                        driverColumn = workingColumnFactory.getDriverColumn(driverColumnType);
                        biModalDateColumn.setPrefWidth(49);
                        dbTableView.getColumns().addAll(biModalDateColumn, serialNumberColumn, driverColumn);
                        loadData("All Rentals");
                        break;
                }
                break;
            case "Status One Date":
                switch (status) {
                    case "Active":
                        serialNumberColumn.setPrefWidth(19);
                        dbTableView.getColumns().addAll(deliveryDateColumn, serialNumberColumn, invoiceColumn);
                        loadData("Active One Date");
                        break;
                    case "Billable":
                        addressColumn.setPrefWidth(105);
                        dbTableView.getColumns().addAll(deliveryDateColumn, callOffDateColumn, invoiceColumn);
                        loadData("Billable One Date");
                        break;
                    case "Upcoming":
                        driverColumnType = "po-number";
                        driverColumn = workingColumnFactory.getDriverColumn(driverColumnType);
                        deliveryDateColumn.setPrefWidth(36);
                        dbTableView.getColumns().addAll(deliveryDateColumn, deliveryTimeColumn, driverColumn);
                        loadData("Upcoming One Date");
                        break;
                    case "Called Off":
                        driverColumnType = "interval";
                        driverColumn = workingColumnFactory.getDriverColumn(driverColumnType);
                        serialNumberColumn.setPrefWidth(26);
                        callOffDateColumn.setPrefWidth(33);
                        dbTableView.getColumns().addAll(serialNumberColumn, callOffDateColumn, driverColumn);
                        loadData("Called Off One Date");
                        break;
                    default:
                        driverColumnType = "time-range";
                        driverColumn = workingColumnFactory.getDriverColumn(driverColumnType);
                        biModalDateColumn.setPrefWidth(49);
                        dbTableView.getColumns().addAll(biModalDateColumn, serialNumberColumn, driverColumn);
                        loadData("One Date");
                        break;
                }
                break;
            case "Status Interval":
                switch (status) {
                    case "Active":
                        serialNumberColumn.setPrefWidth(19);
                        dbTableView.getColumns().addAll(deliveryDateColumn, serialNumberColumn, invoiceColumn);
                        loadData("Active Interval");
                        break;
                    case "Billable":
                        addressColumn.setPrefWidth(105);
                        dbTableView.getColumns().addAll(deliveryDateColumn, callOffDateColumn, invoiceColumn);
                        loadData("Billable Interval");
                        break;
                    case "Upcoming":
                        driverColumnType = "po-number";
                        driverColumn = workingColumnFactory.getDriverColumn(driverColumnType);
                        serialNumberColumn.setPrefWidth(26);
                        callOffDateColumn.setPrefWidth(33);
                        dbTableView.getColumns().addAll(deliveryDateColumn, deliveryTimeColumn, driverColumn);
                        loadData("Upcoming Interval");
                        break;
                    case "Called Off":
                        driverColumnType = "interval";
                        driverColumn = workingColumnFactory.getDriverColumn(driverColumnType);
                        deliveryDateColumn.setPrefWidth(36);
                        dbTableView.getColumns().addAll(serialNumberColumn, callOffDateColumn, driverColumn);
                        loadData("Called Off Interval");
                        break;
                    default:
                        driverColumnType = "po-number";
                        driverColumn = workingColumnFactory.getDriverColumn(driverColumnType);
                        biModalDateColumn.setPrefWidth(49);
                        dbTableView.getColumns().addAll(biModalDateColumn, serialNumberColumn, driverColumn);
                        loadData("Interval");
                        break;
                }
                break;
            case "Customer":
                serialNumberColumn.setPrefWidth(20);
                biModalDateColumn.setPrefWidth(30);
                dbTableView.getColumns().addAll(serialNumberColumn, biModalDateColumn, invoiceColumn);
                loadData("Customer");
                break;
            case "Customer One Date":
                serialNumberColumn.setPrefWidth(20);
                biModalDateColumn.setPrefWidth(30);
                dbTableView.getColumns().addAll(serialNumberColumn, biModalDateColumn, invoiceColumn);
                loadData("Customer One Date");
                break;
            case "Customer Interval":
                serialNumberColumn.setPrefWidth(20);
                biModalDateColumn.setPrefWidth(30);
                dbTableView.getColumns().addAll(serialNumberColumn, biModalDateColumn, invoiceColumn);
                loadData("Customer Interval");
                break;
            case "Driver":
                driverColumnType = "time-range";
                driverColumn = workingColumnFactory.getDriverColumn(driverColumnType);
                biModalDateColumn.setPrefWidth(49);
                dbTableView.getColumns().addAll(serialNumberColumn, biModalDateColumn, driverColumn);
                loadData("Driver");
                break;
            case "Driver One Date":
                driverColumnType = "time-range";
                driverColumn = workingColumnFactory.getDriverColumn(driverColumnType);
                biModalDateColumn.setPrefWidth(49);
                dbTableView.getColumns().addAll(serialNumberColumn, biModalDateColumn, driverColumn);
                loadData("Driver One Date");
                break;
            case "Driver Interval":
                driverColumnType = "time-range";
                driverColumn = workingColumnFactory.getDriverColumn(driverColumnType);
                biModalDateColumn.setPrefWidth(49);
                dbTableView.getColumns().addAll(serialNumberColumn, biModalDateColumn, driverColumn);
                loadData("Driver Interval");
                break;
            case "One Date":
                driverColumnType = "time-range";
                driverColumn = workingColumnFactory.getDriverColumn(driverColumnType);
                biModalDateColumn.setPrefWidth(49);
                dbTableView.getColumns().addAll(biModalDateColumn, serialNumberColumn, driverColumn);
                System.out.println("Adding a biModal date column");
                loadData("One Date");
                break;
            case "Interval":
                driverColumnType = "po-number";
                driverColumn = workingColumnFactory.getDriverColumn(driverColumnType);
                biModalDateColumn.setPrefWidth(49);
                dbTableView.getColumns().addAll(biModalDateColumn, serialNumberColumn, driverColumn);
                System.out.println("Adding a biModal date column");
                loadData("Interval");
                break;
            default:
                driverColumnType = "po-number";
                driverColumn = workingColumnFactory.getDriverColumn(driverColumnType);
                biModalDateColumn.setPrefWidth(49);
                dbTableView.getColumns().addAll(biModalDateColumn, serialNumberColumn, driverColumn);
                System.out.println("Adding a biModal date column");
                loadData("All Rentals");
                break;
        }
        if (lastActionType != null) {
            shiftSidebarHighlighter(null);
            lastActionType = null;
        }
        workingColumnFactory.resetCheckboxes();
        workingColumnFactory.showSelectableCheckboxes(false, lastActionType);
        updateRentalButton.setVisible(false);
        serialNumberField.setVisible(false);
        batchButton.setVisible(false);
        secondInProcessButton.setVisible(false);

        dbTableView.refresh();
    }

    private void resetColumnWidths(){
        deliveryDateColumn.setPrefWidth(31);
        callOffDateColumn.setPrefWidth(31);
        biModalDateColumn.setPrefWidth(31);
        addressColumn.setPrefWidth(117);
        statusColumn.setPrefWidth(24);
        serialNumberColumn.setPrefWidth(10);
        deliveryTimeColumn.setPrefWidth(23);
        driverColumn.setPrefWidth(63);
        invoiceColumn.setPrefWidth(72);
    }

    private static LocalDate getNextWorkDay(LocalDate currentDay) {
        LocalDate nextDay = currentDay.plusDays(1);
        while (nextDay.getDayOfWeek() == DayOfWeek.SATURDAY ||
            nextDay.getDayOfWeek() == DayOfWeek.SUNDAY ||
            Config.COMPANY_HOLIDAYS.contains(nextDay)) {
            nextDay = nextDay.plusDays(1);
        }
        return nextDay;
    }

    private boolean isBusinessDay(LocalDate date) {
        return date.getDayOfWeek().getValue() < 6 && !Config.isHoliday(date);
    }

    private boolean isWithinBusinessDays(String dateStr, int businessDays) {
        if (dateStr == null || dateStr.isEmpty()) {
            return false;
        }
        
        LocalDate date = LocalDate.parse(dateStr, fromJavaObjectFormatter);
        int businessDayCount = 0;
        LocalDate today = LocalDate.now();
        LocalDate tempDate = today;

        while (businessDayCount < businessDays) {
            if (isBusinessDay(tempDate)) {
                businessDayCount++;
            }
            tempDate = tempDate.minusDays(1);
        }

        return !date.isBefore(tempDate);

    }

    // Helper function to parse a date string
    private static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null; // Return null for invalid or empty strings
        }
        try {
            return LocalDate.parse(dateString, fromJavaObjectFormatter);
        } catch (DateTimeParseException e) {
            // Handle invalid date format
            System.err.println("Invalid date format: " + dateString);
            return null;
        }
    }

    private void clearGradientFromToggleSet(List<ToggleButton> toggles, ToggleButton selectedException, String styleClass){
        for (ToggleButton toggle : toggles) {
            toggle.getStyleClass().removeAll(toggle.getStyleClass());
            toggle.getStyleClass().add(styleClass);
            if (toggle == selectedException) {
                toggle.setStyle("-fx-background: orange;");
            } else {

            }
        }
    }

    @FXML
    private void handleSecondInProcess(ActionEvent event) {
        // Get today's date to match the final merged PDF filename
        String todayDate = LocalDate.now().format(fromJavaObjectFormatter);
        String finalOutputFile = PathConfig.getFinalContractFilePath(todayDate);

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


    @Override
    protected void customCleanup() {
        if (dbTableView != null) {
            dbTableView.getItems().clear();
            dbTableView.getColumns().clear();
            dbTableView = null;
        }
    }

}
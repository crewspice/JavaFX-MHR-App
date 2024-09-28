package com.MaxHighReach;

import com.MaxHighReach.utils.StatusNodeFactory;

import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.control.Tooltip;

import javafx.util.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.util.StringConverter;

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

public class DBController extends BaseController {

    @FXML
    private Button backButton, editDriverButton, droppingOffButton, pickingUpButton, updateRentalButton, createInvoicesButton, refreshButton, createContractsButton;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TableView<CustomerOrder> dbTableView;
    @FXML
    private TableColumn<CustomerOrder, Boolean> selectColumn;
    @FXML
    private TableColumn<CustomerOrder, Integer> idColumn;
    @FXML
    private TableColumn<CustomerOrder, String> nameColumn;
    @FXML
    private TableColumn<CustomerOrder, String> orderDateColumn;
    @FXML
    private TableColumn<CustomerOrder, String> driverColumn;
    @FXML
    private TableColumn<CustomerOrder, String> statusColumn;
    @FXML
    private Label loadingLabel;
    @FXML
    private ComboBox<String> filterComboBox;

    private ObservableList<CustomerOrder> ordersList = FXCollections.observableArrayList();
    private ObservableList<String> driverInitials = FXCollections.observableArrayList("JD", "AB", "MG", "CN");
    private boolean isDriverEditMode = false;
    private String lastActionType;
    private boolean isDroppingOff = false;
    private boolean isPickingUp = false;

    @FXML
    public void initialize() {
        super.initialize();

        dbTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        selectColumn.setCellValueFactory(param -> new SimpleBooleanProperty(param.getValue().isSelected()));
        selectColumn.setCellFactory(column -> new TableCell<CustomerOrder, Boolean>() {
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
        });

        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Date formatting without leading zeros for month and day (M/d format)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d");

        // Customize orderDateColumn to show date in M/d format
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        orderDateColumn.setCellFactory(column -> new TableCell<CustomerOrder, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Parse the date string into a LocalDate and format it
                    LocalDate date = LocalDate.parse(item);  // assuming item is in yyyy-MM-dd format
                    setText(date.format(formatter));
                }
            }
        });

        orderDateColumn.setComparator((date1, date2) -> {
            LocalDate d1 = LocalDate.parse(date1);  // assuming dates are stored in yyyy-MM-dd format
            LocalDate d2 = LocalDate.parse(date2);
            return d1.compareTo(d2);  // Ascending order (earlier dates first)
        });

        driverColumn.setCellValueFactory(new PropertyValueFactory<>("driver"));
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        // Disable resizing
        selectColumn.setResizable(false);
        idColumn.setResizable(false);
        nameColumn.setResizable(false);
        orderDateColumn.setResizable(false);
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

        // Set the initial cell factory to default mode for the driver column
        driverColumn.setCellFactory(column -> new TableCell<CustomerOrder, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
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
        Tooltip editDriverTooltip = createCustomTooltip("Assign Driver", editDriverButton, 8, 10);

        // Tooltip for Dropping Off Button
        Tooltip droppingOffTooltip = createCustomTooltip("Record Drop Off", droppingOffButton, 38, 10);

        // Tooltip for Picking Up Button
        Tooltip pickingUpTooltip = createCustomTooltip("Record Pick Up", pickingUpButton, 38, 10);

        // Tooltip for Create Invoices Button
        Tooltip createInvoicesTooltip = createCustomTooltip("Create Invoices", createInvoicesButton, 8, 10);

        if (createInvoicesButton != null) {
                // Start with the button hidden
                createInvoicesButton.setOnAction(this::handleCreateInvoices);
            } else {
                System.err.println("createInvoicesButton is not injected!");
            }
        Tooltip createContractsTooltip = createCustomTooltip("Create Contracts", createContractsButton, 8, 10);

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
        String query = "SELECT customers.customer_id, customers.name, rentals.rental_date, rentals.driver, rentals.status, rentals.RefNumber, rentals.rental_id " +
                   "FROM customers JOIN rentals ON customers.customer_id = rentals.customer_id";

        // Modify query based on filter
        switch (filter) {
            case "Today's Rentals":
                query += " WHERE DATE(rentals.rental_date) = CURDATE()";
                break;
            case "Yesterday's Rentals":
                query += " WHERE DATE(rentals.rental_date) = CURDATE() - INTERVAL 1 DAY";
                break;
            case "Custom Date Range":
                // Implement custom date range logic here if needed
                break;
            case "Ended Rentals":
                query += " WHERE rentals.status = 'Ended'";
                break;
            case "All Rentals":
                // No additional filtering
                break;
        }

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/practice_db", "root", "SQL3225422!a");
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("customer_id");
                String name = resultSet.getString("name");
                String rentalDate = resultSet.getString("rental_date");
                String driver = resultSet.getString("driver");
                String status = resultSet.getString("status");
                int refNumber = resultSet.getInt("RefNumber");
                int rental_id = resultSet.getInt("rental_id");
                String deliveryTime = resultSet.getString("deliveryTime");


                ordersList.add(new CustomerOrder(id, name, rentalDate, deliveryTime, driver != null ? driver : "", status != null ? status : "Unknown", refNumber, rental_id));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    // Handle dropdown selection for filtering
    @FXML
    private void handleFilterSelection() {
        String selectedFilter = filterComboBox.getValue();
        showLoadingMessage(true);
        loadDataAsync(selectedFilter);
    }


    @FXML
    private void handleAssignDrivers() {
        System.out.println("assigndriverclicked");

        // Define the local variable for action type
        // Set initial state

        if (lastActionType == "assigning-drivers") {
            // If already in driver edit mode, revert to previous state
            lastActionType = null; // Reset the action type

            // Switch back to displaying driver names as strings
            driverColumn.setCellFactory(column -> new TableCell<CustomerOrder, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item); // Display the driver name as text
                        setGraphic(null);
                    }
                }
            });

            isDriverEditMode = false; // Exit driver edit mode
            updateRentalButton.setVisible(false); // Hide the Update Drivers button
            System.out.println("Driver assignment mode deactivated.");
        } else {
            // Switch to driver edit mode with combo boxes
            driverColumn.setCellFactory(column -> new TableCell<CustomerOrder, String>() {
                private final ComboBox<String> comboBox = new ComboBox<>(driverInitials);

                {
                    comboBox.setEditable(true);
                    comboBox.setOnAction(e -> commitEdit(comboBox.getValue()));
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        comboBox.getSelectionModel().select(item); // Pre-select the current item
                        setGraphic(comboBox);
                    }
                }

                @Override
                public void commitEdit(String newValue) {
                    super.commitEdit(newValue);
                    if (getTableRow() != null) {
                        CustomerOrder order = getTableRow().getItem();
                        if (order != null) {
                            order.setDriver(newValue); // Update the order with the new driver
                        }
                    }
                }
            });

            isDriverEditMode = true; // Enter driver edit mode
            updateRentalButton.setVisible(true); // Show the Update Drivers button
            System.out.println("Driver assignment mode activated.");
            lastActionType = "assigning-drivers";
        }
    }


    @FXML
    private void handleUpdateDrivers() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/practice_db", "root", "SQL3225422!a")) {
            String updateQuery = "UPDATE rentals SET driver = ? WHERE customer_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
                for (CustomerOrder order : dbTableView.getItems()) {
                    String updatedDriver = order.getDriver();
                    int rentalId = order.getCustomerId();  // Update this if necessary

                    statement.setString(1, updatedDriver);
                    statement.setInt(2, rentalId);

                    int rowsUpdated = statement.executeUpdate();
                    if (rowsUpdated > 0) {
                        System.out.println("Driver updated successfully for rental ID " + rentalId);
                    } else {
                        System.out.println("No update made for rental ID " + rentalId);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Exit driver edit mode (same as before)
        driverColumn.setCellFactory(column -> new TableCell<CustomerOrder, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setGraphic(null);
                }
            }
        });

        isDriverEditMode = false;
        System.out.println("Driver assignment mode deactivated.");
    }



    private void hideCheckboxes() {
        // Set the checkboxes to not be visible
        selectColumn.setCellFactory(tc -> new TableCell<CustomerOrder, Boolean>() {
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
        });
        dbTableView.refresh();
    }

   @FXML
    private void handleDroppingOff(ActionEvent event) {
        if ("dropping-off" == lastActionType) {
            lastActionType = null;
            hideCheckboxes();
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
    private void handlePickingUp(ActionEvent event) {
        if ("picking-up" == lastActionType) {
            lastActionType = null; // Reset the action type if the button is pressed again
            hideCheckboxes();
            showSelectableCheckboxes(false, lastActionType);
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
            System.out.println("Resetting action type. Checkboxes hidden.");
            return; // Exit the method
        } else {
            lastActionType = "creating-contracts"; // Set the action type
            resetCheckboxes(); // Deselect all checkboxes first
            showSelectableCheckboxes(true, lastActionType); // Show the checkboxes
            System.out.println("Action type set to 'creating-contracts'. Checkboxes shown.");
        }
    }


    private void updateInvoiceFlagInDB(int customerId, boolean isFlagged) {
        String updateQuery = "UPDATE rentals SET is_flagged = ? WHERE customer_id = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/practice_db", "root", "SQL3225422!a");
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

            statement.setInt(1, isFlagged ? 1 : 0); // Convert boolean to int (1 or 0)
            statement.setInt(2, customerId);

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
        for (CustomerOrder order : dbTableView.getItems()) {
            order.setSelected(false);
        }
        dbTableView.refresh(); // Refresh the table view to update the checkbox states
    }


    private void showSelectableCheckboxes(boolean visible, String actionType) {
        boolean shouldShowCheckboxes = "dropping-off".equals(actionType) || "picking-up".equals(actionType) || "creating-invoices".equals(actionType);

        selectColumn.setCellFactory(tc -> new TableCell<CustomerOrder, Boolean>() {
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
        });
    }


    private void handleSelection(boolean isSelected, int index, String actionType) {
        dbTableView.getItems().get(index).setSelected(isSelected);

        // Show the update button only if at least one row is selected
        boolean anySelected = dbTableView.getItems().stream().anyMatch(CustomerOrder::isSelected);
        updateRentalButton.setVisible(anySelected);

    }


    @FXML
    private void handleUpdateRental(ActionEvent event) {
        ObservableList<CustomerOrder> selectedRentals = dbTableView.getItems().filtered(CustomerOrder::isSelected);
        boolean statusUpdated = false;

        // Handle the 'creating-invoices' action type
        if (lastActionType.equals("creating-invoices")) {
            for (CustomerOrder order : selectedRentals) {
                if (order.getStatus().equals("Ended")) {
                    order.setFlagged(true);
                    updateInvoiceFlagInDB(order.getCustomerId(), true); // Flag the order for invoicing
                    System.out.println("Order for " + order.getName() + " flagged for invoicing.");
                    statusUpdated = true;
                    checkAndSwitchScene(statusUpdated);
                } else {
                    System.out.println("Order for " + order.getName() + " cannot be flagged; status is not 'Ended'.");
                }
            }
        } else if (lastActionType.equals("creating-contracts")) {
            // Handle the driver assignment status updates
            for (CustomerOrder order : selectedRentals) {

            }
        } else if (lastActionType.equals("assigning-drivers")) {
            // Handle the driver assignment status updates
            for (CustomerOrder order : selectedRentals) {
                String newStatus = "Driver Assigned"; // Set the appropriate new status
                order.setStatus(newStatus);
                statusUpdated = true;
                System.out.println("Order for " + order.getName() + " status updated to 'Driver Assigned'.");
            }
        } else if (lastActionType.equals("picking-up")) {
            // Handle the 'picking-up' action type
            for (CustomerOrder order : selectedRentals) {
                String newStatus = "Ended";  // Set the status for picking-up
                order.setStatus(newStatus);
                updateRentalStatusInDB(order.getCustomerId(), newStatus);  // Sync with DB
                statusUpdated = true;
                System.out.println("Order for " + order.getName() + " status updated to 'Ended'.");
            }
        } else {
            // Existing logic for other action types
            for (CustomerOrder order : selectedRentals) {
                String newStatus = determineNewStatus(order, lastActionType);
                if (!newStatus.equals(order.getStatus())) {
                    order.setStatus(newStatus);
                    updateRentalStatusInDB(order.getCustomerId(), newStatus);  // Sync with DB
                    statusUpdated = true;
                    System.out.println("Order for " + order.getName() + " status updated to '" + newStatus + "'.");
                }
            }
        }

        dbTableView.refresh();

        if (statusUpdated) {
            lastActionType = null;
            hideCheckboxes(); // If needed, based on logic
            updateRentalButton.setVisible(false);

        }
    }


    private void updateRentalStatusInDB(int customerId, String newStatus) {
        String updateQuery = "UPDATE rentals SET status = ? WHERE customer_id = ?"; // Update table name

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/practice_db", "root", "SQL3225422!a");
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

            statement.setString(1, newStatus);
            statement.setInt(2, customerId);

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


    private void highlightRow(CustomerOrder order, String color) {
        TableRow<CustomerOrder> row = new TableRow<>();
        row.setStyle("-fx-background-color: " + color + ";");
    }

    private String determineNewStatus(CustomerOrder order, String actionType) {
        String currentStatus = order.getStatus();
 //       System.out.println("Determining new status for Order ID " + order.getCustomerId() + ": Current Status = " + currentStatus + ", Action Type = " + actionType);

        if (actionType.equals("dropping-off")) {
            if (currentStatus.equals("Upcoming")) {
                return "Active"; // This should work if currentStatus is "Upcoming"
            }
        } else if (actionType.equals("picking-up")) {
            if (currentStatus.equals("Active")) {
                return "Ended"; // This should work if currentStatus is "Active"
            }
        }

        // If no status change applies
 //       System.out.println("No status change applied for Order ID " + order.getCustomerId() + ". Returning current status: " + currentStatus);
        return currentStatus;
    }

    private void createContractPDF(CustomerOrder rental, String contractPath) {
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
                                   "Rental ID: " + rental.getRentalId() + "\n" +
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

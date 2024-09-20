package com.MaxHighReach;

import com.MaxHighReach.utils.StatusNodeFactory;

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

public class DBController extends BaseController {

    @FXML
    private Button backButton, editDriverButton, droppingOffButton, pickingUpButton, updateDriversButton, updateStatusButton, createInvoicesButton, refreshButton;
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

        if (updateStatusButton != null) {
            updateStatusButton.setVisible(false); // Start with the button hidden
        } else {
            System.err.println("updateStatusButton is not injected!");
        }
        hideCheckboxes(); // Call the method to hide checkboxes initially

        // Set tooltip for Edit Driver Button
        Tooltip editDriverTooltip = new Tooltip("Edit Driver");
        editDriverTooltip.setShowDelay(Duration.ZERO); // Instant tooltip
        Tooltip.install(editDriverButton, editDriverTooltip);
        editDriverTooltip.setAnchorX(8);

        // Set tooltip for Dropping Off Button
        Tooltip droppingOffTooltip = new Tooltip("Dropping Off");
        droppingOffTooltip.setShowDelay(Duration.ZERO); // Instant tooltip
        Tooltip.install(droppingOffButton, droppingOffTooltip);
        droppingOffTooltip.setAnchorX(38);

        // Set tooltip for Picking Up Button
        Tooltip pickingUpTooltip = new Tooltip("Picking Up");
        pickingUpTooltip.setShowDelay(Duration.ZERO); // Instant tooltip
        Tooltip.install(pickingUpButton, pickingUpTooltip);
        pickingUpTooltip.setAnchorX(38);

        // Set tooltip for Create Invoices Button
        Tooltip createInvoicesTooltip = new Tooltip("Create Invoices");
        createInvoicesTooltip.setShowDelay(Duration.ZERO); // Instant tooltip
        Tooltip.install(createInvoicesButton, createInvoicesTooltip);
        createInvoicesTooltip.setAnchorX(8);

        // Add the new selection column with circles
       // TableColumn<CustomerOrder, Boolean> selectionColumn = new TableColumn<>("Select");
       // selectionColumn.setCellValueFactory(param -> new SimpleBooleanProperty(param.getValue().isSelected()));
       // selectionColumn.setPrefWidth(30);


        /*
        // Custom cell factory to display circles in the selection column
        selectionColumn.setCellFactory(column -> new TableCell<CustomerOrder, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Circle circle = new Circle(8); // Create a circle with radius 8
                    if (item) {
                        circle.setFill(Color.GREEN); // Fill with green if selected
                    } else {
                        circle.setFill(Color.RED); // Fill with red if not selected
                    }
                    setGraphic(circle); // Set the circle as the graphic of the cell
                }
            }
        }); delete if works */

        // Add the selection column to the table
       // dbTableView.getColumns().add(0, selectionColumn);
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

    // Method to load data from the database
    private void loadData(String filter) {
        ordersList.clear();
        String query = "SELECT customers.customer_id, customers.name, orders.order_date, orders.driver, orders.status " +
                   "FROM customers JOIN orders ON customers.customer_id = orders.customer_id";

        // Modify query based on filter
        switch (filter) {
            case "Today's Rentals":
                query += " WHERE DATE(orders.order_date) = CURDATE()";
                break;
            case "Yesterday's Rentals":
                query += " WHERE DATE(orders.order_date) = CURDATE() - INTERVAL 1 DAY";
                break;
            case "Custom Date Range":
                // Implement custom date range logic here if needed
                break;
            case "Ended Rentals":
                query += " WHERE orders.status = 'Ended'";
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
                String orderDate = resultSet.getString("order_date");
                String driver = resultSet.getString("driver");
                String status = resultSet.getString("status");

                ordersList.add(new CustomerOrder(id, name, orderDate, driver != null ? driver : "", status != null ? status : "Unknown"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
        System.out.println("assigndriverclicked");
        if (isDroppingOff) {
            isDroppingOff = false;
            hideCheckboxes();
            updateStatusButton.setVisible(false);
        }

        if (isPickingUp) {
            isPickingUp = false;
            hideCheckboxes();
            updateStatusButton.setVisible(false);
        }

        if (!isDriverEditMode) {
            // Switch to driver edit mode
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
                        setGraphic(comboBox);
                        comboBox.getSelectionModel().select(item);
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

            isDriverEditMode = true;
            updateDriversButton.setVisible(true); // Show the Update Drivers button
            System.out.println("Driver assignment mode activated.");
        }
    }

    @FXML
    private void handleUpdateDrivers() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/practice_db", "root", "SQL3225422!a")) {

            // Prepare the SQL update statement once
            String updateQuery = "UPDATE orders SET driver = ? WHERE customer_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {

                // Loop through all the items in the table
                for (CustomerOrder order : dbTableView.getItems()) {
                    String updatedDriver = order.getDriver();
                    int orderId = order.getCustomerId();  // or getId() based on how you identify the orders

                    // Set the driver and customer ID for the current order
                    statement.setString(1, updatedDriver);
                    statement.setInt(2, orderId);

                    // Execute the update
                    int rowsUpdated = statement.executeUpdate();
                    if (rowsUpdated > 0) {
                        System.out.println("Driver updated successfully for order ID " + orderId);
                    } else {
                        System.out.println("No update made for order ID " + orderId);
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
        updateDriversButton.setVisible(false); // Hide the Update Drivers button
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
        if (lastActionType == "dropping-off") {
            lastActionType = null;
            hideCheckboxes();
  //          System.out.println("Picking Up button pressed again. Resetting action type.");
            updateStatusButton.setVisible(false); // Call this method to hide checkboxes
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
        if (lastActionType == "picking-up") {
            lastActionType = null; // Reset the action type if the button is pressed again
            hideCheckboxes();
            showSelectableCheckboxes(false, lastActionType);
   //         System.out.println("Picking Up button pressed again. Resetting action type.");
            updateStatusButton.setVisible(false);
        } else {
            lastActionType = "picking-up";
            resetCheckboxes();
            showSelectableCheckboxes(true, lastActionType);
       //     System.out.println("Picking Up button pressed.");
            return; // Exit if no action type is set
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
        boolean shouldShowCheckboxes = "dropping-off".equals(actionType) || "picking-up".equals(actionType);

        // You might also want to update the table's cell factory to reflect visibility
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
        updateStatusButton.setVisible(anySelected);
    }


    @FXML
    private void handleUpdateStatus(ActionEvent event) {
        // Get the selected orders
        ObservableList<CustomerOrder> selectedOrders = dbTableView.getItems().filtered(CustomerOrder::isSelected);

        // Use the last action type
        String actionType = lastActionType;

        // Check if an action type is set
        if (actionType == null) {
       //     System.out.println("No action type set! Please select a button first.");
            return; // Exit if no action type is set
        }

    //    System.out.println("Action Type: " + actionType);
        boolean statusUpdated = false; // Track if any status was updated

        for (CustomerOrder order : selectedOrders) {
            String newStatus = determineNewStatus(order, actionType);
   //         System.out.println("Selected Order ID: " + order.getCustomerId() + ", Current Status: " + order.getStatus());
   //         System.out.println("Determining new status for Order ID " + order.getCustomerId() + ": Current Status = " + order.getStatus() + ", Action Type = " + actionType);

            if (!newStatus.equals(order.getStatus())) {
                updateOrderStatusInDB(order.getCustomerId(), newStatus);
                order.setStatus(newStatus);
   //             System.out.println("Updated Order ID: " + order.getCustomerId() + " to status: " + newStatus);
                statusUpdated = true; // Mark that at least one status was updated
            } else {
     //           System.out.println("No status change applied for Order ID " + order.getCustomerId());
            }
        }

        // Refresh the table and hide the update button
        dbTableView.refresh();


        // Reset lastActionType after a successful update
        if (statusUpdated) {
            lastActionType = null; // Resetting to null means the user must click a button again
            hideCheckboxes();
            updateStatusButton.setVisible(false); // Call this method to hide checkboxes
 //           System.out.println("lastActionType reset to null after successful update.");
        }
    }


    private void updateOrderStatusInDB(int customerId, String newStatus) {
        String updateQuery = "UPDATE orders SET status = ? WHERE customer_id = ?";
 //       System.out.println("Updating Order ID " + customerId + " to status: " + newStatus);

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/practice_db", "root", "SQL3225422!a");
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

            statement.setString(1, newStatus);
            statement.setInt(2, customerId);

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
    //            System.out.println("Order status updated successfully for customer ID " + customerId);
            } else {
   //             System.out.println("No update made for customer ID " + customerId);
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    @FXML
    private void handleCreateInvoices() {
        // Logic for creating invoices
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

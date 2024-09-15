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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.util.StringConverter;

import java.sql.*;

public class DBController extends BaseController {

    @FXML
    private Button backButton;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Button editDriverButton;

    @FXML
    private Button droppingOffButton;

    @FXML
    private Button pickingUpButton;

    @FXML
    private Button updateStatusButton;

    @FXML
    private Button refreshButton;

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
    private TableColumn<CustomerOrder, Double> amountColumn;

    @FXML
    private TableColumn<CustomerOrder, String> driverColumn;

    @FXML
    private TableColumn<CustomerOrder, Node> statusColumn;

    @FXML
    private Label loadingLabel;

    @FXML
    private ComboBox<String> filterComboBox;

    private ObservableList<CustomerOrder> ordersList = FXCollections.observableArrayList();
    private ObservableList<String> driverInitials = FXCollections.observableArrayList("JD", "AB", "MG", "CN");

    private boolean isDriverEditMode = false;

    @FXML
    public void initialize() {
        super.initialize();

        dbTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

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

        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        driverColumn.setCellValueFactory(new PropertyValueFactory<>("driver"));


        statusColumn.setCellFactory(column -> new TableCell<CustomerOrder, Node>() {
            @Override
            protected void updateItem(Node item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : item);
            }
        });

        // Disable resizing
        idColumn.setResizable(false);
        nameColumn.setResizable(false);
        orderDateColumn.setResizable(false);
        amountColumn.setResizable(false);
        driverColumn.setResizable(false);
        statusColumn.setResizable(false);

        // Initialize filter combo box
        filterComboBox.setItems(FXCollections.observableArrayList(
                "All Deliveries",
                "Today's Deliveries",
                "Yesterday's Deliveries",
                "Custom Date Range"
        ));
        filterComboBox.setValue("All Deliveries"); // Default selection

        dbTableView.setPrefWidth(265);

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
        loadDataAsync("All Deliveries");

        // Handle filter changes
        filterComboBox.setOnAction(event -> handleFilterSelection());

        if (updateStatusButton != null) {
            updateStatusButton.setVisible(false); // Start with the button hidden
        } else {
            System.err.println("updateStatusButton is not injected!");
        }

        // Add the new selection column with circles
        TableColumn<CustomerOrder, Boolean> selectionColumn = new TableColumn<>("Select");
        selectionColumn.setCellValueFactory(param -> new SimpleBooleanProperty(param.getValue().isSelected()));

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
        });

        // Add the selection column to the table
        dbTableView.getColumns().add(selectionColumn);
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
        String query = "SELECT customers.customer_id, customers.name, orders.order_date, orders.amount, orders.driver " +
                "FROM customers JOIN orders ON customers.customer_id = orders.customer_id";

        // Modify query based on filter
        switch (filter) {
            case "Today's Deliveries":
                query += " WHERE DATE(orders.order_date) = CURDATE()";
                break;
            case "Yesterday's Deliveries":
                query += " WHERE DATE(orders.order_date) = CURDATE() - INTERVAL 1 DAY";
                break;
            case "Custom Date Range":
                // Implement custom date range logic here if needed
                break;
            case "All Deliveries":
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
                double amount = resultSet.getDouble("amount");
                String driver = resultSet.getString("driver");

                ordersList.add(new CustomerOrder(id, name, orderDate, amount, driver != null ? driver : ""));
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

    // Handle driver edit in the database
    @FXML
    private void handleEditDriver(CustomerOrder order) {
        String driver = order.getDriver();
        int orderId = order.getCustomerId();

        String updateQuery = "UPDATE orders SET driver = ? WHERE customer_id = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/practice_db", "root", "SQL3225422!a");
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

            statement.setString(1, driver);
            statement.setInt(2, orderId);

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Driver updated successfully for order ID " + orderId);
            } else {
                System.out.println("No update made for order ID " + orderId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAssignDrivers() {
        if (isDriverEditMode) {
            // Return to default mode
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
            isDriverEditMode = false;
            System.out.println("Driver assignment mode deactivated.");
        } else {
            // Switch to edit mode
            driverColumn.setCellFactory(column -> new TableCell<CustomerOrder, String>() {
                private final ComboBox<String> comboBox = new ComboBox<>(driverInitials);

                {
                    comboBox.setEditable(true);
                    comboBox.setOnAction(e -> commitEdit(comboBox.getValue()));
                    comboBox.setVisible(false);
                    comboBox.setManaged(false);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        if (isEditing()) {
                            setText(null);
                            setGraphic(comboBox);
                            comboBox.getSelectionModel().select(item);
                        } else {
                            setText(item);
                            setGraphic(null);
                        }
                    }
                }

                @Override
                public void startEdit() {
                    super.startEdit();
                    if (isEmpty()) return;
                    setGraphic(comboBox);
                    setText(null);
                    comboBox.requestFocus();
                }

                @Override
                public void cancelEdit() {
                    super.cancelEdit();
                    setText(getItem());
                    setGraphic(null);
                }

                @Override
                public void commitEdit(String newValue) {
                    super.commitEdit(newValue);
                    if (getTableRow() != null) {
                        CustomerOrder order = getTableRow().getItem();
                        if (order != null) {
                            handleEditDriver(order);
                        }
                    }
                }
            });
            isDriverEditMode = true;
            System.out.println("Driver assignment mode activated.");
        }
    }

    @FXML
    private void handleDroppingOff(ActionEvent event) {
        showSelectableCircles(true, "dropping-off");
    }

    @FXML
    private void handlePickingUp(ActionEvent event) {
        showSelectableCircles(true, "picking-up");
    }

    private void showSelectableCircles(boolean visible, String actionType) {
        selectColumn.setCellFactory(tc -> new TableCell<CustomerOrder, Boolean>() {
            private final CheckBox checkBox = new CheckBox();

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    checkBox.setVisible(visible);
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
        ObservableList<CustomerOrder> selectedOrders = dbTableView.getItems().filtered(CustomerOrder::isSelected);

        for (CustomerOrder order : selectedOrders) {
            Node newStatus = determineNewStatus(order);  // Determine based on current status
            updateOrderStatusInDB(order.getCustomerId(), newStatus);
            order.setStatus(newStatus);  // Update the status in the table
        }

        // Refresh the table view to reflect changes
        dbTableView.refresh();
        updateStatusButton.setVisible(false);  // Hide the button after the update
    }


    private void updateOrderStatusInDB(int customerId, Node newStatus) {
        String statusText = ((Circle) newStatus).getFill().equals(Color.GREEN) ? "Delivered" : "Not Delivered";

        String updateQuery = "UPDATE orders SET status = ? WHERE customer_id = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/practice_db", "root", "SQL3225422!a");
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

            statement.setString(1, statusText);
            statement.setInt(2, customerId);

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Order status updated successfully for customer ID " + customerId);
            } else {
                System.out.println("No update made for customer ID " + customerId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void highlightRow(CustomerOrder order, String color) {
        TableRow<CustomerOrder> row = new TableRow<>();
        row.setStyle("-fx-background-color: " + color + ";");
    }

    private Node determineNewStatus(CustomerOrder order) {
        // Example: Toggle between two states based on current status
        if (order.getStatus() instanceof Circle) {
            Circle currentStatusCircle = (Circle) order.getStatus();
            if (currentStatusCircle.getFill().equals(Color.GREEN)) {
                return StatusNodeFactory.createStatusNode(Color.RED, "Not Delivered");
            } else {
                return StatusNodeFactory.createStatusNode(Color.GREEN, "Delivered");
            }
        } else {
            return StatusNodeFactory.createStatusNode(Color.GREEN, "Delivered");
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

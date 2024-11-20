package com.MaxHighReach;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.YearMonth;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class ComposeInvoicesController extends BaseController {

    @FXML
    private Label statusLabel;

    @FXML
    private Label progressLabel;

    @FXML
    private Label outputLabel;

    @FXML
    private VBox loadingIndicator;

    @FXML
    private TextField dateRangeTextField;

    @FXML
    private Label dateRangeLabel;


    @FXML
    private Button composeInvoicesButton;
    @FXML
    private Button openSDKButton;

    @FXML
    private TableView<CustomerRental> dbTableView;
    @FXML
    private Label loadingLabel;

    @FXML
    private HBox confirmationPrompt;

    private ObservableList<CustomerRental> ordersList = FXCollections.observableArrayList();
    private ObservableList<String> driverInitials = FXCollections.observableArrayList("JD", "AB", "MG", "CN");

    private static final String OUTPUT_DIRECTORY;
    private static final String TEMPLATE_PATH;
    private static final String SCRIPT_PATH;
    private static final String INVOICE_QUERY;
    private static final String SDK_OUTPUT;
    private static final String PREFIX;
    private static final String SRCDIR;
    private static final String SDK_PATH;

    private static boolean atWork = true;

    static {
        if (atWork) {
            PREFIX = "C:\\Users\\maxhi\\OneDrive\\Documents\\Max High Reach\\MONTH END\\";
            SRCDIR = "..\\..\\Quickbooks\\QBProgram Development\\SMM Filing\\";
        } else {
            PREFIX = "C:\\Users\\jacks\\OneDrive\\Desktop\\Professional\\Max High Reach\\SMM\\";
            SRCDIR = "Max-High-Reach\\";
        }
        OUTPUT_DIRECTORY = PREFIX;
        TEMPLATE_PATH = PREFIX + "SMM template 2020.xlsx";
        SCRIPT_PATH = PREFIX + SRCDIR + "scripts\\orchestrate_process.py";
        INVOICE_QUERY = PREFIX + SRCDIR + "scripts\\invoice_batch.xml";
        SDK_OUTPUT = PREFIX + SRCDIR + "outputs\\QBResponse.xml";
        SDK_PATH = PREFIX + "..\\..\\Quickbooks\\QBProgram Development\\Intuit Applications\\IDN\\QBSDK16.0\\tools\\SDKTest\\SDKTestPlus3.exe";
    }

    private void updateProgressLabel(String message) {
        Platform.runLater(() -> progressLabel.setText(message));
    }

    private void updateStatusLabel(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    @FXML
    public void initialize() {
        super.initialize();

        dbTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

                // Enable table editing
        dbTableView.setEditable(true);

        // Load data initially
        showLoadingMessage(true);
        loadData();
        showLoadingMessage(false);

        Image image = new Image(getClass().getResourceAsStream("/images/send-to-quickbooks.png"));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(17);
        imageView.setFitWidth(20);

        HBox hbox = new HBox();
        hbox.getChildren().addAll(new Label("Send to Quickbooks  "), imageView);

        openSDKButton.setGraphic(hbox);

        // Path of the file you want to delete
        String saveDirectory = "C:\\Users\\maxhi\\OneDrive\\Documents\\Quickbooks\\QBProgram Development\\Invoice Creating";
        String filename = "invoice_batch.xml";
        String filePath = saveDirectory + "\\" + filename;


        // Check if the file exists and delete it if found
        File file = new File(filePath);

        if (file.exists()) {
            try {
                Files.delete(Paths.get(filePath));  // Delete the file
                System.out.println("Existing file " + filePath + " deleted.");
            } catch (Exception e) {
                System.err.println("Error deleting the file: " + e.getMessage());
            }
        } else {
            System.out.println("No existing file found to delete.");
        }

    }

    private void loadData() {
        ordersList.clear();
        dbTableView.setItems(ordersList);

        dbTableView.setVisible(true);
        DBColumnFactory dbColumnFactory = new DBColumnFactory(dbTableView, composeInvoicesButton);
        dbTableView.getColumns().addAll(dbColumnFactory.getSelectColumn(),
                dbColumnFactory.getStatusColumn(),
                dbColumnFactory.getAddressColumn(),
                dbColumnFactory.getDeliveryDateColumn(),
                dbColumnFactory.getInvoiceColumn());

        String query = "SELECT * FROM customers " +
               "JOIN rental_orders ON customers.customer_id = rental_orders.customer_id " +
               "JOIN rental_items ON rental_orders.rental_order_id = rental_items.rental_order_id " +
                "JOIN lifts ON lifts.lift_id = rental_items.lift_id " +
                "WHERE rental_items.item_status IN ('Called Off', 'Picked Up')";

        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String id = resultSet.getString("customer_id");
                String name = resultSet.getString("customer_name");
                String rentalDate = resultSet.getString("item_delivery_date");
                String deliveryTime = resultSet.getString("delivery_time");
                String driver = resultSet.getString("driver");
                String status = resultSet.getString("item_status");
                int rental_id = resultSet.getInt("rental_order_id");
                int rental_item_id = resultSet.getInt("rental_item_id");
                String refNumber = resultSet.getString("customer_ref_number");
                String serialNumber = resultSet.getString("serial_number");
                String liftType = resultSet.getString("lift_type");
                boolean isInvoiceWrit = resultSet.getBoolean("invoice_composed");

                CustomerRental rental = new CustomerRental(id, name, rentalDate, deliveryTime, driver != null ? driver : "", status != null ? status : "Unknown", refNumber, rental_id);
                rental.setRentalItemId(rental_item_id);
                rental.setSerialNumber(serialNumber);
                rental.setLiftType(liftType);
                rental.setInvoiceWritten(isInvoiceWrit);

                ordersList.add(rental);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        clearAllComposingInvoiceInDB();
    }

    @FXML
    private void handleComposeInvoices(ActionEvent event) {
        System.out.println("Compose Invoices button clicked");


        // Get selected rentals from the table
        ObservableList<CustomerRental> selectedRentals = dbTableView.getItems().filtered(CustomerRental::isSelected);


        // Check if any rentals are selected
        if (selectedRentals.isEmpty()) {
            System.out.println("No rentals selected.");
            statusLabel.setText("No rentals selected.");
            return;
        }


        System.out.println("Number of selected rentals: " + selectedRentals.size());

        for (CustomerRental rental : ordersList) {
            rental.setWritingInvoice(false);
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

            statusLabel.setText("Selected rentals updated successfully.");
            // Path to the Python script
            String scriptPath = "C:\\Users\\maxhi\\OneDrive\\Documents\\Quickbooks\\QBProgram Development\\Invoice Creating\\.venv\\Scripts\\make_invoices_from_queue.py";

            // Execute the Python script directly without passing data
            executePythonScript(scriptPath);

            openSDKButton.setVisible(true);
        } else {
            System.out.println("No rental items were updated.");
            statusLabel.setText("No rentals updated.");
        }
        resetCheckboxes();
        dbTableView.refresh();
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

            Platform.runLater(() -> {
                if (exitCode == 0) {
                    confirmationPrompt.setVisible(true);
                }
            });

            if (exitCode != 0) {
                System.err.println("Python script returned an error.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOpenSDK(ActionEvent event){
        runSDKTool();
    }

    private void resetCheckboxes() {
        // Deselect all checkboxes in the table
        for (CustomerRental order : dbTableView.getItems()) {
            order.setSelected(false);
        }
    }

    @FXML
    public void handleConfirmationYes(){
        for (CustomerRental rental : ordersList) {
            if (rental.isWritingInvoice()) {
                updateInvoiceWrittenInDB(rental.getRentalItemId());
            }
        }
        confirmationPrompt.setVisible(false);
        dbTableView.refresh();
    }

    @FXML
    public void handleConfirmationNo(){
        confirmationPrompt.setVisible(false);
    }

    @FXML
    public void handleBack(ActionEvent event) {
        try {
            MaxReachPro.goBack("/fxml/compose_invoices.fxml");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void showLoadingMessage(boolean isLoading) {
        if (isLoading) {
            loadingLabel.setText("Loading database...");
            loadingLabel.setVisible(true);
            dbTableView.setVisible(false);

        } else {
            loadingLabel.setVisible(false);
            dbTableView.setVisible(true);
        }
    }

    @Override
    public double getTotalHeight() {
        boolean hardCode = true;
        if (hardCode) {
            return 165;
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

    private void runSDKTool() {
        // Debug output path
        System.out.println("SDK Path: " + SDK_PATH);

        File sdkToolFile = new File(SDK_PATH);
        if (!sdkToolFile.exists()) {
            Platform.runLater(() -> {
                statusLabel.setText("File does not exist: " + SDK_PATH);
                loadingIndicator.setVisible(false);
            });
            return;
        }

        Platform.runLater(() -> {
            statusLabel.setVisible(false);
            progressLabel.setText(
                "Instructions:\n" +
                "1. Select \"invoice_batch.xml\" as the \"Rsquest File\"\n" +
                "2. Open QuickBooks\n" +
                "3. Click \"Open Connection\"\n" +
                "4. Click \"Begin Session\"\n" +
                "5. Click \"Send XML to Request Processor\"\n" +
                "Finished!  -  Click Yes below if new QB invoices exist"
            );
            progressLabel.setVisible(true);
            progressLabel.setTranslateX(-20);
            progressLabel.setTranslateY(-25);
        });

        new Thread(() -> {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(SDK_PATH);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                // Read and output the process's standard output and error streams
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder outputBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    outputBuilder.append(line).append("\n");
                    Platform.runLater(() -> progressLabel.setText(outputBuilder.toString()));
                }

                int exitCode = process.waitFor();
                Platform.runLater(() -> {
                    if (exitCode == 0) {
                        statusLabel.setText("SDK Tool executed successfully.");
                    } else {
                        statusLabel.setText("SDK Tool failed with exit code: " + exitCode);
                    }
                    progressLabel.setText("");
                    loadingIndicator.setVisible(false);
                });

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Error running SDK Tool.");
                    progressLabel.setText("");
                    loadingIndicator.setVisible(false);
                });
            }
        }).start();
    }

    private void updateInvoiceWrittenInDB(int rentalItemId) {
        System.out.println("Attempting to update rental item ID: " + rentalItemId);
        String updateQuery = "UPDATE rental_items SET invoice_composed = 1 WHERE rental_item_id = ?";
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

}
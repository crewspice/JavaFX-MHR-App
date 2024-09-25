package com.MaxHighReach;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.YearMonth;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.google.gson.Gson;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class CreateInvoicesController extends BaseController {

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
    private HBox confirmationPrompt;

    @FXML
    private Button refreshButton, composeInvoicesButton;

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
        INVOICE_QUERY = PREFIX + SRCDIR + "scripts\\invoice_query.xml";
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
        runSDKTool();
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

        showSelectableCheckboxes(true);
        composeInvoicesButton.setVisible(isAnySelected());
    }

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
        String query = "SELECT customers.customer_id, customers.name, rentals.rental_date, rentals.driver, rentals.status " +
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

                ordersList.add(new CustomerOrder(id, name, rentalDate, driver != null ? driver : "", status != null ? status : "Unknown"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFilterSelection() {
        String selectedFilter = filterComboBox.getValue();
        showLoadingMessage(true);
        loadDataAsync(selectedFilter);
    }

    @FXML
    private void handleRefresh() {
        showLoadingMessage(true);
        loadDataAsync (filterComboBox.getValue());
    }

    private void handleSelection(boolean isSelected, int index) {
        dbTableView.getItems().get(index).setSelected(isSelected);

        // Show the update button only if at least one row is selected

        composeInvoicesButton.setVisible(isAnySelected());

    }

    private boolean isAnySelected() {
        return dbTableView.getItems().stream().anyMatch(CustomerOrder::isSelected);
    }

    private void showSelectableCheckboxes(boolean visible) {
        selectColumn.setCellFactory(tc -> new TableCell<CustomerOrder, Boolean>() {
            private final CheckBox checkBox = new CheckBox();

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    CustomerOrder order = getTableView().getItems().get(getIndex());
                    checkBox.setVisible(visible);
                    checkBox.setSelected(order.isSelected() || order.isFlagged()); // Default checked if flagged
                    setGraphic(checkBox);

                    checkBox.setOnAction(e -> {
                        handleSelection(checkBox.isSelected(), getIndex());
                        order.setSelected(checkBox.isSelected());
                        System.out.println("Checkbox at index " + getIndex() + " selected: " + checkBox.isSelected());
                    });
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    @FXML
    private void handleComposeInvoices(ActionEvent event) {
        System.out.println("Compose Invoices button clicked");

        // Path to the Python script
        String scriptPath = "C:\\Users\\maxhi\\OneDrive\\Documents\\Quickbooks\\QBProgram Development\\Invoice Creating\\.venv\\Scripts\\make_invoices_from_queue.py";

        // Execute the Python script directly without passing data
        executePythonScript(scriptPath);
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



    @FXML
    public void handleBack(ActionEvent event) {
        try {
            MaxReachPro.goBack("/fxml/create_invoices.fxml");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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


    @FXML
    private void handleConfirmationYes(ActionEvent event) {
        Platform.runLater(() -> {
            // Hide confirmation prompt and "Please use the Quickbooks SDK..." message
            confirmationPrompt.setVisible(false);
            statusLabel.setText("Processing...");
            runPythonScript();
        });
    }

    @FXML
    private void handleConfirmationNo(ActionEvent event) {
        Platform.runLater(() -> {
            confirmationPrompt.setVisible(false);
            statusLabel.setText("Please prepare the invoices file and try again.");
        });
    }

   private void runPythonScript() {
        // Hide the status label when the script starts running
        Platform.runLater(() -> statusLabel.setVisible(false));

        System.out.println("SDK Path: " + SDK_PATH);

        new Thread(() -> {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("python", SCRIPT_PATH, SDK_OUTPUT);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                Platform.runLater(() -> progressLabel.setText("Running Python script..."));

                StringBuilder outputBuilder = new StringBuilder();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Accumulate the output lines in the StringBuilder
                        outputBuilder.append(line).append("\n");

                        // Update the progressLabel with the latest output
                        Platform.runLater(() -> progressLabel.setText(outputBuilder.toString()));
                    }
                }

                int exitCode = process.waitFor();
                Platform.runLater(() -> {
                    if (exitCode == 0) {
                        renameGeneratedFile();
                        statusLabel.setText("SMM file completed.");
                        statusLabel.setVisible(false);  // Show the status label again
                        progressLabel.setText("Script finished successfully.");
                    } else {
                        statusLabel.setText("SMM Task failed with exit code: " + exitCode);
                        statusLabel.setVisible(true);  // Show the status label again
                        progressLabel.setText("Script encountered an error.");
                    }
                    loadingIndicator.setVisible(false);
                });

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("SMM Task encountered an error.");
                    statusLabel.setVisible(true);  // Show the status label again
                    progressLabel.setText("Error during script execution.");
                    loadingIndicator.setVisible(false);
                });
            }
        }).start();
    }


    private void renameGeneratedFile() {
        String dateRange = normalizeDateRange(dateRangeTextField.getText());
        if (dateRange == null) {
            Platform.runLater(() -> statusLabel.setText("Date range is not valid."));
            return;
        }

        String tempFilePath = OUTPUT_DIRECTORY + "Filled_SMM_Temp.xlsx";
        String newFileName = "SMM_" + dateRange + ".xlsx";
        File tempFile = new File(tempFilePath);
        File renamedFile = new File(OUTPUT_DIRECTORY, newFileName);

        int count = 1;
      //  File renamedFile = new File(OUTPUT_DIRECTORY, "SMM_" + dateRange + "(" + count + ").xlsx");

        // Print the initial state of the renamedFile
        System.out.println("Initial renamedFile path: " + renamedFile.getAbsolutePath());

        while (renamedFile.exists()) {
            newFileName = "SMM_" + dateRange + "(" + count + ").xlsx";
            renamedFile = new File(OUTPUT_DIRECTORY, newFileName);
            count++;

            // Print the new state of the renamedFile each iteration
            System.out.println("Checking if renamedFile exists: " + renamedFile.getAbsolutePath());
        }

        if (tempFile.exists()) {
            System.out.println("Temporary file exists: " + tempFile.getAbsolutePath());

            if (tempFile.renameTo(renamedFile)) {
                Platform.runLater(() -> statusLabel.setText("SMM file completed."));
            } else {
                Platform.runLater(() -> statusLabel.setText("File renaming failed."));
                System.out.println("Failed to rename file. Temp file: " + tempFile.getAbsolutePath() + ", Renamed file: " + renamedFile.getAbsolutePath());
            }
        } else {
            Platform.runLater(() -> statusLabel.setText("Temporary file does not exist."));
            System.out.println("Temporary file does not exist: " + tempFile.getAbsolutePath());
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
                "1. Fill in the request file\n" +
                "   i. Click \"Browse\" by \"Request File\"\n" +
                "   ii. Select \"invoice_query.xml\"\n" +
                "2. Open QuickBooks\n" +
                "3. Click \"Open Connection\"\n" +
                "4. Click \"Begin Session\"\n" +
                "5. Click \"Send XML to Request Processor\"\n" +
                "6. Wait for invoices to be received\n" +
                "7. Click \"View Output\"\n" +
                "8. Save that file\n" +
                "   i. Use shortcut Ctrl + S\n" +
                "   ii. Overwrite \"QBResponse.xml\"\n" +
                "Finished!   -    Click Yes below"
            );
            progressLabel.setVisible(true);
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

    private String normalizeDateRange(String dateRange) {
        Pattern[] patterns = {
                Pattern.compile("(\\d{2})-(\\d{2})"),  // MM-YY
                Pattern.compile("(\\d{2})/(\\d{2})"),  // MM/YY
                Pattern.compile("(\\d{2})/(\\d{4})"),   // MM/YYYY
                Pattern.compile("(\\d{2})-(\\d{4})")   // MM-YYYY
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(dateRange);
            if (matcher.matches()) {
                String month = matcher.group(1);
                String year = matcher.group(2);

                if (year.length() == 2) {
                    year = "20" + year;
                }

                return month + "-" + year;
            }
        }

        return null;
    }

    /*

            <?qbxml version="8.0"?>
        <QBXML>
          <QBXMLMsgsRq onError="stopOnError">
            <InvoiceAddRq>
            <InvoiceAdd defMacro="MACROTYPE">
                 <CustomerRef>
                 <TemplateRef>
                 </TemplateRef>
                 </CustomerRef>
            </InvoiceAdd>
            </InvoiceAddRq>
          </QBXMLMsgsRq>
        </QBXML>

     */

    private boolean updateXmlWithDateRange(String dateRange, String xmlFilePath) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(xmlFilePath));

            Element fromDateElement = (Element) doc.getElementsByTagName("FromTxnDate").item(0);
            Element toDateElement = (Element) doc.getElementsByTagName("ToTxnDate").item(0);

            String[] parts = dateRange.split("-");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid date range format.");
            }

            String month = parts[0];
            String year = parts[1];

            LocalDate firstDate = YearMonth.of(Integer.parseInt(year), Integer.parseInt(month)).atDay(1);
            LocalDate lastDate = firstDate.withDayOfMonth(firstDate.lengthOfMonth());

            fromDateElement.setTextContent(firstDate.toString());
            toDateElement.setTextContent(lastDate.toString());

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(new File(xmlFilePath)));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean copyTemplateFile(String src, String dest) {
        try {
            File sourceFile = new File(src);
            File destFile = new File(dest);

            if (destFile.exists()) {
                destFile.delete();
            }

            Files.copy(sourceFile.toPath(), destFile.toPath());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}

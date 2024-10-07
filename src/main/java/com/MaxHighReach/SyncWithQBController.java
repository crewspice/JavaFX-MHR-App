package com.MaxHighReach;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class SyncWithQBController extends BaseController {

    @FXML
    private Label statusLabel;
    @FXML
    private Label progressLabel;
    @FXML
    private Button syncButton;
    @FXML
    private HBox confirmationPrompt;

    private static final String OUTPUT_DIRECTORY = "C:\\Users\\maxhi\\OneDrive\\Documents\\Quickbooks\\QBProgram Development\\Syncing with QB\\";
    private static final String SDK_OUTPUT = OUTPUT_DIRECTORY + "QBResponse.xml";
    private static final String SDK_PATH = "C:\\Users\\maxhi\\OneDrive\\Documents\\Quickbooks\\QBProgram Development\\Intuit Applications\\IDN\\QBSDK16.0\\tools\\SDKTest\\SDKTestPlus3.exe";

    @FXML
    public void initialize() {
        // Initialization logic here if necessary
    }

    @FXML
    public void handleOpenSDK() {
        statusLabel.setText("Processing...");
        runSDKTool();
    }

    private void runSDKTool() {
        System.out.println("SDK Path: " + SDK_PATH);

        File sdkToolFile = new File(SDK_PATH);
        if (!sdkToolFile.exists()) {
            Platform.runLater(() -> {
                statusLabel.setText("File does not exist: " + SDK_PATH);
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

                // Capture and display the output from the SDK tool
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder outputBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    outputBuilder.append(line).append("\n");
                    String output = outputBuilder.toString();
                    Platform.runLater(() -> progressLabel.setText(output));
                }

                // Capture the exit code from the process
                int exitCode = process.waitFor();  // This captures the exit code

                Platform.runLater(() -> {
                    if (exitCode == 0) {
                        statusLabel.setText("SDK Tool executed successfully.");
                        confirmationPrompt.setVisible(true);  // Show the confirmation prompt
                    } else {
                        statusLabel.setText("SDK Tool failed with exit code: " + exitCode);
                    }
                    progressLabel.setText("");
                });

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Error running SDK Tool.");
                    progressLabel.setText("");
                });
            }
        }).start();
    }

    @FXML
    public void handleConfirmationYes() {
        System.out.println("Confirmation Yes button clicked");
        parseResponseFile(new File(SDK_OUTPUT));
    }

    @FXML
    public void handleConfirmationNo() {
        Platform.runLater(() -> {
            confirmationPrompt.setVisible(false);
            statusLabel.setText("Please try syncing again.");
        });
    }

    private void parseResponseFile(File responseFile) {
        if (!responseFile.exists()) {
            updateStatusLabel("Response file does not exist at: " + responseFile.getAbsolutePath());
            return;
        }

        try {
            // Load and parse the XML response
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(responseFile);
            doc.getDocumentElement().normalize();

            NodeList customerNodes = doc.getElementsByTagName("CustomerRet");

            // Connect to the database
            String connectionUrl = "jdbc:mysql://localhost:3306/practice_db"; // Update with your DB connection details
            try (Connection connection = DriverManager.getConnection(connectionUrl, "root", "SQL3225422!a")) {
                String query = "INSERT INTO customers (customer_id, customer_name, email, price_schedule, contact_method, po_required) " +
                               "VALUES (?, ?, ?, ?, ?, ?) " +
                               "ON DUPLICATE KEY UPDATE customer_name=?, email=?, price_schedule=?, contact_method=?, po_required=?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    for (int i = 0; i < customerNodes.getLength(); i++) {
                        Element customerElement = (Element) customerNodes.item(i);

                        // Extract customer details
                        String customerId = getElementTextContent(customerElement, "ListID");
                        String fullName = getElementTextContent(customerElement, "FullName"); // This contains the customer name and additional info
                        String email = getElementTextContent(customerElement, "Email");

                        // Initialize default values
                        String priceSchedule = "SPR"; // Default pricing scheme
                        String contactMethod = "E"; // Default contact method
                        int poRequired = 0; // Default: PO not required

                        // Split the full name to extract the customer name, price schedule, and contact method
                        String[] parts = fullName.split(" - "); // Split name by hyphens
                        String customerName = parts[0].trim(); // First part is the customer name

                        if (parts.length > 1) {
                            priceSchedule = parts[1].trim(); // Second part is the price schedule
                            if (parts.length > 2) {
                                if (parts[2].equalsIgnoreCase("PO")) {
                                    poRequired = 1; // Set PO required flag
                                } else {
                                    contactMethod = parts[2].trim(); // Third part is the contact method
                                }
                            }
                        }

                        // Validate price_schedule against acceptable enum values
                        if (!isValidPriceSchedule(priceSchedule)) {
                            System.out.println("Invalid price schedule detected for customer: " + customerName + ". Defaulting to SPR.");
                            priceSchedule = "SPR"; // Default to SPR if invalid
                        }

                        // Validate contact_method against acceptable enum values
                        if (!isValidContactMethod(contactMethod)) {
                            System.out.println("Invalid contact method detected for customer: " + customerName + ". Defaulting to E.");
                            contactMethod = "E"; // Default to E if invalid
                        }

                        // Debugging output
                        System.out.printf("Inserting customer: ID=%s, Name=%s, Email=%s, PriceSchedule=%s, ContactMethod=%s, PORequired=%d%n",
                                          customerId, customerName, email, priceSchedule, contactMethod, poRequired);

                        // Ensure we have valid customer data before inserting
                        if (customerId != null && email != null) {
                            preparedStatement.setString(1, customerId); // Store customer_id as a string
                            preparedStatement.setString(2, customerName); // Store only the customer name without the vars
                            preparedStatement.setString(3, email);
                            preparedStatement.setString(4, priceSchedule); // New field
                            preparedStatement.setString(5, contactMethod); // New field
                            preparedStatement.setInt(6, poRequired); // New field
                            preparedStatement.setString(7, customerName); // Update name if exists
                            preparedStatement.setString(8, email); // Update email if exists
                            preparedStatement.setString(9, priceSchedule); // Update price_schedule if exists
                            preparedStatement.setString(10, contactMethod); // Update contact_method if exists
                            preparedStatement.setInt(11, poRequired); // Update PO requirement
                            preparedStatement.executeUpdate();
                        } else {
                            updateStatusLabel("Missing customer data for entry: " + (i + 1));
                        }
                    }
                }
                updateStatusLabel("Customers updated successfully.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            updateStatusLabel("Error updating customers: " + e.getMessage());
        }
    }

    private String getElementTextContent(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            Element node = (Element) nodes.item(0);
            if (node != null) {
                return node.getTextContent();
            }
        }
        return null; // Return null if the element is not found
    }

    private void updateStatusLabel(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    // Method to validate the price schedule
    private boolean isValidPriceSchedule(String priceSchedule) {
        String[] validSchedules = {"SPR", "1", "2", "3", "4", "5", "SCR", "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9"};
        for (String valid : validSchedules) {
            if (valid.equals(priceSchedule)) {
                return true;
            }
        }
        return false;
    }

    // Method to validate the contact method
    private boolean isValidContactMethod(String contactMethod) {
        return "E".equals(contactMethod) || "P".equals(contactMethod) || "B".equals(contactMethod);
    }

    // Overriding getTotalHeight from BaseController
    @Override
    public double getTotalHeight() {
        return 300; // Replace with your logic
    }

    // Method to handle back button functionality
    @FXML
    public void handleBack() {
        System.out.println("Back button clicked on SyncWithQBController");
        try {
            MaxReachPro.goBack("/fxml/sync_with_qb.fxml");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

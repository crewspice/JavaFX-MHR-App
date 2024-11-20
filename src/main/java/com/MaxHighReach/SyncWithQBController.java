package com.MaxHighReach;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javafx.util.Duration;
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
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final String SDK_OUTPUT_CUSTOMERS = OUTPUT_DIRECTORY + "Customers Response.xml";
    private static final String SDK_OUTPUT_LIFTS = OUTPUT_DIRECTORY + "Lifts Response.xml";
    private static final String SDK_PATH = "C:\\Users\\maxhi\\OneDrive\\Documents\\Quickbooks\\QBProgram Development\\Intuit Applications\\IDN\\QBSDK16.0\\tools\\SDKTest\\SDKTestPlus3.exe";

    @FXML
    private HBox syncablesTilePane;
    private ToggleGroup syncablesToggleGroup = new ToggleGroup();
    @FXML
    private ToggleButton customersButton;
    @FXML
    private ToggleButton salesItemsButton;
    private ToggleButton selectedSyncableButton;

    private boolean areSyncablesRotating = false;
    private Timeline rotateSyncablesTimeline;

    @FXML
    public void initialize() {
        for (javafx.scene.Node node : syncablesTilePane.getChildren()) {
            node.getStyleClass().add("view-type-button-rotating");
            if (node instanceof ToggleButton toggleButton) {
                toggleButton.setToggleGroup(syncablesToggleGroup);

                toggleButton.setOnAction(event -> {

                    if (areSyncablesRotating) {
                        rotateSyncablesTimeline.stop();
                        areSyncablesRotating = false;
                        toggleButton.getStyleClass().removeAll("view-type-button-rotating");
                        toggleButton.getStyleClass().add("view-type-button-stopped");
                        syncablesToggleGroup.selectToggle(toggleButton);
                        selectedSyncableButton = toggleButton;
                    } else if (selectedSyncableButton == toggleButton) {
                        toggleButton.getStyleClass().removeAll("view-type-button-stopped");
                        toggleButton.getStyleClass().add("view-type-button-rotating");
                        areSyncablesRotating = true;
                        syncablesToggleGroup.selectToggle(customersButton);
                        startHighlightRotation(syncablesToggleGroup);
                    } else {
                        toggleButton.getStyleClass().removeAll("view-type-button-rotating");
                        toggleButton.getStyleClass().add("view-type-button-stopped");
                        selectedSyncableButton.getStyleClass().removeAll("view-type-button-stopped");
                        selectedSyncableButton.getStyleClass().add("view-type-button-rotating");
                        selectedSyncableButton = toggleButton;
                    }
                });
            }
        }
        startHighlightRotation(syncablesToggleGroup);
    }

    private void startHighlightRotation(ToggleGroup toggleGroup) {
        System.out.println("Starting highlight rotation for" + toggleGroup);
        // Start rotation styling for each toggle button in the group
        for (Toggle toggle : toggleGroup.getToggles()) {
            if (toggle instanceof ToggleButton toggleButton) {
                if (toggleGroup == syncablesToggleGroup) {
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

        selectedSyncableButton = customersButton; // Set an initial toggle button
        currentToggle = selectedSyncableButton; // Store current toggle

        if (toggleGroup == syncablesToggleGroup) {
            areSyncablesRotating = true;
            // Set the initial selected button and configure the timeline for rotation
            selectedSyncableButton = customersButton; // Set an initial toggle button
            toggleGroup.selectToggle(selectedSyncableButton); // Make sure it's selected in the toggle group
            rotateSyncablesTimeline = new Timeline();
        }
        System.out.println("Starting the rotation with button: " + currentToggle.getText());


        // Define the keyframe to toggle through views
            KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), event -> {
            Toggle nextToggle = new ToggleButton(); // Get the next toggle

            if (selectedSyncableButton == customersButton) {
                nextToggle = salesItemsButton;
            } else if (selectedSyncableButton == salesItemsButton) {
                nextToggle = customersButton;
            }

            // Update the selection in the ToggleGroup
            toggleGroup.selectToggle(nextToggle);
            ToggleButton nextButton = (ToggleButton) nextToggle;
            String groupName = new String("");
            if (toggleGroup == syncablesToggleGroup) {
                groupName = "views";
                selectedSyncableButton = (ToggleButton) nextToggle;
            }

        });

        if (toggleGroup == syncablesToggleGroup) {
            // Set up and start the rotating timeline
            rotateSyncablesTimeline.getKeyFrames().add(keyFrame);
            rotateSyncablesTimeline.setCycleCount(Timeline.INDEFINITE);
            rotateSyncablesTimeline.play();
        }

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
        if (selectedSyncableButton == null) {
            System.out.println("No syncable button selected.");
            return;
        } else {
            if (selectedSyncableButton == customersButton) {
                System.out.println("Parsing customer data...");
                parseResponseFile(new File(SDK_OUTPUT_CUSTOMERS));
            } else if (selectedSyncableButton == salesItemsButton) {
                System.out.println("Parsing lift data...");
                parseResponseFile(new File(SDK_OUTPUT_LIFTS));
            }
        }
    }


    @FXML
    public void handleConfirmationNo() {
        Platform.runLater(() -> {
            confirmationPrompt.setVisible(false);
            statusLabel.setText("Please try syncing again.");
        });
    }

    private void parseResponseFile(File responseFile) {
        System.out.println("Parsing response file: " + responseFile.getAbsolutePath());

        if (!responseFile.exists()) {
            updateStatusLabel("Response file does not exist at: " + responseFile.getAbsolutePath());
            System.out.println("Error: Response file does not exist.");
            return;
        }


        try {
            // Load and parse the XML response
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(responseFile);
            doc.getDocumentElement().normalize();
            System.out.println("XML document parsed successfully.");


            // Determine which table to populate based on the response file
            if (responseFile.getAbsolutePath().equals(SDK_OUTPUT_CUSTOMERS)) {
                System.out.println("Parsing customer data...");
                parseCustomerData(doc);
            } else if (responseFile.getAbsolutePath().equals(SDK_OUTPUT_LIFTS)) {
                System.out.println("Parsing lift data...");
                parseLiftData(doc);
            }


        } catch (Exception e) {
            e.printStackTrace();
            updateStatusLabel("Error parsing the response file: " + e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
    }


    private void parseCustomerData(Document doc) {
        NodeList customerNodes = doc.getElementsByTagName("CustomerRet");
        System.out.println("Number of customers found: " + customerNodes.getLength());


        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD)) {
            String query = "INSERT INTO customers (customer_id, customer_name, name_with_codes, email, price_schedule, contact_method, po_required) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                           "ON DUPLICATE KEY UPDATE customer_name=?, email=?, price_schedule=?, contact_method=?, po_required=?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                for (int i = 0; i < customerNodes.getLength(); i++) {
                    System.out.println("Processing entry " + (i + 1));

                    Element customerElement = (Element) customerNodes.item(i);


                    // Extract customer details
                    String customerId = getElementTextContent(customerElement, "ListID");
                    System.out.println("Customer ID: " + (customerId != null ? customerId : "NULL"));

                    String fullName = getElementTextContent(customerElement, "FullName");
                    System.out.println("Full Name: " + (fullName != null ? fullName : "NULL"));

                    String email = getElementTextContent(customerElement, "Email");
                    System.out.println("Email: " + (email != null ? email : "NULL"));


                    // Initialize default values
                    String priceSchedule = "SPR"; // Default pricing scheme
                    String contactMethod = "E"; // Default contact method
                    int poRequired = 0; // Default: PO not required


                    // Split the full name to extract the customer name, price schedule, and contact method
                    if (fullName != null) {
                        String[] parts = fullName.split(" - ");
                        String customerName = parts[0].trim();
                        System.out.println("Extracted Customer Name: " + customerName);


                        if (parts.length > 1) {
                            priceSchedule = parts[1].trim();
                            System.out.println("Extracted Price Schedule: " + priceSchedule);


                            if (parts.length > 2) {
                                if (parts[2].equalsIgnoreCase("PO")) {
                                    poRequired = 1;
                                    System.out.println("PO Required: Yes");
                                } else {
                                    contactMethod = parts[parts.length - 1].trim();
                                    System.out.println("Extracted Contact Method: " + contactMethod);
                                }
                            }
                        }


                        // Insert or update customer in the database
                        if (customerId != null && email != null) {
                            preparedStatement.setString(1, customerId);
                            preparedStatement.setString(2, customerName);
                            preparedStatement.setString(3, fullName);
                            preparedStatement.setString(4, email);
                            preparedStatement.setString(5, priceSchedule);
                            preparedStatement.setString(6, contactMethod);
                            preparedStatement.setInt(7, poRequired);
                            preparedStatement.setString(8, customerName);
                            preparedStatement.setString(9, email);
                            preparedStatement.setString(10, priceSchedule);
                            preparedStatement.setString(11, contactMethod);
                            preparedStatement.setInt(12, poRequired);

                            int rowsAffected = preparedStatement.executeUpdate();
                            System.out.println("Inserted/Updated customer: " + customerId + " | Rows affected: " + rowsAffected);
                        } else {
                            updateStatusLabel("Missing customer data for entry: " + (i + 1));
                            System.out.println("Error: Missing customer data for entry " + (i + 1));
                        }
                    } else {
                        System.out.println("Error: Full name is null for entry " + (i + 1));
                    }
                }
            }
            updateStatusLabel("Customers updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            updateStatusLabel("Error updating customers: " + e.getMessage());
            System.out.println("Error updating customers: " + e.getMessage());
        }
    }



    private void parseLiftData(Document doc) {
        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD)) {
            // Insert or update hardcoded lifts into the database
            String hardcodedLiftsQuery = "INSERT INTO lifts (lift_type, serial_number, quickbooks_list_id, quickbooks_full_name, description, generic) " +
                                         "VALUES (?, ?, ?, ?, ?, ?) " +
                                         "ON DUPLICATE KEY UPDATE lift_type=?, serial_number=?, quickbooks_list_id=?, quickbooks_full_name=?, description=?, generic=?";
            try (PreparedStatement hardcodedStatement = connection.prepareStatement(hardcodedLiftsQuery)) {
                // Sample hardcoded data
                Object[][] hardcodedLifts = {
                    {"12m", "", "800002A9-1731200839", "12 MAST", "12' Mast Lift", 1},
                    {"19s", "", "800002AB-1731201559", "19 Slim", "19' Scissor Lift", 1},
                    {"26s", "", "800002AC-1731201610", "26 Slim", "26' Scissor Lift", 1},
                    {"26", "", "1EF0000-0", "26", "26' Scissor Lift", 1},
                    {"32", "", "800002A8-1731200748", "3246", "32' Scissor Lift", 1},
                    {"40", "", "800002AA-1731201260", "40", "40' Scissor Lift", 1},
                    {"33rt", "", "A70001-0", "174857 - 33RT", "33' Rough Terrain Scissor Lift", 1},
                    {"45b", "", "940001-0", "114266 - 45B", "45' Boom Lift", 1}
                };

                // Insert or update each hardcoded lift
                for (Object[] lift : hardcodedLifts) {
                    hardcodedStatement.setString(1, (String) lift[0]);
                    hardcodedStatement.setString(2, (String) lift[1]);
                    hardcodedStatement.setString(3, (String) lift[2]);
                    hardcodedStatement.setString(4, (String) lift[3]);
                    hardcodedStatement.setString(5, (String) lift[4]);
                    hardcodedStatement.setInt(6, (int) lift[5]);
                    hardcodedStatement.setString(7, (String) lift[0]);
                    hardcodedStatement.setString(8, (String) lift[1]);
                    hardcodedStatement.setString(9, (String) lift[2]);
                    hardcodedStatement.setString(10, (String) lift[3]);
                    hardcodedStatement.setString(11, (String) lift[4]);
                    hardcodedStatement.setInt(12, (int) lift[5]);
                    hardcodedStatement.executeUpdate();
                    System.out.println("Inserted/Updated hardcoded lift: " + lift[3]);
                }
            }

            // Parse and insert XML data after hardcoded lifts have been added
            NodeList liftNodes = doc.getElementsByTagName("ItemServiceRet");
            System.out.println("Number of lifts found: " + liftNodes.getLength());

            String query = "INSERT INTO lifts (lift_type, serial_number, model, quickbooks_list_id, quickbooks_full_name, description) " +
                           "VALUES (?, ?, ?, ?, ?, ?) " +
                           "ON DUPLICATE KEY UPDATE lift_type=?, serial_number=?, model=?, quickbooks_list_id=?, quickbooks_full_name=?, description=?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                for (int i = 0; i < liftNodes.getLength(); i++) {
                    Element liftElement = (Element) liftNodes.item(i);

                    // Extract lift details
                    String liftType = extractModelIndicator(getElementTextContent(liftElement, "FullName"));
                    String serialNumber = extractSerialNumber(getElementTextContent(liftElement, "FullName"));
                    String model = extractModel(getElementTextContent(liftElement, "FullName"));
                    String quickBooksListId = getElementTextContent(liftElement, "ListID");
                    String quickBooksFullName = getElementTextContent(liftElement, "FullName");
                    String description = extractDescription(getElementTextContent(liftElement, "Desc"));

                    // Check for missing/empty values and handle them
                    if (liftType == null || liftType.isEmpty()) liftType = "Unknown";
                    if (serialNumber == null || serialNumber.isEmpty()) serialNumber = null;  // Keep as null for condition check
                    if (model == null || model.isEmpty()) model = "Unknown";
                    if (quickBooksListId == null || quickBooksListId.isEmpty()) quickBooksListId = "Unknown";
                    if (quickBooksFullName == null || quickBooksFullName.isEmpty()) quickBooksFullName = "Unknown";
                    if (description == null || description.isEmpty()) description = "No description available.";

                    // Insert the lift only if it meets the conditions:
                    // 1. Non-null serialNumber and liftType, OR
                    // 2. Non-null liftType and (liftType equals "33RT" or "45B")
                    if ((serialNumber != null && liftType != null) ||
                        (liftType != null && (liftType.equals("3394RT") || liftType.equals("4045R")))) {

                        // Insert or update lift in the database
                        preparedStatement.setString(1, liftType);
                        preparedStatement.setString(2, serialNumber != null ? serialNumber : "N/A");  // Replace null serialNumber for DB insert
                        preparedStatement.setString(3, model);
                        preparedStatement.setString(4, quickBooksListId);
                        preparedStatement.setString(5, quickBooksFullName);
                        preparedStatement.setString(6, description);
                        preparedStatement.setString(7, liftType);
                        preparedStatement.setString(8, serialNumber != null ? serialNumber : "N/A");
                        preparedStatement.setString(9, model);
                        preparedStatement.setString(10, quickBooksListId);
                        preparedStatement.setString(11, quickBooksFullName);
                        preparedStatement.setString(12, description);
                        preparedStatement.executeUpdate();
                        System.out.println("Inserted/Updated lift: " + quickBooksFullName);
                    } else {
                        System.out.println("Lift did not meet insertion criteria: " + quickBooksFullName);
                    }
                }
            }

            updateStatusLabel("Sales items (lifts) updated successfully, including hardcoded lifts.");
        } catch (Exception e) {
            e.printStackTrace();
            updateStatusLabel("Error updating sales items (lifts): " + e.getMessage());
            System.out.println("Error updating sales items (lifts): " + e.getMessage());
        }
    }





    private String extractSerialNumber(String input) {
        if (input != null) {
            Pattern serialPattern = Pattern.compile("^\\d{4,6}");
            Matcher matcher = serialPattern.matcher(input);

            if (matcher.find()) {
                return matcher.group();
            }
        }
        return null;
    }

    private String extractModelIndicator(String input) {
        if (input != null) {
            input = input.trim();
            String[] tokens = input.split("\\s+");
            if (tokens.length > 0) {
                String lastToken = tokens[tokens.length - 1];
                if (!lastToken.matches("^\\d{5,6}$")) {
                    return lastToken;
                }
            }
        }
        return null;
    }

    // Method to map the model based on the serial number and model indicator
    public String extractModel(String input) {
        String serialNumber = extractSerialNumber(input);
        String modelIndicator = extractModelIndicator(input);

        if (modelIndicator != null) {
            // Apply mapping logic
            if (modelIndicator.equalsIgnoreCase("19") || modelIndicator.equalsIgnoreCase("19S")) {
                if (serialNumber != null && serialNumber.startsWith("600")) {
                    return "1932ME"; // If serial number starts with 600, return 1930ME
                } else {
                    return "1930ES"; // Otherwise, return 1930ES
                }
            } else if (modelIndicator.equalsIgnoreCase("26S")) {
                return "2630ES";
            } else if (modelIndicator.equalsIgnoreCase("26")) {
                return "2646ES";
            } else if (modelIndicator.equalsIgnoreCase("32")) {
                return "3246ES";
            } else if (modelIndicator.equalsIgnoreCase("40")) {
                return "4045R";
            } else if (modelIndicator.equalsIgnoreCase("33RT") || modelIndicator.equalsIgnoreCase("45B")) {
                return modelIndicator.toUpperCase(); // Return model indicator as is for 33RT or 45B
            } else if (modelIndicator.equalsIgnoreCase("MAST")) {
                return "1230ES";
            }
        }
        return modelIndicator; // Return model indicator if no mapping matches
    }

    private String extractDescription(String input) {
        if (input != null){
            return input.replace("&apos;", "'");

        }
        return null;
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

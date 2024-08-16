package com.MaxHighReach;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class SMMTaxController extends BaseController {

    @FXML
    private Button runSMMButton;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox loadingIndicator;

    @FXML
    private TextField dateRangeTextField; // Added for user input

    @FXML
    private Label dateRangeLabel;

    @FXML
    private HBox confirmationPrompt; // Reference to the confirmation prompt VBox

    private static final String OUTPUT_DIRECTORY;
    private static final String TEMPLATE_PATH;
    private static final String SCRIPT_PATH;
    private static final String INVOICE_QUERY;
    private static final String SDK_OUTPUT;
    private static final String PREFIX;
    private static final String SRCDIR;

    private static boolean atWork = true; // Set this based on your environment

    static {
        if (atWork) {
            PREFIX = "C:\\Users\\maxhi\\OneDrive\\Documents\\Max High Reach\\MONTH END\\";
            SRCDIR = "..\\..\\Quickbooks\\QBProgram Development\\SMM Filing\\";
        } else {
            PREFIX = "C:\\Users\\jacks\\OneDrive\\Desktop\\Professional\\Max High Reach\\SMM\\";
            SRCDIR = "";
        }
        OUTPUT_DIRECTORY = PREFIX;
        TEMPLATE_PATH = PREFIX + "SMM template 2020.xlsx";
        SCRIPT_PATH = PREFIX + SRCDIR + "scripts\\orchestrate_process.py";
        INVOICE_QUERY = PREFIX + SRCDIR + "scripts\\qbxml_invoice_query.xml";
        SDK_OUTPUT = PREFIX + SRCDIR + "outputs\\QBResponse.xml";
    }

    @Override
    public double getTotalHeight() {
        boolean hardCode = true;
        if (hardCode) {
            return 400; // Hardcoded height
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
    public void handleBack(ActionEvent event) {
        System.out.println("Back button clicked on SMMTaxController");
        try {
            MaxReachPro.goBack("/fxml/smm_tax.fxml"); // Adjusted method call to goBack
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleRunSMM(ActionEvent event) {
        System.out.println("Run SMM Task button clicked");

        // Show loading indicator and reset status label
        Platform.runLater(() -> {
            loadingIndicator.setVisible(true);
            statusLabel.setText("Cloning template file...");
        });

        // Get date range from the input field
        String dateRange = dateRangeTextField.getText();
        if (dateRange.isEmpty()) {
            Platform.runLater(() -> {
                statusLabel.setText("Date range field is empty.");
                loadingIndicator.setVisible(false); // Hide loading indicator
            });
            return; // Exit if date range is not provided
        }

        String normalizedDateRange = normalizeDateRange(dateRange);

        if (normalizedDateRange == null) {
            Platform.runLater(() -> {
                statusLabel.setText("Invalid date range format.");
                loadingIndicator.setVisible(false); // Hide loading indicator
            });
            return; // Exit if the date range format is invalid
        }

        // Create a new file with the format SMM_MM-YY.xlsx
        String newFileName = "SMM_" + normalizedDateRange + ".xlsx";
        String newFilePath = OUTPUT_DIRECTORY + newFileName;

        boolean copySuccess = copyTemplateFile(TEMPLATE_PATH, newFilePath);
        if (!copySuccess) {
            Platform.runLater(() -> {
                statusLabel.setText("Failed to copy template file.");
                loadingIndicator.setVisible(false); // Hide loading indicator
            });
            return; // Exit if the template copy fails
        }

        // Update XML file with normalized date range
        String xmlFile = INVOICE_QUERY;
        boolean updateSuccess = updateXmlWithDateRange(normalizedDateRange, xmlFile);
        if (!updateSuccess) {
            Platform.runLater(() -> {
                statusLabel.setText("Failed to update XML file.");
                loadingIndicator.setVisible(false); // Hide loading indicator
            });
            return; // Exit if XML file update fails
        }

        // Check if the SDK output file exists before proceeding
        File sdkOutputFileObj = new File(SDK_OUTPUT);
        if (!sdkOutputFileObj.exists()) {
            Platform.runLater(() -> {
                statusLabel.setText("SDK output file not found: " + SDK_OUTPUT);
                loadingIndicator.setVisible(false); // Hide loading indicator
            });
            return; // Stop the execution if the SDK output file doesn't exist
        }

        // Show confirmation prompt for the user
        Platform.runLater(() -> {
            confirmationPrompt.setVisible(true);
            statusLabel.setText("Please use the Quickbooks SDK to save the invoices");
        });
    }

    @FXML
    private void handleConfirmationYes(ActionEvent event) {
        // Hide confirmation prompt and proceed with running the Python script
        Platform.runLater(() -> {
            confirmationPrompt.setVisible(false);
            runPythonScript();
        });
    }

    @FXML
    private void handleConfirmationNo(ActionEvent event) {
        // Hide confirmation prompt and update status
        Platform.runLater(() -> {
            confirmationPrompt.setVisible(false);
            statusLabel.setText("Please prepare the invoices file and try again.");
        });
    }

    private void runPythonScript() {
        new Thread(() -> {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("python", SCRIPT_PATH, SDK_OUTPUT);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line); // Print the output of the script
                    }
                }

                int exitCode = process.waitFor();
                Platform.runLater(() -> {
                    if (exitCode == 0) {
                        renameGeneratedFile();
                        statusLabel.setText("SMM Task completed successfully.");
                    } else {
                        statusLabel.setText("SMM Task failed with exit code: " + exitCode);
                    }
                    loadingIndicator.setVisible(false);
                });

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("SMM Task encountered an error.");
                    loadingIndicator.setVisible(false);
                });
            }
        }).start();
    }

    private void renameGeneratedFile() {
        String dateRange = normalizeDateRange(dateRangeTextField.getText());
        if (dateRange == null) {
            statusLabel.setText("Date range is not valid.");
            return;
        }

        String tempFilePath = OUTPUT_DIRECTORY + "Filled_SMM_Temp.xlsx";
        String newFileName = "SMM_" + dateRange + ".xlsx";
        File tempFile = new File(tempFilePath);
        File renamedFile = new File(OUTPUT_DIRECTORY, newFileName);

        if (tempFile.exists()) {
            if (tempFile.renameTo(renamedFile)) {
                System.out.println("File renamed successfully to " + newFileName);
                statusLabel.setText("File renamed successfully.");
            } else {
                System.out.println("File renaming failed.");
                statusLabel.setText("File renaming failed.");
            }
        } else {
            System.out.println("Temporary file does not exist: " + tempFilePath);
            statusLabel.setText("Temporary file does not exist.");
        }
    }


    private String normalizeDateRange(String dateRange) {
        // Define patterns for MM-YY, MM/YY, MM/YYYY
        Pattern[] patterns = {
                Pattern.compile("(\\d{2})-(\\d{2})"),  // MM-YY
                Pattern.compile("(\\d{2})/(\\d{2})"),  // MM/YY
                Pattern.compile("(\\d{2})/(\\d{4})"),   // MM/YYYY
                Pattern.compile("(\\d{2})-(\\d{4})")   // MM-YYYY
        };

        // Try to match the date range with each pattern
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(dateRange);
            if (matcher.matches()) {
                String month = matcher.group(1);
                String year = matcher.group(2);

                // Convert YY to YYYY if needed
                if (year.length() == 2) {
                    year = "20" + year;
                }

                return month + "-" + year;  // Return in MM-YYYY format
            }
        }

        // Return null or handle the invalid date format case
        return null;
    }

    private boolean updateXmlWithDateRange(String dateRange, String xmlFilePath) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(xmlFilePath));

            Element fromDateElement = (Element) doc.getElementsByTagName("FromTxnDate").item(0);
            Element toDateElement = (Element) doc.getElementsByTagName("ToTxnDate").item(0);

            // Extract month and year from dateRange
            String[] parts = dateRange.split("-");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid date range format.");
            }
            String month = parts[0];
            String year = parts[1];

            // Construct LocalDate for the first day of the month
            LocalDate startDate = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), 1);

            // Determine the last day of the month
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

            // Format dates as needed (e.g., YYYY-MM-DD)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String fromDate = startDate.format(formatter);
            String toDate = endDate.format(formatter);

            fromDateElement.setTextContent(fromDate);
            toDateElement.setTextContent(toDate);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(new File(xmlFilePath)));

            return true;

        } catch (DateTimeParseException | IOException | SAXException | ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean copyTemplateFile(String sourcePath, String destinationPath) {
        try {
            Files.copy(Paths.get(sourcePath), Paths.get(destinationPath), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean showConfirmationDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yesButton;
    }
}

package com.MaxHighReach;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
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

    private static final String TEMPLATE_PATH_HOME = "C:\\Users\\jacks\\OneDrive\\Desktop\\Professional\\Max High Reach\\SMM\\example.xlsx";
    private static final String TEMPLATE_PATH_WORK = ""; // Add your work machine path when ready

    @Override
    public double getTotalHeight() {
        boolean hardCode = true;
        if (hardCode) {
            return 400;
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
            MaxReachPro.loadScene("/fxml/home.fxml"); // Hardcoded parent FXML
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

        // Replace template file with a blank template
        replaceTemplateFile(TEMPLATE_PATH_HOME);

        // Show loading indicator and reset status label
        Platform.runLater(() -> {
            statusLabel.setText("");
        });

        // Solicit date range from the user
        Optional<String> dateRangeOpt = solicitDateRange();
        if (!dateRangeOpt.isPresent()) {
            Platform.runLater(() -> {
                statusLabel.setText("Date range input cancelled.");
                loadingIndicator.setVisible(false); // Hide loading indicator
            });
            return; // Exit if date range is not provided
        }

        String dateRange = dateRangeOpt.get();
        String normalizedDateRange = normalizeDateRange(dateRange);

        if (normalizedDateRange == null) {
            Platform.runLater(() -> {
                statusLabel.setText("Invalid date range format.");
                loadingIndicator.setVisible(false); // Hide loading indicator
            });
            return; // Exit if the date range format is invalid
        }

        // Personal machine paths
        String pythonScript = "C:\\Users\\jacks\\OneDrive\\Desktop\\Professional\\Max High Reach\\SMM\\Max-High-Reach\\scripts\\orchestrate_process2.py";
        String xmlFile = "C:\\Users\\jacks\\OneDrive\\Desktop\\Professional\\Max High Reach\\SMM\\Max-High-Reach\\scripts\\qbxml_invoice_query.xml";
        String sdkOutputFile = "C:\\Users\\jacks\\OneDrive\\Desktop\\Professional\\Max High Reach\\SMM\\Max-High-Reach\\outputs\\Filtered_QBResponse2.xml";

        // Commented out SDK path for work machine
        // String sdkExecutable = "C:\\Program Files\\Intuit\\IDN\\QBSDK16.0\\tools\\SDKTest\\SDKTestPlus3.exe";

        // Update XML file with normalized date range
        boolean updateSuccess = updateXmlWithDateRange(normalizedDateRange, xmlFile);
        if (!updateSuccess) {
            Platform.runLater(() -> {
                statusLabel.setText("Failed to update XML file.");
                loadingIndicator.setVisible(false); // Hide loading indicator
            });
            return; // Exit if XML file update fails
        }

        // Check if the XML file exists before proceeding
        File xmlFileObj = new File(xmlFile);
        if (!xmlFileObj.exists()) {
            Platform.runLater(() -> {
                statusLabel.setText("XML file not found: " + xmlFile);
                loadingIndicator.setVisible(false); // Hide loading indicator
            });
            return; // Stop the execution if the file doesn't exist
        }

        // Ensure the SDK output file exists
        File sdkOutputFileObj = new File(sdkOutputFile);
        if (!sdkOutputFileObj.exists()) {
            Platform.runLater(() -> {
                statusLabel.setText("SDK output file not found: " + sdkOutputFile);
                loadingIndicator.setVisible(false); // Hide loading indicator
            });
            return; // Stop the execution if the SDK output file doesn't exist
        }

        // Show confirmation dialog for the user
        boolean fileReady = showConfirmationDialog("Is the output file ready?");
        if (!fileReady) {
            Platform.runLater(() -> {
                statusLabel.setText("Output file not ready.");
                loadingIndicator.setVisible(false); // Hide loading indicator
            });
            return; // Exit if the user does not confirm
        }

        new Thread(() -> {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("python", pythonScript, xmlFile);
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
                        statusLabel.setText("SMM Task completed successfully.");
                    } else {
                        statusLabel.setText("SMM Task failed with exit code: " + exitCode);
                    }
                    loadingIndicator.setVisible(false); // Hide loading indicator
                });

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("SMM Task encountered an error.");
                    loadingIndicator.setVisible(false); // Hide loading indicator
                });
            }
        }).start();
    }

    private String normalizeDateRange(String dateRange) {
        // Define patterns for MM-YY, MM/YY, MM/YYYY
        Pattern[] patterns = {
                Pattern.compile("(\\d{2})-(\\d{2})"),  // MM-YY
                Pattern.compile("(\\d{2})/(\\d{2})"),  // MM/YY
                Pattern.compile("(\\d{2})/(\\d{4})")   // MM/YYYY
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
            String fromDate = parts[1] + "-" + parts[0] + "-01";
            String toDate = parts[1] + "-" + parts[0] + "-31"; // Modify as needed

            fromDateElement.setTextContent(fromDate);
            toDateElement.setTextContent(toDate);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(Paths.get(xmlFilePath).toFile()));

            return true;
        } catch (Exception e) {
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

    private void replaceTemplateFile(String templatePath) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Template File");
        File selectedFile = fileChooser.showOpenDialog(runSMMButton.getScene().getWindow());

        if (selectedFile != null) {
            try {
                Files.copy(selectedFile.toPath(), Paths.get(templatePath), StandardCopyOption.REPLACE_EXISTING);
                Platform.runLater(() -> statusLabel.setText("Template file replaced successfully."));
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> statusLabel.setText("Failed to replace template file."));
            }
        }
    }

    private Optional<String> solicitDateRange() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Date Range Input");
        dialog.setHeaderText("Please enter the date range (MM-YY, MM/YY, MM/YYYY):");
        dialog.setContentText("Date Range:");

        Optional<String> result = dialog.showAndWait();
        return result;
    }

    private boolean replaceTemplateFile(String newTemplatePath) {
        try {
            // Destination path for the template file
            String destinationPath = "C:\\Users\\jacks\\OneDrive\\Desktop\\Professional\\Max High Reach\\SMM\\smm_example.xlsx";

            // Show the file path in the status label
            Platform.runLater(() -> statusLabel.setText("Cloning " + newTemplatePath));

            // Copy the new template file to the destination
            Files.copy(Paths.get(newTemplatePath), Paths.get(destinationPath), StandardCopyOption.REPLACE_EXISTING);

            // Optionally, update status after successful file replacement
            Platform.runLater(() -> statusLabel.setText("Template file cloned successfully."));

            return true;
        } catch (IOException e) {
            e.printStackTrace();

            // Optionally, update status after failure
            Platform.runLater(() -> statusLabel.setText("Failed to clone template file."));

            return false;
        }
    }
}


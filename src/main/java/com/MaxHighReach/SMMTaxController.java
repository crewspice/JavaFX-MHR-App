package com.MaxHighReach;

import com.itextpdf.layout.element.Link;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
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
import java.time.YearMonth;
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
import javax.xml.transform.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class SMMTaxController extends BaseController {

    @FXML
    private Button openSDKButton;

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
    private Hyperlink linkOne;

    @FXML
    private Hyperlink linkTwo;

    @FXML
    private HBox confirmationPrompt;

    
    private static final String OUTPUT_DIRECTORY = PathConfig.OUTPUT_DIRECTORY;
    private static final String TEMPLATE_PATH = PathConfig.TEMPLATE_PATH;
    private static final String SCRIPT_PATH = PathConfig.SCRIPT_PATH;
    private static final String INVOICE_QUERY = PathConfig.INVOICE_QUERY;
    private static final String SDK_OUTPUT = PathConfig.SDK_OUTPUT;
    private static final String SDK_PATH = PathConfig.SDK_PATH;

    public void initialize(){
        super.initialize(null);
        linkOne.setVisible(false);
        linkTwo.setVisible(false);
    }

    private static boolean atWork = true;


    private void updateProgressLabel(String message) {
        Platform.runLater(() -> progressLabel.setText(message));
    }

    private void updateStatusLabel(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    @Override
    public double getTotalHeight() {
        boolean hardCode = true;
        if (hardCode) {
            return 235;
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
            MaxReachPro.goBack("/fxml/smm_tax.fxml");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleOpenSDK(ActionEvent event) {
        statusLabel.setText("Processing. . . ");
        System.out.println("Open SDK Task button clicked");

        // Show the loading indicator and initialize the progressLabel
        Platform.runLater(() -> {
            loadingIndicator.setVisible(true);
            progressLabel.setText("Initializing...");
            statusLabel.setText("Cloning template file...");
        });

        String dateRange = dateRangeTextField.getText();
        if (dateRange.isEmpty()) {
            Platform.runLater(() -> {
                statusLabel.setText("Date range field is empty.");
                progressLabel.setText("");
                loadingIndicator.setVisible(false);
            });
            return;
        }

        String normalizedDateRange = normalizeDateRange(dateRange);

        if (normalizedDateRange == null) {
            Platform.runLater(() -> {
                statusLabel.setText("Invalid date range format.");
                progressLabel.setText("");
                loadingIndicator.setVisible(false);
            });
            return;
        }

      /*  String newFileName = "SMM_" + normalizedDateRange + ".xlsx";
        String newFilePath = OUTPUT_DIRECTORY + newFileName;

        boolean copySuccess = copyTemplateFile(TEMPLATE_PATH, newFilePath);
        if (!copySuccess) {
            Platform.runLater(() -> {
                statusLabel.setText("Failed to copy template file.");
                loadingIndicator.setVisible(false);
            });

            return;
        } */

        boolean updateSuccess = updateXmlWithDateRange(normalizedDateRange, INVOICE_QUERY);
        if (!updateSuccess) {
            Platform.runLater(() -> {
                statusLabel.setText("Failed to update XML file.");
                progressLabel.setText("");
                loadingIndicator.setVisible(false);
            });
            return;
        }

        /* File sdkOutputFileObj = new File(SDK_OUTPUT);
        if (!sdkOutputFileObj.exists()) {
            Platform.runLater(() -> {
                statusLabel.setText("SDK output file not found: " + SDK_OUTPUT);
                progressLabel.setText("");
                loadingIndicator.setVisible(false);
            });
            return;
        } */


        Platform.runLater(() -> {
            runSDKTool();
        });

        // Indicate the next step in progressLabel
        Platform.runLater(() -> {
            progressLabel.setText("Waiting for user confirmation...");
        });

        Platform.runLater(() -> {
            // Hide the loading indicator and update status label
            loadingIndicator.setVisible(false);
            confirmationPrompt.setVisible(true);
            statusLabel.setText("Please use the Quickbooks SDK to save the invoices");
            progressLabel.setText("");
        });
    }


    @FXML
    private void handleConfirmationYes(ActionEvent event) {
        Platform.runLater(() -> {
            // Hide confirmation prompt and "Please use the Quickbooks SDK..." message
            confirmationPrompt.setVisible(false);
            statusLabel.setText("Processing...");
            linkOne.setVisible(false);
            linkTwo.setVisible(false);
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
                            "\n" +
                            "   b. Paste the address in the \"Request File\" field\n" +
                            "   c. Click on the field and use the shortcut Ctrl + V\n" +
                            "2. Open QuickBooks if not already running\n" +
                            "3. Click \"Open Connection\"\n" +
                            "4. Click \"Begin Session\"\n" +
                            "5. Click \"Send XML to Request Processor\"\n" +
                            "6. Wait for invoices to be received\n" +
                            "7. Click \"View Output\"\n" +
                            "8. Save that file \n" +
                            "   \n" +
                            "   b. Click on the output file and press Ctrl + S\n" +
                            "   c. Press Ctrl + V to paste in the save address\n" +
                            "   d. Double click on QBResponse.xml to overwrite\n" +
                            "Finished!   -    Click Yes below"
            );
            progressLabel.setVisible(true);
            linkOne.setVisible(true);
            linkTwo.setVisible(true);
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
                    linkOne.setVisible(false);
                    linkTwo.setVisible(false);
                    loadingIndicator.setVisible(false);
                });

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Error running SDK Tool.");
                    progressLabel.setText("");
                    linkOne.setVisible(false);
                    linkTwo.setVisible(false);
                    loadingIndicator.setVisible(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleCopyLink1(MouseEvent event) {
        String pathToCopy = "C:\\Users\\maxhi\\OneDrive\\Documents\\MaxReachPro\\SMM Filing\\scripts\\invoice_query.xml";

        // Get the system clipboard
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(pathToCopy);

        // Set the clipboard content
        clipboard.setContent(content);

        // Optionally, you can provide feedback to the user
        statusLabel.setText("File path copied to clipboard: " + pathToCopy);
    }

    @FXML
    private void handleCopyLink2(MouseEvent event) {
        // Path for QuickBooks response file
        String qbResponsePath = "C:\\Users\\maxhi\\OneDrive\\Documents\\MaxReachPro\\SMM Filing\\outputs";

        // Get the system clipboard
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(qbResponsePath);

        // Set the clipboard content
        clipboard.setContent(content);

        // Provide feedback to the user
        updateStatusLabel("QBResponse file path copied to clipboard: " + qbResponsePath);
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
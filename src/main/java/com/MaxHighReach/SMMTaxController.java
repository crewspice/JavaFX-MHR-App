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
    private Button runSMMButton;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox loadingIndicator;

    @FXML
    private TextField dateRangeTextField;

    @FXML
    private Label dateRangeLabel;

    @FXML
    private HBox confirmationPrompt;

    private static final String OUTPUT_DIRECTORY;
    private static final String TEMPLATE_PATH;
    private static final String SCRIPT_PATH;
    private static final String INVOICE_QUERY;
    private static final String SDK_OUTPUT;
    private static final String PREFIX;
    private static final String SRCDIR;

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
        INVOICE_QUERY = PREFIX + SRCDIR + "scripts\\qbxml_invoice_query.xml";
        SDK_OUTPUT = PREFIX + SRCDIR + "outputs\\QBResponse.xml";
    }

    @Override
    public double getTotalHeight() {
        boolean hardCode = true;
        if (hardCode) {
            return 380;
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
    private void handleRunSMM(ActionEvent event) {
        System.out.println("Run SMM Task button clicked");

        Platform.runLater(() -> {
            loadingIndicator.setVisible(true);
            statusLabel.setText("Cloning template file...");
        });

        String dateRange = dateRangeTextField.getText();
        if (dateRange.isEmpty()) {
            Platform.runLater(() -> {
                statusLabel.setText("Date range field is empty.");
                loadingIndicator.setVisible(false);
            });
            return;
        }

        String normalizedDateRange = normalizeDateRange(dateRange);

        if (normalizedDateRange == null) {
            Platform.runLater(() -> {
                statusLabel.setText("Invalid date range format.");
                loadingIndicator.setVisible(false);
            });
            return;
        }

        String newFileName = "SMM_" + normalizedDateRange + ".xlsx";
        String newFilePath = OUTPUT_DIRECTORY + newFileName;

        boolean copySuccess = copyTemplateFile(TEMPLATE_PATH, newFilePath);
        if (!copySuccess) {
            Platform.runLater(() -> {
                statusLabel.setText("Failed to copy template file.");
                loadingIndicator.setVisible(false);
            });
            return;
        }

        boolean updateSuccess = updateXmlWithDateRange(normalizedDateRange, INVOICE_QUERY);
        if (!updateSuccess) {
            Platform.runLater(() -> {
                statusLabel.setText("Failed to update XML file.");
                loadingIndicator.setVisible(false);
            });
            return;
        }

        File sdkOutputFileObj = new File(SDK_OUTPUT);
        if (!sdkOutputFileObj.exists()) {
            Platform.runLater(() -> {
                statusLabel.setText("SDK output file not found: " + SDK_OUTPUT);
                loadingIndicator.setVisible(false);
            });
            return;
        }

        Platform.runLater(() -> {
            // Hide "Running" tag
            loadingIndicator.setVisible(false);
            // Show confirmation prompt
            confirmationPrompt.setVisible(true);
            statusLabel.setText("Please use the Quickbooks SDK to save the invoices");
        });
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
        new Thread(() -> {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("python", SCRIPT_PATH, SDK_OUTPUT);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }

                int exitCode = process.waitFor();
                Platform.runLater(() -> {
                    if (exitCode == 0) {
                        renameGeneratedFile();
                        statusLabel.setText("SMM file completed.");
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
            Platform.runLater(() -> statusLabel.setText("Date range is not valid."));
            return;
        }

        String tempFilePath = OUTPUT_DIRECTORY + "Filled_SMM_Temp.xlsx";
        String newFileName = "SMM_" + dateRange + ".xlsx";
        File tempFile = new File(tempFilePath);
        File renamedFile = new File(OUTPUT_DIRECTORY, newFileName);

        int count = 1;
        while (renamedFile.exists()) {
            newFileName = "SMM_" + dateRange + "(" + count + ").xlsx";
            renamedFile = new File(OUTPUT_DIRECTORY, newFileName);
            count++;
        }

        if (tempFile.exists()) {
            if (tempFile.renameTo(renamedFile)) {
                Platform.runLater(() -> statusLabel.setText("SMM file completed."));
            } else {
                Platform.runLater(() -> statusLabel.setText("File renaming failed."));
            }
        } else {
            Platform.runLater(() -> statusLabel.setText("Temporary file does not exist."));
        }
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

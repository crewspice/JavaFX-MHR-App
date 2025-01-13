package com.MaxHighReach;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream; import java.io.IOException;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.ScrollPane;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;

import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

public class UtilizationController extends BaseController {

    @FXML
    private AnchorPane anchorPane;
    private VBox currentLayout;
    @FXML
    private ScrollPane scrollPane;
    private GraphicsContext gc;
    private Canvas utilizationCanvas;
    @FXML
    private HBox monthToggler;
    List<Node> renderedNodes = new ArrayList<>();

    private int numColumns;
    private Month currentMonth;
    private int currentYear;
    private int numBusinessDays;

    // Global variable to hold the list of lifts
    private List<Lift> lifts = MaxReachPro.getLifts();
    private List<Lift> validLifts = new ArrayList<>();

    // List to hold CustomerRental data
    private List<CustomerRental> customerRentals;
    private Map<Integer, Integer> rentalDayCounts;
    private Map<String, Integer> liftTypeCounts = new HashMap<>();
    private Map<String, Integer> serialCounts = new HashMap<>();
    private Map<String, Integer> typeHeaderCoords = new HashMap<>();
    private Map<String, Integer> liftTypeSerialCounts = new HashMap<>();

    private Timeline[] glowTimelines = new Timeline[2];
    private boolean timelineFlagger = false;

    @FXML
    public void initialize() {
        sortLifts();
        rentalDayCounts = new HashMap<>();
        loadCustomerRentalData();


        LocalDate now = LocalDate.now();
        currentMonth = now.getMonth();
        currentYear = now.getYear();

        animateScrollBars(scrollPane);
        renderPanes(currentYear, currentMonth);
    }

    @FXML
    private void handleMonthBack() {
        currentMonth = currentMonth.minus(1);
        if (currentMonth == Month.DECEMBER) {
            currentYear --;
        }
        renderPanes(currentYear, currentMonth);
    }

    @FXML
    private void handleMonthForward() {
        currentMonth = currentMonth.plus(1);
        if (currentMonth == Month.JANUARY) {
            currentYear ++;
        }
        renderPanes(currentYear, currentMonth);
    }

    @FXML
    public void handleExportReport() throws Exception {
        String filePath = getFilePath();


        PdfWriter writer = new PdfWriter(new File(filePath));
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);


        float sizeScalar = 0.75f;


        // Set the document margins
        document.setMargins(5, 5, 5, 5); // Left, top, right, bottom margins


        // Draw the Top Pane with downscaled text size
        document.add(new Paragraph(String.format("Utilization Report: %s %d",
                currentMonth.getDisplayName(TextStyle.FULL, Locale.ENGLISH), currentYear))
                .setFontSize(16 * sizeScalar) // Downscale font size by 15%
                .setBold());


        // Create a Table for the Data
        int totalColumns = numColumns + 3;
        Table table = new Table(UnitValue.createPercentArray(totalColumns));
        table.setWidth(UnitValue.createPercentValue(100));


        // Add headers with downscaled text size
        table.addCell(new Cell().add(new Paragraph("Model"))
                .setFontSize(12 * sizeScalar));
        table.addCell(new Cell().add(new Paragraph("Serial"))
                .setFontSize(12 * sizeScalar));


        for (int col = 1; col <= numColumns; col++) {
            table.addCell(new Cell().add(new Paragraph(String.valueOf(col)))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12 * sizeScalar));
        }


        // Add the "Days on Rent" header
        table.addCell(new Cell().add(new Paragraph("Sum"))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(12 * sizeScalar));


        // Add the row for total lifts on rent per day
        table.addCell(new Cell(1, 2).add(new Paragraph("Total on Rent"))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(12 * sizeScalar)); // Spanning "Model" and "Serial"


        for (int col = 1; col <= numColumns; col++) {
            int count = rentalDayCounts.getOrDefault(col, 0);
            String countStr = String.valueOf(count);
            Paragraph verticalCount = new Paragraph();
            for (char digit : countStr.toCharArray()) {
                verticalCount.add(digit + "\n"); // Add each digit vertically
            }
            table.addCell(new Cell().add(verticalCount)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12 * sizeScalar));
        }


        // Add an empty cell for alignment under "Sum"
        table.addCell(new Cell().add(new Paragraph("")).setFontSize(12 * sizeScalar));


        // Continue populating rows for lifts (existing logic remains unchanged)...


        String previousLiftType = null;


        for (Lift lift : validLifts) {
            String currentLiftType = lift.getLiftType();


            if (previousLiftType != null && !previousLiftType.equals(currentLiftType)) {
               int liftTypeCount = liftTypeCounts.getOrDefault(previousLiftType, 0);
               int liftTypeSerialCount = liftTypeSerialCounts.getOrDefault(previousLiftType, 0);


               if (liftTypeSerialCount > 0) {
                   System.out.println("lifTypeCount: " + liftTypeCount + ", liftTypeSerialCount: "
                       + liftTypeSerialCount + ", numBusinessDays: " + numBusinessDays);


                   // Cast to double to ensure floating-point division
                   double utilPercent = ((double) liftTypeCount * 100) / (liftTypeSerialCount * numBusinessDays);
                   utilPercent = Math.round(utilPercent * 10.0) / 10.0;


                   table.addCell(new Cell(1, totalColumns - 1).add(new Paragraph(
                           previousLiftType + ": "
                                   + utilPercent + "%"))
                           .setTextAlignment(TextAlignment.CENTER)
                           .setFontSize(12 * sizeScalar));
                   table.addCell(new Cell().add(new Paragraph("")));
               }
            }



            String model = lift.getModel();
            table.addCell(new Cell().add(new Paragraph(model))
                    .setFontSize(12 * sizeScalar));


            String serialNumber = lift.getSerialNumber();
            table.addCell(new Cell().add(new Paragraph(serialNumber))
                    .setFontSize(12 * sizeScalar));


            for (int col = 1; col <= numColumns; col++) {
                LocalDate day = LocalDate.of(currentYear, currentMonth, col);
                boolean isOnRent = customerRentals.stream()
                        .filter(rental -> rental.getSerialNumber().equals(serialNumber))
                        .anyMatch(rental -> (LocalDate.parse(rental.getDeliveryDate()).isBefore(day) || LocalDate.parse(rental.getDeliveryDate()).isEqual(day)) &&
                                (rental.getCallOffDate() == null || LocalDate.parse(rental.getCallOffDate()).isAfter(day) || LocalDate.parse(rental.getCallOffDate()).isEqual(day)));
                boolean isBusinessDay = (day.getDayOfWeek().getValue() <= 5 && !Config.COMPANY_HOLIDAYS.contains(day));
                DeviceRgb color = isOnRent ? new DeviceRgb(0, 255, 0) : new DeviceRgb(255, 0, 0);
                color = isBusinessDay ? color : new DeviceRgb(255, 255, 255);


                Cell dayCell = new Cell().setBackgroundColor(color);
                dayCell.setMinWidth(11 * sizeScalar);
                table.addCell(dayCell);
            }


            int daysOnRent = serialCounts.getOrDefault(serialNumber, 0);
            table.addCell(new Cell().add(new Paragraph(String.valueOf(daysOnRent)))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12 * sizeScalar));


            previousLiftType = currentLiftType;
        }


        if (previousLiftType != null) {
            int liftTypeCount = liftTypeCounts.getOrDefault(previousLiftType, 0);
            int liftTypeSerialCount = liftTypeSerialCounts.getOrDefault(previousLiftType, 0);


            if (liftTypeSerialCount > 0) {
                double utilPercent = (liftTypeCount * 100) / (liftTypeSerialCount * numBusinessDays);
                utilPercent = Math.round(utilPercent * 10.0) / 10.0;


                table.addCell(new Cell(1, totalColumns - 1).add(new Paragraph(
                        previousLiftType + ": "
                                + utilPercent + "%"))
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontSize(12 * sizeScalar));
                table.addCell(new Cell().add(new Paragraph("")));
            }
        }


        document.add(table);
        document.close();
    }



    private void renderPanes(int year, Month month) {
        anchorPane.getChildren().remove(currentLayout);
        currentLayout = new VBox();
        currentLayout.setPrefSize(anchorPane.getPrefWidth(), anchorPane.getPrefHeight());
        currentLayout.setLayoutX(6);
        currentLayout.setLayoutY(36);
        currentLayout.setAlignment(Pos.TOP_LEFT);
        currentLayout.setPadding(new Insets(0, 0, -20, 0));
        anchorPane.getChildren().add(currentLayout);


        // Create the top pane
        AnchorPane topPane = new AnchorPane();
        topPane.setPrefHeight(50);
        drawTopPane(topPane, year, month);
        renderedNodes.add(topPane);

        // Create the scrollable pane
        utilizationCanvas = new Canvas(293, 1708); // Adjust height dynamically
        gc = utilizationCanvas.getGraphicsContext2D();
        numColumns = month.length(LocalDate.of(year, month, 1).isLeapYear());
        drawUtilization(gc, year, month);
        addAverages();

        scrollPane = new ScrollPane(utilizationCanvas);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(309, 511);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        scrollPane.setTranslateY(-31);
        renderedNodes.add(scrollPane);

        // Create the bottom pane
        AnchorPane bottomPane = new AnchorPane();
        bottomPane.setPrefHeight(50);
        drawBottomPane(bottomPane, year, month);
        bottomPane.setTranslateY(-31);
        renderedNodes.add(bottomPane);

        // Add all panes to the layout
        currentLayout.getChildren().addAll(topPane, scrollPane, bottomPane);


        animateScrollBars(scrollPane);
    }


    private void loadCustomerRentalData() {
        // SQL query to get customer rental data
        String query = """
            SELECT ro.customer_id, c.customer_name, ri.item_delivery_date, ri.item_call_off_date, ro.po_number,
                   ordered_contacts.first_name AS ordered_contact_name, ordered_contacts.phone_number AS ordered_contact_phone,
                   ri.auto_term, ro.site_name, ro.street_address, ro.city, ri.rental_item_id,
                   l.serial_number, ro.single_item_order, ri.rental_order_id,
                   site_contacts.first_name AS site_contact_name, site_contacts.phone_number AS site_contact_phone
            FROM customers c
            JOIN rental_orders ro ON c.customer_id = ro.customer_id  -- Ensure this is correct
            JOIN rental_items ri ON ro.rental_order_id = ri.rental_order_id
            JOIN lifts l ON ri.lift_id = l.lift_id
            LEFT JOIN contacts AS ordered_contacts ON ri.ordered_contact_id = ordered_contacts.contact_id
            LEFT JOIN contacts AS site_contacts ON ri.site_contact_id = site_contacts.contact_id
        """;

        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet rs = preparedStatement.executeQuery()) {

            customerRentals = new ArrayList<>();

            // Process each result row
            while (rs.next()) {
                String customerId = rs.getString("customer_id");
                String name = rs.getString("customer_name");
                String deliveryDate = rs.getString("item_delivery_date");
                String callOffDate = rs.getString("item_call_off_date");
                String poNumber = rs.getString("po_number");
                String orderedByName = rs.getString("ordered_contact_name");
                String orderedByPhone = rs.getString("ordered_contact_phone");
                boolean autoTerm = rs.getBoolean("auto_term");
                String addressBlockOne = rs.getString("site_name");
                String addressBlockTwo = rs.getString("street_address");
                String addressBlockThree = rs.getString("city");
                int rentalItemId = rs.getInt("rental_item_id");
                String serialNumber = rs.getString("serial_number");
                boolean singleItemOrder = rs.getBoolean("single_item_order");
                int rentalOrderId = rs.getInt("rental_order_id");
                String siteContactName = rs.getString("site_contact_name");
                String siteContactPhone = rs.getString("site_contact_phone");

                // Create CustomerRental objects for each row and add them to the list
                customerRentals.add(new CustomerRental(customerId, name, deliveryDate, callOffDate, poNumber,
                        orderedByName, orderedByPhone, autoTerm, addressBlockOne, addressBlockTwo,
                        addressBlockThree, rentalItemId, serialNumber, singleItemOrder, rentalOrderId,
                        siteContactName, siteContactPhone));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading customer rental data", e);
        }
    }

    private void drawUtilization(@NotNull GraphicsContext gc, int year, Month month) {
        // Define circle size and margins
        final double circleSize = 7;  // Smaller circle size to fit all
        final double margin = 1.5;  // Margin between circles
        final double labelMargin = 2;  // Space for the row and column labels
        final double bufferSize = 5;  // Additional buffer space after every 5 rows
        final double sectionLabelMargin = 20;

        // --------------------- Write in the row labels (serial numbers) -------------------------
        // Get valid lifts (those with serial numbers)
        validLifts = lifts.stream()
            .filter(lift -> lift.getSerialNumber() != null && !lift.getSerialNumber().trim().isEmpty())
            .toList();
        int numRows = validLifts.size();

        // Adjust starting x-coordinate for labels
        double labelOffsetX = 52;

        // Set the font size for column labels (Dates)
        gc.setFont(Font.font("Yu Gothic UI", 10));  // Use "Yu Gothic UI" font for vertical labels
        gc.setFill(Color.BLACK);

        // Translate drawing to make space for row labels
        gc.translate(0, 20);

        String currentLiftType = null;
        double rowLabelOffsetY = -29;
        int sectionRowCount = 0; // Track rows within the current section

        for (int i = 0; i < numRows; i++) {
            Lift lift = validLifts.get(i);

            // Check if the lift type changes (new section starts)
            if (!lift.getLiftType().equals(currentLiftType)) {
                currentLiftType = lift.getLiftType();

                double characterNumberOffset = currentLiftType.length() * -2.5;
                // Draw the section label
                gc.setFont(Font.font("Lucida Handwriting", 11));
                gc.setFill(Color.BLACK);
                gc.fillText(currentLiftType, 12 + characterNumberOffset, rowLabelOffsetY + labelMargin +
                        (i + 1) * (circleSize + margin) + 9);

                // Draw a horizontal line below the lift type
                double lineStartX = 36; // Starting X position
                double lineStartY = rowLabelOffsetY + labelMargin +
                        (i + 1) * (circleSize + margin) + 9; // Position the line just below the text
                double lineLength = 220;
                gc.setStroke(Color.BLACK); // Line color
                gc.setLineWidth(.5); // Line thickness
                gc.strokeLine(lineStartX, lineStartY - 3, lineStartX + lineLength, lineStartY - 3);

                // Draw the additional string after the horizontal line
              //  String additionalText = "Average days on rent:";
              //  gc.setFont(Font.font("Lucida Handwriting", 10));
              //  gc.fillText(additionalText, lineStartX + lineLength + 6, lineStartY); // Position the text slightly below the line
                typeHeaderCoords.put(currentLiftType, (int) lineStartY);

                // Reset the row count for the new section
                sectionRowCount = 0;


                // Add margin after section label
                rowLabelOffsetY += sectionLabelMargin;
            }


            // Increment section-specific row count
            sectionRowCount++;

            // Draw the row label for the lift's serial number
            gc.setFont(Font.font("Arial", 10));
            gc.setFill(Color.BLACK);

            double y = rowLabelOffsetY + labelMargin + (i + 1) * (circleSize + margin);
            gc.fillText(lift.getSerialNumber(), 0, y);


            // Add buffer space every 5 rows within the section
            if (sectionRowCount % 5 == 0) {
                if (i + 1 < validLifts.size()) {
                    if (lift.getLiftType().equals(validLifts.get(i + 1).getLiftType())) {
                        rowLabelOffsetY += bufferSize;
                    }
                }
            }


        }

        // Translate to the right for the utilization circles
        gc.translate(labelOffsetX, -3);

        currentLiftType = null;
        rowLabelOffsetY = -29;
        sectionRowCount = 0; // Track rows within the current section

        // Initialize rentalCounts map for all columns before the loops
        for (int col = 0; col < numColumns; col++) {
            rentalDayCounts.put(col + 1, 0);
        }



        // --------------------------- Draw the circles --------------------------------
        for (int row = 0; row < numRows; row++) {
            Lift lift = validLifts.get(row);

            String serialNumber = lift.getSerialNumber();
            int skippedColumns = 0;

            if (!lift.getLiftType().equals(currentLiftType)) {
                currentLiftType = lift.getLiftType();
                rowLabelOffsetY += sectionLabelMargin;
                sectionRowCount = 0;
            }

            sectionRowCount ++;
            int rentedCount = 0;

            for (int col = 0; col < numColumns; col++) {
                LocalDate day = LocalDate.of(year, month, col + 1);

                // Skip weekends and holidays
                if (day.getDayOfWeek().getValue() > 5 || Config.COMPANY_HOLIDAYS.contains(day)) {
                    skippedColumns++;
                    continue;
                }

                // Determine if the lift is on rent on this day
                boolean isOnRent = customerRentals.stream()
                    .filter(rental -> rental.getSerialNumber().equals(serialNumber)) // Use .get() here
                    .anyMatch(rental -> (LocalDate.parse(rental.getDeliveryDate()).isBefore(day) || LocalDate.parse(rental.getDeliveryDate()).isEqual(day)) &&
                            (rental.getCallOffDate() == null || LocalDate.parse(rental.getCallOffDate()).isAfter(day) || LocalDate.parse(rental.getCallOffDate()).isEqual(day)));

                if (isOnRent) {
                    rentalDayCounts.put(col + 1, rentalDayCounts.get(col + 1) + 1);
                    liftTypeCounts.put(currentLiftType, liftTypeCounts.get(currentLiftType) + 1);
                    serialCounts.put(serialNumber, serialCounts.get(serialNumber) + 1);
                }


                // Set the color based on rental status
                gc.setFill(isOnRent ? Color.GREEN : Color.RED);

                // Calculate position for circles
                double x = labelMargin + col * (circleSize + margin) - 17 + (skippedColumns * -1);  // X position for circles
                double y = labelMargin + (row + 1) * (circleSize + margin) + rowLabelOffsetY - 3;  // Y position for circles

                // Draw the circle at each intersection
                gc.fillOval(x, y, circleSize, circleSize);

            }
            numBusinessDays = numColumns - skippedColumns;
            if (sectionRowCount % 5 == 0) {
                if (validLifts.size() >= row + 2) {
                    if (lift.getLiftType().equals(validLifts.get(row + 1).getLiftType())) {
                        rowLabelOffsetY += bufferSize;
                    }
                }
            }
        }

    }

    private void addAverages() {
        // Set the font and style for drawing text
        gc.setFont(Font.font("Lucida Handwriting", 10));
        gc.setFill(Color.BLACK);

        // Iterate over each lift type
        for (String liftType : liftTypeCounts.keySet()) {
            // Get the total days on rent for this lift type
            double totalDaysOnRent = liftTypeCounts.get(liftType);

            // Get the number of lifts for this lift type
            double numberOfLifts = liftTypeSerialCounts.get(liftType);

            double utilPercent = Math.round(((totalDaysOnRent * 100.0) / (numberOfLifts * numBusinessDays)) * 10.0) / 10.0;
            utilPercent = Math.round(utilPercent * 10.0) / 10.0;
            String utilPercentString = utilPercent + "%";

            // Get the Y-coordinate for this lift type's header
            double yCoord = typeHeaderCoords.getOrDefault(liftType, 0);

            // Draw the average on the canvas
            gc.fillText(utilPercentString, 206, yCoord + 3.5);
        }
        System.out.println(typeHeaderCoords);
    }

    private void sortLifts() {
        // Clear maps to ensure they only reflect current lift types
        liftTypeCounts.clear();
        serialCounts.clear();
        typeHeaderCoords.clear();
        liftTypeSerialCounts.clear();

        // Assign a unique sorting index to each lift type
        for (Lift lift : lifts) {
            String liftType = lift.getLiftType();
            if (!liftTypeCounts.containsKey(liftType)) {
                liftTypeCounts.put(liftType, 0); // Assign unique index for sorting
            }
            serialCounts.put(lift.getSerialNumber(), 0);
            // Initialize serial count for this lift type if not already done
            liftTypeSerialCounts.putIfAbsent(liftType, 0);
        }

        // Use a Set to track unique serial numbers per lift type
        Map<String, Set<String>> uniqueSerialsPerType = new HashMap<>();
        for (String liftType : liftTypeCounts.keySet()) {
            uniqueSerialsPerType.put(liftType, new HashSet<>());
        }

        // Populate the unique serial counts
        for (Lift lift : lifts) {
            String liftType = lift.getLiftType();
            String serialNumber = lift.getSerialNumber();
            if (serialNumber != null && !serialNumber.trim().isEmpty()) {
                Set<String> serialSet = uniqueSerialsPerType.get(liftType);
                if (serialSet != null && serialSet.add(serialNumber)) {
                    // Increment the count only if the serial number is added for the first time
                    liftTypeSerialCounts.put(liftType, liftTypeSerialCounts.get(liftType) + 1);
                }
            }
        }

        // Sort the lifts using a custom comparator
        Collections.sort(lifts, new Comparator<Lift>() {
            @Override
            public int compare(Lift lift1, Lift lift2) {
                // Get the lift type
                String liftType1 = lift1.getLiftType();
                String liftType2 = lift2.getLiftType();

                // Get the index of the lift type from ASCENDING_LIFT_TYPES
                int typeIndex1 = Arrays.asList(Config.ASCENDING_LIFT_TYPES).indexOf(liftType1);
                int typeIndex2 = Arrays.asList(Config.ASCENDING_LIFT_TYPES).indexOf(liftType2);

                // First compare by the lift type index based on ASCENDING_LIFT_TYPES order
                if (typeIndex1 != typeIndex2) {
                    return Integer.compare(typeIndex1, typeIndex2);
                }

                // If lift types are the same, compare by serial number numerically
                try {
                    int serialNumber1 = Integer.parseInt(lift1.getSerialNumber());
                    int serialNumber2 = Integer.parseInt(lift2.getSerialNumber());
                    return Integer.compare(serialNumber1, serialNumber2);
                } catch (NumberFormatException e) {
                    // Fallback to lexicographical comparison if serial numbers are not numeric
                    return lift1.getSerialNumber().compareTo(lift2.getSerialNumber());
                }
            }
        });

    }


   // Helper method to get lift type index from Config.ASCENDING_LIFT_TYPES
   private static int getLiftTypeIndex(String liftType) {
       for (int i = 0; i < Config.ASCENDING_LIFT_TYPES.length; i++) {
           if (Config.ASCENDING_LIFT_TYPES[i].equals(liftType)) {
               return i;
           }
       }
       return Integer.MAX_VALUE; // Return the highest index if the lift type is unknown
   }


    private void animateScrollBars(ScrollPane scrollPane) {
        Platform.runLater(() -> {
            int i = 0;
            // Assuming your scroll bars are part of the scrollPane's skin
            for (Node node : scrollPane.lookupAll(".scroll-bar")) {
                if (node instanceof ScrollBar) {
                    // Create a glow effect for the scroll bar
                    DropShadow glowEffect = new DropShadow();
                    glowEffect.setRadius(10);
                    glowEffect.setSpread(0.5);
                    node.setEffect(glowEffect);

                    // Create a glow animation
                    glowTimelines[i] = new Timeline(
                            new KeyFrame(Duration.ZERO,
                                    new KeyValue(glowEffect.colorProperty(),
                                            timelineFlagger ? Color.web("#FFDEAD", 0.5) : Color.web("#FFDEAD", 0.5))
                            ),
                            new KeyFrame(Duration.seconds(1),
                                    new KeyValue(glowEffect.colorProperty(),
                                            timelineFlagger ? Color.web("#000000", 0.0) : Color.web("#FF7F00", 1.0))
                            )
                    );

                    glowTimelines[i].setCycleCount(Timeline.INDEFINITE);
                    glowTimelines[i].setAutoReverse(true);
                    glowTimelines[i].play();
                }
                i++;
            }
        });
    }

    private void drawTopPane(AnchorPane pane, int year, Month month) {
        Canvas topCanvas = new Canvas(290, 50); // Adjust dimensions as needed
        GraphicsContext gc = topCanvas.getGraphicsContext2D();

        numColumns = month.length(LocalDate.of(year, month, 1).isLeapYear());

        // Draw month and year labels
        String monthLabel = month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH);
        String yearLabel = String.format(" '%02d", year % 100);

        // Set font for bold text
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.setFill(Color.BLACK);

        // Add shadow effect (orange underglow)
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.ORANGE); // Shadow color
        shadow.setOffsetX(2); // Horizontal offset
        shadow.setOffsetY(2); // Vertical offset
        shadow.setRadius(3); // Blur radius for the shadow

        gc.setEffect(shadow); // Apply shadow effect
        gc.fillText(monthLabel, 5, 10);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.setFill(Color.BLACK);
        gc.fillText(yearLabel, 13, 20);
        gc.setEffect(null);

        // Draw column labels
        gc.setFont(Font.font("Yu Gothic UI", 10));
        int skippedColumns = 0;
        for (int col = 0; col < numColumns; col++) {
            LocalDate day = LocalDate.of(year, month, col + 1);
            if (day.getDayOfWeek().getValue() > 5 || Config.COMPANY_HOLIDAYS.contains(day)) {
                skippedColumns++;
                continue;
            }
            String colString = String.valueOf(col + 1);
            String col1 = colString.substring(0, 1);
            String col2 = colString.length() > 1 ? colString.substring(1, 2) : "";

            double x = 54 + col * (7 + 1.5) + (skippedColumns * -1); // Adjust x-coordinate
            double y = 20;

            gc.save();
            gc.translate(x, y);
            if (col2.isEmpty()) {
                gc.fillText(col1, -15, -6);
            } else {
                gc.fillText(col1, -15, -10);
                gc.fillText(col2, -15, -1);
            }
            gc.restore();
        }

        pane.getChildren().add(topCanvas);
    }

    private void drawBottomPane(AnchorPane pane, int year, Month month) {
        Canvas bottomCanvas = new Canvas(293, 50);
        GraphicsContext gc = bottomCanvas.getGraphicsContext2D();

        numColumns = month.length(LocalDate.of(year, month, 1).isLeapYear());

        // Draw column labels
        gc.setFont(Font.font("Yu Gothic UI", 10));
        int skippedColumns = 0;
        for (int col = 0; col < numColumns; col++) {
            LocalDate day = LocalDate.of(year, month, col + 1);
            if (day.getDayOfWeek().getValue() > 5 || Config.COMPANY_HOLIDAYS.contains(day)) {
                skippedColumns++;
                continue;
            }
            String colString = String.valueOf(rentalDayCounts.getOrDefault(col + 1, 0));
            String col1 = colString.substring(0, 1);
            String col2 = colString.length() > 1 ? colString.substring(1, 2) : "";
            String col3 = colString.length() > 2 ? colString.substring(2, 3) : "";
            double x = 54 + col * (7 + 1.5) + (skippedColumns * -1); // Adjust x-coordinate
            double y = 20;

            gc.save();
            gc.translate(x, y);
            gc.fillText(col1, -14, -13);
            gc.fillText(col2, -14, -4);
            if (!col3.isEmpty()) {
                gc.fillText(col3, -14, 5);
            }
            gc.restore();
        }
        String totalsLabel = new String("Totals");
        gc.setFont(Font.font("Arial", 10));
        gc.setFill(Color.BLACK);
        gc.fillText(totalsLabel, 5, 7);
        gc.setEffect(null);

        pane.getChildren().add(bottomCanvas);
    }

    private String getFilePath() {
        String directoryPath = "C:/Users/maxhi/OneDrive/Documents/Max High Reach/MONTH END";

        // Format the file name as "Utilz mm-yy.pdf"
        String formattedFileName = String.format("Utilz %s-%d.pdf",
            currentMonth.getDisplayName(TextStyle.SHORT, Locale.ENGLISH), currentYear);


        // Ensure the directory path ends with a file separator
        if (!directoryPath.endsWith(File.separator)) {
            directoryPath += File.separator;
        }


        // Combine directory path and formatted file name
        return directoryPath + formattedFileName;
    }


    @FXML
    public void handleBack() {
        try {
            MaxReachPro.goBack("/fxml/utilization.fxml");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double getTotalHeight() {
        return 155;
    }
}




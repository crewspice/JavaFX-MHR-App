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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;

import javafx.animation.Animation;
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
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.ScrollPane;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.sql.Date;

import javafx.scene.text.FontWeight;
import javafx.util.Duration;
// import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class UtilizationController extends BaseController {

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Rectangle dragArea;
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
    private int rowLabelOffsetY;

    // Global variable to hold the list of lifts
    private List<Lift> lifts = MaxReachPro.getLifts();
    private List<Lift> validLifts = new ArrayList<>();

    // List to hold CustomerRental data
    private List<Rental> customerRentals;
    
    private Map<Integer, Integer> rentalDayCounts;
    private Map<String, Integer> liftTypeCounts = new HashMap<>();
    private Map<String, Integer> serialCounts = new HashMap<>();
    private Map<String, Double> typeHeaderCoords = new HashMap<>();
    private Map<String, Integer> liftTypeSerialCounts = new HashMap<>();

    private Timeline[] glowTimelines = new Timeline[2];
    private boolean timelineFlagger = false;
    private Map<Rental, List<Circle>> rentalCircles = new HashMap<>();
    private final Map<Rental, Timeline> rentalAnimations = new HashMap<>();


    @FXML
    public void initialize() {
        super.initialize(dragArea);
        sortLifts();
        rentalDayCounts = new HashMap<>();

        LocalDate now = LocalDate.now();
        currentMonth = now.getMonth();
        currentYear = now.getYear();

        loadCustomerRentalData();
        animateScrollBars(scrollPane);
        renderPanes(currentYear, currentMonth);
    }

    private void resetCounts() {
        serialCounts.replaceAll((k, v) -> 0);
        liftTypeCounts.replaceAll((k, v) -> 0);
    }
    
    @FXML
    private void handleMonthBack() {
        currentMonth = currentMonth.minus(1);
        if (currentMonth == Month.DECEMBER) {
            currentYear--;
        }
        resetCounts();
        loadCustomerRentalData();
        renderPanes(currentYear, currentMonth);
    }
    
    @FXML
    private void handleMonthForward() {
        currentMonth = currentMonth.plus(1);
        if (currentMonth == Month.JANUARY) {
            currentYear++;
        }
        resetCounts();
        loadCustomerRentalData();
        renderPanes(currentYear, currentMonth);
    }

    @FXML
    public void handleExportReport() throws Exception {
        System.out.println("=== handleExportReport started ===");
    
        String filePath = PathConfig.getUtilizationReportPath(currentMonth, currentYear);
        System.out.println("File path resolved: " + filePath);
    
        PdfWriter writer = new PdfWriter(new File(filePath));
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        System.out.println("PDF writer and document initialized.");
    
        float sizeScalar = 0.75f;
        document.setMargins(5, 5, 5, 5);
        System.out.println("Margins set.");
    
        // Title
        document.add(new Paragraph(String.format("Utilization Report: %s %d",
                currentMonth.getDisplayName(TextStyle.FULL, Locale.ENGLISH), currentYear))
                .setFontSize(16 * sizeScalar)
                .setBold());
        System.out.println("Title added.");
    
        // Table setup
        int totalColumns = numColumns + 3;
        Table table = new Table(UnitValue.createPercentArray(totalColumns));
        table.setWidth(UnitValue.createPercentValue(100));
        System.out.println("Table created with " + totalColumns + " columns.");
    
        // Headers
        table.addCell(new Cell().add(new Paragraph("Model")).setFontSize(12 * sizeScalar));
        table.addCell(new Cell().add(new Paragraph("Serial")).setFontSize(12 * sizeScalar));
        System.out.println("Basic headers added.");
    
        for (int col = 1; col <= numColumns; col++) {
            table.addCell(new Cell().add(new Paragraph(String.valueOf(col)))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12 * sizeScalar));
        }
        System.out.println("Day headers (1.." + numColumns + ") added.");
    
        table.addCell(new Cell().add(new Paragraph("Sum"))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(12 * sizeScalar));
        System.out.println("Sum header added.");
    
        // Totals row
        table.addCell(new Cell(1, 2).add(new Paragraph("Total on Rent"))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(12 * sizeScalar));
        System.out.println("Totals row header added.");
    
        for (int col = 1; col <= numColumns; col++) {
            int count = rentalDayCounts.getOrDefault(col, 0);
            String countStr = count == 0 ? "" : String.valueOf(count);
            Paragraph verticalCount = new Paragraph();
            for (char digit : countStr.toCharArray()) {
                verticalCount.add(digit + "\n");
            }
            table.addCell(new Cell().add(verticalCount)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12 * sizeScalar));
        }
        table.addCell(new Cell().add(new Paragraph("")).setFontSize(12 * sizeScalar));
        System.out.println("Totals row data added.");
    
        // Loop over lifts
        String previousLiftType = null;
        for (Lift lift : validLifts) {
            String currentLiftType = lift.getLiftType();
    
            if (previousLiftType != null && !previousLiftType.equals(currentLiftType)) {
                int liftTypeCount = liftTypeCounts.getOrDefault(previousLiftType, 0);
                int liftTypeSerialCount = liftTypeSerialCounts.getOrDefault(previousLiftType, 0);
    
                if (liftTypeSerialCount > 0) {
                    double utilPercent = ((double) liftTypeCount * 100) / (liftTypeSerialCount * numBusinessDays);
                    utilPercent = Math.round(utilPercent * 10.0) / 10.0;
    
                    table.addCell(new Cell(1, totalColumns - 1)
                            .add(new Paragraph(previousLiftType + ": " + utilPercent + "%"))
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFontSize(12 * sizeScalar));
                    table.addCell(new Cell().add(new Paragraph("")));
                    System.out.println("Added utilization row for liftType: " + previousLiftType);
                }
            }
    
            // Model + Serial
            String model = lift.getModel();
            String serialNumber = lift.getSerialNumber();
            table.addCell(new Cell().add(new Paragraph(model)).setFontSize(12 * sizeScalar));
            table.addCell(new Cell().add(new Paragraph(serialNumber)).setFontSize(12 * sizeScalar));
    
            // Day loop
            for (int col = 1; col <= numColumns; col++) {
                LocalDate day = LocalDate.of(currentYear, currentMonth, col);
    
                boolean isOnRent = customerRentals.stream()
                        .filter(rental -> rental.getSerialNumber().equals(serialNumber))
                        .anyMatch(rental -> isOnRentForDay(rental, day));
    
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
            System.out.println("Processed lift: " + model + " (" + serialNumber + "), daysOnRent=" + daysOnRent);
    
            previousLiftType = currentLiftType;
        }
    
        // Final utilization row
        if (previousLiftType != null) {
            int liftTypeCount = liftTypeCounts.getOrDefault(previousLiftType, 0);
            int liftTypeSerialCount = liftTypeSerialCounts.getOrDefault(previousLiftType, 0);
    
            if (liftTypeSerialCount > 0) {
                double utilPercent = (liftTypeCount * 100.0) / (liftTypeSerialCount * numBusinessDays);
                utilPercent = Math.round(utilPercent * 10.0) / 10.0;
    
                table.addCell(new Cell(1, totalColumns - 1)
                        .add(new Paragraph(previousLiftType + ": " + utilPercent + "%"))
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontSize(12 * sizeScalar));
                table.addCell(new Cell().add(new Paragraph("")));
                System.out.println("Added final utilization row for liftType: " + previousLiftType);
            }
        }
    
        document.add(table);
        document.close();
        System.out.println("PDF export completed: " + filePath);
        System.out.println("=== handleExportReport finished ===");
    }
    
    /**
     * Helper method: determines if a rental counts as "on rent" for a given day.
     */
    private boolean isOnRentForDay(Rental rental, LocalDate day) {
        LocalDate delivery = LocalDate.parse(rental.getDeliveryDate());
        String callOffRaw = rental.getCallOffDate();
        LocalDate callOff = (callOffRaw == null ? null : LocalDate.parse(callOffRaw));
    
        // Exclude: Picked Up with no call-off date
        if ("Picked Up".equals(rental.getStatus()) && callOff == null) {
            return false;
        }
    
        // Normal on-rent condition
        return (delivery.isBefore(day) || delivery.isEqual(day)) &&
               (callOff == null || callOff.isAfter(day) || callOff.isEqual(day)) &&
               !"A Test Customer".equals(rental.getName());
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
    
        // --- Top Pane ---
        AnchorPane topPane = new AnchorPane();
        topPane.setPrefHeight(50);
        drawTopPane(topPane, year, month);
        renderedNodes.add(topPane);
    
        // --- Middle Scrollable Pane ---
        numColumns = month.length(LocalDate.of(year, month, 1).isLeapYear());
    
        Pane pane = new Pane();
        drawUtilization(pane, year, month);
        addAverages(pane);
    
        // 👇 force content size so scrolling works
        double contentWidth = numColumns * 9.5; // adjust based on circle spacing
        double contentHeight = validLifts.size() * 11.5; // adjust based on row spacing
        pane.setPrefSize(contentWidth, contentHeight);
    
        System.out.println("Were at time of setting pane dims");
        System.out.println("contentHeight: " + contentHeight);
        System.out.println("rowLabelOffsetY: " + rowLabelOffsetY);

        scrollPane = new ScrollPane(pane);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setPrefSize(309, 511);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        scrollPane.setTranslateY(-31);
    
        renderedNodes.add(scrollPane);
    
        // --- Bottom Pane ---
        AnchorPane bottomPane = new AnchorPane();
        bottomPane.setPrefHeight(50);
        drawBottomPane(bottomPane, year, month);
        bottomPane.setTranslateY(-31);
        renderedNodes.add(bottomPane);
    
        // --- Add all to layout ---
        currentLayout.getChildren().addAll(topPane, scrollPane, bottomPane);
    
        animateScrollBars(scrollPane);
    }
    
    private void loadCustomerRentalData() {

        // --- 1. Calculate month boundaries ---
        LocalDate monthStart = LocalDate.of(currentYear, currentMonth, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        // --- 2. Fetch all rental items overlapping current month ---
        String rentalQuery = """
            SELECT ro.customer_id,
                c.customer_name,
                ri.item_delivery_date,
                ri.item_call_off_date,
                ri.delivery_time,
                ro.po_number,
                ordered_contacts.first_name AS ordered_contact_name,
                ordered_contacts.phone_number AS ordered_contact_phone,
                ri.auto_term,
                ro.site_name,
                ro.street_address,
                ro.city,
                ri.rental_item_id,
                ri.lift_id,
                l.lift_type,
                l.serial_number,
                ro.single_item_order,
                ri.rental_order_id,
                ro.longitude,
                ro.latitude,
                site_contacts.first_name AS site_contact_name,
                site_contacts.phone_number AS site_contact_phone,
                ri.item_status
            FROM customers c
            JOIN rental_orders ro ON c.customer_id = ro.customer_id
            JOIN rental_items ri ON ro.rental_order_id = ri.rental_order_id
            JOIN lifts l ON ri.lift_id = l.lift_id
            LEFT JOIN contacts AS ordered_contacts ON ri.ordered_contact_id = ordered_contacts.contact_id
            LEFT JOIN contacts AS site_contacts ON ri.site_contact_id = site_contacts.contact_id
            WHERE
                ri.item_delivery_date <= ?
                AND (ri.item_call_off_date IS NULL OR ri.item_call_off_date >= ?)
        """;

        // We'll collect all rental item IDs to fetch services in a single query
        Map<Integer, Rental> rentalMap = new HashMap<>();
        List<Integer> rentalItemIds = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(
                Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
            PreparedStatement ps = connection.prepareStatement(rentalQuery)) {

            ps.setDate(1, java.sql.Date.valueOf(monthEnd));
            ps.setDate(2, java.sql.Date.valueOf(monthStart));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String customerName = rs.getString("customer_name");
                if ("A Test Customer".equals(customerName)) continue;

                LocalDate deliveryDate = rs.getDate("item_delivery_date").toLocalDate();
                Date callOffSql = rs.getDate("item_call_off_date");
                LocalDate callOffDate = (callOffSql != null) ? callOffSql.toLocalDate() : null;
                String deliveryTime = rs.getString("delivery_time");

                LocalDate effectiveStart = "Any".equalsIgnoreCase(deliveryTime)
                        ? nextBusinessDay(deliveryDate)
                        : deliveryDate;

                boolean overlapsMonth = !effectiveStart.isAfter(monthEnd)
                        && (callOffDate == null || !callOffDate.isBefore(monthStart));
                if (!overlapsMonth) continue;

                int rentalItemId = rs.getInt("rental_item_id");
                Rental rental = new Rental(
                        rs.getString("customer_id"),
                        customerName,
                        deliveryDate.toString(),
                        callOffDate != null ? callOffDate.toString() : null,
                        rs.getString("po_number"),
                        rs.getString("ordered_contact_name"),
                        rs.getString("ordered_contact_phone"),
                        rs.getBoolean("auto_term"),
                        rs.getString("site_name"),
                        rs.getString("street_address"),
                        rs.getString("city"),
                        rentalItemId,
                        rs.getString("serial_number"),
                        rs.getBoolean("single_item_order"),
                        rs.getInt("rental_order_id"),
                        rs.getString("site_contact_name"),
                        rs.getString("site_contact_phone"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getString("lift_type"),
                        rs.getString("item_status")
                );

                rentalMap.put(rentalItemId, rental);
                rentalItemIds.add(rentalItemId);
            }

            if (rentalItemIds.isEmpty()) {
                customerRentals = new ArrayList<>();
                return;
            }

            Map<Integer, List<Service>> servicesByRental = new HashMap<>();

            // --- 3. Fetch all completed change-out services for these rental items ---
            if (!rentalItemIds.isEmpty()) {
                String inClause = rentalItemIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));

                String svcQuery = String.format("""
                    SELECT s.*,
                        lOld.serial_number AS old_lift_serial,
                        lNew.serial_number AS new_lift_serial,
                        lOld.lift_type AS old_lift_type,
                        lNew.lift_type AS new_lift_type,
                        ro.site_name,
                        ro.street_address,
                        ro.city,
                        ro.longitude,
                        ro.latitude
                    FROM services s
                    LEFT JOIN lifts lOld ON s.old_lift_id = lOld.lift_id
                    LEFT JOIN lifts lNew ON s.new_lift_id = lNew.lift_id
                    LEFT JOIN rental_items ri ON s.rental_item_id = ri.rental_item_id
                    LEFT JOIN rental_orders ro ON ri.rental_order_id = ro.rental_order_id
                    WHERE s.rental_item_id IN (%s)
                    AND s.service_status = 'Completed'
                    AND s.service_type IN ('Change Out', 'Service Change Out')
                    ORDER BY s.service_date
                """, inClause);

                try (PreparedStatement svcPs = connection.prepareStatement(svcQuery);
                    ResultSet svcRs = svcPs.executeQuery()) {

                    while (svcRs.next()) {
                        int rid = svcRs.getInt("rental_item_id");
                        
                        Service svc = new Service(
                                svcRs.getInt("service_id"),
                                svcRs.getString("service_type"),
                                svcRs.getString("time"),
                                svcRs.getDate("service_date").toLocalDate().toString(),
                                svcRs.getString("reason"),
                                svcRs.getBoolean("billable"),
                                svcRs.getObject("previous_service_id") != null
                                        ? svcRs.getInt("previous_service_id") : null,
                                svcRs.getInt("new_rental_order_id"),
                                svcRs.getInt("new_lift_id"),
                                svcRs.getString("new_lift_type"),
                                svcRs.getString("new_lift_serial"),
                                svcRs.getInt("old_lift_id"),
                                svcRs.getString("old_lift_type"),
                                svcRs.getString("old_lift_serial"),
                                svcRs.getString("site_name"),
                                svcRs.getString("street_address"),
                                svcRs.getString("city"),
                                svcRs.getDouble("longitude"),
                                svcRs.getDouble("latitude"),
                                svcRs.getString("notes")
                        );

                        servicesByRental.computeIfAbsent(rid, k -> new ArrayList<>()).add(svc);
                    }
                }
            }

            // --- 4. Splice rentals according to services ---
            customerRentals = new ArrayList<>();

            for (Map.Entry<Integer, Rental> entry : rentalMap.entrySet()) {
                Rental baseRental = entry.getValue();
                List<Service> services = servicesByRental.getOrDefault(
                        entry.getKey(), Collections.emptyList()
                );

                if (services.isEmpty()) {
                    customerRentals.add(baseRental);
                    continue;
                }

                String sliceStart = baseRental.getDeliveryDate();
                int currentLiftId = baseRental.getLiftId();
                String currentSerial = baseRental.getSerialNumber();
                String oldSerial = null;

                for (int i = 0; i < services.size(); i++) {
                    Service svc = services.get(i);

                    System.out.println("Chopping up rental item: P" + baseRental.getRentalItemId() + 
                        "\nwith service: " + svc.getServiceId() +
                        "\noldLiftSerial: " + svc.getOldLiftSerial() +
                        "\nnewLiftSerial: " + svc.getNewLiftSerial());

                    String sliceEnd = previousBusinessDay(
                            LocalDate.parse(svc.getDate())
                    ).toString();

                    // --- Slice BEFORE this service ---
                    Rental r = baseRental.copyWith(
                            svc.getNewLiftId(),
                            sliceStart,
                            sliceEnd,
                            svc.getServiceType() + " Completed",
                            svc.getOldLiftSerial()
                    );

                    // Encode OLD / NEW
                    String newSerial = svc.getNewLiftSerial();

                    r.setOrderDate(String.format(
                            "OLD:%s;NEW:%s",
                            oldSerial != null ? oldSerial : "NULL",
                            newSerial != null ? newSerial : "NULL"
                    ));

                    customerRentals.add(r);

                    // Advance state
                    sliceStart = svc.getDate();
                    currentLiftId = svc.getNewLiftId();
                    currentSerial = svc.getNewLiftSerial();
                    oldSerial = svc.getOldLiftSerial();
                }

                // --- Final slice AFTER last service ---
                Rental finalSlice = baseRental.copyWith(
                        baseRental.getLiftId(),
                        sliceStart,
                        baseRental.getCallOffDate(),
                        baseRental.getStatus(),
                        baseRental.getSerialNumber()
                );

                // Final slice: ONLY "Changed from"
                finalSlice.setOrderDate(String.format(
                        "OLD:%s;NEW:NULL",
                        oldSerial != null ? oldSerial : "NULL"
                ));

                customerRentals.add(finalSlice);
            }



        } catch (SQLException e) {
            throw new RuntimeException("Error loading customer rental data", e);
        }
    }


    private void drawUtilization(Pane pane, int year, Month month) {
        final double circleSize = 7;
        final double margin = 1.2;
        final double labelMargin = 2;
        final double bufferSize = 5;
        final double sectionLabelMargin = 20;
    
        validLifts = lifts.stream()
            .filter(lift -> lift.getSerialNumber() != null && !lift.getSerialNumber().trim().isEmpty())
            .toList();
        int numRows = validLifts.size();
    
        double labelOffsetX = 52;
    
        String currentLiftType = null;
        rowLabelOffsetY = -14;
        int sectionRowCount = 0;
    
        // ------------------ Draw section headers + row labels ------------------
        for (int i = 0; i < numRows; i++) {
            Lift lift = validLifts.get(i);
    
            // Section headers (lift type)
            if (!lift.getLiftType().equals(currentLiftType)) {
                currentLiftType = lift.getLiftType();
    
                Label typeLabel = new Label(currentLiftType);
                typeLabel.setFont(Font.font("Lucida Handwriting", 11));
                typeLabel.setLayoutX(12);
                typeLabel.setLayoutY(rowLabelOffsetY + labelMargin +
                                     (i + 1) * (circleSize + margin) + 2);
                pane.getChildren().add(typeLabel);
    
                // Optional: horizontal line under section header
                Line line = new Line(36,
                                     rowLabelOffsetY + labelMargin + (i + 1) * (circleSize + margin) + 14,
                                     36 + 220,
                                     rowLabelOffsetY + labelMargin + (i + 1) * (circleSize + margin) + 14);
                line.setStroke(Color.BLACK);
                line.setStrokeWidth(0.5);
                pane.getChildren().add(line);

                typeHeaderCoords.put(currentLiftType, line.getStartY());
    
                // Reset section row count
                sectionRowCount = 0;
                rowLabelOffsetY += sectionLabelMargin;
            }
    
            sectionRowCount++;
    
            // Row label (serial number)
            Label serialLabel = new Label(lift.getSerialNumber());
            serialLabel.setFont(Font.font("Arial", 10));
            serialLabel.setLayoutX(0);
            serialLabel.setLayoutY(rowLabelOffsetY + labelMargin + (i + 1) * (circleSize + margin));
            pane.getChildren().add(serialLabel);
    
            // Add buffer space every 5 rows
            if (sectionRowCount % 5 == 0 &&
                i + 1 < validLifts.size() &&
                lift.getLiftType().equals(validLifts.get(i + 1).getLiftType())) {
                rowLabelOffsetY += bufferSize;
            }
        }
    
        for (int col = 0; col < numColumns; col++) {
            rentalDayCounts.put(col + 1, 0);
        }


        // ------------------ Draw utilization circles ------------------
        currentLiftType = null;
        rowLabelOffsetY = -29;
        sectionRowCount = 0;
    
        // separate offset for circles
        double circleRowLabelOffsetY = -29;
    
        for (int row = 0; row < numRows; row++) {
            Lift lift = validLifts.get(row);
            String serialNumber = lift.getSerialNumber();
    
            if (!lift.getLiftType().equals(currentLiftType)) {
                currentLiftType = lift.getLiftType();
                rowLabelOffsetY += sectionLabelMargin;
                circleRowLabelOffsetY += sectionLabelMargin;
                sectionRowCount = 0;
            }
            sectionRowCount++;
    
            for (int col = 0; col < numColumns; col++) {
                LocalDate day = LocalDate.of(year, month, col + 1);
                LocalDate today = LocalDate.now();
            
                // Skip weekends, holidays, and days in the future
                if (day.getDayOfWeek().getValue() > 5 || 
                    Config.COMPANY_HOLIDAYS.contains(day) || 
                    day.isAfter(today)) {
                    continue;
                }
            
    
                // Find rental match
                Rental rental = customerRentals.stream()
                    .filter(r -> r.getSerialNumber().equals(serialNumber))
                    .filter(r -> {
                        LocalDate delivery = LocalDate.parse(r.getDeliveryDate());

                        // Normalize call-off date
                        String callOffRaw = r.getCallOffDate();
                        LocalDate callOff = (callOffRaw == null || callOffRaw.isEmpty())
                            ? null
                            : LocalDate.parse(callOffRaw);

                        // Exclude rentals that were picked up but don't have a valid call-off date
                        if ("Picked Up".equals(r.getStatus()) && callOff == null) {
                            return false;
                        }

                        // Normal on-rent condition
                        return (delivery.isBefore(day) || delivery.isEqual(day)) &&
                            (callOff == null || callOff.isAfter(day) || callOff.isEqual(day));
                    })
                    .findFirst()
                    .orElse(null);

                boolean isOnRent = rental != null;

            
    
                if (isOnRent) {
                    rentalDayCounts.put(col + 1, rentalDayCounts.get(col + 1) + 1);
                    liftTypeCounts.put(currentLiftType, liftTypeCounts.get(currentLiftType) + 1);
                    serialCounts.put(serialNumber, serialCounts.get(serialNumber) + 1);
                }



                // Circle
                Circle circle = new Circle(
                    labelOffsetX + col * (circleSize + margin) - 12,
                    labelMargin + (row + 1) * (circleSize + margin) + circleRowLabelOffsetY + 21,
                    circleSize / 2
                );
                circle.setFill(isOnRent ? Color.GREEN : Color.RED);
    
                // Attach Rental (or serial/date key)
                circle.setUserData(rental != null ? rental : serialNumber + ":" + day);
    
                // Track circles per rental
                if (rental != null) {
                    rentalCircles.computeIfAbsent(rental, r -> new ArrayList<>()).add(circle);
                }
    
                // Handle click
                circle.setOnMouseClicked(e -> {
                    e.consume();
                    pane.getChildren().removeIf(node -> node instanceof PopupCard);
                    Object data = circle.getUserData();

                    if (data instanceof Rental rentalData) {
                        // Stop previous animations
                        rentalAnimations.values().forEach(Timeline::stop);
                        rentalAnimations.clear();

                        // Reset all circle sizes
                        rentalCircles.values().forEach(list -> {
                            list.forEach(c -> {
                                c.radiusProperty().unbind();
                                c.setRadius(3.5);
                            });
                        });

                        // Get circles for this rental
                        List<Circle> selectedCircles = rentalCircles.get(rentalData);
                        if (selectedCircles != null && !selectedCircles.isEmpty()) {
                            // Pulse animation
                            Circle ref = selectedCircles.get(0);
                            Timeline pulse = new Timeline(
                                new KeyFrame(Duration.ZERO, new KeyValue(ref.radiusProperty(), 2.5)),
                                new KeyFrame(Duration.seconds(0.9), new KeyValue(ref.radiusProperty(), 7)),
                                new KeyFrame(Duration.seconds(1.8), new KeyValue(ref.radiusProperty(), 2.5))
                            );
                            pulse.setCycleCount(Animation.INDEFINITE);
                            pulse.play();
                            rentalAnimations.put(rentalData, pulse);

                            // Bind all circles in this set to the same pulse
                            selectedCircles.forEach(c -> {
                                if (c != ref) {
                                    c.radiusProperty().bind(ref.radiusProperty());
                                }
                            });
                        }

                        // Show popup
                        PopupCard popup = new PopupCard(rentalData, e.getX(), e.getY() + 10);
                        popup.setLayoutX(45);
                        popup.setLayoutY(e.getY() + 10);
                        pane.getChildren().add(popup);
                    }
                });

                
    
                pane.getChildren().add(circle);
            }
    
            // Add buffer space every 5 rows for circles too
            if (sectionRowCount % 5 == 0 &&
                row + 1 < validLifts.size() &&
                lift.getLiftType().equals(validLifts.get(row + 1).getLiftType())) {
                circleRowLabelOffsetY += bufferSize;
            }
        }
    
        // Background click clears popups + resets highlight
        pane.setOnMouseClicked(e -> {
            if (e.getTarget() == pane) {
                pane.getChildren().removeIf(node -> node instanceof PopupCard);
        
                // Stop all animations + reset
                rentalAnimations.values().forEach(Timeline::stop);
                rentalAnimations.clear();
                rentalCircles.values().forEach(list ->
                    list.forEach(c -> {
                        c.setFill(Color.GREEN);
                        c.setRadius(3.5);
                        c.radiusProperty().unbind();
                    })
                );
            }
        });
        
    }
    
    

    private void addAverages(Pane pane) {
        // Iterate over each lift type
        for (String liftType : liftTypeCounts.keySet()) {
            // Get the total days on rent for this lift type
            double totalDaysOnRent = liftTypeCounts.get(liftType);

            // Get the number of lifts for this lift type
            double numberOfLifts = liftTypeSerialCounts.get(liftType);

            // Debug prints
            System.out.println("---- " + liftType + " ----");
            System.out.println("Total Days on Rent: " + totalDaysOnRent);
            System.out.println("Number of Lifts: " + numberOfLifts);
            System.out.println("Business Days: " + numBusinessDays);

            // Calculate utilization %
            double rawPercent = (totalDaysOnRent * 100.0) / (numberOfLifts * numBusinessDays);
            System.out.println("Raw Percent: " + rawPercent);

            // Round to 2 decimals
            double utilPercent = Math.round(rawPercent * 100.0) / 100.0;
            String utilPercentString = String.format("%.0f%%", utilPercent);

            System.out.println("Final Percent (Rounded): " + utilPercentString);

            // Get the Y-coordinate for this lift type's header
            double yCoord = typeHeaderCoords.getOrDefault(liftType, 0.0);

            Label label = new Label(utilPercentString);
            label.setTranslateX(259);
            label.setTranslateY(yCoord - 9);
            label.setStyle("-fx-font-family: 'Lucida Handwriting'; -fx-font-size: 10;");
            pane.getChildren().add(label);
        }
        System.out.println("Type Header Coords: " + typeHeaderCoords);
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
        numBusinessDays = numColumns - skippedColumns;

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

    private LocalDate previousBusinessDay(LocalDate date) {
        LocalDate prev = date.minusDays(1);
        while (prev.getDayOfWeek() == DayOfWeek.SATURDAY || prev.getDayOfWeek() == DayOfWeek.SUNDAY) {
            prev = prev.minusDays(1);
        }
        return prev;
    }

    private LocalDate nextBusinessDay(LocalDate date) {
        LocalDate next = date.plusDays(1);
        while (next.getDayOfWeek() == DayOfWeek.SATURDAY
                || next.getDayOfWeek() == DayOfWeek.SUNDAY) {
            next = next.plusDays(1);
        }
        return next;
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




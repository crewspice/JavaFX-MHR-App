package com.MaxHighReach;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiConsumer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.awt.Graphics2D;
import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;

public class PopupDisc extends StackPane {
    private static final double SIZE = 100;
    private static final double BORDER_WIDTH = 2;
    private static final double RADIUS = (SIZE - 2 * BORDER_WIDTH) / 2;

    private double spreadStartAngle;
    private double spreadEndAngle;

    private double angleToCenter;
    private Double spreadCenter = null;

    private double nameStartAngle;
    private double nameEndAngle;

    private double addressStartAngle;
    private double addressEndAngle;

    private double openingAngle;
    private double openingSpan;

    public PopupDisc(MapController mapController, Rental rental, double x, double y) {
        // System.out.println(
        //     "PopupDisc constructor called: " +
        //     "mapController=" + mapController +
        //     ", rental=" + rental +
        //     ", x=" + x +
        //     ", y=" + y
        // );

        // existing constructor code...


        setPrefSize(SIZE, SIZE);
        setAlignment(Pos.CENTER);
    
        // setBorder(new Border(new BorderStroke(
        //     Color.DARKGRAY,
        //     BorderStrokeStyle.SOLID,
        //     new CornerRadii(5),
        //     new BorderWidths(BORDER_WIDTH)
        // )));
    
        // Circle circle = new Circle(RADIUS, Color.TRANSPARENT);
        // circle.setStroke(Color.CYAN);
        // circle.setStrokeWidth(1.5);
        // getChildren().add(circle);
    
        setLayoutX(x - SIZE / 2);
        setLayoutY(y - SIZE / 2);
    
        double parentW = mapController.anchorPane.getWidth();
        double parentH = mapController.anchorPane.getHeight();
        double parentCenterX = parentW / 2;
        double parentCenterY = parentH / 2;
    
        double dx = parentCenterX - x;
        double dy = parentCenterY - y;
    
        angleToCenter = Math.atan2(dy, dx);
    
        // Circle redDot = new Circle(4, Color.RED);
        // redDot.setTranslateX(RADIUS * Math.cos(angleToCenter));
        // redDot.setTranslateY(RADIUS * Math.sin(angleToCenter));
        // getChildren().add(redDot);
    
        double leftOverlap = Math.max(0, SIZE / 2 - x);
        double rightOverlap = Math.max(0, (x + SIZE / 2) - parentW);
        double topOverlap = Math.max(0, SIZE / 2 - y);
        double bottomOverlap = Math.max(0, (y + SIZE / 2) - parentH);
    
        double normLeft = leftOverlap / (SIZE / 2);
        double normRight = rightOverlap / (SIZE / 2);
        double normTop = topOverlap / (SIZE / 2);
        double normBottom = bottomOverlap / (SIZE / 2);
    
        double horizOverlap = Math.max(normLeft, normRight);
        double vertOverlap = Math.max(normTop, normBottom);
        boolean cancelSpreadCenter = (horizOverlap < 0.7) && (vertOverlap > horizOverlap);
    
        if (!cancelSpreadCenter && horizOverlap > 0) {
            double targetAngle = normLeft > normRight ? 0.0 : Math.PI;
            spreadCenter = interpolateAngle(angleToCenter, targetAngle, horizOverlap);
    
            // Circle orangeDot = new Circle(4, Color.ORANGE);
            // orangeDot.setTranslateX(RADIUS * Math.cos(spreadCenter));
            // orangeDot.setTranslateY(RADIUS * Math.sin(spreadCenter));
            // getChildren().add(orangeDot);
        }
        
        double outerRadius = 100;
        double innerRadius = 16;
        double wedgeAngle = 45;
        int wedgeCount = 5;
        double totalAngle = wedgeAngle * wedgeCount;
    
        double effectiveSpreadCenter = (spreadCenter != null) ? spreadCenter : angleToCenter;
        double spreadCenterDegrees = Math.toDegrees(effectiveSpreadCenter) - (totalAngle / 2);
        double wedgeStartDeg = spreadCenterDegrees;
        double wedgeEndDeg = wedgeStartDeg + totalAngle;
        spreadStartAngle = wedgeStartDeg;
        spreadEndAngle = wedgeEndDeg;
    
        /* === Composite Seamless Wedge Ring ===
        Path seamlessWedges = createWedgeRing(innerRadius, outerRadius, wedgeCount);
        seamlessWedges.setTranslateX(54);
        seamlessWedges.setTranslateY(35);
        getChildren().add(seamlessWedges);
        seamlessWedges.toBack(); */

        double wedgeSpan = wedgeEndDeg - wedgeStartDeg; // angle length in degrees

        for (int i = 0; i < wedgeCount; i++) {
            double rotation = spreadCenterDegrees + i * wedgeAngle;
    
            double bandInner = 16;
            double bandOuter = 54;
            double bandSplit = 50; // Major band ends, minor band begins
            double bandPeripheral = 105;
            double bandSplit2 = 54;
        
            int iNorm = i + 1;
            String routeName = "route" + iNorm;
            String[] colors = mapController.getRouteColors(routeName);
            Color c0 = Color.web(colors[0]); // Major band color
            Color c1 = Color.web(colors[1]); // Minor outer band color

            // === Major Band (inner) ===
            Path majorBand = createInteractiveBand(bandInner, bandSplit, wedgeAngle);
            majorBand.getTransforms().add(new Rotate(rotation, 0, 0));
            majorBand.setFill(c1);
            majorBand.setStroke(Color.TRANSPARENT);
            majorBand.setTranslateX(29);
            majorBand.setTranslateY(17);
            final int index = i;
            majorBand.setOnMouseClicked(event -> {
                mapController.addStopToRoute(routeName, rental);
             //   System.out.println("calling mapController.addStopToRoute with routeName = " + routeName);
            });
        
            // === Minor Band (outer) ===
            Path minorBand = createInteractiveBand(bandSplit, bandOuter, wedgeAngle);
            minorBand.getTransforms().add(new Rotate(rotation, 0, 0));
            minorBand.setFill(c0);
            minorBand.setStroke(Color.TRANSPARENT);
            minorBand.setTranslateX(43);
            minorBand.setTranslateY(19);

            // === Peripheral band (outermost) ===
            Path peripheralBand = createInteractiveBand(bandSplit2, bandPeripheral, wedgeAngle);
            peripheralBand.getTransforms().add(new Rotate(rotation, 0, 0));
            peripheralBand.setFill(Color.rgb(255, 255, 255, 0.6));
            peripheralBand.setStroke(Color.TRANSPARENT);
            peripheralBand.setTranslateX(70);
            peripheralBand.setTranslateY(37);

            // Add both bands to scene (minor first so it appears behind)
            getChildren().addAll(minorBand, majorBand, peripheralBand);
        
            Node truckVisual;
            Optional<String> maybeAssignedTruck;
            boolean usedTruckImage = false;

            if (mapController.routeAssignments != null && mapController.routeAssignments.containsKey(routeName)) {
                // ✅ Top-priority override: routeAssignments
                String labelText = mapController.routeAssignments.get(routeName);
           //     System.out.println("🟩 routeAssignments has route " + routeName + " → '" + labelText + "'");
                Text truckLabel = new Text(labelText);
                truckLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 11));
                truckLabel.setFill(Color.web(colors[2]));
                truckLabel.setTranslateX(-truckLabel.getLayoutBounds().getWidth() / 2);
                truckLabel.setTranslateY(truckLabel.getLayoutBounds().getHeight() / 4);
                truckVisual = truckLabel;
            } else {
                // Check truckAssignments as a fallback
                maybeAssignedTruck = mapController.truckAssignments != null
                    ? mapController.truckAssignments.entrySet().stream()
                        .filter(entry -> routeName.equals(entry.getValue()))
                        .map(Map.Entry::getKey)
                        .findFirst()
                    : Optional.empty();
            
                if (maybeAssignedTruck.isPresent()) {
                    String assignedTruckName = "'" + maybeAssignedTruck.get();
               //     System.out.println("🟨 truckAssignments has route " + routeName + " → " + assignedTruckName);
                    Text truckLabel = new Text(assignedTruckName);
                    truckLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 11));
                    truckLabel.setFill(Color.web(colors[2]));
                    truckLabel.setTranslateX(-truckLabel.getLayoutBounds().getWidth() / 2);
                    truckLabel.setTranslateY(truckLabel.getLayoutBounds().getHeight() / 4);
                    truckVisual = truckLabel;
                } else {
               //     System.out.println("🟥 No assignment found for " + routeName + ", using fallback image");
                    String imagePath = getClass().getResource("/images/truck-face.png").toExternalForm();
                    Image rawTruckImage = new Image(imagePath);
                    Color symbolColor = Color.web(colors[2]); // Tertiary color
                    Image recoloredImage = recolorImage(rawTruckImage, symbolColor);
                    ImageView truckView = new ImageView(recoloredImage);
                    truckView.setFitWidth(20);
                    truckView.setFitHeight(20);
                    truckView.setPreserveRatio(true);
                    truckView.setTranslateX(-truckView.getFitWidth() / 2);
                    truckView.setTranslateY(-truckView.getFitHeight() / 2);
                    truckVisual = truckView;
                    usedTruckImage = true;
                }
            }
            
            // === Wrap the visual (label or image) in a Group and position/rotate ===
            Group truckGroup = new Group(truckVisual);

            double centerAngleDeg = rotation + wedgeAngle / 2.0;
            double angleRad = Math.toRadians(centerAngleDeg);
            double radius = (bandInner + bandSplit) / 2.0;

            double centerX = radius * Math.cos(angleRad);
            double centerY = radius * Math.sin(angleRad);

            truckGroup.setTranslateX(centerX);
            truckGroup.setTranslateY(centerY);

            // Keep upright
            double truckAngleToCenter = Math.toDegrees(Math.atan2(centerY, centerX));
            truckAngleToCenter = truckAngleToCenter > 0 ? truckAngleToCenter + 180 : truckAngleToCenter;

            truckGroup.setOnMouseClicked(event -> {
                mapController.addStopToRoute(routeName, rental);
            });

            getChildren().add(truckGroup);
            truckGroup.setTranslateX(centerX);
            truckGroup.setTranslateY(centerY);
        
            // Counter-rotate to keep upright
            if (usedTruckImage) {
                truckGroup.getTransforms().add(new Rotate(truckAngleToCenter + 90, 0, 0));
            }
            
            
            truckGroup.setOnMouseClicked(event -> {
                mapController.addStopToRoute(routeName, rental);
            });
        
            //getChildren().add(truckGroup);
        }
        
        
    
        String rawName = Config.CUSTOMER_NAME_MAP.getOrDefault(rental.getName(), rental.getName());
        String name = rawName.replace(".", "");
        int len = name.length();

    
        double labelCenterDeg = Math.toDegrees(effectiveSpreadCenter);
    
        // Find vertical angle (±90°) and shift toward it within wedge space
        double targetAngleDeg = (Math.abs(labelCenterDeg - 90) < Math.abs(labelCenterDeg + 90)) ? 90 : -90;
        double maxShift = targetAngleDeg - labelCenterDeg;
        double labelArcSpan = (len > 1) ? 9.0 * (len - 1) : 9.0;
    
        double labelStartDeg = labelCenterDeg - labelArcSpan / 2;
        double labelEndDeg = labelCenterDeg + labelArcSpan / 2;
        double leftGap = (labelStartDeg - wedgeStartDeg + 360) % 360;
        double rightGap = (wedgeEndDeg - labelEndDeg + 360) % 360;
    
        double clampedShift;
        if (maxShift > 0) {
            clampedShift = Math.min(maxShift, rightGap);
        } else {
            clampedShift = Math.max(maxShift, -leftGap);
        }
    
        double adjustedLabelCenterDeg = labelCenterDeg + clampedShift;
    
        // Compute arc length required by actual text
        double arcLengthPx = 0;
        for (int i = 0; i < name.length(); i++) {
            Text temp = new Text(String.valueOf(name.charAt(i)));
            temp.setFont(Font.font("Verdana", 20));
            arcLengthPx += temp.getLayoutBounds().getWidth();
        }
        arcLengthPx += (name.length() - 1) * 5;
        double neededArcDeg = Math.toDegrees(arcLengthPx / 80.0);
    
        // Max wedge space left/right of center
        double maxLeftDeg = adjustedLabelCenterDeg - wedgeStartDeg;
        double maxRightDeg = wedgeEndDeg - adjustedLabelCenterDeg;
        double maxAvailableDeg = 2 * Math.min(maxLeftDeg, maxRightDeg);
    
        double clampedArcDeg = Math.min(neededArcDeg, maxAvailableDeg);
        double adjustedLabelCenterRad = Math.toRadians(adjustedLabelCenterDeg);

        double[] nameSpan = addSemiCircleLabel(name, adjustedLabelCenterRad, clampedArcDeg, 20, 85, Color.web(Config.getPrimaryColor()), Color.web(Config.getTertiaryColor()));
        nameStartAngle = nameSpan[0];
        nameEndAngle = nameSpan[1];
        String line1 = rental.getAddressBlockOne();
        String line2 = rental.getAddressBlockTwo();
        int maxChars = (line2 != null) ? line2.length() + 4 : 20; // fallback max
        String siteText = line1;
        if (line1 != null && line1.length() > maxChars) {
            siteText = line1.substring(0, Math.max(0, maxChars - 1)) + "…";
        }
        addStreetAddressVisual(siteText, adjustedLabelCenterRad, totalAngle, 65, "site");
                addStreetAddressVisual(rental.getAddressBlockTwo(), adjustedLabelCenterRad, totalAngle, 56, "street");
        String cityText = rental.getAddressBlockThree() + " \u25C6 " + rental.getDeliveryTime();
        addStreetAddressVisual(cityText, adjustedLabelCenterRad, totalAngle, 48, "city");
        
        openingAngle = getWidestOpenAngleDeg();
        secondaryLiftTypeAngleAdjustent();

        /*
        // === Diagnostics Dots ===
        Circle startDot = new Circle(4, Color.CYAN);
        startDot.setTranslateX(RADIUS * Math.cos(Math.toRadians(spreadStartAngle)));
        startDot.setTranslateY(RADIUS * Math.sin(Math.toRadians(spreadStartAngle)));
    
        Circle endDot = new Circle(4, Color.CYAN);
        endDot.setTranslateX(RADIUS * Math.cos(Math.toRadians(spreadEndAngle)));
        endDot.setTranslateY(RADIUS * Math.sin(Math.toRadians(spreadEndAngle)));
    
        Circle nameStartDot = new Circle(4, Color.web(Config.getPrimaryColor()));
        nameStartDot.setTranslateX(RADIUS * Math.cos(Math.toRadians(nameStartAngle)));
        nameStartDot.setTranslateY(RADIUS * Math.sin(Math.toRadians(nameStartAngle)));
    
        Circle nameEndDot = new Circle(4, Color.web(Config.getPrimaryColor()));
        nameEndDot.setTranslateX(RADIUS * Math.cos(Math.toRadians(nameEndAngle)));
        nameEndDot.setTranslateY(RADIUS * Math.sin(Math.toRadians(nameEndAngle)));
    
        Circle addressStartDot = new Circle(4, Color.web(Config.getTertiaryColor()));
        addressStartDot.setTranslateX(RADIUS * Math.cos(Math.toRadians(addressStartAngle)));
        addressStartDot.setTranslateY(RADIUS * Math.sin(Math.toRadians(addressStartAngle)));
    
        Circle addressEndDot = new Circle(4, Color.web(Config.getTertiaryColor()));
        addressEndDot.setTranslateX(RADIUS * Math.cos(Math.toRadians(addressEndAngle)));
        addressEndDot.setTranslateY(RADIUS * Math.sin(Math.toRadians(addressEndAngle)));
        
        Circle openAngleDot = new Circle(4, Color.DARKMAGENTA);
        openAngleDot.setTranslateX(RADIUS * Math.cos(Math.toRadians(openingAngle)));
        openAngleDot.setTranslateY(RADIUS * Math.sin(Math.toRadians(openingAngle)));


        getChildren().addAll(startDot, endDot, nameStartDot, nameEndDot, addressStartDot, addressEndDot, openAngleDot);
    */
        if (rental.isService()) {
            String serviceType = rental.getService().getServiceType();
            String serviceImageName;
            switch (serviceType) {
                case "Move" -> serviceImageName = "move.png";
                case "Change Out" -> serviceImageName = "change-out.png";
                case "Service Change Out" -> serviceImageName = "service-change-out.png";
                case "Service" -> serviceImageName = "service.png";
                default -> serviceImageName = "change-out.png";
            }
        
            Image compoundImage = buildServiceCompoundPeekImage(serviceImageName, rental);
            addImageAtAngle(compoundImage, openingAngle, 79, 48);
        
        } else if (rental.isSingleItemOrder()) {
            addImageAtAngle("/images/" + rental.getLiftType() + ".png", openingAngle, 79, 38);
        
        } else {
            List<Rental> relatedRentals = fetchRelatedRentals(rental);
            List<String> liftTypes = relatedRentals.stream()
                    .map(Rental::getLiftType)
                    .toList();
            Image compoundImage = buildCompoundPeekImage(liftTypes);
            addImageAtAngle(compoundImage, openingAngle, 79, 38);
        }
    
    
        /*
        System.out.println("\n===== ANGLE DIAGNOSTICS =====");
        System.out.printf("🔵 angleToCenter       : %.2f°\n", Math.toDegrees(angleToCenter));
        System.out.printf("🟠 spreadCenter        : %s\n", 
            (spreadCenter != null ? String.format("%.2f°", Math.toDegrees(spreadCenter)) : "null"));
        System.out.printf("🟣 spreadStartAngle    : %.2f°\n", spreadStartAngle);
        System.out.printf("🟣 spreadEndAngle      : %.2f°\n", spreadEndAngle);
        System.out.printf("🟢 nameStartAngle      : %.2f°\n", nameStartAngle);
        System.out.printf("🟢 nameEndAngle        : %.2f°\n", nameEndAngle);
        System.out.printf("🔴 addressStartAngle   : %.2f°\n", addressStartAngle);
        System.out.printf("🔴 addressEndAngle     : %.2f°\n", addressEndAngle);
        System.out.printf("🟤 derivedOpenAngleDeg  : %.2f°\n", openingAngle);
        System.out.printf("🟤 derivedOpeningSpan  : %.2f°\n", openingSpan);
        System.out.println("================================\n");
        
        Circle clipCircle = new Circle(125, 125, 110);  // center is (125,125) for StackPane
        setClip(clipCircle);
        
        // Optional: for debug visibility
        clipCircle.setStroke(Color.RED);
        clipCircle.setFill(Color.color(1, 0, 0, 0.1));
        getChildren().add(clipCircle);
        



        // Simple circular clip centered at 0,0 (same as your content)
        Circle debugClip = new Circle(x, y, 110); // 110 matches your radius cutoff
        this.setClip(debugClip);

        // Optional debug view
        debugClip.setStroke(Color.RED);
        debugClip.setFill(Color.color(1, 0, 0, 0.15));
        getChildren().add(debugClip);



        
        // === Apply Custom Clip to Trim StackPane ===
        Path clipPath = new Path();

        // Elements are centered at (0, 0), so clip center should match
        double clipCenterX = 0;
        double clipCenterY = 0;
        double radius = 110;

        // Normalize angles into [0, 360)
        double normalizedStart = normalize360(spreadStartAngle);
        double normalizedEnd = normalize360(spreadEndAngle);
        if (normalizedEnd <= normalizedStart) normalizedEnd += 360;

        // Move to center
        clipPath.getElements().add(new MoveTo(clipCenterX, clipCenterY));

        // Trace arc
        int steps = 60;
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double angleDeg = normalizedStart + (normalizedEnd - normalizedStart) * t;
            double angleRad = Math.toRadians(angleDeg);
            double xx = clipCenterX + radius * Math.cos(angleRad);
            double yy = clipCenterY + radius * Math.sin(angleRad);
            clipPath.getElements().add(new LineTo(xx, yy));
        }

        clipPath.getElements().add(new ClosePath());
        this.setClip(clipPath);

        // Optional: visualize the clip area for debugging
        clipPath.setStroke(Color.MAGENTA);
        clipPath.setFill(Color.color(1, 0, 1, 0.15)); // Light translucent purple
        getChildren().add(clipPath); // Comment this out in production

        */
        // Make the entire PopupDisc transparent to mouse clicks
        setPickOnBounds(false);

        // For every child that should NOT block clicks (like backgrounds), disable pick
     //   getChildren().forEach(node -> node.setMouseTransparent(true));
        
    }

    private double interpolateAngle(double from, double to, double weight) {
        double delta = to - from;
        while (delta > Math.PI) delta -= 2 * Math.PI;
        while (delta < -Math.PI) delta += 2 * Math.PI;
        return from + delta * weight;
    }

    private Path createWedgeRing(double innerR, double outerR, int wedgeCount) {
        double wedgeAngleDeg = 360.0 / wedgeCount;
        double wedgeAngleRad = Math.toRadians(wedgeAngleDeg);
    
        Path path = new Path();
        boolean first = true;
    
        // --- Outer Arc (clockwise) ---
        for (int i = 0; i < wedgeCount; i++) {
            double angle = i * wedgeAngleRad;
            double x = outerR * Math.cos(angle);
            double y = outerR * Math.sin(angle);
    
            if (first) {
                path.getElements().add(new MoveTo(x, y));
                first = false;
            } else {
                path.getElements().add(new ArcTo(outerR, outerR, 0, x, y, false, true));
            }
        }
    
        // --- Inner Arc (counter-clockwise) ---
        for (int i = wedgeCount - 1; i >= 0; i--) {
            double angle = i * wedgeAngleRad;
            double x = innerR * Math.cos(angle);
            double y = innerR * Math.sin(angle);
            path.getElements().add(new ArcTo(innerR, innerR, 0, x, y, false, false));
        }
    
        path.getElements().add(new ClosePath());
        path.setFill(Color.web("#f4f4f4"));
        path.setStroke(null); // fully eliminate seams
        path.setStrokeWidth(0);
        path.setSmooth(false);
        return path;
    }
    

    private void addLetterAlongAngle(String letter, double angleRad, boolean flip, double distance, double fontSize, Color primaryColor, Color outlineColor) {
        double x = distance * Math.cos(angleRad);
        double y = distance * Math.sin(angleRad);
    
        Group textGroup = new Group();
    
        if (outlineColor != null) {
            Color blendedOutline = interpolateColor(primaryColor, outlineColor, 0.5);
    
            Text outline = new Text(letter);
            outline.setFont(Font.font("Verdana", fontSize));
            outline.setFill(blendedOutline);
            outline.setStroke(blendedOutline);
            outline.setStrokeWidth(1.3);
            outline.setTranslateX(-outline.getLayoutBounds().getWidth() / 2);
            outline.setTranslateY(outline.getLayoutBounds().getHeight() / 4);
            textGroup.getChildren().add(outline);
        }
    
        Text text = new Text(letter);
        text.setFont(Font.font("Verdana", fontSize));
        text.setFill(primaryColor);
        text.setTranslateX(-text.getLayoutBounds().getWidth() / 2);
        text.setTranslateY(text.getLayoutBounds().getHeight() / 4);
        textGroup.getChildren().add(text);
    
        textGroup.setTranslateX(x);
        textGroup.setTranslateY(y);
    
        double angleDeg = Math.toDegrees(angleRad) + 90;
        if (flip) {
            angleDeg += 180;
        }
    
        textGroup.getTransforms().add(new Rotate(angleDeg, 0, 0));
        getChildren().add(textGroup);
    }
    
    
    
    
    private double[] addSemiCircleLabel(String text, double desiredCenterAngleRad,
         double maxArcSpanDeg, double fontSize, double radius, Color primaryColor, Color outlineColor) {
        int len = text.length();
        if (len == 0) return new double[] {0, 0};
    
        double displayRadius = radius * 1.15;
    
        // Step 1: Measure character widths
        double[] letterWidths = new double[len];
        double totalWidth = 0;
        for (int i = 0; i < len; i++) {
            Text temp = new Text(String.valueOf(text.charAt(i)));
            temp.setFont(Font.font("Verdana", fontSize));
            double w = temp.getLayoutBounds().getWidth();
            letterWidths[i] = w;
            totalWidth += w;
        }
    
        double spacing = 0.5;
        double totalArcLengthPx = totalWidth + (len - 1) * spacing;
        double arcSpanRad = totalArcLengthPx / radius;
        double arcSpanDeg = Math.toDegrees(arcSpanRad);
        if (arcSpanDeg > maxArcSpanDeg) arcSpanDeg = maxArcSpanDeg;
    
        double desiredCenterDeg = Math.toDegrees(desiredCenterAngleRad);
        double startAngleDeg = desiredCenterDeg - arcSpanDeg / 2;
    
        boolean flip, reverse;
        if (desiredCenterDeg >= 0 && desiredCenterDeg < 90) {
            flip = true; reverse = true;
        } else if (desiredCenterDeg >= 90 && desiredCenterDeg <= 180) {
            flip = true; reverse = true;
        } else if (desiredCenterDeg >= -180 && desiredCenterDeg < -90) {
            flip = false; reverse = false;
        } else {
            flip = false; reverse = false;
        }
    
        String displayText = reverse ? new StringBuilder(text).reverse().toString() : text;
    
        double[] letterCentersDeg = new double[len];
        double arcOffsetPx = 0;
    
        for (int i = 0; i < len; i++) {
            int index = reverse ? len - 1 - i : i;
            double charWidth = letterWidths[index];
            double centerArcLengthPx = arcOffsetPx + charWidth / 2;
            double angleOffsetDeg = Math.toDegrees(centerArcLengthPx / radius);
            double letterAngleDeg = startAngleDeg + angleOffsetDeg;
    
            letterCentersDeg[i] = letterAngleDeg;
    
            addLetterAlongAngle(
                String.valueOf(displayText.charAt(i)),
                Math.toRadians(letterAngleDeg),
                flip,
                displayRadius,
                fontSize,
                primaryColor,
                outlineColor
            );
    
            arcOffsetPx += charWidth + spacing;
        }
    
        double labelStartDeg = letterCentersDeg[0];
        double labelEndDeg = letterCentersDeg[len - 1];
    
        return new double[] {labelStartDeg, labelEndDeg};
    }
    

    private void addStreetAddressVisual(String street, double centerAngleRad, double maxArcSpanDeg, double radius, String type) {
        int len = street.length();
        if (len == 0) return;
    
        double fontSize = 10;
        double spacing = 0.5;
        double bufferDeg = 2.0;
    
        // Step 1: Measure text arc span
        double totalWidth = 0;
        for (int i = 0; i < len; i++) {
            Text temp = new Text(String.valueOf(street.charAt(i)));
            temp.setFont(Font.font("Verdana", fontSize));
            totalWidth += temp.getLayoutBounds().getWidth();
        }
        totalWidth += (len - 1) * spacing;
    
        double arcSpanRad = totalWidth / radius;
        double arcSpanDeg = Math.toDegrees(arcSpanRad);
    
        double desiredCenterDeg = Math.toDegrees(centerAngleRad);
        double labelStartDeg = desiredCenterDeg - arcSpanDeg / 2;
        double labelEndDeg = desiredCenterDeg + arcSpanDeg / 2;
    
        // Step 2: Clamp to inside of spread bounds (with buffer)
        double adjustedStartDeg = spreadStartAngle + bufferDeg;
        double adjustedEndDeg = spreadEndAngle - bufferDeg;
    
        double overLeft = adjustedStartDeg - labelStartDeg;
        double overRight = labelEndDeg - adjustedEndDeg;
    
        double shift = 0;
        if (overLeft > 0 && overRight <= 0) {
            shift = overLeft;
        } else if (overRight > 0 && overLeft <= 0) {
            shift = -overRight;
        } else if (overLeft > 0 && overRight > 0) {
            // Label is too wide, shift to middle of allowable range
            double allowedCenterDeg = (adjustedStartDeg + adjustedEndDeg) / 2.0;
            shift = allowedCenterDeg - desiredCenterDeg;
        }
    
        double adjustedCenterDeg = desiredCenterDeg + shift;
        double adjustedCenterRad = Math.toRadians(adjustedCenterDeg);
    
        /*System.out.printf("📍 Address label span: %.1f° → %.1f° (%.1f° total)\n", 
            adjustedCenterDeg - arcSpanDeg / 2, adjustedCenterDeg + arcSpanDeg / 2, arcSpanDeg);
        */
        // Step 3: Draw label
        double[] addressSpan = addSemiCircleLabel(
            street,
            adjustedCenterRad,
            maxArcSpanDeg,
            fontSize,
            radius * 1.1,
            Color.web(Config.getTertiaryColor()),
            null
        );
        if (type.equals("street")) {
            addressStartAngle = addressSpan[0];
            addressEndAngle = addressSpan[1];
        } else if (type.equals("city")) {

        }
        
    }
    

    private Path createInteractiveBand(double innerR, double outerR, double angleDeg) {
        double angleRad = Math.toRadians(angleDeg);
    
        double x1 = outerR * Math.cos(0);
        double y1 = outerR * Math.sin(0);
        double x2 = outerR * Math.cos(angleRad);
        double y2 = outerR * Math.sin(angleRad);
    
        double x3 = innerR * Math.cos(angleRad);
        double y3 = innerR * Math.sin(angleRad);
        double x4 = innerR * Math.cos(0);
        double y4 = innerR * Math.sin(0);
    
        Path path = new Path();
        path.getElements().add(new MoveTo(x1, y1));
        path.getElements().add(new ArcTo(outerR, outerR, 0, x2, y2, angleDeg > 180, true));
        path.getElements().add(new LineTo(x3, y3));
        path.getElements().add(new ArcTo(innerR, innerR, 0, x4, y4, angleDeg > 180, false));
        path.getElements().add(new ClosePath());
    
      //  path.setMouseTransparent(false);
        return path;
    }

    private double getWidestOpenAngleDeg() {
        // === Normalize spread to [0, 720) ===
        double spreadStart = normalize360(spreadStartAngle);
        double spreadEnd = normalize360(spreadEndAngle);
        if (spreadEnd <= spreadStart) spreadEnd += 360;
    
      //  System.out.printf("🌀 Normalized spread: [%.2f°, %.2f°]%n", spreadStart, spreadEnd);
    
        // === Normalize label angles and shift forward if needed ===
        double[] labelArcs = {
            normalize360(nameStartAngle), normalize360(nameEndAngle),
            normalize360(addressStartAngle), normalize360(addressEndAngle)
        };
    
        for (int i = 0; i < labelArcs.length; i++) {
            if (labelArcs[i] < spreadStart) labelArcs[i] += 360;
        }
    
        // === Clip occupied arcs to within spread ===
        List<double[]> occupiedArcs = new ArrayList<>();
        for (int i = 0; i < labelArcs.length; i += 2) {
            double start = labelArcs[i];
            double end = labelArcs[i + 1];
            if (end <= start) end += 360;
    
            if (end > spreadStart && start < spreadEnd) {
                double clippedStart = Math.max(start, spreadStart);
                double clippedEnd = Math.min(end, spreadEnd);
                occupiedArcs.add(new double[]{clippedStart, clippedEnd});
            }
        }
    
        // === Sort arcs for gap detection ===
        occupiedArcs.sort(Comparator.comparingDouble(a -> a[0]));
    
        /*
        System.out.println("📌 Occupied Arcs:");
        for (double[] arc : occupiedArcs) {
            System.out.printf("   🔻 [%.2f°, %.2f°]%n", arc[0], arc[1]);
        } */
    
        // === Find widest open segment ===
        double bestWidth = -1;
        double bestCenter = (spreadStart + spreadEnd) / 2;
        double prevEnd = spreadStart;
    
        for (double[] arc : occupiedArcs) {
            double gapStart = prevEnd;
            double gapEnd = arc[0];
    
            if (gapEnd > gapStart) {
                double gapWidth = gapEnd - gapStart;
                if (gapWidth > bestWidth) {
                    bestWidth = gapWidth;
                    bestCenter = (gapStart + gapEnd) / 2;
                }
            }
    
            prevEnd = Math.max(prevEnd, arc[1]);
        }
    
        // Final trailing gap
        if (prevEnd < spreadEnd) {
            double gapWidth = spreadEnd - prevEnd;
            if (gapWidth > bestWidth) {
                bestWidth = gapWidth;
                bestCenter = (prevEnd + spreadEnd) / 2;
            }
        }
    
        openingSpan = bestWidth;
        double derivedDeg = normalize180(bestCenter);
      //  System.out.printf("🟤 derivedOpenAngleDeg  : %.2f° (from %.2f° wide gap)%n", derivedDeg, bestWidth);
        return derivedDeg;
    }

    private void secondaryLiftTypeAngleAdjustent() {
        // Define the target anchor angles (0° or 180°)
        double target = (Math.abs(openingAngle - 180) < Math.abs(openingAngle - 0)) ? 180 : 0;
    
        // Calculate how far span exceeds the threshold
        double excess = Math.max(0, openingSpan - 75);
    
        // Max allowed adjustment is half of the excess
        double maxAdjust = excess / 2;
    
        // Find how far openingAngle is from the target
        double diffToTarget = target - openingAngle;
    
        // Clamp the adjustment to not overshoot maxAdjust
        double adjustAmount = Math.max(-maxAdjust, Math.min(maxAdjust, diffToTarget));
    
        // Apply adjustment
        openingAngle += adjustAmount;
    
        // Optional: normalize the final angle to [-180, 180] or [0, 360]
        openingAngle = normalize180(openingAngle);
    
   /*     System.out.printf("🔧 Adjusted openingAngle to %.2f° (moved %.2f° toward %d°)%n", 
                          openingAngle, adjustAmount, (int) target);  */
    }
    
        
    private void addImageAtAngle(String imageResourcePath, double angleDeg, double radius, double size) {
        try {
            String imagePath = getClass().getResource(imageResourcePath).toExternalForm();
            Image image = new Image(imagePath);
            addImageAtAngle(image, angleDeg, radius, size); // Delegate
        } catch (Exception e) {
            System.err.println("❌ Failed to load image: " + imageResourcePath);
            e.printStackTrace();
        }
    }


    private void addImageAtAngle(Image image, double angleDeg, double radius, double size) {
        try {
            Image recoloredImage = recolorImage(image, Color.web(Config.getTertiaryColor()));
            ImageView imageView = new ImageView(recoloredImage);

            imageView.setFitWidth(size);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);

            // Center image on its origin
            imageView.setTranslateX(-imageView.getFitWidth() / 2);
            imageView.setTranslateY(-imageView.getFitHeight() / 2);

            DropShadow glow = new DropShadow();
            glow.setColor(Color.web("#F4F4F4"));
            // imageView.setEffect(glow);

            Group imageGroup = new Group(imageView);

            // === Distance-from-anchor logic ===
            double normalized = normalize180(angleDeg);

            double distTo0 = Math.abs(normalized - 0);
            double distTo180 = Math.abs(normalized - 180);
            double distToNeg180 = Math.abs(normalized + 180);

            double closestDist = Math.min(distTo0, Math.min(distTo180, distToNeg180));

            // Apply radius expansion rules
            if (closestDist > 25) {
                radius += 5;
            }
            if (closestDist > 40) {
                radius += 10;
            }

            // === Position image in polar coords ===
            double angleRad = Math.toRadians(angleDeg);
            double x = radius * Math.cos(angleRad);
            double y = radius * Math.sin(angleRad);

            imageGroup.setTranslateX(x);
            imageGroup.setTranslateY(y);

            getChildren().add(imageGroup);

        } catch (Exception e) {
            System.err.println(" Failed to process provided image object");
            e.printStackTrace();
        }
    }

    private Image recolorImage(Image original, Color newColor) {
        int width = (int) original.getWidth();
        int height = (int) original.getHeight();
        WritableImage recolored = new WritableImage(width, height);
        PixelReader reader = original.getPixelReader();
        PixelWriter writer = recolored.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixel = reader.getColor(x, y);
                if (pixel.getOpacity() == 0.0) {
                    writer.setColor(x, y, Color.TRANSPARENT);
                } else {
                    // Replace all visible pixels with new color, keeping original opacity
                    writer.setColor(x, y, new Color(
                        newColor.getRed(), newColor.getGreen(), newColor.getBlue(), pixel.getOpacity()
                    ));
                }
            }
        }

        return recolored;
    }

    private Color interpolateColor(Color c1, Color c2, double factor) {
        double r = c1.getRed() * (1 - factor) + c2.getRed() * factor;
        double g = c1.getGreen() * (1 - factor) + c2.getGreen() * factor;
        double b = c1.getBlue() * (1 - factor) + c2.getBlue() * factor;
        double a = c1.getOpacity() * (1 - factor) + c2.getOpacity() * factor;
        return new Color(r, g, b, a);
    }

    private double normalize360(double angle) {
        angle %= 360;
        if (angle < 0) angle += 360;
        return angle;
    }
    
    private double normalize180(double angle) {
        angle = normalize360(angle);
        return (angle > 180) ? angle - 360 : angle;
    }

    
    private List<Rental> fetchRelatedRentals(Rental baseRental) {
        List<Rental> rentals = new ArrayList<>();

        // SQL to fetch all rentals with the same order ID, delivery date, and delivery time
        String query = """
            SELECT ro.customer_id, c.customer_name, ri.item_delivery_date, ri.item_call_off_date, ro.po_number,
                ordered_contacts.first_name AS ordered_contact_name, ordered_contacts.phone_number AS ordered_contact_phone,
                ri.auto_term, ro.site_name, ro.street_address, ro.city, ri.rental_item_id, ri.item_status, l.lift_type,
                l.serial_number, ro.single_item_order, ri.rental_order_id, ro.longitude, ro.latitude,
                site_contacts.first_name AS site_contact_name, site_contacts.phone_number AS site_contact_phone,
                ri.driver, ri.driver_number, ri.driver_initial, ri.delivery_truck, ri.pick_up_truck, ri.delivery_time, 
                ri.invoice_composed
            FROM customers c
            JOIN rental_orders ro ON c.customer_id = ro.customer_id
            JOIN rental_items ri ON ro.rental_order_id = ri.rental_order_id
            JOIN lifts l ON ri.lift_id = l.lift_id
            LEFT JOIN contacts AS ordered_contacts ON ri.ordered_contact_id = ordered_contacts.contact_id
            LEFT JOIN contacts AS site_contacts ON ri.site_contact_id = site_contacts.contact_id
            WHERE ri.rental_order_id = ?
            AND ri.item_delivery_date = ?
            AND ri.delivery_time = ?
        """;

        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, baseRental.getRentalOrderId());
            preparedStatement.setString(2, baseRental.getDeliveryDate());
            preparedStatement.setString(3, baseRental.getDeliveryTime());

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    Rental r = new Rental(
                        rs.getString("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("item_delivery_date"),
                        rs.getString("item_call_off_date"),
                        rs.getString("po_number"),
                        rs.getString("ordered_contact_name"),
                        rs.getString("ordered_contact_phone"),
                        rs.getBoolean("auto_term"),
                        rs.getString("site_name"),
                        rs.getString("street_address"),
                        rs.getString("city"),
                        rs.getInt("rental_item_id"),
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
                    r.setDriver(rs.getString("driver"));
                    r.setDriverInitial(rs.getString("driver_initial"));
                    r.setDriverNumber(rs.getInt("driver_number"));
                    r.setDeliveryTruck(rs.getString("delivery_truck"));
                    r.setPickUpTruck(rs.getString("pick_up_truck"));
                    r.setDeliveryTime(rs.getString("delivery_time"));
                    r.setInvoiceComposed(rs.getBoolean("invoice_composed"));
                    r.decapitalizeLiftType();
                    rentals.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rentals;
    }

    private Image buildCompoundPeekImage(List<String> liftTypes) {
        try {
            if (liftTypes.isEmpty()) {
                return null;
            }
    
            List<BufferedImage> croppedImages = new ArrayList<>();
    
            for (String liftType : liftTypes) {
                BufferedImage cropped = loadAndCropLiftType(liftType);
                if (cropped != null) {
                    croppedImages.add(cropped);
                }
            }
    
            if (croppedImages.isEmpty()) {
                return null;
            }
    
            int totalHeight = croppedImages.stream().mapToInt(BufferedImage::getHeight).sum();
            int width = croppedImages.get(0).getWidth();
    
            BufferedImage finalImage = new BufferedImage(width, totalHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = finalImage.createGraphics();
    
            int y = 0;
            for (BufferedImage img : croppedImages) {
                g2d.drawImage(img, 0, y, null);
                y += img.getHeight();
            }
            g2d.dispose();
    
            return SwingFXUtils.toFXImage(finalImage, null);
    
        } catch (Exception e) {
            System.err.println("Failed to build compound peek image");
            e.printStackTrace();
            return null;
        }
    }
    

    private Image buildServiceCompoundPeekImage(String serviceImageName, Rental rental) {
        try {
            List<BufferedImage> stackedImages = new ArrayList<>();
            // --- Load service image (top, unmodified) ---
            String servicePath = "/images/" + serviceImageName;
            System.out.println("about to load service image at: " + servicePath);
            Image fxServiceImage = new Image(getClass().getResource(servicePath).toExternalForm());
            BufferedImage serviceBImage = SwingFXUtils.fromFXImage(fxServiceImage, null);
            stackedImages.add(serviceBImage);
    
            // --- Special case: "change-out.png" ---
            if ("change-out.png".equalsIgnoreCase(serviceImageName)) {
                List<BufferedImage> rowImages = new ArrayList<>();
    
                // 1. Old lift type
                if (rental.getLiftType() != null && !rental.getLiftType().isBlank()) {
                    rowImages.add(loadAndCropLiftType(rental.getLiftType()));
                }
    
                // 2. Arrow image
                String arrowPath = "/images/arrow.png";
                Image fxArrow = new Image(getClass().getResource(arrowPath).toExternalForm());
                rowImages.add(SwingFXUtils.fromFXImage(fxArrow, null));
    
                // 3. New lift type
                if (rental.getService() != null &&
                    rental.getService().getNewLiftType() != null &&
                    !rental.getService().getNewLiftType().isBlank()) {
                    rowImages.add(loadAndCropLiftType(rental.getService().getNewLiftType()));
                }
    
                // --- Scale row images proportionally so total width = service image width ---
                int targetTotalWidth = serviceBImage.getWidth();
                int totalOriginalWidth = rowImages.stream().mapToInt(BufferedImage::getWidth).sum();
    
                List<BufferedImage> scaledRow = new ArrayList<>();
                int rowHeight = 0;
                for (BufferedImage img : rowImages) {
                    double scale = (double) targetTotalWidth / totalOriginalWidth;
                    int newW = (int) (img.getWidth() * scale);
                    int newH = (int) (img.getHeight() * scale);
    
                    BufferedImage scaled = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = scaled.createGraphics();
                    g2.drawImage(img, 0, 0, newW, newH, null);
                    g2.dispose();
    
                    scaledRow.add(scaled);
                    rowHeight = Math.max(rowHeight, newH);
                }
    
                // --- Merge row into single row image ---
                BufferedImage rowCombined = new BufferedImage(targetTotalWidth, rowHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gRow = rowCombined.createGraphics();
                int x = 0;
                for (BufferedImage img : scaledRow) {
                    gRow.drawImage(img, x, 0, null);
                    x += img.getWidth();
                }
                gRow.dispose();
    
                stackedImages.add(rowCombined);
            }
            // --- Normal case: one liftType image ---
            else if (rental.getLiftType() != null && !rental.getLiftType().isBlank()) {
                BufferedImage liftTypeImg = loadAndCropLiftType(rental.getLiftType());
    
                // Scale to service image width
                int targetWidth = serviceBImage.getWidth();
                int targetHeight = (int) ((double) liftTypeImg.getHeight() * targetWidth / liftTypeImg.getWidth());
    
                BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = scaled.createGraphics();
                g2.drawImage(liftTypeImg, 0, 0, targetWidth, targetHeight, null);
                g2.dispose();
    
                stackedImages.add(scaled);
            }
    
            // --- Stack all vertically (service + row or service + liftType) ---
            int totalHeight = stackedImages.stream().mapToInt(BufferedImage::getHeight).sum();
            int width = serviceBImage.getWidth();
            BufferedImage finalImage = new BufferedImage(width, totalHeight, BufferedImage.TYPE_INT_ARGB);
    
            Graphics2D gFinal = finalImage.createGraphics();
            int y = 0;
            for (BufferedImage img : stackedImages) {
                gFinal.drawImage(img, 0, y, null);
                y += img.getHeight();
            }
            gFinal.dispose();
    
            return SwingFXUtils.toFXImage(finalImage, null);
    
        } catch (Exception e) {
            System.err.println("Failed to build service compound peek image");
            e.printStackTrace();
            return null;
        }
    }

    private BufferedImage loadAndCropLiftType(String liftType) throws Exception {
        String path = "/images/" + liftType + "-peek.png";
        System.out.println("about to get the path for cropping: " + path);
        Image fxImage = new Image(getClass().getResource(path).toExternalForm());
        BufferedImage bImage = SwingFXUtils.fromFXImage(fxImage, null);
    
        int cropHeight = (int) (bImage.getHeight() * 0.42);
        return bImage.getSubimage(0, 0, bImage.getWidth(), cropHeight);
    }
    

    private BufferedImage loadBufferedImage(String resourcePath) throws IOException {
        return ImageIO.read(getClass().getResource(resourcePath));
    }

    private Image loadImage(String resourcePath) {
        try {
            return SwingFXUtils.toFXImage(loadBufferedImage(resourcePath), null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    
}

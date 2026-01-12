package com.MaxHighReach;

import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PopupCard extends StackPane {
    private boolean isDriveTimePopup = false;

    public PopupCard(Rental rental, double x, double y) {
        int cardWidth = 238;
        // Fixed width
        setPrefWidth(cardWidth);
        setMinWidth(cardWidth);
        setMaxWidth(cardWidth);

        // Background and border styling
        setStyle(
            "-fx-background-color: #f4f4f4;" +
            "-fx-background-radius: 10;" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 4;" +
            "-fx-border-insets: 0;" +
            "-fx-border-color: transparent;" +
            "-fx-border-image-source: linear-gradient(to bottom right, "
                + Config.getSecondaryColor() + ", derive(" + Config.getSecondaryColor() + ", -20%));" +
            "-fx-border-image-slice: 1;" +
            "-fx-border-image-repeat: stretch;"
        );
        setEffect(new DropShadow(10, Color.GRAY));

        Rectangle clip = new Rectangle(cardWidth, 250);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        setClip(clip);

        int runningYOffset = 7;
        int varXOffset = 51;
        int labelXOffset = 11;

        StackPane contentBoxx = new StackPane();
        contentBoxx.setAlignment(Pos.TOP_LEFT);

        // Rental name
        Label title = new Label(Config.CUSTOMER_NAME_MAP.getOrDefault(rental.getName(),rental.getName()));
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        contentBoxx.getChildren().add(title);
        title.setTranslateX(labelXOffset);
        title.setTranslateY(runningYOffset);

        // Rental Item ID in top-right corner with fancy border box
        Label idLabel = new Label("P" + rental.getRentalItemId());
        idLabel.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-padding: 0 4 0 4;" +
            "-fx-border-color: " + Config.getPrimaryColor() + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-text-fill: " + Config.getTertiaryColor() + ";" +
            "-fx-alignment: center;"
        );

        contentBoxx.getChildren().add(idLabel);
        StackPane.setAlignment(idLabel, Pos.TOP_RIGHT);

        double yOffset = runningYOffset + 1;
        idLabel.setTranslateY(yOffset);
        idLabel.setTranslateX(-8);

        // --- Status symbol ---
        Node statusNode = createStatusDot(rental);
        contentBoxx.getChildren().add(statusNode);
        StackPane.setAlignment(statusNode, Pos.TOP_RIGHT);

        // IMPORTANT: wait for layout pass
        idLabel.applyCss();
        idLabel.layout();

        double labelWidth = idLabel.getLayoutBounds().getWidth();

        // Base offsets
        double baseY = yOffset + 2;
        double baseX = -8 - labelWidth - 50;

        // 🔑 Only bump change-out icons
        boolean isChangeOut =
                "Change Out Completed".equals(rental.getStatus()) ||
                "Service Change Out Completed".equals(rental.getStatus());

        statusNode.setTranslateY(baseY - (isChangeOut ? 2 : 0));
        statusNode.setTranslateX(baseX);


        Label addressLabel = null;

        // Address Block One
        if (rental.getAddressBlockOne() != null && !rental.getAddressBlockOne().isEmpty()) {
            Label l = new Label(rental.getAddressBlockOne());
            runningYOffset += 23;
            l.setTranslateX(varXOffset);
            l.setTranslateY(runningYOffset);
            l.setStyle("-fx-font-size: 14px;");
            contentBoxx.getChildren().add(l);
            addressLabel = new Label("@:");
            contentBoxx.getChildren().add(addressLabel);
            addressLabel.setTranslateY(runningYOffset);
            addressLabel.setTranslateX(labelXOffset);
            addressLabel.setStyle("-fx-font-size: 14px;");

        }

        

        // Address Block Two
        if (rental.getAddressBlockTwo() != null && !rental.getAddressBlockTwo().isEmpty()) {
            runningYOffset += 14;
            if (addressLabel == null) {
                runningYOffset +=9;                
                addressLabel = new Label("@:");
                contentBoxx.getChildren().add(addressLabel);
                addressLabel.setTranslateY(runningYOffset);
                addressLabel.setTranslateX(labelXOffset);
                addressLabel.setStyle("-fx-font-size: 14px;");
            }
            Label l = new Label(rental.getAddressBlockTwo());
            l.setTranslateY(runningYOffset);
            l.setTranslateX(varXOffset);
            l.setStyle("-fx-font-size: 14px;");
            contentBoxx.getChildren().add(l);

        }

        // City / Address Block Three
        if (rental.getCity() != null && !rental.getCity().isEmpty()) {
            Label l = new Label(rental.getAddressBlockThree());
            runningYOffset += 14;
            l.setTranslateY(runningYOffset);
            l.setTranslateX(varXOffset);
            l.setStyle("-fx-font-size: 14px;");
            contentBoxx.getChildren().add(l);
        }


        // PO Number
        if (rental.getPoNumber() != null && !rental.getPoNumber().isEmpty()) {
            Label l = new Label(rental.getPoNumber());
            runningYOffset += 16;
            l.setTranslateY(runningYOffset);
            l.setTranslateX(varXOffset);
            l.setStyle("-fx-font-size: 14px;");

            contentBoxx.getChildren().add(l);

            Label poLabel = new Label("po#:");
            contentBoxx.getChildren().add(poLabel);
            poLabel.setTranslateY(runningYOffset);
            poLabel.setTranslateX(labelXOffset);
            poLabel.setStyle("-fx-font-size: 14px;");

        }



        // Ordered By
        if (rental.getOrderedByName() != null && !rental.getOrderedByName().isEmpty()) {
            String phone = formatPhone(rental.getOrderedByPhone());

            runningYOffset += 16;

            Label askLabel = new Label("ask:");
            askLabel.setTranslateY(runningYOffset);
            askLabel.setTranslateX(labelXOffset);
            askLabel.setStyle("-fx-font-size: 14px;");


            contentBoxx.getChildren().add(askLabel);

            Label askValue = new Label(rental.getOrderedByName() +
                                    (phone.isEmpty() ? "" : " " + phone));
            askValue.setTranslateY(runningYOffset);
            askValue.setTranslateX(varXOffset);
            askValue.setStyle("-fx-font-size: 14px;");

            contentBoxx.getChildren().add(askValue);
        }

        // Site Contact
        if (rental.getSiteContactName() != null && !rental.getSiteContactName().isEmpty()) {
            String phone = formatPhone(rental.getSiteContactPhone());

            runningYOffset += 16;

            Label siteLabel = new Label("site:");
            siteLabel.setTranslateY(runningYOffset);
            siteLabel.setTranslateX(labelXOffset);
            siteLabel.setStyle("-fx-font-size: 14px;");


            contentBoxx.getChildren().add(siteLabel);

            Label siteValue = new Label(rental.getSiteContactName() +
                                    (phone.isEmpty() ? "" : " " + phone));
            siteValue.setTranslateY(runningYOffset);
            siteValue.setTranslateX(varXOffset);
            siteValue.setStyle("-fx-font-size: 14px;");
            contentBoxx.getChildren().add(siteValue);
        }

        if (rental.getOrderDate() != null) {

            String tag = rental.getOrderDate(); // OLD:___;NEW:___
            String oldSerial = null;
            String newSerial = null;

            if (tag != null && tag.contains(";")) {
                String[] parts = tag.split(";");
                for (String part : parts) {
                    if (part.startsWith("OLD:")) {
                        String val = part.substring(4);
                        if (!"NULL".equalsIgnoreCase(val)) {
                            oldSerial = val;
                        }
                    } else if (part.startsWith("NEW:")) {
                        String val = part.substring(4);
                        if (!"NULL".equalsIgnoreCase(val)) {
                            newSerial = val;
                        }
                    }
                }
            }

            // --- Changed FROM line ---
            if (oldSerial != null) {
                Label fromLabel = new Label("Changed from " + oldSerial);
                runningYOffset += 15;
                fromLabel.setTranslateY(runningYOffset);
                fromLabel.setTranslateX(varXOffset);
                fromLabel.setStyle("-fx-font-size: 14px;");
                contentBoxx.getChildren().add(fromLabel);
            }

            // --- Changed TO line ---
            if (newSerial != null) {
                Label toLabel = new Label("Changed to " + newSerial);
                runningYOffset += 15;
                toLabel.setTranslateY(runningYOffset);
                toLabel.setTranslateX(varXOffset);
                toLabel.setStyle("-fx-font-size: 14px;");
                contentBoxx.getChildren().add(toLabel);
            }
        }

        // Duration (business days)
        if (rental.getDeliveryDate() != null && !rental.getDeliveryDate().isEmpty()) {
            LocalDate start = LocalDate.parse(rental.getDeliveryDate());
            LocalDate end;

            if (rental.getCallOffDate() != null && !rental.getCallOffDate().isEmpty()) {
                end = LocalDate.parse(rental.getCallOffDate());
            } else {
                LocalDate today = LocalDate.now();
                LocalTime cutoff = LocalTime.of(10, 30);
                if (isBusinessDay(today)) {
                    if (LocalTime.now().isAfter(cutoff)) {
                        end = today; // include today
                    } else {
                        end = getMostRecentBusinessDay(today.minusDays(1));
                    }
                } else {
                    end = getMostRecentBusinessDay(today.minusDays(1));
                }
            }

            int businessDays = countBusinessDays(start, end);
            String durationString = formatDuration(businessDays);

            runningYOffset += 15;

            Label durationLabel = new Label("rent:");
            durationLabel.setTranslateY(runningYOffset);
            durationLabel.setTranslateX(labelXOffset);
            durationLabel.setStyle("-fx-font-size: 14px;");
            contentBoxx.getChildren().add(durationLabel);

            Label durationValue = new Label(durationString);
            durationValue.setTranslateY(runningYOffset);
            durationValue.setTranslateX(varXOffset);
            durationValue.setStyle("-fx-font-size: 14px;");         
            contentBoxx.getChildren().add(durationValue);
        }


        // Add content box
        getChildren().add(contentBoxx);

        setLayoutX(10);
        setLayoutY(y - 50);

        setMinHeight(runningYOffset + 35);
        setMaxHeight(runningYOffset + 35);
        setPrefHeight(runningYOffset + 35);

        int borderThickness = 6;
        int marginToBorder = 4;

        // --- Top border ---
        Rectangle topBorder = new Rectangle();
        topBorder.setWidth(cardWidth);
        topBorder.setHeight(borderThickness);
        topBorder.setFill(new LinearGradient(
            0, 0, 1, 0,  // Left → Right
            true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web(Config.getSecondaryColor())),  // Left
            new Stop(1, Color.web(Config.getPrimaryColor()))     // Right
        ));
        getChildren().add(topBorder);
        StackPane.setAlignment(topBorder, Pos.TOP_CENTER);
        topBorder.setTranslateY(-marginToBorder);
        topBorder.setArcWidth(10);
        topBorder.setArcHeight(20);
        topBorder.toBack();

        // --- Left border ---
        Rectangle leftBorder = new Rectangle();
        leftBorder.setWidth(borderThickness);
        leftBorder.setArcWidth(20);
        leftBorder.setArcHeight(10);
        leftBorder.setTranslateX(-marginToBorder);
        leftBorder.setHeight(runningYOffset + 32);
        leftBorder.setFill(new LinearGradient(
            0, 0, 0, 1,  // Top → Bottom
            true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web(Config.getSecondaryColor())), // Top
            new Stop(1, Color.web("#F4F4F4"))                   // Bottom
        ));
        getChildren().add(leftBorder);
        StackPane.setAlignment(leftBorder, Pos.CENTER_LEFT);
        leftBorder.toBack();

        // --- Right border (Primary → Secondary, vertical) ---
        Rectangle rightBorder = new Rectangle();
        rightBorder.setWidth(borderThickness);
        rightBorder.setArcWidth(20);
        rightBorder.setArcHeight(10);
        rightBorder.setTranslateX(marginToBorder);
        rightBorder.setHeight(runningYOffset + 32);
        rightBorder.setFill(new LinearGradient(
            0, 0, 1, 1,  // Top → Bottom
            true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web(Config.getPrimaryColor())),   // Top
            new Stop(1, Color.web(Config.getSecondaryColor()))  // Bottom
        ));
        getChildren().add(rightBorder);
        StackPane.setAlignment(rightBorder, Pos.CENTER_RIGHT);
        rightBorder.toBack();

        // --- Bottom border (F4F4F4 → Secondary, horizontal) ---
        Rectangle bottomBorder = new Rectangle();
        bottomBorder.setWidth(cardWidth);
        bottomBorder.setHeight(borderThickness);
        bottomBorder.setFill(new LinearGradient(
            0, 0, 1, 0,  // Left → Right
            true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#F4F4F4")),                  // Left
            new Stop(1, Color.web(Config.getSecondaryColor()))  // Right
        ));
        getChildren().add(bottomBorder);
        StackPane.setAlignment(bottomBorder, Pos.BOTTOM_CENTER);
        bottomBorder.setArcWidth(10);
        bottomBorder.setArcHeight(20);
        bottomBorder.setTranslateY(marginToBorder);
        bottomBorder.toBack();
    
    }

    private Node createStatusDot(Rental rental) {
        String status = rental.getStatus();

        return switch (status) {
            case "Called Off" -> wrapWithTooltip(createOctagon(Color.RED), status);
            case "Picked Up" -> wrapWithTooltip(createCircle(Color.BLACK), status);
            case "Upcoming" -> wrapWithTooltip(createCircle(Color.web(Config.getPrimaryColor())), status);
            case "Active" -> wrapWithTooltip(createCircle(Color.GREEN), status);
            case "Change Out Completed" -> createChangeOutIcon("Change Out");
            case "Service Change Out Completed" -> createChangeOutIcon("Service Change Out");
            default -> wrapWithTooltip(createCircle(Color.GRAY), status);
        };
    }

    private Circle createCircle(Color color) {
        Circle c = new Circle(0, 0, 7);
        c.setFill(color);
        return c;
    }

    private Polygon createOctagon(Color color) {
        double radius = 7;
        Polygon octagon = new Polygon();
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(45 * i + 22.5);
            octagon.getPoints().addAll(
                radius * Math.cos(angle),
                radius * Math.sin(angle)
            );
        }
        octagon.setFill(color);
        return octagon;
    }

    private Node createChangeOutIcon(String serviceType) {
        String imagePath = switch (serviceType) {
            case "Change Out" -> "/images/change-out.png";
            case "Service Change Out" -> "/images/service-change-out.png";
            default -> "/images/service.png";
        };

        URL url = getClass().getResource(imagePath);
        if (url == null) {
            System.err.println("❌ Resource not found: " + imagePath);
            return createCircle(Color.GRAY);
        }

        ImageView imageView = new ImageView(new Image(url.toExternalForm()));
        imageView.setFitWidth(20);
        imageView.setPreserveRatio(true);

        // 🔑 This makes alpha pixels clickable
        imageView.setPickOnBounds(true);

        StackPane wrapper = new StackPane(imageView);
        wrapper.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        wrapper.setPrefSize(20, 20);
        wrapper.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        wrapper.setPickOnBounds(true);

        Tooltip tooltip = new Tooltip("Changed Out");
        attachTooltip(wrapper, tooltip, 8, 6);

        return wrapper;
    }

    private void attachTooltip(Node node, Tooltip tooltip, double xOffset, double yOffset) {
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setHideDelay(Duration.ZERO);

        node.setOnMouseEntered(event -> {
            Bounds bounds = node.localToScreen(node.getBoundsInLocal());
            tooltip.show(
                node,
                bounds.getMinX() + xOffset,
                bounds.getMaxY() + yOffset
            );
        });

        node.setOnMouseExited(event -> tooltip.hide());
    }

    private Node wrapWithTooltip(Shape shape, String tooltipText) {
        shape.setPickOnBounds(true);

        StackPane wrapper = new StackPane(shape);
        wrapper.setAlignment(Pos.CENTER);

        // 🔑 Prevent StackPane from expanding (same fix as icons)
        wrapper.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        wrapper.setPrefSize(16, 16);
        wrapper.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        wrapper.setPickOnBounds(true);

        Tooltip tooltip = new Tooltip(tooltipText);
        attachTooltip(wrapper, tooltip, 8, 6);

        return wrapper;
    }


    private String formatPhone(String phone) {
        if (phone == null || phone.length() != 10) return "";
        return "(" + phone.substring(0, 3) + ") " +
               phone.substring(3, 6) + "-" +
               phone.substring(6, 10);
    }

    private boolean isBusinessDay(LocalDate date) {
        return !(date.getDayOfWeek().getValue() >= 6 || Config.COMPANY_HOLIDAYS.contains(date));
    }

    private LocalDate getMostRecentBusinessDay(LocalDate date) {
        while (!isBusinessDay(date)) {
            date = date.minusDays(1);
        }
        return date;
    }

    private int countBusinessDays(LocalDate start, LocalDate end) {
        int count = 0;
        LocalDate d = start;
        while (!d.isAfter(end)) {
            if (isBusinessDay(d)) count++;
            d = d.plusDays(1);
        }
        return count;
    }

    private String formatDuration(int businessDays) {
        int months = businessDays / 20; // 4 weeks per month
        int remainder = businessDays % 20;
        int weeks = remainder / 5;
        int days = remainder % 5;

        StringBuilder sb = new StringBuilder();
        if (months > 0) sb.append(months).append(months == 1 ? " month" : " months");
        if (weeks > 0) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(weeks).append(weeks == 1 ? " week" : " weeks");
        }
        if (days > 0) {
            if (sb.length() > 0) sb.append(", & ");
            sb.append(days).append(days == 1 ? " day" : " days");
        }
        return sb.toString();
    }
}

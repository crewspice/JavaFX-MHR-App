package com.MaxHighReach;

import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
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
            "-fx-font-size: 13px;" +                 // slightly smaller font
            "-fx-padding: 0 4 0 4;" +                // top/right/bottom/left -> minimal vertical padding
            "-fx-border-color: " + Config.getPrimaryColor() + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-text-fill: " + Config.getPrimaryColor() + ";" +
            "-fx-alignment: center;"                 // centers text inside box
        );
        contentBoxx.getChildren().add(idLabel);
        StackPane.setAlignment(idLabel, Pos.TOP_RIGHT);
        idLabel.setTranslateY(runningYOffset + 1); // small nudge down if needed
        idLabel.setTranslateX(-8);

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


        // Status
        String status = rental.getStatus();
        if (status != null && !status.isEmpty()) {
            Label statusLabel = new Label(status);
            if ("Active".equalsIgnoreCase(status) && rental.isAutoTerm()) {
                String callOff = rental.getCallOffDate();
                if (callOff != null && !callOff.isEmpty()) {
                    LocalDate date = LocalDate.parse(callOff);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                    statusLabel.setText(statusLabel.getText() +
                        " | Auto term end: " + date.format(formatter));
                }
            }
            runningYOffset += 15;
            statusLabel.setTranslateY(runningYOffset);
            statusLabel.setTranslateX(varXOffset);
            statusLabel.setStyle("-fx-font-size: 14px;");
            contentBoxx.getChildren().add(statusLabel);
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

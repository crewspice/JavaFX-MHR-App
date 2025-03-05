package com.MaxHighReach;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.scene.input.MouseEvent;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/* This class represents the graphic scissor lift on the app stage
*  The Lift class represents the data structure for single object lifts in the MHR inventory. */
public class ScissorLift extends Pane {
    private static final int ARM_COUNT = 6;
    private static final double WHEEL_RADIUS = 30;
    private final Circle WHEEL_1 = createWheel1();
    private Line[] arm1, arm2;
    private Line[] arm1Borders, arm2Borders; // Add references for borders
    private Rectangle[] basket;

    public ScissorLift() {
        drawLift(-100);
    }

    public ScissorLift(double drawHeight) {
        drawLift(-drawHeight);
    }

    private Circle createWheel1() {
        Circle wheel1 = new Circle(WHEEL_RADIUS, Color.GRAY);
        wheel1.setCenterX(WHEEL_RADIUS + Config.PADDING);
        wheel1.setCenterY(Config.WINDOW_HEIGHT - Config.PADDING - WHEEL_RADIUS);
        wheel1.setOnMouseClicked(ScissorLift::handleWheelClick);
        return wheel1;
    }

    private void drawLift(double drawHeight) {
        double width = Config.SCISSOR_LIFT_WIDTH;
        double baseHeight = WHEEL_RADIUS;

        getChildren().clear();

        Circle wheel2 = new Circle(WHEEL_RADIUS, Color.GRAY);
        wheel2.setCenterX(width - WHEEL_RADIUS - Config.PADDING);
        wheel2.setCenterY(Config.WINDOW_HEIGHT - Config.PADDING - WHEEL_RADIUS);

        double rectYCoordinate = WHEEL_RADIUS * 2.7;
        Rectangle base = new Rectangle(Config.PADDING, Config.WINDOW_HEIGHT - rectYCoordinate,
                Config.SCISSOR_LIFT_WIDTH - (Config.PADDING * 2), WHEEL_RADIUS * 1.8);
        base.setFill(Color.ORANGE);

        arm1 = new Line[ARM_COUNT];
        arm2 = new Line[ARM_COUNT];
        arm1Borders = new Line[ARM_COUNT]; // Initialize border arrays
        arm2Borders = new Line[ARM_COUNT];
        double armWidth = 9;
        double borderWidth = armWidth + 2;
        double lastArm = Config.WINDOW_HEIGHT - rectYCoordinate;
        double armSpacing = drawHeight / ARM_COUNT;

        for (int i = 0; i < ARM_COUNT; i++) {
            // Only add borders for arms that are not the bottom two
            if (i < ARM_COUNT) {
            // Create arm1 border (grey)
            arm1Borders[i] = new Line(Config.PADDING + 15, lastArm,
                                       width - Config.PADDING - 15,
                                       lastArm - armSpacing);
            arm1Borders[i].setStroke(Color.GREY);
            arm1Borders[i].setStrokeWidth(borderWidth);

            // Create arm2 border (grey)
            arm2Borders[i] = new Line(Config.PADDING + 15, lastArm - armSpacing,
                                       width - Config.PADDING - 15, lastArm);
            arm2Borders[i].setStroke(Color.GREY);
            arm2Borders[i].setStrokeWidth(borderWidth);

         }

            // Create arm1 (actual line with color)
            arm1[i] = new Line(Config.PADDING + 15, lastArm,
                               width - Config.PADDING - 15, lastArm - armSpacing);
            arm1[i].setStroke(Color.web("#FFDEAD"));
            arm1[i].setStrokeWidth(armWidth);


            // Create arm2 (actual line with color)
            arm2[i] = new Line(Config.PADDING + 15, lastArm - armSpacing,
                               width - Config.PADDING - 15, lastArm);
            arm2[i].setStroke(Color.web("#FFDEAD"));
            arm2[i].setStrokeWidth(armWidth);


            // Update the max arm height
            lastArm += armSpacing;
        }

        getChildren().addAll(arm1Borders);
        getChildren().addAll(arm1);
        getChildren().addAll(arm2Borders);
        getChildren().addAll(arm2);

        lastArm = lastArm - armSpacing - (armWidth * 2);
        getChildren().remove(arm1[0]);
        getChildren().remove(arm2[0]);
        getChildren().remove(arm1Borders[0]);
        getChildren().remove(arm2Borders[0]);

        Rectangle platform = new Rectangle(Config.PADDING, lastArm,
                width - (Config.PADDING * 2), Config.PADDING * 2);
        platform.setFill(Color.ORANGE);

        double railHeight = 30;
        double railWidth = 5;

        Rectangle verticalRail1 = new Rectangle(Config.PADDING, lastArm - (railHeight * 2),
                railWidth, railHeight * 2);
        verticalRail1.setFill(Color.ORANGE);

        Rectangle verticalRail2 = new Rectangle(width / 2 - 5, lastArm - (railHeight * 2),
                railWidth, railHeight * 2);
        verticalRail2.setFill(Color.ORANGE);

        Rectangle verticalRail3 = new Rectangle(Config.WINDOW_WIDTH - Config.PADDING - railWidth,
                lastArm - (railHeight * 2), railWidth, railHeight * 2);
        verticalRail3.setFill(Color.ORANGE);

        Rectangle horizontalRail1 = new Rectangle(Config.PADDING, lastArm - railHeight,
                width - (Config.PADDING * 2), railWidth);
        horizontalRail1.setFill(Color.ORANGE);

        Rectangle horizontalRail2 = new Rectangle(Config.PADDING, lastArm - (railHeight * 2),
                width - (Config.PADDING * 2), railWidth);
        horizontalRail2.setFill(Color.ORANGE);

        basket = new Rectangle[]{verticalRail1, verticalRail2, verticalRail3, horizontalRail1, horizontalRail2,
                platform};

        getChildren().addAll(basket);
        getChildren().addAll(base, WHEEL_1, wheel2);
    }

    public Circle getWheel1() {
        return WHEEL_1;
    }

    private static void handleWheelClick(MouseEvent event) {
        try {
            URI uri = new URI("https://github.com/crewspice/Max-High-Reach/tree/main");
            Desktop.getDesktop().browse(uri);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void animateTransition(double newHeight) {
        double measuredHeight = 0;
        Timeline timeline = new Timeline();

        measuredHeight = basket[5].getY() - Config.WINDOW_HEIGHT;

        double heightDifference = measuredHeight + newHeight;
        double durationInSeconds = 1.0;


        // Animate the basket
        for (Rectangle rectangle : basket) {
            double currentY = rectangle.getY();
            double newY = currentY - heightDifference;

            KeyFrame keyFrame = new KeyFrame(Duration.seconds(durationInSeconds),
                    new javafx.animation.KeyValue(rectangle.yProperty(), newY)
            );

            timeline.getKeyFrames().add(keyFrame);
        }

        // Animate arms and their borders
        for (int i = 0; i < ARM_COUNT; i++) {
            double currentProportion = (double) (i - 1) / (ARM_COUNT - 1);
            double newProportion = (double) (i) / (ARM_COUNT - 1);
            double armCurrentMovement = heightDifference * currentProportion;
            double armNewMovement = heightDifference * newProportion;

            // Animate arm 1
            KeyFrame arm1KeyFrame = new KeyFrame(Duration.seconds(durationInSeconds),
                    new javafx.animation.KeyValue(arm1[i].startYProperty(), arm1[i].getStartY() - armNewMovement),
                    new javafx.animation.KeyValue(arm1[i].endYProperty(), arm1[i].getEndY() - armCurrentMovement)
            );

            // Animate arm 1 border if it exists
            if (arm1Borders[i] != null) {
                KeyFrame arm1BorderKeyFrame = new KeyFrame(Duration.seconds(durationInSeconds),
                        new javafx.animation.KeyValue(arm1Borders[i].startYProperty(), arm1Borders[i].getStartY() - armNewMovement),
                        new javafx.animation.KeyValue(arm1Borders[i].endYProperty(), arm1Borders[i].getEndY() - armCurrentMovement)
                );
                timeline.getKeyFrames().add(arm1BorderKeyFrame);
            }

            // Animate arm 2
            KeyFrame arm2KeyFrame = new KeyFrame(Duration.seconds(durationInSeconds),
                    new javafx.animation.KeyValue(arm2[i].startYProperty(), arm2[i].getStartY() - armCurrentMovement),
                    new javafx.animation.KeyValue(arm2[i].endYProperty(), arm2[i].getEndY() - armNewMovement)
            );

            // Animate arm 2 border if it exists
            if (arm2Borders[i] != null) {
                KeyFrame arm2BorderKeyFrame = new KeyFrame(Duration.seconds(durationInSeconds),
                        new javafx.animation.KeyValue(arm2Borders[i].startYProperty(), arm2Borders[i].getStartY() - armCurrentMovement),
                        new javafx.animation.KeyValue(arm2Borders[i].endYProperty(), arm2Borders[i].getEndY() - armNewMovement)
                );
                timeline.getKeyFrames().add(arm2BorderKeyFrame);
            }

            // Add all keyframes to the timeline
            timeline.getKeyFrames().addAll(arm1KeyFrame, arm2KeyFrame);
        }

        timeline.setOnFinished(event -> {
            timeline.getKeyFrames().clear();
        });

        timeline.play();

    }

}
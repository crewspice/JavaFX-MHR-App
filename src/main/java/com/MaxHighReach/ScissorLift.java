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

public class ScissorLift extends Pane {
    private static final int ARM_COUNT = 6;
    private static final double WHEEL_RADIUS = 30;
    private static final Circle WHEEL_1 = createWheel1();
    private Line[] arm1, arm2;
    private Rectangle[] basket;

    public ScissorLift() {
        drawLift(-100);
    }

    public ScissorLift(double drawHeight) {
        drawLift(-drawHeight);
    }

    private static Circle createWheel1() {
        Circle wheel1 = new Circle(WHEEL_RADIUS, Color.GRAY);
        wheel1.setCenterX(WHEEL_RADIUS + AppConstants.PADDING); // Positioned close to the left
        wheel1.setCenterY(AppConstants.WINDOW_HEIGHT - AppConstants.PADDING - WHEEL_RADIUS);
        wheel1.setOnMouseClicked(ScissorLift::handleWheelClick);
        return wheel1;
    }

    private void drawLift(double drawHeight) {
        double width = AppConstants.SCISSOR_LIFT_WIDTH;
        double baseHeight = WHEEL_RADIUS;

        // Clear any previous graphics
        getChildren().clear();

        Circle wheel2 = new Circle(WHEEL_RADIUS, Color.GRAY);
        wheel2.setCenterX(width - WHEEL_RADIUS - AppConstants.PADDING); // Positioned close to the right
        wheel2.setCenterY(AppConstants.WINDOW_HEIGHT - AppConstants.PADDING - WHEEL_RADIUS);

        // Lift base (brown) - Position it at the bottom
        double rectYCoordinate = WHEEL_RADIUS * 2.7;
        Rectangle base = new Rectangle(AppConstants.PADDING, AppConstants.WINDOW_HEIGHT - rectYCoordinate,
                AppConstants.SCISSOR_LIFT_WIDTH - (AppConstants.PADDING * 2), WHEEL_RADIUS * 1.8);
        base.setFill(Color.ORANGE);

        // Scissor arms (brown)
        arm1 = new Line[ARM_COUNT];
        arm2 = new Line[ARM_COUNT];
        double armWidth = 9;
        double lastArm = AppConstants.WINDOW_HEIGHT - rectYCoordinate;
        double armSpacing = drawHeight / ARM_COUNT;

        for (int i = 0; i < ARM_COUNT; i++) {
            arm1[i] = new Line(AppConstants.PADDING + 15, lastArm, width - AppConstants.PADDING - 15,
                    lastArm - armSpacing);
            arm1[i].setStroke(Color.TAN);
            arm1[i].setStrokeWidth(armWidth);

            arm2[i] = new Line(AppConstants.PADDING + 15, lastArm - armSpacing,
                    width - AppConstants.PADDING - 15, lastArm);
            arm2[i].setStroke(Color.TAN);
            arm2[i].setStrokeWidth(armWidth);
            // Update the max arm height
            lastArm = lastArm + armSpacing;
        }
        lastArm = lastArm - armSpacing - (armWidth * 2);

        // Add arms to the pane explicitly
        getChildren().addAll(arm1);
        getChildren().addAll(arm2);

        // Remove the first arms because they go below the lift
        getChildren().remove(arm1[0]);
        getChildren().remove(arm2[0]);

        // Lift platform (orange) - Position it above the highest arm
        Rectangle platform = new Rectangle(AppConstants.PADDING, lastArm,
                width - (AppConstants.PADDING * 2), AppConstants.PADDING * 2);
        platform.setFill(Color.ORANGE);

        double railHeight = 30;
        double railWidth = 5;

        // Basket rails
        Rectangle verticalRail1 = new Rectangle(AppConstants.PADDING, lastArm - (railHeight * 2),
                railWidth, railHeight * 2);
        verticalRail1.setFill(Color.ORANGE);

        Rectangle verticalRail2 = new Rectangle(width / 2 - 5, lastArm - (railHeight * 2),
                railWidth, railHeight * 2);
        verticalRail2.setFill(Color.ORANGE);

        Rectangle verticalRail3 = new Rectangle(AppConstants.WINDOW_WIDTH - AppConstants.PADDING - railWidth,
                lastArm - (railHeight * 2), railWidth, railHeight * 2);
        verticalRail3.setFill(Color.ORANGE);

        Rectangle horizontalRail1 = new Rectangle(AppConstants.PADDING, lastArm - railHeight,
                width - (AppConstants.PADDING * 2), railWidth);
        horizontalRail1.setFill(Color.ORANGE);

        Rectangle horizontalRail2 = new Rectangle(AppConstants.PADDING, lastArm - (railHeight * 2),
                width - (AppConstants.PADDING * 2), railWidth);
        horizontalRail2.setFill(Color.ORANGE);

        // Store references to the vertical rails for later animation
        basket = new Rectangle[]{verticalRail1, verticalRail2, verticalRail3, horizontalRail1, horizontalRail2,
                platform};

        // Add shapes to the pane
        getChildren().addAll(basket);
        getChildren().addAll(base, WHEEL_1, wheel2);
    }

    public Circle getWheel1() {
        return WHEEL_1;
    }

    private static void handleWheelClick(MouseEvent event) {
        try {
            URI uri = new URI("https://github.com/crewspice/Max-High-Reach/tree/main");  // Replace with the desired URL
            Desktop.getDesktop().browse(uri);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void animateTransition(double currentHeight, double newHeight) {
        double heightDifference = newHeight - currentHeight;
        double durationInSeconds = 1.0;
        Timeline timeline = new Timeline();

        for (Rectangle rectangle : basket) {
            double currentX = rectangle.getX();
            double newX = currentX;
            double currentY = rectangle.getY();
            double newY = currentY - heightDifference;

            KeyFrame keyFrame = new KeyFrame(Duration.seconds(durationInSeconds),
                    new javafx.animation.KeyValue(rectangle.xProperty(), newX),
                    new javafx.animation.KeyValue(rectangle.yProperty(), newY)
            );

            timeline.getKeyFrames().add(keyFrame);
        }

        for (int i = 0; i < ARM_COUNT; i++) {
            double currentProportion = (double) (i - 1) / (ARM_COUNT - 1);
            double newProportion = (double) (i) / (ARM_COUNT - 1);
            double armCurrentMovement = heightDifference * currentProportion;
            double armNewMovement = heightDifference * newProportion;

            double startY1 = arm1[i].getStartY();
            double endY1 = arm1[i].getEndY();

            KeyFrame arm1KeyFrame = new KeyFrame(Duration.seconds(durationInSeconds),
                    new javafx.animation.KeyValue(arm1[i].startYProperty(), startY1 - armNewMovement),
                    new javafx.animation.KeyValue(arm1[i].endYProperty(), endY1 - armCurrentMovement)
            );

            double startY2 = arm2[i].getStartY();
            double endY2 = arm2[i].getEndY();

            KeyFrame arm2KeyFrame = new KeyFrame(Duration.seconds(durationInSeconds),
                    new javafx.animation.KeyValue(arm2[i].startYProperty(), startY2 - armCurrentMovement),
                    new javafx.animation.KeyValue(arm2[i].endYProperty(), endY2 - armNewMovement)
            );

            timeline.getKeyFrames().addAll(arm1KeyFrame, arm2KeyFrame);
        }

        timeline.play();
    }
}

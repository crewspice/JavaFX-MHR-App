package com.MaxHighReach;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class ScissorLift extends Pane {
    private static final int ARM_COUNT = 6;
    private Line[] arm1, arm2;
    private Rectangle[] basket;

    public ScissorLift() {
        drawLift(-100);
    }

    public ScissorLift(double drawHeight) {
        drawLift(-drawHeight);
    }

    private void drawLift(double drawHeight) {
        double width = AppConstants.SCISSOR_LIFT_WIDTH;
        double wheelRadius = 30;
        double baseHeight = wheelRadius;

        // Clear any previous graphics
        getChildren().clear();

        // Wheels (gray)
        Circle wheel1 = new Circle(wheelRadius, Color.GRAY);
        wheel1.setCenterX(wheelRadius + AppConstants.PADDING); // Positioned close to the left
        wheel1.setCenterY(AppConstants.WINDOW_HEIGHT - AppConstants.PADDING - wheelRadius);

        Circle wheel2 = new Circle(wheelRadius, Color.GRAY);
        wheel2.setCenterX(width - wheelRadius - AppConstants.PADDING); // Positioned close to the right
        wheel2.setCenterY(AppConstants.WINDOW_HEIGHT - AppConstants.PADDING - wheelRadius);

        // Lift base (brown) - Position it at the bottom
        double rectYCoordinate = wheelRadius * 2.7;
        Rectangle base = new Rectangle(AppConstants.PADDING, AppConstants.WINDOW_HEIGHT - rectYCoordinate,
                AppConstants.SCISSOR_LIFT_WIDTH - (AppConstants.PADDING * 2), wheelRadius * 1.8);
        base.setFill(Color.ORANGE);

        // Scissor arms (brown)
        arm1 = new Line[ARM_COUNT];
        arm2 = new Line[ARM_COUNT];
        double armWidth = 9;
        double lastArm = AppConstants.WINDOW_HEIGHT - (rectYCoordinate);
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
        for (Line arm : arm1) {
            getChildren().add(arm); // Add each left arm
        }

        for (Line arm : arm2) {
            getChildren().add(arm); // Add each right arm
        }

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
        verticalRail1.setFill(Color.ORANGE); // Vertical rail color

        Rectangle verticalRail2 = new Rectangle(width / 2 - 5, lastArm - (railHeight * 2),
                railWidth, railHeight * 2);
        verticalRail2.setFill(Color.ORANGE); // Vertical rail color

        Rectangle verticalRail3 = new Rectangle(AppConstants.WINDOW_WIDTH - AppConstants.PADDING - railWidth,
                lastArm - (railHeight * 2), railWidth, railHeight * 2);
        verticalRail3.setFill(Color.ORANGE); // Vertical rail color

        Rectangle horizontalRail1 = new Rectangle(AppConstants.PADDING, lastArm - railHeight,
                width - (AppConstants.PADDING * 2), railWidth);
        horizontalRail1.setFill(Color.ORANGE); // Horizontal rail color

        Rectangle horizontalRail2 = new Rectangle(AppConstants.PADDING, lastArm - (railHeight * 2),
                width - (AppConstants.PADDING * 2), railWidth);
        horizontalRail2.setFill(Color.ORANGE); // Horizontal rail color

        // Store references to the vertical rails for later animation
        basket = new Rectangle[]{verticalRail1, verticalRail2, verticalRail3, horizontalRail1, horizontalRail2,
                platform};

        // Add shapes to the pane
        getChildren().addAll(basket);
        getChildren().addAll(base, wheel1, wheel2);
    }

    public void animateTransition(double currentHeight, double newHeight) {
        double heightDifference = newHeight - currentHeight;

        //double minusOneRate = (ARM_COUNT - 1) / (ARM_COUNT);
        //double proportion = (heightDifference / AppConstants.WINDOW_HEIGHT) * 50;
        //double adjustedProportion = proportion * minusOneRate;

        // Print the calculated proportions and offsets
       // System.out.println("Height Difference: " + heightDifference);
       // System.out.println("Proportion: " + proportion);
        //System.out.println("Adjusted Proportion: " + adjustedProportion);

        double durationInSeconds = 1.0;
        Timeline timeline = new Timeline();

        for (Rectangle rectangle : basket) {
            double currentX = rectangle.getX();
            double newX = currentX;
            double currentY = rectangle.getY();
            double newY = currentY - heightDifference;

            // Print the offsets for basket rectangles
           // System.out.println("Basket - Current Y: " + currentY);
            //System.out.println("Basket - New Y: " + newY);
            //System.out.println("Basket - Offset (25 * proportion): " + (25 * proportion));

            KeyFrame keyFrame = new KeyFrame(Duration.seconds(durationInSeconds),
                new javafx.animation.KeyValue(rectangle.xProperty(), newX),
                new javafx.animation.KeyValue(rectangle.yProperty(), newY)
            );

            timeline.getKeyFrames().add(keyFrame);
        }

        /* TODO: set up calculations for horizontal displacement of animated
        *   scissor lift arms during animation */


        for (int i = 0; i < ARM_COUNT; i++) {
            double currentProportion = (double) (i - 1) / (ARM_COUNT - 1);
            double newProportion = (double) (i) / (ARM_COUNT - 1);
            double armCurrentMovement = heightDifference * currentProportion;
            double armNewMovement = heightDifference * newProportion;

            // Print the offsets for arm movements
           // System.out.println("Arm " + i + " - Current Movement: " + armCurrentMovement);
           // System.out.println("Arm " + i + " - New Movement: " + armNewMovement);

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

package com.MaxHighReach;


import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.TilePane;
import javafx.util.Duration;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ButtonGroup {
   private List<ToggleButton> buttons = new ArrayList<>();
   private static final String STYLE_TEMPLATE =
       "-fx-background-color: radial-gradient(center %f%% 25%%, radius 40%%, orange 0%%, transparent 50%%, hotpink 100%%);";
   private static TilePane buttonPane = new TilePane();


   // Initial positions for center1 and center2
   private final DoubleProperty center1Property = new SimpleDoubleProperty(0.0);
   private final DoubleProperty center2Property = new SimpleDoubleProperty(100.0);


   private Random random = new Random();
   private double lastRandomReturned = 0.0;


   // AnimationTimer for continuous updates
   private AnimationTimer animationTimer;


   // Track the last update time to slow down the animation
   private long lastUpdateTime = 0;


   // Delay in nanoseconds between updates (larger value = slower animation)
   private static final long UPDATE_DELAY_NANOS = 500_000_000L;  // 0.5 seconds between updates


   // Threshold for resetting the center values when their absolute values exceed this
   private static final double RESET_THRESHOLD = 220.0;


   // Timeline for smooth transition of center values
   private Timeline center1Timeline;
   private Timeline center2Timeline;


   public ButtonGroup() {
       center1Property.addListener((obs, oldVal, newVal) -> updateButtonStyles());
       center2Property.addListener((obs, oldVal, newVal) -> updateButtonStyles());
   }


   // Method to add a ToggleButton and apply styles
   public void addButton(ToggleButton toggleButton) {
       // Determine the style based on the current number of buttons
       String styleClass = (buttons.size() % 2 == 0) ? "delivery-time-button" : "delivery-time-button-reverse";
       toggleButton.getStyleClass().add(styleClass);


       // Add the button to the list
       buttons.add(toggleButton);
   }


   // Method to apply alternating styles to all buttons in the TilePane
   public void applyStylesToButtons(TilePane tilePane) {
       int counter = 0;
       buttonPane = tilePane;
       for (Node node : tilePane.getChildren()) {
           if (node instanceof ToggleButton) {
               ToggleButton toggleButton = (ToggleButton) node;


               // Assign alternating styles
               String styleClass = (counter % 2 == 0) ? "delivery-time-button" : "delivery-time-button-reverse";
               toggleButton.getStyleClass().add(styleClass);


               // Increment counter for alternating styles
               counter++;
           }
       }
   }


   // Method to start the continuous random walk
   public void startRandomWalk() {
       // Create and start the AnimationTimer
       animationTimer = new AnimationTimer() {
           @Override
           public void handle(long now) {
               // Only update at the specified delay interval to slow it down
               if (now - lastUpdateTime >= UPDATE_DELAY_NANOS) {
                   // Randomly update the centers at each frame
                   double newCenter1 = center1Property.get() + randomNewCenter();
                   double newCenter2 = center1Property.get() + 100 + lastRandomReturned;


                   // Start smooth animation for center1
                   animateCenter1(newCenter1);


                   // Start smooth animation for center2
                   animateCenter2(newCenter2);


                   // Check if absolute values of center1 or center2 exceed the threshold
                   if (Math.abs(center1Property.get()) > RESET_THRESHOLD || Math.abs(center2Property.get()) > RESET_THRESHOLD) {
                       // Reset the centers smoothly
                       resetCenters();
                   }


                   // Print a message at each transition
                   System.out.println("New transition: center1 = " + center1Property.get() + ", center2 = " + center2Property.get());


                   // Apply the new styles to the buttons
                   updateButtonStyles();


                   // Update the last update time to the current time
                   lastUpdateTime = now;
               }
           }
       };
       animationTimer.start();
   }


   // Method to update the gradient centers dynamically based on properties
   private void updateButtonStyles() {
       // Apply the updated gradient styles to the buttons
       int counter = 0;
       for (Node node : buttonPane.getChildren()) {
           if (node instanceof ToggleButton) {
               ToggleButton toggleButton = (ToggleButton) node;
               String gradientStyle = (counter % 2 == 0)
                   ? String.format(STYLE_TEMPLATE, center1Property.get())
                   : String.format(STYLE_TEMPLATE, center2Property.get());
               toggleButton.setStyle(gradientStyle);
               counter++;
           }
       }
   }


   // Method to generate a random change for center1 (between -10 and 10)
   private double randomNewCenter() {
       lastRandomReturned = random.nextDouble() * 20 - 10; // Generates a value between -10 and 10
       return lastRandomReturned;
   }


   // Method to reset the centers when the threshold is exceeded
   private void resetCenters() {
       // Create smooth reset animation for center1 and center2
       animateCenter1(0.0);
       animateCenter2(100.0);
   }


   // Method to animate center1's smooth transition
   private void animateCenter1(double targetValue) {
       // Create or restart the timeline for center1 smooth transition
       if (center1Timeline != null) {
           center1Timeline.stop();
       }


       KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), // 1 second for smooth transition
           event -> center1Property.set(targetValue)
       );


       center1Timeline = new Timeline(keyFrame);
       center1Timeline.play();
   }


   // Method to animate center2's smooth transition
   private void animateCenter2(double targetValue) {
       // Create or restart the timeline for center2 smooth transition
       if (center2Timeline != null) {
           center2Timeline.stop();
       }


       KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), // 1 second for smooth transition
           event -> center2Property.set(targetValue)
       );


       center2Timeline = new Timeline(keyFrame);
       center2Timeline.play();
   }
}






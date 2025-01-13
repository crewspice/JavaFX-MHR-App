package com.MaxHighReach;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.util.Duration;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;

public class GradientAnimator {
    private static final double ANIMATION_DURATION = 1.0; // Duration in seconds
    private static final double COLORED_BAND_WIDTH = 0.7; // Width of the orange band
    private static int sets = 3;


    private static Button[] currentButton;
    private static ToggleButton[] currentToggleButton;
    private static Button hoveredButton;
    private static ToggleButton hoveredToggleButton;


    @SuppressWarnings("unchecked")
    private static List<Timeline>[] activeTimelines;
    private static boolean[] hoverPropertiesSet;
    private static Queue<Button>[] buttonQueue;
    private static Queue<ToggleButton>[] toggleButtonQueue;

    //private boolean isInitialized = new boolean(false);


    // Initialize or reset all static fields
    // Needs to be called on every time (way out) you leave a scene that uses the class
    public static void initialize() {
        // Allocate arrays based on the number of sets
        currentButton = new Button[sets];
        currentToggleButton = new ToggleButton[sets];
        activeTimelines = new List[sets];
        hoverPropertiesSet = new boolean[sets];
        buttonQueue = new LinkedList[sets];
        toggleButtonQueue = new LinkedList[sets];


        // Reset other fields
        hoveredButton = null;
        hoveredToggleButton = null;


        // Initialize each list and queue to avoid null references
        for (int i = 0; i < sets; i++) {
            activeTimelines[i] = new ArrayList<>();
            buttonQueue[i] = new LinkedList<>();
            toggleButtonQueue[i] = new LinkedList<>();
        }

    }

    public static void initializeIfNotInitialized(){
        if (activeTimelines == null) {
           initialize();
       }
    }


// --------- idea for making more efficient: pre-load backgrounds into arrays accessible in each key frame -------
    // ---- a hovered and a non hovered set of arrays -------
    // ---- an array for up/down orientation and an array for left/right orientation -------


   // Method to animate a single button
   private static Timeline createButtonAnimation(Button button, Runnable onFinish, int setIndex) {
       Timeline timeline = new Timeline();
       Button nextButton = getNextButton(currentButton[setIndex], setIndex);


       for (double offset = 0; offset < 1; offset += 0.02) {
           double[] bounds = calculateGradientBounds(offset);
           KeyFrame keyFrame = new KeyFrame(Duration.seconds(bounds[0] * ANIMATION_DURATION), event -> {
               // Only animate the button if it's the current button being animated


               if (button == currentButton[setIndex]) {
                   Background background;
                   if (bounds[1] >= .94){
                       background = createGradientBackground(
                               bounds[0], 1,
                               hoveredButton == button ? Color.ORANGE : Color.web("#F4F4F4"),
                               interpolateColor(1 - (((bounds[0] - 1) * -1) / (COLORED_BAND_WIDTH / 2)) ,
                                       currentButton[setIndex] == hoveredButton)
                       );
                       Background nextBackground = createGradientBackground(
                               0, bounds[1] - 1, bounds[2] - 1,
                               interpolateColor((bounds[1] - 1) / (COLORED_BAND_WIDTH / 2),
                                       nextButton == hoveredButton),
                               Color.web("#FFDEAD"),
                               hoveredButton == nextButton ? Color.ORANGE : Color.web("#F4F4F4")
                       );
                       assert nextButton != null;
                       nextButton.setBackground(nextBackground);
                   } else if (bounds[2] >= 1){
                       background = createGradientBackground(
                               bounds[0], bounds[1], 1,
                               hoveredButton == button ? Color.ORANGE : Color.web("#F4F4F4"),
                               Color.web("#FFDEAD"),
                               interpolateColor(((bounds[1] - 1) * -1) / (COLORED_BAND_WIDTH / 2),
                                       currentButton[setIndex] == hoveredButton)
                       );
                       Background nextBackground = createGradientBackground(
                               0, bounds[2] - 1,
                               interpolateColor(1 - ((bounds[2] - 1) / (COLORED_BAND_WIDTH / 2)),
                                       nextButton == hoveredButton),
                               hoveredButton == nextButton ? Color.ORANGE : Color.web("#F4F4F4")
                       );
                       assert nextButton != null;
                       nextButton.setBackground(nextBackground);
                   } else {
                       background = createGradientBackground(
                               bounds[0], bounds[1], bounds[2],
                               hoveredButton == button ? Color.ORANGE : Color.web("#F4F4F4"),
                               Color.web("#FFDEAD"),
                               hoveredButton == button ? Color.ORANGE : Color.web("#F4F4F4")
                       );
                   }


                   button.setBackground(background);
               }


           });
           timeline.getKeyFrames().add(keyFrame);
       }


       // Set on-finished action to trigger the next animation in the chain
       timeline.setOnFinished(event -> {
           if (onFinish != null) {
               // After animation ends, if the current button is hovered, set it to a solid orange fill
              /* if (currentButton[setIndex] != null) {
                   Color firstColor;
                   if (currentButton[setIndex] == hoveredButton) {
                       firstColor = Color.ORANGE;
                   } else {
                       firstColor = Color.TRANSPARENT;
                   }
                   currentButton[setIndex].setBackground(new Background(
                               new BackgroundFill(
                                       firstColor,
                                       CornerRadii.EMPTY,
                                       null
                               )
                       ));
               } */
               onFinish.run();
           }


       });
       // Add timeline to the activeTimelines list
       for (Timeline tl : activeTimelines[setIndex]) {
           tl.stop();
       }
       activeTimelines[setIndex].clear();
       activeTimelines[setIndex].add(timeline);
       return timeline;
   }


   // Method to animate a single button
   private static Timeline createToggleButtonAnimation(ToggleButton button, Runnable onFinish, int setIndex) {
     //   printLineCounter++;
      // System.out.println("0th Print line checkpoint: " + printLineCounter);
       //System.out.println(printLineCounter + " toggle button is: " + button + " set index is: " + setIndex
        //    + " current toggle button is: " + currentToggleButton[setIndex]);

        Timeline timeline = new Timeline();
       ToggleButton nextToggleButton = getNextToggleButton(currentToggleButton[setIndex], setIndex);


       for (double offset = 0; offset < 1; offset += 0.02) {
           double[] bounds = calculateGradientBounds(offset);
           KeyFrame keyFrame = new KeyFrame(Duration.seconds(bounds[0] * ANIMATION_DURATION), event -> {
               // Only animate the button if it's the current button being animated


               if (button == currentToggleButton[setIndex]) {
                   Background background;
                   if (bounds[1] >= .94){
                       background = createGradientBackground(
                               bounds[0], 1,
                               hoveredToggleButton == button ? Color.ORANGE : Color.web("#F4F4F4"),
                               interpolateColor(1 - (((bounds[0] - 1) * -1) / (COLORED_BAND_WIDTH / 2)) ,
                                       currentToggleButton[setIndex] == hoveredToggleButton)
                       );
                       assert nextToggleButton != null;
                       Background nextBackground = createGradientBackground(
                               0, bounds[1] - 1, bounds[2] - 1,
                               interpolateColor((bounds[1] - 1) / (COLORED_BAND_WIDTH / 2),
                                       nextToggleButton == hoveredToggleButton),
                               Color.web("#FFDEAD"),
                               hoveredToggleButton == nextToggleButton ? Color.ORANGE : Color.web("#F4F4F4")
                       );
                       nextToggleButton.setBackground(nextBackground);
                   } else if (bounds[2] >= 1){
                       background = createGradientBackground(
                               bounds[0], bounds[1], 1,
                               hoveredToggleButton == button ? Color.ORANGE : Color.web("#F4F4F4"),
                               Color.web("#FFDEAD"),
                               interpolateColor(((bounds[1] - 1) * -1) / (COLORED_BAND_WIDTH / 2),
                                       currentToggleButton[setIndex] == hoveredToggleButton)
                       );
                       assert nextToggleButton != null;
                       Background nextBackground = createGradientBackground(
                               0, bounds[2] - 1,
                               interpolateColor(1 - ((bounds[2] - 1) / (COLORED_BAND_WIDTH / 2)),
                                       nextToggleButton == hoveredToggleButton),
                               hoveredToggleButton == nextToggleButton ? Color.ORANGE : Color.web("#F4F4F4")
                       );
                       nextToggleButton.setBackground(nextBackground);
                   } else {
                       background = createGradientBackground(
                               bounds[0], bounds[1], bounds[2],
                               hoveredToggleButton == button ? Color.ORANGE : Color.web("#F4F4F4"),
                               Color.web("#FFDEAD"),
                               hoveredToggleButton == button ? Color.ORANGE : Color.web("#F4F4F4")
                       );
                   }


                   button.setBackground(background);
               }


           });
           timeline.getKeyFrames().add(keyFrame);
       }

     //   System.out.println("1st Print line checkpoint: " + printLineCounter);
       // Set on-finished action to trigger the next animation in the chain
       timeline.setOnFinished(event -> {
           if (onFinish != null) {
               // After animation ends, if the current button is hovered, set it to a solid orange fill
               if (currentToggleButton[setIndex] != null) {
                   if (currentToggleButton[setIndex] == hoveredToggleButton) {
                       currentToggleButton[setIndex].setBackground(new Background(
                               new BackgroundFill(
                                       Color.ORANGE, // Full orange fill
                                       CornerRadii.EMPTY,
                                       null
                               )
                       ));
                   }
               }
               onFinish.run();
           }
       });
      // System.out.println("2nd print line checkpoint: " + printLineCounter);
       // Add timeline to the activeTimelines list
       for (Timeline tl : activeTimelines[setIndex]) {
           tl.stop();
       }
       activeTimelines[setIndex].clear();
       activeTimelines[setIndex].add(timeline);
       return timeline;
   }


   // Method to apply sequential gradient animations to a list of buttons, restarting after completion
   public static void applySequentialGradientAnimation(List<Button> buttons, int setIndex) {
      initializeIfNotInitialized();
       if (buttons.isEmpty()) {
           return; // No buttons to animate
       }


       buttonQueue[setIndex].clear(); // Clear any existing buttons
       buttonQueue[setIndex].addAll(buttons); // Add all buttons to the queue


       if (!hoverPropertiesSet[setIndex]) {
           for (Button button : buttons) {
               // Handle hover behavior during animation
               button.setOnMouseEntered(event -> {
                   hoveredButton = button;
               });


               button.setOnMouseExited(event -> {
                   // Restore the gradient if the hover ends
                   hoveredButton = null;
               });
           }
           hoverPropertiesSet[setIndex] = true;
       }


       // Recursive function to animate one button at a time
       Runnable animateNext = new Runnable() {
           private int index = 0;


           @Override
           public void run() {
               if (index < buttons.size()) {
                   Button button = buttons.get(index);
                   currentButton[setIndex] = button; // Set the current button as the one animating
                   index++;
                   Timeline timeline = createButtonAnimation(button, this, setIndex);
                   timeline.play();
               } else {
                   // Restart the sequence when all buttons are done
                   index = 0;
                   currentButton = null; // Reset current button to allow next sequence to start cleanly
                   this.run();
               }
           }
       };


       // Start the sequence
       animateNext.run();
   }


   // Method to apply sequential gradient animations to a list of buttons, restarting after completion
   public static void applySequentialGradientAnimationToggles(List<ToggleButton> buttons, int setIndex, String baseStyleClass) {
       initializeIfNotInitialized();

        if (setIndex < 0 || setIndex >= activeTimelines.length) {
           throw new IllegalArgumentException("Invalid set index: " + setIndex);
       }

        Timeline timeline;
       if (buttons.isEmpty()) {
           return; // No buttons to animate
       }

       List<Timeline> timelinesForSet = activeTimelines[setIndex];
       timelinesForSet.clear();

       toggleButtonQueue[setIndex].clear(); // Clear any existing buttons
       toggleButtonQueue[setIndex].addAll(buttons); // Add all buttons to the queue


       for (ToggleButton button : buttons) {
           if (!hoverPropertiesSet[setIndex]) {
               // Handle hover behavior during animation
               button.setOnMouseEntered(event -> {
                   hoveredToggleButton = button;
               });


               button.setOnMouseExited(event -> {
                   // Restore the gradient if the hover ends
                   hoveredToggleButton = null;
               });
           }
       }
       hoverPropertiesSet[setIndex] = true;

      // printLineCounter++;
       // System.out.println("3rd print line checkpoint: " + printLineCounter);
       // Recursive function to animate one button at a time
       Runnable animateNext = new Runnable() {
           private int index = 0;



           @Override
           public void run() {
               if (index < buttons.size()) {
                   ToggleButton button = buttons.get(index);
                   currentToggleButton[setIndex] = button; // Set the current button as the one animating
                   index++;
                   Timeline timeline = createToggleButtonAnimation(button, this, setIndex);
                   timelinesForSet.add(timeline);
                   for (ToggleButton butn : buttons) {
                       butn.getStyleClass().removeAll(butn.getStyleClass());
                       if (butn != button) {
                          butn.getStyleClass().add(baseStyleClass);
                       }
                   }
                   timeline.play();
               } else {
                   // Restart the sequence when all buttons are done
                   index = 0;
                   //currentToggleButton = null; // Reset current button to allow next sequence to start cleanly
                   this.run();
               }
           }
       };


       // Start the sequence
       animateNext.run();
   }


   // Method to stop the current button animation if any
   public static void stopCurrentAnimation() {
        initializeIfNotInitialized();
        for (int i = 0; i < sets; i++) {
            for (Timeline tl : activeTimelines[i]) {
                tl.stop();
            }
            activeTimelines[i].clear();
            for (ToggleButton button : toggleButtonQueue[i]) {
               button.setStyle("-fx-background-color: #F4F4F4");
           }
        }
        
      /* if (currentButton[setIndex] != null) {
           // Reset the background to its default state (or set it to a default color)
           currentButton[setIndex].setBackground(null);
           currentButton[setIndex] = null;
           hoverPropertiesSet = false;
       }*/
   }


   // Method to stop all active animations
   public static void stopAllAnimations() {
        initializeIfNotInitialized();
        for (int i = 0; i < sets; i++) {
            for (Timeline timeline : activeTimelines[i]) {
                timeline.stop(); // Stop each active timeline
            }

            activeTimelines[i].clear(); // Clear the list of active timelines
            hoverPropertiesSet[i] = false;
        }
   }

   public static Button getNextButton(Button current, int setIndex) {
       if (buttonQueue[setIndex].isEmpty() || current == null) {
           return null; // Return null if the list is empty or the current button is null
       }


       // Find the index of the current button
       int currentIndex = new ArrayList<>(buttonQueue[setIndex]).indexOf(current);


       if (currentIndex == -1) {
           return null; // Button not found in the list
       }


       // Get the next button, cycling back to the first button if at the end
       int nextIndex = (currentIndex + 1) % buttonQueue[setIndex].size();
       return new ArrayList<>(buttonQueue[setIndex]).get(nextIndex);
   }


   public static ToggleButton getNextToggleButton(ToggleButton current, int setIndex) {
       if (toggleButtonQueue[setIndex].isEmpty() || current == null) {
           return new ToggleButton(); // Return null if the list is empty or the current button is null
       }


       // Find the index of the current button
       int currentIndex = new ArrayList<>(toggleButtonQueue[setIndex]).indexOf(current);


       if (currentIndex == -1) {
           return new ToggleButton(); // Button not found in the list
       }


       // Get the next button, cycling back to the first button if at the end
       int nextIndex = (currentIndex + 1) % toggleButtonQueue[setIndex].size();
       return new ArrayList<>(toggleButtonQueue[setIndex]).get(nextIndex);
   }


   private static Background createGradientBackground(double start, double middle, double end, Color startColor,
                                                      Color middleColor, Color endColor) {
      /* printLnCounter++;
       if (printLnCounter == 17) {
           System.out.println("creating a 3 stop gradient with start=" + start + " middle=" + middle + " end=" + end
           + " startColor=" + startColor + " middleColor=" + middleColor + " endColor=" + endColor + " type=" + type);
           printLnCounter = 0;
       } */


       return new Background(new BackgroundFill(
               new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                       new Stop(start, startColor),
                       new Stop(middle, middleColor),
                       new Stop(end, endColor)),
               CornerRadii.EMPTY,
               null
       ));
   }


   private static Background createGradientBackground(double start, double end, Color startColor, Color endColor) {
      /* printLnCounter++;
       if (printLnCounter == 17) {
           System.out.println("creating a 2 stop gradient with start=" + start + " end=" + end
                   + " startColor=" + startColor + " endColor=" + endColor + " type=" + type);
           printLnCounter = 0;
       } */




       return new Background(new BackgroundFill(
               new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                       new Stop(start, startColor),
                       new Stop(end, endColor)),
               CornerRadii.EMPTY,
               null
       ));
   }




   private static double[] calculateGradientBounds(double baseOffset) {
       return new double[]{
               baseOffset,
               baseOffset + (COLORED_BAND_WIDTH / 2),
               baseOffset + COLORED_BAND_WIDTH
       };
   }


   private static Color interpolateColor(double position, boolean hovered) {
       // Clamp the position to the range [0, 1]
       position = Math.min(1.0, Math.max(0.0, position));


       // Define the start and end colors based on the hovered state
       Color startColor = Color.web("#FFDEAD");
       Color endColor = hovered ? Color.ORANGE : Color.web("#F4F4F4");


       // Extract RGBA components of the start and end colors
       double startRed = startColor.getRed();
       double startGreen = startColor.getGreen();
       double startBlue = startColor.getBlue();
       double startAlpha = startColor.getOpacity();


       double endRed = endColor.getRed();
       double endGreen = endColor.getGreen();
       double endBlue = endColor.getBlue();
       double endAlpha = endColor.getOpacity();


       // Interpolate each component based on the position
       double red = startRed + (endRed - startRed) * position;
       double green = startGreen + (endGreen - startGreen) * position;
       double blue = startBlue + (endBlue - startBlue) * position;
       double alpha = startAlpha + (endAlpha - startAlpha) * position;


       // Return the interpolated color
       //System.out.println("Returning interpolated color: red=" + red + ", green=" + green + ", blue=" + blue);
       return new Color(red, green, blue, alpha);
   }

}

@echo off
rem Change to the directory containing the JAR file
cd /d "C:\Users\maxhi\OneDrive\Documents\Quickbooks\QBProgram Development\IdeaProjects\JavaFX-MHR-App"

rem Run the JAR file directly with the correct module path and modules
java --module-path "C:/Program Files/JavaFX/javafx-sdk-21.0.4/lib" --add-modules javafx.controls,javafx.fxml -jar build\libs\MaxReachPro.jar

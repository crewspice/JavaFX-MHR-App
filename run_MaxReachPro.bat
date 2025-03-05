@echo off
rem Change to the directory containing the JAR file
cd /d "%USERPROFILE%\OneDrive\Documents\MaxReachPro\Max Reach Pro"

rem Run the JAR file directly with the correct module path and modules
java --module-path "%USERPROFILE%\OneDrive\Documents\MaxReachPro\program_files\javafx-sdk-17.0.14\lib" --add-modules javafx.controls,javafx.fxml,javafx.swing -jar build\libs\MaxReachPro-all.jar

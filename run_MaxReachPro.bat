@echo off
cd /d "%USERPROFILE%\OneDrive\Documents\MaxReachPro\Max Reach Pro"

echo Launching...
"%USERPROFILE%\OneDrive\Documents\MaxReachPro\program_files\openjdk-17.0.13\bin\java.exe" ^
  --module-path "%USERPROFILE%\OneDrive\Documents\MaxReachPro\program_files\javafx-sdk-17.0.14\lib" ^
  --add-modules javafx.controls,javafx.fxml,javafx.swing ^
  -jar build\libs\MaxReachPro-all.jar

echo Exit code: %ERRORLEVEL%
pause

Set WshShell = CreateObject("WScript.Shell")
batFile = WshShell.ExpandEnvironmentStrings("%USERPROFILE%\OneDrive\Documents\MaxReachPro\Max Reach Pro\run_MaxReachPro.bat")
WshShell.Run """" & batFile & """", 0, False

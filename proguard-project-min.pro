# === Input/Output ===
-injars build/libs/MaxReachPro-all.jar
-outjars build/libs/MaxReachPro-obfuscated.jar

# === JavaFX & JDK Modules ===
-libraryjars "C:/Users/offic/OneDrive/Documents/MaxReachPro/program_files/javafx-sdk-17.0.14/lib/javafx.base.jar"
-libraryjars "C:/Users/offic/OneDrive/Documents/MaxReachPro/program_files/javafx-sdk-17.0.14/lib/javafx.controls.jar"
-libraryjars "C:/Users/offic/OneDrive/Documents/MaxReachPro/program_files/javafx-sdk-17.0.14/lib/javafx.fxml.jar"
-libraryjars "C:/Users/offic/OneDrive/Documents/MaxReachPro/program_files/javafx-sdk-17.0.14/lib/javafx.graphics.jar"
-libraryjars "C:/Users/offic/OneDrive/Documents/MaxReachPro/program_files/javafx-sdk-17.0.14/lib/javafx.media.jar"
-libraryjars "C:/Users/offic/OneDrive/Documents/MaxReachPro/program_files/javafx-sdk-17.0.14/lib/javafx.swing.jar"
-libraryjars "C:/Users/offic/OneDrive/Documents/MaxReachPro/program_files/javafx-sdk-17.0.14/lib/javafx.web.jar"

# Required JDK modules file
-libraryjars "C:/Users/offic/OneDrive/Documents/MaxReachPro/program_files/openjdk-17.0.13/lib/modules"

# === General Settings ===
-ignorewarnings
-dontoptimize
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-keepattributes *Annotation*,InnerClasses,EnclosingMethod,Signature,LineNumberTable,SourceFile

# === KEEP everything EXCEPT ColumnFactory ===

# Keep all app code except the one class to obfuscate
-keep class com.MaxHighReach.** { *; }

# Keep all controllers completely (not obfuscated)
-keep class com.MaxHighReach.controllers.** { *; }

# Entry point
-keep public class com.MaxHighReach.Main {
    public static void main(java.lang.String[]);
}

# JavaFX Application class
-keep public class com.MaxHighReach.MaxReachPro extends javafx.application.Application {
    public void start(javafx.stage.Stage);
}

# Required method for JavaFX mouse event wiring
-keepclassmembers class com.MaxHighReach.MaxReachPro {
    public void e(javafx.scene.input.MouseEvent);
}

# Gson / Jackson
-keep class com.google.gson.** { *; }
-keep class com.fasterxml.jackson.** { *; }
-keepclassmembers class * {
    @com.fasterxml.jackson.annotation.JsonCreator <init>(...);
    @com.fasterxml.jackson.annotation.JsonProperty <fields>;
}

# OkHttp & MySQL
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class com.mysql.** { *; }

# JavaFX runtime
-keep class javafx.** { *; }

# === COLUMNFACTORY - Only class allowed to be obfuscated ===
# No keep rule = allowed to be renamed
# Make sure this class is NOT covered by any general -keep class **
# With all the above keep rules, ColumnFactory is NOT preserved, so will be obfuscated

# === Mapping output (optional) ===
-printmapping build/libs/proguard-map.txt

# Keep everything except ColumnFactory
-keep class com.MaxHighReach.** { *; }

# But explicitly allow obfuscation for ColumnFactory by not keeping it:
# So override the above keep for ColumnFactory by negating its keep:
# (ProGuard doesn't support negation directly,
# so instead, exclude ColumnFactory from the keep by moving it outside or
# use -keep for everything except ColumnFactory)

# Practically, do NOT keep ColumnFactory explicitly:
# Remove any -keep for com.MaxHighReach.ColumnFactory

# To be explicit:
-keep class !com.MaxHighReach.ColumnFactory,com.MaxHighReach.** { *; }

# or if ! (negation) is unsupported, just:
-keep class com.MaxHighReach.** { *; }
# Then add -keepclassmembers for other classes except ColumnFactory.

# To keep all controllers safe:
-keep class com.MaxHighReach.controllers.** { *; }

# Keep entry points:
-keep public class com.MaxHighReach.Main {
    public static void main(java.lang.String[]);
}
-keep public class com.MaxHighReach.MaxReachPro extends javafx.application.Application {
    public void start(javafx.stage.Stage);
}

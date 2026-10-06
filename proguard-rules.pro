# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html

-dontusemixedcaseclassnames
-verbose

# Preserve line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep all public classes and their public members
-keep public class * {
    public <methods>;
    public <fields>;
}

# Keep classes that are referenced in AndroidManifest.xml
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.Fragment
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends android.view.View

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# Keep our app classes
-keep class com.solitaire.game.** { *; }

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public abstract **;
}

# Google Mobile Ads
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# GSON
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Coroutines
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# AndroidX
-dontwarn androidx.**
-keep class androidx.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

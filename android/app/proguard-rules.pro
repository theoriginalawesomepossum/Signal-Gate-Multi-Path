# Keep your main application class and important components
-keep class com.signalgate.multipoint.** { *; }

# Keep Room database, entities, and DAOs
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity
-keep class * extends androidx.room.Dao
-keepclassmembers class * {
    @androidx.room.* *;
}

# Keep Kotlin coroutines & reflection
-keep class kotlin.coroutines.** { *; }
-keep class kotlinx.coroutines.** { *; }

# Keep CallScreeningService (critical for the app to work)
-keep public class com.signalgate.multipoint.CallScreeningService

# Keep anything using SharedPreferences or settings
-keep class com.signalgate.multipoint.ui.SettingsFragment

# Optional: Suppress some common R8 warnings
-dontwarn org.jetbrains.annotations.**
-dontwarn kotlinx.coroutines.**

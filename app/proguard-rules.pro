# Preserve line numbers for obfuscated stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# kotlinx.serialization rules
-keepattributes *Annotation*, EnclosingMethod, InnerClasses, Signature
-keepclassmembernames class com.prajwalpawar.fiscus.** {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class com.prajwalpawar.fiscus.** {
    *** Companion;
    *** serializer(...);
    <init>(int, ...);
}

# Room rules - Only keep the classes that are specifically needed for the database
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keep class * extends androidx.room.RoomDatabase { *; }

# Hilt/Dagger rules (mostly handled by R8, but preserve entry points)
-keep class * extends androidx.lifecycle.ViewModel { *; }

# General optimization
-optimizations !code/allocation/variable

# Keep metadata for reflection if needed (e.g. for serialization)
-keep class kotlin.Metadata { *; }
# Duka.AI ProGuard Rules
# Comprehensive rules for all dependencies

#-------------------------------------------------
# General Android Rules
#-------------------------------------------------

# Keep line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotations
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

#-------------------------------------------------
# Kotlin
#-------------------------------------------------

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.example.dukaai.**$$serializer { *; }
-keepclassmembers class com.example.dukaai.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.dukaai.** {
    kotlinx.serialization.KSerializer serializer(...);
}

#-------------------------------------------------
# Room Database
#-------------------------------------------------

-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Room entities
-keep class com.example.dukaai.data.local.entity.** { *; }

# Keep Room DAOs
-keep class com.example.dukaai.data.local.dao.** { *; }

#-------------------------------------------------
# Hilt / Dagger
#-------------------------------------------------

-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }
-keepclasseswithmembers class * {
    @dagger.* <fields>;
}
-keepclasseswithmembers class * {
    @dagger.* <methods>;
}
-keepclasseswithmembers class * {
    @javax.inject.* <fields>;
}
-keepclasseswithmembers class * {
    @javax.inject.* <methods>;
}

# Hilt generated classes
-keep class com.example.dukaai.Hilt_* { *; }
-keep class com.example.dukaai.**_HiltModules* { *; }
-keep class com.example.dukaai.**_Factory { *; }
-keep class com.example.dukaai.**_MembersInjector { *; }

#-------------------------------------------------
# Firebase
#-------------------------------------------------

-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firebase Firestore
-keep class com.google.firebase.firestore.** { *; }
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
}

# Firebase Auth
-keepattributes Signature
-keepattributes *Annotation*

#-------------------------------------------------
# TensorFlow Lite
#-------------------------------------------------

-keep class org.tensorflow.lite.** { *; }
-keep class org.tensorflow.lite.support.** { *; }
-keep class org.tensorflow.lite.gpu.** { *; }
-dontwarn org.tensorflow.lite.**

# Keep TFLite model classes
-keepclassmembers class * {
    @org.tensorflow.lite.support.metadata.* <fields>;
}

#-------------------------------------------------
# ML Kit
#-------------------------------------------------

-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Barcode scanning
-keep class com.google.mlkit.vision.barcode.** { *; }

#-------------------------------------------------
# CameraX
#-------------------------------------------------

-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

#-------------------------------------------------
# Coil Image Loading
#-------------------------------------------------

-keep class coil.** { *; }
-dontwarn coil.**

#-------------------------------------------------
# Jetpack Compose
#-------------------------------------------------

-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Compose runtime classes
-keep class androidx.compose.runtime.** { *; }

#-------------------------------------------------
# Navigation Compose
#-------------------------------------------------

-keep class androidx.navigation.** { *; }
-keepclassmembers class * {
    @androidx.navigation.NavDestination <fields>;
}

#-------------------------------------------------
# DataStore
#-------------------------------------------------

-keep class androidx.datastore.** { *; }
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}

#-------------------------------------------------
# WorkManager
#-------------------------------------------------

-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class androidx.work.** { *; }

#-------------------------------------------------
# App-specific Rules
#-------------------------------------------------

# Keep ViewModels
-keep class com.example.dukaai.ui.viewmodel.** { *; }

# Keep data classes used for serialization
-keep class com.example.dukaai.data.** { *; }

# Keep voice command classes
-keep class com.example.dukaai.voice.** { *; }

# Keep ML classifier
-keep class com.example.dukaai.ml.** { *; }

#-------------------------------------------------
# Debugging (Comment out for production)
#-------------------------------------------------

# Uncomment to print seeds (what ProGuard keeps)
#-printseeds seeds.txt

# Uncomment to print usage (what ProGuard removes)
#-printusage usage.txt

# Uncomment to print mapping (for deobfuscation)
#-printmapping mapping.txt

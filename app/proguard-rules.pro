# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep NativeAudioEngine
-keep class com.smartinstrument.app.audio.NativeAudioEngine { *; }

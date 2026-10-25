# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Kotlinx Serialization
-keepattributes *Annotation*,InnerClasses,Signature
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keep,includedescriptorclasses class com.diegodeabreu.ifoodprocesso.data.model.** { *; }

# Ktor & OkHttp
-dontwarn io.ktor.**
-dontwarn okhttp3.**
-keep class io.ktor.** { *; }
-keep class okhttp3.** { *; }

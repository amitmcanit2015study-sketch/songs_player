# Media3 ExoPlayer Proguard Rules
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Retrofit & OkHttp Proguard Rules
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Room Database Proguard Rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Glide Proguard Rules
-keep public class * implements com.github.bumptech.glide.module.GlideModule
-keep class * extends com.github.bumptech.glide.module.AppGlideModule {
    <init>(...);
}
-keep public enum com.github.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

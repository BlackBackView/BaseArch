-keep class com.bbv.base.** { *; }
-keep interface com.bbv.base.** { *; }

-keepattributes *Annotation*

-keep class androidx.databinding.** { *; }
-keep class * extends androidx.databinding.DataBinderMapper { *; }
-keep class * extends androidx.databinding.DataBindingComponent { *; }

-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

-keep class kotlinx.coroutines.** { *; }

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclassmembers class * extends android.view.View {
   void set*(***);
   *** get*();
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable *;
}

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter { *; }

# Moshi
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**
-keep class * extends com.squareup.moshi.JsonAdapter { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

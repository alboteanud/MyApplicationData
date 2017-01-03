# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\dan_2\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class namef to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}


-keepattributes Signature
-keepattributes *Annotation*

-keep class com.alboteanu.myapplicationdata.viewholder.** {
    *;
}

# This rule will properly ProGuard all the model classes in
# the package com.yourcompany.models. Modify to fit the structure
# of your app.
-keepclassmembers class com.alboteanu.myapplicationdata.models.** {
    *;
}
# Proguard rules for BetterShell
-keepattributes *Annotation*
-dontwarn com.jcraft.jsch.**
-keep class com.jcraft.jsch.** { *; }

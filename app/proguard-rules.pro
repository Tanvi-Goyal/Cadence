# R8 keep rules for the release build. Everything not listed here is fair game for shrinking,
# optimization and obfuscation.
#
# Deliberately short. Most of this stack needs no rules: Koin keys bindings by KClass (renamed
# consistently at both ends), kotlinx.serialization bakes serial names into generated descriptors
# as compile-time constants (so the sync wire format and the type-safe nav routes survive
# obfuscation), and Room 3's KMP builder is a generated expect/actual, not Class.forName. Room,
# OkHttp, Okio, Coil and coroutines all ship consumer rules inside their own artifacts.

# --- Enums persisted as strings ---------------------------------------------------------------
# WeightUnit / ThemeMode / Gender / RaceMode / SessionType are written to DataStore and Room as
# `name` and read back with valueOf()/enumValueOf() (PreferencesRepositoryImpl, HistoryViewModel,
# StationsViewModel). Enum constants are static fields, and R8 renames fields — so a renamed
# constant makes every value already on a user's disk fail to resolve. The runCatching wrappers
# swallow that, so it lands as a silent settings reset on upgrade, not a crash. Keep the constant
# names for every enum we own.
-keepclassmembers enum com.mindset.** {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- Readable Crashlytics stack traces --------------------------------------------------------
# Without these the release build has no line numbers to de-obfuscate against. The mapping file
# uploaded by the Crashlytics Gradle plugin is what turns the obfuscated trace back into source;
# -renamesourcefileattribute collapses the file name to a constant so the APK itself leaks nothing.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Firebase's documented rule: keeps exception type names meaningful in reported traces.
-keep public class * extends java.lang.Exception

# --- Missing-class warnings -------------------------------------------------------------------
# Do NOT add speculative -dontwarn lines. If R8 fails on missing classes, AGP writes the exact
# rules it wants to build/outputs/mapping/release/missing_rules.txt — read that, understand why the
# class is absent, then copy the specific lines here.

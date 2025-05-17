plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // Google services Gradle plugin (for Firebase)
    id("com.google.gms.google-services") version "4.4.0" apply false
}

// Add repositories section
buildscript {
    repositories {
        google()
        mavenCentral()
    }
}
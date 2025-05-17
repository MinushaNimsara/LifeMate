pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        // Add JitPack repository for MPAndroidChart
        maven { url = uri("https://jitpack.io") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)  // Changed from FAIL_ON_PROJECT_REPOS
    repositories {
        google()
        mavenCentral()
        // Add JitPack repository for MPAndroidChart
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "LifeMate"
include(":app")
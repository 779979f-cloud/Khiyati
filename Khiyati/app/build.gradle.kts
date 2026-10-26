plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.khiyati.app"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.khiyati.app"
        minSdk = 23
        targetSdk = 35
        versionCode = 2
        versionName = "2.0"
    }
}

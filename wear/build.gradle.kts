plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.example.sleeptandard_mvp_demo.wear"
    compileSdk = 36

    defaultConfig {
        // CRITICAL: applicationId must match Phone app for Wearable Data Layer API communication
        // namespace remains ".wear" to preserve R class and internal code structure
        applicationId = "com.example.sleeptandard_mvp_demo"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    
    // Wearable API for Phone-Watch communication
    implementation("com.google.android.gms:play-services-wearable:18.1.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // JSON serialization for data transfer
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    
    // PyTorch Mobile for AI inference
    implementation("org.pytorch:pytorch_android_lite:1.10.0")
    implementation("org.pytorch:pytorch_android_torchvision_lite:1.10.0")
}


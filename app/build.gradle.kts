plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.gms.google-services") version "4.4.2"  // This is enough for Google Services
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" // For Jetpack Compose
    id ("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.example.recreationapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.recreationapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15" // Compatible with Kotlin 2.1.0
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui:1.7.0")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.google.code.gson:gson:2.11.0")
    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    // Add specific Firebase dependencies
    implementation("com.google.firebase:firebase-auth-ktx") // For Firebase Authentication
    implementation("com.google.firebase:firebase-database-ktx") // For Firebase Realtime Database
    implementation ("androidx.compose.material3:material3")
    implementation ("io.coil-kt:coil-compose:2.4.0")
    implementation ("androidx.navigation:navigation-compose")
    implementation(libs.androidx.navigation.runtime.android)
    implementation ("androidx.compose.material3:material3:1.2.0")
    implementation ("io.coil-kt:coil-compose:2.4.0")
    implementation ("androidx.navigation:navigation-compose:2.7.7")
    implementation ("androidx.activity:activity-compose:1.8.2")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation ("androidx.compose.material:material-icons-extended")
    implementation ("androidx.compose.ui:ui:1.6.0")
    implementation ("androidx.compose.foundation:foundation:1.6.0")
    implementation ("androidx.compose.material3:material3:1.2.0")
    implementation ("androidx.compose.ui:ui:1.6.0")
    implementation ("androidx.compose.foundation:foundation:1.6.0")
    implementation ("androidx.compose.material3:material3:1.2.0")
    implementation ("androidx.navigation:navigation-compose:2.7.5")
    implementation ("io.coil-kt:coil-compose:2.5.0")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation ("androidx.compose.material3:material3:1.3.0") // Or latest version
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation ("io.coil-kt:coil-compose:2.6.0") // For AsyncImage
    implementation ("androidx.compose.material3:material3:1.3.0") // Or latest
    implementation ("androidx.compose.foundation:foundation:1.7.0")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation ("io.coil-kt:coil-compose:2.6.0")
    // ExoPlayer for media playback
//    implementation ("androidx.media3:media3-exoplayer:1.2.0")
    implementation ("androidx.media3:media3-exoplayer-dash:1.2.0")
//    implementation ("androidx.media3:media3-ui:1.2.0")
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")

    // OkHttp for network requests
    implementation ("com.squareup.okhttp3:okhttp:4.11.0")
    implementation ("androidx.media3:media3-datasource-okhttp:1.2.0")

    // Retrofit for API calls
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    // Coil for image loading
    implementation ("io.coil-kt:coil-compose:2.4.0")

    // Compose dependencies
//    implementation ("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation ("androidx.compose.material:material-icons-extended:1.5.4")
    implementation ("androidx.activity:activity-compose:1.8.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

//    implementation ("com.github.kotvertolet:youtube-jextractor:0.2.3")
//    implementation ("com.github.kotvertolet:youtube-jextractor:1.0.0")

    implementation ("com.pierfrancescosoffritti.androidyoutubeplayer:core:11.1.0")


}
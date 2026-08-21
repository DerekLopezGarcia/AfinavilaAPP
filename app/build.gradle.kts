plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val releaseStorePassword = (findProperty("keystore.storePassword") as String?)
    ?: System.getenv("KS_STORE_PASSWORD")
val releaseKeyAlias = (findProperty("keystore.keyAlias") as String?)
    ?: System.getenv("KS_KEY_ALIAS")
val releaseKeyPassword = (findProperty("keystore.keyPassword") as String?)
    ?: System.getenv("KS_KEY_PASSWORD")
val releaseKeystorePath = (findProperty("keystore.file") as String?)
    ?: System.getenv("KS_KEYSTORE_PATH")
    ?: "../afinavila-release.jks"
val releaseKeystore = file(releaseKeystorePath)
val hasReleaseSigning = releaseKeystore.exists() &&
    !releaseStorePassword.isNullOrBlank() &&
    !releaseKeyAlias.isNullOrBlank() &&
    !releaseKeyPassword.isNullOrBlank()

gradle.taskGraph.whenReady {
    if (allTasks.any { it.name == "bundleRelease" || it.name == "assembleRelease" }) {
        require(hasReleaseSigning) {
            "Release signing is required. Set KS_KEYSTORE_PATH, KS_STORE_PASSWORD, KS_KEY_ALIAS and KS_KEY_PASSWORD."
        }
    }
}

android {
    namespace = "es.afinavila"
    compileSdk = 35

    defaultConfig {
        applicationId = "es.afinavila"
        minSdk = 21
        targetSdk = 35
        versionCode = 2
        versionName = "0.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "API_URL", "\"https://www.afinavila.es/api/\"")
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = releaseKeystore
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation (libs.androidx.ui.text.google.fonts)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    //glide
    implementation (libs.glide)
    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.annotations)
    ksp(libs.koin.ksp.compiler)
    // Material Icons Extended
    implementation(libs.androidx.compose.material.icons.extended)
    // Navigation
    implementation(libs.androidx.navigation.compose)
    // ViewModel Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    // OkHttp
    implementation(libs.okhttp)
    implementation(libs.androidx.security.crypto)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

}


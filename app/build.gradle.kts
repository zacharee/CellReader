plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.bugsnag.android)
    id("kotlin-parcelize")
}

android {
    compileSdk = rootProject.properties["compileSdk"].toString().toInt()

    defaultConfig {
        applicationId = "dev.zwander.cellreader"
        minSdk = rootProject.properties["minSdk"].toString().toInt()
        targetSdk = rootProject.properties["targetSdk"].toString().toInt()
        versionCode = rootProject.properties["versionCode"].toString().toInt()
        versionName = rootProject.properties["versionName"].toString()

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    val javaVersion: Int by rootProject.extra

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(javaVersion)
        targetCompatibility = JavaVersion.toVersion(javaVersion)
    }
    kotlinOptions {
        jvmTarget = javaVersion.toString()
    }
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    namespace = "dev.zwander.cellreader"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.ui)
    implementation(libs.accompanist.systemuicontroller)

    implementation(project(":data"))

    wearApp(project(":wear"))
}

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    compileSdk = rootProject.properties["compileSdk"].toString().toInt()

    defaultConfig {
        applicationId = "dev.zwander.cellreader"
        minSdk = 26
        targetSdk = rootProject.properties["targetSdk"].toString().toInt()
        versionCode = rootProject.properties["versionCode"].toString().toInt()
        versionName = rootProject.properties["versionName"].toString()
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    namespace = "dev.zwander.cellreader.wear"

    val javaVersion: Int by rootProject.extra

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(javaVersion)
        targetCompatibility = JavaVersion.toVersion(javaVersion)
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.percentlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.wear)
    implementation(libs.androidx.tiles.renderer)

    implementation(project(":data"))
}
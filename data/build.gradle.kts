import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-parcelize")
}

android {
    compileSdk = rootProject.properties["compileSdk"].toString().toInt()

    defaultConfig {
        minSdk = rootProject.properties["minSdk"].toString().toInt()
        lint.targetSdk = rootProject.properties["targetSdk"].toString().toInt()
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
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
        aidl = true
    }
    namespace = "dev.zwander.cellreader.data"
}

dependencies {
    api(libs.androidx.core.ktx)
    api(libs.androidx.appcompat)
    api(libs.material)
    api(libs.play.services.wearable)
    api(libs.androidx.savedstate)
    api(libs.androidx.savedstate.ktx)

    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.tooling.preview)
    api(libs.androidx.compose.material)

    api(libs.androidx.activity.compose)
    api(libs.androidx.glance)
    api(libs.androidx.glance.appwidget)
    api(libs.androidx.runtime.livedata)
    api(libs.androidx.material3)

    api(libs.androidx.wear.compose.material)
    api(libs.androidx.wear.compose.foundation)
    api(libs.androidx.glance.wear.tiles)

    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.androidx.lifecycle.runtime.ktx)

    api(libs.patreonSupportersRetrieval)

    api(libs.gson.fire)

    api(libs.androidChart)

    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.android)
    api(libs.kotlinx.coroutines.play.services)
    api(libs.kotlin.reflect)

    api(libs.shizuku.api)
    api(libs.shizuku.provider)

    api(libs.hiddenapibypass)

    api(libs.bugsnag.android)
}

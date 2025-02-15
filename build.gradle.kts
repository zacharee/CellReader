val versionCode by extra(35)
val versionName by extra("0.22.3")
val targetSdk by extra(35)
// This is the number you want to match when downloading the modified SDK JAR.
val compileSdk by extra(35)
val minSdk by extra(24)

val javaVersion by extra(17)

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.bugsnag.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}
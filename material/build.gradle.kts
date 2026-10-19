@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
}

//1.14.0

fun isDevelopBuild() = libs.versions.developerBuild.get().toBoolean()
fun Provider<String>.asInt() = get().toInt()

val srcDirs = arrayOf(
    "com/google/android/material/animation",
    "com/google/android/material/appbar",
    "com/google/android/material/badge",
    "com/google/android/material/behavior",
    "com/google/android/material/bottomappbar",
    "com/google/android/material/bottomnavigation",
    "com/google/android/material/bottomsheet",
    "com/google/android/material/button",
    "com/google/android/material/canvas",
    "com/google/android/material/card",
    "com/google/android/material/carousel",
    "com/google/android/material/checkbox",
    "com/google/android/material/chip",
    "com/google/android/material/circularreveal",
    "com/google/android/material/circularreveal/cardview",
    "com/google/android/material/circularreveal/coordinatorlayout",
    "com/google/android/material/color",
    "com/google/android/material/datepicker",
    "com/google/android/material/dialog",
    "com/google/android/material/divider",
    "com/google/android/material/dockedtoolbar",
    "com/google/android/material/drawable",
    "com/google/android/material/drawerlayout",
    "com/google/android/material/elevation",
    "com/google/android/material/expandable",
    "com/google/android/material/floatingactionbutton",
    "com/google/android/material/floatingtoolbar",
    "com/google/android/material/focus",
    "com/google/android/material/imageview",
    "com/google/android/material/internal",
    "com/google/android/material/loadingindicator",
    "com/google/android/material/listitem",
    "com/google/android/material/lists",
    "com/google/android/material/materialswitch",
    "com/google/android/material/math",
    "com/google/android/material/menu",
    "com/google/android/material/motion",
    "com/google/android/material/navigation",
    "com/google/android/material/navigationrail",
    "com/google/android/material/overflow",
    "com/google/android/material/progressindicator",
    "com/google/android/material/radiobutton",
    "com/google/android/material/resources",
    "com/google/android/material/ripple",
    "com/google/android/material/search",
    "com/google/android/material/shape",
    "com/google/android/material/shadow",
    "com/google/android/material/sidesheet",
    "com/google/android/material/slider",
    "com/google/android/material/snackbar",
    "com/google/android/material/stateful",
    "com/google/android/material/switchmaterial",
    "com/google/android/material/tabs",
    "com/google/android/material/textfield",
    "com/google/android/material/textview",
    "com/google/android/material/theme",
    "com/google/android/material/theme/overlay",
    "com/google/android/material/timepicker",
    "com/google/android/material/tooltip",
    "com/google/android/material/transition",
    "com/google/android/material/transformation",
    "com/google/android/material/typography",
)

android {
    namespace = "com.google.android.material"

    sourceSets {
        named("main") {
            java.directories.clear()
            java.directories.add("java")
            srcDirs.forEach {
                res.directories.add("java/$it/res")
                res.directories.add("java/$it/res-public")
            }
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        include(srcDirs.map { "$it/**/*.java" })
        exclude("**/build/**")
    }

    compileSdk {
        version = preview(libs.versions.appCompileSDK.get())
    }
    buildToolsVersion = libs.versions.appBuildTools.get()

    defaultConfig {
        minSdk =
            if (isDevelopBuild()) libs.versions.appMinSDKDevelop.asInt() else libs.versions.appMinSDKNotDevelop.asInt()
        vectorDrawables.generatedDensities?.clear()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        encoding = "utf-8"
        aaptOptions.additionalParameters.add("--no-version-vectors")
    }

    lint {
        checkOnly.clear()
        checkOnly.add("NewApi")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.parcelize.runtime)
    compileOnly(libs.kotlin.annotations)
    implementation(libs.jspecify)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.customview)
    implementation(libs.androidx.customview.poolingcontainer)
    implementation(libs.androidx.graphics.shapes)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.dynamicanimation)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.androidx.vectordrawable)
    implementation(libs.androidx.transition)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.errorprone.annotations)
    implementation(libs.androidx.resourceinspection.annotation)
    implementation(project(":recyclerview"))
}

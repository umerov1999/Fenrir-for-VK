plugins {
    id("com.android.library")
}

//1.7.0-alpha02

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
    "com/google/android/material/checkbox",
    "com/google/android/material/chip",
    "com/google/android/material/circularreveal",
    "com/google/android/material/circularreveal/cardview",
    "com/google/android/material/circularreveal/coordinatorlayout",
    "com/google/android/material/color",
    "com/google/android/material/datepicker",
    "com/google/android/material/dialog",
    "com/google/android/material/divider",
    "com/google/android/material/drawable",
    "com/google/android/material/elevation",
    "com/google/android/material/expandable",
    "com/google/android/material/floatingactionbutton",
    "com/google/android/material/imageview",
    "com/google/android/material/internal",
    "com/google/android/material/materialswitch",
    "com/google/android/material/math",
    "com/google/android/material/lists",
    "com/google/android/material/menu",
    "com/google/android/material/motion",
    "com/google/android/material/navigation",
    "com/google/android/material/navigationrail",
    "com/google/android/material/progressindicator",
    "com/google/android/material/radiobutton",
    "com/google/android/material/resources",
    "com/google/android/material/ripple",
    "com/google/android/material/shape",
    "com/google/android/material/shadow",
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
    sourceSets.getByName("main") {
        manifest.srcFile("java/com/google/android/material/AndroidManifest.xml")
        java.srcDir("java")
        java.includes.addAll(srcDirs.map { "$it/**/*.java" })
        java.excludes.add("**/build/**")
        srcDirs.forEach {
            res.srcDir("java/$it/res")
            res.srcDir("java/$it/res-public")
        }
    }
    compileSdk = MakeConfig.appCompileSDK
    buildToolsVersion = MakeConfig.appBuildTools

    defaultConfig {
        minSdk = MakeConfig.appMinSDK
        targetSdk = MakeConfig.appTargetSDK
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        aaptOptions.additionalParameters.add("--no-version-vectors")
        encoding = "utf-8"
    }
}

dependencies {
    api("androidx.appcompat:appcompat:${MakeConfig.appcompatVersion}")
    api("androidx.annotation:annotation:${MakeConfig.annotationVersion}")
    api("androidx.core:core:${MakeConfig.coreVersion}")
    api("androidx.activity:activity:${MakeConfig.activityVersion}")
    api("androidx.fragment:fragment:${MakeConfig.fragmentVersion}")
    api("androidx.cardview:cardview:1.0.0")
    api("androidx.dynamicanimation:dynamicanimation:1.1.0-alpha03")
    api("androidx.constraintlayout:constraintlayout:${MakeConfig.constraintlayoutVersion}")
    api("androidx.recyclerview:recyclerview:${MakeConfig.recyclerviewVersion}")
    api("androidx.coordinatorlayout:coordinatorlayout:${MakeConfig.coordinatorlayoutVersion}")
    api("androidx.vectordrawable:vectordrawable:${MakeConfig.vectordrawableVersion}")
    api("androidx.transition:transition:1.4.1")
    api("androidx.lifecycle:lifecycle-runtime:${MakeConfig.lifecycleVersion}")
    api("androidx.drawerlayout:drawerlayout:${MakeConfig.drawerlayoutVersion}")
    api("androidx.viewpager2:viewpager2:${MakeConfig.viewpager2Version}")
}

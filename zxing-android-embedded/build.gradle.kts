plugins {
    id("com.android.library")
}

android {
    compileSdk = MakeConfig.appCompileSDK
    buildToolsVersion = MakeConfig.appBuildTools

    defaultConfig {
        resourcePrefix = "zxing_"
        minSdk = MakeConfig.appMinSDK
        targetSdk = MakeConfig.appTargetSDK
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        encoding = "utf-8"
    }
}


dependencies {
    implementation("androidx.appcompat:appcompat:${MakeConfig.appcompatVersion}")
    implementation("androidx.core:core:${MakeConfig.coreVersion}")
    implementation("androidx.activity:activity:${MakeConfig.activityVersion}")
    implementation("androidx.fragment:fragment:${MakeConfig.fragmentVersion}")
    implementation(if (MakeConfig.appMinSDK >= 26) "com.google.zxing:core:3.4.1" else "com.google.zxing:core:3.3.3")
    implementation("androidx.annotation:annotation:${MakeConfig.annotationVersion}")
    implementation(project("path" to ":material"))
    implementation("androidx.constraintlayout:constraintlayout:${MakeConfig.constraintlayoutVersion}")
}

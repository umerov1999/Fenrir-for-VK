plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
}

android {
    compileSdk = MakeConfig.appCompileSDK
    buildToolsVersion = MakeConfig.appBuildTools

    defaultConfig {
        minSdk = MakeConfig.appMinSDK
        targetSdk = MakeConfig.appTargetSDK
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        encoding = "utf-8"
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${MakeConfig.kotlin_version}")
    implementation("org.jetbrains.kotlin:kotlin-parcelize-runtime:${MakeConfig.kotlin_version}")
    implementation("org.jetbrains.kotlin:kotlin-android-extensions-runtime:${MakeConfig.kotlin_version}")
    api("androidx.annotation:annotation:${MakeConfig.annotationVersion}")
    api("androidx.appcompat:appcompat:${MakeConfig.appcompatVersion}")
    api("androidx.core:core-ktx:${MakeConfig.coreVersion}")
    implementation("androidx.collection:collection-ktx:${MakeConfig.collectionVersion}")
    implementation("androidx.activity:activity-ktx:${MakeConfig.activityVersion}")
    api("androidx.fragment:fragment-ktx:${MakeConfig.fragmentVersion}")
    api("androidx.recyclerview:recyclerview:${MakeConfig.recyclerviewVersion}")
    api("androidx.constraintlayout:constraintlayout:${MakeConfig.constraintlayoutVersion}")
    implementation(project("path" to ":material"))
}

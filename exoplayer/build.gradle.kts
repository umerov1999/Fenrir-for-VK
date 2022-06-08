plugins {
    id("com.android.library")
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
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-annotations-jvm:${MakeConfig.kotlin_version}")
    implementation("androidx.annotation:annotation:${MakeConfig.annotationVersion}")
    compileOnly("org.checkerframework:checker-qual-android:3.22.0")
    compileOnly("org.checkerframework:checker-compat-qual:2.5.5")
    implementation("com.google.guava:guava:${MakeConfig.guavaVersion}")
    implementation("androidx.core:core-ktx:${MakeConfig.coreVersion}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${MakeConfig.lifecycleVersion}")
    implementation("androidx.lifecycle:lifecycle-common:${MakeConfig.lifecycleVersion}")
    implementation("androidx.collection:collection-ktx:${MakeConfig.collectionVersion}")
}

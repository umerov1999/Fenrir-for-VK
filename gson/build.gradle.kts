plugins {
    id("com.android.library")
}

android {
    compileSdk = MakeConfig.appCompileSDK
    buildToolsVersion = MakeConfig.appBuildTools

    defaultConfig {
        minSdk = MakeConfig.appMinSDK
        targetSdk = MakeConfig.appTargetSDK
        buildConfigField("String", "VERSION", "\"2.9.0\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        encoding = "utf-8"
    }
}

dependencies {
    implementation("androidx.annotation:annotation:${MakeConfig.annotationVersion}")
}

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
    implementation("androidx.appcompat:appcompat:${MakeConfig.appcompatVersion}")
    implementation("androidx.recyclerview:recyclerview:${MakeConfig.recyclerviewVersion}")
    implementation("androidx.core:core:${MakeConfig.coreVersion}")
    implementation("androidx.activity:activity:${MakeConfig.activityVersion}")
    implementation("androidx.fragment:fragment:${MakeConfig.fragmentVersion}")
    implementation(project("path" to ":material"))
    implementation("androidx.constraintlayout:constraintlayout:${MakeConfig.constraintlayoutVersion}")
    implementation("io.reactivex.rxjava3:rxjava:${MakeConfig.rxJavaVersion}")
    implementation("io.reactivex.rxjava3:rxandroid:${MakeConfig.rxAndroidVersion}")
    implementation("androidx.exifinterface:exifinterface:${MakeConfig.exifinterfaceVersion}")
}

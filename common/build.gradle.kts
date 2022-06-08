plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdk = MakeConfig.appCompileSDK
    buildToolsVersion = MakeConfig.appBuildTools

    defaultConfig {
        minSdk = MakeConfig.appMinSDK
        targetSdk = MakeConfig.appTargetSDK
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-Xlint:deprecation")
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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${MakeConfig.kotlin_coroutines}")
    compileOnly("org.jetbrains.kotlin:kotlin-annotations-jvm:${MakeConfig.kotlin_version}")
    implementation("androidx.appcompat:appcompat:${MakeConfig.appcompatVersion}")
    implementation("androidx.recyclerview:recyclerview:${MakeConfig.recyclerviewVersion}")
    implementation("androidx.core:core:${MakeConfig.coreVersion}")
    implementation("androidx.activity:activity:${MakeConfig.activityVersion}")
    implementation("androidx.fragment:fragment:${MakeConfig.fragmentVersion}")
    implementation(project("path" to ":material"))
    implementation("androidx.constraintlayout:constraintlayout:${MakeConfig.constraintlayoutVersion}")
}

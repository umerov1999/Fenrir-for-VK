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
    implementation("androidx.annotation:annotation:${MakeConfig.annotationVersion}")
    implementation("androidx.exifinterface:exifinterface:${MakeConfig.exifinterfaceVersion}")
    implementation("androidx.lifecycle:lifecycle-common:${MakeConfig.lifecycleVersion}")
    implementation("androidx.core:core-ktx:${MakeConfig.coreVersion}")
    implementation("com.squareup.okhttp3:okhttp-android:${MakeConfig.okhttpLibraryVersion}")
    implementation("com.squareup.okio:okio:${MakeConfig.okioVersion}")
}

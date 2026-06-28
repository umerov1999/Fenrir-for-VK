@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
}

fun isDevelopBuild(): Boolean = libs.versions.developerBuild.get().toBoolean()
fun isDevelopBuildString(): String = libs.versions.developerBuild.get()
fun Provider<String>.asInt() = get().toInt()

android {
    namespace = "dev.ragnarok.filegallery"
    buildFeatures {
        aidl = true
        buildConfig = true
    }
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
        resources.excludes.add("META-INF/notice.txt")
        resources.excludes.add("META-INF/license.txt")
        resources.excludes.add("META-INF/LICENSE")
        resources.excludes.add("META-INF/NOTICE")
        resources.excludes.add("META-INF/*.version")
        resources.excludes.add("META-INF/versions/**")
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    compileSdk {
        version = preview(libs.versions.appCompileSDK.get())
    }
    buildToolsVersion = libs.versions.appBuildTools.get()
    ndkVersion = libs.versions.appNdk.get()

    defaultConfig {
        minSdk =
            if (isDevelopBuild()) libs.versions.appMinSDKDevelop.asInt() else libs.versions.appMinSDKNotDevelop.asInt()
        targetSdk = libs.versions.appTargetSDK.asInt()
        versionCode = libs.versions.appVersionCode.asInt()
        versionName = libs.versions.appVersionName.get()
        buildConfigField("int", "TARGET_SDK", libs.versions.appTargetSDK.get())
        buildConfigField("boolean", "FORCE_DEVELOPER_MODE", isDevelopBuildString())

        applicationId = "dev.ragnarok.filegallery"

        ndk {
            abiFilters.clear()
            //noinspection ChromeOsAbiSupport
            abiFilters.addAll(
                (if (isDevelopBuild()) libs.versions.targetAbiDevelop else libs.versions.targetAbiNotDevelop).get()
                    .split(",")
            )
        }
        androidResources.localeFilters.clear()
        androidResources.localeFilters.addAll(setOf("en", "ru"))
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
    }

    compileOptions {
        //coreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        encoding = "utf-8"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            //noinspection NotShrinkingResources
            isShrinkResources = false
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.addAll(
            setOf(
                "-opt-in=kotlin.contracts.ExperimentalContracts",
                "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.serialization.internal.CoreFriendModuleApi",
                "-opt-in=kotlinx.serialization.SealedSerializationApi",
                "-opt-in=kotlinx.serialization.InternalSerializationApi",
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlin.ExperimentalStdlibApi"
            )
        )
    }
}

dependencies {
    implementation(fileTree("../compiled_native") { include("*.jar", "*.aar") })
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.parcelize.runtime)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    compileOnly(libs.kotlin.annotations)
    implementation(libs.guava)
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.vectordrawable)
    implementation(libs.androidx.emoji2)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.customview)
    implementation(libs.androidx.customview.poolingcontainer)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.exifinterface)
    //implementation(project(":native"))
    implementation(project(":picasso3"))
    implementation(project(":recyclerview"))
    implementation(project(":material"))
    implementation(project(":preference"))
    implementation(project(":camera2"))
    implementation(libs.okhttp)
    //implementation(libs.okhttp.logginginterceptor)
    implementation(libs.okio)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.drawerlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.collection)
    implementation(libs.androidx.savedstate)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.errorprone.annotations)

    modules {
        module("com.google.guava:listenablefuture") {
            replacedBy("com.google.guava:guava", "listenablefuture is part of guava")
        }
    }
    //coreLibraryDesugaring(libs.desugarjdklibs)
}

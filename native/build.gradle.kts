@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
}

fun isDevelopBuild() = libs.versions.developerBuild.get().toBoolean()
fun Provider<String>.asInt() = get().toInt()

android {
    namespace = "dev.ragnarok.fenrir.module"
    compileSdk {
        version = preview(libs.versions.appCompileSDK.get())
    }
    buildToolsVersion = libs.versions.appBuildTools.get()
    ndkVersion = libs.versions.appNdk.get()

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk =
            if (isDevelopBuild()) libs.versions.appMinSDKDevelop.asInt() else libs.versions.appMinSDKNotDevelop.asInt()
        buildTypes {
            release {
                externalNativeBuild {
                    cmake {
                        arguments.addAll(
                            setOf(
                                "-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON",
                                "-DANDROID_STL=c++_static",
                                "-DANDROID_PLATFORM=android-$minSdk",
                                "-DCMAKE_BUILD_TYPE=Release"
                            )
                        )
                    }
                }
            }
            debug {
                externalNativeBuild {
                    cmake {
                        arguments.addAll(
                            setOf(
                                "-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON",
                                "-DANDROID_STL=c++_static",
                                "-DANDROID_PLATFORM=android-$minSdk",
                                "-DCMAKE_BUILD_TYPE=Debug"
                            )
                        )
                    }
                }
            }
        }
        ndk {
            abiFilters.clear()
            //noinspection ChromeOsAbiSupport
            abiFilters.addAll(setOf("arm64-v8a", "armeabi-v7a", "x86_64"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        encoding = "utf-8"
    }

    externalNativeBuild {
        cmake {
            version = libs.versions.appCMake.get()
            path = file("src/main/jni/CMakeLists.txt")
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.addAll(
            setOf(
                "-opt-in=kotlin.contracts.ExperimentalContracts",
                "-opt-in=kotlinx.coroutines.DelicateCoroutinesApi"
            )
        )
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.parcelize.runtime)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    compileOnly(libs.kotlin.annotations)
    implementation(libs.guava)
    implementation(libs.androidx.core)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.collection)
    implementation(libs.okio)
    implementation(libs.okhttp)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.errorprone.annotations)

    modules {
        module("com.google.guava:listenablefuture") {
            replacedBy("com.google.guava:guava", "listenablefuture is part of guava")
        }
    }
}

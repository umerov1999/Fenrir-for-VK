import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
}

fun isDevelopBuild() = libs.versions.developerBuild.get().toBoolean()
fun Provider<String>.asInt() = get().toInt()

android {
    namespace = "me.minetsh.imaging"
    compileSdk {
        version = release(libs.versions.appCompileSDK.asInt()) {
            minorApiLevel = libs.versions.appCompileSDKMinor.asInt()
        }
    }
    buildToolsVersion = libs.versions.appBuildTools.get()

    defaultConfig {
        minSdk =
            if (isDevelopBuild()) libs.versions.appMinSDKDevelop.asInt() else libs.versions.appMinSDKNotDevelop.asInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        encoding = "utf-8"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.parcelize.runtime)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    compileOnly(libs.kotlin.annotations)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.appcompat)
    implementation(project(":recyclerview"))
    implementation(libs.androidx.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment)
    implementation(project(":material"))
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.transition)
}
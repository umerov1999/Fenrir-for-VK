import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
}

fun isDevelopBuild(): Boolean = libs.versions.developerBuild.get().toBoolean()
fun isDevelopBuildString(): String = libs.versions.developerBuild.get()
fun String.commaString() = "\"${this}\""
fun Provider<String>.commaString() = "\"${this.get()}\""
fun Provider<String>.asInt() = get().toInt()

android {
    namespace = "com.google.firebase.installations"

    compileSdk {
        version = preview(libs.versions.appCompileSDK.get())
    }
    buildToolsVersion = libs.versions.appBuildTools.get()

    buildFeatures {
        buildConfig = true
    }

    flavorDimensions.add("type")
    productFlavors {
        create("fenrir") {
            dimension = "type"
            buildConfigField(
                "String",
                "PATCH_APP_ID",
                libs.versions.vkAppPackage.commaString()
            )
            buildConfigField(
                "String",
                "PATCH_APP_FINGERPRINT",
                libs.versions.vkAppFingerPrint.commaString()
            )
        }
        create("kate") {
            dimension = "type"
            buildConfigField(
                "String",
                "PATCH_APP_ID",
                libs.versions.kateAppPackage.commaString()
            )
            buildConfigField(
                "String",
                "PATCH_APP_FINGERPRINT",
                libs.versions.kateAppFingerPrint.commaString()
            )
        }
    }

    defaultConfig {
        minSdk =
            if (isDevelopBuild()) libs.versions.appMinSDKDevelop.asInt() else libs.versions.appMinSDKNotDevelop.asInt()
        buildConfigField("String", "VERSION_NAME_INSTALLATION", "19.1.1".commaString())
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
    compileOnly(libs.kotlin.annotations)
    implementation(libs.jspecify)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core)
    implementation(libs.androidx.collection)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment)
    implementation(libs.firebase.common)
    implementation(libs.firebase.installations.interop)
    implementation(libs.firebase.components)
    implementation(libs.firebase.annotations)
    implementation(libs.gms.tasks)

    compileOnly(libs.auto.value.annotations)
    annotationProcessor(libs.auto.value)
}

@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.gms.services)
}

fun isDevelopBuild(): Boolean = libs.versions.developerBuild.get().toBoolean()
fun isDevelopBuildString(): String = libs.versions.developerBuild.get()
fun Provider<String>.commaString() = "\"${this.get()}\""
fun Provider<String>.asInt() = get().toInt()

android {
    namespace = "dev.ragnarok.fenrir"
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

        buildConfigField("String", "SERVICE_TOKEN", libs.versions.vkServiceToken.commaString())
        buildConfigField("String", "FAKE_ABI", libs.versions.fakeAbi.commaString())
        buildConfigField("String", "FAKE_DEVICE", libs.versions.fakeDevice.commaString())
        buildConfigField("int", "TARGET_SDK", libs.versions.appTargetSDK.get())
        buildConfigField("boolean", "FORCE_DEVELOPER_MODE", isDevelopBuildString())
        buildConfigField("int", "VK_API_GROUP_ADMIN_APP_ID", libs.versions.vkGroupAdminAppId.get())

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

    flavorDimensions.add("type")
    productFlavors {
        create("fenrir") {
            applicationId = "dev.ragnarok.fenrir"
            dimension = "type"
            buildConfigField("int", "VK_API_APP_ID", libs.versions.vkAppId.get())
            buildConfigField("int", "DEFAULT_ACCOUNT_TYPE", "1") //VK_ANDROID
            buildConfigField(
                "String",
                "VK_CLIENT_SECRET",
                libs.versions.vkClientSecret.commaString()
            )
        }
        create("kate") {
            applicationId = "dev.ragnarok.fenrir_kate"
            dimension = "type"
            buildConfigField("int", "VK_API_APP_ID", libs.versions.kateAppId.get())
            buildConfigField("int", "DEFAULT_ACCOUNT_TYPE", "3") //KATE
            buildConfigField(
                "String",
                "VK_CLIENT_SECRET",
                libs.versions.kateClientSecret.commaString()
            )
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
    implementation(libs.androidx.webkit)
    implementation(libs.androidx.exifinterface)
    implementation(libs.firebase.datatransport)
    implementation(libs.firebase.messaging) {
        exclude("com.google.firebase", "firebase-installations")
    }
    //implementation(project(":native"))
    implementation(project(":fenrir_common"))
    implementation(project(":firebase-installations"))
    implementation(project(":picasso3"))
    implementation(project(":image"))
    implementation(project(":recyclerview"))
    implementation(project(":material"))
    implementation(project(":preference"))
    implementation(project(":camera2"))
    implementation(libs.okhttp)
    //implementation(libs.okhttp.logginginterceptor)
    implementation(libs.okio)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.hls)
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
    implementation(libs.gms.base)
    //implementation("com.vk.id.captcha:vkid-captcha:0.0.5")

    modules {
        module("com.google.guava:listenablefuture") {
            replacedBy("com.google.guava:guava", "listenablefuture is part of guava")
        }
    }
    //coreLibraryDesugaring(libs.desugarjdklibs)
}

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.devtools.ksp)
}

//1.6.1

fun isDevelopBuild() = libs.versions.developerBuild.get().toBoolean()
fun Provider<String>.asInt() = get().toInt()

android {
    namespace = "androidx.camera"
    compileSdk {
        version = preview(libs.versions.appCompileSDK.get())
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
        freeCompilerArgs.addAll(
            setOf(
                "-XXLanguage:+PropertyParamAnnotationDefaultTargetMode",
                "-jvm-default=no-compatibility"
            )
        )
    }
}

ksp {
    arg("dagger.fastInit", "enabled")
    arg("dagger.fullBindingGraphValidation", "ERROR")
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.parcelize.runtime)
    compileOnly(libs.kotlin.annotations)
    implementation(libs.jspecify)
    compileOnly(libs.checkerqual)
    compileOnly(libs.auto.value.annotations)
    implementation(libs.guava)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core)
    implementation(libs.androidx.core.backported.fixes)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.collection)
    implementation(libs.androidx.tracing)
    implementation(libs.androidx.media3.muxer)
    implementation(libs.errorprone.annotations)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.concurrent.futures)
    implementation(libs.kotlinx.atomicfu)
    implementation(libs.dagger)
    annotationProcessor(libs.auto.value)

    ksp(libs.daggerCompiler)

    modules {
        module("com.google.guava:listenablefuture") {
            replacedBy("com.google.guava:guava", "listenablefuture is part of guava")
        }
    }
}

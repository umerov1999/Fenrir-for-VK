plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlinx-serialization")
}

fun selectAppSignVersion(config: com.android.build.api.dsl.ApkSigningConfig) {
    config.enableV1Signing = MakeConfig.appMinSDK < 24
    config.enableV2Signing = MakeConfig.appMinSDK < 28
    config.enableV3Signing = MakeConfig.appMinSDK >= 28
    config.enableV4Signing = false
}

android {
    packagingOptions {
        resources.excludes.addAll(
            listOf(
                "META-INF/notice.txt",
                "META-INF/license.txt",
                "META-INF/LICENSE",
                "META-INF/NOTICE",
                "META-INF/DEPENDENCIES",
                "META-INF/*.version"
            )
        )
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    fun asStringVar(str: String): String {
        return "\"$str\""
    }

    compileSdk = MakeConfig.appCompileSDK
    buildToolsVersion = MakeConfig.appBuildTools
    ndkVersion = MakeConfig.appNdk

    defaultConfig {
        minSdk = MakeConfig.appMinSDK
        targetSdk = MakeConfig.appTargetSDK
        versionCode = MakeConfig.appVersionCode
        versionName = MakeConfig.appVersionName

        buildConfigField("String", "SERVICE_TOKEN", asStringVar(MakeConfig.Props.vk_service_token))
        buildConfigField("String", "FAKE_ABI", asStringVar(MakeConfig.Props.fake_abi))
        buildConfigField("String", "FAKE_DEVICE", asStringVar(MakeConfig.Props.fake_device))
        buildConfigField(
            "String",
            "FCM_SESSION_ID_GEN_URL",
            asStringVar(MakeConfig.Props.fcm_session_id_gen_url)
        )
        buildConfigField("boolean", "MANAGE_SCOPED_STORAGE", "true")

        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a", "x86_64"))
        }
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.addAll(listOf("-Xmaxwarns", "1000", "-Xmaxerrs", "1000"))
    }

    tasks.withType<com.android.build.gradle.tasks.MergeResources> {
        mustRunAfter(tasks.withType<com.google.gms.googleservices.GoogleServicesTask>())
    }

    compileOptions {
        // Flag to enable support for the new language APIs
        isCoreLibraryDesugaringEnabled = MakeConfig.appMinSDK < 26

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        encoding = "utf-8"
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf(
            "-opt-in=kotlin.contracts.ExperimentalContracts",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-opt-in=kotlin.RequiresOptIn"
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
        }

        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    flavorDimensions.add("type")
    productFlavors {
        create("fenrir") {
            applicationId = "dev.ragnarok.fenrir"
            dimension = "type"
            buildConfigField("int", "VK_API_APP_ID", MakeConfig.Props.vk_app_id)
            buildConfigField("int", "DEFAULT_ACCOUNT_TYPE", "AccountType.VK_ANDROID")
            buildConfigField(
                "String",
                "VK_CLIENT_SECRET",
                asStringVar(MakeConfig.Props.vk_client_secret)
            )
        }
        create("kate") {
            applicationId = "dev.ragnarok.fenrir_kate"
            dimension = "type"
            buildConfigField("int", "VK_API_APP_ID", MakeConfig.Props.kate_app_id)
            buildConfigField("int", "DEFAULT_ACCOUNT_TYPE", "AccountType.KATE")
            buildConfigField(
                "String",
                "VK_CLIENT_SECRET",
                asStringVar(MakeConfig.Props.kate_client_secret)
            )
        }
    }
}

dependencies {
    implementation(fileTree("include" to "*.aar", "dir" to "libs"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${MakeConfig.kotlin_version}")
    implementation("org.jetbrains.kotlin:kotlin-parcelize-runtime:${MakeConfig.kotlin_version}")
    implementation("org.jetbrains.kotlin:kotlin-android-extensions-runtime:${MakeConfig.kotlin_version}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${MakeConfig.kotlin_coroutines}")
    compileOnly("org.jetbrains.kotlin:kotlin-annotations-jvm:${MakeConfig.kotlin_version}")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")
    implementation("androidx.core:core-ktx:${MakeConfig.coreVersion}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${MakeConfig.lifecycleVersion}")
    implementation("androidx.lifecycle:lifecycle-common:${MakeConfig.lifecycleVersion}")
    implementation("androidx.annotation:annotation:${MakeConfig.annotationVersion}")
    implementation("androidx.recyclerview:recyclerview:${MakeConfig.recyclerviewVersion}")
    implementation("androidx.viewpager2:viewpager2:${MakeConfig.viewpager2Version}")
    implementation("androidx.vectordrawable:vectordrawable:${MakeConfig.vectordrawableVersion}")
    implementation("androidx.appcompat:appcompat:${MakeConfig.appcompatVersion}")
    implementation("androidx.customview:customview:1.1.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")
    implementation("androidx.browser:browser:1.4.0")
    implementation("androidx.webkit:webkit:1.4.0")
    implementation("androidx.exifinterface:exifinterface:${MakeConfig.exifinterfaceVersion}")
    implementation("io.reactivex.rxjava3:rxjava:${MakeConfig.rxJavaVersion}")
    implementation("io.reactivex.rxjava3:rxandroid:${MakeConfig.rxAndroidVersion}")
    implementation("com.google.firebase:firebase-database:20.0.5")
    implementation("com.google.firebase:firebase-datatransport:18.1.3")
    implementation("com.google.firebase:firebase-messaging:23.0.2") {
        exclude("com.google.firebase", "firebase-installations")
    }
    //implementation(project("path" to ":libfenrir"))
    implementation(project("path" to ":common"))
    implementation(project("path" to ":firebase-installations"))
    implementation(project("path" to ":picasso"))
    implementation(project("path" to ":image"))
    implementation(project("path" to ":material"))
    implementation(project("path" to ":gson"))
    implementation(project("path" to ":preference"))
    implementation(project("path" to ":retrofit"))
    implementation("com.squareup.okhttp3:okhttp:${MakeConfig.okhttpLibraryVersion}")
    implementation("com.squareup.okhttp3:logging-interceptor:${MakeConfig.okhttpLibraryVersion}")
    implementation("com.squareup.okio:okio:${MakeConfig.okioVersion}")
    implementation("com.google.android.exoplayer:exoplayer-core:${MakeConfig.exoLibraryVersion}")
    implementation("com.google.android.exoplayer:exoplayer-hls:${MakeConfig.exoLibraryVersion}")
    implementation("androidx.constraintlayout:constraintlayout:${MakeConfig.constraintlayoutVersion}")
    implementation("androidx.biometric:biometric-ktx:1.2.0-alpha04")
    implementation("androidx.media:media:1.6.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:${MakeConfig.coordinatorlayoutVersion}")
    implementation("androidx.activity:activity-ktx:${MakeConfig.activityVersion}")
    implementation("androidx.fragment:fragment-ktx:${MakeConfig.fragmentVersion}")
    implementation("androidx.work:work-runtime-ktx:2.8.0-alpha02")
    implementation("com.google.guava:guava:${MakeConfig.guavaVersion}")
    implementation("androidx.drawerlayout:drawerlayout:${MakeConfig.drawerlayoutVersion}")
    implementation("androidx.loader:loader:1.1.0")
    implementation("androidx.collection:collection-ktx:${MakeConfig.collectionVersion}")
    implementation("androidx.savedstate:savedstate-ktx:${MakeConfig.savedStateVersion}")
    implementation("androidx.camera:camera-core:${MakeConfig.cameraVersion}")
    implementation("androidx.camera:camera-lifecycle:${MakeConfig.cameraVersion}")
    implementation("androidx.camera:camera-view:${MakeConfig.cameraVersion}")
    implementation("androidx.camera:camera-camera2:${MakeConfig.cameraVersion}")
    implementation(if (MakeConfig.appMinSDK >= 26) "com.google.zxing:core:3.5.0" else "com.google.zxing:core:3.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.3")
}
apply(plugin = "com.google.gms.google-services")

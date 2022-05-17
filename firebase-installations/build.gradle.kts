plugins {
    id("com.android.library")
}

android {
    compileSdk = MakeConfig.appCompileSDK
    buildToolsVersion = MakeConfig.appBuildTools

    fun asStringVar(str: String): String {
        return "\"$str\""
    }

    flavorDimensions.add("type")
    productFlavors {
        create("fenrir") {
            dimension = "type"
            buildConfigField(
                "String",
                "PATCH_APP_ID",
                asStringVar(MakeConfig.FireBasePatch.vk_app_package)
            )
            buildConfigField(
                "String",
                "PATCH_APP_FINGERPRINT",
                asStringVar(MakeConfig.FireBasePatch.vk_app_finger_print)
            )
        }
        create("kate") {
            dimension = "type"
            buildConfigField(
                "String",
                "PATCH_APP_ID",
                asStringVar(MakeConfig.FireBasePatch.kate_app_package)
            )
            buildConfigField(
                "String",
                "PATCH_APP_FINGERPRINT",
                asStringVar(MakeConfig.FireBasePatch.kate_app_finger_print)
            )
        }
    }

    defaultConfig {
        minSdk = MakeConfig.appMinSDK
        targetSdk = MakeConfig.appTargetSDK
        buildConfigField("String", "VERSION_NAME", asStringVar("17.0.1"))
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        encoding = "utf-8"
    }
}

dependencies {
    implementation("com.google.firebase:firebase-common:20.1.1")
    implementation("com.google.firebase:firebase-installations-interop:17.0.1")
    implementation("com.google.firebase:firebase-components:17.0.0")
    implementation("com.google.android.gms:play-services-tasks:18.0.1")

    compileOnly("com.google.auto.value:auto-value-annotations:1.9")
    annotationProcessor("com.google.auto.value:auto-value:1.9")
}

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.4.0"
    id("org.jetbrains.intellij.platform") version "2.16.0"
}

group = "dev.ragnarok"
version = "1.0"

repositories {
    mavenCentral()
    intellijPlatform {
        jetbrainsRuntime()
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea("2026.1.2") {
            useInstaller = false
            useCache = false
        }
        pluginVerifier()
        zipSigner()
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

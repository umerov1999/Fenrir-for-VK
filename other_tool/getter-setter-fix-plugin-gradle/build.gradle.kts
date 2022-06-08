import org.jetbrains.intellij.IntelliJPluginExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

apply {
    plugin("kotlin")
    plugin("org.jetbrains.intellij")
}

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("gradle.plugin.org.jetbrains.intellij.plugins:gradle-intellij-plugin:latest.release")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
}

group = "dev.ragnarok"
version = "1.0"

configure<IntelliJPluginExtension> {
    version = "2021.2.1"
}
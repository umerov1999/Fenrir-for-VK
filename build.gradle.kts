// Top-level build file where you can add configuration options common to all subprojects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.gms.services) apply false
}

tasks.register("clean") {
    description = "Clean build directories"
}.configure {
    delete(rootProject.layout.buildDirectory.asFile)
}

tasks.withType<JavaCompile>().configureEach {
    options.isFork = true
    options.compilerArgs.addAll(listOf("-Xmaxwarns", "1000", "-Xmaxerrs", "1000"))
    //options.compilerArgs.add("-Xlint:deprecation")
}
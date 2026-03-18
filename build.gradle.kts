plugins {
    id("common-config")
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
}

allprojects {
    apply {
        plugin("common-config")
    }
}

tasks.register("check") {
    dependsOn(gradle.includedBuild("plugins").task(":check"))
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}

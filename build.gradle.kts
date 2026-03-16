plugins {
    id("common-config")
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
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

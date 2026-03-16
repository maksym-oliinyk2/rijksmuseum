plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

kotlin {
    // set JDK for the whole project,
    // see https://kotlinlang.org/docs/gradle-configure-project.html#gradle-java-toolchains-support
    jvmToolchain(17)
}

dependencies {
    implementation(libs.convention.kotlin)
    implementation(libs.convention.detekt)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
}

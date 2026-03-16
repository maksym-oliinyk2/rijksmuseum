plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

kotlin {
    // set JDK for the whole project,
    // see https://kotlinlang.org/docs/gradle-configure-project.html#gradle-java-toolchains-support
    jvmToolchain(17)
}

afterEvaluate {
    tasks.withType<Test>().configureEach {
        val buildDir = File(File(File(rootProject.rootDir.parentFile, "build"), "junit-reports"), project.name)

        description = "$description Also copies test reports to $buildDir"

        reports {
            html.outputLocation.set(File(buildDir, "html"))
        }
    }
}

dependencies {
    implementation(libs.convention.kotlin)
    implementation(libs.convention.detekt)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
}

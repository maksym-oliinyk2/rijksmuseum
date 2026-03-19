plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.buildconfig)
}

group = "io.github.oliinyk.maksym.rijksmuseum"
version = libraryVersion.toVersionName()

kotlin {
    explicitApi()
    applyDefaultHierarchyTemplate()

    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xexpect-actual-classes",
            "-Xconsistent-data-class-copy-visibility"
        )

        if (project.findProperty("enableComposeCompilerLogs").toString().toBoolean()) {
            freeCompilerArgs.addAll(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${project.composeMetricsDir.absolutePath}",
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=${project.composeMetricsDir.absolutePath}",
            )
        }
    }

    android {
        namespace = "io.github.oliinyk.maksym.rijksmuseum"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources.enable = true

        withJava() // enable java compilation support
        withHostTestBuilder {}.configure {}
        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            execution = "HOST"

            managedDevices {
                localDevices {
                    create("pixel5") {
                        device = "Pixel 5"
                        apiLevel = 35
                        systemImageSource = "aosp-atd"
                    }
                }
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "RijksmuseumLib"
            isStatic = true
            binaryOption("bundleId", "io.github.oliinyk.maksym.rijksmuseum.lib")
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(libs.coroutines.core)
                api(libs.compose.ui)
                api(libs.compose.runtime)
                api(libs.compose.foundation)
                api(libs.compose.components.ui.tooling.preview)
                api(libs.tea.core)
                implementation(libs.compose.components.resources)
                implementation(libs.bundles.coil)
                implementation(libs.compose.material)
                implementation(libs.compose.material.icons.extended)
                implementation(libs.stdlib)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.negotiation)
                implementation(libs.ktor.serialization.json)
                implementation(libs.serialization.core)
                implementation(libs.ui.tooling.preview)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.viewmodel.nav3)
                implementation(libs.androidx.lifecycle.runtime)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.androidx.nav3.ui)
                implementation(libs.koin.compose)
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.compose.nav3)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.arrow.core)
                implementation(libs.arrow.coroutines)
                implementation(libs.bundles.coil)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.coroutines.test)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.client.logging)
                implementation(libs.coroutines.android)
            }
        }

        appleMain {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        getByName("androidHostTest") {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.junit)
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.compose.ui.test)
                implementation(libs.compose.test.manifest)
            }
        }
    }
}

buildConfig {
    packageName.set("io.github.oliinyk.maksym.rijksmuseum")
    useKotlinOutput { internalVisibility = true }

    val debugEnabled = project.findProperty("forceDebug")?.toString()
        ?.toBoolean() == true || libraryVersion.isSnapshot

    logger.log(LogLevel.LIFECYCLE, "DEBUG enabled: $debugEnabled")
    buildConfigField("kotlin.Boolean", "DEBUG", "$debugEnabled")
}

compose.resources {
    publicResClass = false
    packageOfResClass = "io.github.oliinyk.maksym.rijksmuseum.res"
}

dependencies {
    androidRuntimeClasspath(libs.ui.tooling)
}

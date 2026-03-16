import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

group = "io.github.oliinyk.maksym.rijksmuseum"
version = "1.0.0"

kotlin {
    //explicitApi()
    jvm()

    androidLibrary {
        namespace = "io.github.oliinyk.maksym.rijksmuseum"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withJava() // enable java compilation support
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {

            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

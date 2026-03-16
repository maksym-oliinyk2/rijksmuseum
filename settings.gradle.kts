pluginManagement {
    includeBuild("plugins")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS

    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "Rijksmuseum"
include(":rijksmuseum-library")
include(":android-app")

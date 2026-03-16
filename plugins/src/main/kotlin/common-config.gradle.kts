import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask

plugins {
    id("io.gitlab.arturbosch.detekt")
}

//noinspection UseTomlInstead
dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.8")
}

tasks.withType<Test>().configureEach {
    reports {
        html.outputLocation.set(htmlTestReportsDir)
    }
}

detekt {
    parallel = true
    ignoreFailures = false
    disableDefaultRuleSets = false
    buildUponDefaultConfig = true
    config.setFrom(detektConfig)
    baseline = file(detektBaseline)
}

tasks.withType<Detekt>().configureEach {
    include("**/*.kt", "**/*.kts")
    exclude("resources/", "**/build/**", "**/test/java/**")
    setSource(files(projectDir))
    reports {
        xml.required.set(false)
        txt.required.set(false)
        html.required.set(true)
    }
}

val detektProjectBaseline by tasks.registering(DetektCreateBaselineTask::class) {
    ignoreFailures.set(true)
    parallel.set(true)
    setSource(files(rootDir))
    config.setFrom(detektConfig)
    baseline.set(detektBaseline)
    include("**/*.kt", "**/*.kts")
    exclude("**/resources/**", "**/build/**")
}

val detektFormat by tasks.registering(Detekt::class) {
    parallel = true
    autoCorrect = true
    ignoreFailures = false
    setSource(files(projectDir))

    include("**/*.kt", "**/*.kts")
    exclude("**/resources/**", "**/build/**")

    config.setFrom(detektConfig)
    baseline.set(detektBaseline)
}

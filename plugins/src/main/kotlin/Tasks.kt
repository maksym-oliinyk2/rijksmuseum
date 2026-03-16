import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestReport
import org.gradle.kotlin.dsl.withType

fun Project.configureTests() = afterEvaluate {
    tasks.withType<Test>().configureEach {
        reports {
            html.outputLocation.set(project.htmlTestReportsDir)
        }
    }
}

fun Project.configureTestReporting() = afterEvaluate {
    tasks.withType<TestReport>().configureEach {
        destinationDirectory.set(project.testReportsDir(project.name))
    }
}

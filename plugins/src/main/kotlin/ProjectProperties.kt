import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import java.io.File
import java.nio.file.Paths

const val CommitHashLength = 6
private const val RefTag = "refs/tags/"

val tag: String?
    get() = getenvSafe("GITHUB_REF")
        ?.takeIf { tag -> tag.startsWith(RefTag) }
        ?.removePrefix(RefTag)

val commitSha: String?
    get() = getenvSafe("GITHUB_SHA")

val projectVersion: Version
    get() = Version(tag, commitSha)

val Project.detektConfig: File
    get() = Paths.get(rootDir.path, "detekt", "detekt-config.yml").toFile()

val Project.detektBaseline: File
    get() = Paths.get(rootDir.path, "detekt", "detekt-baseline.xml").toFile()

val Project.composeMetricsDir: File
    get() = File(layout.buildDirectory.get().asFile, "compose_metrics")

val Project.testReportsDir: Provider<out Directory>
    get() = rootMostProject.layout.buildDirectory.map { it.dir("junit-reports").dir(project.name) }

val Project.htmlTestReportsDir: Provider<out Directory>
    get() = testReportsDir.map { it.dir("html") }

fun Project.testReportsDir(
    vararg subdirs: String,
): Provider<out Directory> = testReportsDir.map { subdirs.fold(it) { acc, path -> acc.dir(path) } }

fun getenvSafe(
    name: String,
): String? =
    System.getenv(name).takeUnless(CharSequence?::isNullOrEmpty)

private val Project.rootMostProject: Project
    get() {
        var root = this

        while (root != root.rootProject) {
            root = root.rootProject
        }

        return root
    }

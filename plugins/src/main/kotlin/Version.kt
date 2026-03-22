data class MajorMinorPatch(
    val major: Int,
    val minor: Int,
    val patch: Int,
)

/**
 * Denotes artifact version as per [sem ver](https://semver.org/) spec.
 * Currently it includes:
 * * Snapshot - `hash-SNAPSHOT` or just `SNAPSHOT`
 * * Stable - `v1.2.3`
 */
sealed class Version {
    abstract val versionCode: Int
}

data class Snapshot(
    val commit: String?,
) : Version() {
    init {
        require(commit == null || commit.length >= CommitHashLength) { "Invalid hash: $commit" }
    }

    override val versionCode: Int
        get() = 1
}

data class Stable(
    val value: String,
    val mainVersion: MajorMinorPatch,
) : Version() {
    companion object {
        fun fromTag(
            rawTag: String,
        ) = Stable(rawTag, StableRegexp.groupValues(rawTag).toMajorMinorPatch())
    }

    override val versionCode: Int
        get() = mainVersion.major * 10000 + mainVersion.minor * 100 + mainVersion.patch
}

fun Version(
    rawTag: String?,
    commit: String?
): Version =
    when {
        rawTag.isNullOrEmpty() -> Snapshot(commit)
        rawTag.matches(StableRegexp) -> Stable.fromTag(rawTag)
        else -> error("Invalid tag: $rawTag")
    }

val Version.isSnapshot: Boolean get() = this is Snapshot

private fun Regex.groupValues(
    s: String,
) = (find(s) ?: error("Couldn't parse string '$s' for regex '$pattern'")).groupValues

private fun List<String>.toMajorMinorPatch() =
    MajorMinorPatch(this[1].toInt(), this[2].toInt(), this[3].toInt())

/**
 * Groups:
 * * 1 major, 123
 * * 2 minor, 123
 * * 3 patch, 123
 */
private val StableRegexp = Regex("^v(\\d+)\\.(\\d+)\\.(\\d+)$")

fun Version.toVersionName(): String =
    when (this) {
        is Snapshot -> commit?.let { sha -> "${sha.take(CommitHashLength)}-SNAPSHOT" } ?: "SNAPSHOT"
        is Stable -> mainVersion.versionName
    }

val MajorMinorPatch.versionName
    get() = "$major.$minor.$patch"

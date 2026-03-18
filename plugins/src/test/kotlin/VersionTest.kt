import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class VersionTest {

    private companion object {
        const val Major = 1
        const val Minor = 2
        const val Patch = 3
        val MajorMinorPatch = MajorMinorPatch(Major, Minor, Patch)
        const val CommitHash = "d41d8cd98f00b204e9800998ecf8427e"
    }

    @Test
    fun `when convert to version, given snapshot version, then parsed as snapshot`() {
        assertEquals(Snapshot(null), Version(null, null))
        assertEquals(Snapshot(null), Version("", null))
        assertEquals(Snapshot(CommitHash), Version("", CommitHash))
    }

    @Test
    fun `when convert to version, given stable version, then parsed as stable`() {
        val tag = "v$Major.$Minor.$Patch"
        assertEquals(Stable(tag, MajorMinorPatch), Version(tag, null))
    }

    @Test
    fun `when convert to version, given invalid version, then parse exception thrown`() {
        assertFailsWith<IllegalStateException> {
            Version("$Major.$Minor.$Patch", null)
        }
    }
}

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.Test

internal class ToVersionNameExtensionTest {

    private companion object {
        const val LongCommitHash = "d41d8cd98f00b204e9800998ecf8427e"
        val ShortCommitHash = LongCommitHash.take(CommitHashLength)
    }

    // Snapshot tests

    @Test
    fun `when convert to version name, given snapshot version and long hash, then short hash used`() =
        assertEquals("$ShortCommitHash-SNAPSHOT", Snapshot(LongCommitHash).toVersionName())

    @Test
    fun `when convert to version name, given snapshot version and null hash, then no hash used`() =
        assertEquals("SNAPSHOT", Snapshot(null).toVersionName())

    @Test
    fun `when convert to version name, given snapshot version and empty hash, then exception thrown`() {
        assertFailsWith<IllegalArgumentException> {
            Snapshot("").toVersionName()
        }
    }

    // Alpha tests

    @Test
    fun `when convert to version name, given parse alpha version from tag, then it's correct`() =
        assertEquals("1.2.3-alpha4", Alpha.fromTag("v1.2.3-alpha4").toVersionName())

    @Test
    fun `when convert to version name, given parse alpha version from invalid tag, then parse exception thrown`() =
        shouldThrowForEach<IllegalStateException>(
            Alpha::fromTag,
            "v1.2.3-alpha4-rc56",
            "1.2.3-alpha4-rc56",
            "v1.2.3alpha4-rc56",
            "v1.2.3-56",
        )

    // RC tests

    @Test
    fun `when convert to version name, given parse alpha RC version from tag, then it's correct`() =
        assertEquals("1.2.3-alpha4-rc56", ReleaseCandidate.fromTag("v1.2.3-alpha4-rc56").toVersionName())

    @Test
    fun `when convert to version name, given parse RC version from tag, then it's correct`() =
        assertEquals("1.2.3-rc45", ReleaseCandidate.fromTag("v1.2.3-rc45").toVersionName())

    @Test
    fun `when convert to version name, given parse RC version from invalid tag, then parse exception thrown`() =
        shouldThrowForEach<IllegalStateException>(
            ReleaseCandidate::fromTag,
            "v1.2.3",
            "1.2.3",
            "v1.2.3-r1",
            "v1.2.3rc1",
            "v1.2.3-rc01",
        )

    // Release tests

    @Test
    fun `when convert to version name, given parse release version from tag, then it's correct`() =
        assertEquals("1.2.3", Stable.fromTag("v1.2.3").toVersionName())

    @Test
    fun `when convert to version name, given parse release version from invalid tag, then parse exception thrown`() =
        shouldThrowForEach<IllegalStateException>(Stable::fromTag, "v1.2.3-rc1", "1.2.3")
}

private inline fun <reified T : Throwable> shouldThrowForEach(
    constructor: (String) -> Version,
    vararg args: String,
) {
    val fails = args.fold(ArrayList<Pair<String, Any>>(args.size)) { fails, arg ->

        runCatching { constructor(arg) }
            .fold(onSuccess = { arg to it }, onFailure = { if (it is T) null else arg to it })
            ?.also { fails += it }

        fails
    }

    val message = fails.joinToString(separator = ",\n") { (arg, res) ->
        if (res is Throwable) {
            "wanted exception of type ${T::class} but was $res\n"
        } else {
            "test case didn't throw an exception for argument \"$arg\" -> $res\n"
        }
    }

    if (message.isNotEmpty()) {
        throw AssertionError(message)
    }
}

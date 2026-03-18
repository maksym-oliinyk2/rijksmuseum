import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

package io.github.kotlin.fibonacci

import kotlin.test.Test
import kotlin.test.assertEquals

private const val ExpectedElementCount = 3
private const val ExpectedLastElementValue = 7

class IosFibiTest {

    @Test
    fun `test 3rd element`() {
        assertEquals(ExpectedLastElementValue, generateFibi().take(ExpectedElementCount).last())
    }
}

package io.github.kotlin.fibonacci

import kotlin.test.Test
import kotlin.test.assertEquals

private const val ExpectedElementCount = 3
private const val ExpectedLastElementValue = 3

class AndroidFibiTest {

    @Test
    fun testThirdElement() {
        assertEquals(ExpectedLastElementValue, generateFibi().take(ExpectedElementCount).last())
    }
}

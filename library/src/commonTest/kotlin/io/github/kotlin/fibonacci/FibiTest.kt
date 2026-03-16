package io.github.kotlin.fibonacci

import kotlin.test.Test
import kotlin.test.assertEquals

private const val ExpectedElementCount = 3

class FibiTest {

    @Test
    fun `test 3rd element`() {
        assertEquals(FirstElement + SecondElement, generateFibi().take(ExpectedElementCount).last())
    }
}

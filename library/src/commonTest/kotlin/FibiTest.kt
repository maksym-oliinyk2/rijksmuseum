package io.github.kotlin.fibonacci

import kotlin.test.Test
import kotlin.test.assertEquals

class FibiTest {

    @Test
    fun `test 3rd element`() {
        assertEquals(FirstElement + SecondElement, generateFibi().take(3).last())
    }
}

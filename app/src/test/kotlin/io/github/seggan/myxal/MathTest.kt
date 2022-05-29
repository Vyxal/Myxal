package io.github.seggan.myxal

import io.github.seggan.myxal.runtime.math.BigComplex
import io.github.seggan.myxal.TestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class MathTest {

    @Test
    fun testAddition() {
        Assertions.assertEquals(BigComplex.valueOf(3), TestHelper.run("--code", "1 2 +"))
    }
}

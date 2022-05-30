package io.github.seggan.myxal

import io.github.seggan.myxal.runtime.math.BigComplex
import io.github.seggan.myxal.TestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/** Tests that run full programs to check if they compile and run */
class FullProgramTests {

    @Test
    fun testVectAddition() {
        TestHelper.run("-O", "--code", "1 5 r 2 6 r +")
    }
}

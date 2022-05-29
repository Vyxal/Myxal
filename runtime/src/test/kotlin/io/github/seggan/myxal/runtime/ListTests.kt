package io.github.seggan.myxal.runtime

import io.github.seggan.myxal.runtime.list.JyxalList
import io.github.seggan.myxal.runtime.math.BigComplex
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ListTests {

    @Test
    fun testDotProduct() {
        Assertions.assertEquals(
                "-8foofoo",
                dotProduct(
                        ProgramStack(
                                listOf(
                                        JyxalList.create(BigComplex.valueOf(-2), "foo"),
                                        JyxalList.create(
                                                BigComplex.valueOf(4),
                                                BigComplex.valueOf(2)
                                        )
                                )
                        )
                )
        )
    }
}

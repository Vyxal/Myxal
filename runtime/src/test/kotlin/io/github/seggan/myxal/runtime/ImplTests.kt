package io.github.seggan.myxal.runtime

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions

import io.github.seggan.myxal.runtime.list.JyxalList
import io.github.seggan.myxal.runtime.math.BigComplex

/** Tests checking element implementations directly instead of running Myxal programs */
class ImplTests {

    @Test
    fun testDotProduct() {
        Assertions.assertEquals(
                "-8foofoo",
                dotProduct(
                        JyxalList.create((-2).jyxal(), "foo"),
                        JyxalList.create(4.jyxal(), 2.jyxal())
                )
        )
    }
}

package io.github.seggan.myxal.app

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions
import io.github.seggan.myxal.runtime.jyxal
import io.github.seggan.myxal.runtime.list.JyxalList

class ElementTests {

    @Test
    fun testVectSubtraction() {
        Assertions.assertArrayEquals(
            arrayOf((1).jyxal(), (1).jyxal(), (1).jyxal()),
            (TestHelper.run("1 4 r 2 5 r 2 - -").pop() as JyxalList).toTypedArray()
        )
    }
}

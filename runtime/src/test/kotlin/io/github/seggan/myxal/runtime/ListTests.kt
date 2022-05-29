package io.github.seggan.myxal.runtime

import io.github.seggan.myxal.TestHelper
import io.github.seggan.myxal.runtime.list.JyxalList
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ListTests {

    @Test
    fun testDotProduct() {
        Assertions.assertEquals(
            dotProduct(JyxalList.create(0, 1, "foo"), JyxalList.create(2, 4, 2)),
            "04foofoo"
        )
    }
}

package io.github.seggan.myxal.runtime

import io.github.seggan.myxal.runtime.list.MyxalList
import io.github.seggan.myxal.runtime.math.BigComplex
import io.github.seggan.myxal.runtime.text.JsonParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JsonTest {
    @Test
    fun testJson() {
        val expected = MyxalList.create(
            MyxalList.create("a", "b", "c"), MyxalList.create(
                BigComplex.valueOf(1),
                BigComplex.valueOf(2),
                BigComplex.valueOf(3)
            ), "a"
        )
        val actual = JsonParser("[[\"a\",\"b\",\"c\"],[1,2,3],\"a\"]").parse() as MyxalList
        Assertions.assertEquals(expected, actual)
    }
}
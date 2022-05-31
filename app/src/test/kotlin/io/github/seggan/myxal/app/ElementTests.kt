package io.github.seggan.myxal.app

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions
import io.github.seggan.myxal.runtime.jyxal
import io.github.seggan.myxal.runtime.list.JyxalList

class ElementTests {

    @Test
    fun testLists() {
        TestHelper.assertTopEquals("@[1|2|3@]", "[1|2|3]")
        TestHelper.assertTopEquals("@[\"Hello, World!\"|2|3@]", "[Hello, World!|2|3]")
        TestHelper.assertTopEquals("@[1|kH|3@]", "[1|Hello, World!|3]")
        TestHelper.assertTopEquals("@[1|1 1+|3@]", "[1|2|3]")
    }

    @Test
    fun doMathTests() {
        TestHelper.assertTopEquals("1 1+", "2")
        TestHelper.assertTopEquals("1 \"a\"+", "1a")
        TestHelper.assertTopEquals("\"a\" 1+", "a1")
        TestHelper.assertTopEquals("\"a\" \"b\"+", "ab")
        TestHelper.assertTopEquals("@[1|2|4@] 2+", "[3|4|6]")
        TestHelper.assertTopEquals("@[1|2|3@]@[1|2|3@]+", "[2|4|6]")

        TestHelper.assertTopEquals("1 1-", "0")
        TestHelper.assertTopEquals("2 \"a\"-", "--a")
        TestHelper.assertTopEquals("\"a\" 2-", "a--")
        TestHelper.assertTopEquals("\"abcdef\"\"cd\"-", "abef")
        TestHelper.assertTopEquals("@[1|2|4@] 2-", "[-1|0|2]")
        TestHelper.assertTopEquals("@[1|2|3@]@[1|2|3@]-", "[0|0|0]")
    }
}

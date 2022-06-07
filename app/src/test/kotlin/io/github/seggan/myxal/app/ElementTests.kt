package io.github.seggan.myxal.app

import org.junit.jupiter.api.Test

class ElementTests {

    @Test
    fun testLists() {
        TestHelper.assertTopEquals("@[1|2|3@]", "[1|2|3]")
        TestHelper.assertTopEquals("@[\"Hello, World!\"|2|3@]", "[Hello, World!|2|3]")
        TestHelper.assertTopEquals("@[1|kH|3@]", "[1|Hello, World!|3]")
        TestHelper.assertTopEquals("@[1|1 1+|3@]", "[1|2|3]")
    }

    @Test
    fun testStringAliases() {
        TestHelper.assertTopEquals("\"Hello,\"¢h\" World!\"¢w \"hw\"", "Hello, World!")
    }

    @Test
    fun doMathTests() {
        TestHelper.assertTopEquals("1 1+", "2")
        TestHelper.assertTopEquals("1 \"a\"+", "1a")
        TestHelper.assertTopEquals("\"a\" 1+", "a1")
        TestHelper.assertTopEquals("\"a\" \"b\"+", "ab")
        TestHelper.assertTopEquals("@[1|2|4@] 2+", "[3|4|6]")
        TestHelper.assertTopEquals("@[1|2|3@]@[1|2|3@]+", "[2|4|6]")

        TestHelper.assertTopEquals("5b", "[1|0|1]")
        TestHelper.assertTopEquals("@[1|2|5@]b", "[[1]|[1|0]|[1|0|1]]")

        TestHelper.assertTopEquals("5⌐", "-4")
        TestHelper.assertTopEquals("\"Hello, World!\"⌐", "[Hello| World!]")
        TestHelper.assertTopEquals("@[1|2|5@]⌐", "[0|-1|-4]")

        TestHelper.assertTopEquals("1 1-", "0")
        TestHelper.assertTopEquals("2 \"a\"-", "--a")
        TestHelper.assertTopEquals("\"a\" 2-", "a--")
        TestHelper.assertTopEquals("\"abcdef\"\"cd\"-", "abef")
        TestHelper.assertTopEquals("@[1|2|4@] 2-", "[-1|0|2]")
        TestHelper.assertTopEquals("@[1|2|3@]@[1|2|3@]-", "[0|0|0]")
    }
}

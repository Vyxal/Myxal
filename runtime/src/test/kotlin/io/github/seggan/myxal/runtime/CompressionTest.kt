package io.github.seggan.myxal.runtime

import io.github.seggan.myxal.runtime.text.Compression
import io.github.seggan.myxal.runtime.text.Compression.compress
import io.github.seggan.myxal.runtime.text.Compression.decompress
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CompressionTest {

    @BeforeEach
    fun setUp() {
        Compression.isTest = true
    }

    @Test
    fun testDecompress() {
        Assertions.assertEquals("Hello, World!", decompress("ƈṡ, ƛ€!"))
        Assertions.assertEquals(
            "This is a test String to decompress.",
            decompress("λ« is a ∨Ḋ øẏ to de•⅛⟑Ŀ.")
        )
        Assertions.assertEquals("A test string.", decompress("A test string."))
    }

    @Test
    fun testCompress() {
        Assertions.assertEquals("`ƈṡ, ƛ€!`", compress("Hello, World!"))
        Assertions.assertEquals(
            "`λ« is a ∨Ḋ øẏ to de•⅛⟑Ŀ.`",
            compress("This is a test String to decompress.")
        )
    }
}
package io.github.seggan.myxal.compiler

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ElementTest {

    @Test
    fun assertNoDuplicateElements() {
        val names = Element.values().map { it.text }
        val duplicates = names.groupBy { it }.filter { it.value.size > 1 }
        Assertions.assertTrue(duplicates.isEmpty(), "Duplicate elements found: ${duplicates.keys}")
    }
}
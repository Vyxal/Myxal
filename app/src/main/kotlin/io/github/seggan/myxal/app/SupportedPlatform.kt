package io.github.seggan.myxal.app

import io.github.seggan.myxal.compiler.Element
import io.github.seggan.myxal.compiler.Element.*

enum class SupportedPlatform(val platform: String, vararg overrides: Element) {
    JVM("jvm", SORT, HEAD, TAIL, DOUBLE_REPEAT),
    NATIVE("native");


    val overrides: Set<String> = overrides.map { it.text }.toSet()

    companion object {
        fun fromString(platform: String): SupportedPlatform {
            return values().find { it.platform == platform }
                ?: throw IllegalArgumentException("Unsupported platform: $platform")
        }
    }

}
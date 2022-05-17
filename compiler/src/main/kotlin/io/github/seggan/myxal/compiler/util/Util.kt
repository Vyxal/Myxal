package io.github.seggan.myxal.compiler.util

fun screamingSnakeCaseToCamelCase(s: String): String {
    return buildString {
        var upper = false
        for (c in s) {
            if (c == '_') {
                upper = true
            } else {
                if (upper) {
                    append(c.uppercaseChar())
                    upper = false
                } else {
                    append(c.lowercaseChar())
                }
            }
        }
    }
}
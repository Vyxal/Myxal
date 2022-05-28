package io.github.seggan.myxal.compiler

class MyxalCompileException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {
    constructor(message: String?) : this(message, null)
}
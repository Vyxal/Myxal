package io.github.seggan.myxal.compiler.jvm.wrappers

import org.objectweb.asm.Label

internal data class Loop(val start: Label, val end: Label)

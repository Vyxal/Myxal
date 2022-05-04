package io.github.seggan.myxal.compiler.tree

import io.github.seggan.myxal.compiler.ICompiler

interface Node {
    fun accept(visitor: ICompiler)
}
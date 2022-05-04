package io.github.seggan.myxal.compiler.tree

import io.github.seggan.myxal.compiler.ICompiler

class ElementNode(val text: String) : Node {

    override fun accept(visitor: ICompiler) {
        visitor.visitElement(this)
    }
}
package io.github.seggan.myxal.compiler.tree

import io.github.seggan.myxal.compiler.Element
import io.github.seggan.myxal.compiler.ICompiler

class ElementNode(val element: Element) : Node {
    override fun accept(visitor: ICompiler<*>) {
        visitor.visitElement(element)
    }

    override fun toString(): String {
        return element.text
    }
}
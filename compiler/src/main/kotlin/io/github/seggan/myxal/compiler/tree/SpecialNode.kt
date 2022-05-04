package io.github.seggan.myxal.compiler.tree

import io.github.seggan.myxal.compiler.ICompiler

class ContextNode : Node {
    override fun accept(visitor: ICompiler) {
        visitor.loadContext()
    }
}

class BreakNode : Node {
    override fun accept(visitor: ICompiler) {
        visitor.breakLoop()
    }
}

class ContinueNode : Node {
    override fun accept(visitor: ICompiler) {
        visitor.continueLoop()
    }
}
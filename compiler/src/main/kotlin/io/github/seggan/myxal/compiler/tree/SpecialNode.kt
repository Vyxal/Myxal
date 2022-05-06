package io.github.seggan.myxal.compiler.tree

import io.github.seggan.myxal.compiler.ICompiler

class ContextNode : Node {
    override fun accept(visitor: ICompiler<*>) {
        visitor.loadContext()
    }

    override fun toString(): String {
        return "(context)"
    }
}

class BreakNode : Node {
    override fun accept(visitor: ICompiler<*>) {
        visitor.breakLoop()
    }

    override fun toString(): String {
        return "(break)"
    }
}

class ContinueNode : Node {
    override fun accept(visitor: ICompiler<*>) {
        visitor.continueLoop()
    }

    override fun toString(): String {
        return "(continue)"
    }
}

class InputNode : Node {
    override fun accept(visitor: ICompiler<*>) {
        visitor.input()
    }

    override fun toString(): String {
        return "(input)"
    }
}

class ExitNode : Node {
    override fun accept(visitor: ICompiler<*>) {
        visitor.exit()
    }

    override fun toString(): String {
        return "(exit)"
    }
}

class WrapStackNode : Node {
    override fun accept(visitor: ICompiler<*>) {
        visitor.wrapStack()
    }

    override fun toString(): String {
        return "(wrapStack)"
    }
}

class StackSizeNode : Node {
    override fun accept(visitor: ICompiler<*>) {
        visitor.stackSize()
    }

    override fun toString(): String {
        return "(stackSize)"
    }
}
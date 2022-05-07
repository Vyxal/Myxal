package io.github.seggan.myxal.compiler.tree

import io.github.seggan.myxal.compiler.ICompiler

class BlockNode(val block: List<Node>) : Node {
    override fun accept(visitor: ICompiler<*>) {
        visitor.visit(block)
    }

    override fun toString(): String {
        return block.toString()
    }
}

class ForNode(val body: List<Node>) : Node {
    override fun accept(visitor: ICompiler<*>) {
        visitor.visitFor(this)
    }

    override fun toString(): String {
        return "(for $body)"
    }
}

class ForINode(val times: Int, val body: List<Node>) : Node {
    override fun accept(visitor: ICompiler<*>) {
        visitor.visitForI(this)
    }

    override fun toString(): String {
        return "(fori $times $body)"
    }
}

class IfNode(val ifBlock: List<Node>, val elseBlock: List<Node>?) : Node {
    override fun accept(visitor: ICompiler<*>) {
        visitor.visitIf(this)
    }

    override fun toString(): String {
        return "(if $ifBlock $elseBlock)"
    }
}

class WhileNode(val condition: List<Node>?, val body: List<Node>) : Node {
    override fun accept(visitor: ICompiler<*>) {
        visitor.visitWhile(this)
    }

    override fun toString(): String {
        return if (condition == null) {
            "(while $body)"
        } else {
            "(while $condition $body)"
        }
    }
}

class LambdaNode(val arity: Int, val body: List<Node>) : Node {
    override fun accept(visitor: ICompiler<*>) {
        visitor.visitLambda(this)
    }

    override fun toString(): String {
        return "(lambda $arity $body)"
    }
}
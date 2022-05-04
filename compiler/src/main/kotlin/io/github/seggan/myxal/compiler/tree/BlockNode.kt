package io.github.seggan.myxal.compiler.tree

import io.github.seggan.myxal.compiler.ICompiler

class BlockNode(val block: List<Node>) : Node {
    override fun accept(visitor: ICompiler) {
        visitor.visit(block)
    }
}

class ForNode(val body: List<Node>) : Node {
    override fun accept(visitor: ICompiler) {
        visitor.visitFor(this)
    }
}

class ForINode(val times: Int, val body: List<Node>) : Node {
    override fun accept(visitor: ICompiler) {
        visitor.visitForI(this)
    }
}

class IfNode(val ifBlock: List<Node>, val elseBlock: List<Node>?) : Node {
    override fun accept(visitor: ICompiler) {
        visitor.visitIf(this)
    }
}

class WhileNode(val condition: List<Node>?, val body: List<Node>) : Node {
    override fun accept(visitor: ICompiler) {
        visitor.visitWhile(this)
    }
}

class LambdaNode(val arity: Int, val body: List<Node>) : Node {
    override fun accept(visitor: ICompiler) {
        visitor.visitLambda(this)
    }
}

class MapLambdaNode(val body: List<Node>) : Node {
    override fun accept(visitor: ICompiler) {
        visitor.visitMapLambda(this)
    }
}

class FilterLambdaNode(val body: List<Node>) : Node {
    override fun accept(visitor: ICompiler) {
        visitor.visitFilterLambda(this)
    }
}
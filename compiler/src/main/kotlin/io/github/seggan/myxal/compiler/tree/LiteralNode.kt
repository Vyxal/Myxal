package io.github.seggan.myxal.compiler.tree

import io.github.seggan.myxal.compiler.ICompiler

class StringNode(val value: String) : Node {
    override fun accept(visitor: ICompiler<*>) {
        visitor.visitString(value)
    }
    
    override fun toString(): String {
        return "'$value'"
    }
}

class NumNode(val value: String) : Node {
    override fun accept(visitor: ICompiler<*>) {
        visitor.visitNum(this)
    }

    fun isInt32(): Boolean = value.length <= 9

    fun isDouble(): Boolean = value.contains('.')
    
    override fun toString(): String {
        return value
    }
}

class ComplexNode(val real: NumNode, val imag: NumNode) : Node {
    override fun accept(visitor: ICompiler<*>) {
        visitor.visitComplex(this)
    }
    
    override fun toString(): String {
        return "$real + ${imag}i"
    }
}

class ListNode(val values: List<Node>) : Node {
    override fun accept(visitor: ICompiler<*>) {
        visitor.visitList(this)
    }
    
    override fun toString(): String {
        return values.toString()
    }
}
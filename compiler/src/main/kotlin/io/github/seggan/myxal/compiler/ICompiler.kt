package io.github.seggan.myxal.compiler

import io.github.seggan.myxal.compiler.tree.ComplexNode
import io.github.seggan.myxal.compiler.tree.ForINode
import io.github.seggan.myxal.compiler.tree.ForNode
import io.github.seggan.myxal.compiler.tree.IfNode
import io.github.seggan.myxal.compiler.tree.LambdaNode
import io.github.seggan.myxal.compiler.tree.ListNode
import io.github.seggan.myxal.compiler.tree.Node
import io.github.seggan.myxal.compiler.tree.NumNode
import io.github.seggan.myxal.compiler.tree.WhileNode
import org.apache.commons.cli.CommandLine

abstract class ICompiler<O>(protected val options: CommandLine) {

    protected lateinit var currentNode: Node

    open fun visit(node: Node) {
        currentNode = node
        node.accept(this)
    }

    fun visit(nodes: List<Node>) {
        nodes.forEach { visit(it) }
    }

    abstract fun compile(ast: List<Node>): O

    abstract fun visitElement(element: Element)

    abstract fun visitIf(node: IfNode)
    abstract fun visitWhile(node: WhileNode)
    abstract fun visitFor(node: ForNode)
    abstract fun visitForI(node: ForINode)

    abstract fun loadContext()
    abstract fun breakLoop()
    abstract fun continueLoop()

    abstract fun input()
    abstract fun exit()

    abstract fun loadVariable(name: String)
    abstract fun storeVariable(name: String)
    abstract fun loadRegister()
    abstract fun setRegister()

    abstract fun visitString(value: String)
    abstract fun visitNum(value: NumNode)
    abstract fun visitComplex(value: ComplexNode)
    abstract fun visitList(value: ListNode)

    abstract fun visitLambda(node: LambdaNode)

    abstract fun wrapStack()
    abstract fun stackSize()

}
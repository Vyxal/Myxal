package io.github.seggan.myxal.compiler

import io.github.seggan.myxal.compiler.tree.ComplexNode
import io.github.seggan.myxal.compiler.tree.ElementNode
import io.github.seggan.myxal.compiler.tree.FilterLambdaNode
import io.github.seggan.myxal.compiler.tree.ForINode
import io.github.seggan.myxal.compiler.tree.ForNode
import io.github.seggan.myxal.compiler.tree.IfNode
import io.github.seggan.myxal.compiler.tree.LambdaNode
import io.github.seggan.myxal.compiler.tree.ListNode
import io.github.seggan.myxal.compiler.tree.MapLambdaNode
import io.github.seggan.myxal.compiler.tree.Node
import io.github.seggan.myxal.compiler.tree.NumNode
import io.github.seggan.myxal.compiler.tree.WhileNode

interface ICompiler {

    fun visit(node: Node?) {
        node?.accept(this)
    }

    fun visit(nodes: List<Node>) {
        nodes.forEach { visit(it) }
    }

    fun visitElement(node: ElementNode)

    fun visitIf(node: IfNode)
    fun visitWhile(node: WhileNode)
    fun visitFor(node: ForNode)
    fun visitForI(node: ForINode)

    fun loadContext()
    fun breakLoop()
    fun continueLoop()

    fun loadVariable(name: String)
    fun storeVariable(name: String)
    fun loadRegister()
    fun setRegister()

    fun visitString(value: String)
    fun visitNum(value: NumNode)
    fun visitComplex(value: ComplexNode)
    fun visitList(value: ListNode)

    fun visitLambda(node: LambdaNode)
    fun visitMapLambda(node: MapLambdaNode)
    fun visitFilterLambda(node: FilterLambdaNode)

}
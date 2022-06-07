package io.github.seggan.myxal.compiler.util

import io.github.seggan.myxal.compiler.Element
import io.github.seggan.myxal.compiler.ICompiler
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

object TreeUtil {

    fun getFirstElement(tree: List<Node>): Element? {
        var result: Element? = null
        object : BasicVisitor() {
            override fun visitElement(element: Element) {
                result = element
                stopVisiting()
            }
        }.compile(tree)
        return result
    }
}

private open class BasicVisitor : ICompiler<Unit>(CommandLine.Builder().build()) {

    private class StopVisitor : RuntimeException()

    protected fun stopVisiting() {
        throw StopVisitor()
    }

    override fun compile(ast: List<Node>) {
        try {
            visit(ast)
        } catch (e: StopVisitor) {
            // do nothing
        }
    }

    override fun visitElement(element: Element) {
        // Nothing to do
    }

    override fun visitIf(node: IfNode) {
        visit(node.ifBlock)
        if (node.elseBlock != null) {
            visit(node.elseBlock)
        }
    }

    override fun visitWhile(node: WhileNode) {
        if (node.condition != null) {
            visit(node.condition)
        }
        visit(node.body)
    }

    override fun visitFor(node: ForNode) {
        visit(node.body)
    }

    override fun visitForI(node: ForINode) {
        visit(node.body)
    }

    override fun loadContext() {
        // Nothing to do
    }

    override fun breakLoop() {
        // Nothing to do
    }

    override fun continueLoop() {
        // Nothing to do
    }

    override fun input() {
        // Nothing to do
    }

    override fun exit() {
        // Nothing to do
    }

    override fun loadVariable(name: String) {
        // Nothing to do
    }

    override fun storeVariable(name: String) {
        // Nothing to do
    }

    override fun loadRegister() {
        // Nothing to do
    }

    override fun setRegister() {
        // Nothing to do
    }

    override fun visitString(value: String) {
        // Nothing to do
    }

    override fun visitNum(value: NumNode) {
        // Nothing to do
    }

    override fun visitComplex(value: ComplexNode) {
        // Nothing to do
    }

    override fun visitList(value: ListNode) {
        // Nothing to do
    }

    override fun visitLambda(node: LambdaNode) {
        // Nothing to do
    }

    override fun wrapStack() {
        // Nothing to do
    }

    override fun stackSize() {
        // Nothing to do
    }

}
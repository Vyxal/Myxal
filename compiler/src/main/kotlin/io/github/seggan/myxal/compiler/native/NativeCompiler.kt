package io.github.seggan.myxal.compiler.native

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

const val TEMPLATE = """
#include <iostream>
#include "lib/prog.hpp"

int main(int argc, char **argv) {
enterFunction();
%s
cout << pop() << endl;
return 0;
}
"""

class NativeCompiler(options: CommandLine) : ICompiler<String>(options) {

    private val code = StringBuilder()

    override fun compile(ast: List<Node>): String {
        visit(ast)
        return TEMPLATE.format(code)
    }

    override fun visitElement(element: Element) {
        TODO("Not yet implemented")
    }

    override fun visitIf(node: IfNode) {
        TODO("Not yet implemented")
    }

    override fun visitWhile(node: WhileNode) {
        TODO("Not yet implemented")
    }

    override fun visitFor(node: ForNode) {
        TODO("Not yet implemented")
    }

    override fun visitForI(node: ForINode) {
        TODO("Not yet implemented")
    }

    override fun loadContext() {
        TODO("Not yet implemented")
    }

    override fun breakLoop() {
        TODO("Not yet implemented")
    }

    override fun continueLoop() {
        TODO("Not yet implemented")
    }

    override fun input() {
        TODO("Not yet implemented")
    }

    override fun exit() {
        TODO("Not yet implemented")
    }

    override fun loadVariable(name: String) {
        TODO("Not yet implemented")
    }

    override fun storeVariable(name: String) {
        TODO("Not yet implemented")
    }

    override fun loadRegister() {
        TODO("Not yet implemented")
    }

    override fun setRegister() {
        TODO("Not yet implemented")
    }

    override fun visitString(value: String) {
        TODO("Not yet implemented")
    }

    override fun visitNum(value: NumNode) {
        TODO("Not yet implemented")
    }

    override fun visitComplex(value: ComplexNode) {
        TODO("Not yet implemented")
    }

    override fun visitList(value: ListNode) {
        TODO("Not yet implemented")
    }

    override fun visitLambda(node: LambdaNode) {
        TODO("Not yet implemented")
    }

    override fun wrapStack() {
        TODO("Not yet implemented")
    }

    override fun stackSize() {
        TODO("Not yet implemented")
    }
}
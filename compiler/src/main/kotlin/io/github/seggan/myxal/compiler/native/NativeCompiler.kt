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
import io.github.seggan.myxal.compiler.util.CallStack
import org.apache.commons.cli.CommandLine

const val TEMPLATE = """
#include <iostream>
#include "lib/prog.hpp"
#include "lib/helpers.cpp" // this is non-standard, but im too lazy to write all those headers

%s

int main(int argc, char **argv) {
enterFunction();
%s
cout << pop()->asString() << endl;
return 0;
}
"""

class NativeCompiler(options: CommandLine) : ICompiler<String>(options) {

    private val callStack = CallStack<StringBuilder>()
    private val functions = mutableListOf<String>()

    override fun compile(ast: List<Node>): String {
        val main = StringBuilder()
        callStack.push(main)
        visit(ast)
        return TEMPLATE.format(functions.joinToString("\n"), main)
    }

    override fun visitElement(element: Element) {
        TODO("Not yet implemented")
    }

    override fun visitIf(node: IfNode) {
        code.appendLine("if (truthValue(pop())) {")
        visit(node.ifBlock)
        if (node.elseBlock != null) {
            code.appendLine("} else {")
            visit(node.elseBlock)
        }
        code.appendLine("}")
    }

    override fun visitWhile(node: WhileNode) {
        if (node.condition != null) {
            val label =
                code.appendLine("")
        } else {
            code.appendLine("while (true) {")
            visit(node.body)
            code.appendLine("}")
        }
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

fun StringBuilder.appendLine(line: String) {
    append(line).append("\n")
}
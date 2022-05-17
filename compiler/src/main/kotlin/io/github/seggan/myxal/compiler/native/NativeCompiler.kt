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
import io.github.seggan.myxal.compiler.util.screamingSnakeCaseToCamelCase
import io.github.seggan.myxal.runtime.escapeString
import io.github.seggan.myxal.runtime.times
import org.apache.commons.cli.CommandLine

const val TEMPLATE = """
#include <iostream>
#include <string>
#include "prog.hpp"
#include "types.hpp"
#include "helpers.hpp"
#include "elements.hpp"

%s

int main(int argc, char **argv) {
enterFunction();
try {
%s
} catch (char *e) {
if (e == "exit") {
return 0;
} else {
throw e;
}
}
std::cout << pop()->asString() << std::endl;
return 0;
}
"""

class NativeCompiler(options: CommandLine) : ICompiler<String>(options) {

    private val callStack = CallStack<StringBuilder>()
    private val code: StringBuilder
        get() = callStack.peek()
    private val functions = mutableListOf<String>()

    private var counter = 0

    override fun compile(ast: List<Node>): String {
        val main = StringBuilder()
        callStack.push(main)
        visit(ast)
        return TEMPLATE.format(functions.joinToString("\n"), main)
    }

    override fun visitElement(element: Element) {
        if (element == Element.PRINT || element == Element.PRINTLN) {
            code.append("std::cout << pop()->asString()")
            if (element == Element.PRINTLN) {
                code.append(" << \"\\n\"")
            }
            code.appendLine(';')
        } else {
            val list = listOf("pop()") * element.arity
            code.append("push(")
            code.append(screamingSnakeCaseToCamelCase(element.name))
            code.append("(")
            code.append(list.joinToString(", "))
            code.appendLine("));")
        }
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
        code.appendLine("enterScope(mt::mnumber(1));")
        if (node.condition != null) {
            val label = "lbl_${counter++}:"
            val end = "lbl_${counter++}"
            code.appendLine(label)
            visit(node.condition)
            code.appendLine("if (!truthValue(pop())) {")
            code.appendLine("goto $end")
            code.appendLine("}")
            visit(node.body)
            code.appendLine("context() = context()->asNumber()->add(1);")
            code.appendLine("goto $label")
            code.appendLine(end)
        } else {
            code.appendLine("while (true) {")
            visit(node.body)
            code.appendLine("context() = context()->asNumber()->add(1);")
            code.appendLine("}")
        }
        code.appendLine("exitScope();")
    }

    override fun visitFor(node: ForNode) {
        TODO("Not yet implemented")
    }

    override fun visitForI(node: ForINode) {
        code.appendLine("for (long i = 0; i < ${node.times}; i++) {")
        visit(node.body)
        code.appendLine("}")
    }

    override fun loadContext() {
        code.appendLine("push(context());")
    }

    override fun breakLoop() {
        code.appendLine("break;")
    }

    override fun continueLoop() {
        code.appendLine("continue;")
    }

    override fun input() {
        TODO("Not yet implemented")
    }

    override fun exit() {
        code.appendLine("throw \"exit\";")
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
        code.appendLine("push(mt::mstring(\"${escapeString(value)}\"));")
    }

    override fun visitNum(value: NumNode) {
        code.appendLine("push(mt::mnumber(${value.value}));")
    }

    override fun visitComplex(value: ComplexNode) {
        throw IllegalStateException("Myxal/native does not yet support complex numbers")
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
        code.appendLine("push(mt::mnumber(getStack().size()));")
    }
}
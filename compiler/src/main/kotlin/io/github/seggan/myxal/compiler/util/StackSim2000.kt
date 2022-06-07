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

class StackSim2000(private val ast: List<Node>) {

    private val theStacks = mutableMapOf<Node, StackView>()

    init {
        simulate()
    }

    private fun simulate() {
        val stackOfStacks = ArrayDeque<StackView>()
        stackOfStacks.addFirst(StackView.EMPTY)
        fun stack(block: (StackView) -> StackView) = stackOfStacks.addFirst(block(stackOfStacks.removeFirst()))
        object : ICompiler<Unit>(CommandLine.Builder().build()) {

            override fun compile(ast: List<Node>) {
                visit(ast)
            }

            override fun visit(node: Node) {
                super.visit(node)
                theStacks[node] = stackOfStacks.first()
            }

            override fun visitElement(element: Element) {
                stack { element.doOutput(it) }
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
                stack { it.push(StackObject.ANY) }
            }

            override fun breakLoop() {
                stack { StackView.EMPTY }
            }

            override fun continueLoop() {
                stack { StackView.EMPTY }
            }

            override fun input() {
                stack { it.push(StackObject.ANY) }
            }

            override fun exit() {
                // nothing to do
            }

            override fun loadVariable(name: String) {
                stack { it.push(StackObject.ANY) }
            }

            override fun storeVariable(name: String) {
                stack { it.pop() }
            }

            override fun loadRegister() {
                stack { it.push(StackObject.ANY) }
            }

            override fun setRegister() {
                stack { it.pop() }
            }

            override fun visitString(value: String) {
                stack { it.push(StackObject.STRING) }
            }

            override fun visitNum(value: NumNode) {
                stack { it.push(StackObject.NUMBER) }
            }

            override fun visitComplex(value: ComplexNode) {
                stack { it.push(StackObject.NUMBER) }
            }

            override fun visitList(value: ListNode) {
                TODO("Not yet implemented")
            }

            override fun visitLambda(node: LambdaNode) {
                stackOfStacks.addFirst(StackView.EMPTY)
                stack {
                    val new = ArrayList<StackObject>()
                    for (i in 0 until node.arity) {
                        new.add(StackObject.ANY)
                    }
                    it.pushAll(new)
                }
            }

            override fun wrapStack() {
                stack { StackView.EMPTY.push(StackObject.LIST) }
            }

            override fun stackSize() {
                stack { it.push(StackObject.NUMBER) }
            }
        }.compile(ast)
    }

    fun getStackAt(node: Node): StackView {
        return theStacks.getOrElse(node) {
            throw IllegalArgumentException("Node $node not found in AST")
        }
    }
}

class StackView private constructor(private val backing: List<StackObject>) : Collection<StackObject> by backing {

    companion object {
        val EMPTY = StackView(emptyList())
    }

    fun push(obj: StackObject): StackView {
        val newBacking = backing.toMutableList()
        newBacking.add(0, obj)
        return StackView(newBacking)
    }

    fun pushAll(objs: Iterable<StackObject>): StackView {
        val newBacking = backing.toMutableList()
        for (obj in objs) {
            newBacking.add(0, obj)
        }
        return StackView(newBacking)
    }

    fun pop(): StackView {
        if (backing.isEmpty()) {
            return this
        }
        val newBacking = backing.toMutableList()
        newBacking.removeAt(0)
        return StackView(newBacking)
    }

    fun pop(count: Int): StackView {
        val newBacking = backing.toMutableList()
        val it = newBacking.iterator()
        for (i in 0 until count) {
            if (!it.hasNext()) break
            it.next()
            it.remove()
        }
        return StackView(newBacking)
    }

    fun peek(): StackObject? {
        return backing.firstOrNull()
    }

    fun peekPop(): Pair<StackView, StackObject?> {
        return Pair(pop(), peek())
    }
}

enum class StackObject(val char: Char) {
    NUMBER('n'),
    STRING('s'),
    LIST('l'),
    FUNCTION('f'),

    /**
     * A special value meaning that the output is the same type as the input
     */
    SAME('='),

    /**
     * Anything
     */
    ANY('?');

    fun matches(other: StackObject): Boolean {
        return this == other || this == ANY || other == ANY
    }

    companion object {
        private val charMap = values().associateBy { it.char }

        fun fromChar(char: Char): StackObject {
            return charMap[char] ?: throw IllegalArgumentException("Unknown StackObject: $char")
        }
    }
}
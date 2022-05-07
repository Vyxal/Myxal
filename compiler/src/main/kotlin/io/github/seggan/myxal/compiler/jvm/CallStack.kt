package io.github.seggan.myxal.compiler.jvm

import io.github.seggan.myxal.compiler.jvm.wrappers.MyxalMethod
import java.util.ArrayDeque
import java.util.Deque

/**
 * A stack that caches the top value
 */
class CallStack(private val stack: Deque<MyxalMethod>) : Deque<MyxalMethod> by stack {

    private var top: MyxalMethod? = null

    override fun peek(): MyxalMethod {
        return top!!
    }

    override fun push(e: MyxalMethod?) {
        top = e
        stack.push(e)
    }

    override fun pop(): MyxalMethod {
        val e = stack.pop()
        top = stack.peek()
        return e
    }
}
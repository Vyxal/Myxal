package io.github.seggan.myxal.compiler.util

import java.util.ArrayDeque

/**
 * A stack that caches the top value, useful for tracking the current scope
 */
class CallStack<E> : ArrayDeque<E>() {

    private var top: E? = null

    override fun push(element: E) {
        super.push(element)
        top = element
    }

    override fun pop(): E {
        val element = super.pop()
        top = super.peek()
        return element
    }

    override fun peek(): E {
        return top ?: super.peek()
    }
}
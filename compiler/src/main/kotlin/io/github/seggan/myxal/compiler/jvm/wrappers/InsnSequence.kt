package io.github.seggan.myxal.compiler.jvm.wrappers

import org.objectweb.asm.tree.AbstractInsnNode

internal data class InsnSequence(val insns: List<Int>) {

    constructor(vararg insns: Int) : this(insns.toList())

    fun matches(insnNode: AbstractInsnNode): List<AbstractInsnNode> {
        var insn = insnNode
        val matches = mutableListOf<AbstractInsnNode>()
        for (i in insns) {
            if (insn.opcode != i) {
                return ArrayList()
            }
            matches.add(insn)
            insn = insn.next
        }
        return matches
    }
}
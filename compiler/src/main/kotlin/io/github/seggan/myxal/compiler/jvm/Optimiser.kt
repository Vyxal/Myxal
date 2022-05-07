package io.github.seggan.myxal.compiler.jvm

import io.github.seggan.myxal.compiler.jvm.wrappers.MyxalMethod
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode

fun optimise(codeBlock: InsnList, myxalMethod: MyxalMethod) {
    // first remove all stack loads
    val toRemove = HashSet<AbstractInsnNode>()
    for (insn in codeBlock) {
        if (insn is VarInsnNode && insn.`var` == myxalMethod.stackVar && insn.opcode == ALOAD) {
            toRemove.add(insn)
            val next = insn.next
            if (next != null && next.opcode == SWAP) {
                toRemove.add(next)
            }
        }
    }
    for (insn in toRemove) {
        codeBlock.remove(insn)
    }
    toRemove.clear()
    toRemove.clear()
    // then remove all push-pop pairs
    for (insn in codeBlock) {
        if (insn.opcode == INVOKEVIRTUAL
            && insn is MethodInsnNode
            && insn.name == "push"
            && insn.desc == "(Ljava/lang/Object;)V"
            && insn.owner == "io/github/seggan/myxal/runtime/ProgramStack"
        ) {
            val next = insn.getNext()
            if (next.opcode == INVOKEVIRTUAL
                && next is MethodInsnNode
                && next.name == "pop"
                && next.desc == "()Ljava/lang/Object;"
                && next.owner == "io/github/seggan/myxal/runtime/ProgramStack"
            ) {
                toRemove.add(insn)
                toRemove.add(next)
            }
        }
    }
    for (insn in toRemove) {
        codeBlock.remove(insn)
    }
    for (insn in codeBlock) {
        if (insn is MethodInsnNode) {
            if (insn.owner == "io/github/seggan/myxal/runtime/ProgramStack" && insn.name != "<init>") {
                val arguments = (Type.getArgumentsAndReturnSizes(insn.desc) shr 2) - 1
                codeBlock.insertBefore(insn, VarInsnNode(ALOAD, myxalMethod.stackVar))
                if (arguments != 0) {
                    // one argument
                    codeBlock.insertBefore(insn, InsnNode(SWAP))
                }
            } else if (insn.desc.startsWith("(Lio/github/seggan/myxal/runtime/ProgramStack;)")) {
                // these all take the stack as input
                codeBlock.insertBefore(insn, VarInsnNode(ALOAD, myxalMethod.stackVar))
            }
        }
    }
}
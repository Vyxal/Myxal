package io.github.seggan.myxal.compiler.jvm.wrappers

import io.github.seggan.myxal.compiler.jvm.msplit.SplitMethod
import io.github.seggan.myxal.compiler.jvm.optimise
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.MethodNode

abstract class MyxalMethod internal constructor(cw: ClassWriter, access: Int, name: String, desc: String) :
    MethodNode(Opcodes.ASM7, access, name, desc, null, null), Opcodes {

    var stackVar = 0
        protected set
    var ctxVar = 0
        protected set

    var optimise = true

    private val runtime = "runtime/".toRegex()

    private val reservedVars: MutableSet<ContextualVariable> = HashSet()
    fun loadStack() {
        visitVarInsn(Opcodes.ALOAD, stackVar)
    }

    fun loadContextVar() {
        visitVarInsn(Opcodes.ALOAD, ctxVar)
    }

    @JvmOverloads
    fun reserveVar(name: String? = null): ContextualVariable {
        var max = 0
        for (variable in reservedVars) {
            if (variable.index > max) {
                max = variable.index
            }
        }
        val variable = ContextualVariable(if (max == 0) ctxVar + 1 else max + 1, this)
        reservedVars.add(variable)
        if (name != null) {
            val start = Label()
            visitLabel(start)
            visitLocalVariable(name, "Ljava/lang/Object;", null, start, variable.end, variable.index)
        }
        return variable
    }

    fun freeVar(variable: ContextualVariable) {
        reservedVars.remove(variable)
        visitLabel(variable.end)
    }

    override fun visitEnd() {
        if (optimise) {
            val codeBlocks: MutableList<InsnList> = ArrayList()
            var code = InsnList()
            val iterator = instructions.iterator()
            while (iterator.hasNext()) {
                val insn = iterator.next()
                iterator.remove()
                code.add(insn)
                if (insn is LabelNode || insn.opcode == Opcodes.RETURN || insn.opcode == Opcodes.ATHROW || insn.opcode == Opcodes.IRETURN || insn.opcode == Opcodes.LRETURN || insn.opcode == Opcodes.FRETURN || insn.opcode == Opcodes.DRETURN || insn.opcode == Opcodes.ARETURN || insn.opcode == Opcodes.GOTO) {
                    codeBlocks.add(code)
                    code = InsnList()
                }
            }
            for (block in codeBlocks) {
                optimise(block, this)
            }
            for (block in codeBlocks) {
                instructions.add(block)
            }
        }
        accept(mv)
    }

    override fun visitMethodInsn(
        opcodeAndSource: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {
        super.visitMethodInsn(opcodeAndSource, remap(owner), name, remap(descriptor), isInterface)
    }

    override fun visitFieldInsn(opcodeAndSource: Int, owner: String, name: String, descriptor: String) {
        super.visitFieldInsn(opcodeAndSource, remap(owner), name, remap(descriptor))
    }

    override fun visitTypeInsn(opcode: Int, type: String) {
        super.visitTypeInsn(opcode, remap(type))
    }

    override fun visitLdcInsn(value: Any) {
        if (value is Handle) {
            super.visitLdcInsn(Handle(value.tag, remap(value.owner), value.name, value.desc, value.isInterface))
        } else {
            super.visitLdcInsn(value)
        }
    }

    private fun remap(owner: String): String {
        return runtime.replace(owner, "io/github/seggan/myxal/runtime/")
    }

    init {
        mv = cw.visitMethod(access, name, desc, null, null)
    }
}
package io.github.seggan.myxal.compiler.jvm.wrappers

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*

class Function internal constructor(cw: ClassWriter, access: Int, name: String, desc: String) :
    MyxalMethod(cw, access, name, desc) {
    init {
        stackVar = 0
        ctxVar = 1
        if (desc.startsWith("()")) {
            visitTypeInsn(NEW, "runtime/ProgramStack")
            visitInsn(DUP)
            visitMethodInsn(INVOKESPECIAL, "runtime/ProgramStack", "<init>", "()V", false)
            visitVarInsn(ASTORE, stackVar)

            visitFieldInsn(GETSTATIC, "runtime/math/BigComplex", "ZERO", "Lruntime/math/BigComplex;")
            visitVarInsn(ASTORE, ctxVar)
        } else {
            loadStack()
            visitMethodInsn(
                INVOKESTATIC,
                "runtime/list/JyxalList",
                "create",
                "(Ljava/util/Collection;)Lruntime/list/JyxalList;",
                false
            )
            visitVarInsn(ASTORE, ctxVar)
        }
    }
}
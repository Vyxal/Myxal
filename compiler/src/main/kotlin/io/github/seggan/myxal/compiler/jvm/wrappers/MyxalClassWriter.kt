package io.github.seggan.myxal.compiler.jvm.wrappers

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.util.regex.Pattern

class MyxalClassWriter(flags: Int) : ClassWriter(flags) {

    fun visitMethod(access: Int, name: String, desc: String): MyxalMethod {
        return if (name == "main" && desc == "([Ljava/lang/String;)V" && access == Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC) {
            MainMethod(this, access, name, desc)
        } else {
            Function(this, access, name, desc)
        }
    }

    companion object {
        private val RUNTIME = Pattern.compile("^runtime/")
    }
}
package io.github.seggan.myxal.app

import io.github.seggan.myxal.antlr.MyxalLexer
import io.github.seggan.myxal.antlr.MyxalParser
import io.github.seggan.myxal.compiler.jvm.JvmCompiler
import io.github.seggan.myxal.runtime.ProgramStack
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.apache.commons.cli.CommandLine
import org.objectweb.asm.ClassReader
import org.objectweb.asm.util.TraceClassVisitor
import java.io.FileOutputStream
import java.io.PrintWriter

object TestHelper {

    private var programStack: ProgramStack? = null

    private class DynamicClassLoader(parent: ClassLoader, val bytes: ByteArray) : ClassLoader(parent) {

        override fun loadClass(name: String?, resolve: Boolean): Class<*> {
            return if (name!!.startsWith("myxal")) {
                val clazz = defineClass(name, bytes, 0, bytes.size)
                if (resolve) {
                    resolveClass(clazz)
                }
                clazz
            } else {
                super.loadClass(name, resolve)
            }
        }
    }

    fun run(code: String, vararg input: String): ProgramStack {
        val parsed = MyxalParser(CommonTokenStream(MyxalLexer(CharStreams.fromString(code))))
        val transformed = Transformer.transform(parsed.file(), SupportedPlatform.JVM)
        val bytecode = JvmCompiler(CommandLine.Builder().build(), true).compile(transformed)
        val classLoader = DynamicClassLoader(Thread.currentThread().contextClassLoader, bytecode)
        val mainClass = classLoader.loadClass("myxal.Main")
        try {
            mainClass.getMethod("main", Array<String>::class.java).invoke(null, input)
        } catch (e: VerifyError) {
            val cr = ClassReader(bytecode)
            FileOutputStream("debug.log").use { os ->
                val tcv = TraceClassVisitor(PrintWriter(os))
                cr.accept(tcv, 0)
            }
            throw RuntimeException("Verification error. See debug.log for details.", e)
        }
        return programStack!!
    }

    @JvmStatic
    @Suppress("unused")
    fun sendStack(stack: ProgramStack) {
        programStack = stack
    }

    inline fun <reified T> assertInstanceTop(code: String, vararg input: String): T {
        val top = run(code, *input).pop()
        if (top !is T) {
            throw AssertionError("Expected top of stack to be of type ${T::class.java.name}, but was ${top::class.java.name}")
        }
        return top
    }

    fun assertTopEquals(code: String, expected: String, vararg input: String): Any {
        val top = run(code, *input).pop()
        val actual = top.toString().replace('⟨', '[').replace('⟩', ']')
            .replace(" | ", "|")
        if (actual != expected) {
            throw AssertionError("Expected top of stack to be $expected, but was $actual")
        }
        return top
    }
}
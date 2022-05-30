package io.github.seggan.myxal.app

import io.github.seggan.myxal.antlr.MyxalLexer
import io.github.seggan.myxal.antlr.MyxalParser
import io.github.seggan.myxal.compiler.jvm.JvmCompiler
import io.github.seggan.myxal.runtime.ProgramStack
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.apache.commons.cli.CommandLine

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

    fun run(code: String, vararg args: String): ProgramStack {
        val parsed = MyxalParser(CommonTokenStream(MyxalLexer(CharStreams.fromString(code))))
        val transformed = Transformer.transform(parsed.file(), SupportedPlatform.JVM)
        val bytecode = JvmCompiler(CommandLine.Builder().build(), true).compile(transformed)
        val classLoader = DynamicClassLoader(Thread.currentThread().contextClassLoader, bytecode)
        val mainClass = classLoader.loadClass("myxal.Main")
        mainClass.getMethod("main", Array<String>::class.java).invoke(null, args)
        return programStack!!
    }

    @JvmStatic
    fun sendStack(stack: ProgramStack) {
        programStack = stack
    }
}
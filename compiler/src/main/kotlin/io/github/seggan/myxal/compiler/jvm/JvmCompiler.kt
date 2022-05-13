package io.github.seggan.myxal.compiler.jvm

import io.github.seggan.myxal.compiler.Element
import io.github.seggan.myxal.compiler.ICompiler
import io.github.seggan.myxal.compiler.jvm.wrappers.Loop
import io.github.seggan.myxal.compiler.jvm.wrappers.MyxalClassWriter
import io.github.seggan.myxal.compiler.jvm.wrappers.MyxalMethod
import io.github.seggan.myxal.compiler.tree.BlockNode
import io.github.seggan.myxal.compiler.tree.ComplexNode
import io.github.seggan.myxal.compiler.tree.ForINode
import io.github.seggan.myxal.compiler.tree.ForNode
import io.github.seggan.myxal.compiler.tree.IfNode
import io.github.seggan.myxal.compiler.tree.LambdaNode
import io.github.seggan.myxal.compiler.tree.ListNode
import io.github.seggan.myxal.compiler.tree.Node
import io.github.seggan.myxal.compiler.tree.NumNode
import io.github.seggan.myxal.compiler.tree.WhileNode
import io.github.seggan.myxal.compiler.util.CallStack
import org.apache.commons.cli.CommandLine
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.util.CheckClassAdapter
import java.io.FileOutputStream
import java.io.PrintWriter
import java.util.ArrayDeque

class JvmCompiler(options: CommandLine) : ICompiler<ByteArray>(options) {

    private val callStack = CallStack<MyxalMethod>()
    private val loopStack = object : ArrayDeque<Loop>() {
        override fun peek(): Loop {
            return super.peek() ?: throw IllegalStateException("Illegal use of break/continue outside of loop")
        }
    }

    private val mv: MyxalMethod
        get() = callStack.peek()

    private lateinit var clinit: MethodVisitor
    private lateinit var writer: MyxalClassWriter

    private val vars = HashSet<String>()

    private var counter = 0

    override fun compile(ast: List<Node>): ByteArray {
        writer = MyxalClassWriter(ClassWriter.COMPUTE_FRAMES)
        writer.visit(V11, ACC_PUBLIC, "myxal/Main", null, "java/lang/Object", null)

        writer.visitField(
            ACC_PUBLIC or ACC_STATIC,
            "register",
            "Ljava/lang/Object;",
            null,
            null
        ).visitEnd()
        clinit = writer.visitMethod(
            ACC_STATIC,
            "<clinit>",
            "()V",
            null,
            null
        )
        clinit.visitCode()
        clinit.visitFieldInsn(
            GETSTATIC,
            "io/github/seggan/myxal/runtime/math/BigComplex",
            "ZERO",
            "Lio/github/seggan/myxal/runtime/math/BigComplex;"
        )
        clinit.visitFieldInsn(PUTSTATIC, "myxal/Main", "register", "Ljava/lang/Object;")
        val main = writer.visitMethod(
            ACC_PUBLIC or ACC_STATIC,
            "main",
            "([Ljava/lang/String;)V"
        )
        callStack.push(main)
        main.visitCode()
        visit(ast)

        clinit.visitInsn(RETURN)
        clinit.visitMaxs(0, 0)
        clinit.visitEnd()

        main.loadStack()
        main.visitMethodInsn(
            INVOKEVIRTUAL,
            "runtime/ProgramStack",
            "pop",
            "()Ljava/lang/Object;",
            false
        )
        main.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        main.visitInsn(SWAP)
        main.visitMethodInsn(
            INVOKEVIRTUAL,
            "java/io/PrintStream",
            "println",
            "(Ljava/lang/Object;)V",
            false
        )
        main.visitInsn(RETURN)
        try {
            main.visitMaxs(0, 0)
            main.visitEnd()
        } catch (e: Exception) {
            FileOutputStream("debug.log").use { os ->
                CheckClassAdapter.verify(
                    ClassReader(writer.toByteArray()),
                    true,
                    PrintWriter(os)
                )
            }
            throw e
        }
        return writer.toByteArray()
    }

    override fun visitElement(element: Element) {
        val name = screamingSnakeCaseToCamelCase(element.name)
        if (element == Element.DUPLICATE) {
            AsmHelper.pop(mv)
            mv.visitInsn(DUP)
            AsmHelper.push(mv)
            AsmHelper.push(mv)
        } else if (element == Element.PRINT || element == Element.PRINTLN) {
            AsmHelper.pop(mv)
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
            mv.visitInsn(SWAP)
            mv.visitMethodInsn(
                INVOKEVIRTUAL,
                "java/io/PrintStream",
                if (element == Element.PRINTLN) "println" else "print",
                "(Ljava/lang/Object;)V",
                false
            )
        } else if (element.modifiesStack) {
            mv.loadStack()
            mv.visitMethodInsn(
                INVOKESTATIC,
                "runtime/RuntimeMethods",
                name,
                "(Lruntime/ProgramStack;)V",
                false
            )
        } else {
            when (element.arity) {
                0 -> {
                    mv.visitMethodInsn(
                        INVOKESTATIC,
                        "runtime/RuntimeMethods",
                        name,
                        "()Ljava/lang/Object;",
                        false
                    )
                    AsmHelper.push(mv)
                }
                1 -> {
                    AsmHelper.pop(mv)
                    if (element.vectorise) {
                        mv.visitLdcInsn(
                            Handle(
                                H_INVOKESTATIC,
                                "runtime/RuntimeMethods",
                                name,
                                "(Ljava/lang/Object;)Ljava/lang/Object;",
                                false
                            )
                        )
                        mv.visitMethodInsn(
                            INVOKESTATIC,
                            "runtime/RuntimeMethods",
                            "monadVectorise",
                            "(Ljava/lang/Object;Ljava/lang/invoke/MethodHandle;)Ljava/lang/Object;",
                            false
                        )
                    } else {
                        mv.visitMethodInsn(
                            INVOKESTATIC,
                            "runtime/RuntimeMethods",
                            name,
                            "(Ljava/lang/Object;)Ljava/lang/Object;",
                            false
                        )
                    }
                    AsmHelper.push(mv)
                }
                else -> {
                    mv.loadStack()
                    mv.visitMethodInsn(
                        INVOKESTATIC,
                        "runtime/RuntimeMethods",
                        name,
                        "(Lruntime/ProgramStack;)Ljava/lang/Object;",
                        false
                    )
                    AsmHelper.push(mv)
                }
            }
        }
    }

    override fun visitIf(node: IfNode) {
        val end = Label()
        loopStack.push(Loop(end, end))
        AsmHelper.pop(mv)
        mv.visitMethodInsn(
            INVOKESTATIC,
            "runtime/RuntimeHelpers",
            "truthValue",
            "(Ljava/lang/Object;)Z",
            false
        )
        mv.visitJumpInsn(IFEQ, end)
        visit(node.ifBlock)
        if (node.elseBlock != null) {
            val elseEnd = Label()
            mv.visitJumpInsn(GOTO, elseEnd)
            mv.visitLabel(end)
            visit(node.elseBlock)
            mv.visitLabel(elseEnd)
        } else {
            mv.visitLabel(end)
        }
        loopStack.pop()
    }

    override fun visitWhile(node: WhileNode) {
        val start = Label()
        val end = Label()
        loopStack.push(Loop(start, end))
        mv.reserveVar().use { ctxStore ->
            mv.visitVarInsn(ALOAD, mv.ctxVar)
            ctxStore.store()
            mv.visitFieldInsn(
                GETSTATIC,
                "runtime/math/BigComplex",
                "ONE",
                "Lruntime/math/BigComplex;"
            )
            mv.visitVarInsn(ASTORE, mv.ctxVar)
            mv.visitLabel(start)
            if (node.condition != null) {
                // we have a finite loop
                visit(node.condition)
                AsmHelper.pop(mv)
                mv.visitMethodInsn(
                    INVOKESTATIC,
                    "runtime/RuntimeHelpers",
                    "truthValue",
                    "(Ljava/lang/Object;)Z",
                    false
                )
                mv.visitJumpInsn(IFEQ, end)
            }
            visit(node.body)
            mv.visitVarInsn(ALOAD, mv.ctxVar)
            mv.visitTypeInsn(CHECKCAST, "runtime/math/BigComplex")
            mv.visitFieldInsn(
                GETSTATIC,
                "runtime/math/BigComplex",
                "ONE",
                "Lruntime/math/BigComplex;"
            )
            mv.visitMethodInsn(
                INVOKEVIRTUAL,
                "runtime/math/BigComplex",
                "add",
                "(Lruntime/math/BigComplex;)Lruntime/math/BigComplex;",
                false
            )
            mv.visitVarInsn(ASTORE, mv.ctxVar)
            mv.visitJumpInsn(GOTO, start)
            mv.visitLabel(end)
            ctxStore.load()
            mv.visitVarInsn(ASTORE, mv.ctxVar)
        }
        loopStack.pop()
    }

    override fun visitFor(node: ForNode) {
        val start = Label()
        val end = Label()
        loopStack.push(Loop(start, end))
        AsmHelper.pop(mv)
        mv.visitMethodInsn(
            INVOKESTATIC,
            "runtime/RuntimeHelpers",
            "forify",
            "(Ljava/lang/Object;)Ljava/util/Iterator;",
            false
        )
        mv.reserveVar().use { iteratorVar ->
            mv.reserveVar().use { ctxStore ->
                mv.visitVarInsn(ALOAD, mv.ctxVar)
                ctxStore.store()
                iteratorVar.store()
                mv.visitLabel(start)
                iteratorVar.load()
                mv.visitMethodInsn(
                    INVOKEINTERFACE,
                    "java/util/Iterator",
                    "hasNext",
                    "()Z",
                    true
                )
                mv.visitJumpInsn(IFEQ, end)
                iteratorVar.load()
                mv.visitMethodInsn(
                    INVOKEINTERFACE,
                    "java/util/Iterator",
                    "next",
                    "()Ljava/lang/Object;",
                    true
                )
                mv.visitVarInsn(ASTORE, mv.ctxVar)
                visit(node.body)
                mv.visitJumpInsn(GOTO, start)
                mv.visitLabel(end)
                ctxStore.load()
                mv.visitVarInsn(ASTORE, mv.ctxVar)
            }
        }
        loopStack.pop()
    }

    override fun visitForI(node: ForINode) {
        AsmHelper.selectNumberInsn(mv, node.times)
        val variable = mv.reserveVar()
        variable.store(ISTORE)
        val start = Label()
        val end = Label()
        loopStack.push(Loop(start, end))
        mv.visitLabel(start)
        mv.visitIincInsn(variable.index, -1)
        variable.load(ILOAD)
        mv.visitJumpInsn(IFEQ, end)
        visit(node.body)
        mv.visitJumpInsn(GOTO, start)
        mv.visitLabel(end)
        loopStack.pop()
    }

    override fun loadContext() {
        mv.loadContextVar()
        AsmHelper.push(mv)
    }

    override fun breakLoop() {
        mv.visitJumpInsn(
            GOTO,
            loopStack.peek().end
        )
    }

    override fun continueLoop() {
        mv.visitJumpInsn(
            GOTO,
            loopStack.peek().start
        )
    }

    override fun input() {
        mv.loadStack()
        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            "runtime/ProgramStack",
            "getInput",
            "()Ljava/lang/Object;",
            false
        )
        AsmHelper.push(mv)
    }

    override fun exit() {
        mv.visitInsn(ICONST_0)
        mv.visitMethodInsn(
            INVOKESTATIC,
            "java/lang/System",
            "exit",
            "(I)V",
            false
        )
    }

    override fun loadVariable(name: String) {
        if (name !in vars) {
            writer.visitField(
                ACC_PUBLIC or ACC_STATIC,
                name,
                "Ljava/lang/Object;",
                null,
                null
            )
            vars.add(name)
            clinit.visitFieldInsn(
                GETSTATIC,
                "io/github/seggan/myxal/runtime/math/BigComplex",
                "ZERO",
                "Lio/github/seggan/myxal/runtime/math/BigComplex;"
            )
            clinit.visitFieldInsn(PUTSTATIC, "myxal/Main", name, "Ljava/lang/Object;")
        }
        mv.visitFieldInsn(GETSTATIC, "myxal/Main", name, "Ljava/lang/Object;")
        AsmHelper.push(mv)
    }

    override fun storeVariable(name: String) {
        if (name !in vars) {
            writer.visitField(
                ACC_PUBLIC or ACC_STATIC,
                name,
                "Ljava/lang/Object;",
                null,
                null
            )
            vars.add(name)
            clinit.visitFieldInsn(
                GETSTATIC,
                "io/github/seggan/myxal/runtime/math/BigComplex",
                "ZERO",
                "Lio/github/seggan/myxal/runtime/math/BigComplex;"
            )
            clinit.visitFieldInsn(PUTSTATIC, "myxal/Main", name, "Ljava/lang/Object;")
        }
        AsmHelper.pop(mv)
        mv.visitFieldInsn(PUTSTATIC, "myxal/Main", name, "Ljava/lang/Object;")
    }

    override fun loadRegister() {
        mv.visitFieldInsn(
            GETSTATIC,
            "myxal/Main",
            "register",
            "Ljava/lang/Object;"
        )
        AsmHelper.push(mv)
    }

    override fun setRegister() {
        AsmHelper.pop(mv)
        mv.visitFieldInsn(
            PUTSTATIC,
            "myxal/Main",
            "register",
            "Ljava/lang/Object;"
        )
    }

    override fun visitString(value: String) {
        mv.visitLdcInsn(value)
        AsmHelper.push(mv)
    }

    override fun visitNum(value: NumNode) {
        AsmHelper.addBigComplex(value.value, mv)
    }

    override fun visitComplex(value: ComplexNode) {
        AsmHelper.addBigDecimal(value.real.value, mv)
        AsmHelper.addBigDecimal(value.imag.value, mv)
        mv.visitMethodInsn(
            INVOKESTATIC,
            "runtime/math/BigComplex",
            "valueOf",
            "(Ljava/math/BigDecimal;Ljava/math/BigDecimal;)Lruntime/math/BigComplex;",
            false
        )
        AsmHelper.push(mv)
    }

    override fun visitList(value: ListNode) {
        AsmHelper.selectNumberInsn(mv, value.values.size)
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object")
        for ((i, node) in value.values.withIndex()) {
            mv.visitInsn(DUP)
            AsmHelper.selectNumberInsn(mv, i)
            if (node is BlockNode) {
                val methodName = "listInit$${counter++}"
                val method = writer.visitMethod(
                    ACC_PRIVATE or ACC_STATIC,
                    methodName,
                    "()Ljava/lang/Object;"
                )
                method.visitCode()
                callStack.push(method)
                visit(node)
                callStack.pop()
                AsmHelper.pop(method)
                method.visitInsn(ARETURN)
                method.visitMaxs(0, 0)
                method.visitEnd()
                mv.visitMethodInsn(
                    INVOKESTATIC,
                    "myxal/Main",
                    methodName,
                    "()Ljava/lang/Object;",
                    false
                )
            } else {
                visit(node)
                AsmHelper.pop(mv)
            }
            mv.visitInsn(AASTORE)
        }
        mv.visitMethodInsn(
            INVOKESTATIC,
            "runtime/list/JyxalList",
            "create",
            "([Ljava/lang/Object;)Lruntime/list/JyxalList;",
            false
        )
        AsmHelper.push(mv)
    }

    override fun visitLambda(node: LambdaNode) {
        val methodName = "lambda$$${counter++}"
        val method = writer.visitMethod(
            ACC_PRIVATE or ACC_STATIC,
            methodName,
            "(Lruntime/ProgramStack;)Ljava/lang/Object;"
        )
        method.visitCode()
        callStack.push(method)
        visit(node.body)
        callStack.pop()
        AsmHelper.pop(method)
        method.visitInsn(ARETURN)
        method.visitMaxs(0, 0)
        method.visitEnd()
        mv.visitTypeInsn(NEW, "runtime/Lambda")
        mv.visitInsn(DUP)
        AsmHelper.selectNumberInsn(mv, node.arity)
        mv.visitLdcInsn(
            Handle(
                H_INVOKESTATIC,
                "myxal/Main",
                methodName,
                "(Lruntime/ProgramStack;)Ljava/lang/Object;",
                false
            )
        )
        mv.visitMethodInsn(
            INVOKESPECIAL,
            "runtime/Lambda",
            "<init>",
            "(ILjava/lang/invoke/MethodHandle;)V",
            false
        )
        AsmHelper.push(mv)
    }

    override fun wrapStack() {
        mv.loadStack()
        mv.visitMethodInsn(
            INVOKESTATIC,
            "runtime/list/JyxalList",
            "create",
            "(Lruntime/ProgramStack;)Lruntime/list/JyxalList;",
            false
        )
        AsmHelper.push(mv)
    }

    override fun stackSize() {
        mv.loadStack()
        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            "runtime/ProgramStack",
            "size",
            "()I",
            false
        )
        mv.visitInsn(I2L)
        mv.visitMethodInsn(
            INVOKESTATIC,
            "runtime/math/BigComplex",
            "valueOf",
            "(J)Lruntime/math/BigComplex;",
            false
        )
        AsmHelper.push(mv)
    }

    private fun screamingSnakeCaseToCamelCase(s: String): String {
        return buildString {
            var upper = false
            for (c in s) {
                if (c == '_') {
                    upper = true
                } else {
                    if (upper) {
                        append(c.uppercaseChar())
                        upper = false
                    } else {
                        append(c.lowercaseChar())
                    }
                }
            }
        }
    }
}
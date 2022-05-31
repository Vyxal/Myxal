package io.github.seggan.myxal.app

import io.github.seggan.myxal.antlr.MyxalLexer
import io.github.seggan.myxal.antlr.MyxalParser
import io.github.seggan.myxal.compiler.cpp.NativeCompiler
import io.github.seggan.myxal.compiler.jvm.JvmCompiler
import io.github.seggan.myxal.compiler.tree.Node
import io.github.seggan.myxal.runtime.text.Compression
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.objectweb.asm.ClassReader
import org.objectweb.asm.util.TraceClassVisitor
import proguard.ClassPath
import proguard.ClassPathEntry
import proguard.Configuration
import proguard.ConfigurationParser
import proguard.ProGuard
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.PrintWriter
import java.net.URL
import java.net.URLClassLoader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.Locale
import java.util.Scanner
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import java.util.regex.Pattern

object Main {
    private const val runtimeClasses = "../runtime/build/runtimeLibs"

    private val p = Pattern.compile("\\..*$")

    @JvmStatic
    fun doMain(args: Array<String>, isTest: Boolean) {
        val cmdParser = DefaultParser(false)

        val options = Options()
        options.addOption("h", "help", false, "Print this help message")
            .addOption("c", "codepage", false, "Use the Myxal codepage")
            .addOption("d", "debug", false, "Print debug stuff")
            .addOption("p", "platform", true, "Platform to compile for")
            .addOption("f", "file", true, "Input file")
            .addOption("C", "code", true, "Input code")
            .addOption("O", "nooptimize", false, "Do not optimize")

        val cmd = cmdParser.parse(options, args)

        println("Parsing program...")
        val inputFile = if (cmd.hasOption("f")) cmd.getOptionValue("f") else "Input"
        val bytes = if (cmd.hasOption("f")) {
            Files.readAllBytes(Path.of(cmd.getOptionValue("f")))
        } else if (cmd.hasOption("C")) {
            cmd.getOptionValue("C").toByteArray()
        } else {
            throw MyxalCompileException("Either file or code must be given as arguments")
        }
        val s: String = if (cmd.hasOption("c")) {
            val sb = StringBuilder()
            for (b in bytes) {
                sb.append(Compression.CODEPAGE[b.toInt()])
            }
            sb.toString()
        } else {
            String(bytes, StandardCharsets.UTF_8)
        }
        val lexer = MyxalLexer(CharStreams.fromString(s))
        val parser = MyxalParser(CommonTokenStream(lexer))
        val platform = SupportedPlatform.fromString(cmd.getOptionValue('p') ?: "jvm")
        val transformed = Transformer.transform(parser.file(), platform)
        if (cmd.hasOption("d")) {
            println(transformed)
        }
        println("Compiling program...")
        val extIndex = inputFile.lastIndexOf('.')
        val fileName = if (extIndex == -1) inputFile else inputFile.substring(0, extIndex)
        if (platform == SupportedPlatform.JVM) {
            val main = JvmCompiler(cmd, false).compile(transformed)
            val cr = ClassReader(main)
            FileOutputStream("debug.log").use { os ->
                val tcv = TraceClassVisitor(PrintWriter(os))
                cr.accept(tcv, 0)
            }
            println("Extracting runtime classes...")
            val resourceList: MutableSet<String> = HashSet()
            val buildDir: Path = Path.of(System.getProperty("user.dir"), runtimeClasses)
            Scanner(
                if (isTest) Files.newInputStream(buildDir.resolve("runtime.list")) else
                    Main::class.java.getResourceAsStream("/runtime.list")!!
            ).use { scanner ->
                while (scanner.hasNextLine()) {
                    resourceList.add(scanner.nextLine())
                }
            }
            println("Writing to jar...")
            val file = File("$fileName-temp.jar")
            val final = File("$fileName.jar")
            JarOutputStream(FileOutputStream(file)).use { jar ->
                for (resource in resourceList) {
                    val entry = JarEntry(resource)
                    entry.time = System.currentTimeMillis()
                    jar.putNextEntry(entry)
                    if (isTest) Files.newInputStream(buildDir.resolve(resource)) else Main::class.java.getResourceAsStream(
                        "/$resource"
                    ).use { inp ->
                        inp?.copyTo(jar) ?: println("Skipping resource: $resource")
                    }
                }
                val manifest = Manifest()
                manifest.mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
                manifest.mainAttributes[Attributes.Name.MAIN_CLASS] = "myxal.Main"
                jar.putNextEntry(JarEntry("META-INF/MANIFEST.MF"))
                manifest.write(jar)
                val entry = JarEntry("myxal/Main.class")
                entry.time = System.currentTimeMillis()
                jar.putNextEntry(entry)
                jar.write(main)
            }
            if (!cmd.hasOption('O')) {
                println("Performing post-compilation optimisations...")
                val config = Configuration()
                ConfigurationParser(Main::class.java.getResource("/rules.pro")!!, System.getProperties())
                    .parse(config)
                config.obfuscate = false
                config.optimizationPasses = 2
                config.programJars = ClassPath()
                config.programJars.add(ClassPathEntry(file, false))
                config.programJars.add(ClassPathEntry(final, true))
                config.libraryJars = ClassPath()
                config.libraryJars.add(
                    ClassPathEntry(
                        File(
                            "${System.getProperty("java.home")}/jmods/java.base.jmod"
                        ), false
                    )
                )
                config.libraryJars.add(
                    ClassPathEntry(
                        File(
                            "${System.getProperty("java.home")}/jmods/jdk.jshell.jmod"
                        ), false
                    )
                )
                config.warn = mutableListOf("!java.lang.invoke.MethodHandle")
                config.optimizations = mutableListOf("!class/unboxing/enum")
                ProGuard(config).execute()
            } else {
                FileInputStream(file).use { fis ->
                    FileOutputStream(final).use { fos ->
                        fis.copyTo(fos)
                    }
                }
            }
            if (!cmd.hasOption('d')) {
                file.delete()
            }
        } else if (platform == SupportedPlatform.NATIVE) {
            val main = NativeCompiler(cmd).compile(transformed)

            println("Compiling native code...")
            val buildDir = Path.of(System.getProperty("user.dir"), "build-${System.currentTimeMillis()}")
            val file = buildDir.resolve("main.cpp")
            Files.createDirectories(buildDir)
            Files.writeString(file, main)

            val resourceList = mutableListOf<String>()
            Scanner(Main::class.java.getResourceAsStream("/cpp-resources.txt")!!).use { scanner ->
                while (scanner.hasNextLine()) {
                    resourceList.add(scanner.nextLine())
                }
            }
            val includes = mutableListOf<String>()
            for (resource in resourceList) {
                val inp = Main::class.java.getResourceAsStream("/$resource")
                if (inp != null) {
                    Files.copy(inp, buildDir.resolve(resource), StandardCopyOption.REPLACE_EXISTING)
                }
                includes.add(resource)
            }
            includes.add("main.cpp")

            val isWindows = System.getProperty("os.name").lowercase(Locale.getDefault()).contains("win")
            val executableName = if (isWindows) "$fileName.exe" else fileName

            val a = mutableListOf(
                "g++",
                "-o", executableName
            )
            if (cmd.hasOption('d')) {
                a.add("-g")
            }
            if (!cmd.hasOption('O')) {
                a.add("-O3")
            }
            a.addAll(includes)
            ProcessBuilder(*a.toTypedArray())
                .directory(buildDir.toFile())
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
                .waitFor()

            try {
                val final = buildDir.resolve(executableName)
                if (!Files.exists(final)) {
                    throw RuntimeException("Failed to compile native code. See g++ output for details.")
                }

                println("Cleaning up...")
                Files.move(final, buildDir.parent.resolve(executableName), StandardCopyOption.REPLACE_EXISTING)
            } finally {
                if (!cmd.hasOption('d')) {
                    Files.walk(buildDir).filter { !Files.isDirectory(it) }.forEach {
                        Files.delete(it)
                    }

                    Files.walk(buildDir).forEach {
                        if (Files.isDirectory(it)) {
                            Files.delete(it)
                        } else {
                            Files.deleteIfExists(it)
                        }
                    }
                }
            }
        }
        println("Done!")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        doMain(args, false)
    }
}

package io.github.seggan.myxal.app

import io.github.seggan.myxal.antlr.MyxalLexer
import io.github.seggan.myxal.antlr.MyxalParser
import io.github.seggan.myxal.antlr.MyxalParser.*
import io.github.seggan.myxal.antlr.MyxalParserBaseVisitor
import io.github.seggan.myxal.compiler.Element
import io.github.seggan.myxal.compiler.tree.BlockNode
import io.github.seggan.myxal.compiler.tree.BreakNode
import io.github.seggan.myxal.compiler.tree.ComplexNode
import io.github.seggan.myxal.compiler.tree.ContextNode
import io.github.seggan.myxal.compiler.tree.ContinueNode
import io.github.seggan.myxal.compiler.tree.ElementNode
import io.github.seggan.myxal.compiler.tree.ForINode
import io.github.seggan.myxal.compiler.tree.ForNode
import io.github.seggan.myxal.compiler.tree.IfNode
import io.github.seggan.myxal.compiler.tree.InputNode
import io.github.seggan.myxal.compiler.tree.LambdaNode
import io.github.seggan.myxal.compiler.tree.ListNode
import io.github.seggan.myxal.compiler.tree.Node
import io.github.seggan.myxal.compiler.tree.NumNode
import io.github.seggan.myxal.compiler.tree.RegisterLoadNode
import io.github.seggan.myxal.compiler.tree.RegisterSetNode
import io.github.seggan.myxal.compiler.tree.StringNode
import io.github.seggan.myxal.compiler.tree.VariableGetNode
import io.github.seggan.myxal.compiler.tree.VariableSetNode
import io.github.seggan.myxal.compiler.tree.WhileNode
import io.github.seggan.myxal.runtime.text.Compression.decompress
import io.github.seggan.myxal.runtime.unescapeString
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ErrorNode

/**
 * Transforms the ANTLR AST into an AST that is easier to work with
 */
class Transformer private constructor() : MyxalParserBaseVisitor<List<Node>>() {

    private var aliases: MutableMap<String, ProgramContext> = HashMap()

    override fun visitAlias(ctx: AliasContext): List<Node> {
        aliases[ctx.theAlias.text] = ctx.program()
        return emptyList()
    }

    override fun visitFor_loop(ctx: For_loopContext): List<Node> {
        return listOf(ForNode(visit(ctx.program())))
    }

    override fun visitFori_loop(ctx: Fori_loopContext): List<Node> {
        val num = ctx.DIGIT().joinToString("")
        return if (ctx.program().getTokens(CONTEXT_VAR).isEmpty()) {
            listOf(ForINode(num.toInt(), visit(ctx.program())))
        } else {
            listOf(NumNode(num), ForNode(visit(ctx.program())))
        }
    }

    override fun visitComplex_number(ctx: Complex_numberContext): List<Node> {
        val parts = COMPLEX_SEPARATOR.split(ctx.text)
        return listOf(ComplexNode(NumNode(parts[0]), NumNode(parts[1])))
    }

    override fun visitNumber(ctx: NumberContext): List<Node> {
        return listOf(NumNode(ctx.text))
    }

    override fun visitModifier(ctx: ModifierContext): List<Node> {
        val result: List<Node> = visit(ctx.program_node())
        return when (ctx.MODIFIER().text) {
            "ß" -> listOf(IfNode(result, null))
            "&" -> listOf(RegisterLoadNode(), *result.toTypedArray(), RegisterSetNode())
            "v" -> listOf(LambdaNode(1, result), ElementNode(Element.MAP))
            "~" -> listOf(LambdaNode(1, result), ElementNode(Element.FILTER))
            "¨=" -> listOf(ElementNode(Element.DUPLICATE), *result.toTypedArray(), ElementNode(Element.EQUAL))
            else -> result
        }
    }

    override fun visitElement(ctx: ElementContext): List<Node> {
        var element = ctx.element_type().text
        if (ctx.PREFIX() != null) {
            element = ctx.PREFIX().text + element
        }
        if (aliases.containsKey(element) && !ctx.isInAlias) {
            return visit(aliases[element]!!)
        }
        return listOf(
            when (element) {
                "X" -> BreakNode()
                "x" -> ContinueNode()
                "n" -> ContextNode()
                "£" -> RegisterSetNode()
                "¥" -> RegisterLoadNode()
                "?" -> InputNode()
                else -> ElementNode(Element.getByText(element))
            }
        )
    }

    override fun visitIf_statement(ctx: If_statementContext): List<Node> {
        val ifBlock = visit(ctx.program(0))
        val elseBlock = if (ctx.program().size == 2) visit(ctx.program(1)) else null
        return listOf(IfNode(ifBlock, elseBlock))
    }

    override fun visitWhile_loop(ctx: While_loopContext): List<Node> {
        val condition = if (ctx.cond != null) visit(ctx.cond) else null
        return listOf(WhileNode(condition, visit(ctx.body)))
    }

    override fun visitVariable_assn(ctx: Variable_assnContext): List<Node> {
        return if (ctx.ASSN_SIGN().text == "→") {
            listOf(VariableSetNode(ctx.variable().text))
        } else {
            listOf(VariableGetNode(ctx.variable().text))
        }
    }

    override fun visitString(ctx: StringContext): List<Node> {
        var str = ctx.text.substring(1)
        if (str.endsWith("«") || str.endsWith("\"")) {
            str = str.substring(0, str.length - 1)
        }
        return listOf(StringNode(decompress(unescapeString(str))))
    }

    override fun visitList(ctx: ListContext): List<Node> {
        val list = mutableListOf<Node>()
        for (element in ctx.program()) {
            if (element.childCount == 1 && element.getChild(0) is LiteralContext) {
                list.addAll(visit(element.getChild(0)))
            } else {
                list.add(BlockNode(visit(element)))
            }
        }
        // crazy nesting of lists
        return listOf(ListNode(list))
    }

    override fun visitLambda(ctx: LambdaContext): List<Node> {
        return when (ctx.LAMBDA_TYPE().text) {
            "λ", "⟑" -> listOf(
                LambdaNode(
                    if (ctx.integer() == null) 1 else ctx.integer().text.toInt(),
                    visit(ctx.program())
                )
            )
            "ƛ" -> listOf(LambdaNode(1, visit(ctx.program())), ElementNode(Element.MAP))
            "'" -> listOf(LambdaNode(1, visit(ctx.program())), ElementNode(Element.FILTER))
            else -> throw IllegalArgumentException("Unknown lambda type: ${ctx.LAMBDA_TYPE().text}")
        }
    }

    override fun visitOne_element_lambda(ctx: One_element_lambdaContext): List<Node> {
        return listOf(LambdaNode(1, visit(ctx.program_node())))
    }

    override fun visitTwo_element_lambda(ctx: Two_element_lambdaContext): List<Node> {
        val result = mutableListOf<Node>()
        for (element in ctx.program_node()) {
            result.addAll(visit(element))
        }
        return listOf(LambdaNode(1, result))
    }

    override fun visitThree_element_lambda(ctx: Three_element_lambdaContext): List<Node> {
        val result = mutableListOf<Node>()
        for (element in ctx.program_node()) {
            result.addAll(visit(element))
        }
        return listOf(LambdaNode(1, result))
    }

    override fun defaultResult(): List<Node> {
        return emptyList()
    }

    override fun visitErrorNode(node: ErrorNode): List<Node> {
        throw IllegalArgumentException("Error node: ${node.text}")
    }

    override fun aggregateResult(aggregate: List<Node>, nextResult: List<Node>): List<Node> {
        return aggregate + nextResult
    }

    companion object {
        private val COMPLEX_SEPARATOR = "°".toRegex()

        private val STDLIB: MyxalParser

        init {
            MyxalParser::class.java.getResourceAsStream("/stdlib.myx").use {
                STDLIB = MyxalParser(CommonTokenStream(MyxalLexer(CharStreams.fromStream(it))))
            }
        }

        fun transform(program: FileContext, platform: SupportedPlatform): List<Node> {
            val transformer = Transformer()
            transformer.visit(STDLIB.file())
            transformer.aliases = transformer.aliases.filterKeys { !platform.overrides.contains(it) }.toMutableMap()
            return transformer.visit(program)
        }
    }
}
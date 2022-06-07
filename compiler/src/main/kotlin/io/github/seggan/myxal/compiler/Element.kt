package io.github.seggan.myxal.compiler

import io.github.seggan.myxal.compiler.util.StackObject
import io.github.seggan.myxal.compiler.util.StackView

private val colon = ":".toRegex()

@Suppress("unused")
enum class Element(
    val text: String, val arity: Int, val vectorise: Boolean,
    contractStr: String = "", val modifiesStack: Boolean = false
) {

    /**
     * Math
     */
    ADD("+", 2, true, "nn:n|??:s"),
    BINARY("b", 1, true),
    COMPLEMENT("⌐", 1, true),
    DECREMENT("‹", 1, true),
    DIVIDE("/", 2, true),
    DIV_FIVE("₅", 1, false),
    DIV_THREE("₃", 1, false),
    DOUBLE_REPEAT("d", 1, true),
    EXPONENTIATE("e", 2, true),
    FACTORS("K", 1, false),
    HALVE("½", 2, true, "n:n|s:ss", true),
    HEX_TO_DECIMAL("H", 1, true),
    INCREMENT("›", 1, true),
    INFINITE_PRIMES("Þp", 0, false),
    IS_EVEN("₂", 1, false),
    IS_PRIME("æ", 1, true),
    MODULO_FORMAT("%", 2, true),
    MULTI_COMMAND("•", 2, true),
    MULTIPLY("*", 2, true),
    NEGATE("N", 1, true),
    SQRT("√", 1, true),
    SUBTRACT("-", 2, true),
    SUM("∑", 1, false),
    TWO_POW("E", 1, true),

    /**
     * Boolean
     */
    ALL("A", 1, false),
    ANY("a", 1, false),
    BOOLIFY("ḃ", 1, false),
    EQUAL("=", 2, true),
    GREATER_THAN(">", 2, true),
    GREATER_THAN_OR_EQUAL("≥", 2, true),
    LESS_THAN("<", 2, true),
    LESS_THAN_OR_EQUAL("≤", 2, true),
    LOGICAL_AND("∧", 2, false),
    LOGICAL_NOT("¬", 1, false),
    LOGICAL_OR("∨", 2, false),

    /**
     * String
     */
    CHR_ORD("C", 1, true),
    INFINITE_REPLACE("ÞI", 2, false),
    ITEM_SPLIT("÷", 2, false, "?:!", true),
    JOIN_BY_NEWLINES("⁋", 1, false),
    JOIN_BY_NOTHING("ṅ", 1, false),
    JSON_PARSE("øJ", 1, true),
    MIRROR("m", 1, false),
    REMOVE("o", 2, false),
    REVERSE("Ṙ", 1, false),
    SPACES("I", 1, false),
    SPLIT_ON("€", 2, false),
    STRINGIFY("S", 1, false),
    STRIP("P", 2, false),
    UNEVAL("q", 1, false),

    /**
     * List
     */
    CONTAINS("c", 2, false),
    COUNT("O", 2, false),
    CUMULATIVE_GROUPS("l", 2, false),
    DOT_PRODUCT("Þ•", 2, false),
    FILTER("F", 2, false),
    FLATTEN("f", 1, false),
    HEAD("h", 1, false),
    HEAD_EXTRACT("ḣ", 1, false, "l:?l|?:ss", true),
    INDEX_INTO("i", 2, false),
    INTERLEAVE("Y", 2, false),
    IOR("ɾ", 1, true), // inclusive one range
    IZR("ʀ", 1, true), // inclusive zero range
    JOIN("j", 2, false),
    LISTI("w", 1, false),
    LENGTH("L", 1, false),
    MAP("M", 2, false),
    MAP_GET_SET("Þd", 2, false),
    MAX("G", 1, false),
    MERGE("J", 2, false),
    MIN("g", 1, false),
    PREPEND("p", 2, false),
    RANGE("r", 2, true),
    REDUCE("R", 2, false),
    REMOVE_AT_INDEX("⟇", 2, false),
    REPLACE("V", 2, false),
    SLICE_UNTIL("Ẏ", 2, false),
    SORT("s", 1, false),
    SORT_BY_FUNCTION("ṡ", 2, false),
    TAIL("t", 1, false),
    TRUTHY_INDEXES("T", 1, false),
    UNINTERLEAVE("y", 2, false, "l:ll|?:ss", true),
    UNIQUIFY("U", 1, false),
    ZIP("Z", 2, false),
    ZIP_SELF("z", 1, false),

    /**
     * Stack
     */
    TRIPLICATE("D", 1, false, "=:===", true),
    DUPLICATE("`", 1, false, "=:==", true),
    POP("_", 1, false, "?:", true),

    /**
     * Misc
     */
    FUNCTION_CALL("†", 2, false),
    GET_REQUEST("¨U", 1, true),
    PRINT("₴", 1, false),
    PRINT_NO_POP("…", 1, false),
    PRINTLN(",", 1, false, "?:");

    private val contracts: List<InOutContract>

    init {
        contracts = ArrayList()
        if (contractStr.isEmpty()) {
            val ins = ArrayList<StackObject>()
            for (i in 0 until arity) {
                ins.add(StackObject.ANY)
            }
            contracts.add(InOutContract(ins, listOf(StackObject.ANY)))
        } else {
            for (contract in contractStr.split("|")) {
                val (inStr, outStr) = contract.split(colon)
                val ins = inStr.map(StackObject::fromChar).toList()
                val out = if (outStr == "!") null else outStr.map(StackObject::fromChar).toList()
                contracts.add(InOutContract(ins, out))
            }
        }
    }

    fun doOutput(stack: StackView): StackView {
        for (contract in contracts) {
            if (contract.matches(stack)) {
                val out = contract.getOutput(stack) ?: return StackView.EMPTY
                if (out.isEmpty()) return stack
                return stack.pop(contract.ins.size).pushAll(out)
            }
        }
        return stack
    }

    companion object {
        fun getByText(text: String): Element {
            for (e in values()) {
                if (e.text == text) {
                    return e
                }
            }
            throw MyxalCompileException("Unknown element: $text")
        }
    }
}

data class InOutContract(val ins: List<StackObject>, val outs: List<StackObject>?) {

    fun getOutput(input: StackView): List<StackObject>? {
        if (outs == null) return null
        val inputIt = input.iterator()
        val sames = ArrayList<StackObject>()
        for (obj in ins.reversed()) {
            if (!inputIt.hasNext()) {
                return emptyList()
            }
            val next = inputIt.next()
            if (obj == StackObject.SAME) {
                sames.add(0, next)
            }
        }
        val output = ArrayList<StackObject>()
        var i = 0
        for (obj in outs) {
            if (obj == StackObject.SAME) {
                output.add(sames[i++])
                i %= sames.size
            } else {
                output.add(obj)
            }
        }
        return output
    }

    fun matches(input: StackView): Boolean {
        val inputIt = input.iterator()
        val insIt = ins.reversed().iterator()
        while (inputIt.hasNext() && insIt.hasNext()) {
            val inputObj = inputIt.next()
            val insObj = insIt.next()
            if (insObj == StackObject.SAME) {
                continue
            } else if (!inputObj.matches(insObj)) {
                return false
            }
        }
        return !insIt.hasNext()
    }
}
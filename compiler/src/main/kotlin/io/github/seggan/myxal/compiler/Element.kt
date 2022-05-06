package io.github.seggan.myxal.compiler

@Suppress("unused")
enum class Element(val text: String, val arity: Int, val vectorise: Boolean, val modifiesStack: Boolean = false) {

    /**
     * Math
     */
    ADD("+", 2, true),
    BINARY("b", 1, true),
    COMPLEMENT("⌐", 1, true),
    DIVIDE("/", 2, true),
    DIV_FIVE("₅", 1, false),
    DIV_THREE("₃", 1, false),
    DOUBLE_REPEAT("d", 1, true),
    EXPONENTIATE("e", 2, true),
    FACTORS("K", 1, false),
    HALVE("½", 2, true, true),
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
    ITEM_SPLIT("÷", 2, false, true),
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
    FILTER("F", 2, false),
    FLATTEN("f", 1, false),
    HEAD("h", 1, false),
    HEAD_EXTRACT("ḣ", 1, false, true),
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
    UNINTERLEAVE("y", 2, false, true),
    UNIQUIFY("U", 1, false),
    ZIP("Z", 2, false),
    ZIP_SELF("z", 1, false),

    /**
     * Stack
     */
    TRIPLICATE("D", 1, false, true),
    DUPLICATE("`", 1, false, true),
    POP("_", 1, false, true),

    /**
     * Misc
     */
    FUNCTION_CALL("†", 2, false),
    GET_REQUEST("¨U", 1, true),
    PRINT("₴", 1, false),
    PRINT_NO_POP("…", 1, false),
    PRINTLN(",", 1, false);

    companion object {
        fun getByText(text: String): Element {
            for (e in values()) {
                if (e.text == text) {
                    return e
                }
            }
            throw JyxalCompileException("Unknown element: $text")
        }
    }
}
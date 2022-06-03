package io.github.seggan.myxal.runtime.text

import io.github.seggan.myxal.runtime.list.MyxalList
import io.github.seggan.myxal.runtime.math.BigComplex
import io.github.seggan.myxal.runtime.myxal
import io.github.seggan.myxal.runtime.unescapeString
import java.math.BigDecimal

class JsonParser(private val json: String) {
    private var index = 0

    // must be String, BigComplex, or JyxalList
    fun parse(): Any {
        skipWhitespace()
        checkEnd()
        val result = parseValue()
        index = 0
        return result
    }

    private fun parseValue(): Any {
        return when (json[index]) {
            '"' -> parseString()
            '{' -> parseObject()
            '[' -> parseArray()
            't' -> parseTrue()
            'f' -> parseFalse()
            'n' -> parseNull()
            '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> parseNumber()
            else -> throw RuntimeException("Unexpected character ${json[index]} at index $index in string $json")
        }
    }

    private fun parseTrue(): BigComplex {
        index += 4
        return true.myxal()
    }

    private fun parseFalse(): BigComplex {
        index += 5
        return false.myxal()
    }

    private fun parseNull(): BigComplex {
        index += 4
        return false.myxal()
    }

    private fun parseNumber(): BigComplex {
        val sb = StringBuilder()
        if (json[index] == '-') {
            sb.append(json[index])
            index++
        }
        while (index < json.length && (Character.isDigit(json[index]) || json[index] == '.')) {
            sb.append(json[index])
            index++
        }
        return BigDecimal(sb.toString()).myxal()
    }

    private fun parseArray(): MyxalList {
        index++
        skipWhitespace()
        val result = ArrayList<Any>()
        if (json[index] == ']') {
            index++
            return result.myxal()
        }
        while (true) {
            checkEnd()
            result.add(parseValue())
            skipWhitespace()
            checkEnd()
            if (json[index] == ']') {
                index++
                break
            }
            if (json[index] != ',') {
                throw RuntimeException("Expected ',' or ']' at index $index in string $json")
            }
            index++
            skipWhitespace()
        }
        return result.myxal()
    }

    private fun parseObject(): MyxalList {
        index++
        skipWhitespace()
        val result = ArrayList<Any>()
        if (json[index] == '}') {
            index++
            return result.myxal()
        }
        while (true) {
            checkEnd()
            val key = parseString()
            skipWhitespace()
            checkEnd()
            if (json[index] != ':') {
                throw RuntimeException("Expected ':' at index $index in string $json")
            }
            index++
            skipWhitespace()
            checkEnd()
            result.add(MyxalList.create(key, parseValue()))
            skipWhitespace()
            checkEnd()
            if (json[index] == '}') {
                index++
                break
            }
            if (json[index] != ',') {
                throw RuntimeException("Expected ',' or '}' at index $index in string $json")
            }
            index++
            skipWhitespace()
        }
        return result.myxal()
    }

    private fun parseString(): String {
        index++
        val sb = StringBuilder()
        var c = json[index]
        while (c != '"') {
            sb.append(c)
            index++
            checkEnd()
            c = json[index]
        }
        index++
        return unescapeString(sb.toString())
    }

    private fun checkEnd() {
        check(index < json.length) { "Unexpected end of string '$json'" }
    }

    private fun skipWhitespace() {
        while (index < json.length && Character.isWhitespace(json[index])) {
            index++
        }
    }
}
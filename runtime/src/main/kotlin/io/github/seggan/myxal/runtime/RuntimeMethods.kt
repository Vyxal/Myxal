@file:Suppress("unused", "MemberVisibilityCanBePrivate") @file:JvmName("RuntimeMethods")

package io.github.seggan.myxal.runtime

import io.github.seggan.myxal.runtime.list.MyxalList
import io.github.seggan.myxal.runtime.math.BigComplex
import io.github.seggan.myxal.runtime.math.BigComplexMath
import io.github.seggan.myxal.runtime.text.JsonParser
import java.io.ByteArrayInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.invoke.MethodHandle
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern
import java.util.zip.GZIPInputStream
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.streams.toList

private val COMMA_PATTERN: Regex by lazy(LazyThreadSafetyMode.NONE) { ",".toRegex() }
private val SPACE_PATTERN: Regex by lazy(LazyThreadSafetyMode.NONE) { " ".toRegex() }
private val PLUS_SPACE_I_PATTERN: Regex by lazy(LazyThreadSafetyMode.NONE) { "[+\\si]".toRegex() }

private val LONG_MAX_VALUE_AS_BIG: BigInteger by lazy(LazyThreadSafetyMode.NONE) { BigInteger.valueOf(Long.MAX_VALUE) }
private val LONG_MIN_VALUE_AS_BIG: BigInteger by lazy(LazyThreadSafetyMode.NONE) { BigInteger.valueOf(Long.MIN_VALUE) }

private val regexCache = mutableMapOf<String, Regex>()

fun add(a: Any, b: Any): Any {
    if (a is MyxalList) {
        if (b is MyxalList) {
            return a.zipmap(b) { o, p -> add(o, p) }.myxal()
        }
        return a.map { item: Any -> add(item, b) }
    } else if (b is MyxalList) {
        return b.map { item: Any -> add(a, item) }
    }
    return if (a is BigComplex && b is BigComplex) {
        a + b
    } else {
        a.toString() + b.toString()
    }
}

fun all(obj: Any): Any {
    if (obj is MyxalList) {
        for (item in obj) {
            if (!truthValue(item)) {
                return false.myxal()
            }
        }
        return true.myxal()
    } else if (obj is String) {
        for (c in obj) {
            if (!VOWELS.contains(c)) {
                return false.myxal()
            }
        }
        return true.myxal()
    }
    return true.myxal()
}

fun any(obj: Any): Any {
    return if (obj is String) {
        if (obj.length == 1) {
            obj.first().isUpperCase().myxal()
        } else {
            obj.any(Char::isUpperCase).myxal()
        }
    } else {
        for (item in iterator(obj)) {
            if (truthValue(item)) {
                return true.myxal()
            }
        }
        false.myxal()
    }
}

fun binary(obj: Any): Any {
    return if (obj is BigComplex) {
        binHelper(obj.toInt())
    } else {
        obj.toString().map { binHelper(it.code) }.myxal()
    }
}

fun binHelper(i: Int): MyxalList {
    return i.toString(2).map { (it - '0').myxal() }.myxal()
}

fun chrOrd(obj: Any): Any {
    return if (obj is BigComplex) {
        Character.toString(obj.re.intValueExact())
    } else {
        val str = obj.toString()
        if (str.length == 1) {
            str[0].code.myxal()
        } else {
            str.codePoints().toList().myxal()
        }
    }
}

private inline fun compare(a: Any, b: Any, predicate: (Int) -> Boolean): Any {
    return if (a is BigComplex && b is BigComplex) {
        predicate(a.compareTo(b)).myxal()
    } else {
        predicate(a.toString().compareTo(b.toString())).myxal()
    }
}

fun complement(obj: Any): Any {
    return if (obj is BigComplex) {
        BigComplex.ONE - obj
    } else {
        MyxalList.create(COMMA_PATTERN.split(obj.toString()))
    }
}

fun contains(a: Any, b: Any): Any {
    return if (a is MyxalList) {
        a.contains(b).myxal()
    } else {
        a.toString().contains(b.toString()).myxal()
    }
}

fun count(a: Any, b: Any): Any {
    return if (a is MyxalList) {
        a.count { it == b }
    } else {
        val pattern = Pattern.compile(Pattern.quote(b.toString()))
        var count = 0
        val matcher = pattern.matcher(a.toString())
        while (matcher.find()) {
            count++
        }
        count.myxal()
    }
}

fun cumulativeGroups(a: Any, b: Any): Any {
    return if (b is BigComplex) {
        if (a is MyxalList) {
            sequence {
                val window = ArrayList<Any>()
                val size = b.toInt()
                for (item in a) {
                    window.add(item)
                    if (window.size == size) {
                        yield(window.myxal())
                        window.removeAt(0)
                    }
                }
            }.myxal()
        } else {
            sequence {
                val window = StringBuilder()
                val size = b.toInt()
                for (item in a.toString()) {
                    window.append(item)
                    if (window.length == size) {
                        yield(window.toString())
                        window.deleteCharAt(0)
                    }
                }
            }.myxal()
        }
    } else {
        (len(a) == len(b)).myxal()
    }
}

fun divide(a: Any, b: Any): Any {
    return if (a is BigComplex) {
        if (b is BigComplex) {
            return a.divide(b, MathContext.DECIMAL128)
        }
        val list = ArrayList<Any>()
        var sb = StringBuilder()
        var count = BigInteger.ZERO
        val max = a.re.toBigInteger()
        for (c in a.toString()) {
            if (count == max) {
                list.add(sb.toString())
                sb = StringBuilder()
            }
            sb.append(c)
            count = count.add(BigInteger.ONE)
        }
        if (sb.isNotEmpty()) {
            list.add(sb.toString())
        }
        list.myxal()
    } else if (b is BigComplex) {
        val list = ArrayList<Any>()
        var sb = StringBuilder()
        var count = BigInteger.ZERO
        val max = b.re.toBigInteger()
        for (c in a.toString()) {
            if (count == max) {
                list.add(sb.toString())
                sb = StringBuilder()
            }
            sb.append(c)
            count += BigInteger.ONE
        }
        if (sb.isNotEmpty()) {
            list.add(sb.toString())
        }
        list.myxal()
    } else {
        val list = ArrayList<Any>()
        val str = a.toString()
        val delimiter = b.toString()
        var start = 0
        var end = str.indexOf(delimiter)
        while (end != -1) {
            list.add(str.substring(start, end))
            start = end + delimiter.length
            end = str.indexOf(delimiter, start)
        }
        if (start < str.length) {
            list.add(str.substring(start))
        }
        list.myxal()
    }
}

fun divFive(obj: Any): Any = divisibleBy(obj, 5)

fun divThree(obj: Any): Any = divisibleBy(obj, 3)

fun dotProduct(a: Any, b: Any): Any {
    return listify(a).zip(listify(b), ::multiply).reduce(::add)
}

fun doubleRepeat(obj: Any): Any {
    return when (obj) {
        is MyxalList -> {
            obj.map(::doubleRepeat)
        }
        is BigComplex -> {
            obj * 2
        }
        is Lambda -> {
            Lambda(2, obj.handle)
        }
        else -> {
            obj.toString() * 2
        }
    }
}

fun equal(a: Any, b: Any): Any {
    return if (a is BigComplex && b is BigComplex) {
        (a == b).myxal()
    } else {
        (a.toString() == b.toString()).myxal()
    }
}

fun exponentiate(a: Any, b: Any): Any {
    return if (a is BigComplex) {
        if (b is BigComplex) {
            BigComplexMath.pow(a, b, MathContext.DECIMAL128)
        } else {
            val str = b.toString()
            val c = str[0]
            val sb = StringBuilder(str)
            while (sb.length <= a.re.toInt()) {
                sb.append(c)
            }
            sb.toString()
        }
    } else if (b is BigComplex) {
        val str = a.toString()
        val c = str[0]
        val sb = StringBuilder(str)
        while (sb.length <= b.re.toInt()) {
            sb.append(c)
        }
        sb.toString()
    } else {
        val regex = regexCache.computeIfAbsent(a.toString(), String::toRegex)
        val groups = regex.find(b.toString())?.groups
        if (groups == null || groups.isEmpty()) {
            MyxalList.create()
        } else {
            val range = groups[0]?.range
            if (range == null) {
                MyxalList.create()
            } else {
                MyxalList.create(range.first, range.last)
            }
        }
    }
}

fun factors(obj: Any): Any {
    return when (obj) {
        is BigComplex -> {
            val value = obj.re.toBigInteger()
            val factors = LinkedHashSet<BigComplex>()
            if (LONG_MIN_VALUE_AS_BIG <= value && value <= LONG_MAX_VALUE_AS_BIG) {
                val num = value.toLong()
                val sqrt = sqrt(num.toDouble())
                val incrementer = if (num and 1 == 0L) 1 else 2
                var i = 1L
                while (i <= sqrt) {
                    if (num % i == 0L) {
                        factors.add(i.myxal())
                    }
                    i += incrementer
                }
            } else {
                val sqrt = value.sqrt()
                val incrementer = if (value and BigInteger.ONE == BigInteger.ZERO) BigInteger.ONE else BigInteger.TWO
                var i = BigInteger.ONE
                while (i <= sqrt) {
                    if (value % i == BigInteger.ZERO) {
                        factors.add(i.myxal())
                    }
                    i += incrementer
                }
            }
            factors.add(obj)
            factors.myxal()
        }
        is MyxalList -> {
            val prefixes = ArrayList<MyxalList>()
            var prefix = MyxalList.create()
            for (i in obj) {
                prefix = prefix.add(i)
                prefixes.add(prefix)
            }
            prefixes.myxal()
        }
        else -> {
            val str = obj.toString()
            val frequencies = LinkedHashMap<String, Int>()
            for (i in str.indices) {
                for (j in (i + 1)..str.length) {
                    val sub = str.substring(i, j)
                    frequencies.compute(sub) { _, value -> value?.plus(1) ?: 1 }
                }
            }
            frequencies.filterValues { it > 1 }.keys.myxal()
        }
    }
}

fun filter(a: Any, b: Any): Any {
    return if (b is Lambda) {
        listify(a).filter { truthValue(b.call(it)) }
    } else {
        val list = mutableListOf<Any>()
        iterator(a).forEach(list::add)
        listify(a).filter(list::contains)
    }
}

fun flatten(obj: Any): Any {
    return if (obj is MyxalList) {
        flattenImpl(obj)
    } else {
        val list = ArrayList<Any>()
        for (c in obj.toString()) {
            list.add(c.toString())
        }
        list.myxal()
    }
}

private fun flattenImpl(list: MyxalList): MyxalList {
    val newList = ArrayList<Any>()
    for (item in list) {
        if (item is MyxalList) {
            newList.addAll(flattenImpl(item))
        } else {
            newList.add(item)
        }
    }
    return newList.myxal()
}

fun functionCall(stack: ProgramStack): Any {
    return when (val obj = stack.pop()) {
        is Lambda -> obj.call(stack)
        is MyxalList -> obj.map { o -> (!truthValue(o)).myxal() }
        is BigComplex -> primeFactors(obj, ::HashSet).size
        else -> exec(obj.toString())
    }
}

fun getRequest(obj: Any): Any {
    var url = obj.toString()
    if (!url.startsWith("http://") && !url.startsWith("https://")) {
        url = "http://$url"
    }
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.setRequestProperty("User-Agent", "Mozilla/5.0 Jyxal")
    connection.setRequestProperty("Content-Type", "text/plain; charset=UTF-8")
    connection.instanceFollowRedirects = true
    connection.connect()
    val code = connection.responseCode
    if (code / 100 == 3) {
        val location = connection.getHeaderField("Location")
        return if (location != null) {
            getRequest(location)
        } else {
            throw IOException("Redirect without location")
        }
    } else if (code / 100 != 2) {
        return code.myxal()
    }
    var response: ByteArray
    connection.inputStream.use { inputStream -> response = inputStream.readAllBytes() }
    if (connection.contentEncoding == "gzip") {
        try {
            GZIPInputStream(ByteArrayInputStream(response)).use { stream ->
                return String(
                    stream.readAllBytes(),
                    StandardCharsets.UTF_8
                )
            }
        } catch (e: IOException) {
            return String(response, StandardCharsets.UTF_8)
        }
    }
    return String(response, StandardCharsets.UTF_8)
}

fun greaterThan(a: Any, b: Any): Any {
    return compare(a, b) { i: Int -> i > 0 }
}

fun greaterThanOrEqual(a: Any, b: Any): Any {
    return compare(a, b) { i: Int -> i >= 0 }
}

fun halve(stack: ProgramStack) {
    val o = vectorise(1, ::halve, stack)
    if (o != null) {
        stack.push(o)
        return
    }
    val obj = stack.pop()
    return if (obj is BigComplex) {
        stack.push(obj.divide(BigComplex.TWO, MathContext.DECIMAL128))
    } else {
        val str = obj.toString()
        val limit = str.length / 2 + 1
        stack.push(str.substring(0, limit))
        stack.push(str.substring(limit))
    }
}

fun head(obj: Any): Any {
    return if (obj is MyxalList) {
        obj[0]
    } else {
        if (obj.toString().isNotEmpty()) {
            obj.toString().substring(0, 1)
        } else {
            BigComplex.ZERO
        }
    }
}

fun headExtract(stack: ProgramStack) {
    val obj = stack.pop()
    return if (obj is MyxalList) {
        stack.push(obj[0])
        val iterator = obj.iterator()
        iterator.next()
        stack.push(MyxalList.create(iterator))
    } else {
        stack.push(obj.toString().substring(1))
        stack.push(obj.toString().substring(0, 1))
    }
}

fun hexToDecimal(obj: Any): Any {
    return BigInteger(obj.toString(), 16).myxal()
}

fun increment(obj: Any): Any {
    return if (obj is BigComplex) {
        obj + 1
    } else {
        SPACE_PATTERN.replace(obj.toString(), "0")
    }
}

fun indexInto(stack: ProgramStack): Any {
    val index = stack.pop()
    val obj = listify(stack.pop())
    if (index is BigComplex) {
        return obj[index.toInt()]
    } else if (index is MyxalList) {
        // using local function because im too lazy to do a bunch of casts
        fun i(ind: Int) = (index[ind] as BigComplex).toInt()
        return if (index.hasAtLeast(3)) {
            obj[i(0) until i(1) step i(2)]
        } else if (index.hasAtLeast(2)) {
            obj[i(0) until i(1)]
            // using size check here because who cares about generating a single list element
        } else if (index.size == 1) {
            obj[i(0)]
        } else {
            0.myxal()
        }
    } else {
        throw IllegalArgumentException("No strings allowed, ok? ($index)")
    }
}

fun infinitePrimes(): Any {
    return MyxalList.create(object : Iterator<Any> {
        private var n = 2L
        private var isOverflowed = false
        private val max = Long.MAX_VALUE - 1
        private var big = Long.MAX_VALUE.myxal()

        override fun hasNext(): Boolean {
            return true
        }

        override fun next(): Any {
            if (n == 2L) {
                n++
                return 1.myxal()
            }
            if (!isOverflowed) {
                if (n and 1L == 0L) {
                    n++
                }
                while (!isPrime(n)) {
                    n += 2
                    if (n >= max) {
                        isOverflowed = true
                        break
                    }
                }
                val res = BigComplex.valueOf(n)
                n += 2
                if (isOverflowed) {
                    while (!truthValue(isPrime(big))) {
                        big += 2
                    }
                    val result = big
                    big += 2
                    return result
                }
                return res
            } else {
                while (!truthValue(isPrime(big))) {
                    big += 2
                }
                val result = big
                big += 2
                return result
            }
        }
    })
}

fun infiniteReplace(stack: ProgramStack): Any {
    val c = stack.pop()
    val b = stack.pop()
    val a = stack.pop()
    return if (a is MyxalList) {
        var prev: MyxalList = a
        var list: MyxalList = a
        do {
            list = list.map { o: Any -> if (o == b) c else o }
            if (list == prev) break
            prev = list
        } while (true)
        prev
    } else {
        var aString = a.toString()
        val bString = b.toString()
        val cString = c.toString()
        var prev = aString
        do {
            aString = aString.replace(bString, cString)
            if (a == prev) break
            prev = aString
        } while (true)
        aString
    }
}

fun interleave(a: Any, b: Any): Any {
    if (a is String && b is String) {
        return buildString {
            for (i in 0 until min(a.length, b.length)) {
                append(a[i])
                append(b[i])
            }
        }
    } else {
        val aIt = iterator(a)
        val bIt = iterator(b)
        return MyxalList.create(object : Iterator<Any> {
            private var isA = true

            override fun hasNext(): Boolean {
                return aIt.hasNext() || bIt.hasNext()
            }

            override fun next(): Any {
                return if (isA) {
                    isA = false
                    aIt.next()
                } else {
                    isA = true
                    bIt.next()
                }
            }
        })
    }
}

fun ior(obj: Any): Any {
    return when (obj) {
        is MyxalList -> {
            obj.map(::ior)
        }
        is BigComplex -> {
            MyxalList.range(BigComplex.ONE, obj + 1)
        }
        else -> {
            obj.toString().uppercase()
        }
    }
}

fun isEven(obj: Any): Any = divisibleBy(obj, 2)

private fun divisibleBy(obj: Any, factor: Int): BigComplex {
    return if (obj is BigComplex) {
        (obj % factor.toLong() == BigComplex.ZERO).myxal()
    } else {
        (len(obj) % factor == 0).myxal()
    }
}

fun isPrime(obj: Any): Any {
    return if (obj is BigComplex) {
        val n = obj.re.toBigInteger()
        if (LONG_MIN_VALUE_AS_BIG <= n && n <= LONG_MAX_VALUE_AS_BIG) {
            return isPrime(n.toLong()).myxal()
        } else {
            // we don't need to check if n is a small prime, because the other branch will do it
            if (!n.testBit(0)) return false.myxal()
            val sqrt = n.sqrt().add(BigInteger.ONE)
            val six = BigInteger.valueOf(6)
            var i = six
            while (i <= sqrt) {
                if (n % (i - BigInteger.ONE) == BigInteger.ZERO || n % (i + BigInteger.ONE) == BigInteger.ZERO) {
                    return false.myxal()
                }
                i += six
            }
        }
        true.myxal()
    } else {
        val str = obj.toString()
        val isUppercase = Character.isUpperCase(str[0])
        for (c in str) {
            if (Character.isUpperCase(c) != isUppercase) {
                return (-1).myxal()
            }
        }
        isUppercase.myxal()
    }
}

private fun isPrime(l: Long): Boolean {
    if (l < 2) return false
    if (l == 2L || l == 3L || l == 5L) return true
    if (l and 1 == 0L || l % 3 == 0L || l % 5 == 0L) return false
    val sqrt = sqrt(l.toDouble()).toLong() + 1
    var step = 4L
    var i = 6L
    while (i <= sqrt) {
        if (l % i == 0L) {
            return false
        }
        step = 6 - step
        i += step
    }
    return true
}

fun itemSplit(stack: ProgramStack) {
    var obj = stack.pop()
    if (obj is BigComplex) {
        obj = obj.re.toBigInteger().toString()
    }
    if (obj is MyxalList) {
        obj.forEach(stack::push)
    } else {
        obj.toString().forEach {
            stack.push(it.toString())
        }
    }
}

fun izr(obj: Any): Any {
    return if (obj is BigComplex) {
        MyxalList.range(BigComplex.ZERO, obj + 1)
    } else {
        val str = obj.toString()
        val list = ArrayList<Any>(str.length)
        for (c in str) {
            list.add(Character.isAlphabetic(c.code).myxal())
        }
        list.myxal()
    }
}

fun join(a: Any, b: Any): Any {
    val list = listify(a)
    return list.joinToString(b.toString())
}

fun joinByNothing(obj: Any): Any {
    return when (obj) {
        is MyxalList -> {
            val sb = StringBuilder()
            for (item in obj) {
                sb.append(item)
            }
            sb.toString()
        }
        is BigComplex -> (obj.abs(MathContext.DECIMAL128) <= BigDecimal.ONE).myxal()
        is Lambda -> {
            var result = BigComplex.ZERO
            while (!truthValue(obj.call(result))) {
                result += 1
            }
            result
        }
        else -> obj.toString()
    }
}

fun joinByNewlines(obj: Any): Any {
    val sb = StringBuilder()
    val it = iterator(obj)
    while (it.hasNext()) {
        sb.append(it.next())
        sb.append('\n')
    }
    return sb.toString()
}

fun jsonParse(obj: Any): Any {
    return JsonParser(obj.toString()).parse()
}

fun length(obj: Any): Any {
    return if (obj is MyxalList) obj.size.myxal() else obj.toString().length.myxal()
}

fun lessThan(a: Any, b: Any): Any {
    return compare(a, b) { i: Int -> i < 0 }
}

fun lessThanOrEqual(a: Any, b: Any): Any {
    return compare(a, b) { i: Int -> i <= 0 }
}

fun listi(obj: Any): Any = MyxalList.create(obj)

fun logicalAnd(a: Any, b: Any): Any {
    return if (truthValue(a)) {
        if (truthValue(b)) {
            a
        } else {
            b
        }
    } else {
        a
    }
}

fun logicalOr(a: Any, b: Any): Any {
    return if (truthValue(a)) {
        a
    } else {
        if (truthValue(b)) {
            b
        } else {
            a
        }
    }
}

fun map(a: Any, b: Any): Any {
    return if (b is Lambda) {
        if (a is MyxalList) a.map(b::call) else listify(a).map(b::call)
    } else {
        val list = ArrayList<MyxalList>()
        for (item in iterator(b)) {
            list.add(MyxalList.create(a, item))
        }
        list.myxal()
    }
}

fun mapGetSet(stack: ProgramStack): Any {
    val map = stack.pop()
    var key = stack.pop()
    if (key is MyxalList) {
        val list = key
        // set
        key = stack.pop()
        for ((i, o) in list.withIndex()) {
            if (o is MyxalList) {
                if (o.size >= 2 && o[0] == key) {
                    return MyxalList.create(replacementIterator(list.iterator(), i, MyxalList.create(o[0], map)))
                }
            }
        }
        return list.add(MyxalList.create(key, map))
    }
    for (o in map as MyxalList) {
        if (o is MyxalList) {
            if (o.size >= 2 && o[0] == key) {
                return if (o.size == 2) {
                    o[1]
                } else {
                    val iterator = o.iterator()
                    iterator.next()
                    MyxalList.create(iterator)
                }
            }
        }
    }
    return BigComplex.ZERO
}

fun max(obj: Any): Any {
    val iterator = iterator(obj)
    if (!iterator.hasNext()) {
        return 0.myxal()
    }
    var max = iterator.next()
    while (iterator.hasNext()) {
        val next = iterator.next()
        if (sortHelper(next) > sortHelper(max)) {
            max = next
        }
    }
    return max
}

fun merge(a: Any, b: Any): Any {
    return if (a is MyxalList) {
        if (b is MyxalList) {
            a.addAll(b)
        } else {
            a.add(b)
        }
    } else if (b is MyxalList) {
        b.add(BigInteger.ZERO, a)
    } else {
        a.toString() + b.toString()
    }
}

fun min(obj: Any): Any {
    val iterator = iterator(obj)
    if (!iterator.hasNext()) {
        return 0.myxal()
    }
    var min = iterator.next()
    while (iterator.hasNext()) {
        val next = iterator.next()
        if (sortHelper(next) < sortHelper(min)) {
            min = next
        }
    }
    return min
}

fun mirror(obj: Any): Any {
    val reversed = reverse(obj)
    return when (obj) {
        is MyxalList -> obj.addAll(reversed as MyxalList)
        is BigComplex -> BigComplex.valueOf(BigDecimal(obj.toString() + reversed.toString()))
        else -> obj.toString() + reversed
    }
}

fun moduloFormat(a: Any, b: Any): Any {
    return if (a is BigComplex && b is BigComplex) {
        a % b
    } else {
        if (a is BigComplex) {
            b.toString().replace("%", a.toString())
        } else {
            a.toString().replace("%", b.toString())
        }
    }
}

fun multiCommand(a: Any, b: Any): Any {
    return if (a is BigComplex) {
        if (b is BigComplex) {
            b loga a
        } else {
            repeatCharacters(b.toString(), a.toInt())
        }
    } else if (b is BigComplex) {
        repeatCharacters(a.toString(), b.toInt())
    } else {
        val sb = StringBuilder()
        val aString = a.toString()
        val bString = b.toString()
        for (i in aString.indices) {
            val ch = bString[i % bString.length]
            val aChar = aString[i]
            if (Character.isUpperCase(ch)) {
                sb.append(aChar.uppercaseChar())
            } else if (Character.isLowerCase(ch)) {
                sb.append(aChar.lowercaseChar())
            } else {
                sb.append(aChar)
            }
        }
        if (aString.length > bString.length) {
            sb.append(aString.substring(bString.length))
        }
        sb.toString()
    }
}

fun multiply(a: Any, b: Any): Any {
    return if (a is BigComplex) {
        if (b is BigComplex) {
            return a * b
        } else if (b is Lambda) {
            return Lambda(a.toInt(), b.handle)
        }
        b.toString() * a.toInt()
    } else if (a is Lambda && b is BigComplex) {
        Lambda(b.toInt(), a.handle)
    } else {
        val aString = a.toString()
        if (b is BigComplex) {
            return aString * b.toInt()
        }
        val bString = b.toString()
        val sb = StringBuilder()
        for (c in bString) {
            val index = aString.indexOf(c)
            if (index >= 0) {
                sb.append(aString[(index + 1) % aString.length])
            } else {
                sb.append(c)
            }
        }
        sb.toString()
    }
}

fun negate(obj: Any): Any {
    return if (obj is BigComplex) obj.negate()
    else obj.toString().map { if (it.isUpperCase()) it.lowercaseChar() else it.uppercaseChar() }.joinToString("")
}

fun prepend(a: Any, b: Any): Any {
    return if (a is MyxalList) {
        a.add(BigInteger.ZERO, b)
    } else if (a is BigComplex && b is BigComplex) {
        BigDecimal(b.toString() + a.toString()).myxal()
    } else {
        a.toString() + b.toString()
    }
}

fun printToFile(stack: ProgramStack) {
    FileOutputStream("test.out").use { os ->
        while (!stack.isEmpty()) {
            os.write(stack.pop().toString().toByteArray(StandardCharsets.UTF_8))
        }
    }
}

fun range(a: Any, b: Any): Any {
    return if (a is BigComplex) {
        when (b) {
            is BigComplex -> MyxalList.range(a, b)
            is Lambda -> TODO("Cumulative reduce")
            else -> b.toString().padEnd(a.toInt(), ' ')
        }
    } else if (b is BigComplex) {
        a.toString().padStart(b.toInt(), ' ')
    } else {
        val regex = regexCache.computeIfAbsent(a.toString(), String::toRegex)
        regex.matches(b.toString()).myxal()
    }
}

fun reduce(stack: ProgramStack): Any {
    val o = vectorise(2, ::reduce, stack)
    if (o != null) return o
    val b = stack.pop()
    if (b is Lambda) {
        val a = iterator(stack.pop())
        if (!a.hasNext()) {
            return BigInteger.ZERO
        }
        var result = a.next()
        while (a.hasNext()) {
            result = b.call(result, a.next())
        }
        return result
    }

    return when (b) {
        is BigComplex -> BigComplex.valueOf(
            BigDecimal(b.re.toString().reversed()),
            BigDecimal(b.im.toString().reversed())
        )
        is MyxalList -> {
            val result = ArrayList<Any>()
            for (i in b.size - 1 downTo 0) {
                result.add(b[i])
            }
            result.myxal()
        }
        else -> b.toString().reversed()
    }

}

fun remove(a: Any, b: Any): Any {
    fun firstInts(f: Lambda, limit: BigInteger): MyxalList {
        return sequence {
            var i = BigInteger.ZERO
            var num = BigComplex.ONE
            while (i < limit) {
                if (truthValue(f.call(num))) {
                    yield(num)
                    i += BigInteger.ONE
                }
                num += BigComplex.ONE
            }
        }.myxal()
    }
    return when {
        a is MyxalList -> a.filter { it != b }
        a is BigComplex && b is Lambda -> firstInts(b, a.re.toBigInteger())
        a is Lambda && b is BigComplex -> firstInts(a, b.re.toBigInteger())
        else -> a.toString().replace(b.toString(), "")
    }
}

fun removeAtIndex(a: Any, b: Any): Any {
    return if (a is BigComplex) {
        if (b is MyxalList) {
            return b.remove(a.re.toInt())
        }
        val str = b.toString()
        val index = a.toInt()
        val sb = StringBuilder()
        for (i in str.indices) {
            if (i != index) {
                sb.append(str[i])
            }
        }
        sb.toString()
    } else if (b is BigComplex) {
        if (a is MyxalList) {
            return a.remove(b.re.toInt())
        }
        val str = a.toString()
        val index = b.toInt()
        val sb = StringBuilder()
        for (i in str.indices) {
            if (i != index) {
                sb.append(str[i])
            }
        }
        sb.toString()
    } else {
        throw IllegalArgumentException("$a, $b")
    }
}

fun replace(stack: ProgramStack): Any {
    val c = stack.pop()
    val b = stack.pop()
    val a = stack.pop()
    return if (a is MyxalList) {
        a.map { if (it == b) c else it }
    } else {
        a.toString().replace(b.toString(), c.toString())
    }
}

fun reverse(obj: Any): Any {
    return if (obj is MyxalList) {
        val newList = ArrayList<Any>()
        for (item in obj) {
            newList.add(0, item)
        }
        newList.myxal()
    } else {
        val str = obj.toString()
        val sb = StringBuilder()
        for (i in str.length - 1 downTo 0) {
            sb.append(str[i])
        }
        sb.toString()
    }
}

fun sliceUntil(a: Any, b: Any): Any {
    return if (a is BigComplex) {
        sliceUntilImpl(b, a.re.toBigInteger())
    } else if (b is BigComplex) {
        sliceUntilImpl(a, b.re.toBigInteger())
    } else {
        regexCache.computeIfAbsent(a.toString(), String::toRegex).findAll(b.toString()).map(MatchResult::value).myxal()
    }
}

private fun sliceUntilImpl(a: Any, b: BigInteger): Any {
    val iterator = iterator(a)
    return MyxalList.create(object : Iterator<Any> {
        private var count = BigInteger.ZERO

        override fun hasNext(): Boolean {
            return count < b
        }

        override fun next(): Any {
            count += BigInteger.ONE
            return iterator.next()
        }
    })
}

fun sort(obj: Any): Any = listify(obj).sortedBy(::sortHelper).myxal()

fun sortByFunction(a: Any, b: Any): Any {
    return if (b is Lambda) {
        listify(a).sortedBy { sortHelper(b.call(it)) }.myxal()
    } else if (a is BigComplex && b is BigComplex) {
        MyxalList.range(a, b + 1)
    } else {
        regexCache.computeIfAbsent(b.toString(), String::toRegex).split(a.toString()).myxal()
    }
}

private fun sortHelper(obj: Any): BigComplex {
    return when (obj) {
        is BigComplex -> obj
        is MyxalList -> obj.size.myxal()
        else -> obj.toString().length.myxal()
    }
}

fun splitOn(a: Any, b: Any): Any {
    return if (a is MyxalList) {
        val superList = ArrayList<Any>()
        var newList = ArrayList<Any>()
        for (item in a) {
            if (item == b) {
                superList.add(newList.myxal())
                newList = ArrayList()
            } else {
                newList.add(item)
            }
        }
        superList.add(newList.myxal())
        superList.myxal()
    } else {
        MyxalList.create(a.toString().split(b.toString()))
    }
}

fun sqrt(obj: Any): Any {
    return if (obj is BigComplex) {
        BigComplexMath.sqrt(obj, MathContext.DECIMAL128)
    } else {
        buildString {
            for ((i, c) in obj.toString().withIndex()) {
                if (i % 2 == 0) {
                    append(c)
                }
            }
        }
    }
}

fun subtract(a: Any, b: Any): Any {
    return if (a is BigComplex) {
        if (b is BigComplex) {
            a.subtract(b)
        } else "-" * a.toInt() + b
    } else if (b is BigComplex) {
        a.toString() + "-" * b.toInt()
    } else {
        a.toString().replace(b.toString(), "")
    }
}

fun sum(obj: Any): Any {
    return when (obj) {
        is MyxalList -> {
            var sum: Any = BigComplex.ZERO
            for (item in obj) {
                sum = add(sum, item)
            }
            sum
        }
        is BigComplex -> {
            obj.toString().filter { it.isDigit() }.map { it.code - 48 }.sum().myxal()
        }
        else -> {
            obj.toString().sumOf(Char::code).myxal()
        }
    }
}

fun spaces(obj: Any): Any {
    return when (obj) {
        is BigComplex -> " " * obj.toInt()
        is MyxalList -> {
            val half = obj.size / 2
            val list = ArrayList<Any>()
            val iterator = obj.iterator()
            for (i in 0 until half) {
                list.add(iterator.next())
            }
            while (iterator.hasNext()) {
                list.add(iterator.next())
            }
            list.myxal()
        }
        else -> {
            val string = obj.toString()
            "`${unescapeString(string)}`$string"
        }
    }
}

fun strip(a: Any, b: Any): Any {
    return if (a is MyxalList) {
        val iterator = a.listIterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next != b) {
                iterator.previous()
                break
            }
        }
        if (!iterator.hasNext()) {
            return MyxalList.create()
        }
        var list = MyxalList.create(iterator)
        while (list[list.size - 1] == b) {
            list = list.remove(list.size - 1)
        }
        list
    } else {
        val patternString = b.toString()
        "(^$patternString)|($patternString\$)".toRegex().replace(a.toString(), "")
    }
}

fun tail(obj: Any): Any {
    return if (obj is MyxalList) {
        if (obj.isLazy()) {
            val iterator = obj.iterator()
            var last: Any = BigComplex.ZERO
            while (iterator.hasNext()) {
                last = iterator.next()
            }
            last
        } else {
            obj[obj.size - 1]
        }
    } else {
        val s = obj.toString()
        if (s.isEmpty()) {
            BigComplex.ZERO
        } else {
            s[s.length - 1]
        }
    }
}

fun triplicate(stack: ProgramStack) {
    val obj = stack.pop()
    for (i in 0 until 3) {
        stack.push(obj)
    }
}

fun truthyIndexes(obj: Any): Any {
    return when (obj) {
        is MyxalList -> {
            val list = ArrayList<Any>()
            for ((index, item) in obj.withIndex()) {
                if (truthValue(item)) {
                    list.add(index.myxal())
                }
            }
            list.myxal()
        }
        is BigComplex -> obj * 3
        is Lambda -> Lambda(3, obj.handle)
        else -> {
            if (obj.toString().isEmpty()) {
                MyxalList.create()
            } else {
                MyxalList.range(0, obj.toString().length)
            }
        }
    }
}

fun twoPow(obj: Any): Any {
    return if (obj is BigComplex) {
        BigComplexMath.pow(BigComplex.TWO, obj, MathContext.DECIMAL128)
    } else {
        exec(obj.toString())
    }
}

fun uneval(obj: Any): Any = "\"${escapeString(obj.toString())}\""

fun uninterleave(stack: ProgramStack) {
    val obj = stack.pop()
    stack.push(sequence {
        val iterator = iterator(obj)
        while (iterator.hasNext()) {
            yield(iterator.next())
            if (iterator.hasNext()) {
                iterator.next()
            }
        }
    }.myxal())
    stack.push(sequence {
        val iterator = iterator(obj)
        while (iterator.hasNext()) {
            iterator.next()
            if (iterator.hasNext()) {
                yield(iterator.next())
            }
        }
    }.myxal())
}

fun uniquify(obj: Any): Any {
    return if (obj is MyxalList) {
        obj.distinct().myxal()
    } else {
        obj.toString().toCharArray().distinct().joinToString("")
    }
}

fun zip(a: Any, b: Any): Any {
    val toZip = if (b is Lambda) {
        listify(a).map(b::call)
    } else {
        listify(b)
    }
    return listify(a).zip(toZip)
}

fun zipSelf(obj: Any): Any {
    return if (obj is MyxalList) {
        obj.zip(obj)
    } else {
        obj.toString().toCharArray().zip(obj.toString().toCharArray()) { a, b -> MyxalList.create(a, b) }.myxal()
    }
}

fun monadVectorise(obj: Any, handle: MethodHandle): Any {
    if (obj is MyxalList) {
        return obj.map { monadVectorise(it, handle) }
    }
    return handle.invoke(obj)
}

fun dyadVectorise(left: Any, right: Any, handle: MethodHandle): Any {
    return if (left is MyxalList && right is MyxalList) {
        left.zipmap(right) { l, r -> dyadVectorise(l, r, handle) }
    } else if (left is MyxalList) {
        left.map { dyadVectorise(it, right, handle) }
    } else if (right is MyxalList) {
        right.map { dyadVectorise(left, it, handle) }
    } else {
        handle.invoke(left, right)
    }
}

operator fun BigComplex.plus(other: BigComplex): BigComplex = this.add(other)
operator fun BigComplex.plus(other: Long): BigComplex = this.add(BigComplex.valueOf(other))
operator fun BigComplex.minus(other: BigComplex): BigComplex = this.subtract(other)
operator fun BigComplex.minus(other: Long): BigComplex = this.subtract(BigComplex.valueOf(other))
operator fun BigComplex.times(other: BigComplex): BigComplex = this.multiply(other)
operator fun BigComplex.times(other: Long): BigComplex = this.multiply(BigComplex.valueOf(other))
operator fun BigComplex.rem(other: BigComplex) = BigComplex.valueOf(re.remainder(other.re), im.remainder(other.re))
operator fun BigComplex.rem(other: Long) = rem(BigComplex.valueOf(other))

infix fun BigComplex.loga(a: BigComplex): BigComplex {
    // complex numbers go brr
    // going off of that fact that ln(x)/ln(a) = loga(x)
    // and ln(x) = ln(|x|) + i * arg(x)
    val top = BigComplexMath.log(this, MathContext.DECIMAL128)
    val bottom = BigComplexMath.log(a, MathContext.DECIMAL128)
    return top.divide(bottom, MathContext.DECIMAL128)
}

infix fun BigComplex.loga(a: Long): BigComplex = this.loga(BigComplex.valueOf(a))
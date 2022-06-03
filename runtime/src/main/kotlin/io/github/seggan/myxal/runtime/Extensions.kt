package io.github.seggan.myxal.runtime

import io.github.seggan.myxal.runtime.list.MyxalList
import io.github.seggan.myxal.runtime.math.BigComplex
import java.math.BigDecimal
import java.math.BigInteger

fun List<Any>.myxal(): MyxalList = MyxalList.create(this)

@JvmName("nullableJyxal")
fun List<Any?>.myxal(): MyxalList {
    val result = ArrayList<Any>()
    for (item in this) {
        result.add(item!!)
    }
    return MyxalList.create(this)
}

fun Iterable<Any>.myxal(): MyxalList = MyxalList.fromIterableLazy(this)
fun Sequence<Any>.myxal(): MyxalList = MyxalList.create(this.iterator())

fun Boolean.myxal(): BigComplex = if (this) BigComplex.ONE else BigComplex.ZERO
fun Int.myxal(): BigComplex = BigComplex.valueOf(this.toLong())
fun Long.myxal(): BigComplex = BigComplex.valueOf(this)
fun BigInteger.myxal(): BigComplex = BigComplex.valueOf(this.toBigDecimal())
fun BigDecimal.myxal(): BigComplex = BigComplex.valueOf(this)

operator fun <T> List<T>.times(n: Int): List<T> {
    val result = ArrayList<T>()
    for (i in 0 until n) {
        result.addAll(this)
    }
    return result
}

@JvmName("mutableTimes")
operator fun <T> MutableList<T>.times(n: Int): MutableList<T> {
    val result = ArrayList<T>()
    for (i in 0 until n) {
        result.addAll(this)
    }
    return result
}

operator fun CharSequence.times(n: Int): String {
    val result = StringBuilder()
    for (i in 0 until n) {
        result.append(this)
    }
    return result.toString()
}

operator fun String.times(n: Int): String = JavaNatives.repeat(this, n)
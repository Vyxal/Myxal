package io.github.seggan.myxal.runtime.list

import io.github.seggan.myxal.runtime.ProgramStack
import io.github.seggan.myxal.runtime.math.BigComplex
import io.github.seggan.myxal.runtime.myxal
import io.github.seggan.myxal.runtime.plus
import java.math.BigInteger

abstract class MyxalList : Collection<Any> {

    companion object {

        fun create(generator: Iterator<Any>): MyxalList {
            return LazyList(generator)
        }

        @JvmStatic
        fun create(vararg array: Any): MyxalList {
            return FiniteList(listOf(*array))
        }

        @JvmStatic
        fun create(collection: Collection<Any>): MyxalList {
            return FiniteList(collection.toList())
        }

        @JvmStatic
        @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
        fun create(stack: ProgramStack): MyxalList {
            val list = ArrayList<Any>(stack.size)
            while (!stack.isEmpty()) {
                list.add(stack.removeLast()!!)
            }
            return FiniteList(list)
        }

        fun create(): MyxalList {
            return FiniteList()
        }

        /**
         * Create an infinite list
         */
        fun createInf(generator: () -> Any): MyxalList {
            return LazyList(object : Iterator<Any> {
                override fun hasNext(): Boolean {
                    return true
                }

                override fun next(): Any {
                    return generator()
                }
            })
        }

        fun range(start: BigComplex, end: BigComplex): MyxalList {
            return LazyList(object : Iterator<BigComplex> {
                var current = start

                override fun hasNext(): Boolean {
                    return current < end
                }

                override fun next(): BigComplex {
                    val result = current
                    current += 1
                    return result
                }
            })
        }

        fun range(start: Int, end: Int): MyxalList {
            return LazyList(object : Iterator<BigComplex> {
                var current = start

                override fun hasNext(): Boolean {
                    return current < end
                }

                override fun next(): BigComplex {
                    val result = current++
                    return result.myxal()
                }
            })
        }

        fun fromIterableLazy(iterable: Iterable<Any>): MyxalList {
            return LazyList(iterable.iterator())
        }
    }

    protected fun vyxalListFormat(list: List<Any>): String {
        val sb = StringBuilder()
        sb.append("⟨")
        val it = list.iterator()
        var isFirst = true
        while (it.hasNext()) {
            if (isFirst) {
                isFirst = false
            } else {
                sb.append(" | ")
            }
            sb.append(it.next())
        }
        sb.append("⟩")
        return sb.toString()
    }

    /**
     * Whether there exists an element at the given index
     */
    abstract fun hasInd(ind: Int): Boolean

    abstract fun isLazy(): Boolean

    abstract fun toNonLazy(): MyxalList

    abstract operator fun get(ind: Int): Any
    abstract operator fun get(ind: IntProgression): MyxalList

    abstract fun hasAtLeast(amount: Int): Boolean

    abstract fun listIterator(): ListIterator<Any>

    override fun iterator(): Iterator<Any> {
        return listIterator()
    }

    abstract fun remove(ind: Int): MyxalList

    abstract fun map(f: (Any) -> Any): MyxalList

    abstract fun filter(pred: (Any) -> Boolean): MyxalList

    abstract fun add(ind: BigInteger, value: Any): MyxalList

    abstract fun add(value: Any): MyxalList

    abstract fun addAll(iterable: Iterable<Any>): MyxalList

    fun zip(iterable: Iterable<Any>): MyxalList {
        return zipmap(iterable, MyxalList::create)
    }

    abstract fun zipmap(iterable: Iterable<Any>, f: (Any, Any) -> Any): MyxalList

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MyxalList) return false

        val it1 = this.iterator()
        val it2 = other.iterator()
        while (it1.hasNext() && it2.hasNext()) {
            if (it1.next() != it2.next()) {
                return false
            }
        }

        return !it1.hasNext() && !it2.hasNext()
    }

    abstract override fun hashCode(): Int

    override fun containsAll(elements: Collection<Any>): Boolean {
        for (element in elements) {
            if (!this.contains(element)) {
                return false
            }
        }
        return true
    }

    override fun isEmpty(): Boolean {
        return !this.iterator().hasNext()
    }
}

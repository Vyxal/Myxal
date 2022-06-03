package io.github.seggan.myxal.runtime.list

import io.github.seggan.myxal.runtime.math.BigComplex
import java.math.BigInteger

internal class FiniteList(private val backing: List<Any>) : MyxalList() {

    override val size: Int
        get() = backing.size

    override fun contains(element: Any): Boolean {
        return backing.contains(element)
    }

    constructor() : this(emptyList<Any>())

    override fun get(ind: Int): Any {
        return if (backing.size > ind) {
            backing[ind]
        } else {
            BigComplex.ZERO
        }
    }

    override fun get(ind: IntProgression): MyxalList {
        return create(backing.filter(ind::contains))
    }

    override fun toNonLazy(): MyxalList {
        return this
    }

    override fun add(ind: BigInteger, value: Any): MyxalList {
        val newBacking = ArrayList<Any>(backing)
        newBacking.add(ind.toInt(), value)
        return FiniteList(newBacking)
    }

    override fun addAll(iterable: Iterable<Any>): MyxalList {
        val newBacking = ArrayList(backing)
        newBacking.addAll(iterable)
        return FiniteList(newBacking)
    }

    override fun remove(ind: Int): MyxalList {
        val newBacking = ArrayList(backing)
        newBacking.removeAt(ind)
        return FiniteList(newBacking)
    }

    override fun containsAll(elements: Collection<Any>): Boolean {
        return backing.containsAll(elements)
    }

    override fun hashCode(): Int {
        return backing.hashCode()
    }

    override fun add(value: Any): MyxalList {
        val newBacking = ArrayList(backing)
        newBacking.add(value)
        return FiniteList(newBacking)
    }

    override fun listIterator(): ListIterator<Any> {
        return backing.listIterator()
    }

    override fun isLazy(): Boolean {
        return false
    }

    override fun toString(): String {
        return vyxalListFormat(backing)
    }

    override fun hasAtLeast(amount: Int): Boolean {
        return size >= amount
    }

    override fun hasInd(ind: Int): Boolean {
        return size > ind
    }

    override fun map(f: (Any) -> Any): MyxalList {
        return FiniteList(backing.map(f))
    }

    override fun filter(pred: (Any) -> Boolean): MyxalList {
        return FiniteList(backing.filter(pred))
    }

    override fun zipmap(iterable: Iterable<Any>, f: (Any, Any) -> Any): MyxalList {
        return FiniteList(backing.zip(iterable, f))
    }
}
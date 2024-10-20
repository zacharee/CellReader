package dev.zwander.cellreader.data.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.*
import java.util.function.Consumer
import java.util.function.IntFunction
import java.util.function.Predicate
import java.util.stream.Stream
import kotlin.Comparator

class UpdatableTreeSet<E> : TreeSet<E> {
    private var wrapped: TreeSet<E>

    override val size: Int
        get() = wrapped.size

    constructor() {
        wrapped = TreeSet()
    }

    constructor(comparator: Comparator<in E>) {
        wrapped = TreeSet(comparator)
    }

    constructor(c: Collection<E>) : this() {
        addAll(c)
    }

    constructor(s: SortedSet<E>) : this(s.comparator()) {
        addAll(s)
    }

    fun updateComparator(newComparator: Comparator<in E>) {
        val newSet = TreeSet(newComparator)
        newSet.addAll(wrapped)
        wrapped = newSet
    }

    override fun clear() {
        wrapped.clear()
    }

    override fun clone(): Any {
        return wrapped.clone()
    }

    override fun toString(): String {
        return wrapped.toString()
    }

    override fun add(element: E): Boolean {
        return wrapped.add(element)
    }

    override fun comparator(): Comparator<in E> {
        return wrapped.comparator()
    }

    override fun addAll(elements: Collection<E>): Boolean {
        return wrapped.addAll(elements)
    }

    override fun ceiling(e: E): E {
        return wrapped.ceiling(e)
    }

    override fun contains(element: E): Boolean {
        return wrapped.contains(element)
    }

    override fun descendingIterator(): MutableIterator<E> {
        return wrapped.descendingIterator()
    }

    override fun descendingSet(): NavigableSet<E> {
        return wrapped.descendingSet()
    }

    override fun first(): E {
        return wrapped.first()
    }

    override fun hashCode(): Int {
        return wrapped.hashCode()
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        return wrapped.containsAll(elements)
    }

    override fun equals(other: Any?): Boolean {
        return wrapped.equals(other)
    }

    override fun floor(e: E): E {
        return wrapped.floor(e)
    }

    override fun headSet(toElement: E): SortedSet<E> {
        return wrapped.headSet(toElement)
    }

    override fun isEmpty(): Boolean {
        return wrapped.isEmpty()
    }

    override fun iterator(): MutableIterator<E> {
        return wrapped.iterator()
    }

    override fun last(): E {
        return wrapped.last()
    }

    override fun parallelStream(): Stream<E> {
        return wrapped.parallelStream()
    }

    override fun pollFirst(): E {
        return wrapped.pollFirst()
    }

    override fun pollLast(): E {
        return wrapped.pollLast()
    }

    override fun higher(e: E): E {
        return wrapped.higher(e)
    }

    override fun lower(e: E): E {
        return wrapped.lower(e)
    }

    override fun remove(element: E): Boolean {
        return wrapped.remove(element)
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        return wrapped.removeAll(elements.toSet())
    }

    override fun removeIf(filter: Predicate<in E>): Boolean {
        return wrapped.removeIf(filter)
    }

    override fun spliterator(): Spliterator<E> {
        return wrapped.spliterator()
    }

    override fun stream(): Stream<E> {
        return wrapped.stream()
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        return wrapped.retainAll(elements)
    }

    override fun headSet(toElement: E, inclusive: Boolean): NavigableSet<E> {
        return wrapped.headSet(toElement, inclusive)
    }

    override fun toArray(): Array<Any> {
        return wrapped.toArray()
    }

    override fun subSet(fromElement: E, toElement: E): SortedSet<E> {
        return wrapped.subSet(fromElement, toElement)
    }

    override fun tailSet(fromElement: E): SortedSet<E> {
        return wrapped.tailSet(fromElement)
    }

    override fun subSet(
        fromElement: E,
        fromInclusive: Boolean,
        toElement: E,
        toInclusive: Boolean
    ): NavigableSet<E> {
        return wrapped.subSet(fromElement, fromInclusive, toElement, toInclusive)
    }

    override fun <T : Any?> toArray(a: Array<out T>): Array<T> {
        return wrapped.toArray(a)
    }

    override fun forEach(action: Consumer<in E>) {
        wrapped.forEach(action)
    }

    override fun tailSet(fromElement: E, inclusive: Boolean): NavigableSet<E> {
        return wrapped.tailSet(fromElement, inclusive)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Deprecated(
        "This member is not fully supported by Kotlin compiler, so it may be absent or have different signature in next major version",
        replaceWith = ReplaceWith(""),
        level = DeprecationLevel.WARNING,
    )
    override fun <T : Any?> toArray(generator: IntFunction<Array<T>>): Array<T> {
        val array = generator.apply(0)
        return wrapped.toArray(array)
    }
}
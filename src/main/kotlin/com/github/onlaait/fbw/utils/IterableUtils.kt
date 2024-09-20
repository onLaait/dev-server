package com.github.onlaait.fbw.utils

import kotlin.random.Random

object IterableUtils {
    fun <E> MutableIterable<E>.removeSingle(predicate: (E) -> Boolean): Boolean {
        val iterator = this.iterator()
        while (iterator.hasNext()) {
            val element = iterator.next()
            if (predicate(element)) {
                iterator.remove()
                return true
            }
        }
        return false
    }

    fun <T> Collection<T>.takeRandom(n: Int): List<T> {
        require(n >= 0) { "Requested element count $n is less than zero." }
        if (n == 0) return emptyList()
        if (n >= size) return shuffled()
        val list = mutableListOf<T>()
        val mutableList = toMutableList()
        repeat(n) {
            val i = Random.nextInt(mutableList.size)
            list += mutableList[i]
            mutableList.removeAt(i)
        }
        return list
    }
}
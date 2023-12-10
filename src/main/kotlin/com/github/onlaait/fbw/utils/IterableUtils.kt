package com.github.onlaait.fbw.utils

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
}
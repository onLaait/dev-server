package com.github.onlaait.fbw.game.utils

import com.github.onlaait.fbw.geometry.Shape

class Hitbox(private val maker: () -> Array<Element>) {

    private var elements: Array<Element>? = null

    fun get(): Array<Element> {
        if (elements == null) {
            elements = maker()
//            Logger.debug { "hitbox" }
        }
        return elements!!
    }

    fun refresh() {
        elements = null
    }

    data class Element(val shape: Shape, val critical: Boolean = false)
}
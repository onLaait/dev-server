package com.github.onlaait.fbw.model

abstract class Model {

    var shouldTick = true

    fun start() {
        ModelManager.addModel(this)
    }

    fun stop() {
        ModelManager.removeModel(this)
    }

    fun remove() {
        ModelManager.removeModel(this)
        onRemoved()
    }

    abstract fun onRemoved()

    fun tick() {
        if (!shouldTick) return
        onTick()
    }

    abstract fun onTick()
}
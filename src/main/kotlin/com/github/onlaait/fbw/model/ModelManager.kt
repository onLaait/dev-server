package com.github.onlaait.fbw.model

object ModelManager {

    private val models = mutableListOf<Model>()

    fun addModel(model: Model) {
        if (models.contains(model)) return
        models += model
    }

    fun removeModel(model: Model) {
        models -= model
    }

    fun tick() {
        models.forEach { it.tick() }
    }
}
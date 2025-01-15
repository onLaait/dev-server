package com.github.onlaait.fbw.model

object ModelManager {

    private val models = mutableListOf<FGenericModel>()

    fun registerModel(model: FGenericModel) {
        if (models.contains(model)) return
        models += model
    }

    fun unregisterModel(model: FGenericModel) {
        models -= model
    }

    fun tick() {
        models.forEach { if (it.shouldTick) it.draw() }
    }
}
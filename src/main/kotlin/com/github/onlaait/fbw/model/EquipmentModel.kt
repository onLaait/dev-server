package com.github.onlaait.fbw.model

import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.Instance
import net.worldseed.multipart.GenericModelImpl
import java.util.function.Function
import java.util.function.Predicate

class EquipmentModel(val model: PlayerModel, val id: String) : GenericModelImpl() {

    override fun getId() = id

    lateinit var animationHandler: FAnimationHandler

    override fun registerBoneSuppliers() {
        boneSuppliers[Predicate { it == "head" }] = Function { EquipmentModelBone(it.pivot, it.name, it.rotation, it.model, it.scale) }
    }

    override fun init(instance: Instance?, position: Pos, scale: Float) {
        super.init(instance, position, scale)
        animationHandler = FAnimationHandler(this)
    }
}
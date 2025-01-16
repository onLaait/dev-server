package com.github.onlaait.fbw.model

import net.kyori.adventure.util.RGBLike
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.worldseed.multipart.GenericModel
import net.worldseed.multipart.model_bones.ModelBoneImpl

class DummyBone(pivot: Point, name: String, rotation: Point, model: GenericModel, scale: Float) : ModelBoneImpl(pivot, name, rotation, model, scale) {
    override fun draw() {}

    override fun setState(state: String?) {}

    override fun getPosition(): Point = Pos.ZERO

    override fun calculateScale(): Point = Pos.ZERO

    override fun calculatePosition(): Pos  = Pos.ZERO

    override fun calculateRotation(): Point = Pos.ZERO

    override fun addViewer(player: Player?) {}

    override fun removeViewer(player: Player?) {}

    override fun removeGlowing() {}

    override fun removeGlowing(player: Player?) {}

    override fun setGlowing(color: RGBLike?) {}

    override fun setGlowing(player: Player?, color: RGBLike?) {}

    override fun attachModel(model: GenericModel?) {}

    override fun getAttachedModels() = emptyList<GenericModel>()

    override fun detachModel(model: GenericModel?) {}

    override fun setGlobalRotation(yaw: Double, pitch: Double) {}
}
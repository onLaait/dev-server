package com.github.onlaait.fbw.model

import com.github.onlaait.fbw.math.times
import com.github.onlaait.fbw.utils.editMeta
import net.kyori.adventure.util.RGBLike
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta
import net.minestom.server.entity.metadata.display.ItemDisplayMeta
import net.worldseed.multipart.GenericModel
import net.worldseed.multipart.Quaternion
import net.worldseed.multipart.model_bones.BoneEntity
import net.worldseed.multipart.model_bones.ModelBone
import net.worldseed.multipart.model_bones.ModelBoneImpl

class FirstPersonViewModelBone(
    pivot: Point,
    name: String,
    rotation: Point,
    model: GenericModel,
    scale: Float,
) : ModelBoneImpl(pivot, name, rotation, model, scale) {

    init {
        if (this.offset != null) {
            this.stand = BoneEntity(EntityType.ITEM_DISPLAY, model, name)

            this.stand.editMeta<ItemDisplayMeta> {
                this.itemStack = items["normal"]!!
                this.scale = Vec(scale.toDouble(), scale.toDouble(), scale.toDouble())
                this.displayContext = ItemDisplayMeta.DisplayContext.THIRD_PERSON_LEFT_HAND
                this.transformationInterpolationDuration = 1
                this.billboardRenderConstraints = AbstractDisplayMeta.BillboardConstraints.CENTER
            }
        }
    }

    override fun draw() {
        children.forEach { it.draw() }
        if (offset == null || stand == null) return

        val q = Quaternion(calculateRotation())

        val scale = Vec.fromPoint(calculateScale() * super.scale)
        val rightRotation = floatArrayOf(q.x().toFloat(), q.y().toFloat(), q.z().toFloat(), q.w().toFloat())
        val translation = calculatePositionInternal()

        val meta = stand.entityMeta as ItemDisplayMeta
        val scaleChanged = meta.scale != scale
        val rightRotationChanged = !meta.rightRotation.contentEquals(rightRotation)
        val translationChanged = meta.translation != translation
        if (scaleChanged || rightRotationChanged || translationChanged) {
            stand.editMeta<ItemDisplayMeta> {
                this.transformationInterpolationStartDelta = 0
                if (scaleChanged) this.scale = scale
                if (rightRotationChanged) this.rightRotation = rightRotation
                if (translationChanged) this.translation = translation
            }
        }
    }

    private fun calculatePositionInternal(): Vec {
        if (offset == null) return Vec.ZERO
        var p = offset
        p = applyTransform(p)
        p = calculateGlobalRotation(p)
        return Pos.fromPoint(p).div(4.0).mul(scale.toDouble()).asVec()
    }

    override fun calculatePosition() = model.position.withView(0f, 0f)

    override fun calculateRotation(): Point {
        var q = calculateFinalAngle(Quaternion(propogatedRotation))
        val pq = Quaternion(Vec(0.0, 180 - model.globalRotation, 0.0))
        q = pq.multiply(q)
        return q.toEuler()
    }

    override fun calculateScale() = calculateFinalScale(propogatedScale)

    override fun getPosition(): Point = calculatePosition()

    override fun setGlobalRotation(yaw: Double, pitch: Double) {}

    override fun setState(state: String) {}

    override fun addViewer(player: Player) {
        stand?.addViewer(player)
    }

    override fun removeViewer(player: Player) {
        stand?.removeViewer(player)
    }

    override fun removeGlowing() {}

    override fun setGlowing(color: RGBLike) {}

    override fun removeGlowing(player: Player) {}

    override fun setGlowing(player: Player, color: RGBLike) {}

    override fun attachModel(model: GenericModel) {}

    override fun getAttachedModels(): List<GenericModel> = emptyList()

    override fun detachModel(model: GenericModel) {}

    override fun getChildren(): Collection<ModelBone> = children
}
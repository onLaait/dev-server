package com.github.onlaait.fbw.model

import com.github.onlaait.fbw.math.Vec3d
import com.github.onlaait.fbw.math.minus
import com.github.onlaait.fbw.math.plus
import com.github.onlaait.fbw.math.times
import com.github.onlaait.fbw.utils.editMeta
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.metadata.display.ItemDisplayMeta
import net.minestom.server.instance.Instance
import net.worldseed.multipart.GenericModel
import net.worldseed.multipart.ModelLoader.AnimationType
import net.worldseed.multipart.Quaternion
import net.worldseed.multipart.model_bones.BoneEntity
import java.util.concurrent.CompletableFuture

class EquipmentModelBone(
    pivot: Point,
    name: String,
    rotation: Point,
    model: GenericModel,
    scale: Float,
    private var following: HumanModelBone?
) : HumanModelBone(pivot, name, rotation, model, scale) {

    override val extraRotation: Vec3d = Vec3d()
        get() = following?.extraRotation ?: field
    override val extraOffset = Vec3d()
        get() = following?.extraOffset ?: field

    init {
        if (this.offset != null) {
            this.stand = BoneEntity(EntityType.ITEM_DISPLAY, model, name)

            this.stand.editMeta<ItemDisplayMeta> {
                this.itemStack = items["normal"]!!
                this.scale = Vec(scale.toDouble(), scale.toDouble(), scale.toDouble())
                this.displayContext = ItemDisplayMeta.DisplayContext.THIRD_PERSON_LEFT_HAND
                this.transformationInterpolationDuration = 1
                this.width = 3f
                this.height = 3f
                this.viewRange = 0.6f
            }
        }
    }

    override fun spawn(instance: Instance, position: Pos): CompletableFuture<Void> {
        parent.let { if (it is HumanModelBone) following = it }
        return super.spawn(instance, position)
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

    override fun applyTransform(p: Point): Point {
        var endPos = p

        if (diff != null) {
            val a = pivot + extraOffset - diff
            endPos = calculateScale(endPos, propogatedScale, a)
            endPos = calculateRotation(endPos, propogatedRotation + extraRotation, a)
        } else {
            val a = pivot + extraOffset
            endPos = calculateScale(endPos, propogatedScale, a)
            endPos = calculateRotation(endPos, propogatedRotation + extraRotation, a)
        }

        for (currentAnimation in allAnimations) {
            if (currentAnimation != null && currentAnimation.isPlaying) {
                if (currentAnimation.type == AnimationType.TRANSLATION) {
                    val calculatedTransform = currentAnimation.transform
                    endPos += calculatedTransform
                }
            }
        }

        parent?.let { endPos = it.applyTransform(endPos) }

        return endPos
    }

    private fun calculatePositionInternal(): Vec {
        if (offset == null) return Vec.ZERO
        var p = offset
        p += extraOffset
        p = applyTransform(p)
        p = calculateGlobalRotation(p)
        return Pos.fromPoint(p).div(4.0).mul(scale.toDouble()).asVec()
    }
}
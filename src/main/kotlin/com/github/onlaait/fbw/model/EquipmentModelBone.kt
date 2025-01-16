package com.github.onlaait.fbw.model

import com.github.onlaait.fbw.math.Vec3d
import com.github.onlaait.fbw.math.minus
import com.github.onlaait.fbw.math.plus
import com.github.onlaait.fbw.math.times
import com.github.onlaait.fbw.server.DefaultExceptionHandler
import com.github.onlaait.fbw.utils.editMeta
import net.kyori.adventure.util.RGBLike
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.metadata.display.ItemDisplayMeta
import net.minestom.server.instance.Instance
import net.worldseed.multipart.GenericModel
import net.worldseed.multipart.ModelLoader.AnimationType
import net.worldseed.multipart.Quaternion
import net.worldseed.multipart.model_bones.BoneEntity
import net.worldseed.multipart.model_bones.ModelBone
import net.worldseed.multipart.model_bones.ModelBoneImpl
import net.worldseed.multipart.model_bones.ModelBoneViewable
import java.util.concurrent.CompletableFuture

class EquipmentModelBone(
    pivot: Point,
    name: String,
    rotation: Point,
    model: GenericModel,
    scale: Float,
    private val parentBone: PlayerModelBone?
) : ModelBoneImpl(pivot, name, rotation, model, scale), ModelBoneViewable, FModelBone {

    private val attached = mutableListOf<GenericModel>()

    override val extraRotation = Vec3d()
        get() = parentBone?.extraRotation ?: field
    override val extraOffset = Vec3d()
        get() = parentBone?.extraOffset ?: field

    init {
        if (this.offset != null) {
            stand = BoneEntity(EntityType.ITEM_DISPLAY, model, name)

            stand.editMeta<ItemDisplayMeta> {
                this.scale = Vec(scale.toDouble(), scale.toDouble(), scale.toDouble())
                this.displayContext = ItemDisplayMeta.DisplayContext.THIRD_PERSON_LEFT_HAND
                this.transformationInterpolationDuration = 1
                this.posRotInterpolationDuration = 1
                this.viewRange = 1000f
            }
        }
        setState("normal")
    }

    override fun spawn(instance: Instance, position: Pos): CompletableFuture<Void> {
        val correctLocation = (180 + model.globalRotation + 360) % 360
        return super.spawn(instance, Pos.fromPoint(position).withYaw(correctLocation.toFloat()))
            .whenCompleteAsync { _, t -> DefaultExceptionHandler.uncaughtException(null, t) }
    }

    override fun draw() {
        children.forEach { it.draw() }
        if (offset == null || stand == null) return

        val scale = calculateScale()
        val q = Quaternion(calculateRotation())
        stand.editMeta<ItemDisplayMeta> {
            transformationInterpolationStartDelta = 0
            this.scale = Vec.fromPoint(scale * super.scale)
            rightRotation = floatArrayOf(q.x().toFloat(), q.y().toFloat(), q.z().toFloat(), q.w().toFloat())
            translation = calculatePositionInternal()
        }

        val position = calculatePosition()
        stand.teleport(position)
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
                    endPos = endPos.add(calculatedTransform)
                }
            }
        }

        parent?.let { endPos = it.applyTransform(endPos) }

        return endPos
    }

    override fun calculatePosition(): Pos {
        return model.position.withView(0f, 0f)
    }

    private fun calculatePositionInternal(): Vec {
        if (this.offset == null) return Vec.ZERO
        var p = this.offset
        p += extraOffset
        p = applyTransform(p)
        p = calculateGlobalRotation(p)
        return Pos.fromPoint(p).div(4.0).mul(scale.toDouble()).asVec()
    }

    override fun calculateRotation(): Point {
        var q = calculateFinalAngle(Quaternion(propogatedRotation + extraRotation))
        val pq = Quaternion(Vec(0.0, 180 - model.globalRotation, 0.0))
        q = pq.multiply(q)
        return q.toEuler()
    }

    override fun calculateScale() = calculateFinalScale(propogatedScale)

    override fun setState(state: String) {
        if (this.stand == null) return
        val item = items[state] ?: return
        stand.editMeta<ItemDisplayMeta> {
            itemStack = item
        }
    }

    override fun getPosition(): Point = calculatePosition()

    override fun addViewer(player: Player) {
        stand?.addViewer(player)
        attached.forEach { it.addViewer(player) }
    }

    override fun removeViewer(player: Player) {
        stand?.removeViewer(player)
    }

    override fun removeGlowing() {
        stand?.isGlowing = false
    }

    override fun setGlowing(color: RGBLike) {
        stand?.isGlowing = true
    }

    override fun removeGlowing(player: Player) {}

    override fun setGlowing(player: Player, color: RGBLike) {}

    override fun attachModel(model: GenericModel) {
        attached.add(model)
    }

    override fun getAttachedModels() = attached

    override fun detachModel(model: GenericModel) {
        attached.remove(model)
    }

    override fun getChildren(): Collection<ModelBone> = emptyList()

    override fun setGlobalRotation(yaw: Double, pitch: Double) {}
}
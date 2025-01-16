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
import net.minestom.server.item.ItemComponent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.item.component.HeadProfile
import net.worldseed.multipart.GenericModel
import net.worldseed.multipart.ModelLoader.AnimationType
import net.worldseed.multipart.Quaternion
import net.worldseed.multipart.model_bones.BoneEntity
import net.worldseed.multipart.model_bones.ModelBone
import net.worldseed.multipart.model_bones.ModelBoneImpl
import net.worldseed.multipart.model_bones.ModelBoneViewable
import java.util.concurrent.CompletableFuture

class PlayerModelBone(
    pivot: Point,
    name: String,
    rotation: Point,
    model: GenericModel,
    translation: Int,
    private val extraDiff: Vec,
    headProfile: HeadProfile,
    private val slim: Boolean
) : ModelBoneImpl(pivot, name, rotation, model, 1f), ModelBoneViewable, FModelBone {

    private val baseTranslation = translation.toDouble()

    init {
        if (this.offset != null) {
            this.stand = BoneEntity(EntityType.ITEM_DISPLAY, model, name)
            this.stand.editMeta<ItemDisplayMeta> {
                width = 2f
                height = 2f
                viewRange = 0.6f
                transformationInterpolationDuration = 1
                posRotInterpolationDuration = 1
                this.translation = Vec(0.0, baseTranslation, 0.0)
                displayContext = ItemDisplayMeta.DisplayContext.THIRD_PERSON_RIGHT_HAND
                itemStack = ItemStack.builder(Material.PLAYER_HEAD)
                    .set(ItemComponent.PROFILE, headProfile)
                    .set(ItemComponent.CUSTOM_MODEL_DATA, customModelDataFromName(name))
                    .build()
            }
        }

        this.diff = this.pivot.add(extraDiff)
    }

    private fun customModelDataFromName(name: String): Int {
        return when (name) {
            "head" -> 1
            "body" -> 2
            "right_arm" -> if (!slim) 3 else 4
            "left_arm" -> if (!slim) 5 else 6
            "right_leg" -> 7
            "left_leg" -> 8
            else -> throw IllegalArgumentException()
        }
    }

    override val extraRotation = Vec3d()
    override val extraOffset = Vec3d()

    override fun spawn(instance: Instance, position: Pos): CompletableFuture<Void> {
        val correctLocation = (180 + model.globalRotation + 360) % 360
        return super.spawn(instance, Pos.fromPoint(position).withYaw(correctLocation.toFloat()))
            .whenCompleteAsync { _, t -> DefaultExceptionHandler.uncaughtException(null, t) }
    }

    override fun draw() {
        if (offset == null || stand == null) return

        val scale = calculateScale()
        val q = Quaternion(calculateRotation())
        stand.editMeta<ItemDisplayMeta> {
            transformationInterpolationStartDelta = 0
            this.scale = Vec(scale.x() * super.scale, scale.y() * super.scale, scale.z() * super.scale)
            rightRotation = floatArrayOf(q.x().toFloat(), q.y().toFloat(), q.z().toFloat(), q.w().toFloat())
            translation = calculatePositionInternal().withY { it + baseTranslation }
        }

        val position = calculatePosition()
        stand.teleport(position)
    }

    override fun applyTransform(p: Point): Point {
        var endPos = p

        if (diff != null) {
            endPos = calculateScale(endPos, propogatedScale, pivot - diff + extraOffset * PlayerModel.MAGIC_NUMBER)
            endPos = calculateRotation(endPos, propogatedRotation, pivot - diff + extraOffset * PlayerModel.MAGIC_NUMBER)
        } else {
            endPos = calculateScale(endPos, propogatedScale, pivot)
            endPos = calculateRotation(endPos, propogatedRotation, pivot)
        }

        for (currentAnimation in allAnimations) {
            if (currentAnimation != null && currentAnimation.isPlaying) {
                if (currentAnimation.type == AnimationType.TRANSLATION) {
                    val calculatedTransform = currentAnimation.transform * PlayerModel.MAGIC_NUMBER
                    endPos = endPos.add(calculatedTransform)
                }
            }
        }

        if (this.parent != null) {
            endPos = parent.applyTransform(endPos)
        }

        return endPos
    }

    override fun calculatePosition(): Pos {
        return model.position.withView(0f, 0f)
    }

    private fun calculatePositionInternal(): Vec {
        if (this.offset == null) return Vec.ZERO
        var p = this.offset
        p += extraOffset * PlayerModel.MAGIC_NUMBER
        p += Vec(0.0, -extraDiff.y, 0.0)
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

    override fun setState(state: String) {}

    override fun getPosition(): Point = calculatePosition()

    override fun addViewer(player: Player) {
        stand?.addViewer(player)
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

    override fun attachModel(model: GenericModel) {}

    override fun getAttachedModels(): List<GenericModel> = emptyList()

    override fun detachModel(model: GenericModel) {}

    override fun getChildren(): Collection<ModelBone> = emptyList()

    override fun setGlobalRotation(yaw: Double, pitch: Double) {}
}

package com.github.onlaait.fbw.game.gestures

import com.github.onlaait.fbw.server.DefaultExceptionHandler
import com.github.onlaait.fbw.utils.editMeta
import net.kyori.adventure.util.RGBLike
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerSkin
import net.minestom.server.entity.metadata.display.ItemDisplayMeta
import net.minestom.server.instance.Instance
import net.minestom.server.item.ItemComponent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.item.component.HeadProfile
import net.worldseed.multipart.GenericModel
import net.worldseed.multipart.Quaternion
import net.worldseed.multipart.model_bones.BoneEntity
import net.worldseed.multipart.model_bones.ModelBone
import net.worldseed.multipart.model_bones.ModelBoneImpl
import net.worldseed.multipart.model_bones.ModelBoneViewable
import java.util.concurrent.CompletableFuture

class ModelBoneEmote(
    pivot: Point,
    name: String,
    rotation: Point,
    model: GenericModel,
    translation: Int,
    private val verticalOffset: Double,
    skin: PlayerSkin,
    private val slim: Boolean
) : ModelBoneImpl(pivot, name, rotation, model, 1f), ModelBoneViewable {

    init {
        if (this.offset != null) {
            this.stand = BoneEntity(EntityType.ITEM_DISPLAY, model, name)
            this.stand.editMeta<ItemDisplayMeta> {
                width = 1f
                height = 1f
                viewRange = 0.6f
                transformationInterpolationDuration = 1
                posRotInterpolationDuration = 1
                this.translation = Vec(0.0, translation.toDouble(), 0.0)
                displayContext = ItemDisplayMeta.DisplayContext.THIRD_PERSON_RIGHT_HAND
                itemStack = ItemStack.builder(Material.PLAYER_HEAD)
                    .set(ItemComponent.PROFILE, HeadProfile(skin))
                    .set(ItemComponent.CUSTOM_MODEL_DATA, customModelDataFromName(name))
                    .build()
            }
        }

        when (this.name) {
            "head", "body" -> {
                this.diff = this.pivot
            }

            "right_arm" -> {
                this.diff = this.pivot.add(-1.17, 0.0, 0.0)
            }

            "left_arm" -> {
                this.diff = this.pivot.add(1.17, 0.0, 0.0)
            }

            "right_leg" -> {
                this.diff = this.pivot.add(-0.4446, 0.0, 0.0)
            }

            "left_leg" -> {
                this.diff = this.pivot.add(0.4446, 0.0, 0.0)
            }
        }
    }

    override fun spawn(instance: Instance, position: Pos): CompletableFuture<Void> {
        val correctLocation = (180 + model.globalRotation + 360) % 360
        return super.spawn(instance, Pos.fromPoint(position).withYaw(correctLocation.toFloat()))
            .whenCompleteAsync { _, t ->
                DefaultExceptionHandler.uncaughtException(null, t)
            }
    }

    override fun draw() {
        children.forEach { it.draw() }
        if (offset == null || stand == null) return

        val scale = calculateScale()
        val position = calculatePosition()

        val q = Quaternion(calculateRotation())
        stand.editMeta<ItemDisplayMeta> {
            transformationInterpolationStartDelta = 0
            this.scale = Vec(scale.x() * super.scale, scale.y() * super.scale, scale.z() * super.scale)
            rightRotation = floatArrayOf(q.x().toFloat(), q.y().toFloat(), q.z().toFloat(), q.w().toFloat())
        }
        stand.teleport(position.withView(0f, 0f))
    }

    override fun calculatePosition(): Pos {
        var p = offset ?: Pos.ZERO
        p = applyTransform(p)
        p = calculateGlobalRotation(p)

        return Pos.fromPoint(p)
            .div(4.0, 4.0, 4.0).mul(scale.toDouble())
            .add(model.position)
            .add(0.0, verticalOffset, 0.0)
            .add(model.globalOffset)
    }

    override fun calculateRotation(): Point {
        var q = calculateFinalAngle(Quaternion(propogatedRotation))
        val pq = Quaternion(Vec(0.0, 180 - model.globalRotation, 0.0))
        q = pq.multiply(q)

        return q.toEuler()
    }

    override fun calculateScale() = Vec.ONE

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

    override fun setState(state: String) {
        throw UnsupportedOperationException("Cannot set state on an emote")
    }

    override fun getPosition(): Point = calculatePosition()

    override fun addViewer(player: Player) {
        if (stand != null) stand.addViewer(player)
    }

    override fun removeViewer(player: Player) {
        if (stand != null) stand.removeViewer(player)
    }

    override fun removeGlowing() {
        if (stand != null) stand.isGlowing = false
    }

    override fun setGlowing(color: RGBLike) {
        if (stand != null) stand.isGlowing = true
    }

    override fun removeGlowing(player: Player) {}

    override fun setGlowing(player: Player, color: RGBLike) {}

    override fun attachModel(model: GenericModel) {
        throw UnsupportedOperationException("Cannot attach a model to this bone type")
    }

    override fun getAttachedModels(): List<GenericModel> = emptyList()

    override fun detachModel(model: GenericModel) {
        throw UnsupportedOperationException("Cannot detach a model from this bone type")
    }

    override fun getChildren(): Collection<ModelBone> = emptyList()

    override fun setGlobalRotation(yaw: Double, pitch: Double) {}
}

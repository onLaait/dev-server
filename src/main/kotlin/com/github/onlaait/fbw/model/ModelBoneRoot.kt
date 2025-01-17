package com.github.onlaait.fbw.model

import com.github.onlaait.fbw.entity.UntickingEntity
import com.github.onlaait.fbw.server.Schedule
import com.github.onlaait.fbw.utils.editMeta
import net.kyori.adventure.util.RGBLike
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.metadata.display.ItemDisplayMeta
import net.minestom.server.instance.Instance
import net.worldseed.multipart.GenericModel
import net.worldseed.multipart.Quaternion
import net.worldseed.multipart.model_bones.ModelBoneImpl
import net.worldseed.multipart.model_bones.ModelBoneViewable
import java.util.concurrent.CompletableFuture

class ModelBoneRoot(pivot: Point, name: String, rotation: Point, model: GenericModel, scale: Float) : ModelBoneImpl(pivot, name, rotation, model, scale), ModelBoneViewable {

    val baseStand = UntickingEntity(EntityType.ITEM_DISPLAY).apply {
        editMeta<ItemDisplayMeta> {
            this.posRotInterpolationDuration = 2
            this.width = 2f
            this.height = 2f
            this.viewRange = 0.6f
        }
    }

    override fun spawn(instance: Instance, position: Pos): CompletableFuture<Void> {
        baseStand.setInstance(instance, position).join()
        Schedule.manager.scheduleNextTick {
            val model = model as PlayerModel
            (model.parts + model.equipment.parts).forEach {
                it.entity?.let { baseStand.addPassenger(it) }
            }
        }
        return CompletableFuture.completedFuture(null)
    }

    override fun draw() {
        children.forEach { it.draw() }
        val pos = calculatePosition()
        if (position != pos) teleport(pos)
    }

    override fun calculatePosition() = model.position.withView(0f, 0f)

    override fun calculateRotation(): Point {
        val q = calculateFinalAngle(Quaternion(propogatedRotation))
        return q.toEuler()
    }

    override fun calculateScale() = calculateFinalScale(propogatedScale)

    override fun getPosition() = baseStand.position

    override fun setGlobalRotation(yaw: Double, pitch: Double) {}

    override fun teleport(position: Point) {
        baseStand.teleport(Pos.fromPoint(position))
    }

    override fun addViewer(player: Player) {
        baseStand.addViewer(player)
    }

    override fun removeViewer(player: Player) {
        baseStand.removeViewer(player)
    }

    override fun setState(state: String) {}

    override fun setGlowing(color: RGBLike) {}

    override fun removeGlowing() {}

    override fun setGlowing(player: Player, color: RGBLike) {}

    override fun removeGlowing(player: Player) {}

    override fun attachModel(model: GenericModel) {}

    override fun getAttachedModels(): List<GenericModel> = emptyList()

    override fun detachModel(model: GenericModel) {}

    override fun destroy() {
        super.destroy()
        baseStand.remove()
    }
}
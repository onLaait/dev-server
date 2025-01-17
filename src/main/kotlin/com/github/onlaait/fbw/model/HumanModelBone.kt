package com.github.onlaait.fbw.model

import com.github.onlaait.fbw.math.Vec3d
import com.github.onlaait.fbw.math.plus
import com.github.onlaait.fbw.server.DefaultExceptionHandler
import net.kyori.adventure.util.RGBLike
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.worldseed.multipart.GenericModel
import net.worldseed.multipart.Quaternion
import net.worldseed.multipart.model_bones.ModelBone
import net.worldseed.multipart.model_bones.ModelBoneImpl
import net.worldseed.multipart.model_bones.ModelBoneViewable
import java.util.concurrent.CompletableFuture

abstract class HumanModelBone(pivot: Point, name: String, rotation: Point, model: GenericModel, scale: Float) :
    ModelBoneImpl(pivot, name, rotation, model, scale), ModelBoneViewable {

    abstract val extraRotation: Vec3d
    abstract val extraOffset: Vec3d

    override fun spawn(instance: Instance, position: Pos): CompletableFuture<Void> {
        return super.spawn(instance, position.withView(0f, 0f))
            .whenCompleteAsync { _, t -> DefaultExceptionHandler.uncaughtException(null, t) }
    }

    override fun calculatePosition() = model.position.withView(0f, 0f)

    override fun calculateRotation(): Point {
        var q = calculateFinalAngle(Quaternion(propogatedRotation + extraRotation))
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

    override fun removeGlowing() {
        stand?.isGlowing = false
    }

    override fun setGlowing(color: RGBLike) {
        stand?.isGlowing = true
    }

    override fun removeGlowing(player: Player) {

    }

    override fun setGlowing(player: Player, color: RGBLike) {

    }

    override fun attachModel(model: GenericModel) {}

    override fun getAttachedModels(): List<GenericModel> = emptyList()

    override fun detachModel(model: GenericModel) {}

    override fun getChildren(): Collection<ModelBone> = children
}
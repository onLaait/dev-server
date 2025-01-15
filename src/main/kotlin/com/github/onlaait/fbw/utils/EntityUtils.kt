package com.github.onlaait.fbw.utils

import com.github.onlaait.fbw.math.Vec2f
import com.github.onlaait.fbw.math.minus
import com.github.onlaait.fbw.math.toDeg
import com.github.onlaait.fbw.server.Logger
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Entity
import net.minestom.server.entity.metadata.EntityMeta
import net.minestom.server.network.packet.server.play.HitAnimationPacket

inline fun <reified TMeta : EntityMeta> Entity.editMeta(noinline block: TMeta.() -> Unit) {
    editEntityMeta(TMeta::class.java, block)
}

fun Entity.shakeScreen(yaw: Float = 0f) {
    sendPacketToViewersAndSelf(HitAnimationPacket(entityId, yaw))
}

fun Entity.shakeScreen(from: Point) {
    val position = position
    val pos = Vec2f(position.x.toFloat(), position.z.toFloat())
    val fromPos = Vec2f(from.x().toFloat(), from.z().toFloat())
    val dir = position.direction().run { Vec2f(x.toFloat(), z.toFloat()).normalize() }
    val fromDir = (fromPos - pos).normalize()
    shakeScreen((90 + dir.angle(fromDir).toDeg()).also { Logger.debug { it } })
}
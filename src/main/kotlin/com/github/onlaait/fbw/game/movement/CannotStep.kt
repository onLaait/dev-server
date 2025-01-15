package com.github.onlaait.fbw.game.movement

import com.github.onlaait.fbw.entity.FEntity
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.EntityType

open class CannotStep : Movement() {

    private val vehicle = FEntity(EntityType.ITEM_DISPLAY)

    override fun start() {
        val pos = player.position
        vehicle.setInstance(player.instance, Pos(pos.x, pos.y + 0.6, pos.z, pos.yaw, 0f))
        vehicle.addPassenger(player)
    }

    override fun end() {
        vehicle.remove()
    }
}
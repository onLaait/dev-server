package com.github.onlaait.fbw.game.movement

import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType

open class CannotStep : Movement() {

    private val vehicle = Entity(EntityType.ITEM_DISPLAY)

    override fun start() {
        vehicle.setNoGravity(true)
        vehicle.setInstance(player.instance, player.position.withY { it + 0.6 }.withPitch(0f))
        vehicle.addPassenger(player)
    }

    override fun end() {
        vehicle.remove()
    }
}
package com.github.onlaait.fbw.game.movement

import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType

class CannotStepOrRotate : Movement() {

    private val dummy = Entity(EntityType.ITEM_DISPLAY)

    override fun start() {
        dummy.setInstance(player.instance, player.position.withY { it + player.eyeHeight })
        player.spectate(dummy)
    }

    override fun end() {
        dummy.remove()
    }
}
package com.github.onlaait.fbw.game.movement

import net.minestom.server.entity.Player

class PlayerMovements(val player: Player) {

    private val list = mutableListOf<Movement>()

    fun apply(movement: Movement) {
        movement.player = player
        list += movement
        movement.start()
    }

    fun remove(movement: Movement) {
        movement.end()
        list -= movement
    }
}
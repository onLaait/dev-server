package com.github.onlaait.fbw.game.movement

import com.github.onlaait.fbw.server.FPlayer


class PlayerMovements(val player: FPlayer) {

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
package com.github.onlaait.fbw.event

import net.minestom.server.entity.Player
import net.minestom.server.event.trait.PlayerEvent

class PlayerKeyInputEvent(private val player: Player, val key: Key) : PlayerEvent {

    override fun getPlayer() = player

    enum class Key {
        MOUSE_LEFT_DOWN,
        MOUSE_LEFT_UP,
        MOUSE_RIGHT_DOWN,
        MOUSE_RIGHT_UP,
        F,
        Q,
        NUM_1,
        NUM_2,
        NUM_3,
        NUM_4,
    }
}

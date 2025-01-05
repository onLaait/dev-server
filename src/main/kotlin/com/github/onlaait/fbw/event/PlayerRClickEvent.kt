package com.github.onlaait.fbw.event

import net.minestom.server.entity.Player
import net.minestom.server.event.trait.PlayerEvent

class PlayerRClickEvent(private val player: Player) : PlayerEvent {

    override fun getPlayer() = player
}

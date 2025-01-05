package com.github.onlaait.fbw.server

import com.github.onlaait.fbw.game.movement.DefaultMovement
import com.github.onlaait.fbw.game.movement.PlayerMovements
import com.github.onlaait.fbw.game.obj.Doll
import com.github.onlaait.fbw.system.PlayerData
import net.minestom.server.entity.Player
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.network.player.GameProfile
import net.minestom.server.network.player.PlayerConnection

class FPlayer(playerConnection: PlayerConnection, gameProfile: GameProfile) : Player(playerConnection, gameProfile) {

    val data: PlayerData by lazy { PlayerData.load(this) }

    var brand: String? = null

    val mouseInputs: PlayerMouseInputs = PlayerMouseInputs()

    var movement: PlayerMovements = PlayerMovements(this).apply { apply(DefaultMovement()) }

    var doll: Doll? = null

    fun changeMovementSpeed(value: Float) {
        getAttribute(Attribute.MOVEMENT_SPEED).baseValue = value.toDouble()
        fieldViewModifier = value
    }

    fun spectate(doll: Doll) {
    }

}
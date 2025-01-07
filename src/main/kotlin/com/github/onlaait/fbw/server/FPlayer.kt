package com.github.onlaait.fbw.server

import com.github.onlaait.fbw.game.movement.DefaultMovement
import com.github.onlaait.fbw.game.movement.PlayerMovements
import com.github.onlaait.fbw.game.obj.Doll
import com.github.onlaait.fbw.system.PlayerData
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.network.player.GameProfile
import net.minestom.server.network.player.PlayerConnection
import java.net.InetSocketAddress

class FPlayer(playerConnection: PlayerConnection, gameProfile: GameProfile) : Player(playerConnection, gameProfile) {

    val data: PlayerData by lazy { PlayerData.load(this) }

    val ipAddress: String
        get() = (playerConnection.remoteAddress as InetSocketAddress).address.hostAddress

    var brand: String? = null

    val mouseInputs: PlayerMouseInputs = PlayerMouseInputs()

    var movement: PlayerMovements = PlayerMovements(this).apply { apply(DefaultMovement()) }

    var doll: Doll? = null

    var spectating: Entity? = null

    fun isSpectating(): Boolean = spectating != null

    override fun spectate(entity: Entity) {
        super.spectate(entity)
        if (entity != this) spectating = entity
    }

    override fun stopSpectating() {
        spectating = null
        super.stopSpectating()
    }

    fun setMovementSpeed(value: Float) {
        getAttribute(Attribute.MOVEMENT_SPEED).baseValue = value.toDouble()
        fieldViewModifier = value
    }

    fun getPov(): Pos = position.withY { it + eyeHeight }

    fun spectate(doll: Doll) {
    }

}
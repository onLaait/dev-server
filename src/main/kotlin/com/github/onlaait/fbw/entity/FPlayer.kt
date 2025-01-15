package com.github.onlaait.fbw.entity

import com.github.onlaait.fbw.game.movement.DefaultMovement
import com.github.onlaait.fbw.game.movement.PlayerMovements
import com.github.onlaait.fbw.game.obj.Doll
import com.github.onlaait.fbw.math.minus
import com.github.onlaait.fbw.math.times
import com.github.onlaait.fbw.server.Schedule
import com.github.onlaait.fbw.server.Server
import com.github.onlaait.fbw.system.PlayerData
import com.github.onlaait.fbw.utils.PlayerMouseInputs
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.item.component.HeadProfile
import net.minestom.server.network.player.GameProfile
import net.minestom.server.network.player.PlayerConnection
import net.minestom.server.timer.ExecutionType
import java.net.InetSocketAddress

class FPlayer(playerConnection: PlayerConnection, gameProfile: GameProfile) : Player(playerConnection, gameProfile) {

    val data: PlayerData by lazy { PlayerData.load(this) }

    var isSlim: Boolean = false

    var headProfile = HeadProfile.EMPTY

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

    fun getPov(): Pos =
        position.withY {
            var y = it + eyeHeight
            if (vehicle != null) y -= 0.6
            y
        }

    fun getRealVelocity(): Vec = (position - previousPosition).asVec() * (19.0 * Server.CLIENT_2_SERVER_TICKS)

    fun spectate(doll: Doll) {
    }

    override fun remove() {
        super.remove()
        doll?.remove()
    }

    override fun setSneaking(sneaking: Boolean) {
        super.setSneaking(sneaking)
        doll?.setSneaking(sneaking)
    }

    private var lastPosition: Pos = position

    init {
        scheduler().buildTask {
            if (position == lastPosition) {
                previousPosition = position
            }
            lastPosition = position
        }.executionType(ExecutionType.TICK_END).repeat(Schedule.NEXT_CLIENT_TICK).schedule()
    }
}
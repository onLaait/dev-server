package com.github.laaitq.fbw

import com.github.laaitq.fbw.system.PlayerData
import net.minestom.server.entity.Player
import net.minestom.server.network.player.PlayerConnection
import java.util.*

class PlayerP(uuid: UUID, username: String, playerConnection: PlayerConnection) :
    Player(uuid, username, playerConnection) {

    var isOp: Boolean = false
    var brand: String? = null

    val data: PlayerData.PlayerData by lazy { PlayerData.read(this) }
}
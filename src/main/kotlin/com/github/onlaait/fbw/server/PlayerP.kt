package com.github.onlaait.fbw.server

import com.github.onlaait.fbw.system.PlayerData
import net.minestom.server.entity.Player
import net.minestom.server.network.player.PlayerConnection
import java.util.*

class PlayerP(uuid: UUID, username: String, playerConnection: PlayerConnection) :
    Player(uuid, username, playerConnection) {

    var brand: String? = null

    val data: PlayerData by lazy { PlayerData.read(this) }
}
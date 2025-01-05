package com.github.onlaait.fbw.game

import com.github.onlaait.fbw.game.mode.GameMode
import com.github.onlaait.fbw.game.obj.GameObj
import com.github.onlaait.fbw.game.utils.PlayerStats
import net.minestom.server.entity.Player
import java.util.*

object GameManager {

    var currentGame: GameMode? = null

    val players: MutableList<Player> = mutableListOf()

    val objs: MutableList<GameObj> = mutableListOf()

    var elapsedTime: Long = 0

    val stats: MutableMap<UUID, PlayerStats> = mutableMapOf()
}
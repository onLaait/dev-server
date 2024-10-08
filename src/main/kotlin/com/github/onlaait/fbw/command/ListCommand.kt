package com.github.onlaait.fbw.command

import com.github.onlaait.fbw.system.ServerProperties
import com.github.onlaait.fbw.utils.allPlayers
import com.github.onlaait.fbw.utils.infoMsg
import net.minestom.server.command.builder.Command

object ListCommand : Command("list") {
    init {
        val MSG = "최대 %s명 중 %s명이 접속 중입니다: %s"

        setDefaultExecutor { sender, _ ->
            val allPlayers = allPlayers
            sender.infoMsg(
                String.format(
                    MSG,
                    ServerProperties.MAX_PLAYERS,
                    allPlayers.size,
                    allPlayers.joinToString(", ") { it.username })
            )
        }
    }
}
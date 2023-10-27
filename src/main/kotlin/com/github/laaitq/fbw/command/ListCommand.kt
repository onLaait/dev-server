package com.github.laaitq.fbw.command

import com.github.laaitq.fbw.system.ServerProperties
import com.github.laaitq.fbw.utils.AudienceUtils.infoMsg
import com.github.laaitq.fbw.utils.PlayerUtils
import net.minestom.server.command.builder.Command

object ListCommand : Command("list") {
    init {
        val MSG = "최대 %s명 중 %s명이 접속 중입니다: %s"

        setDefaultExecutor { sender, _ ->
            val allPlayers = PlayerUtils.allPlayers
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
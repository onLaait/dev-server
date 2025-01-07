package com.github.onlaait.fbw.command

import com.github.onlaait.fbw.server.FPlayer
import com.github.onlaait.fbw.server.Logger
import com.github.onlaait.fbw.system.OpSystem.isOp
import com.github.onlaait.fbw.utils.infoMsg
import com.github.onlaait.fbw.utils.sendMsg
import com.github.onlaait.fbw.utils.warnMsg
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity

object InfoCommand : Command("info") {
    init {
        val MSG_PLAYER_NOTFOUND = "플레이어를 찾을 수 없습니다."

        setCondition { sender, _ -> sender.isOp }

        val argPlayer = ArgumentEntity("플레이어")
            .onlyPlayers(true)
            .singleEntity(true)
        argPlayer.setCallback { _, _ -> // Doesn't work
            Logger.debug { "Command argument callback works!!!" }
        }

        setDefaultExecutor { sender, context ->
            sender.sendMsg(usage("${context.commandName} <플레이어>"))
        }

        addSyntax({ sender, context ->
            val player = context[argPlayer].findFirstPlayer(sender) as FPlayer?
            if (player == null) {
                sender.warnMsg(MSG_PLAYER_NOTFOUND)
                return@addSyntax
            }
            val pos = player.position
            sender.infoMsg(
                "<${player.username}의 정보>",
                "UUID: ${player.uuid}",
                "IP: ${player.ipAddress}",
                "Ping: ${player.latency} ms",
                "접속 주소: ${player.playerConnection.serverAddress}",
                "클라이언트 유형: ${player.brand}",
                "언어: ${player.locale}(${player.locale?.displayLanguage})",
                "시야 거리: ${player.settings.viewDistance}",
                "위치: [x: ${String.format("%.2f", pos.x)}, y: ${String.format("%.2f", pos.y)}, z: ${String.format("%.2f", pos.z)}, yaw: ${String.format("%.2f", pos.yaw)}, pitch: ${String.format("%.2f", pos.pitch)}]",
            )
        }, argPlayer)
    }
}
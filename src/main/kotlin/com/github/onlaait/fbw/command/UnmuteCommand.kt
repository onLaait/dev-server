package com.github.onlaait.fbw.command

import com.github.onlaait.fbw.system.MuteSystem.isMuted
import com.github.onlaait.fbw.system.OpSystem.isOp
import com.github.onlaait.fbw.utils.alertMsg
import com.github.onlaait.fbw.utils.data
import com.github.onlaait.fbw.utils.sendMsg
import com.github.onlaait.fbw.utils.warnMsg
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity

object UnmuteCommand : Command("unmute") {
    init {
        val MSG_SUCCESS = "%s의 채팅을 활성화했습니다."
        val MSG_FAILED = "해당 플레이어는 이미 채팅이 활성화되어 있습니다."
        val MSG_PLAYER_NOTFOUND = "플레이어를 찾을 수 없습니다."

        setCondition { sender, _ -> sender.isOp }

        setDefaultExecutor { sender, context ->
            sender.sendMsg(usage("${context.commandName} <대상>"))
        }

        val argPlayer = ArgumentEntity("플레이어")
            .onlyPlayers(true)
            .singleEntity(true)

        addSyntax({ sender, context ->
            val player = context[argPlayer].findFirstPlayer(sender)
            if (player == null) {
                sender.warnMsg(MSG_PLAYER_NOTFOUND)
                return@addSyntax
            }
            if (!player.isMuted()) {
                sender.warnMsg(MSG_FAILED)
                return@addSyntax
            }
            player.data.muteTime = null
            sender.alertMsg(String.format(MSG_SUCCESS, player.username))
        }, argPlayer)
    }
}
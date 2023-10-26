package com.github.laaitq.fbw.command

import com.github.laaitq.fbw.system.MuteSystem.isMuted
import com.github.laaitq.fbw.system.OpSystem.isOp
import com.github.laaitq.fbw.utils.AudienceUtils.alertMsg
import com.github.laaitq.fbw.utils.AudienceUtils.sendMsg
import com.github.laaitq.fbw.utils.AudienceUtils.warnMsg
import com.github.laaitq.fbw.utils.CommandUtils.usage
import com.github.laaitq.fbw.utils.PlayerUtils.data
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
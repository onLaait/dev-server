package com.github.onlaait.fbw.command

import com.github.onlaait.fbw.command.argument.ArgumentText
import com.github.onlaait.fbw.system.OpSystem.isOp
import com.github.onlaait.fbw.utils.AudienceUtils.alertMsg
import com.github.onlaait.fbw.utils.AudienceUtils.sendMsg
import com.github.onlaait.fbw.utils.AudienceUtils.warnMsg
import com.github.onlaait.fbw.utils.CommandUtils
import net.kyori.adventure.text.Component
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import net.minestom.server.entity.Player

object KickCommand : Command("kick") {
    init {
        val MSG_SUCCESS = "%s을(를) 서버에서 추방했습니다."
        val MSG_SUCCESS_REASON = "%s을(를) 서버에서 추방했습니다. (사유: %s)"
        val MSG_PLAYER_NOTFOUND = "플레이어를 찾을 수 없습니다."

        setCondition { sender, _ -> sender.isOp }

        setDefaultExecutor { sender, context ->
            sender.sendMsg(CommandUtils.usage("${context.commandName} <대상> [사유]"))
        }

        val argPlayer = ArgumentEntity("플레이어")
            .onlyPlayers(true)
        val argReason = ArgumentText("사유")

        fun kickTask(sender: CommandSender, context: CommandContext) {
            val players = context[argPlayer].find(sender).filterIsInstance<Player>()
            if (players.isEmpty()) {
                sender.warnMsg(MSG_PLAYER_NOTFOUND)
                return
            }
            val reason = context[argReason]
            val isReasonNull = (reason == null)
            val kickMsg = if (isReasonNull) {
                Component.translatable("multiplayer.disconnect.kicked")
            } else {
                Component.text(reason)
            }
            players.forEach { player ->
                player.kick(kickMsg)
                sender.alertMsg(
                    if (isReasonNull) {
                        String.format(MSG_SUCCESS, player.username)
                    } else {
                        String.format(MSG_SUCCESS_REASON, player.username, reason)
                    }
                )
            }
        }

        addSyntax({ sender, context ->
            kickTask(sender, context)
        }, argPlayer)

        addSyntax({ sender, context ->
            kickTask(sender, context)
        }, argPlayer, argReason)
    }
}
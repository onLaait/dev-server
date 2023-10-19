package com.github.laaitq.fbw.command

import com.github.laaitq.fbw.system.OpSystem.isOp
import com.github.laaitq.fbw.utils.AudienceUtils.alertMsg
import com.github.laaitq.fbw.utils.AudienceUtils.sendMsg
import com.github.laaitq.fbw.utils.AudienceUtils.warnMsg
import com.github.laaitq.fbw.utils.CommandUtils
import net.kyori.adventure.text.Component
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentString
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import net.minestom.server.entity.Player

object KickCommand : Command("kick") {
    init {
        setCondition { sender, _ -> sender.isOp }

        setDefaultExecutor { sender, context ->
            sender.sendMsg(CommandUtils.usage("${context.commandName} <플레이어> [사유]"))
        }

        val argPlayer = ArgumentEntity("플레이어")
            .onlyPlayers(true)
        val argReason = ArgumentString("사유")
            .setDefaultValue("")

        addSyntax({ sender, context ->
            val entities = context[argPlayer].find(sender)
            if (entities.size == 0) {
                sender.warnMsg("유효한 플레이어를 입력하십시오.")
                return@addSyntax
            } else {
                val kickMsg: Component
                val part: String
                if (context[argReason].isBlank()) {
                    kickMsg = Component.translatable("multiplayer.disconnect.kicked")
                    part = ""
                } else {
                    kickMsg = Component.text(context[argReason])
                    part = " (사유: ${context[argReason]})"
                }
                for (entity in entities) {
                    if (entity is Player) {
                        entity.kick(kickMsg)
                        sender.alertMsg("${entity.username}을(를) 서버에서 추방했습니다." + part)
                    }
                }
            }
        }, argPlayer, argReason)
    }
}
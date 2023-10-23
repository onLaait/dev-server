package com.github.laaitq.fbw.command

import com.github.laaitq.fbw.command.argument.ArgumentText
import com.github.laaitq.fbw.system.OpSystem.isOp
import com.github.laaitq.fbw.utils.AudienceUtils.broadcast
import com.github.laaitq.fbw.utils.AudienceUtils.sendMsg
import com.github.laaitq.fbw.utils.CommandUtils.usage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.minestom.server.command.builder.Command

object BroadcastCommand : Command("broadcast", "say") {
    init {
        setCondition { sender, _ -> sender.isOp }

        setDefaultExecutor { sender, context ->
            sender.sendMsg(usage("${context.commandName} [!] <메시지>"))
        }

        val argMessage = ArgumentText("메시지")

        addSyntax({ sender, context ->
            val message = context[argMessage]
            broadcast(LegacyComponentSerializer.legacyAmpersand().deserialize(if (message.startsWith('!')) message.substring(1).trimStart() else "&e[공지] $message"))
        }, argMessage)
    }
}
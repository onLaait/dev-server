package com.github.onlaait.fbw.command

import com.github.onlaait.fbw.command.argument.ArgumentText
import com.github.onlaait.fbw.system.OpSystem.isOp
import com.github.onlaait.fbw.utils.broadcast
import com.github.onlaait.fbw.utils.sendMsg
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.minestom.server.command.builder.Command

object BroadcastCommand : Command("broadcast", "say") {
    init {
        setCondition { sender, _ -> sender.isOp }

        setDefaultExecutor { sender, context ->
            sender.sendMsg(usage("${context.commandName} [!] <메시지>"))
        }

        val argMessage = ArgumentText("메시지")

        addSyntax({ _, context ->
            val message = context[argMessage]
            broadcast(LegacyComponentSerializer.legacyAmpersand().deserialize(if (message.startsWith('!')) message.substring(1).trimStart() else "&e[공지] $message"))
        }, argMessage)
    }
}
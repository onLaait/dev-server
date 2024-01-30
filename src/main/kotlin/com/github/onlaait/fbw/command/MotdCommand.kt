package com.github.onlaait.fbw.command

import com.github.onlaait.fbw.command.CommandUtils.usage
import com.github.onlaait.fbw.command.argument.ArgumentText
import com.github.onlaait.fbw.system.OpSystem.isOp
import com.github.onlaait.fbw.system.ServerProperties
import com.github.onlaait.fbw.utils.AudienceUtils.alertMsg
import com.github.onlaait.fbw.utils.AudienceUtils.infoMsg
import com.github.onlaait.fbw.utils.AudienceUtils.sendMsg
import com.github.onlaait.fbw.utils.ServerUtils
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentLiteral

object MotdCommand : Command("motd") {
    init {
        setCondition { sender, _ -> sender.isOp }

        setDefaultExecutor { sender, context ->
            sender.sendMsg(usage(
                "${context.commandName} get",
                "${context.commandName} set <메시지>"
            ))
        }

        val argGet = ArgumentLiteral("get")
        val argSet = ArgumentLiteral("set")
        val argMessage = ArgumentText("메시지")

        addSyntax({ sender, _ ->
            sender.infoMsg(Component.text("MOTD: ").append(ServerUtils.responseData.description.colorIfAbsent(NamedTextColor.GRAY)))
        }, argGet)

        addSyntax({ sender, context ->
            val message = context[argMessage]
            val newMotd = LegacyComponentSerializer.legacyAmpersand().deserialize(message)
            ServerUtils.responseData.description = newMotd
            ServerProperties.MOTD = LegacyComponentSerializer.legacySection().serialize(newMotd)
            sender.alertMsg(Component.text("서버 MOTD를 \"").append(newMotd.colorIfAbsent(NamedTextColor.GRAY)).append(Component.text("\"(으)로 설정했습니다.")))
        }, argSet, argMessage)
    }
}
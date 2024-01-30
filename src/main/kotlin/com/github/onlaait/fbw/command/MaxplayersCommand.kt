package com.github.onlaait.fbw.command

import com.github.onlaait.fbw.command.CommandUtils.usage
import com.github.onlaait.fbw.system.OpSystem.isOp
import com.github.onlaait.fbw.system.ServerProperties
import com.github.onlaait.fbw.system.ServerStatus.sendTabList
import com.github.onlaait.fbw.utils.AudienceUtils.alertMsg
import com.github.onlaait.fbw.utils.AudienceUtils.sendMsg
import com.github.onlaait.fbw.utils.AudienceUtils.warnMsg
import com.github.onlaait.fbw.utils.ServerUtils
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.number.ArgumentInteger

object MaxplayersCommand : Command("maxplayers") {
    init {
        val MSG_SUCCESS = "최대 플레이어 수를 %s(으)로 설정했습니다."
        val MSG_FAILED = "최대 플레이어 수가 이미 해당 값입니다."

        setCondition { sender, _ -> sender.isOp }

        setDefaultExecutor { sender, context ->
            sender.sendMsg(usage("${context.commandName} <숫자>"))
        }

        val argInt = ArgumentInteger("숫자")
            .min(0)

        addSyntax({ sender, context ->
            val value = context[argInt]
            if (ServerProperties.MAX_PLAYERS == value) {
                sender.warnMsg(MSG_FAILED)
                return@addSyntax
            }
            ServerProperties.MAX_PLAYERS = value
            ServerUtils.responseData.maxPlayer = value
            Audiences.players().sendTabList()
            sender.alertMsg(String.format(MSG_SUCCESS, value))
        }, argInt)
    }
}
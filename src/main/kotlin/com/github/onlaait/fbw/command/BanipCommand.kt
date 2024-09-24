package com.github.onlaait.fbw.command

import com.github.onlaait.fbw.command.argument.ArgumentText
import com.github.onlaait.fbw.command.argument.ArgumentUsername
import com.github.onlaait.fbw.system.BanSystem.banIp
import com.github.onlaait.fbw.system.OpSystem.isOp
import com.github.onlaait.fbw.utils.*
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext

object BanipCommand : Command("ban-ip") {
    init {
        val MSG_SUCCESS = "IP %s을(를) 서버에서 차단했습니다."
        val MSG_SUCCESS_REASON = "IP %s을(를) 서버에서 차단했습니다. (사유: %s)"
        val MSG_INFO = "이 차단은 플레이어 %s명에게 영향을 줍니다: %s"
        val MSG_FAILED = "해당 IP는 이미 차단되어 있습니다."
        val MSG_INVALID = "잘못된 IP 주소이거나 알 수 없는 플레이어입니다."

        setCondition { sender, _ -> sender.isOp }

        setDefaultExecutor { sender, context ->
            sender.sendMsg(usage("${context.commandName} <대상> [사유]"))
        }

        val argTarget = ArgumentUsername("대상")
            .strict(false)
        val argReason = ArgumentText("사유")

        fun banipTask(sender: CommandSender, context: CommandContext) {
            val target = context[argTarget]
            val address = if (target.contains('.')) {
                if (!target.isIPv4Address()) {
                    sender.warnMsg(MSG_INVALID)
                    return
                }
                target
            } else {
                val player = allPlayers.find { it.username.equals(target, ignoreCase = true) }
                if (player == null) {
                    sender.warnMsg(MSG_INVALID)
                    return
                }
                player.ipAddress
            }
            val reason = context[argReason]
            val banned = banIp(address, reason)
            if (banned == null) {
                sender.warnMsg(MSG_FAILED)
                return
            }
            val msg = mutableListOf(if (reason == null) {
                String.format(MSG_SUCCESS, address)
            } else {
                String.format(MSG_SUCCESS_REASON, address, reason)
            })
            if (banned.isNotEmpty()) {
                msg += String.format(MSG_INFO, banned.size, banned.joinToString(", ") { it.username })
            }
            sender.alertMsg(msg)
        }

        addSyntax({ sender, context ->
            banipTask(sender, context)
        }, argTarget)

        addSyntax({ sender, context ->
            banipTask(sender, context)
        }, argTarget, argReason)
    }
}
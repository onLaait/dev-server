package com.github.onlaait.fbw.command

import com.github.onlaait.fbw.system.BanSystem
import com.github.onlaait.fbw.system.OpSystem.isOp
import com.github.onlaait.fbw.utils.AudienceUtils.alertMsg
import com.github.onlaait.fbw.utils.AudienceUtils.sendMsg
import com.github.onlaait.fbw.utils.AudienceUtils.warnMsg
import com.github.onlaait.fbw.utils.CommandUtils.usage
import com.github.onlaait.fbw.utils.StringUtils
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentString
import net.minestom.server.command.builder.suggestion.SuggestionEntry

object PardonipCommand : Command("pardon-ip", "unban-ip") {
    init {
        val MSG_SUCCESS = "IP %s의 차단을 해제했습니다."
        val MSG_FAILED = "해당 IP는 차단되어 있지 않습니다."
        val MSG_INVALID = "잘못된 IP 주소입니다."

        setCondition { sender, _ -> sender.isOp }

        setDefaultExecutor { sender, context ->
            sender.sendMsg(usage("${context.commandName} <IP 주소>"))
        }

        val argIpAddress = ArgumentString("IP 주소")
            .setSuggestionCallback { _, _, suggestion ->
                BanSystem.bannedIps.forEach { suggestion.addEntry(SuggestionEntry(it.ip)) }
            }

        addSyntax({ sender, context ->
            val ipAddress = context[argIpAddress]
            if (!StringUtils.isIPv4Address(ipAddress)) {
                sender.warnMsg(MSG_INVALID)
                return@addSyntax
            }
            val pardoned = BanSystem.pardonIp(ipAddress)
            if (!pardoned) {
                sender.warnMsg(MSG_FAILED)
                return@addSyntax
            }
            sender.alertMsg(String.format(MSG_SUCCESS, ipAddress))
        }, argIpAddress)
    }
}
package com.github.laaitq.fbw.command

import com.github.laaitq.fbw.system.BanSystem
import com.github.laaitq.fbw.system.OpSystem.isOp
import com.github.laaitq.fbw.utils.AudienceUtils.errorMsg
import com.github.laaitq.fbw.utils.AudienceUtils.infoMsg
import com.github.laaitq.fbw.utils.AudienceUtils.sendMsg
import com.github.laaitq.fbw.utils.CommandUtils.usage
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentLiteral

object BanlistCommand : Command("banlist") {
    init {
        val MSG_LIST = "차단 목록 (%s): %s"
        val MSG_NONE = "차단 목록이 비었습니다."

        setCondition { sender, _ -> sender.isOp }

        setDefaultExecutor { sender, context ->
            sender.sendMsg(usage("${context.commandName} [ips|players]"))
        }

        val argPlayers = ArgumentLiteral("players")
        val argIps = ArgumentLiteral("ips")

        fun banlist(sender: CommandSender, list: List<String>) {
            if (list.isEmpty()) {
                sender.errorMsg(MSG_NONE)
                return
            }
            sender.infoMsg(String.format(MSG_LIST, list.size, list.joinToString(", ")))
        }

        addSyntax({ sender, _ ->
            banlist(sender, BanSystem.bannedPlayers.map { it.name } + BanSystem.bannedIps.map { it.ip })
        })

        addSyntax({ sender, _ ->
            banlist(sender, BanSystem.bannedPlayers.map { it.name })
        }, argPlayers)

        addSyntax({ sender, _ ->
            banlist(sender, BanSystem.bannedIps.map { it.ip })
        }, argIps)
    }
}
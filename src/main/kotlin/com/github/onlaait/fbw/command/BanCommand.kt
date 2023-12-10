package com.github.onlaait.fbw.command

import com.github.onlaait.fbw.command.argument.ArgumentText
import com.github.onlaait.fbw.system.BanSystem
import com.github.onlaait.fbw.system.BanSystem.ban
import com.github.onlaait.fbw.system.OpSystem.isOp
import com.github.onlaait.fbw.utils.AudienceUtils.alertMsg
import com.github.onlaait.fbw.utils.AudienceUtils.sendMsg
import com.github.onlaait.fbw.utils.AudienceUtils.warnMsg
import com.github.onlaait.fbw.utils.CommandUtils.usage
import com.github.onlaait.fbw.utils.StringUtils.toUUID
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import net.minestom.server.entity.Player
import net.minestom.server.utils.mojang.MojangUtils
import kotlin.concurrent.thread

object BanCommand : Command("ban") {
    init {
        val MSG_SUCCESS = "%s을(를) 서버에서 차단했습니다."
        val MSG_SUCCESS_REASON = "%s을(를) 서버에서 차단했습니다. (사유: %s)"
        val MSG_FAILED = "해당 플레이어는 이미 차단되어 있습니다."
        val MSG_PLAYER_NOTFOUND = "플레이어를 찾을 수 없습니다."
        val MSG_PLAYER_UNKNOWN = "해당 플레이어는 존재하지 않습니다."

        setCondition { sender, _ -> sender.isOp }

        setDefaultExecutor { sender, context ->
            sender.sendMsg(usage("${context.commandName} <대상> [사유]"))
        }

        val argPlayer = ArgumentEntity("플레이어")
            .onlyPlayers(true)
        val argReason = ArgumentText("사유")

        fun banTask(sender: CommandSender, context: CommandContext) {
            val players = context[argPlayer].find(sender).filterIsInstance<Player>()
            val reason = context[argReason]
            if (players.isNotEmpty()) {
                players.forEach { player ->
                    player.ban(reason)
                    sender.alertMsg(
                        if (reason == null) {
                            String.format(MSG_SUCCESS, player.username)
                        } else {
                            String.format(MSG_SUCCESS_REASON, player.username, reason)
                        }
                    )
                }
                return
            }
            if (context.getRaw(argPlayer)[0] == '@') {
                sender.warnMsg(MSG_PLAYER_NOTFOUND)
                return
            }
            thread {
                val user = MojangUtils.fromUsername(context.getRaw(argPlayer))
                if (user == null) {
                    sender.warnMsg(MSG_PLAYER_UNKNOWN)
                    return@thread
                }
                val uuid = user["id"].asString.toUUID()
                if (BanSystem.bannedPlayers.find { it.uuid == uuid } != null) {
                    sender.warnMsg(MSG_FAILED)
                    return@thread
                }
                val name = user["name"].asString
                BanSystem.run {
                    bannedPlayers.add(BanSystem.BannedPlayer(uuid, name, reason))
                    writePlayers()
                }
                if (reason == null) {
                    sender.alertMsg(String.format(MSG_SUCCESS, name))
                } else {
                    sender.alertMsg(String.format(MSG_SUCCESS_REASON, name, reason))
                }
            }
        }

        addSyntax({ sender, context ->
            banTask(sender, context)
        }, argPlayer)

        addSyntax({ sender, context ->
            banTask(sender, context)
        }, argPlayer, argReason)

    }
}
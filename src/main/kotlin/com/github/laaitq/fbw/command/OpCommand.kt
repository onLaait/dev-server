package com.github.laaitq.fbw.command

import com.github.laaitq.fbw.system.OpSystem
import com.github.laaitq.fbw.system.OpSystem.isOp
import com.github.laaitq.fbw.system.OpSystem.setOp
import com.github.laaitq.fbw.utils.AudienceUtils.alertMsg
import com.github.laaitq.fbw.utils.AudienceUtils.sendMsg
import com.github.laaitq.fbw.utils.AudienceUtils.warnMsg
import com.github.laaitq.fbw.utils.CommandUtils.usage
import com.github.laaitq.fbw.utils.PlayerUtils
import com.github.laaitq.fbw.utils.StringUtils.toUUID
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.entity.Player
import net.minestom.server.utils.mojang.MojangUtils
import kotlin.concurrent.thread

object OpCommand : Command("op") {
    init {
        val MSG_SUCCESS = "%s을(를) 서버 관리자로 임명했습니다."
        val MSG_FAILED = "해당 플레이어는 이미 관리자입니다."
        val MSG_PLAYER_UNKNOWN = "해당 플레이어는 존재하지 않습니다."

        setCondition { sender, _ -> sender.isOp }

        setDefaultExecutor { sender, context ->
            sender.sendMsg(usage("${context.commandName} <플레이어>"))
        }

        val argPlayer = ArgumentEntity("플레이어")
            .onlyPlayers(true)
            .setSuggestionCallback { _, _, suggestion ->
                PlayerUtils.allPlayers.filter { !it.isOp }.forEach { suggestion.addEntry(SuggestionEntry(it.username)) }
            }

        addSyntax({ sender, context ->
            val players = context[argPlayer].find(sender).filterIsInstance<Player>()
            if (players.isNotEmpty()) {
                var successOnce = false
                players.filter { it.setOp(true) }.forEach { player ->
                    successOnce = true
                    sender.alertMsg(String.format(MSG_SUCCESS, player.username))
                }
                if (!successOnce) sender.warnMsg(MSG_FAILED)
                return@addSyntax
            }
            thread {
                val user = MojangUtils.fromUsername(context.getRaw(argPlayer))
                if (user == null) {
                    sender.warnMsg(MSG_PLAYER_UNKNOWN)
                    return@thread
                }
                val uuid = user["id"].asString.toUUID()
                if (OpSystem.opPlayersData.find { it.uuid == uuid } != null) {
                    sender.warnMsg(MSG_FAILED)
                    return@thread
                }
                val name = user["name"].asString
                OpSystem.run {
                    opPlayersData.add(OpSystem.OpPlayer(uuid, name))
                    write()
                }
                sender.alertMsg(String.format(MSG_SUCCESS, name))
            }
        }, argPlayer)
    }
}
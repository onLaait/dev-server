package com.github.onlaait.fbw.command

import com.github.onlaait.fbw.command.CommandUtils.usage
import com.github.onlaait.fbw.system.OpSystem
import com.github.onlaait.fbw.system.OpSystem.isOp
import com.github.onlaait.fbw.system.OpSystem.setOp
import com.github.onlaait.fbw.utils.AudienceUtils.alertMsg
import com.github.onlaait.fbw.utils.AudienceUtils.sendMsg
import com.github.onlaait.fbw.utils.AudienceUtils.warnMsg
import com.github.onlaait.fbw.utils.IterableUtils.removeSingle
import com.github.onlaait.fbw.utils.StringUtils.toUUID
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.entity.Player
import net.minestom.server.utils.mojang.MojangUtils
import kotlin.concurrent.thread

object DeopCommand : Command("deop") {
    init {
        val MSG_SUCCESS = "%s은(는) 더이상 서버 관리자가 아닙니다."
        val MSG_FAILED = "해당 플레이어는 관리자가 아닙니다."
        val MSG_PLAYER_UNKNOWN = "해당 플레이어는 존재하지 않습니다."

        setCondition { sender, _ -> sender.isOp }

        setDefaultExecutor { sender, context ->
            sender.sendMsg(usage("${context.commandName} <대상>"))
        }

        val argTarget = ArgumentEntity("대상")
            .onlyPlayers(true)
            .setSuggestionCallback { _, _, suggestion ->
                OpSystem.opPlayers.forEach { suggestion.addEntry(SuggestionEntry(it.name)) }
            }

        addSyntax({ sender, context ->
            val players = context[argTarget].find(sender).filterIsInstance<Player>()
            if (players.isNotEmpty()) {
                var successOnce = false
                players.filter { it.setOp(false) }.forEach { player ->
                    successOnce = true
                    sender.alertMsg(String.format(MSG_SUCCESS, player.username))
                }
                if (!successOnce) sender.warnMsg(MSG_FAILED)
                return@addSyntax
            }
            thread {
                val user = MojangUtils.fromUsername(context.getRaw(argTarget))
                if (user == null) {
                    sender.warnMsg(MSG_PLAYER_UNKNOWN)
                    return@thread
                }
                val removed = OpSystem.opPlayers.removeSingle { it.uuid == user["id"].asString.toUUID() }
                if (!removed) {
                    sender.warnMsg(MSG_FAILED)
                    return@thread
                }
                OpSystem.write()
                sender.alertMsg(String.format(MSG_SUCCESS, user["name"].asString))
            }
        }, argTarget)
    }
}
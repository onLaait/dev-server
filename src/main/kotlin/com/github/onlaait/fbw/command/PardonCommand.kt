package com.github.onlaait.fbw.command

import com.github.onlaait.fbw.system.BanSystem
import com.github.onlaait.fbw.system.OpSystem.isOp
import com.github.onlaait.fbw.utils.alertMsg
import com.github.onlaait.fbw.utils.sendMsg
import com.github.onlaait.fbw.utils.toUUID
import com.github.onlaait.fbw.utils.warnMsg
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentString
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.utils.mojang.MojangUtils
import kotlin.concurrent.thread

object PardonCommand : Command("pardon", "unban") {
    init {
        val MSG_SUCCESS = "%s의 차단을 해제했습니다."
        val MSG_FAILED = "해당 플레이어는 차단되어 있지 않습니다."
        val MSG_PLAYER_NOTFOUND = "플레이어를 찾을 수 없습니다."
        val MSG_PLAYER_UNKNOWN = "해당 플레이어는 존재하지 않습니다."

        setCondition { sender, _ -> sender.isOp }

        setDefaultExecutor { sender, context ->
            sender.sendMsg(usage("${context.commandName} <대상> [사유]"))
        }

        val argTarget = ArgumentString("대상")
            .setSuggestionCallback { _, _, suggestion ->
                BanSystem.bannedPlayers.forEach { suggestion.addEntry(SuggestionEntry(it.name)) }
            }

        addSyntax({ sender, context ->
            thread {
                run {
                    val find = BanSystem.bannedPlayers.find { it.name == context[argTarget] }
                        ?: BanSystem.bannedPlayers.find { it.name.equals(context[argTarget], ignoreCase = true) }
                    if (find != null) {
                        if (context.getRaw(argTarget)[0] == '@') {
                            sender.warnMsg(MSG_PLAYER_NOTFOUND)
                            return@thread
                        }
                        val currentName = MojangUtils.fromUuid(find.uuid.toString())?.get("name")?.asString
                        BanSystem.run {
                            bannedPlayers.remove(find)
                            storePlayers()
                        }
                        sender.alertMsg(String.format(MSG_SUCCESS, currentName))
                        return@thread
                    }
                }
                val user = MojangUtils.fromUsername(context[argTarget])
                if (user == null) {
                    sender.warnMsg(MSG_PLAYER_UNKNOWN)
                    return@thread
                }
                val pardoned = BanSystem.pardon(user["id"].asString.toUUID())
                if (!pardoned) {
                    sender.warnMsg(MSG_FAILED)
                    return@thread
                }
                sender.alertMsg(String.format(MSG_SUCCESS, user["name"].asString))
            }
        }, argTarget)
    }
}
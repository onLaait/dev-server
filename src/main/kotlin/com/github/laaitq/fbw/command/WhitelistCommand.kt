package com.github.laaitq.fbw.command

import com.github.laaitq.fbw.system.OpSystem.isOp
import com.github.laaitq.fbw.system.ServerProperties
import com.github.laaitq.fbw.system.Whitelist
import com.github.laaitq.fbw.system.Whitelist.isWhitelisted
import com.github.laaitq.fbw.system.Whitelist.kickIfNotWhitelisted
import com.github.laaitq.fbw.utils.AudienceUtils.alertMsg
import com.github.laaitq.fbw.utils.AudienceUtils.infoMsg
import com.github.laaitq.fbw.utils.AudienceUtils.sendMsg
import com.github.laaitq.fbw.utils.AudienceUtils.warnMsg
import com.github.laaitq.fbw.utils.CommandUtils.usage
import com.github.laaitq.fbw.utils.PlayerUtils.allPlayers
import com.github.laaitq.fbw.utils.StringUtils.toUUID
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentLiteral
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.entity.Player
import net.minestom.server.utils.mojang.MojangUtils
import kotlin.concurrent.thread

object WhitelistCommand : Command("whitelist") {
    init {
        val MSG_ENABLED = "화이트리스트가 켜졌습니다."
        val MSG_DISABLED = "화이트리스트가 꺼졌습니다."
        val MSG_ALREADY_ON = "화이트리스트가 이미 켜져 있습니다."
        val MSG_ALREADY_OFF = "화이트리스트가 이미 꺼져 있습니다."
        val MSG_ADD_SUCCESS = "%s을(를) 화이트리스트에 추가했습니다."
        val MSG_ADD_FAILED = "플레이어가 이미 화이트리스트에 있습니다."
        val MSG_REMOVE_SUCCESS = "%s을(를) 화이트리스트에서 제거했습니다."
        val MSG_REMOVE_FAILED = "플레이어가 화이트리스트에 없습니다."
        val MSG_LIST = "화이트리스트에 플레이어가 %s명 있습니다: %s"
        val MSG_NONE = "화이트리스트에 플레이어가 없습니다."
        val MSG_RELOADED = "화이트리스트를 새로 고쳤습니다."
        val MSG_PLAYER_NOTFOUND = "플레이어를 찾을 수 없습니다."
        val MSG_PLAYER_UNKNOWN = "해당 플레이어는 존재하지 않습니다."

        setCondition { sender, _ -> sender.isOp }

        setDefaultExecutor { sender, context ->
            sender.sendMsg(
                usage(
                    "${context.commandName} on",
                    "${context.commandName} off",
                    "${context.commandName} add <대상>",
                    "${context.commandName} remove <대상>",
                    "${context.commandName} list",
                    "${context.commandName} reload"
                )
            )
        }

        val argOn = ArgumentLiteral("on")
        val argOff = ArgumentLiteral("off")
        val argAdd = ArgumentLiteral("add")
        val argRemove = ArgumentLiteral("remove")
        val argList = ArgumentLiteral("list")
        val argReload = ArgumentLiteral("reload")

        val argAddPlayer = ArgumentEntity("플레이어")
            .onlyPlayers(true)
            .setSuggestionCallback { _, _, suggestion ->
                allPlayers.filter { !it.isWhitelisted }.forEach { suggestion.addEntry(SuggestionEntry(it.username)) }
            }
        val argRemovePlayer = ArgumentEntity("플레이어")
            .onlyPlayers(true)
            .setSuggestionCallback { _, _, suggestion ->
                Whitelist.whitelistedPlayers.forEach { suggestion.addEntry(SuggestionEntry(it.name)) }
            }

        addSyntax({ sender, _ ->
            if (!ServerProperties.WHITE_LIST) {
                Whitelist.enable()
                sender.alertMsg(MSG_ENABLED)
                return@addSyntax
            }
            sender.warnMsg(MSG_ALREADY_ON)
        }, argOn)

        addSyntax({ sender, _ ->
            if (ServerProperties.WHITE_LIST) {
                Whitelist.disable()
                sender.alertMsg(MSG_DISABLED)
                return@addSyntax
            }
            sender.warnMsg(MSG_ALREADY_OFF)
        }, argOff)

        addSyntax({ sender, context ->
            val players = context[argAddPlayer].find(sender).filterIsInstance<Player>()
            if (players.isNotEmpty()) {
                var successOnce = false
                players.filter { Whitelist.add(it) }.forEach { player ->
                    successOnce = true
                    sender.alertMsg(String.format(MSG_ADD_SUCCESS, player.username))
                }
                if (!successOnce) sender.warnMsg(MSG_ADD_FAILED)
                return@addSyntax
            }
            if (context.getRaw(argAddPlayer)[0] == '@') {
                sender.warnMsg(MSG_PLAYER_NOTFOUND)
                return@addSyntax
            }
            thread {
                val user = MojangUtils.fromUsername(context.getRaw(argAddPlayer))
                if (user == null) {
                    sender.warnMsg(MSG_PLAYER_UNKNOWN)
                    return@thread
                }
                val uuid = user["id"].asString.toUUID()
                val name = user["name"].asString
                val added = Whitelist.add(uuid, name)
                if (!added) {
                    sender.warnMsg(MSG_ADD_FAILED)
                    return@thread
                }
                sender.alertMsg(String.format(MSG_ADD_SUCCESS, name))
            }
        }, argAdd, argAddPlayer)

        addSyntax({ sender, context ->
            val players = context[argRemovePlayer].find(sender).filterIsInstance<Player>()
            var successOnce = false
            if (players.isNotEmpty()) {
                players.filter { Whitelist.remove(it) }.forEach { player ->
                    successOnce = true
                    sender.alertMsg(String.format(MSG_REMOVE_SUCCESS, player.username))
                }
                if (!successOnce) sender.warnMsg(MSG_REMOVE_FAILED)
                return@addSyntax
            }
            if (context.getRaw(argRemovePlayer)[0] == '@') {
                sender.warnMsg(MSG_PLAYER_NOTFOUND)
                return@addSyntax
            }
            thread {
                val user = MojangUtils.fromUsername(context.getRaw(argRemovePlayer))
                if (user == null) {
                    sender.warnMsg(MSG_PLAYER_UNKNOWN)
                    return@thread
                }
                val uuid = user["id"].asString.toUUID()
                val name = user["name"].asString
                val find = Whitelist.whitelistedPlayers.run {
                    find { it.uuid == uuid }
                        ?: find { it.name == name }
                        ?: find { it.name.equals(name, ignoreCase = true) }
                }
                if (find == null) {
                    sender.warnMsg(MSG_REMOVE_FAILED)
                    return@thread
                }
                val currentName = MojangUtils.fromUuid(find.uuid.toString())?.get("name")?.asString
                Whitelist.whitelistedPlayers.remove(find)
                Whitelist.write()
                if (ServerProperties.WHITE_LIST && ServerProperties.ENFORCE_WHITELIST) {
                    allPlayers.find { it.uuid == find.uuid }?.kickIfNotWhitelisted()
                }
                sender.alertMsg(String.format(MSG_REMOVE_SUCCESS, currentName))
            }
        }, argRemove, argRemovePlayer)

        addSyntax({ sender, _ ->
            sender.infoMsg(
                if (Whitelist.whitelistedPlayers.size > 0) {
                    String.format(MSG_LIST, Whitelist.whitelistedPlayers.size, Whitelist.whitelistedPlayers.joinToString(", ") { it.name })
                } else {
                    MSG_NONE
                }
            )
        }, argList)

        addSyntax({ sender, _ ->
            Whitelist.read()
            Whitelist.write()
            sender.alertMsg(MSG_RELOADED)
            if (ServerProperties.WHITE_LIST && ServerProperties.ENFORCE_WHITELIST) {
                allPlayers.forEach { it.kickIfNotWhitelisted() }
            }
        }, argReload)
    }
}
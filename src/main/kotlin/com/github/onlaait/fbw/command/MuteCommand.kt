package com.github.onlaait.fbw.command

import com.github.onlaait.fbw.command.argument.ArgumentDuration
import com.github.onlaait.fbw.entity.FPlayer
import com.github.onlaait.fbw.system.OpSystem.isOp
import com.github.onlaait.fbw.utils.alertMsg
import com.github.onlaait.fbw.utils.formattedString
import com.github.onlaait.fbw.utils.sendMsg
import com.github.onlaait.fbw.utils.warnMsg
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object MuteCommand : Command("mute") {
    init {
        val MSG_SUCCESS = "%s의 채팅을 비활성화했습니다."
        val MSG_SUCCESS_DURATION = "%s의 채팅을 %s동안 비활성화했습니다."
        val MSG_DURATION_INVALID = "잘못된 기간입니다."
        val MSG_PLAYER_NOTFOUND = "플레이어를 찾을 수 없습니다."

        setCondition { sender, _ -> sender.isOp }

        setDefaultExecutor { sender, context ->
            sender.sendMsg(usage("${context.commandName} <대상> [시간]"))
        }

        val argPlayer = ArgumentEntity("플레이어")
            .onlyPlayers(true)
            .singleEntity(true)
        val argDuration = ArgumentDuration("기간")

        fun muteTask(sender: CommandSender, context: CommandContext) {
            val p = context[argPlayer].findFirstPlayer(sender) as FPlayer?
            if (p == null) {
                sender.warnMsg(MSG_PLAYER_NOTFOUND)
                return
            }
            val input = context[argDuration]
            if (input == null) {
                p.data.muteTime = -1L
                sender.alertMsg(String.format(MSG_SUCCESS, p.username))
                return
            }
            var duration = Duration.ZERO
            var chars = charArrayOf()
            var gotS = false
            var gotM = false
            var gotH = false
            var gotD = false
            try {
                input.forEach {
                    when (it) {
                        in '0'..'9' -> {
                            chars += it
                        }
                        's' -> {
                            if (gotS || chars.isEmpty()) {
                                sender.warnMsg(MSG_DURATION_INVALID)
                                return
                            }
                            gotS = true
                            duration += String(chars).toInt().seconds
                            chars = charArrayOf()
                        }
                        'm' -> {
                            if (gotM || chars.isEmpty()) {
                                sender.warnMsg(MSG_DURATION_INVALID)
                                return
                            }
                            gotM = true
                            duration += String(chars).toInt().minutes
                            chars = charArrayOf()
                        }
                        'h' -> {
                            if (gotH || chars.isEmpty()) {
                                sender.warnMsg(MSG_DURATION_INVALID)
                                return
                            }
                            gotH = true
                            duration += String(chars).toInt().hours
                            chars = charArrayOf()
                        }
                        'd' -> {
                            if (gotD || chars.isEmpty()) {
                                sender.warnMsg(MSG_DURATION_INVALID)
                                return
                            }
                            gotD = true
                            duration += String(chars).toInt().days
                            chars = charArrayOf()
                        }
                        else -> {
                            sender.warnMsg(MSG_DURATION_INVALID)
                            return
                        }
                    }
                }
            } catch (e: IllegalArgumentException) {
                sender.warnMsg(MSG_DURATION_INVALID)
                return
            }
            if (chars.isNotEmpty()) {
                duration += String(chars).toInt().seconds
            }
            p.data.muteTime = System.currentTimeMillis() + duration.inWholeMilliseconds
            sender.alertMsg(String.format(MSG_SUCCESS_DURATION, p.username, duration.formattedString()))
        }

        addSyntax({ sender, context ->
            muteTask(sender, context)
        }, argPlayer)

        addSyntax({ sender, context ->
            muteTask(sender, context)
        }, argPlayer, argDuration)
    }
}
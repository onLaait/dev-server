package com.github.laaitq.fbw.utils

import com.github.laaitq.fbw.system.OpSystem.isOp
import com.github.laaitq.fbw.utils.AudienceUtils.sendMsg
import com.github.laaitq.fbw.utils.TextUtils.YELLOW_QUESTION_MARK
import com.github.laaitq.fbw.utils.TextUtils.formatText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minestom.server.command.CommandSender
import net.minestom.server.entity.Player

object CommandUtils {
    private val messageNoPermission = formatText("${TextUtils.RED_EXCLAMATION_MARK}<white> 권한이 없습니다.")

    fun Component.plainText() = PlainTextComponentSerializer.plainText().serialize(this)

    fun warnIfNotOp(sender: CommandSender): Boolean {
        return if (sender is Player && !sender.isOp) {
            sender.sendMsg(messageNoPermission)
            true
        } else {
            false
        }
    }

    fun usage(vararg usage: String): Component {
        return Component.text().apply {
            for ((i, str) in usage.withIndex()) {
                it.append(YELLOW_QUESTION_MARK.append(formatText("사용법: /$str")))
                if (i != usage.lastIndex) it.appendNewline()
            }
        }.build()
    }
}
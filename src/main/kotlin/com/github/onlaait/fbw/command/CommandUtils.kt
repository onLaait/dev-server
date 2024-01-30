package com.github.onlaait.fbw.command

import com.github.onlaait.fbw.system.OpSystem.isOp
import com.github.onlaait.fbw.utils.AudienceUtils.sendMsg
import com.github.onlaait.fbw.utils.TextUtils
import com.github.onlaait.fbw.utils.TextUtils.YELLOW_QUESTION_MARK
import com.github.onlaait.fbw.utils.TextUtils.formatText
import net.kyori.adventure.text.Component
import net.minestom.server.command.CommandSender
import net.minestom.server.entity.Player

object CommandUtils {
    private val messageNoPermission = formatText("${TextUtils.RED_EXCLAMATION_MARK}<white> 권한이 없습니다.")

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
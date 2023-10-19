package com.github.laaitq.fbw.utils

import com.github.laaitq.fbw.system.Logger
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.command.ConsoleSender


object AudienceUtils {
    fun broadcast(message: Component) {
        Audiences.players().sendMsg(message)
        Logger.info(message)
    }

    fun Audience.sendMsg(message: String) {
        if (this is ConsoleSender) {
            Logger.info(message)
        } else {
            this.sendMessage(Component.text(message))
        }
    }

    fun Audience.sendMsg(messages: Array<String>) {
        if (this is ConsoleSender) {
            for (message in messages) Logger.info(message)
        } else {
            for (message in messages) this.sendMessage(Component.text(message))
        }
    }

    fun Audience.sendMsg(message: Component) {
        if (this is ConsoleSender) {
            Logger.info(message)
        } else {
            this.sendMessage(message)
        }
    }

    fun Audience.sendMsg(vararg messages: Component) {
        val message = Component.text().apply {
            for ((i, component) in messages.withIndex()) {
                it.append(component)
                if (i != messages.size) it.appendNewline()
            }
        }.build()

        if (this is ConsoleSender) {
            Logger.info(message)
        } else {
            this.sendMessage(message)
        }
    }

    fun Audience.sendMsg(message: ComponentLike) {
        val component = message.asComponent()
        if (this is ConsoleSender) {
            Logger.info(component)
        } else {
            this.sendMessage(component)
        }
    }

    private fun Audience.msg(prefix: Component, message: String) {
        sendMsg(prefix.append(Component.text(message)))
    }

    private fun Audience.msg(prefix: Component, messages: Array<String>) {
        for (message in messages) {
            sendMsg(prefix.append(Component.text(message)))
        }
    }

    private fun Audience.msg(prefix: Component, message: Component) {
        sendMsg(prefix.append(message))
    }

    private fun Audience.msg(prefix: Component, messages: Array<out Component>) {
        sendMsg(
            Component.text().apply {
                for ((i, component) in messages.withIndex()) {
                    it.append(prefix)
                    it.append(component)
                    if (i != messages.lastIndex) it.appendNewline()
                }
            }.build()
        )
    }

    private fun Audience.msg(prefix: Component, message: ComponentLike) {
        sendMsg(prefix.append(message))
    }

    fun Audience.infoMsg(message: String) {
        msg(TextUtils.YELLOW_QUESTION_MARK, message)
    }

    fun Audience.infoMsg(messages: Array<String>) {
        msg(TextUtils.YELLOW_QUESTION_MARK, messages)
    }

    fun Audience.infoMsg(message: Component) {
        msg(TextUtils.YELLOW_QUESTION_MARK, message)
    }

    fun Audience.infoMsg(vararg messages: Component) {
        msg(TextUtils.YELLOW_QUESTION_MARK, messages)
    }

    fun Audience.infoMsg(message: ComponentLike) {
        msg(TextUtils.YELLOW_QUESTION_MARK, message)
    }

    fun Audience.alertMsg(message: String) {
        msg(TextUtils.YELLOW_EXCLAMATION_MARK, message)
    }

    fun Audience.alertMsg(messages: Array<String>) {
        msg(TextUtils.YELLOW_EXCLAMATION_MARK, messages)
    }

    fun Audience.alertMsg(message: Component) {
        msg(TextUtils.YELLOW_EXCLAMATION_MARK, message)
    }

    fun Audience.alertMsg(vararg messages: Component) {
        msg(TextUtils.YELLOW_EXCLAMATION_MARK, messages)
    }

    fun Audience.alertMsg(message: ComponentLike) {
        msg(TextUtils.YELLOW_EXCLAMATION_MARK, message)
    }

    fun Audience.warnMsg(message: String) {
        msg(TextUtils.RED_EXCLAMATION_MARK, message)
    }

    fun Audience.warnMsg(messages: Array<String>) {
        msg(TextUtils.RED_EXCLAMATION_MARK, messages)
    }

    fun Audience.warnMsg(message: Component) {
        msg(TextUtils.RED_EXCLAMATION_MARK, message)
    }

    fun Audience.warnMsg(vararg messages: Component) {
        msg(TextUtils.RED_EXCLAMATION_MARK, messages)
    }

    fun Audience.warnMsg(message: ComponentLike) {
        msg(TextUtils.RED_EXCLAMATION_MARK, message)
    }

}
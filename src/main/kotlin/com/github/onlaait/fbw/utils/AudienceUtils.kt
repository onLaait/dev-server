package com.github.onlaait.fbw.utils

import com.github.onlaait.fbw.server.Logger
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.command.ConsoleSender

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

fun Audience.sendMsg(messages: Collection<String>, @Suppress("UNUSED_PARAMETER") dummyImplicit: Any? = null) {
    val joined = messages.joinToString("\n")
    if (this is ConsoleSender) {
        Logger.info(joined)
    } else {
        this.sendMessage(Component.text(joined))
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
        messages.forEachIndexed { i, component ->
            it.append(component)
            if (i != messages.size-1) it.appendNewline()
        }
    }.build()

    if (this is ConsoleSender) {
        Logger.info(message)
    } else {
        this.sendMessage(message)
    }
}

fun Audience.sendMsg(messages: Collection<Component>) {
    val message = Component.text().apply {
        messages.forEachIndexed { i, component ->
            it.append(component)
            if (i != messages.size-1) it.appendNewline()
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

private fun Audience.msg(prefix: Component, messages: Array<out String>) {
    sendMsg(messages.map { prefix.append(Component.text(it)) })
}

private fun Audience.msg(prefix: Component, messages: Collection<String>) {
    sendMsg(messages.map { prefix.append(Component.text(it)) })
}

private fun Audience.msg(prefix: Component, message: Component) {
    sendMsg(prefix.append(message))
}

private fun Audience.msg(prefix: Component, messages: Array<out Component>) {
    sendMsg(messages.map { prefix.append(it) })
}

private fun Audience.msg(prefix: Component, message: ComponentLike) {
    sendMsg(prefix.append(message))
}

fun Audience.infoMsg(message: String) {
    msg(YELLOW_QUESTION_MARK, message)
}

fun Audience.infoMsg(vararg messages: String) {
    msg(YELLOW_QUESTION_MARK, messages)
}

fun Audience.infoMsg(messages: Collection<String>) {
    msg(YELLOW_QUESTION_MARK, messages)
}

fun Audience.infoMsg(message: Component) {
    msg(YELLOW_QUESTION_MARK, message)
}

fun Audience.infoMsg(vararg messages: Component) {
    msg(YELLOW_QUESTION_MARK, messages)
}

fun Audience.infoMsg(message: ComponentLike) {
    msg(YELLOW_QUESTION_MARK, message)
}

fun Audience.alertMsg(message: String) {
    msg(YELLOW_EXCLAMATION_MARK, message)
}

fun Audience.alertMsg(vararg messages: String) {
    msg(YELLOW_EXCLAMATION_MARK, messages)
}

fun Audience.alertMsg(messages: Collection<String>) {
    msg(YELLOW_EXCLAMATION_MARK, messages)
}

fun Audience.alertMsg(message: Component) {
    msg(YELLOW_EXCLAMATION_MARK, message)
}

fun Audience.alertMsg(vararg messages: Component) {
    msg(YELLOW_EXCLAMATION_MARK, messages)
}

fun Audience.alertMsg(message: ComponentLike) {
    msg(YELLOW_EXCLAMATION_MARK, message)
}

fun Audience.warnMsg(message: String) {
    msg(RED_EXCLAMATION_MARK, message)
}

fun Audience.warnMsg(vararg messages: String) {
    msg(RED_EXCLAMATION_MARK, messages)
}

fun Audience.warnMsg(messages: Collection<String>) {
    msg(RED_EXCLAMATION_MARK, messages)
}

fun Audience.warnMsg(message: Component) {
    msg(RED_EXCLAMATION_MARK, message)
}

fun Audience.warnMsg(vararg messages: Component) {
    msg(RED_EXCLAMATION_MARK, messages)
}

fun Audience.warnMsg(message: ComponentLike) {
    msg(RED_EXCLAMATION_MARK, message)
}

fun Audience.errorMsg(message: String) {
    msg(RED_QUESTION_MARK, message)
}

fun Audience.errorMsg(vararg messages: String) {
    msg(RED_QUESTION_MARK, messages)
}

fun Audience.errorMsg(messages: Collection<String>) {
    msg(RED_QUESTION_MARK, messages)
}

fun Audience.errorMsg(message: Component) {
    msg(RED_QUESTION_MARK, message)
}

fun Audience.errorMsg(vararg messages: Component) {
    msg(RED_QUESTION_MARK, messages)
}

fun Audience.errorMsg(message: ComponentLike) {
    msg(RED_QUESTION_MARK, message)
}

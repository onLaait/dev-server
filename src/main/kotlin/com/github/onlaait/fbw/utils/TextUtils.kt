package com.github.onlaait.fbw.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

object TextUtils {
    val RED_EXCLAMATION_MARK = formatText("<red><bold>[!]</bold> ")
    val RED_QUESTION_MARK = formatText("<red><bold>[?]</bold> ")
    val YELLOW_EXCLAMATION_MARK = formatText("<yellow><bold>[!]</bold> ")
    val YELLOW_QUESTION_MARK = formatText("<yellow><bold>[?]</bold> ")

    fun formatText(str: String): Component = MiniMessage.miniMessage().deserialize(str)
}
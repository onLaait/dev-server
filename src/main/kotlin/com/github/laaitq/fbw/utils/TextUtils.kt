package com.github.laaitq.fbw.utils

import net.kyori.adventure.text.minimessage.MiniMessage

object TextUtils {
    val RED_EXCLAMATION_MARK = formatText("<red><bold>[!]</bold> ")
    val RED_QUESTION_MARK = formatText("<red><bold>[?]</bold> ")
    val YELLOW_EXCLAMATION_MARK = formatText("<yellow><bold>[!]</bold> ")
    val YELLOW_QUESTION_MARK = formatText("<yellow><bold>[?]</bold> ")

    fun formatText(str: String) = MiniMessage.miniMessage().deserialize(str)
}




/*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration.*
import java.util.regex.Pattern

object TextUtils {

    fun formatText(text: String): TextComponent {
        var group: String
        var length: Int
        var color = '0'
        var hex = ""
        var colorType = 0
        var styleObfuscated = false
        var styleBold = false
        var styleStrikethrough = false
        var styleUnderline = false
        var styleItalic = false
        var code: Char
        val matcher = Pattern.compile("(<#[0-9a-f]{6}>|&[0-9a-fk-or])").matcher(text)
        var component: Component
        val result = Component.text()
        var start: Int
        var end: Int
        var lastEnd = 0

        fun appendLastComponent(substring: String) {
            component = Component.text(substring)
            component = component.color(
                when (colorType) {
                    1 -> {
                        when (color) {
                            '0' -> BLACK
                            '1' -> DARK_BLUE
                            '2' -> DARK_GREEN
                            '3' -> DARK_AQUA
                            '4' -> DARK_RED
                            '5' -> DARK_PURPLE
                            '6' -> GOLD
                            '7' -> GRAY
                            '8' -> DARK_GRAY
                            '9' -> BLUE
                            'a' -> GREEN
                            'b' -> AQUA
                            'c' -> RED
                            'd' -> LIGHT_PURPLE
                            'e' -> YELLOW
                            'f' -> WHITE
                            else -> null
                        }
                    }

                    2 -> TextColor.fromHexString(hex)
                    else -> null
                }
            )
            if (styleObfuscated) component = component.decorate(OBFUSCATED)
            if (styleBold) component = component.decorate(BOLD)
            if (styleStrikethrough) component = component.decorate(STRIKETHROUGH)
            if (styleUnderline) component = component.decorate(UNDERLINED)
            if (styleItalic) component = component.decorate(ITALIC)
            result.append(component)
        }


        fun removeAllStyle() {
            styleObfuscated = false
            styleBold = false
            styleStrikethrough = false
            styleUnderline = false
            styleItalic = false
        }

        while (matcher.find()) {
            start = matcher.start()
            end = matcher.end()
            length = end - start
            if (lastEnd != start) {
                appendLastComponent(text.substring(lastEnd until start))
            }
            lastEnd = end
            group = matcher.group()
            if (length == 2) {
                code = group[1]
                when (code) {
                    'k' -> styleObfuscated = true
                    'l' -> styleBold = true
                    'm' -> styleStrikethrough = true
                    'n' -> styleUnderline = true
                    'o' -> styleItalic = true
                    'r' -> {
                        colorType = 0
                        removeAllStyle()
                    }

                    else -> {
                        colorType = 1
                        color = code
                        removeAllStyle()
                    }
                }
            } else {
                colorType = 2
                hex = group.substring(1..7)
                removeAllStyle()
            }
        }

        appendLastComponent(text.substring(lastEnd))
        return result.build()
    }

}*/

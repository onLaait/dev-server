package com.github.laaitq.fbw.utils

import com.github.laaitq.fbw.serializer.MessageFormatAsStringSerializer
import com.github.laaitq.fbw.system.Logger
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.TranslationRegistry
import java.text.MessageFormat
import java.util.*

object ComponentUtils {
    private val locale: Locale = Locale.getDefault().takeIf { it == Locale.KOREA } ?: Locale.US

    init {
        Logger.debug("Loading languages")
        val map: Map<String, @Serializable(with = MessageFormatAsStringSerializer::class) MessageFormat> =
            Json.decodeFromString(
                Thread.currentThread().contextClassLoader.getResourceAsStream("lang/$locale.json")!!.bufferedReader()
                    .use { it.readText() })
        val registry = TranslationRegistry.create(Key.key("fbw"))
        registry.registerAll(locale, map)
        GlobalTranslator.translator().addSource(registry)
    }

    fun Component.plainText() = PlainTextComponentSerializer.plainText().serialize(this)

    fun Component.render() = GlobalTranslator.render(this, locale)
}
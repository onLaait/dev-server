package com.github.onlaait.fbw.utils

import com.github.onlaait.fbw.serializer.MessageFormatAsStringSerializer
import com.github.onlaait.fbw.server.Logger
import kotlinx.serialization.Serializable
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
        val localeStr = locale.toString().lowercase()
        Logger.debug { "Loading language $localeStr" }
        val map: Map<String, @Serializable(with = MessageFormatAsStringSerializer::class) MessageFormat> =
            Json.decodeFromString(ClassLoader.getSystemResource("lang/$localeStr.json").readText())
        val registry = TranslationRegistry.create(Key.key("fbw"))
        registry.registerAll(locale, map)
        GlobalTranslator.translator().addSource(registry)
    }

    fun Component.plainText() = PlainTextComponentSerializer.plainText().serialize(this)

    fun Component.render() = GlobalTranslator.render(this, locale)
}
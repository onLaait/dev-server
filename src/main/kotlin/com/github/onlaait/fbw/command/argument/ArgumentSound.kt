package com.github.onlaait.fbw.command.argument

import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.minestom.server.command.builder.arguments.minecraft.SuggestionType
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentRegistry

/**
 * Represents an argument giving an [Sound.Type].
 */
class ArgumentSound(id: String) : ArgumentRegistry<Sound.Type>(id) {

    init {
        suggestionType = SuggestionType.AVAILABLE_SOUNDS
    }

    override fun parser(): String = "minecraft:resource_location"

    override fun getRegistry(value: String): Sound.Type = Sound.Type { Key.key(value) }

    override fun toString(): String = String.format("Sound<%s>", id)
}
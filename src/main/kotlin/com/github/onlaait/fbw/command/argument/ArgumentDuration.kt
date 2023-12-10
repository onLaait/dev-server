package com.github.onlaait.fbw.command.argument

import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.utils.binary.BinaryWriter

class ArgumentDuration(id: String) : Argument<String>(id) {

    companion object {
        private const val WRONG_FORMAT_ERROR = 1
        private const val TOO_LONG_ERROR = 1
    }

    init {
        this.setSuggestionCallback { _, context, suggestion ->
            val input = context.map["기간"] as? String ?: return@setSuggestionCallback
            if (input.length > 9) return@setSuggestionCallback
            var num = false
            var gotM = false
            var gotH = false
            var gotD = false
            input.forEach {
                when (it) {
                    in '0'..'9' -> {
                        num = true
                    }
                    'm' -> {
                        if (gotM || !num) return@setSuggestionCallback
                        gotM = true
                        num = false
                    }
                    'h' -> {
                        if (gotH || !num) return@setSuggestionCallback
                        gotH = true
                        num = false
                    }
                    'd' -> {
                        if (gotD || !num) return@setSuggestionCallback
                        gotD = true
                        num = false
                    }
                    else -> return@setSuggestionCallback
                }
            }
            if (!num) return@setSuggestionCallback
            suggestion.addEntry(SuggestionEntry(input + 's'))
            if (!gotM) {
                suggestion.addEntry(SuggestionEntry(input + 'm'))
                if (!gotH) {
                    suggestion.addEntry(SuggestionEntry(input + 'h'))
                    if (!gotD) {
                        suggestion.addEntry(SuggestionEntry(input + 'd'))
                    }
                }
            }
        }
    }

    override fun parse(sender: CommandSender, input: String): String = input

    override fun parser(): String = "brigadier:string"

    override fun nodeProperties(): ByteArray {
        return BinaryWriter.makeArray { packetWriter ->
            packetWriter.writeVarInt(0) // Single word
        }
    }

    override fun toString(): String = String.format("Duration<%s>", id)
}
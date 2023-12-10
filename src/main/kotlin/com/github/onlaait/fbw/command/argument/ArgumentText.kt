package com.github.onlaait.fbw.command.argument

import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.utils.binary.BinaryWriter

class ArgumentText(id: String) : Argument<String>(id, true, true) {

    override fun parse(sender: CommandSender, input: String): String = input

    override fun parser(): String = "brigadier:string"

    override fun nodeProperties(): ByteArray {
        return BinaryWriter.makeArray { packetWriter ->
            packetWriter.writeVarInt(2) // Greedy phrase
        }
    }

    override fun toString(): String = String.format("Text<%s>", id)
}
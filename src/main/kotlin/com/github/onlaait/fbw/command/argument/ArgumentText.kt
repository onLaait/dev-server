package com.github.onlaait.fbw.command.argument

import net.minestom.server.command.ArgumentParserType
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.network.NetworkBuffer

class ArgumentText(id: String) : Argument<String>(id, true, true) {

    override fun parse(sender: CommandSender, input: String) = input

    override fun parser() = ArgumentParserType.STRING

    override fun nodeProperties() = NetworkBuffer.makeArray(NetworkBuffer.VAR_INT, 2) // Greedy phrase

    override fun toString() = "Text<$id>"
}
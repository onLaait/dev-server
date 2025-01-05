package com.github.onlaait.fbw.command.argument

import net.minestom.server.command.ArgumentParserType
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.exception.ArgumentSyntaxException
import net.minestom.server.network.NetworkBuffer

class ArgumentUsername(id: String) : Argument<String>(id) {

    private companion object {
        const val INVALID_USERNAME = 1
        val USERNAME_RGX = Regex("^[a-zA-Z0-9_]{1,16}$")
    }

    private var strict = true

    @Throws(ArgumentSyntaxException::class)
    override fun parse(sender: CommandSender, input: String): String {
        if (strict && !USERNAME_RGX.matches(input)) {
            throw ArgumentSyntaxException(
                "Invalid username",
                input,
                INVALID_USERNAME
            )
        }
        return input
    }

    override fun parser() = ArgumentParserType.ENTITY

    override fun nodeProperties() = NetworkBuffer.makeArray(NetworkBuffer.BYTE, 0x03)

    fun strict(strict: Boolean): ArgumentUsername {
        this.strict = strict
        return this
    }

    override fun toString() = "Username<$id>"
}
package com.github.laaitq.fbw.command.argument

import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.exception.ArgumentSyntaxException
import net.minestom.server.utils.binary.BinaryWriter
import java.util.regex.Pattern

class ArgumentUsername(id: String) : Argument<String>(id) {

    companion object {
        private const val INVALID_USERNAME = 1
        private val USERNAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]{1,16}")
    }

    private var strict = true

    @Throws(ArgumentSyntaxException::class)
    override fun parse(sender: CommandSender, input: String): String {
        if (this.strict && !USERNAME_PATTERN.matcher(input).matches()) {
            throw ArgumentSyntaxException(
                "Invalid username",
                input,
                INVALID_USERNAME
            )
        }
        return input
    }

    override fun parser(): String = "minecraft:entity"

    override fun nodeProperties(): ByteArray {
        return BinaryWriter.makeArray { packetWriter: BinaryWriter ->
            packetWriter.writeByte(0x03)
        }
    }

    fun strict(strict: Boolean): ArgumentUsername {
        this.strict = strict
        return this
    }

    override fun toString(): String = String.format("Username<%s>", id)
}
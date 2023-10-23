package com.github.laaitq.fbw.command

import com.github.laaitq.fbw.system.OpSystem.isOp
import com.github.laaitq.fbw.utils.AudienceUtils.sendMsg
import com.github.laaitq.fbw.utils.CommandUtils.usage
import net.minestom.server.command.builder.Command

object PlaysoundCommand : Command("playsound") {
    init {
        setCondition { sender, _ -> sender.isOp }

        setDefaultExecutor { sender, context ->
            sender.sendMsg(usage("${context.commandName} <sound> <source> <targets> [pos] [volume] [pitch]"))
        }
    }
}
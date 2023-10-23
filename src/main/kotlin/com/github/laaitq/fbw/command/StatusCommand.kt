package com.github.laaitq.fbw.command

import com.github.laaitq.fbw.system.OpSystem.isOp
import net.minestom.server.command.builder.Command

object StatusCommand : Command("status") {
    init {
        setCondition { sender, _ -> sender.isOp }

        setDefaultExecutor { sender, context ->

        }
    }
}
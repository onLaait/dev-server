package com.github.onlaait.fbw.command

import com.github.onlaait.fbw.utils.infoMsg
import net.minestom.server.command.builder.Command

object HelpCommand : Command("help") {
    init {
        setDefaultExecutor { sender, _ ->
            sender.infoMsg("도움말")
        }
    }
}
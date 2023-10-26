package com.github.laaitq.fbw.command

import com.github.laaitq.fbw.utils.AudienceUtils.infoMsg
import net.minestom.server.command.builder.Command

object HelpCommand : Command("help") {
    init {
        setDefaultExecutor { sender, _ ->
            sender.infoMsg("도움말")
        }
    }
}
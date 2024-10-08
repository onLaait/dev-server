package com.github.onlaait.fbw.command

import com.github.onlaait.fbw.system.ServerProperties
import com.github.onlaait.fbw.utils.warnMsg
import net.minestom.server.MinecraftServer
import net.minestom.server.utils.callback.CommandCallback

object CommandRegister {
    init {
        MinecraftServer.getCommandManager().run {
            unknownCommandCallback = CommandCallback { sender, command ->
                if (command.isNotBlank()) {
                    sender.warnMsg("알 수 없는 명령어입니다.")
                }
            }

            val cmds = mutableSetOf(
                BanCommand,
                BanipCommand,
                BanlistCommand,
                BroadcastCommand,
                DeopCommand,
                GamemodeCommand,
                HelpCommand,
                InfoCommand,
                KickCommand,
                ListCommand,
                MaxplayersCommand,
                MotdCommand,
                MuteCommand,
                OpCommand,
                PardonCommand,
                PardonipCommand,
                PlaysoundCommand,
                StatusCommand,
                StopCommand,
                TestCommand,
                UnmuteCommand,
                WhitelistCommand,
            )
            if (ServerProperties.ENABLE_KAKC) cmds += KakcCommand

            cmds.forEach {
                register(it)
            }
        }
    }
}
package com.github.laaitq.fbw.command

import com.github.laaitq.fbw.utils.AudienceUtils.warnMsg
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

            arrayOf(
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
            ).forEach {
                register(it)
            }
        }
    }
}
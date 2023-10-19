package com.github.laaitq.fbw.command

import com.github.laaitq.fbw.utils.AudienceUtils.warnMsg
import net.minestom.server.MinecraftServer
import net.minestom.server.utils.callback.CommandCallback

object CommandRegister {
    init {
        MinecraftServer.getCommandManager().run {
            unknownCommandCallback = CommandCallback { sender, command ->
                if (command != "") {
                    sender.warnMsg("알 수 없는 명령어입니다.")
                }
            }
            register(BanCommand)
            register(DeopCommand)
            register(GamemodeCommand)
            register(KickCommand)
            register(OpCommand)
            register(PardonCommand)
            register(StopCommand)
            register(TestCommand)
            register(WhitelistCommand)
        }
    }
}
package com.github.onlaait.fbw.command

import com.github.onlaait.fbw.system.OpSystem.isOp
import com.github.onlaait.fbw.utils.AudienceUtils.alertMsg
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command

object StopCommand : Command("stop") {
    init {
        setCondition { sender, _ -> sender.isOp }

        setDefaultExecutor { sender, _ ->
            sender.alertMsg("서버를 종료합니다.")
            MinecraftServer.stopCleanly()
        }
    }
}
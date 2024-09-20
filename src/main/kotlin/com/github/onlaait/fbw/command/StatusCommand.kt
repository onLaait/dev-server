package com.github.onlaait.fbw.command

import com.github.onlaait.fbw.system.ServerStatus
import com.github.onlaait.fbw.utils.AudienceUtils.infoMsg
import com.github.onlaait.fbw.utils.CoroutineManager
import kotlinx.coroutines.launch
import net.minestom.server.MinecraftServer
import net.minestom.server.ServerFlag
import net.minestom.server.command.builder.Command

object StatusCommand : Command("status") {
    init {
        setDefaultExecutor { sender, _ ->
            CoroutineManager.subScope.launch {
                sender.infoMsg(
                    "TPS: ${StringBuilder(String.format("%.1f", ServerStatus.tps))}/${ServerFlag.SERVER_TICKS_PER_SECOND} (${String.format("%.1f", ServerStatus.mspt)}/${MinecraftServer.TICK_MS} MSPT)",
                    "CPU: ${String.format("%.1f", ServerStatus.cpuLoad * 100)} %",
                    "메모리: ${ServerStatus.usedMem} / ${ServerStatus.totalMem} MB"
                )
            }
        }
    }
}
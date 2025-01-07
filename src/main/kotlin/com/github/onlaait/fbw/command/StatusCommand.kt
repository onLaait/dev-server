package com.github.onlaait.fbw.command

import com.github.onlaait.fbw.system.ServerStatusMonitor
import com.github.onlaait.fbw.utils.CoroutineManager
import com.github.onlaait.fbw.utils.infoMsg
import kotlinx.coroutines.launch
import net.minestom.server.MinecraftServer
import net.minestom.server.ServerFlag
import net.minestom.server.command.builder.Command

object StatusCommand : Command("status") {
    init {
        setDefaultExecutor { sender, _ ->
            CoroutineManager.SUB_SCOPE.launch {
                sender.infoMsg(
                    "TPS: ${StringBuilder(String.format("%.1f", ServerStatusMonitor.tps))}/${ServerFlag.SERVER_TICKS_PER_SECOND} (${String.format("%.1f", ServerStatusMonitor.mspt)}/${MinecraftServer.TICK_MS} MSPT)",
                    "CPU: ${String.format("%.1f", ServerStatusMonitor.cpuLoad * 100)} %",
                    "메모리: ${ServerStatusMonitor.usedMem} / ${ServerStatusMonitor.totalMem} MB"
                )
            }
        }
    }
}
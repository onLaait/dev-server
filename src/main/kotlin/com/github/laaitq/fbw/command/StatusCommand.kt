package com.github.laaitq.fbw.command

import com.github.laaitq.fbw.system.ServerStatus
import com.github.laaitq.fbw.utils.AudienceUtils.infoMsg
import com.github.laaitq.fbw.utils.MyCoroutines
import kotlinx.coroutines.launch
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command

object StatusCommand : Command("status") {
    init {
        setDefaultExecutor { sender, _ ->
            MyCoroutines.subScope.launch {
                sender.infoMsg(
                    "TPS: ${StringBuilder(String.format("%.1f", ServerStatus.tps))}/${MinecraftServer.TICK_PER_SECOND} (${String.format("%.1f", ServerStatus.mspt)}/${MinecraftServer.TICK_MS} MSPT)",
                    "CPU: ${String.format("%.1f", ServerStatus.cpuLoad * 100)} %",
                    "메모리: ${ServerStatus.usedMem} / ${ServerStatus.totalMem} MB"
                )
            }
        }
    }
}
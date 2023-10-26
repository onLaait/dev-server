package com.github.laaitq.fbw.command

import com.github.laaitq.fbw.system.ServerStatus
import com.github.laaitq.fbw.utils.AudienceUtils.infoMsg
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import kotlin.concurrent.thread

object StatusCommand : Command("status") {
    init {
        setDefaultExecutor { sender, _ ->
            thread {
                sender.infoMsg(
                    "TPS: ${StringBuilder(String.format("%.1f", ServerStatus.tps))}/${MinecraftServer.TICK_PER_SECOND} (${String.format("%.1f", ServerStatus.mspt)}/${MinecraftServer.TICK_MS} MSPT)",
                    "CPU: ${String.format("%.1f", ServerStatus.cpuLoad * 100)} %",
                    "메모리: ${ServerStatus.usedMem} / ${ServerStatus.totalMem} MB"
                )
            }
        }
    }
}
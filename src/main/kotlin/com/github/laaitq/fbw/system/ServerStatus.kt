package com.github.laaitq.fbw.system

import com.github.laaitq.fbw.utils.PlayerUtils
import com.github.laaitq.fbw.utils.TextUtils
import com.sun.management.OperatingSystemMXBean
import net.kyori.adventure.audience.Audience
import net.minestom.server.MinecraftServer
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.entity.Player
import java.lang.management.ManagementFactory
import java.util.concurrent.ArrayBlockingQueue
import kotlin.concurrent.thread
import kotlin.math.min

object ServerStatus {

    private var tabListContent = Pair("", "")

    private val runtime = Runtime.getRuntime()
    private val os = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean

    val cpuLoad: Double
        get() = os.processCpuLoad

    val totalMem: Long
        get() = runtime.totalMemory() / 1024 / 1024
    val freeMem: Long
        get() = runtime.freeMemory() / 1024 / 1024
    val usedMem: Long
        get() = totalMem - freeMem


    private val maxTps = MinecraftServer.TICK_PER_SECOND.toDouble()
    private val queueSize = MinecraftServer.TICK_PER_SECOND * 5
    private val lastTicks = ArrayBlockingQueue<Double>(queueSize)
    val mspt: Double
        get() = lastTicks.average()
    val tps: Double
        get() = min(maxTps, 1000 / mspt)
    fun onTick(tickTime: Double) {
        if (lastTicks.remainingCapacity() == 0) lastTicks.poll()
        lastTicks.offer(tickTime)
    }

    init {
        thread(name = "ServerStatusThread", isDaemon = true) {
            while (true) {
                val tps = tps
                val tpsStr = StringBuilder(String.format("%.1f", tps))
                if (tps == maxTps) tpsStr.append('*')

                tabListContent = Pair(
                    (
                        "                                   \n" +
                        "테스트 서버\n" +
                        "%s/%s\n"
                    ),
                    (
                        "\n" +
                        "<gray>TPS: <white>$tpsStr <dark_gray>| <gray>Memory: <white>$usedMem<gray>/$totalMem MB\n" +
                        "<gray>Ping: <white>%s <gray>ms\n"
                    )
                )

                Audiences.players().sendTabList()

                Thread.sleep(500)
            }
        }
    }

    fun Audience.sendTabList() {
        val content = tabListContent
        val header = content.first
        val footer = content.second

        val onlinePlayersCount = PlayerUtils.onlinePlayersCount
        val maxPlayers = ServerProperties.MAX_PLAYERS

        this.forEachAudience { audience ->
            if (audience !is Player) return@forEachAudience
            audience.sendPlayerListHeaderAndFooter(
                TextUtils.formatText(String.format(header, onlinePlayersCount, maxPlayers)),
                TextUtils.formatText(String.format(footer, audience.latency))
            )
        }
    }
}
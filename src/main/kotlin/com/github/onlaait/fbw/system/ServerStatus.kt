package com.github.onlaait.fbw.system

import com.github.onlaait.fbw.utils.allPlayersCount
import com.github.onlaait.fbw.utils.formatText
import com.sun.management.OperatingSystemMXBean
import net.kyori.adventure.audience.Audience
import net.minestom.server.ServerFlag
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


    private val maxTps = ServerFlag.SERVER_TICKS_PER_SECOND.toDouble()
    private val lastTicks = ArrayBlockingQueue<Double>(ServerFlag.SERVER_TICKS_PER_SECOND * 5)
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
            cpuLoad // 첫 실행 시 렉 발생

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

        val onlinePlayersCount = allPlayersCount
        val maxPlayers = ServerProperties.MAX_PLAYERS

        this.forEachAudience { audience ->
            if (audience !is Player) return@forEachAudience
            audience.sendPlayerListHeaderAndFooter(
                formatText(String.format(header, onlinePlayersCount, maxPlayers)),
                formatText(String.format(footer, audience.latency))
            )
        }
    }
}
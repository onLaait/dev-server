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

    private lateinit var tabListFormat: Pair<String, String>

    private val os = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean

    val cpuLoad: Double
        get() = os.processCpuLoad

    val totalMem: Long
        get() = Runtime.getRuntime().totalMemory() / 1024 / 1024
    val freeMem: Long
        get() = Runtime.getRuntime().freeMemory() / 1024 / 1024
    val usedMem: Long
        get() = totalMem - freeMem


    private val maxTps = ServerFlag.SERVER_TICKS_PER_SECOND.toDouble()
    private val lastTicks = ArrayBlockingQueue<Double>(ServerFlag.SERVER_TICKS_PER_SECOND * 5)
    val mspt: Double
        get() = lastTicks.average()
    val tps: Double
        get() = min(maxTps, 1000 / mspt)
    fun onTick(tickTime: Double) {
        tick++
        if (lastTicks.remainingCapacity() == 0) lastTicks.poll()
        lastTicks.offer(tickTime)
    }

    var tick: Long = 0

    init {
        thread(name = "ServerStatusThread", isDaemon = true) {
            cpuLoad // 첫 실행 시 렉 발생

            while (true) {
                val tps = tps
                val tpsStr = String.format("%.1f", tps)

                tabListFormat = Pair(
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
        val format = tabListFormat
        val header = format.first
        val footer = format.second

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
package com.github.onlaait.fbw.system

import com.github.onlaait.fbw.server.Event
import com.github.onlaait.fbw.server.Schedule
import com.github.onlaait.fbw.server.Schedule.seconds
import com.github.onlaait.fbw.utils.allPlayersCount
import com.github.onlaait.fbw.utils.formatText
import com.sun.management.OperatingSystemMXBean
import net.kyori.adventure.audience.Audience
import net.minestom.server.ServerFlag
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.entity.Player
import net.minestom.server.event.server.ServerTickMonitorEvent
import java.lang.management.ManagementFactory
import kotlin.math.min

object ServerStatusMonitor {

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

    private val TICKS_DEQUE_SIZE = ServerFlag.SERVER_TICKS_PER_SECOND * 5
    private val ticksDeque = ArrayDeque<Double>(TICKS_DEQUE_SIZE)

    val mspt: Double
        get() = ticksDeque.average()

    val tps: Double
        get() = min(maxTps, 1000 / mspt)

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

    init {
        cpuLoad // 첫 실행 시 렉

        Event.addListener<ServerTickMonitorEvent> { e ->
            if (ticksDeque.size >= TICKS_DEQUE_SIZE) ticksDeque.removeFirst()
            ticksDeque += e.tickMonitor.tickTime
        }

        Schedule.manager.buildTask {
            val tps = tps
            val tpsStr = String.format("%.1f", tps)

            tabListFormat = Pair(
                "                                   \n" +
                "테스트 서버\n" +
                "%s/%s\n"
                ,
                "\n" +
                "<gray>TPS: <white>$tpsStr <dark_gray>| <gray>Memory: <white>$usedMem<gray>/$totalMem MB\n" +
                "<gray>Ping: <white>%s <gray>ms\n"
            )

            Audiences.players().sendTabList()
        }.repeat(0.5.seconds).schedule()
    }
}
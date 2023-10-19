package com.github.laaitq.fbw.system

import com.github.laaitq.fbw.utils.PlayerUtils.sendTabList
import net.minestom.server.MinecraftServer
import java.util.concurrent.ArrayBlockingQueue
import kotlin.concurrent.thread
import kotlin.math.min

object ServerStatus {
    object Memory {
        private var totalMemP: Long = 0
        private var freeMemP: Long = 0

        val totalMem: Long
            get() = totalMemP
        val freeMem: Long
            get() = freeMemP
        val usedMem: Long
            get() = totalMemP - freeMemP

        init {
            thread(isDaemon = true) {
                val runtime = Runtime.getRuntime()
                while (true) {
                    totalMemP = runtime.totalMemory() / 1024 / 1024
                    freeMemP = runtime.freeMemory() / 1024 / 1024
                    MinecraftServer.getConnectionManager().onlinePlayers.forEach { player ->
                        player.sendTabList()
                    }
                    Thread.sleep(500)
                }
            }
        }
    }

    object TPS {
        private val maxTps = MinecraftServer.TICK_PER_SECOND.toDouble()
        private val queueSize = MinecraftServer.TICK_PER_SECOND*5
        private val lastTicks = ArrayBlockingQueue<Double>(queueSize)
        val tps: Double
            get() = min(maxTps, 1000 / lastTicks.average())

        fun onTick(tickTime: Double) {
            if (lastTicks.remainingCapacity() == 0) lastTicks.poll()
            lastTicks.offer(tickTime)
        }
    }
}
package com.github.onlaait.fbw.server

import com.github.onlaait.fbw.event.PlayerLClickEvent
import com.github.onlaait.fbw.event.PlayerRClickEvent
import com.github.onlaait.fbw.utils.allPlayers
import net.minestom.server.MinecraftServer
import net.minestom.server.ServerFlag
import net.minestom.server.timer.TaskSchedule

val scheduleManager = MinecraftServer.getSchedulerManager()

object Schedule {

    val NEXT_CLIENT_TICK = 0.05.seconds

    init {
        val schedule = scheduleManager

        schedule.buildTask {
            allPlayers.forEach { p ->
                p.mouseInputs.run {
                    if (left) {
//                        println("${ServerStatus.tick} L schedule")
                        Event.handler.call(PlayerLClickEvent(p))
                    }
                    if (right) {
//                        println("${ServerStatus.tick} R schedule")
                        Event.handler.call(PlayerRClickEvent(p))
                    }
                }
//                it.setHeldItemSlot(4)
            }
        }.repeat(TaskSchedule.nextTick()).schedule()

        schedule.buildTask {
            allPlayers.forEach { p ->
//                if (!p.isSpectating()) p.stopSpectating()
            }
        }.repeat(NEXT_CLIENT_TICK).schedule()
    }

    inline val Double.seconds: TaskSchedule
        get() = TaskSchedule.tick((this * ServerFlag.SERVER_TICKS_PER_SECOND).toInt())
}
package com.github.onlaait.fbw.server

import com.github.onlaait.fbw.event.PlayerLClickEvent
import com.github.onlaait.fbw.event.PlayerRClickEvent
import com.github.onlaait.fbw.utils.allPlayers
import net.minestom.server.MinecraftServer
import net.minestom.server.timer.ExecutionType
import net.minestom.server.timer.TaskSchedule

val scheduleManager = MinecraftServer.getSchedulerManager()

object Schedule {
    init {
        val schedule = scheduleManager

        schedule.buildTask {
            tick++
            allPlayers.forEach {
                val p = it as FPlayer
                p.mouseInputs.run {
                    if (left) {
                        println("$tick L schedule")
                        eventHandler.call(PlayerLClickEvent(p))
                    }
                    if (right) {
                        println("$tick R schedule")
                        eventHandler.call(PlayerRClickEvent(p))
                    }
                }
//                it.setHeldItemSlot(4)
            }
        }
            .repeat(TaskSchedule.nextTick())
            .schedule()

        schedule.buildTask {

        }
            .executionType(ExecutionType.TICK_END)
            .repeat(TaskSchedule.nextTick())
            .schedule()
    }

    var tick: Long = 0
}
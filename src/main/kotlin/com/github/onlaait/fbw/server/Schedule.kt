package com.github.onlaait.fbw.server

import com.github.onlaait.fbw.model.ModelManager
import com.github.onlaait.fbw.utils.allPlayers
import net.minestom.server.MinecraftServer
import net.minestom.server.ServerFlag
import net.minestom.server.timer.ExecutionType
import net.minestom.server.timer.TaskSchedule

object Schedule {

    val manager = MinecraftServer.getSchedulerManager()

    val NEXT_CLIENT_TICK = 0.05.seconds

    init {
        manager.buildTask {
            allPlayers.forEach { p ->
                p.mouseInputs.run {
                    if (left) {
                        p.doll?.weaponHolder?.lClick()
                    }
                    if (right) {
                        p.doll?.weaponHolder?.rClick()
                    }
                }
//                it.setHeldItemSlot(4)
            }
        }.repeat(TaskSchedule.nextTick()).schedule()

        manager.buildTask {
            allPlayers.forEach { p ->
//                if (!p.isSpectating()) p.stopSpectating()
            }
        }.repeat(NEXT_CLIENT_TICK).schedule()

        manager.buildTask {
            ModelManager.tick()
        }.executionType(ExecutionType.TICK_END).repeat(TaskSchedule.nextTick()).schedule()

    }

    inline val Double.seconds: TaskSchedule
        get() = TaskSchedule.tick((this * ServerFlag.SERVER_TICKS_PER_SECOND).toInt())
}
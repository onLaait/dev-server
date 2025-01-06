package com.github.onlaait.fbw.game.movement

import com.github.onlaait.fbw.server.scheduleManager
import com.github.onlaait.fbw.utils.editMeta
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.metadata.display.ItemDisplayMeta
import net.minestom.server.entity.metadata.other.InteractionMeta
import net.minestom.server.timer.Task
import net.minestom.server.timer.TaskSchedule

class CannotStepOrRotate : Movement() {

    private val dummy = Entity(EntityType.ITEM_DISPLAY)
    private val dummy2 = Entity(EntityType.INTERACTION)
    private lateinit var schedule: Task
//    private var eyeHeight: Double = 0.0

    override fun start() {
        dummy.synchronizationTicks = Long.MAX_VALUE
        dummy2.synchronizationTicks = Long.MAX_VALUE
        dummy.editMeta<ItemDisplayMeta> {
            isHasNoGravity = true
        }
        dummy2.editMeta<InteractionMeta> {
            isHasNoGravity = true
            height = player.eyeHeight.toFloat() + 0.2859f
            width = 0f
        }
//        eyeHeight = player.eyeHeight
        val pos = player.position
        dummy.setInstance(player.instance, pos)
        dummy2.setInstance(player.instance, pos)
        dummy.addPassenger(dummy2)
//        dummy.refreshPosition(pos)
//        dummy.teleport(pos)
//        dummy.setView(pos.yaw, pos.pitch)
//        player.gameMode = GameMode.SPECTATOR
        player.spectate(dummy2)
        schedule =
            scheduleManager.buildTask {
//                dummy.refreshPosition(pos)
//                dummy.teleport(pos)
//                dummy.setView(pos.yaw, pos.pitch)
                player.teleport(pos)
            }.repeat(TaskSchedule.tick(2)).schedule()
    }

    override fun end() {
        schedule.cancel()
        player.stopSpectating()
//        player.gameMode = GameMode.SURVIVAL
        dummy.remove()
        dummy2.remove()
    }
}
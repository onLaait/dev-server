package com.github.onlaait.fbw.game.movement

import com.github.onlaait.fbw.entity.FEntity
import com.github.onlaait.fbw.server.Schedule
import com.github.onlaait.fbw.utils.editMeta
import com.github.onlaait.fbw.utils.shakeScreen
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.metadata.display.ItemDisplayMeta
import net.minestom.server.entity.metadata.villager.VillagerMeta
import net.minestom.server.timer.Task

class CannotStepOrRotate : Movement() {

    private val baseE = FEntity(EntityType.ITEM_DISPLAY)
    private val camE = FEntity(EntityType.VILLAGER)
    private lateinit var schedule: Task
//    private var eyeHeight: Double = 0.0

    override fun start() {
        baseE.run {
            editMeta<ItemDisplayMeta> {
                isHasNoGravity = false
                posRotInterpolationDuration = 1
            }
            aerodynamics = player.aerodynamics
            boundingBox = player.boundingBox
            velocity = player.getRealVelocity()
        }
        camE.editMeta<VillagerMeta> {
            isInvisible = true
        }
//        eyeHeight = player.eyeHeight
        val pos = player.position
        baseE.setInstance(player.instance, pos)
        camE.setInstance(player.instance, pos)
        baseE.addPassenger(camE)
        player.spectate(camE)
        camE.shakeScreen()
        schedule =
            Schedule.manager.buildTask {
                syncMovement()
            }.repeat(Schedule.NEXT_CLIENT_TICK).schedule()
    }

    override fun end() {
        schedule.cancel()
        player.stopSpectating()
        syncMovement()
        baseE.remove()
        camE.remove()
    }

    private fun syncMovement() {
        player.teleport(baseE.position)
        player.velocity = baseE.velocity
    }
}
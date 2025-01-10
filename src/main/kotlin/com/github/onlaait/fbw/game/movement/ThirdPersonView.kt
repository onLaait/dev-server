package com.github.onlaait.fbw.game.movement

import com.github.onlaait.fbw.game.obj.Doll
import com.github.onlaait.fbw.math.*
import com.github.onlaait.fbw.server.Schedule
import com.github.onlaait.fbw.utils.editMeta
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.metadata.display.ItemDisplayMeta
import net.minestom.server.timer.Task

class ThirdPersonView : Movement() {

    private lateinit var centerE: Doll
    private val vehicleE = Entity(EntityType.ITEM_DISPLAY)

    private lateinit var schedule: Task

    override fun start() {
        centerE = player.doll!!
        vehicleE.editMeta<ItemDisplayMeta> {
            isHasNoGravity = true
            posRotInterpolationDuration = 1
        }
        vehicleE.setInstance(player.instance, player.position.withY { it + 0.6 }.withPitch(0f))
        centerE.disableSyncPosition()
        vehicleE.addPassenger(player)
        schedule =
            Schedule.manager.buildTask {
                sync()
            }.repeat(Schedule.NEXT_CLIENT_TICK).schedule()
    }

    override fun end() {
        schedule.cancel()
        vehicleE.remove()
        centerE.enableSyncPosition()
    }

    private fun sync() {
        val position = player.getPov()
        val pos = position.toVec3f()
        val dir = position.direction().toVec3f()
        val focus = pos + dir * 5f
        val delta = centerE.pos.toVec3f() - focus
        vehicleE.teleport(vehicleE.position + delta.toVec())
    }
}
package com.github.onlaait.fbw.entity

import com.github.onlaait.fbw.server.Schedule
import com.github.onlaait.fbw.server.Server
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.timer.ExecutionType

open class FEntity(entityType: EntityType) : Entity(entityType) {

    private var lastPosition: Pos = position

    init {
        setNoGravity(true)

        scheduler().buildTask {
            if (position == lastPosition) {
                previousPosition = position
            }
            lastPosition = position
        }.executionType(ExecutionType.TICK_END).repeat(Schedule.NEXT_CLIENT_TICK).schedule()
    }

    override fun movementTick() {
        if (Server.isClientTickTime) movementClientTick()
    }

    open fun movementClientTick() {
        super.movementTick()
    }

    override fun spawn() {
        super.spawn()
        onSpawn()
    }

    open fun onSpawn() {}

    override fun despawn() {
        onDespawn()
        super.despawn()
    }

    open fun onDespawn() {}
}
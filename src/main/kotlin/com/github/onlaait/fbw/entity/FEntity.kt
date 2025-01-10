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

    private var d = 1

    override fun movementTick() {
        if (!Server.IS_SERVER_TICK_EQUAL_TO_CLIENT_TICK) {
            if (d != 1) {
                if (d == Server.CLIENT_2_SERVER_TICKS) {
                    d = 1
                } else {
                    d++
                }
                return
            }
            d++
        }
        movementClientTick()
    }

    open fun movementClientTick() {
        super.movementTick()
    }
}
package com.github.onlaait.fbw.entity

import com.github.onlaait.fbw.server.Server
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType

open class FEntity(entityType: EntityType) : Entity(entityType) {

    private var d = 1

    override fun tick(time: Long) {
        if (!Server.IS_SERVER_TICK_EQUAL_TO_CLIENT_TICK) {
            if (d == Server.CLIENT_2_SERVER_TICKS) {
                d = 1
                return
            }
            d++
        }
        super.tick(time)
    }
}
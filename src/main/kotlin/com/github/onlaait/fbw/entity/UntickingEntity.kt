package com.github.onlaait.fbw.entity

import net.minestom.server.entity.EntityType

open class UntickingEntity(entityType: EntityType) : FEntity(entityType) {

    init {
        hasPhysics = false
    }

    override fun movementTick() {}
//    override fun touchTick() {}
    override fun update(time: Long) {}
//    override fun effectTick() {}
}
package com.github.onlaait.fbw.game.obj

import com.github.onlaait.fbw.entity.FEntity
import net.minestom.server.entity.EntityType

abstract class Agent : Caster, Hittable, FEntity(EntityType.PLAYER) {

    override val isHittableByTeammate = false

    init {
        entityType = EntityType.INTERACTION
    }
}
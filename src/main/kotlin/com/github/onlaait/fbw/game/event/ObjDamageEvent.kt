package com.github.onlaait.fbw.game.event

import com.github.onlaait.fbw.game.obj.Caster
import com.github.onlaait.fbw.game.obj.Hittable
import net.minestom.server.event.Event

class ObjDamageEvent(
    val victim: Hittable, val attacker: Caster?, val damage: Double, val critical: Boolean
) : Event

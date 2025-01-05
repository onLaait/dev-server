package com.github.onlaait.fbw.game.skill

import com.github.onlaait.fbw.game.obj.Caster

interface Skill {

    val description: String

    val cooldown: Double

    fun cast(o: Caster)

}
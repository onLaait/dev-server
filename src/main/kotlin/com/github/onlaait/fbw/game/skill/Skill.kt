package com.github.onlaait.fbw.game.skill

import com.github.onlaait.fbw.game.obj.Caster

abstract class Skill(val name: String) {

    abstract val description: String

    abstract val cooldown: Double

    fun cast(caster: Caster) {
        function(caster)
    }

    private var function: (Caster) -> Unit = {}

    fun skill(function: (Caster) -> Unit) {
        this.function = function
    }
}
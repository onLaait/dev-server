package com.github.onlaait.fbw.game.skill

import com.github.onlaait.fbw.game.obj.Caster

class SkillHolder(val caster: Caster) {

    val skillMap = mutableMapOf<Int, Skill>()

    fun cast(key: Int) {
        val skill = skillMap[key] ?: return
        skill.cast(caster)
    }
}
package com.github.onlaait.fbw.game.character

import com.github.onlaait.fbw.game.skill.Skill
import com.github.onlaait.fbw.game.weapon.Weapon

abstract class Character(val name: String) {

    abstract val modelId: String

    abstract val weapons: Array<Weapon>
    abstract val skills: Map<Int, Skill>

}
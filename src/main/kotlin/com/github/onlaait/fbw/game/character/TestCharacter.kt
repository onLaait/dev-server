package com.github.onlaait.fbw.game.character

import com.github.onlaait.fbw.game.skill.ExampleSkill
import com.github.onlaait.fbw.game.skill.Skill
import com.github.onlaait.fbw.game.weapon.Weapon
import com.github.onlaait.fbw.game.weapon.WhiteFang465

object TestCharacter : Character("테스트") {

    override val modelId: String = "test"

    override val weapons: Array<Weapon> = arrayOf(WhiteFang465)
    override val skills: Map<Int, Skill> = mapOf(1 to ExampleSkill)
}
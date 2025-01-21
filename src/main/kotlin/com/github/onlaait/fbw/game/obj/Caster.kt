package com.github.onlaait.fbw.game.obj

import com.github.onlaait.fbw.entity.FPlayer
import com.github.onlaait.fbw.game.skill.SkillHolder
import com.github.onlaait.fbw.game.weapon.WeaponHolder
import net.minestom.server.coordinate.Pos

interface Caster : GameObj {

    fun getPov(): Pos

    val weaponHolder: WeaponHolder
    val skillHolder: SkillHolder

    val audiences: MutableList<FPlayer>
}
package com.github.onlaait.fbw.game.weapon

import com.github.onlaait.fbw.game.obj.Caster

class WeaponHolder(val caster: Caster) {

    val weapons = mutableListOf<Weapon>()

    var holding = 0

    fun use() {

    }

    fun reload() {

    }

    fun next() {
        val prev = holding
        if (++holding > weapons.lastIndex) holding = 0
        if (holding == prev) return
        // TODO
    }

    fun before() {
        val prev = holding
        if (--holding < 0) holding = weapons.lastIndex
        if (holding == prev) return
        // TODO
    }
}
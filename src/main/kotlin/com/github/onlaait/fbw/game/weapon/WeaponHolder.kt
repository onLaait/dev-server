package com.github.onlaait.fbw.game.weapon

import com.github.onlaait.fbw.game.obj.Caster
import com.github.onlaait.fbw.game.obj.Doll
import com.github.onlaait.fbw.model.FirstPersonViewModel

class WeaponHolder(val caster: Caster, weapons: Array<Weapon>) {

    val weapons: MutableList<WeaponData>

    var holding: WeaponData
    var holdingI = 0
    
    init {
        this.weapons = weapons.mapTo(mutableListOf()) { WeaponData(caster, it) }
        holding = this.weapons.first()
        caster.audiences.forEach {
            it.hud.showWeaponSeparator()
            it.hud.setAmmo(holding.ammo, holding.weapon.ammo)
        }
        showModel()
    }

    fun lClick() {
        holding.let { it.weapon.lClick(it) }
    }

    fun rClick() {
        holding.let { it.weapon.rClick(it) }
    }

    fun lHold() {
        holding.let { it.weapon.lHold(it) }
    }

    fun rHold() {
        holding.let { it.weapon.rHold(it) }
    }

    fun lRelease() {
        holding.let { it.weapon.lRelease(it) }
    }

    fun rRelease() {
        holding.let { it.weapon.rRelease(it) }
    }

    fun reload() {
        holding.let { it.weapon.reload(it) }
    }

    fun next() {
        val prev = holdingI
        if (++holdingI > weapons.lastIndex) holdingI = 0
        if (holdingI == prev) return
        holding = weapons[holdingI]
        // TODO
    }

    fun before() {
        val prev = holdingI
        if (--holdingI < 0) holdingI = weapons.lastIndex
        if (holdingI == prev) return
        holding = weapons[holdingI]
        // TODO
    }

    private fun showModel() {
        if (caster !is Doll) return
        FirstPersonViewModel(caster.player, holding.weapon.modelId).init()
    }

    class WeaponData(val caster: Caster, val weapon: Weapon) {
        var ammo: Int = weapon.ammo
        set(value) {
            var value = value
            if (value <= 0) {
                value = 0
            }
            field = value
            caster.audiences.forEach {
                it.hud.setAmmo(value)
            }
            if (value == 0) {
                weapon.reload(this)
            }
        }
    }
}
package com.github.onlaait.fbw.game.weapon

abstract class Weapon(val name: String) {

    abstract val modelId: String

    abstract val ammo: Int
}
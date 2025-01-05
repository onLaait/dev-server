package com.github.onlaait.fbw.game.obj

import net.minestom.server.coordinate.Pos

interface Caster : GameObj {

    fun getPov(): Pos
}
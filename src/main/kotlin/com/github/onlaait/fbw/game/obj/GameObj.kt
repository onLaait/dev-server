package com.github.onlaait.fbw.game.obj

import net.minestom.server.coordinate.Pos

interface GameObj {

    val caster: Caster
    var pos: Pos
}
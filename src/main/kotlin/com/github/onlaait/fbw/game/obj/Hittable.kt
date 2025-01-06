package com.github.onlaait.fbw.game.obj

import com.github.onlaait.fbw.game.utils.Hitbox

interface Hittable : GameObj {

    val maxHp: Int
    var hp: Double

    val isHittableByTeammate: Boolean

    val hitbox: Hitbox

}
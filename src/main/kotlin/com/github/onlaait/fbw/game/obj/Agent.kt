package com.github.onlaait.fbw.game.obj

abstract class Agent : Caster, Hittable {

    override val isHittableByTeammate = false
}
package com.github.onlaait.fbw.game.movement

import net.minestom.server.entity.Player

abstract class Movement {

    internal lateinit var player: Player

    open fun start() {}
    open fun end() {}

    open fun forward() {}
    open fun backward() {}
    open fun left() {}
    open fun right() {}
    open fun jump() {}
    open fun shift() {}
    open fun sprint() {}
}
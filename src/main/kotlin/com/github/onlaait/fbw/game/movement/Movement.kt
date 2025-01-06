package com.github.onlaait.fbw.game.movement

import com.github.onlaait.fbw.server.FPlayer

abstract class Movement {

    internal lateinit var player: FPlayer

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
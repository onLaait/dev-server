package com.github.onlaait.fbw.game.attack

import com.github.onlaait.fbw.game.obj.GameObject
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player

open class Attack {

    lateinit var attacker: GameObject
    lateinit var targets: HashSet<GameObject>
    lateinit var origin: Pos
    lateinit var successTask: (HashSet<Player>) -> Unit

    fun buildSuccessTask(action: (HashSet<Player>) -> Unit) {
        successTask = action
    }
}
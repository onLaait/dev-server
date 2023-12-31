package com.github.onlaait.fbw.game.obj

import com.github.onlaait.fbw.game.Hitbox
import net.minestom.server.coordinate.Pos

interface GameObject {

    var isPenetrable: Boolean
    var isPenetrableByTeammate: Boolean
    var position: Pos
    val hitbox: Set<Hitbox>
}
package com.github.laaitq.fbw.game.obj

import com.github.laaitq.fbw.game.attack.Ray
import net.minestom.server.coordinate.Pos

interface GameObject {
    var isPenetrable: Boolean
    var isPenetrableByTeammate: Boolean
    var position: Pos
    fun intersect(ray: Ray): Intersection?
}

data class Intersection(val distance: Double, val isHead: Boolean)
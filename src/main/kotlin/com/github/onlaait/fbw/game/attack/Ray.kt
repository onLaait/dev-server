package com.github.onlaait.fbw.game.attack

import com.github.onlaait.fbw.game.obj.GameObject
import com.github.onlaait.fbw.game.obj.Intersection
import com.github.onlaait.fbw.game.utils.getTargetBlockIntersection
import com.github.onlaait.fbw.server.Instance
import net.minestom.server.coordinate.Vec

class Ray : Attack() {
    lateinit var direction: Vec
    var maxDistance: Double = 300.0
    var stopOnHitGround: Boolean = true

    fun cast(): RaycastResult {
        var distanceToGround: Double? = null
        if (stopOnHitGround) {
            distanceToGround = getTargetBlockIntersection(origin, direction, maxDistance, Instance.instance)
        }
        val intersectantObjs = mutableSetOf<ObjWithIntersection>()
        for (target in targets) {
            val intersection = target.intersect(this) ?: continue
            if (intersection.distance < maxDistance && (distanceToGround == null || intersection.distance < distanceToGround)) {
                intersectantObjs += ObjWithIntersection(target, intersection)
            }
        }
        return RaycastResult(
            intersectantObjs.sortedWith { a, b ->
                when {
                    a.intersection.distance > b.intersection.distance -> 1
                    a.intersection.distance < b.intersection.distance -> -1
                    else -> 0
                }
            }, distanceToGround
        )
    }

    data class ObjWithIntersection(val obj: GameObject, val intersection: Intersection)
    data class RaycastResult(val objWithIntersection: List<ObjWithIntersection>, val distanceToGround: Double?)
}
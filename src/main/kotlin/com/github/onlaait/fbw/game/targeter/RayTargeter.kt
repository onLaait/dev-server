package com.github.onlaait.fbw.game.targeter

import com.github.onlaait.fbw.game.Hitbox
import com.github.onlaait.fbw.game.obj.GameObject
import com.github.onlaait.fbw.game.utils.BlockUtils
import com.github.onlaait.fbw.geometry.Ray
import com.github.onlaait.fbw.math.toVec
import com.github.onlaait.fbw.server.Instance

class RayTargeter(ray: Ray, objects: Iterable<GameObject>, stopOnHitGround: Boolean = true) {


    var ray: Ray = ray
    var objects: Iterable<GameObject> = objects
    var stopOnHitGround = stopOnHitGround

    var distanceToGround: Double? = null
        private set

    fun target(): List<Hit> {
        val ray = ray.copy() // TODO: clone으로
        println("origin: ${ray.origin} direction: ${ray.direction} objs: ${objects.count()}")

        val p = mutableListOf<Hit>()

        if (stopOnHitGround) {
            val distGrnd = BlockUtils.getTargetBlockIntersection(ray.origin.toVec(), ray.direction.toVec(), ray.maxDistance.toDouble(), Instance.instance)
            if (distGrnd != null && ray.maxDistance > distGrnd)
                ray.maxDistance = distGrnd.toFloat()

            distanceToGround = distGrnd
        }

        for (obj in objects) {
            val distHitboxSet = mutableListOf<Pair<Float, Hitbox>>()
            for (hitbox in obj.hitbox) {
                val dist = ray.intersect(hitbox.shape) ?: continue
                distHitboxSet += Pair(dist, hitbox)
            }
            if (distHitboxSet.isEmpty()) continue
            val nearest = distHitboxSet.minBy { it.first }
            p += Hit(obj, nearest.first, nearest.second.isHead)
        }

        return p.sortedBy { it.distance }
    }

    data class Hit(val obj: GameObject, val distance: Float, val isHead: Boolean)
}
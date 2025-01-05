package com.github.onlaait.fbw.game.targeter

import com.github.onlaait.fbw.game.obj.Hittable
import com.github.onlaait.fbw.game.utils.BlockUtils
import com.github.onlaait.fbw.game.utils.Hitbox
import com.github.onlaait.fbw.geometry.Ray
import com.github.onlaait.fbw.math.toVec
import com.github.onlaait.fbw.server.Instance

object RayTargeter {

    fun target(ray: Ray, objects: Iterable<Hittable>, stopOnHitGround: Boolean = true): HitResult {
        var ray = ray
        println("origin: ${ray.origin} direction: ${ray.direction} objs: ${objects.count()}")

        val hits = mutableListOf<Hit>()

        val distGrnd =
            if (stopOnHitGround) {
                BlockUtils.getTargetBlockIntersection(
                    ray.origin.toVec(),
                    ray.direction.toVec(),
                    ray.maxDistance.toDouble(),
                    Instance.instance
                ).also {
                    if (it != null && ray.maxDistance > it) {
                        ray = ray.copy()
                        ray.maxDistance = it.toFloat()
                    }
                }
            } else {
                null
            }

        for (obj in objects) {
            val distHitboxSet = mutableListOf<Pair<Float, Hitbox.Element>>()
            for (hitbox in obj.hitbox.get()) {
                val dist = ray.intersect(hitbox.shape) ?: continue
                distHitboxSet += Pair(dist, hitbox)
            }
            if (distHitboxSet.isEmpty()) continue
            val nearest = distHitboxSet.minBy { it.first }
            hits += Hit(obj, nearest.first, nearest.second.critical)
        }

        return HitResult(hits.sortedBy { it.distance }, distGrnd)
    }

    data class HitResult(val hits: List<Hit>, val distanceToGround: Double?)

    data class Hit(val obj: Hittable, val distance: Float, val critical: Boolean)
}
package com.github.onlaait.fbw.geometry

import com.github.onlaait.fbw.math.Vec3f
import com.github.onlaait.fbw.math.toVec3f
import com.github.onlaait.fbw.physx.PxCylinderGeometry
import com.github.onlaait.fbw.physx.createPxRaycastHit
import com.github.onlaait.fbw.physx.createPxTransform
import com.github.onlaait.fbw.physx.toPxVec3
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec
import org.lwjgl.system.MemoryStack
import physx.geometry.PxBoxGeometry
import physx.geometry.PxGeometry
import physx.geometry.PxGeometryQuery
import physx.physics.PxHitFlags

class Ray(var origin: Vec3f, var direction: Vec3f, var maxDistance: Float = MAX_DIST) {

    constructor(origin: Point, direction: Vec, maxDistance: Float = MAX_DIST) : this(origin.toVec3f(), direction.toVec3f(), maxDistance)

    companion object {
        const val MAX_DIST = 280f
        val HIT_FLAGS = PxHitFlags(0)
    }

    fun intersect(shape: Shape): Float? {
        MemoryStack.stackPush().use { mem ->
            val geom: PxGeometry = when (shape) {
                is Cylinder -> PxCylinderGeometry.createAt(mem, shape.height, shape.radius)
                is Box -> PxBoxGeometry.createAt(mem, MemoryStack::nmalloc, shape.hx, shape.hy, shape.hz)
                else -> return null
            }
            val hit = mem.createPxRaycastHit()
            PxGeometryQuery.raycast(
                origin.toPxVec3(mem),
                direction.toPxVec3(mem),
                geom,
                mem.createPxTransform(shape.origin, shape.rotation),
                maxDistance,
                HIT_FLAGS,
                1,
                hit
            )
//            println(PxHitFlagEnum.entries.joinToString(" ") { "${it.name}:${it.value}" })
//            println(PxHitFlagEnum.entries.filter { DEFAULT_HIT_FLAGS.isSet(it) }.map { it.name })
//            println("${hit.distance} ${hit.position.toVec3f()} ${hit.actor} ${hit.shape} ${hit.normal.toVec3f()} ${hit.faceIndex} ${hit.u} ${hit.v}")
            return hit.distance.let { if (it <= maxDistance) it else null }
        }
    }

    fun copy(): Ray {
        return Ray(origin.clone() as Vec3f, direction.clone() as Vec3f, maxDistance)
    }
}
package com.github.onlaait.fbw.geometry

import com.github.onlaait.fbw.math.Vec3f
import com.github.onlaait.fbw.physx.PxCylinderGeometry
import com.github.onlaait.fbw.physx.createPxRaycastHit
import com.github.onlaait.fbw.physx.createPxTransform
import com.github.onlaait.fbw.physx.toPxVec3
import org.lwjgl.system.MemoryStack
import physx.geometry.PxBoxGeometry
import physx.geometry.PxGeometry
import physx.geometry.PxGeometryQuery
import physx.physics.PxHitFlags

class Ray(var origin: Vec3f, var direction: Vec3f, var maxDistance: Float) {

    private companion object {
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

    fun copy(): Ray = Ray(origin, direction, maxDistance)
}
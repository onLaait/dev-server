package com.github.onlaait.fbw.physx

import com.github.onlaait.fbw.math.Vec3f
import org.lwjgl.system.MemoryStack
import physx.PxTopLevelFunctions
import physx.common.PxVec3
import physx.cooking.PxConvexFlagEnum
import physx.geometry.PxConvexMesh
import physx.geometry.PxConvexMeshGeometry
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object PxCylinderGeometry {

    private val UNIT_CYLINDER = makePxConvexMesh(convexMeshPoints(1f, 1f))

    fun createAt(mem: MemoryStack, length: Float, radius: Float): PxConvexMeshGeometry {
        return PxConvexMeshGeometry(UNIT_CYLINDER, mem.createPxMeshScale(Vec3f(length, radius, radius)))
    }

    private fun convexMeshPoints(length: Float, radius: Float, n: Int = 10): List<Vec3f> {
        val points = mutableListOf<Vec3f>()
        for (i in 0 until n) {
            val a = i * 2f * PI.toFloat() / n
            val y = cos(a) * radius
            val z = sin(a) * radius
            points += Vec3f(length * -0.5f, y, z)
            points += Vec3f(length * 0.5f, y, z)
        }
        return points
    }

    private fun makePxConvexMesh(points: List<Vec3f>): PxConvexMesh = MemoryStack.stackPush().use { mem ->
        val vec3Vector = points.toVector_PxVec3()
        val desc = mem.createPxConvexMeshDesc()
        desc.flags.raise(PxConvexFlagEnum.eCOMPUTE_CONVEX)
        desc.flags.raise(PxConvexFlagEnum.eFAST_INERTIA_COMPUTATION)
        desc.points.count = points.size
        desc.points.stride = PxVec3.SIZEOF
        desc.points.data = vec3Vector.data()

        val pxConvexMesh = PxTopLevelFunctions.CreateConvexMesh(PxManager.cookingParams, desc)
        vec3Vector.destroy()

        pxConvexMesh
    }
}
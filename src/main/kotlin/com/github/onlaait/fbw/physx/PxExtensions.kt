package com.github.onlaait.fbw.physx

import com.github.onlaait.fbw.math.Quatf
import com.github.onlaait.fbw.math.Vec3f
import org.lwjgl.system.MemoryStack
import physx.common.*
import physx.cooking.PxConvexFlags
import physx.cooking.PxConvexMeshDesc
import physx.cooking.PxTriangleMeshDesc
import physx.extensions.PxRevoluteJointFlags
import physx.geometry.*
import physx.physics.*
import physx.support.Vector_PxVec3

fun PxQuat.identity(): PxQuat { x = 0f; y = 0f; z = 0f; w = 1f; return this }
fun PxQuat.toQuatf(result: Quatf = Quatf()) = result.set(x, y, z, w)
fun PxQuat.set(q: Quatf): PxQuat { x = q.x; y = q.y; z = q.z; w = q.w; return this }
fun Quatf.toPxQuat(result: PxQuat) = result.set(this)
fun Quatf.toPxQuat(mem: MemoryStack) = mem.createPxQuat().set(this)

fun PxVec3.toVec3f(result: Vec3f = Vec3f()) = result.set(x, y, z)
fun PxVec3.set(v: Vec3f): PxVec3 { x = v.x; y = v.y; z = v.z; return this }
fun Vec3f.toPxVec3(result: PxVec3) = result.set(this)
fun Vec3f.toPxVec3(mem: MemoryStack) = mem.createPxVec3().set(this)

fun List<Vec3f>.toVector_PxVec3(): Vector_PxVec3 {
    val vector = Vector_PxVec3(size)
    forEachIndexed { i, v -> v.toPxVec3(vector.at(i)) }
    return vector
}

fun MemoryStack.createPxArticulationDrive() = PxArticulationDrive.createAt(this, MemoryStack::nmalloc)
fun MemoryStack.createPxArticulationLimit(low: Float, high: Float) =
    PxArticulationLimit.createAt(this, MemoryStack::nmalloc, low, high)
fun MemoryStack.createPxBoundedData() = PxBoundedData.createAt(this, MemoryStack::nmalloc)
fun MemoryStack.createPxFilterData() = PxFilterData.createAt(this, MemoryStack::nmalloc)
fun MemoryStack.createPxFilterData(w0: Int, w1: Int, w2: Int, w3: Int) =
    PxFilterData.createAt(this, MemoryStack::nmalloc, w0, w1, w2, w3)
fun MemoryStack.createPxHeightFieldSample() = PxHeightFieldSample.createAt(this, MemoryStack::nmalloc)
fun MemoryStack.createPxHullPolygon() = PxHullPolygon.createAt(this, MemoryStack::nmalloc)
fun MemoryStack.createPxMeshScale(s: PxVec3, r: PxQuat) = PxMeshScale.createAt(this, MemoryStack::nmalloc, s, r)
fun MemoryStack.createPxMeshScale(s: Vec3f, r: Quatf = Quatf().identity()) =
    PxMeshScale.createAt(this, MemoryStack::nmalloc, s.toPxVec3(createPxVec3()), r.toPxQuat(createPxQuat()))
fun MemoryStack.createPxVec3() = PxVec3.createAt(this, MemoryStack::nmalloc)
fun MemoryStack.createPxVec3(x: Float, y: Float, z: Float) = PxVec3.createAt(this, MemoryStack::nmalloc, x, y, z)

fun MemoryStack.createPxQuat() = PxQuat.createAt(this, MemoryStack::nmalloc)
fun MemoryStack.createPxQuat(x: Float, y: Float, z: Float, w: Float) =
    PxQuat.createAt(this, MemoryStack::nmalloc, x, y, z, w)

fun MemoryStack.createPxTransform() = PxTransform.createAt(this, MemoryStack::nmalloc, PxIDENTITYEnum.PxIdentity)
fun MemoryStack.createPxTransform(p: PxVec3, q: PxQuat) = PxTransform.createAt(this, MemoryStack::nmalloc, p, q)
fun MemoryStack.createPxTransform(p: Vec3f, q: Quatf) = PxTransform.createAt(this, MemoryStack::nmalloc, p.toPxVec3(this), q.toPxQuat(this))

fun MemoryStack.createPxConvexMeshDesc() = PxConvexMeshDesc.createAt(this, MemoryStack::nmalloc)
fun MemoryStack.createPxHeightFieldDesc() = PxHeightFieldDesc.createAt(this, MemoryStack::nmalloc)
fun MemoryStack.createPxTriangleMeshDesc() = PxTriangleMeshDesc.createAt(this, MemoryStack::nmalloc)

fun MemoryStack.createPxActorFlags(flags: Int) = PxActorFlags.createAt(this, MemoryStack::nmalloc, flags.toByte())
fun MemoryStack.createPxBaseFlags(flags: Int) = PxBaseFlags.createAt(this, MemoryStack::nmalloc, flags.toShort())
fun MemoryStack.createPxConvexFlags(flags: Int) = PxConvexFlags.createAt(this, MemoryStack::nmalloc, flags.toShort())
fun MemoryStack.createPxConvexMeshGeometryFlags(flags: Int) = PxConvexMeshGeometryFlags.createAt(this, MemoryStack::nmalloc, flags.toByte())
fun MemoryStack.createPxHitFlags(flags: Short) = PxHitFlags.createAt(this, MemoryStack::nmalloc, flags.toShort())
fun MemoryStack.createPxMeshGeometryFlags(flags: Int) = PxMeshGeometryFlags.createAt(this, MemoryStack::nmalloc, flags.toByte())
fun MemoryStack.createPxRevoluteJointFlags(flags: Int) = PxRevoluteJointFlags.createAt(this, MemoryStack::nmalloc, flags.toShort())
fun MemoryStack.createPxRigidBodyFlags(flags: Int) = PxRigidBodyFlags.createAt(this, MemoryStack::nmalloc, flags.toByte())
fun MemoryStack.createPxRigidDynamicLockFlags(flags: Int) = PxRigidDynamicLockFlags.createAt(this, MemoryStack::nmalloc, flags.toByte())
fun MemoryStack.createPxSceneFlags(flags: Int) = PxSceneFlags.createAt(this, MemoryStack::nmalloc, flags)
fun MemoryStack.createPxShapeFlags(flags: Int) = PxShapeFlags.createAt(this, MemoryStack::nmalloc, flags.toByte())

fun MemoryStack.createPxRaycastHit() = PxRaycastHit.createAt(this, MemoryStack::nmalloc)
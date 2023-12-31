package com.github.onlaait.fbw.math

import net.minestom.server.coordinate.Vec
import org.joml.*

typealias Vec2f = Vector2f
typealias Vec2d = Vector2d
typealias Vec2i = Vector2i

typealias Vec3f = Vector3f
typealias Vec3d = Vector3d
typealias Vec3i = Vector3i

typealias Quatf = Quaternionf
typealias Quatd = Quaterniond

typealias Mat2f = Matrix2f
typealias Mat2d = Matrix2d

typealias Mat3f = Matrix3f
typealias Mat3d = Matrix3d

typealias Mat4f = Matrix4f
typealias Mat4d = Matrix4d

operator fun Vec3f.plus(other: Vec3f): Vec3f = this.add(other, Vec3f())
operator fun Vec3f.minus(other: Vec3f): Vec3f = this.sub(other, Vec3f())
operator fun Vec3f.times(other: Vec3f): Vec3f = this.mul(other, Vec3f())

operator fun Vec3d.plus(other: Vec3d): Vec3d = this.add(other, Vec3d())
operator fun Vec3d.minus(other: Vec3d): Vec3d = this.sub(other, Vec3d())
operator fun Vec3d.times(other: Vec3d): Vec3d = this.mul(other, Vec3d())

operator fun Quatf.plus(other: Quatf): Quatf = this.add(other, Quatf())
operator fun Quatf.times(other: Quatf): Quatf = this.mul(other, Quatf())

fun Vec3f.toVec(): Vec  = Vec(x.toDouble(), y.toDouble(), z.toDouble())
fun Vec3d.toVec(): Vec  = Vec(x, y, z)
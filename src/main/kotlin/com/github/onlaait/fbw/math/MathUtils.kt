package com.github.onlaait.fbw.math

import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import kotlin.math.PI
import kotlin.math.sqrt

const val PI_F = PI.toFloat()

const val DEG_2_RAD = PI / 180.0
const val RAD_2_DEG = 180.0 / PI
const val DEG_2_RAD_F = DEG_2_RAD.toFloat()
const val RAD_2_DEG_F = RAD_2_DEG.toFloat()

fun Float.toDeg() = this * RAD_2_DEG_F
fun Float.toRad() = this * DEG_2_RAD_F
fun Double.toDeg() = this * RAD_2_DEG
fun Double.toRad() = this * DEG_2_RAD

operator fun Pos.plus(value: Double): Pos = this.add(value)

operator fun Pos.plus(pos: Pos): Pos = this.add(pos)

operator fun Pos.plus(vec: Vec): Pos = this.add(vec)

operator fun Pos.minus(value: Double): Pos = this.sub(value)

operator fun Pos.minus(pos: Pos): Pos = this.sub(pos)

operator fun Pos.times(value: Double): Pos = this.mul(value)

operator fun Pos.times(pos: Pos): Pos = this.mul(pos)

operator fun Vec.plus(value: Double): Vec = this.add(value)

operator fun Vec.plus(vec: Vec): Vec = this.add(vec)

operator fun Vec.minus(value: Double): Vec = this.sub(value)

operator fun Vec.minus(vec: Vec): Vec = this.sub(vec)

operator fun Vec.times(value: Double): Vec = this.mul(value)

operator fun Vec.times(vec: Vec): Vec = this.mul(vec)

fun Point.toVec3f(): Vec3f = Vec3f(x().toFloat(), y().toFloat(), z().toFloat())

fun Vec.toVec3f(): Vec3f = Vec3f(x.toFloat(), y.toFloat(), z.toFloat())

fun Point.toVec3d(): Vec3d = Vec3d(x(), y(), z())

fun Vec.toVec3d(): Vec3d = Vec3d(x, y, z)

fun fixYaw(yaw: Float): Float {
    var yaw = yaw % 360
    if (yaw < -180) {
        yaw += 360
    } else if (yaw > 180) {
        yaw -= 360
    }
    return yaw
}

fun squaredMagnitude(a: Double, b: Double, c: Double): Double {
    return a * a + b * b + c * c
}

fun magnitude(a: Double, b: Double, c: Double): Double {
    return sqrt(squaredMagnitude(a, b, c))
}
object Utils {

    object Vec3f {
        val X_AXIS = Vec3f(1f, 0f, 0f)
        val Y_AXIS = Vec3f(0f, 1f, 0f)
        val Z_AXIS = Vec3f(0f, 0f, 1f)
        val NEG_X_AXIS = Vec3f(-1f, 0f, 0f)
        val NEG_Y_AXIS = Vec3f(0f, -1f, 0f)
        val NEG_Z_AXIS = Vec3f(0f, 0f, -1f)
    }

    object Vec3d {
        val X_AXIS = Vec3d(1.0, 0.0, 0.0)
        val Y_AXIS = Vec3d(0.0, 1.0, 0.0)
        val Z_AXIS = Vec3d(0.0, 0.0, 1.0)
        val NEG_X_AXIS = Vec3d(-1.0, 0.0, 0.0)
        val NEG_Y_AXIS = Vec3d(0.0, -1.0, 0.0)
        val NEG_Z_AXIS = Vec3d(0.0, 0.0, -1.0)
    }

}
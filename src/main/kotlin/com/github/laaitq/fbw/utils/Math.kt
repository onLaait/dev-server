package com.github.laaitq.fbw.utils

import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import org.joml.Vector3d

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

fun Vec.toVector3d(): Vector3d {
    return Vector3d(x, y, z)
}

fun Pos.toVector3d(): Vector3d {
    return Vector3d(x, y, z)
}
package com.github.onlaait.fbw.geometry

import com.github.onlaait.fbw.math.Quatf
import com.github.onlaait.fbw.math.Vec3f
import com.github.onlaait.fbw.math.toRad
import com.github.onlaait.fbw.math.toVec3f
import net.minestom.server.coordinate.Point

class Cylinder(
    override var origin: Vec3f,
    override var rotation: Quatf = DEFAULT_ROTATION,
    var radius: Float,
    var height: Float
) : Shape {

    constructor(
        origin: Point,
        rotation: Quatf = DEFAULT_ROTATION,
        radius: Float,
        height: Float
    ) : this(origin.toVec3f(), rotation, radius, height)

    companion object {
        private val DEFAULT_ROTATION: Quatf = Quatf().rotationZ(90f.toRad())
    }
}
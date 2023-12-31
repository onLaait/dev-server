package com.github.onlaait.fbw.geometry

import com.github.onlaait.fbw.math.Quatf
import com.github.onlaait.fbw.math.Vec3f

class Box(
    override var origin: Vec3f,
    override var rotation: Quatf = Quatf(),
    var hx: Float,
    var hy: Float,
    var hz: Float
) : Shape
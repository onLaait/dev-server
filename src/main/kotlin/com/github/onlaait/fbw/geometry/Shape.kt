package com.github.onlaait.fbw.geometry

import com.github.onlaait.fbw.math.Quatf
import com.github.onlaait.fbw.math.Vec3f

interface Shape {
    var origin: Vec3f
    var rotation: Quatf
}
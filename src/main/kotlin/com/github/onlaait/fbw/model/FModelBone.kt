package com.github.onlaait.fbw.model

import com.github.onlaait.fbw.math.Vec3d

interface FModelBone {

    val extraRotation: Vec3d
    val extraOffset: Vec3d
}
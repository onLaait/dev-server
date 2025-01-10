package com.github.onlaait.fbw.model

import kotlin.math.min

class LimbAnimator {
    private var prevSpeed = 0f
    private var speed = 0f
    private var pos = 0f
    private var scale = 1f

    fun updateLimbs(speed: Float, multiplier: Float, scale: Float) {
        this.prevSpeed = this.speed
        this.speed += (speed - this.speed) * multiplier
        this.pos += this.speed
        this.scale = scale
    }

    fun reset() {
        prevSpeed = 0f
        speed = 0f
        pos = 0f
    }

    fun getSpeed(tickDelta: Float): Float = min(lerp(tickDelta, prevSpeed, speed), 1f)

    fun getPos(): Float = pos * scale

    fun getPos(tickDelta: Float): Float = (pos - speed * (1.0f - tickDelta)) * scale

    fun isLimbMoving(): Boolean = speed > 1.0E-5f

    private companion object {
        fun lerp(delta: Float, start: Float, end: Float): Float = start + delta * (end - start)
    }
}
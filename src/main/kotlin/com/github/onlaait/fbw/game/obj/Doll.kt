package com.github.onlaait.fbw.game.obj

import com.github.onlaait.fbw.entity.FPlayer
import com.github.onlaait.fbw.game.utils.Hitbox
import com.github.onlaait.fbw.geometry.Box
import com.github.onlaait.fbw.geometry.Cylinder
import com.github.onlaait.fbw.math.*
import com.github.onlaait.fbw.model.PlayerModel
import net.minestom.server.coordinate.Pos

class Doll(var player: FPlayer) : Agent() {

    override val caster = this

    override val maxHp: Int
        get() = TODO("Not yet implemented")

    override var hp: Double
        get() = TODO("Not yet implemented")
        set(value) {}

    override var pos: Pos
        get() = position
        set(value) {
            teleport(value)
            if (syncPosition) player.teleport(value)
        }

    var syncPosition = true
        private set

    fun enableSyncPosition(affectPlayer: Boolean = true) {
        if (syncPosition) return
        syncPosition = true
        setNoGravity(true)
        if (affectPlayer) {
            player.teleport(pos)
            player.velocity = velocity
        }
    }

    fun disableSyncPosition() {
        setNoGravity(false)
        velocity = player.getRealVelocity()
    }

    override val hitbox = Hitbox {
        val pos = position.toVec3f()

        val bodyH = 1.40625f
        val body = Cylinder(pos + Vec3f(0f, bodyH * 0.5f, 0f), radius = 0.5f, height = bodyH)

        val headRot = Quatf()
            .rotateYXZ(-position.yaw.toRad(), position.pitch.toRad(), 0f)
            .normalize()

        val headTopO = Mat4f()
            .setTranslation(pos + Vec3f(0f, 1.772625f, 0f))
            .rotateAround(headRot, 0f, -0.366375f, 0f)
            .getTranslation(Vec3f())
        val headTop = Box(headTopO, headRot, 0.264f, 0.132f, 0.264f)

        val headBottomO = Mat4f()
            .setTranslation(pos + Vec3f(0f, 1.508625f, 0f))
            .rotateAround(headRot, 0f, -0.102375f, 0f)
            .getTranslation(Vec3f())
        val headBottom = Box(headBottomO, headRot, 0.264f, 0.132f, 0.264f)

        arrayOf(
            Hitbox.Element(body),
            Hitbox.Element(headBottom),
            Hitbox.Element(headTop, critical = true)
        )
    }

    override fun getPov() = player.getPov()

    val model = PlayerModel(this, player.skin!!)




    // Minestom Entity

    override fun onSpawn() {
        model.start()
    }

    override fun onDespawn() {
        model.stop()
    }

    override fun movementClientTick() {
        if (!syncPosition) super.movementClientTick()
        model.run {
            updateState()
            updateLimbs()
        }
    }

//    override fun updateNewViewer(player: Player) {}
}
package com.github.onlaait.fbw.game.obj

import com.github.onlaait.fbw.entity.FPlayer
import com.github.onlaait.fbw.game.character.Character
import com.github.onlaait.fbw.game.skill.SkillHolder
import com.github.onlaait.fbw.game.utils.Hitbox
import com.github.onlaait.fbw.game.weapon.WeaponHolder
import com.github.onlaait.fbw.geometry.Box
import com.github.onlaait.fbw.geometry.Cylinder
import com.github.onlaait.fbw.math.*
import com.github.onlaait.fbw.model.PlayerModel
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player

class Doll(var player: FPlayer, val character: Character) : Agent() {

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

    override val audiences = mutableListOf(player)

    override val weaponHolder = WeaponHolder(this, character.weapons)
    override val skillHolder = SkillHolder(this)


    var model: PlayerModel

    init {
        skillHolder.skillMap.putAll(character.skills)
        model = PlayerModel(this, player.headProfile, player.isSlim, character.modelId)
    }




    // Minestom Entity

    override fun onSpawn() {
        model.init(instance, pos)
        viewers.forEach { model.addViewer(it) }
    }

    override fun onDespawn() {
        model.destroy()
    }

    override fun movementClientTick() {
        if (!syncPosition) super.movementClientTick()
        model.run {
            updateState()
            updateLimbs()
        }
    }

    override fun updateNewViewer(player: Player) {
        model.addViewer(player)
    }

    override fun updateOldViewer(player: Player) {
        model.removeViewer(player)
    }
}
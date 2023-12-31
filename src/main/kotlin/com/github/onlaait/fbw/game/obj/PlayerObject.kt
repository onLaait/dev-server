package com.github.onlaait.fbw.game.obj

import com.github.onlaait.fbw.game.Hitbox
import com.github.onlaait.fbw.geometry.Box
import com.github.onlaait.fbw.geometry.Cylinder
import com.github.onlaait.fbw.math.*
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import org.joml.AxisAngle4f

class PlayerObject(var player: Player) : GameObject {
    override var isPenetrable = false
    override var isPenetrableByTeammate = true

    lateinit var viewers: Set<Player>

    override var position: Pos
        get() {
            return player.position
        }
        set(pos) {
            player.teleport(pos)
        }
    override val hitbox: Set<Hitbox>
        get() {
            val pos = position.toVec3f()

            val height = 1.40625f
            val body = Cylinder(pos + Vec3f(0f, height * 0.5f, 0f), radius = 0.5f, height = height)

            val transform = Mat4f()
                .setTranslation(pos + Vec3f(0f, 1.77785f, 0f))
                .rotateAround(
                    Quatf(AxisAngle4f(position.yaw.toRad(), Vec3f(0f, -1f, 0f)))
                            * Quatf(AxisAngle4f(position.pitch.toRad(), Vec3f(1f, 0f, 0f))),
                    0f, -0.3716f, 0f
                )
            val box = Box(
                transform.getTranslation(Vec3f()),
                transform.getNormalizedRotation(Quatf()),
                0.264f, 0.12785f, 0.264f
            )

            return setOf(
                Hitbox(body),
                Hitbox(box, isHead = true)
            )
        }
}
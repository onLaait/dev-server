package com.github.onlaait.fbw.game.obj

import com.github.onlaait.fbw.game.Hitbox
import com.github.onlaait.fbw.geometry.Box
import com.github.onlaait.fbw.geometry.Cylinder
import com.github.onlaait.fbw.math.*
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player

class PlayerObject(var player: Player) : GameObject {

    override var isPenetrable = false
    override var isPenetrableByTeammate = true

    lateinit var viewers: Set<Player>

    override var position: Pos
        get() = player.position
        set(pos) {
            player.teleport(pos)
        }

    override val hitbox: Array<Hitbox>
        get() {
            val pos = position.toVec3f()

            val height = 1.40625f
            val body = Cylinder(pos + Vec3f(0f, height * 0.5f, 0f), radius = 0.5f, height = height)

            val rot = Quatf().rotateYXZ(-position.yaw.toRad(), position.pitch.toRad(), 0f).normalize()

            val t1 = Mat4f()
                .setTranslation(pos + Vec3f(0f, 1.772625f, 0f))
                .rotateAround(
                    rot,
                    0f, -0.366375f, 0f
                )
            val headTop = Box(
                t1.getTranslation(Vec3f()),
                rot,
                0.264f, 0.132f, 0.264f
            )

            val t2 = Mat4f()
                .setTranslation(pos + Vec3f(0f, 1.508625f, 0f))
                .rotateAround(
                    rot,
                    0f, -0.102375f, 0f
                )
            val headBottom = Box(
                t2.getTranslation(Vec3f()),
                rot,
                0.264f, 0.132f, 0.264f
            )

            return arrayOf(
                Hitbox(body),
                Hitbox(headBottom),
                Hitbox(headTop, isHead = true)
            )
        }
}
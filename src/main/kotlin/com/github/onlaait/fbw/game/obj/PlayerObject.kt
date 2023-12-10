package com.github.onlaait.fbw.game.obj

import com.github.onlaait.fbw.command.OBB
import com.github.onlaait.fbw.command.intersectRayOBB
import com.github.onlaait.fbw.game.attack.Ray
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import org.joml.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class PlayerObject(var player: Player): GameObject {
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

    override fun intersect(ray: Ray): Intersection? {
        val pos = position
        val origin = ray.origin
        val dir = ray.direction

        // 전방으로 한정
        if (dir.dot(pos.withY { y -> y + 0.9375 }.sub(origin).asVec()) < 0) return null

        // 원기둥: 옆면
        val x = origin.sub(pos).asVec()
        val a = dir.dot(dir) - dir.y.pow(2)
        val b = dir.dot(x) - dir.y * x.y
        val c = x.dot(x) - x.y.pow(2) - 0.2025
        val discriminant = b.pow(2) - a * c
        if (discriminant < 0) return null
        val sqrtDiscriminant = sqrt(discriminant)
        val p = mutableListOf<Double>()
        listOf(((-1) * b + sqrtDiscriminant) / a, ((-1) * b - sqrtDiscriminant) / a).forEach {
            if (dir.y * it + x.y in 0.0..1.65) {
                p += it
            }
        }
        // 원기둥: 밑면
        val l = mutableListOf<Double>()
        if (x.y >= 0 && dir.y < 0 || x.y < 0 && dir.y > 0) {
            l += (-1) * x.y / dir.y
        }
        if ((pos.y + 1.65 - origin.y) >= 0 && dir.y > 0 || (pos.y + 1.65 - origin.y) < 0 && dir.y < 0) {
            l += (pos.y + 1.65 - origin.y) / dir.y
        }
        if (l.size != 0) {
            val lmin = l.min()
            val v = origin.add(dir.mul(lmin))
            if (abs(v.x - pos.x) <= 0.45 && abs(v.z - pos.z) <= 0.45) {
                p += lmin
            }
        }

        // OBB
        val transform = Matrix4d()
            .setTranslation(Vector3d(pos.x, pos.y + 1.77785, pos.z))
            .rotateAround(
                Quaterniond(AxisAngle4d(Math.toRadians(pos.yaw.toDouble()), Vector3d(0.0, -1.0, 0.0)))
                    .mul(Quaterniond(AxisAngle4d(Math.toRadians(pos.pitch.toDouble()), Vector3d(1.0, 0.0, 0.0)))),
                0.0, -0.3716, 0.0
            )
        val intersectionHead = intersectRayOBB(
            ray,
            OBB(transform, Vector3d(0.264, 0.12785, 0.264))
        )
        if (intersectionHead != null) {
            p += intersectionHead
        }

        if (p.isEmpty()) return null
        val intersection = p.min()
        var isHead = false
        if (intersection == intersectionHead) {
            isHead = true
        }

        return Intersection(intersection, isHead)
    }
}
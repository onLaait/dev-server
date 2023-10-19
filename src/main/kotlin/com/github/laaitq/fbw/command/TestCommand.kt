package com.github.laaitq.fbw.command

import com.github.laaitq.fbw.Instance
import com.github.laaitq.fbw.game.attack.Ray
import com.github.laaitq.fbw.game.obj.GameObject
import com.github.laaitq.fbw.game.obj.PlayerObject
import com.github.laaitq.fbw.game.utils.showOneDust
import com.github.laaitq.fbw.utils.AudienceUtils.broadcast
import com.github.laaitq.fbw.utils.AudienceUtils.sendMsg
import com.github.laaitq.fbw.utils.toVector3d
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.particle.Particle
import net.minestom.server.particle.ParticleCreator
import net.minestom.server.sound.SoundEvent
import net.minestom.server.timer.TaskSchedule
import org.joml.*
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.math.*

object TestCommand : Command("test") {
    init {
        val num1 = ArgumentType.Integer("arg1")
        val num2 = ArgumentType.Double("arg2").setDefaultValue(1.0)

        addSyntax({ sender, context ->
            when (context[num1]) {
                1 -> {
                    println("test")
                    sender.playSound(Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_PLING, Sound.Source.MASTER, 1f, 2f))
                }

                2 -> {
                    val player = sender as Player
                    val pos = player.position.add(0.0, 1.62, 0.0)
                    var posP = pos
                    val dir = player.position.direction()
                    val dirP = dir.div(2.0)
                    repeat(100) {
                        posP = posP.add(dirP)
                        player.sendPacketToViewersAndSelf(ParticleCreator.createParticlePacket(
                            Particle.DUST,
                            true,
                            posP.x,
                            posP.y,
                            posP.z,
                            0f,
                            0f,
                            0f,
                            0f,
                            1
                        ) { binaryWriter ->
                            binaryWriter.writeFloat(0.4745098f)
                            binaryWriter.writeFloat(0.19607843f)
                            binaryWriter.writeFloat(0.7882353f)
                            binaryWriter.writeFloat(0.3f)
                        })
                    }
                    for (target in MinecraftServer.getConnectionManager().onlinePlayers) {
                        if (target != player) {
                            val targetPos = target.position
                            if (dir.dot(targetPos.withY { y -> y + 0.9375 }.sub(pos).asVec()) < 0) continue
                            val x = pos.sub(targetPos).asVec()
                            val a = dir.dot(dir) - dir.y.pow(2)
                            val b = dir.dot(x) - dir.y * x.y
                            val c = x.dot(x) - x.y.pow(2) - 0.1225
                            val discriminant = b.pow(2) - a * c
                            if (discriminant >= 0) {
                                val sqrtDiscriminant = sqrt(discriminant)
                                val p = mutableListOf<Double>()
                                listOf(((-1) * b + sqrtDiscriminant) / a, ((-1) * b - sqrtDiscriminant) / a).forEach {
                                    if (dir.y * it + x.y in 0.0..1.65) {
                                        p += it
                                    }
                                }
                                // 밑면
                                val l = mutableListOf<Double>()
                                if (x.y >= 0 && dir.y < 0 || x.y < 0 && dir.y > 0) {
                                    l += (-1) * x.y / dir.y
                                }
                                if ((targetPos.y + 1.65 - pos.y) >= 0 && dir.y > 0 || (targetPos.y + 1.65 - pos.y) < 0 && dir.y < 0) {
                                    l += (targetPos.y + 1.65 - pos.y) / dir.y
                                }
                                if (l.size != 0) {
                                    val lmin = l.min()
                                    val v = pos.add(dir.mul(lmin))
                                    if (abs(v.x - targetPos.x) <= 0.35 && abs(v.z - targetPos.z) <= 0.35) {
                                        p += lmin
                                    }
                                }
                                if (p.size != 0) {
                                    val point = pos.add(dir.mul(p.min()))
                                    println("$p ${point.y - targetPos.y}")
                                    broadcast(Component.text("${target.username} 명중"))
                                    player.sendPacketToViewersAndSelf(ParticleCreator.createParticlePacket(
                                        Particle.DUST,
                                        true,
                                        point.x,
                                        point.y,
                                        point.z,
                                        0f,
                                        0f,
                                        0f,
                                        0f,
                                        50
                                    ) { binaryWriter ->
                                        binaryWriter.writeFloat(1f)
                                        binaryWriter.writeFloat(1f)
                                        binaryWriter.writeFloat(1f)
                                        binaryWriter.writeFloat(1f)
                                    })
                                }
                            }
                        }
                    }
                }

                3 -> {
                    MinecraftServer.getSchedulerManager().buildTask {
                        sender.sendMsg("a")
                    }.delay(TaskSchedule.tick(1)).schedule()
                }

                4 -> {
                    val player = sender as Player
                    val pos = player.position
                    val dir = player.position.direction().mul(8.0)
                    val entity = Entity(EntityType.ARMOR_STAND)
                    entity.setInstance(Instance.instance, player.position)
                    entity.setNoGravity(true)
                    entity.spawn()
                    entity.teleport(pos.add(dir))
                    repeat(100) {
                        MinecraftServer.getSchedulerManager().buildTask {
                            entity.teleport(pos.add(dir.mul(it + 1.0)))
                        }.delay(TaskSchedule.tick(it + 1)).schedule()
                    }
                }

                5 -> {
                    val player = sender as Player
                    val entity = Entity(EntityType.ARMOR_STAND)
                    entity.setInstance(Instance.instance, player.position)
                    entity.setNoGravity(true)
                    entity.spawn()
                }

                6 -> {
                    val shooter = sender as Player
                    val targetObjs = hashSetOf<GameObject>()
                    for (player in MinecraftServer.getConnectionManager().onlinePlayers) {
                        if (player != shooter) {
                            targetObjs.add(PlayerObject(player))
                        }
                    }
                    var pos = shooter.position.add(0.0, 1.62, 0.0)
                    val ray = Ray().apply {
                        targets = targetObjs
                        origin = pos
                        direction = shooter.position.direction()
                    }
                    val rayCastResult = ray.cast()
                    val distances = mutableSetOf<Double>()
                    if (rayCastResult.objWithIntersection.isNotEmpty()) {
                        for (e in rayCastResult.objWithIntersection) {
                            if (e.obj is PlayerObject) {
                                distances += e.intersection.distance
                                sender.playSound(
                                    Sound.sound(
                                        SoundEvent.BLOCK_NOTE_BLOCK_SNARE,
                                        Sound.Source.MASTER,
                                        1f,
                                        1.5f
                                    ), pos
                                )
                                if (e.intersection.isHead) {
                                    sender.playSound(
                                        Sound.sound(
                                            SoundEvent.ENTITY_ARROW_HIT_PLAYER,
                                            Sound.Source.MASTER,
                                            1f,
                                            2f
                                        ), pos
                                    )
                                }
                            }
                            break
                        }
                    } else if (rayCastResult.distanceToGround != null) {
                        distances += rayCastResult.distanceToGround
                        sender.playSound(
                            Sound.sound(SoundEvent.BLOCK_GLASS_BREAK, Sound.Source.MASTER, 1f, 2f),
                            ray.origin.add(ray.direction.mul(rayCastResult.distanceToGround))
                        )
                    } else {
                        distances += ray.maxDistance
                    }
                    repeat(distances.min().toInt()) {
                        pos = pos.add(ray.direction)
                        showOneDust(252, 140, 255, pos)
                    }

                }

                7 -> {
                    val player = sender as Player
                    var pos = player.position.add(0.0, 1.62, 0.0)
                    var v = player.position.direction().mul(context[num2])
                    thread {
                        val entity = Entity(EntityType.ARMOR_STAND)
                        entity.setInstance(Instance.instance, pos)
                        entity.setNoGravity(true)
                        entity.spawn()
                        repeat(400) {
                            v = v.mul(0.999).withY { y -> y - 0.12 }
                            pos = pos.add(v)
                            println(entity.position.distance(pos))
                            entity.teleport(pos)
                            showOneDust(252, 140, 255, pos)
                            sleep(50)
                        }
                        entity.remove()
                    }
                }

                8 -> { // 헤드 반두께: 0.1125
                    val player = sender as Player
                    var t = player
                    for (target in MinecraftServer.getConnectionManager().onlinePlayers) {
                        if (target != player) {
                            t = target
                            break
                        }
                    }

                    var pos = t.position
                    val transform = Matrix4d()
                        .setTranslation(Vector3d(pos.x, pos.y + 1.7625, pos.z))
                        .rotateAround(
                            Quaterniond(AxisAngle4d(Math.toRadians(pos.yaw.toDouble()), Vector3d(0.0, -1.0, 0.0)))
                                .mul(Quaterniond(AxisAngle4d(Math.toRadians(pos.pitch.toDouble()), Vector3d(1.0, 0.0, 0.0)))),
                            0.0, -0.35625, 0.0
                        )
                    println("T ${transform.getTranslation(Vector3d())}")
                    for (rX in -10..10) {
                        for (rY in -10..10) {
                            for (rZ in -10..10) {
                                val ray = Ray().apply {
                                    origin = player.position.add(0.0, 1.62, 0.0)
                                    direction = player.position.direction().rotate(rX.toDouble(), rY.toDouble(), rZ.toDouble())
                                }
                                val intersection = intersectRayOBB(
                                    ray,
                                    OBB(transform, Vector3d(0.234375, 0.1125, 0.234375))
                                )
                                if (intersection != null) {
                                    showOneDust(255, 255, 255, 0.5f, ray.origin.add(ray.direction.mul(intersection)))
                                    println(intersection)
                                }
                            }
                        }
                    }
                }
            }
        }, num1, num2)
    }
}

fun intersectRayOBB(ray: Ray, obb: OBB): Double? {
    var tMin = 0.0
    var tMax = Double.POSITIVE_INFINITY

    val dir = ray.direction.toVector3d()
    val delta: Vector3d = obb.transform.getTranslation(Vector3d()).sub(ray.origin.toVector3d())

    for (i in 0..2) {
        val axis = obb.transform.getColumn(i, Vector3d())
        val e = axis.dot(delta)
        val f = dir.dot(axis)

        if (abs(f) > 0.0001) {
            var t1 = (e - obb.extents[i]) / f
            var t2 = (e + obb.extents[i]) / f

            if (t1 > t2) {
                t1 = t2.also { t2 = t1 }
            }

            if (t2 < tMax)
                tMax = t2
            if (t1 > tMin)
                tMin = t1

            if (tMax < tMin) return null
        } else {
            if (-e - obb.extents[i] > 0 || -e + obb.extents[i] < 0) return null
        }
    }
    return tMin
}

fun intersectRayOBBLegacy(ray: Ray, obb: OBB): Double? {
    val inverse = obb.transform.invert()
    val localRayOrigin = ray.origin.toVector3d().mulPosition(inverse)
    val localRayDir = ray.direction.toVector3d().mulDirection(inverse)

    val (tmin, tmax) = obb.extents.let {
        val t1 = (it.x - localRayOrigin.x) / localRayDir.x
        val t2 = (-it.x - localRayOrigin.x) / localRayDir.x
        val t3 = (it.y - localRayOrigin.y) / localRayDir.y
        val t4 = (-it.y - localRayOrigin.y) / localRayDir.y
        val t5 = (it.z - localRayOrigin.z) / localRayDir.z
        val t6 = (-it.z - localRayOrigin.z) / localRayDir.z

        Pair(max(max(min(t1, t2), min(t3, t4)), min(t5, t6)),
            min(min(max(t1, t2), max(t3, t4)), max(t5, t6)))
    }

    if (tmax < 0 || tmin > tmax) return null

    return tmin
}
class OBB(val transform: Matrix4d, val extents: Vector3d)
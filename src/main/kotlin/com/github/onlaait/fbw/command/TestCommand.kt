package com.github.onlaait.fbw.command

import com.github.onlaait.fbw.game.obj.PlayerObject
import com.github.onlaait.fbw.game.targeter.RayTargeter
import com.github.onlaait.fbw.game.utils.showOneDust
import com.github.onlaait.fbw.geometry.Ray
import com.github.onlaait.fbw.server.Instance
import com.github.onlaait.fbw.system.OpSystem.isOp
import com.github.onlaait.fbw.utils.AudienceUtils.sendMsg
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.attribute.Attribute
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentWord
import net.minestom.server.command.builder.arguments.number.ArgumentDouble
import net.minestom.server.command.builder.arguments.number.ArgumentInteger
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.potion.Potion
import net.minestom.server.potion.PotionEffect
import net.minestom.server.scoreboard.Sidebar
import net.minestom.server.scoreboard.Sidebar.ScoreboardLine
import net.minestom.server.sound.SoundEvent
import net.minestom.server.timer.TaskSchedule
import java.lang.Thread.sleep
import kotlin.concurrent.thread

object TestCommand : Command("test") {
    init {
        setCondition { sender, _ -> sender.isOp }

        val argWord = ArgumentWord("word")
        val argInt1 = ArgumentInteger("int1")
        val argDouble1 = ArgumentDouble("double1")
        val argDouble2 = ArgumentDouble("double2")
        val argDouble3 = ArgumentDouble("double3")

        setDefaultExecutor { sender, _ ->
            sender.sendMsg("?")
        }

        addSyntax({ sender, _ ->
            sender.sendMsg("test")
            sender.playSound(Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_PLING, Sound.Source.MASTER, 1f, 2f))
        })

        addSyntax({ sender, context ->
            when (context[argWord]) {
                "throw_as" -> {
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

                "spawn_as" -> {
                    val player = sender as Player
                    val entity = Entity(EntityType.ARMOR_STAND)
                    entity.setInstance(Instance.instance, player.position)
                    entity.setNoGravity(true)
                    entity.spawn()
                }

                "ray" -> {
                    val shooter = sender as Player
                    val targetObjs = MinecraftServer.getConnectionManager().onlinePlayers.filter { it != shooter }.map { PlayerObject(it) }

                    var pos = shooter.position.withY { it + shooter.eyeHeight }
                    val dir = pos.direction()
                    val maxDist = 50f
                    val targeter = RayTargeter(
                        Ray(pos, dir, maxDist),
                        targetObjs
                    )
                    val targets = targeter.target()
                    val dist: Float
                    if (targets.isNotEmpty()) {
                        val target = targets.first()
                        dist = target.distance
                        val sound = if (!target.isHead) {
                            Sound.sound(
                                SoundEvent.BLOCK_NOTE_BLOCK_SNARE,
                                Sound.Source.MASTER,
                                1f,
                                1.5f
                            )
                        } else {
                            Sound.sound(
                                SoundEvent.ENTITY_ARROW_HIT_PLAYER,
                                Sound.Source.MASTER,
                                1f,
                                2f
                            )
                        }
                        sender.playSound(sound, pos)
                    } else {
                        val distToGrnd = targeter.distanceToGround
                        if (distToGrnd == null) {
                            dist = maxDist
                        } else {
                            if (distToGrnd <= maxDist) {
                                sender.playSound(
                                    Sound.sound(SoundEvent.BLOCK_GLASS_BREAK, Sound.Source.MASTER, 1f, 2f),
                                    pos.add(dir.mul(distToGrnd))
                                )
                            }
                            dist = distToGrnd.toFloat()
                        }
                    }

                    repeat(dist.toInt()) {
                        pos = pos.add(dir)
                        showOneDust(252, 140, 255, pos)
                    }
                }
                "lag" -> {
                    sleep(10000)
                }
                "haste" -> {
                    val player = sender as Player
                    player.getAttribute(Attribute.ATTACK_SPEED).baseValue = 1024F
                    player.addEffect(Potion(PotionEffect.HASTE, Byte.MAX_VALUE, Int.MAX_VALUE))
                }
            }
        }, argWord)

        addSyntax({ sender, context ->
            when (context[argWord]) {
                "schedule" -> {
                    if (sender !is Player) return@addSyntax
                    repeat(context[argInt1]) { i ->
                        MinecraftServer.getSchedulerManager().buildTask {
                            sender.sendMsg("$i")
                            sender.playSound(Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_PLING, Sound.Source.MASTER, 1f, 2f))
                       }.delay(if (i == 0) TaskSchedule.immediate() else TaskSchedule.millis((i*50).toLong())).schedule()
                    }
                }
                "sidebar" -> {
                    val sidebar = Sidebar(Component.text("사이드바"))
                    sidebar.createLine(ScoreboardLine("a",
                        Component.text("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
                            .color(TextColor.color(context[argInt1], 0, 0)),
                        0))
                    sidebar.addViewer(sender as Player)
                }
            }
        }, argWord, argInt1)

        addSyntax({ sender, context ->
            when (context[argWord]) {
                "projectile" -> {
                    val player = sender as Player
                    var pos = player.position.add(0.0, 1.62, 0.0)
                    var v = player.position.direction().mul(context[argDouble1])
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
            }
        }, argWord, argDouble1)

        addSyntax({ sender, context ->
            when (context[argWord]) {
                "velocity" -> { // 최댓값 ≒ 410
                    if (sender !is Player) return@addSyntax
                    repeat(context[argInt1]) { i ->
                        MinecraftServer.getSchedulerManager().buildTask {
                            sender.velocity = Vec(context[argDouble1], context[argDouble2], context[argDouble3])
                        }.delay(if (i == 0) TaskSchedule.immediate() else TaskSchedule.millis((i*50).toLong())).schedule()
                    }
                }
            }
        }, argWord, argDouble1, argDouble2, argDouble3, argInt1)
    }
}
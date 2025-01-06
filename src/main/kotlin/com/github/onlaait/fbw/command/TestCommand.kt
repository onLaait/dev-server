package com.github.onlaait.fbw.command

import com.github.onlaait.fbw.entity.DUIBlock
import com.github.onlaait.fbw.game.movement.CannotStep
import com.github.onlaait.fbw.game.movement.CannotStepOrRotate
import com.github.onlaait.fbw.game.utils.showOneDust
import com.github.onlaait.fbw.math.Vec2d
import com.github.onlaait.fbw.server.FPlayer
import com.github.onlaait.fbw.server.Instance
import com.github.onlaait.fbw.server.scheduleManager
import com.github.onlaait.fbw.system.OpSystem.isOp
import com.github.onlaait.fbw.utils.editMeta
import com.github.onlaait.fbw.utils.seconds
import com.github.onlaait.fbw.utils.sendMsg
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentWord
import net.minestom.server.command.builder.arguments.number.ArgumentDouble
import net.minestom.server.command.builder.arguments.number.ArgumentInteger
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.metadata.display.ItemDisplayMeta
import net.minestom.server.item.ItemComponent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.scoreboard.Sidebar
import net.minestom.server.scoreboard.Sidebar.ScoreboardLine
import net.minestom.server.sound.SoundEvent
import net.minestom.server.timer.TaskSchedule
import java.lang.Thread.sleep

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
            val p = sender as FPlayer
            when (context[argWord]) {
                "throw_as" -> {
                    val pos = p.position
                    val dir = p.position.direction().mul(8.0)
                    val entity = Entity(EntityType.ARMOR_STAND)
                    entity.setNoGravity(true)
                    entity.setInstance(Instance.instance, p.position)
                    entity.teleport(pos.add(dir))
                    repeat(100) {
                        MinecraftServer.getSchedulerManager().buildTask {
                            entity.teleport(pos.add(dir.mul(it + 1.0)))
                        }.delay(TaskSchedule.tick(it + 1)).schedule()
                    }
                }

                "spawn_as" -> {
                    val e = Entity(EntityType.ARMOR_STAND)
                    e.setNoGravity(true)
                    e.setInstance(Instance.instance, p.position)
                }

                "lag" -> {
                    sleep(10000)
                }
                "attr" -> {
                    p.getAttribute(Attribute.ATTACK_SPEED).baseValue = 1024.0
                    p.getAttribute(Attribute.BLOCK_BREAK_SPEED).baseValue = 0.0
                    p.getAttribute(Attribute.BLOCK_INTERACTION_RANGE).baseValue = 64.0
                    p.getAttribute(Attribute.ENTITY_INTERACTION_RANGE).baseValue = 0.0
                    p.getAttribute(Attribute.MAX_HEALTH).baseValue = 2.0
                    p.getAttribute(Attribute.SNEAKING_SPEED).baseValue = 0.78
                }
                "dp" -> {
                    val e = DUIBlock()
                    e.color = java.awt.Color(255, 192, 203, 255)
                    e.scale = Vec2d(0.1, 0.1)
                    e.setInstance(Instance.instance, p.position)
                }
                "item" -> {
                    val i = ItemStack.of(Material.SHIELD).builder()
                        .customName(Component.empty())
                        .set(ItemComponent.HIDE_TOOLTIP)
                        .set(ItemComponent.ITEM_MODEL, "air")
                        .build()
                    p.inventory.setItemStack(6, i)
                }
                "interaction" -> {
                    val e = Entity(EntityType.INTERACTION)
                    e.setInstance(Instance.instance, p.position)
                }
                "player" -> {
                    val e = Entity(EntityType.PLAYER)
                    e.setInstance(Instance.instance, p.position)
                }
                "interpolationtest" -> {
                    val e = Entity(EntityType.ITEM_DISPLAY)
                    e.editMeta<ItemDisplayMeta> {
                        itemStack = ItemStack.of(Material.STONE)
                        isHasNoGravity = true
                    }
                    e.setInstance(Instance.instance)
                    scheduleManager.buildTask {
                        e.teleport(Pos(-1.0, 1.0, -1.0))
                    }.repeat(TaskSchedule.tick(4)).delay(TaskSchedule.tick(1))
                        .schedule()
                    scheduleManager.buildTask {
                        e.teleport(Pos(1.0, 1.0, -1.0))
                    }.repeat(TaskSchedule.tick(4)).delay(TaskSchedule.tick(2))
                        .schedule()
                    scheduleManager.buildTask {
                        e.teleport(Pos(1.0, 1.0, 1.0))
                    }.repeat(TaskSchedule.tick(4)).delay(TaskSchedule.tick(3))
                        .schedule()
                    scheduleManager.buildTask {
                        e.teleport(Pos(-1.0, 1.0, 1.0))
                    }.repeat(TaskSchedule.tick(4)).delay(TaskSchedule.tick(4))
                        .schedule()
                }
                "rooted" -> {
                    val m = CannotStep()
                    p.movement.apply(m)
                    scheduleManager.buildTask {
                        p.movement.remove(m)
                    }.delay(5.0.seconds)
                        .schedule()
                }
                "stun" -> {
                    val m = CannotStepOrRotate()
                    p.movement.apply(m)
                    scheduleManager.buildTask {
                        p.movement.remove(m)
                    }.delay(2.0.seconds)
                        .schedule()
                }
            }
        }, argWord)

        addSyntax({ sender, context ->
            val p = sender as Player
            when (context[argWord]) {
                "schedule" -> {
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
                    sidebar.addViewer(p)
                }
            }
        }, argWord, argInt1)

        addSyntax({ sender, context ->
            val p = sender as FPlayer
            when (context[argWord]) {
                "projectile" -> {
                    var pos = p.position.add(0.0, 1.62, 0.0)
                    var v = p.position.direction().mul(context[argDouble1])
                    val entity = Entity(EntityType.ITEM_DISPLAY)
                    (entity.entityMeta as ItemDisplayMeta).run {
                        itemStack = ItemStack.of(Material.STONE)
                    }
                    entity.setNoGravity(true)
                    entity.setInstance(Instance.instance, pos)
                    MinecraftServer.getSchedulerManager().buildTask {
                        v = v.mul(0.999).withY { it - 0.12 }
                        pos = pos.add(v)
//                        Logger.debug { entity.position.distance(pos) }
                        entity.teleport(pos)
                        showOneDust(252, 140, 255, pos)
                    }
                        .repeat(TaskSchedule.nextTick())
                        .schedule()
                }
                "speed" -> {
                    p.changeMovementSpeed(context[argDouble1].toFloat())
                }
            }
        }, argWord, argDouble1)

        addSyntax({ sender, context ->
            val p = sender as Player
            when (context[argWord]) {
                "velocity" -> { // 최댓값 ≒ 410
                    repeat(context[argInt1]) { i ->
                        MinecraftServer.getSchedulerManager().buildTask {
                            p.velocity = Vec(context[argDouble1], context[argDouble2], context[argDouble3])
                        }.delay(if (i == 0) TaskSchedule.immediate() else TaskSchedule.millis((i*50).toLong())).schedule()
                    }
                }
            }
        }, argWord, argDouble1, argDouble2, argDouble3, argInt1)
    }
}
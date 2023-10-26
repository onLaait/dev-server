package com.github.laaitq.fbw.command

import com.github.laaitq.fbw.command.argument.ArgumentSound
import com.github.laaitq.fbw.system.OpSystem.isOp
import com.github.laaitq.fbw.utils.AudienceUtils.sendMsg
import com.github.laaitq.fbw.utils.CommandUtils.usage
import net.kyori.adventure.sound.Sound
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentWord
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import net.minestom.server.command.builder.arguments.number.ArgumentFloat
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec3
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Player
import net.minestom.server.utils.entity.EntityFinder
import net.minestom.server.utils.location.RelativeVec


object PlaysoundCommand : Command("playsound") {
    init {
        setCondition { sender, _ -> sender.isOp }

        setDefaultExecutor { sender, context ->
            sender.sendMsg(usage("${context.commandName} <sound> [source] [targets] [pos] [volume] [pitch]"))
        }

        val argSound = ArgumentSound("sound")
        val argSource = ArgumentWord("source")
            .from(*Sound.Source.values().map { it.name.lowercase() }.toTypedArray())
            .setDefaultValue("master")
        val argTargets = ArgumentEntity("targets")
            .onlyPlayers(true)
            .setDefaultValue(EntityFinder().setTargetSelector(EntityFinder.TargetSelector.ALL_PLAYERS))
        val argPos = ArgumentRelativeVec3("pos")
            .setDefaultValue(RelativeVec(Vec(0.0, 0.0, 0.0), RelativeVec.CoordinateType.RELATIVE, true, true, true))
        val argVolume = ArgumentFloat("volume")
            .setDefaultValue(1F)
        val argPitch = ArgumentFloat("pitch")
            .setDefaultValue(1F)

        addSyntax({ sender, context ->
            val sound = Sound.sound(
                context[argSound],
                Sound.Source.values().find { it.name == context[argSource].uppercase() }!!,
                context[argVolume],
                context[argPitch]
            )
            val pos = context[argPos].fromSender(sender)
            context[argTargets].find(sender).filterIsInstance<Player>().forEach {
                it.playSound(sound, pos)
            }
        }, argSound, argSource, argTargets, argPos, argVolume, argPitch)
    }
}
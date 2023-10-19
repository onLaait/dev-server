package com.github.laaitq.fbw.game.utils

import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.particle.Particle
import net.minestom.server.particle.ParticleCreator

fun showDust(
    r: Int,
    g: Int,
    b: Int,
    size: Float,
    offsetX: Float,
    offsetY: Float,
    offsetZ: Float,
    count: Int,
    pos: Pos,
    viewers: MutableCollection<Player>
) {
    val particlepacket =
        ParticleCreator.createParticlePacket(
            Particle.DUST,
            true,
            pos.x,
            pos.y,
            pos.z,
            offsetX,
            offsetY,
            offsetZ,
            0f,
            count
        ) { binaryWriter ->
            binaryWriter.writeFloat(r.toFloat() / 255)
            binaryWriter.writeFloat(g.toFloat() / 255)
            binaryWriter.writeFloat(b.toFloat() / 255)
            binaryWriter.writeFloat(size)
        }
    viewers.forEach {
        it.sendPacket(particlepacket)
    }
}

fun showOneDust(
    r: Int,
    g: Int,
    b: Int,
    size: Float = 1f,
    pos: Pos,
    viewers: MutableCollection<Player> = MinecraftServer.getConnectionManager().onlinePlayers
) {
    showDust(r, g, b, size, 0f, 0f, 0f, 1, pos, viewers)
}

fun showOneDust(
    r: Int,
    g: Int,
    b: Int,
    pos: Pos,
    viewers: MutableCollection<Player> = MinecraftServer.getConnectionManager().onlinePlayers
) {
    showOneDust(r, g, b, 1f, pos, viewers)
}
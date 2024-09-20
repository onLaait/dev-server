package com.github.onlaait.fbw.game.utils

import net.minestom.server.MinecraftServer
import net.minestom.server.color.Color
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.play.ParticlePacket
import net.minestom.server.particle.Particle

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
    viewers: Iterable<Player>
) {
    val packet =
        ParticlePacket(
            Particle.DUST.withProperties(Color(r, g, b), size),
            true,
            pos.x,
            pos.y,
            pos.z,
            offsetX,
            offsetY,
            offsetZ,
            0f,
            count
        )
    viewers.forEach {
        it.sendPacket(packet)
    }
}

fun showOneDust(
    r: Int,
    g: Int,
    b: Int,
    size: Float,
    pos: Pos,
    viewers: Iterable<Player> = MinecraftServer.getConnectionManager().onlinePlayers
) {
    showDust(r, g, b, size, 0f, 0f, 0f, 1, pos, viewers)
}

fun showOneDust(
    r: Int,
    g: Int,
    b: Int,
    pos: Pos,
    viewers: Iterable<Player> = MinecraftServer.getConnectionManager().onlinePlayers
) {
    showOneDust(r, g, b, 1f, pos, viewers)
}
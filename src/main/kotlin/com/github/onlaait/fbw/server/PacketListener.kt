package com.github.onlaait.fbw.server

import com.github.onlaait.fbw.entity.FPlayer
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.client.ClientPacket
import net.minestom.server.network.packet.client.play.ClientInteractEntityPacket
import net.minestom.server.network.packet.client.play.ClientUseItemPacket

object PacketListener {

    val manager = MinecraftServer.getPacketListenerManager()

    init {
        setPlayListener<ClientInteractEntityPacket> { packet, p ->
        }

        setPlayListener<ClientUseItemPacket> { _, p ->
        }


    }

    inline fun <reified T : ClientPacket> setPlayListener(noinline consumer: (T, FPlayer) -> Unit) {
/*        val new: (T, Player) -> Unit = { a, b ->
            consumer(a, b as FPlayer)
        }*/
        manager.setPlayListener(T::class.java, consumer as (T, Player) -> Unit)
    }
}
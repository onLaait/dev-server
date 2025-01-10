package com.github.onlaait.fbw.server

import com.github.onlaait.fbw.entity.FPlayer
import com.github.onlaait.fbw.event.PlayerLClickEvent
import com.github.onlaait.fbw.event.PlayerRClickEvent
import com.github.onlaait.fbw.server.Event.handler
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.client.ClientPacket
import net.minestom.server.network.packet.client.play.ClientInteractEntityPacket
import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket
import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket.Status.*
import net.minestom.server.network.packet.client.play.ClientUseItemPacket

object PacketListener {

    val manager = MinecraftServer.getPacketListenerManager()

    init {
        setPlayListener<ClientPlayerDiggingPacket> { packet, p ->
            when (packet.status) {
                STARTED_DIGGING -> {
//                    Logger.debug { "${ServerStatus.tick} packet L start" }
                    p.mouseInputs.left = true
                    handler.call(PlayerLClickEvent(p))
                }
                CANCELLED_DIGGING, FINISHED_DIGGING -> {
//                    Logger.debug { "${ServerStatus.tick} packet L end" }
                    p.mouseInputs.left = false
                }
                UPDATE_ITEM_STATE -> {
//                    Logger.debug { "${ServerStatus.tick} packet R end" }
                    p.mouseInputs.right = false
                }
                else -> {}
            }
        }

        setPlayListener<ClientInteractEntityPacket> { packet, p ->
            if (packet.type is ClientInteractEntityPacket.Attack) {
//                Logger.debug { "${ServerStatus.tick} packet L end" }
                handler.call(PlayerLClickEvent(p))
            }
        }

        setPlayListener<ClientUseItemPacket> { _, p ->
//            Logger.debug { "${ServerStatus.tick} packet R start" }
            p.mouseInputs.right = true
            handler.call(PlayerRClickEvent(p))
        }

    }

    inline fun <reified T : ClientPacket> setPlayListener(noinline consumer: (T, FPlayer) -> Unit) {
/*        val new: (T, Player) -> Unit = { a, b ->
            consumer(a, b as FPlayer)
        }*/
        manager.setPlayListener(T::class.java, consumer as (T, Player) -> Unit)
    }
}
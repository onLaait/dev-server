package com.github.laaitq.fbw

import com.github.laaitq.fbw.system.*
import com.github.laaitq.fbw.system.BanSystem.kickIfBanned
import com.github.laaitq.fbw.system.PlayerData.playerdata
import com.github.laaitq.fbw.system.Whitelist.kickIfNotWhitelisted
import com.github.laaitq.fbw.utils.AudienceUtils.broadcast
import com.github.laaitq.fbw.utils.PlayerUtils.sendTabList
import com.github.laaitq.fbw.utils.TextUtils
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.player.*
import net.minestom.server.event.server.ServerListPingEvent
import net.minestom.server.event.server.ServerTickMonitorEvent
import net.minestom.server.network.packet.client.play.ClientChatSessionUpdatePacket
import net.minestom.server.network.packet.client.play.ClientSetRecipeBookStatePacket
import net.minestom.server.ping.ResponseData
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import javax.imageio.ImageIO


object Listener {
    init {
        val event = MinecraftServer.getGlobalEventHandler()
        val packet = MinecraftServer.getPacketListenerManager()

        val responseData = ResponseData().apply {
            description = LegacyComponentSerializer.legacySection().deserialize(ServerProperties.MOTD)
            maxPlayer = ServerProperties.MAX_PLAYERS
            File("server-icon.png").let { file ->
                val inputStream = if (file.exists()) {
                    file.inputStream()
                } else {
                    this.javaClass.classLoader.getResourceAsStream("server-icon.png")!!
                }
                val img = ImageIO.read(inputStream).getScaledInstance(64, 64, Image.SCALE_SMOOTH)
                val bufferedImg = BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB)
                bufferedImg.graphics.run {
                    drawImage(img, 0, 0, null)
                    dispose()
                }
                val base64Str = ByteArrayOutputStream().use { outputStream ->
                    ImageIO.write(bufferedImg, "png", outputStream)
                    Base64.getEncoder().encodeToString(outputStream.toByteArray())
                }
                favicon = "data:image/png;base64,$base64Str"
            }
        }

        event.addListener(ServerListPingEvent::class.java) { e -> e.responseData = responseData }

        event.addListener(AsyncPlayerPreLoginEvent::class.java) { e ->
            val player = e.player
            Logger.info("UUID of player ${player.username} is ${player.uuid}")
            if (OpSystem.opPlayersData.find { it.uuid == player.uuid } != null) {
                player.addPermission(OpSystem.opPermission)
            } else if (ServerProperties.WHITE_LIST) {
                player.kickIfNotWhitelisted()
            }
            player.kickIfBanned()
        }

        event.addListener(PlayerLoginEvent::class.java) { e ->
            val player = e.player
            Logger.info("${player.username}[${player.playerConnection.remoteAddress}] logged in")
            e.setSpawningInstance(Instance.instance)
            player.respawnPoint = Pos(0.5, 1.0, 0.5)
            player.playerdata.let {
                if (it.lastKnownName != player.username) {
                    it.lastKnownName = player.username
                    PlayerData.write(player)
                }
            }
            responseData.online = MinecraftServer.getConnectionManager().onlinePlayers.size
            responseData.refreshEntries()
            player.sendTabList()
            broadcast(TextUtils.formatText("<green><bold>●</bold><white> ${player.username}"))
            /*val hpBar = Entity(EntityType.TEXT_DISPLAY)
            hpBar.setNoGravity(true)
            (hpBar.entityMeta as TextDisplayMeta).run {
                text = Component.text("ABCDEF")
                billboardRenderConstraints = AbstractDisplayMeta.BillboardConstraints.CENTER

            }
            hpBar.instance = Instance.instance
            val armorStand = Entity(EntityType.ARMOR_STAND)
            armorStand.setNoGravity(true)
            (armorStand.entityMeta as ArmorStandMeta).run {
                isSmall = true
                isHasNoBasePlate = true
            }
            armorStand.instance = Instance.instance
            armorStand.addPassenger(hpBar)
            MinecraftServer.getSchedulerManager().scheduleNextTick {
                player.addPassenger(armorStand)
            }
            MinecraftServer.getSchedulerManager().submitTask {
                armorStand.setView(player.position.yaw, 0F)
                return@submitTask TaskSchedule.tick(1)
            }*/
        }

        event.addListener(PlayerDisconnectEvent::class.java) { e ->
            val player = e.player
            Logger.info("${player.username} lost connection")
            responseData.online = MinecraftServer.getConnectionManager().onlinePlayers.size
            responseData.refreshEntries()
            broadcast(TextUtils.formatText("<gray><bold>●</bold><white> ${player.username}"))
        }

        event.addListener(PlayerChatEvent::class.java) { e ->
            val player = e.player
            val chat = "<${player.username}> ${e.message}"
            e.setChatFormat { Component.text(chat) }
            Logger.info(chat)
        }

        event.addListener(PlayerCommandEvent::class.java) { e ->
            Logger.info("${e.player.username} issued server command: /${e.command}")
        }

        /*var javaClass: String
        globalEvent.addListener(PlayerPacketEvent::class.java) { e ->
            javaClass = e.packet.javaClass.name.toString()
            if (javaClass != "net.minestom.server.network.packet.client.play.ClientPlayerPositionPacket"
                && javaClass != "net.minestom.server.network.packet.client.play.ClientPlayerRotationPacket"
                && javaClass != "net.minestom.server.network.packet.client.play.ClientKeepAlivePacket"
                && javaClass != "net.minestom.server.network.packet.client.play.ClientPlayerPositionAndRotationPacket") {
                val player: Player = e.player
                println(e.packet)
            }
        }

        event.addListener(PlayerPacketOutEvent::class.java) { e ->
            val javaClass = e.packet.javaClass.name.toString()
            if (javaClass != "net.minestom.server.network.packet.server.play.PlayerListHeaderAndFooterPacket"
                && javaClass != "net.minestom.server.network.packet.server.play.UnloadChunkPacket"
                && javaClass != "net.minestom.server.network.packet.server.play.KeepAlivePacket"
                && javaClass != "net.minestom.server.network.packet.server.play.ChunkDataPacket"
                && javaClass != "net.minestom.server.network.packet.server.play.PlayerInfoPacket"
                && javaClass != "net.minestom.server.network.packet.server.play.TimeUpdatePacket") {
                println("<- ${e.packet}")
            }
        }*/

        event.addListener(PlayerHandAnimationEvent::class.java) { e ->
            MinecraftServer.getCommandManager().execute(e.player, "test 6")
        }

        event.addListener(PlayerDeathEvent::class.java) { e ->
            e.chatMessage = null
            e.deathText = null
        }

        event.addListener(ServerTickMonitorEvent::class.java) { e ->
            ServerStatus.TPS.onTick(e.tickMonitor.tickTime)
        }

        packet.setListener(ClientSetRecipeBookStatePacket::class.java) { _, _ -> }
        packet.setListener(ClientChatSessionUpdatePacket::class.java) { _, _ -> }
    }

    private fun ResponseData.refreshEntries() {
        clearEntries()
        addEntries(MinecraftServer.getConnectionManager().onlinePlayers)
    }
}
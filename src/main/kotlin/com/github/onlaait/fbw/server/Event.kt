package com.github.onlaait.fbw.server

import com.github.onlaait.fbw.event.PlayerLClickEvent
import com.github.onlaait.fbw.event.PlayerRClickEvent
import com.github.onlaait.fbw.game.GameManager
import com.github.onlaait.fbw.game.event.ObjDamageEvent
import com.github.onlaait.fbw.game.obj.Doll
import com.github.onlaait.fbw.game.skill.ExampleSkill
import com.github.onlaait.fbw.game.utils.GameUtils
import com.github.onlaait.fbw.system.BanSystem.kickIfBanned
import com.github.onlaait.fbw.system.OpSystem.isOp
import com.github.onlaait.fbw.system.PlayerData
import com.github.onlaait.fbw.system.ServerProperties
import com.github.onlaait.fbw.system.ServerStatus
import com.github.onlaait.fbw.system.ServerStatus.sendTabList
import com.github.onlaait.fbw.system.Whitelist.kickIfNotWhitelisted
import com.github.onlaait.fbw.utils.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.minestom.server.MinecraftServer
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.event.EventNode
import net.minestom.server.event.entity.EntityDamageEvent
import net.minestom.server.event.player.*
import net.minestom.server.event.server.ServerListPingEvent
import net.minestom.server.event.server.ServerTickMonitorEvent
import net.minestom.server.network.packet.client.common.ClientKeepAlivePacket
import net.minestom.server.network.packet.client.play.*
import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket.Status.*
import net.minestom.server.network.packet.server.common.DisconnectPacket
import net.minestom.server.network.packet.server.common.KeepAlivePacket
import net.minestom.server.network.packet.server.login.LoginDisconnectPacket
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket
import net.minestom.server.network.packet.server.play.PlayerListHeaderAndFooterPacket
import net.minestom.server.network.packet.server.play.SetTickStatePacket
import java.util.regex.Pattern

val eventHandler = MinecraftServer.getGlobalEventHandler()

object Event {
    init {
        val event = eventHandler
        val packet = MinecraftServer.getPacketListenerManager()

        event.addListener(ServerListPingEvent::class.java) { e ->
            if (!ServerProperties.ENABLE_STATUS) {
                e.isCancelled = true
                return@addListener
            }
            e.responseData = ServerUtils.responseData
        }

        event.addListener(AsyncPlayerPreLoginEvent::class.java) { e ->
            val gameProfile = e.gameProfile
            Logger.info("UUID of player ${gameProfile.name} is ${gameProfile.uuid}")
        }

        event.addListener(AsyncPlayerConfigurationEvent::class.java) { e ->
            val player = e.player

            if (
                !player.isOp &&
                !player.kickIfBanned() &&
                (!ServerProperties.WHITE_LIST || !player.kickIfNotWhitelisted()) &&
                allPlayersCount >= ServerProperties.MAX_PLAYERS
            ) {
                player.kick(Component.translatable("multiplayer.disconnect.server_full"))
            }

            Logger.info("${player.username}[${player.playerConnection.remoteAddress}] logged in with (entityId=${player.entityId},serverAddress=${player.playerConnection.serverAddress},locale=${player.settings.locale},viewDistance=${player.settings.viewDistance})")
            player.respawnPoint = Pos(0.5, 1.0, 0.5)
            e.spawningInstance = Instance.instance
//            player.setReducedDebugScreenInformation(true)

            if (player.data.lastKnownName == player.username) {
                broadcast(formatText("<green><bold>●</bold><white> ${player.username}"))
            } else {
                broadcast(formatText("<green><bold>●</bold><white> ${player.username}<gray>(${player.data.lastKnownName})"))
                player.data.lastKnownName = player.username
            }

            Audiences.players().sendTabList()
            ServerUtils.refreshResponse()

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

        val setTickStatePacket = SetTickStatePacket(40f, false)
        event.addListener(PlayerSpawnEvent::class.java) { e ->
            val player = e.player as FPlayer
            Logger.info("PlayerSpawnEvent $player/${e.entity}/${e.instance}/${e.isFirstSpawn}")
            player.sendPacket(setTickStatePacket)

            val doll = Doll(player)
            player.doll = doll
            GameManager.objs += doll
        }

        event.addListener(PlayerDisconnectEvent::class.java) { e ->
            val player = e.player as FPlayer
            Logger.info("${player.username} lost connection")
            broadcast(formatText("<gray><bold>●</bold><white> ${player.username}"))
            ServerUtils.refreshResponse()
            PlayerData.store(player)

            GameManager.objs -= player.doll!!
        }


        val urlSerializer = LegacyComponentSerializer.builder()
            .extractUrls(
                Pattern.compile(
                    "https?://([a-zA-Z0-9가-힣-]+\\.)+([a-zA-Z]{2,}|한국)(/[a-zA-Z0-9-_.~!*'();:@&=+$,/?%#\\[\\]]*)?"
                ),
                Style.style(TextDecoration.UNDERLINED)
                    .color(NamedTextColor.BLUE)
            )
            .build()
        event.addChild(
            EventNode.all("chat").setPriority(0).addListener(PlayerChatEvent::class.java) { e ->
                e.formattedMessage =
                    Component.text("<${e.player.username}> ")
                        .append(urlSerializer.deserialize(e.rawMessage))
                        .colorIfAbsent(NamedTextColor.WHITE)
                        .also { Logger.info(it) }
            }
        )

        event.addListener(PlayerCommandEvent::class.java) { e ->
            Logger.info("${e.player.username} issued server command: /${e.command}")
        }

        event.addListener(PlayerLClickEvent::class.java) { e ->
            println("${Schedule.tick} L event")
            val p = e.player as FPlayer
            ExampleSkill.cast(p.doll!!)
        }

        event.addListener(PlayerRClickEvent::class.java) { e ->
            println("${Schedule.tick} R event")
            val p = e.player as FPlayer
            ExampleSkill.cast(p.doll!!)
        }

        val voidJumper = mutableSetOf<Player>()
        event.addListener(EntityDamageEvent::class.java) { e ->
            e.isCancelled = true
            val entity = e.entity
            Logger.debug { "$entity damaged: ${e.damage}" }
/*            if (entity !is Player) return@addListener
            if (!voidJumper.contains(entity)) {
                voidJumper += entity
                val pos = entity.position
                entity.sendPacket(ExplosionPacket(pos.x, pos.y, pos.z, 0F, byteArrayOf(), 0F, 11F, 0F))
                MinecraftServer.getSchedulerManager().buildTask {
                    voidJumper -= entity
                }.delay(TaskSchedule.millis(500)).schedule()
            }*/
        }

        event.addListener(PlayerPluginMessageEvent::class.java) { e ->
            if (e.identifier != "minecraft:brand") return@addListener
            val brand = e.messageString.let {
                if (!it.first().isLetter()) it.substring(1) else it
            }
            Logger.debug { "${e.player.username} is using client '$brand'" }
            e.player.brand = brand
        }

        event.addListener(PlayerSettingsChangeEvent::class.java) { e ->
            val player = e.player
            Logger.debug { ("${player.username} (locale=${player.settings.locale},viewDistance=${player.settings.viewDistance})") }
        }

        event.addListener(PlayerDeathEvent::class.java) { e ->
            e.chatMessage = null
            e.deathText = null
        }

        event.addListener(PlayerMoveEvent::class.java) { e ->
            val p = e.player as FPlayer
            p.doll?.let { if (it.syncPosition) it.hitbox.refresh() }
        }

        event.addListener(PlayerStartSneakingEvent::class.java) { e ->
            val p = e.player as FPlayer
            p.doll?.let { if (it.syncPosition) it.hitbox.refresh() }
        }

        event.addListener(PlayerStopSneakingEvent::class.java) { e ->
            val p = e.player as FPlayer
            p.doll?.let { if (it.syncPosition) it.hitbox.refresh() }
        }

        event.addListener(ObjDamageEvent::class.java) { e ->
//            e.victim.hp -= e.damage
            e.attacker?.run {
                if (this is Doll) player.run {
                    playSound(
                        if (e.critical) GameUtils.CRITICAL_HIT_SOUND else GameUtils.HIT_SOUND,
                        position.withY { it + eyeHeight }
                    )
                }
            }
        }

        event.addListener(ServerTickMonitorEvent::class.java) { e ->
            ServerStatus.onTick(e.tickMonitor.tickTime)
        }

        val ignoringInPackets =
            arrayOf(
                ClientTickEndPacket::class,
                ClientKeepAlivePacket::class,
                ClientPlayerPositionPacket::class,
                ClientPlayerRotationPacket::class,
                ClientPlayerPositionAndRotationPacket::class,
            )
        event.addListener(PlayerPacketEvent::class.java) { e ->
            val p = e.packet
//            if (!ignoringInPackets.contains(p::class)) println("-> ${Schedule.tick} $p")
        }

        val ignoringOutPackets =
            arrayOf(
                KeepAlivePacket::class,
                PlayerListHeaderAndFooterPacket::class,
                PlayerInfoUpdatePacket::class,
            )
        event.addListener(PlayerPacketOutEvent::class.java) { e ->
            val p = e.packet
//            if (!ignoringOutPackets.contains(p::class)) println("<- $p")
            fun disconnectInfo(kickMessage: Component) {
                Logger.info("Disconnecting ${e.player.username} (${e.player.playerConnection.remoteAddress}): ${kickMessage.render().plainText()}")
            }
            when (p) {
                is DisconnectPacket -> {
                    disconnectInfo(p.message)
                }
                is LoginDisconnectPacket -> {
                    disconnectInfo(p.kickMessage)
                }
                else -> {}
            }
        }

        packet.setPlayListener(ClientPlayerDiggingPacket::class.java) { packet, player ->
            val p = player as FPlayer
            when (packet.status) {
                STARTED_DIGGING -> {
                    println("${Schedule.tick} packet L start")
                    p.mouseInputs.left = true
                    event.call(PlayerLClickEvent(player))
                }
                CANCELLED_DIGGING, FINISHED_DIGGING -> {
                    println("${Schedule.tick} packet L end")
                    p.mouseInputs.left = false
                }
                UPDATE_ITEM_STATE -> {
                    println("${Schedule.tick} packet R end")
                    p.mouseInputs.right = false
                }
                else -> {}
            }
        }

        packet.setPlayListener(ClientInteractEntityPacket::class.java) { packet, player ->
            if (packet.type is ClientInteractEntityPacket.Attack) {
                println("${Schedule.tick} packet L end")
                event.call(PlayerLClickEvent(player))
            }
        }

        packet.setPlayListener(ClientUseItemPacket::class.java) { _, player ->
            println("${Schedule.tick} packet R start")
            val p = player as FPlayer
            p.mouseInputs.right = true
            event.call(PlayerRClickEvent(player))
        }

    }
}
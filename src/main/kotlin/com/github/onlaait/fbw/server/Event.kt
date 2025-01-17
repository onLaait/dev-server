package com.github.onlaait.fbw.server

import com.github.onlaait.fbw.entity.FPlayer
import com.github.onlaait.fbw.event.PlayerKeyInputEvent
import com.github.onlaait.fbw.game.GameManager
import com.github.onlaait.fbw.game.event.ObjDamageEvent
import com.github.onlaait.fbw.game.obj.Doll
import com.github.onlaait.fbw.game.utils.GameUtils
import com.github.onlaait.fbw.system.BanSystem.kickIfBanned
import com.github.onlaait.fbw.system.OpSystem.isOp
import com.github.onlaait.fbw.system.PlayerData
import com.github.onlaait.fbw.system.ServerProperties
import com.github.onlaait.fbw.system.ServerStatusMonitor.sendTabList
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
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.entity.EntityDamageEvent
import net.minestom.server.event.entity.EntityTeleportEvent
import net.minestom.server.event.player.*
import net.minestom.server.event.server.ServerListPingEvent
import net.minestom.server.event.server.ServerTickMonitorEvent
import net.minestom.server.item.component.HeadProfile
import net.minestom.server.network.packet.client.common.ClientKeepAlivePacket
import net.minestom.server.network.packet.client.play.ClientInteractEntityPacket
import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket
import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket.Status.*
import net.minestom.server.network.packet.client.play.ClientTickEndPacket
import net.minestom.server.network.packet.client.play.ClientUseItemPacket
import net.minestom.server.network.packet.server.common.DisconnectPacket
import net.minestom.server.network.packet.server.common.KeepAlivePacket
import net.minestom.server.network.packet.server.login.LoginDisconnectPacket
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket
import net.minestom.server.network.packet.server.play.PlayerListHeaderAndFooterPacket
import net.minestom.server.network.packet.server.play.SetTickStatePacket
import java.util.regex.Pattern

object Event {
    init {
        addListener<ServerListPingEvent> { e ->
            if (!ServerProperties.ENABLE_STATUS) {
                e.isCancelled = true
                return@addListener
            }
            e.responseData = Server.pingResponse
        }

        addListener<AsyncPlayerPreLoginEvent> { e ->
            val gameProfile = e.gameProfile
            Logger.info("UUID of player ${gameProfile.name} is ${gameProfile.uuid}")
        }

        addListener<AsyncPlayerConfigurationEvent> { e ->
            val p = e.player as FPlayer

            if (
                !p.isOp &&
                !p.kickIfBanned() &&
                (!ServerProperties.WHITE_LIST || !p.kickIfNotWhitelisted()) &&
                allPlayersCount >= ServerProperties.MAX_PLAYERS
            ) {
                p.kick(Component.translatable("multiplayer.disconnect.server_full"))
            }

            Logger.info("${p.username}[${p.playerConnection.remoteAddress}] logged in with (entityId=${p.entityId},serverAddress=${p.playerConnection.serverAddress},locale=${p.settings.locale},viewDistance=${p.settings.viewDistance})")
            p.respawnPoint = Pos(0.5, 1.0, 0.5)
            e.spawningInstance = Instance.instance
        }

        val setTickStatePacket = SetTickStatePacket(40f, false)
        addListener<PlayerSpawnEvent> { e ->
            val p = e.player as FPlayer
            Logger.debug { "PlayerSpawnEvent ${p.username}/${e.instance}/${e.isFirstSpawn}" }
            if (e.isFirstSpawn) {
                if (p.data.lastKnownName == p.username) {
                    broadcast(formatText("<green><bold>●</bold><white> ${p.username}"))
                } else {
                    broadcast(formatText("<green><bold>●</bold><white> ${p.username}<gray>(${p.data.lastKnownName})"))
                    p.data.lastKnownName = p.username
                }
                Audiences.players().sendTabList()
                Server.refreshPingResponse()
            }
//            p.setReducedDebugScreenInformation(true)
            p.sendPacket(setTickStatePacket)
            p.isInvisible = true

            val doll = Doll(p)
            doll.setInstance(p.instance, p.position)
            p.doll = doll
            GameManager.objs += doll
        }

        addListener<PlayerDisconnectEvent> { e ->
            val p = e.player as FPlayer
            Logger.debug { p.instance }
            Logger.info("${p.username} lost connection")
            broadcast(formatText("<gray><bold>●</bold><white> ${p.username}"))
            Server.refreshPingResponse()
            PlayerData.store(p)

            GameManager.objs -= p.doll!!
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
        handler.addChild(
            EventNode.all("chat").setPriority(0).addListener<PlayerChatEvent> { e ->
                e.formattedMessage =
                    Component.text("<${e.player.username}> ")
                        .append(urlSerializer.deserialize(e.rawMessage))
                        .colorIfAbsent(NamedTextColor.WHITE)
                        .also { Logger.info(it) }
            }
        )

        addListener<PlayerSkinInitEvent> { e ->
            val p = e.player as FPlayer
            Logger.debug { "PlayerSkinInitEvent ${p.username}" }
            val skin = e.skin ?: return@addListener
            p.isSlim = skin.isSlim()
            p.headProfile = HeadProfile(skin)
        }

        addListener<PlayerCommandEvent> { e ->
            Logger.info("${e.player.username} issued server command: /${e.command}")
        }

        val voidJumper = mutableSetOf<Player>()
        addListener<EntityDamageEvent> { e ->
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

        addListener<PlayerPluginMessageEvent> { e ->
            if (e.identifier != "minecraft:brand") return@addListener
            val brand = e.messageString.let {
                if (!it.first().isLetter()) it.substring(1) else it
            }
            val p = e.player as FPlayer
            Logger.debug { "Brand of player ${p.username} is '$brand'" }
            p.brand = brand
        }

        addListener<PlayerSettingsChangeEvent> { e ->
            val p = e.player
            val settings = p.settings
            Logger.debug { ("Settings of player ${p.username} are $settings") }
        }

        addListener<PlayerDeathEvent> { e ->
            e.chatMessage = null
            e.deathText = null
        }

        addListener<PlayerMoveEvent> { e ->
            val p = e.player as FPlayer
            val newPos = e.newPosition
            p.doll?.run {
                if (syncPosition) {
                    hitbox.refresh()
                    teleport(newPos)
                }
            }
        }

        addListener<EntityTeleportEvent> { e ->
            val p = e.entity as? FPlayer ?: return@addListener
            p.doll?.run {
                if (syncPosition) {
                    hitbox.refresh()
                    teleport(e.newPosition)
                }
            }
        }

        addListener<PlayerStartSneakingEvent> { e ->
            val p = e.player as FPlayer
            p.doll?.run {
                if (syncPosition) {
                    hitbox.refresh()
                }
            }
        }

        addListener<PlayerStopSneakingEvent> { e ->
            val p = e.player as FPlayer
            p.doll?.run {
                if (syncPosition) {
                    hitbox.refresh()
                }
            }
        }

        addListener<PlayerBlockBreakEvent> { e ->
            e.isCancelled = true
        }


        if (!Server.IS_SERVER_TICK_EQUAL_TO_CLIENT_TICK) {
            var d = 1
            addListener<ServerTickMonitorEvent> { e ->
                if (d != 1) {
                    if (d == Server.CLIENT_2_SERVER_TICKS) {
                        d = 1
                    } else {
                        d++
                    }
                    Server.isClientTickTime = false
                    return@addListener
                }
                d++
                Server.isClientTickTime = true
            }
        }
        addListener<ServerTickMonitorEvent> { e ->
            Server.ticks++
        }

        val ignoringInPackets =
            arrayOf(
                ClientTickEndPacket::class,
                ClientKeepAlivePacket::class,
//                ClientPlayerPositionPacket::class,
//                ClientPlayerRotationPacket::class,
//                ClientPlayerPositionAndRotationPacket::class,
            )
        addListener<PlayerPacketEvent> { e ->
            val p = e.player as FPlayer
            val packet = e.packet

            when (packet) {
                is ClientPlayerDiggingPacket -> {
                    when (packet.status) {
                        STARTED_DIGGING -> {
//                    Logger.debug { "${ServerStatus.tick} packet L start" }
                            p.mouseInputs.left = true
                            handler.call(PlayerKeyInputEvent(p, PlayerKeyInputEvent.Key.MOUSE_LEFT))
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
                is ClientInteractEntityPacket -> {
                    if (packet.type is ClientInteractEntityPacket.Attack) {
//                        Logger.debug { "${ServerStatus.tick} packet L end" }
                        handler.call(PlayerKeyInputEvent(p, PlayerKeyInputEvent.Key.MOUSE_LEFT))
                    }
                }
                is ClientUseItemPacket -> {
//                    Logger.debug { "${ServerStatus.tick} packet R start" }
                    p.mouseInputs.right = true
                    handler.call(PlayerKeyInputEvent(p, PlayerKeyInputEvent.Key.MOUSE_RIGHT))
                }
            }
//            if (!ignoringInPackets.contains(packet::class)) println("-> ${Server.ticks} $packet")
        }

        val ignoringOutPackets =
            arrayOf(
                KeepAlivePacket::class,
                PlayerListHeaderAndFooterPacket::class,
                PlayerInfoUpdatePacket::class,
            )
        addListener<PlayerPacketOutEvent> { e ->
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




        addListener<PlayerKeyInputEvent> { e ->
            val p = e.player as FPlayer
            when (e.key) {
                PlayerKeyInputEvent.Key.MOUSE_LEFT -> p.doll?.skillHolder?.cast(0)
                PlayerKeyInputEvent.Key.MOUSE_RIGHT -> p.doll?.skillHolder?.cast(1)
                PlayerKeyInputEvent.Key.NUM_1 -> p.doll?.skillHolder?.cast(2)
                PlayerKeyInputEvent.Key.NUM_2 -> p.doll?.skillHolder?.cast(3)
                PlayerKeyInputEvent.Key.NUM_3 -> p.doll?.skillHolder?.cast(4)
                PlayerKeyInputEvent.Key.NUM_4 -> p.doll?.skillHolder?.cast(5)
                PlayerKeyInputEvent.Key.Q -> p.doll?.skillHolder?.cast(10)
                PlayerKeyInputEvent.Key.F -> p.doll?.weaponHolder?.reload()
            }
        }

        addListener<ObjDamageEvent> { e ->
//            e.victim.hp -= e.damage
            e.attacker?.run {
                if (this is Doll) player.run {
                    playSound(
                        if (e.critical) GameUtils.CRITICAL_HIT_SOUND else GameUtils.HIT_SOUND,
                        getPov()
                    )
                }
            }
        }
    }

    val handler get() = MinecraftServer.getGlobalEventHandler()

    inline fun <reified T : Event> addListener(noinline listener: (T) -> Unit): EventNode<Event> =
        handler.addListener(T::class.java, listener)

    inline fun <reified T : Event> EventNode<Event>.addListener(noinline listener: (T) -> Unit): EventNode<Event> =
        addListener(T::class.java, listener)
}
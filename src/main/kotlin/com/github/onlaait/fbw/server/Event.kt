package com.github.onlaait.fbw.server

import com.github.onlaait.fbw.entity.FPlayer
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
import net.minestom.server.network.packet.client.common.ClientKeepAlivePacket
import net.minestom.server.network.packet.client.play.ClientTickEndPacket
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
//            player.setReducedDebugScreenInformation(true)

            if (p.data.lastKnownName == p.username) {
                broadcast(formatText("<green><bold>●</bold><white> ${p.username}"))
            } else {
                broadcast(formatText("<green><bold>●</bold><white> ${p.username}<gray>(${p.data.lastKnownName})"))
                p.data.lastKnownName = p.username
            }

            Audiences.players().sendTabList()
            Server.refreshPingResponse()

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
        addListener<PlayerSpawnEvent> { e ->
            val p = e.player as FPlayer
            Logger.info("PlayerSpawnEvent $p/${e.entity}/${e.instance}/${e.isFirstSpawn}")
            p.sendPacket(setTickStatePacket)
            p.isInvisible = true

            val doll = Doll(p)
            doll.setInstance(p.instance, p.position)
            p.doll = doll
            GameManager.objs += doll
        }

        addListener<PlayerDisconnectEvent> { e ->
            val player = e.player as FPlayer
            Logger.info("${player.username} lost connection")
            broadcast(formatText("<gray><bold>●</bold><white> ${player.username}"))
            Server.refreshPingResponse()
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
        handler.addChild(
            EventNode.all("chat").setPriority(0).addListener<PlayerChatEvent> { e ->
                e.formattedMessage =
                    Component.text("<${e.player.username}> ")
                        .append(urlSerializer.deserialize(e.rawMessage))
                        .colorIfAbsent(NamedTextColor.WHITE)
                        .also { Logger.info(it) }
            }
        )

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
            Logger.debug { "${p.username} is using client '$brand'" }
            p.brand = brand
        }

        addListener<PlayerSettingsChangeEvent> { e ->
            val player = e.player
            val settings = player.settings
            Logger.debug { ("${player.username}'s settings are [locale=${settings.locale},viewDistance=${settings.viewDistance}]") }
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
            val p = e.packet
//            if (!ignoringInPackets.contains(p::class)) println("-> ${Server.ticks} $p")
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




        addListener<PlayerLClickEvent> { e ->
//            println("${ServerStatus.tick} L event")
            val p = e.player as FPlayer
            ExampleSkill.cast(p.doll!!)
        }

        addListener<PlayerRClickEvent> { e ->
//            println("${ServerStatus.tick} R event")
            val p = e.player as FPlayer
            ExampleSkill.cast(p.doll!!)
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
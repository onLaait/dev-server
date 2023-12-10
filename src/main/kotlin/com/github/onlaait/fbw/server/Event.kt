package com.github.onlaait.fbw.server

import com.github.onlaait.fbw.system.BanSystem.kickIfBanned
import com.github.onlaait.fbw.system.MuteSystem.isMuted
import com.github.onlaait.fbw.system.OpSystem
import com.github.onlaait.fbw.system.PlayerData
import com.github.onlaait.fbw.system.ServerProperties
import com.github.onlaait.fbw.system.ServerStatus
import com.github.onlaait.fbw.system.ServerStatus.sendTabList
import com.github.onlaait.fbw.system.Whitelist.kickIfNotWhitelisted
import com.github.onlaait.fbw.utils.AudienceUtils.broadcast
import com.github.onlaait.fbw.utils.AudienceUtils.warnMsg
import com.github.onlaait.fbw.utils.ComponentUtils.plainText
import com.github.onlaait.fbw.utils.ComponentUtils.render
import com.github.onlaait.fbw.utils.PlayerUtils
import com.github.onlaait.fbw.utils.PlayerUtils.brand
import com.github.onlaait.fbw.utils.PlayerUtils.data
import com.github.onlaait.fbw.utils.ServerUtils
import com.github.onlaait.fbw.utils.ServerUtils.refreshEntries
import com.github.onlaait.fbw.utils.TextUtils.formatText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.entity.fakeplayer.FakePlayer
import net.minestom.server.event.entity.EntityDamageEvent
import net.minestom.server.event.player.*
import net.minestom.server.event.server.ServerListPingEvent
import net.minestom.server.event.server.ServerTickMonitorEvent
import net.minestom.server.network.packet.client.play.ClientChatSessionUpdatePacket
import net.minestom.server.network.packet.client.play.ClientSetRecipeBookStatePacket
import net.minestom.server.network.packet.server.login.LoginDisconnectPacket
import net.minestom.server.network.packet.server.play.DisconnectPacket
import net.minestom.server.network.packet.server.play.ExplosionPacket
import net.minestom.server.timer.TaskSchedule
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

object Event {
    init {
        val event = MinecraftServer.getGlobalEventHandler()
        MinecraftServer.getPacketListenerManager()
        val packet = MinecraftServer.getPacketListenerManager()

        event.addListener(ServerListPingEvent::class.java) { e ->
            if (!ServerProperties.ENABLE_STATUS) {
                e.isCancelled = true
                return@addListener
            }
            e.responseData = ServerUtils.responseData
        }

        event.addListener(AsyncPlayerPreLoginEvent::class.java) { e ->
            val player = e.player
            if (player is FakePlayer) return@addListener
            Logger.info("UUID of player ${player.username} is ${player.uuid}")

            if (OpSystem.opPlayers.find { it.uuid == player.uuid } != null) {
                (player as PlayerP).isOp = true
            } else if (ServerProperties.MAX_PLAYERS <= PlayerUtils.onlinePlayersCount) {
                player.kick(Component.translatable("multiplayer.disconnect.server_full"))
            } else if (ServerProperties.WHITE_LIST) {
                player.kickIfNotWhitelisted()
            }
            player.kickIfBanned()
        }

        event.addListener(PlayerLoginEvent::class.java) { e ->
            val player = e.player
            if (player is FakePlayer) return@addListener
            Logger.info("${player.username}[${player.playerConnection.remoteAddress}] logged in with (entityId=${player.entityId},serverAddress=${player.playerConnection.serverAddress},locale=${player.settings.locale},viewDistance=${player.settings.viewDistance})")
            player.respawnPoint = Pos(0.5, 1.0, 0.5)
            e.setSpawningInstance(Instance.instance)
            if (player.data.lastKnownName == player.username) {
                broadcast(formatText("<green><bold>●</bold><white> ${player.username}"))
            } else {
                broadcast(formatText("<green><bold>●</bold><white> ${player.username}<gray>(${player.data.lastKnownName})"))
                player.data.lastKnownName = player.username
            }
            Audiences.players().sendTabList()
            ServerUtils.responseData.online = MinecraftServer.getConnectionManager().onlinePlayers.size
            ServerUtils.responseData.refreshEntries()

            FakePlayer.initPlayer(UUID.randomUUID(), player.username) { fakePlayer ->
                fakePlayer.displayName = Component.text("Shadow")
                fakePlayer.setNoGravity(true)
                fakePlayer.updateViewableRule { viewer -> viewer != player }

                event.addListener(PlayerMoveEvent::class.java) a@{ e ->
                    if (e.player != player) return@a
                    fakePlayer.refreshPosition(e.newPosition.add(0.0, 2.0, 0.0))
                }
                event.addListener(PlayerDisconnectEvent::class.java) a@{ e ->
                    if (e.player != player) return@a
                    fakePlayer.remove()
                }
            }

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

        event.addListener(PlayerLoginEvent::class.java) { e ->
            val player = e.player
            if (player !is FakePlayer) return@addListener
            e.setSpawningInstance(Instance.instance)
        }

        event.addListener(PlayerDisconnectEvent::class.java) { e ->
            val player = e.player
            if (player is FakePlayer) return@addListener
            Logger.info("${player.username} lost connection")
            broadcast(formatText("<gray><bold>●</bold><white> ${player.username}"))
            ServerUtils.responseData.online = MinecraftServer.getConnectionManager().onlinePlayers.size
            ServerUtils.responseData.refreshEntries()
            PlayerData.write(player)
        }

        event.addListener(PlayerChatEvent::class.java) { e ->
            val player = e.player
            if (player.isMuted()) {
                val muteTime = player.data.muteTime!!
                if (muteTime == -1L) {
                    player.warnMsg("채팅이 비활성화된 상태입니다.")
                } else {
                    val remainTime = (muteTime - System.currentTimeMillis()).milliseconds
                        .toComponents { days, hours, minutes, seconds, _ ->
                            return@toComponents if (days != 0L) {
                                 "${days}일"
                            } else if (hours != 0) {
                                "${hours}시간"
                            } else if (minutes != 0) {
                                "${minutes}분"
                            } else {
                                "${seconds}초"
                            }
                        }
                    player.warnMsg("채팅이 비활성화된 상태입니다. $remainTime 후에 활성화됩니다.")
                }
                e.isCancelled = true
                return@addListener
            }
            e.setChatFormat {
                Component.text("<${player.username}> ${e.message}")
                    .color(NamedTextColor.WHITE)
                    .also { Logger.info(it) }
            }
        }

        event.addListener(PlayerCommandEvent::class.java) { e ->
            Logger.info("${e.player.username} issued server command: /${e.command}")
        }

        event.addListener(PlayerHandAnimationEvent::class.java) { e ->
            MinecraftServer.getCommandManager().execute(e.player, "test ray")
        }

        val voidJumper = mutableSetOf<Player>()
        event.addListener(EntityDamageEvent::class.java) { e ->
            e.isCancelled = true
            val entity = e.entity
            if (entity !is Player) return@addListener
            if (e.damageType == DamageType.VOID && !voidJumper.contains(entity)) {
                voidJumper += entity
                val pos = entity.position
                entity.sendPacket(ExplosionPacket(pos.x, pos.y, pos.z, 0F, byteArrayOf(), 0F, 11F, 0F))
                MinecraftServer.getSchedulerManager().buildTask {
                    voidJumper -= entity
                }.delay(TaskSchedule.millis(500)).schedule()
            }
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

        event.addListener(ServerTickMonitorEvent::class.java) { e ->
            ServerStatus.onTick(e.tickMonitor.tickTime)
        }

        packet.setListener(ClientSetRecipeBookStatePacket::class.java) { _, _ -> }
        packet.setListener(ClientChatSessionUpdatePacket::class.java) { _, _ -> }

        event.addListener(PlayerPacketEvent::class.java) { e ->
//            println("-> ${e.packet}")
        }

        event.addListener(PlayerPacketOutEvent::class.java) { e ->
//            println("<- ${e.packet}")
            fun disconnectInfo(kickMessage: Component) {
                Logger.info("Disconnecting ${e.player.username} (${e.player.playerConnection.remoteAddress}): ${kickMessage.render().plainText()}")
            }
            when (val packet = e.packet) {
                is DisconnectPacket -> {
                    disconnectInfo(packet.message)
                }
                is LoginDisconnectPacket -> {
                    disconnectInfo(packet.kickMessage)
                }
            }
        }
    }
}
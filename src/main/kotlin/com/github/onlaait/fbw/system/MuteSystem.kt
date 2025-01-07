package com.github.onlaait.fbw.system

import com.github.onlaait.fbw.server.FPlayer
import com.github.onlaait.fbw.utils.warnMsg
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerChatEvent
import kotlin.time.Duration.Companion.milliseconds

object MuteSystem {

    init {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerChatEvent::class.java) { e ->
            val player = e.player as FPlayer
            if (!player.isMuted()) return@addListener

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
        }
    }

    fun Player.isMuted(): Boolean {
        this as FPlayer
        val muteTime = data.muteTime ?: return false
        if (muteTime == -1L || muteTime > System.currentTimeMillis()) return true
        data.muteTime = null
        return false
    }
}
package com.github.onlaait.fbw.system

import com.github.onlaait.fbw.utils.PlayerUtils.data
import net.minestom.server.entity.Player

object MuteSystem {

    fun Player.isMuted(): Boolean {
        val muteTime = this.data.muteTime ?: return false
        if (muteTime == -1L || muteTime > System.currentTimeMillis()) return true
        this.data.muteTime = null
        return false
    }
}
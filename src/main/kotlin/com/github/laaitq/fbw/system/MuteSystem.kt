package com.github.laaitq.fbw.system

import com.github.laaitq.fbw.utils.PlayerUtils.data
import net.minestom.server.entity.Player

object MuteSystem {

    fun Player.isMuted(): Boolean {
        val muteTime = this.data.muteTime ?: return false
        if (muteTime > System.currentTimeMillis()) return true
        if (muteTime == -1L) return true
        this.data.muteTime = null
        return false
    }
}
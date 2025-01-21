package com.github.onlaait.fbw.game.weapon

import com.github.onlaait.fbw.game.obj.Doll
import com.github.onlaait.fbw.utils.sendMsg

object WhiteFang465 : Weapon("WHITE FANG 465") {

    override val modelId = "whitefang465"
    override val ammo = 100

    init {
        onLClick {
            (it.caster as Doll).player.sendMsg("shot")
            it.ammo -= 1
        }

        onReload {
            (it.caster as Doll).player.sendMsg("reload")
            it.ammo = ammo
        }
    }
}
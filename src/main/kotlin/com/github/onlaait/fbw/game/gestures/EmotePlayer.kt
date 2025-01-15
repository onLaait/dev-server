package com.github.onlaait.fbw.game.gestures

import com.google.gson.JsonObject
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerSkin
import net.minestom.server.instance.Instance
import net.worldseed.multipart.animations.AnimationHandler
import net.worldseed.multipart.animations.AnimationHandlerImpl

abstract class EmotePlayer(instance: Instance, pos: Pos, skin: PlayerSkin, slim: Boolean) {

    private val model = EmoteModel(skin, slim)
    protected val animationHandler: AnimationHandler =
        object : AnimationHandlerImpl(model) {
            override fun loadDefaultAnimations() {}
        }
    private var emoteIndex = 0

    init {
        model.init(instance, pos)
        model.draw()
        model.draw()
    }

    /**
     * Loads the emotes into the animation handler
     *
     * @param emotes Map containing the emote name, and emote data
     */
    fun loadEmotes(emotes: Map<String, JsonObject>) {
        for ((key, value) in emotes) {
            animationHandler.registerAnimation(key, value, emoteIndex)
            emoteIndex++
        }
    }

    fun remove() {
        model.destroy()
        animationHandler.destroy()
    }

    fun setRotation(yaw: Double) {
        model.globalRotation = yaw
    }

    fun addViewer(player: Player) {
        model.addViewer(player)
    }

    fun removeViewer(player: Player) {
        model.removeViewer(player)
    }
}
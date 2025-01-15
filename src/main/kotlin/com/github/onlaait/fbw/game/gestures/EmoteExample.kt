package com.github.onlaait.fbw.game.gestures

import com.google.gson.JsonObject
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.PlayerSkin
import net.minestom.server.instance.Instance
import net.worldseed.multipart.ModelLoader

class EmoteExample(instance: Instance, pos: Pos, skin: PlayerSkin, slim: Boolean) : EmotePlayer(instance, pos, skin, slim) {

    init {
        loadEmotes(ANIMATIONS)
    }

    fun play() {
        animationHandler.playOnce("animation") {}
    }

    companion object {
        private const val ANIMATION_STRING =
            "{\n" +
                    "\t\"format_version\": \"1.8.0\",\n" +
                    "\t\"animations\": {\n" +
                    "\t\t\"animation\": {\n" +
                    "\t\t\t\"animation_length\": 2,\n" +
                    "\t\t\t\"bones\": {\n" +
                    "\t\t\t\t\"head\": {\n" +
                    "\t\t\t\t\t\"rotation\": {\n" +
                    "\t\t\t\t\t\t\"0.35\": [0, 0, 0],\n" +
                    "\t\t\t\t\t\t\"1.35\": [-90, 0, 0]\n" +
                    "\t\t\t\t\t}\n" +
                    "\t\t\t\t},\n" +
                    "\t\t\t\t\"right_arm\": {\n" +
                    "\t\t\t\t\t\"rotation\": {\n" +
                    "\t\t\t\t\t\t\"0.35\": [0, 0, 0],\n" +
                    "\t\t\t\t\t\t\"1.35\": [-90, 0, 0]\n" +
                    "\t\t\t\t\t}\n" +
                    "\t\t\t\t},\n" +
                    "\t\t\t\t\"left_arm\": {\n" +
                    "\t\t\t\t\t\"rotation\": {\n" +
                    "\t\t\t\t\t\t\"0.35\": [0, 0, 0],\n" +
                    "\t\t\t\t\t\t\"1.35\": [0, 0, -90]\n" +
                    "\t\t\t\t\t}\n" +
                    "\t\t\t\t},\n" +
                    "\t\t\t\t\"right_leg\": {\n" +
                    "\t\t\t\t\t\"rotation\": {\n" +
                    "\t\t\t\t\t\t\"0.35\": [0, 0, 0],\n" +
                    "\t\t\t\t\t\t\"1.35\": [-90, 0, 0]\n" +
                    "\t\t\t\t\t}\n" +
                    "\t\t\t\t},\n" +
                    "\t\t\t\t\"left_leg\": {\n" +
                    "\t\t\t\t\t\"rotation\": {\n" +
                    "\t\t\t\t\t\t\"0.35\": [0, 0, 0],\n" +
                    "\t\t\t\t\t\t\"1.35\": [90, 0, 0]\n" +
                    "\t\t\t\t\t}\n" +
                    "\t\t\t\t}\n" +
                    "\t\t\t}\n" +
                    "\t\t}\n" +
                    "\t}\n" +
                    "}"
        private val ANIMATIONS: Map<String, JsonObject> =
            ModelLoader.parseAnimations(ANIMATION_STRING)
    }
}
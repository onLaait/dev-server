package com.github.onlaait.fbw.game.utils

import net.kyori.adventure.sound.Sound
import net.minestom.server.sound.SoundEvent

object GameUtils {

    val HIT_SOUND =
        Sound.sound(
            SoundEvent.BLOCK_NOTE_BLOCK_SNARE,
            Sound.Source.MASTER,
            1f,
            1.5f
        )

    val CRITICAL_HIT_SOUND =
        Sound.sound(
            SoundEvent.ENTITY_ARROW_HIT_PLAYER,
            Sound.Source.MASTER,
            1f,
            2f
        )
}
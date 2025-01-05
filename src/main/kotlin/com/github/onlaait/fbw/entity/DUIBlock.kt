package com.github.onlaait.fbw.entity

import com.github.onlaait.fbw.math.Vec2d
import com.github.onlaait.fbw.math.Vec3d
import com.github.onlaait.fbw.math.times
import com.github.onlaait.fbw.math.toVec
import com.github.onlaait.fbw.utils.editMeta
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.metadata.display.TextDisplayMeta
import java.awt.Color

class DUIBlock : Entity(EntityType.TEXT_DISPLAY) {

    companion object {
        val TRANSLATION_BASE: Vec3d = Vec3d(-0.0125, -0.125, 0.0)
        const val SCALE_UNIT: Double = 4.0

        val DEFAULT_SCALE = Vec2d(1.0, 1.0)
        val DEFAULT_COLOR = Color(255, 255, 255)

        val TEXT = Component.text('\uF009').font(Key.key("fbw", "space"))
    }

    var scale: Vec2d = DEFAULT_SCALE
        set(value) {
            field = value
            val scale2 = value * SCALE_UNIT
            val scale3 = Vec3d(scale2.x, scale2.y, 1.0)
            editMeta<TextDisplayMeta> {
                translation = (TRANSLATION_BASE * scale3).toVec()
                scale = scale3.toVec()
            }
        }

    var color: Color = DEFAULT_COLOR
        set(value) {
            field = value
            editMeta<TextDisplayMeta> {
                backgroundColor = value.rgb
            }
        }

    init {
        editMeta<TextDisplayMeta> {
            isHasNoGravity = true
            text = TEXT
            textOpacity = 0
        }

        scale = scale
        color = color
    }
}
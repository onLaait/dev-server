package com.github.onlaait.fbw.game.skill

import com.github.onlaait.fbw.game.GameManager
import com.github.onlaait.fbw.game.event.ObjDamageEvent
import com.github.onlaait.fbw.game.obj.Caster
import com.github.onlaait.fbw.game.obj.Hittable
import com.github.onlaait.fbw.game.targeter.RayTargeter
import com.github.onlaait.fbw.game.utils.showOneDust
import com.github.onlaait.fbw.geometry.Ray
import com.github.onlaait.fbw.math.toVec3f
import com.github.onlaait.fbw.server.eventHandler
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.sound.SoundEvent

object ExampleSkill : Skill {

    override val description = "스킬 예제"

    override val cooldown = 3.0

    private const val MAX_DIST = 50f

    private val SHOOT_SOUND = Sound.sound(Key.key("fbw", "beep"), Sound.Source.MASTER, 1f, 1f)
    private val HIT_GROUND_SOUND = Sound.sound(SoundEvent.BLOCK_GLASS_BREAK, Sound.Source.MASTER, 1f, 2f)

    override fun cast(o: Caster) {
        var pos = o.getPov()
        Audiences.players().playSound(SHOOT_SOUND, pos.x, pos.y, pos.z)
        val dir = pos.direction()
        val hit = RayTargeter.target(
            Ray(pos.toVec3f(), dir.toVec3f(), MAX_DIST),
            (GameManager.objs - o).filterIsInstance<Hittable>()
        )
        val targets = hit.hits
        val dist: Float
        if (targets.isNotEmpty()) {
            val target = targets.first()
            dist = target.distance
            eventHandler.call(ObjDamageEvent(target.obj, o, 0.0, target.critical))
        } else {
            val distGrnd = hit.distanceToGround
            if (distGrnd == null) {
                dist = MAX_DIST
            } else {
                if (distGrnd <= MAX_DIST) {
                    val grndPos = pos.add(dir.mul(distGrnd))
                    Audiences.players().playSound(HIT_GROUND_SOUND, grndPos.x, grndPos.y, grndPos.z)
                }
                dist = distGrnd.toFloat()
            }
        }

        repeat(dist.toInt()) {
            pos = pos.add(dir)
            showOneDust(252, 140, 255, pos)
        }
    }
}
package com.github.onlaait.fbw.model

import com.github.onlaait.fbw.entity.FEntity
import com.github.onlaait.fbw.math.*
import com.github.onlaait.fbw.server.Server
import com.google.gson.Gson
import com.google.gson.JsonObject
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.item.component.HeadProfile
import net.worldseed.multipart.animations.AnimationHandler
import net.worldseed.multipart.model_bones.ModelBoneImpl
import net.worldseed.multipart.model_bones.ModelBoneViewable
import java.util.function.Function
import java.util.function.Predicate
import kotlin.math.cos
import kotlin.math.sqrt

class PlayerModel(val entity: FEntity, private val headProfile: HeadProfile, private val slim: Boolean, val modelId: String = "default") : FGenericModel() {

    lateinit var animationHandler: AnimationHandler

    lateinit var head: PlayerModelBone
    lateinit var body: PlayerModelBone
    lateinit var rightArm: PlayerModelBone
    lateinit var leftArm: PlayerModelBone
    lateinit var rightLeg: PlayerModelBone
    lateinit var leftLeg: PlayerModelBone

    lateinit var organs: Array<PlayerModelBone>

    val equipment = EquipmentModel(this)

    val animConfig = AnimationConfig.configs[modelId]

    private val realId = "$modelId.bbmodel"

    override fun getId() = realId

    override fun registerBoneSuppliers() {
        boneSuppliers[Predicate { it == "base" }] = Function { ModelBoneRoot(it.pivot, it.name, it.rotation, it.model, it.scale) }
        boneSuppliers[Predicate { BONE_TRANSLATIONS.containsKey(it) }] =
            Function {
                PlayerModelBone(
                    it.pivot, it.name, it.rotation, it.model,
                    BONE_TRANSLATIONS[it.name()]!!, DIFFS[it.name()]!!, headProfile, slim
                )
            }
        boneSuppliers[Predicate { true }] = Function { null }
    }

    override fun init(instance: Instance?, position: Pos, scale: Float) {
        this.instance = instance
        this.position = position

        super.loadBones(MODEL_JSON, scale)

        for (modelBonePart in getParts()) {
            if (modelBonePart is ModelBoneViewable) viewableBones.add(modelBonePart as ModelBoneImpl)

            modelBonePart.spawn(instance, modelBonePart.calculatePosition()).join()
        }

        head = parts["head"]!! as PlayerModelBone
        body = parts["body"]!! as PlayerModelBone
        rightArm = parts["right_arm"]!! as PlayerModelBone
        leftArm = parts["left_arm"]!! as PlayerModelBone
        rightLeg = parts["right_leg"]!! as PlayerModelBone
        leftLeg = parts["left_leg"]!! as PlayerModelBone
        organs = arrayOf(head, body, rightArm, leftArm, rightLeg, leftLeg)

        animationHandler = FAnimationHandler(this)

        equipment.init(instance, position, scale)

        setGlobalScale(1f)

        ModelManager.registerModel(this)

        draw()
    }

    override fun getDiff(boneName: String) = null

    override fun getOffset(boneName: String) = Vec.ZERO

    override fun setGlobalRotation(yaw: Double, pitch: Double) {
        super.setGlobalRotation(yaw, 0.0)
    }

    private var scale = 1f

    override fun setGlobalScale(scale: Float) {
        super.setGlobalScale(scale)
        this.scale = scale
        equipment.setGlobalScale(scale * MAGIC_NUMBER.toFloat())
    }

    private var prevPrevPos = Pos.ZERO
    private var prevPos = Pos.ZERO
    private var d = 1

    override fun draw() {
        val pos0 = entity.position
        if (d++ != Server.CLIENT_2_SERVER_TICKS && pos0 == prevPos) return
        d = 1
        var pos = pos0
//        (entity as Doll).player.sendMsg("${(pos.toVec3d() - prevPos.toVec3d()).length().toFloat()}")
        if (pos0 == prevPos && !prevPos.samePoint(prevPrevPos)) {
            val v = prevPos.toVec3d() - prevPrevPos.toVec3d()
            if (v.lengthSquared() > SPEED_MIN_FOR_PREDICTION_SQUARED) {
                pos += v * 0.5
//                broadcast(Component.text("predict"))
            }
        }
        setPosition(pos)
        setGlobalRotation(pos.yaw.toDouble())

        head.extraRotation.x = -pos.pitch.toDouble()

        val g = limbFrequency
        val h = limbAmplitudeMultiplier
        val a = g * 0.6662f
        val b = cos(a + PI_F)
        val c = cos(a)
        val d = 1.4f * h
        rightArm.extraRotation.x += (b * h).toDeg()
        leftArm.extraRotation.x += (c * h).toDeg()
        rightLeg.extraRotation.x += (c * d).toDeg()
        leftLeg.extraRotation.x += (b * d).toDeg()

        if (entity.isSneaking) {
            head.extraOffset.y += SNEAKING_HEAD_OFFSET_Y
            body.extraOffset.y += SNEAKING_BODY_OFFSET_Y
            body.extraRotation.x += SNEAKING_BODY_ROTATION_X
            rightArm.extraOffset.y += SNEAKING_RIGHT_ARM_OFFSET_Y
            rightArm.extraRotation.x += SNEAKING_RIGHT_ARM_ROTATION_X
            leftArm.extraOffset.y += SNEAKING_LEFT_ARM_OFFSET_Y
            leftArm.extraRotation.x += SNEAKING_LEFT_ARM_ROTATION_X
            rightLeg.extraOffset.y += SNEAKING_RIGHT_LEG_OFFSET_Y
            rightLeg.extraOffset.z += SNEAKING_RIGHT_LEG_OFFSET_Z
            leftLeg.extraOffset.y += SNEAKING_LEFT_LEG_OFFSET_Y
            leftLeg.extraOffset.z += SNEAKING_LEFT_LEG_OFFSET_Z
        }

        if (animConfig != null) {
            val playing = animationHandler.playing
            if (playing != null) {
                val animConfig = animConfig[playing]
                if (animConfig != null) {
                    for ((part, tags) in animConfig) {
                        val part = parts[part] as? HumanModelBone ?: continue
                        val rot = part.extraRotation
                        if (tags.contains(AnimationConfig.Config.FIXED)) {
                            rot.zero()
                        }
                        if (tags.contains(AnimationConfig.Config.FOLLOWING_HEAD)) {
                            rot.x -= pos.pitch
                        }
                    }
                }
            }
        }

        prevPrevPos = prevPos
        prevPos = pos0

        super.draw()
        equipment.draw()

        organs.forEach {
            it.extraOffset.zero()
            it.extraRotation.zero()
        }
    }

    override fun setPosition(pos: Pos?) {
        position = pos
    }

    override fun addViewer(player: Player): Boolean {
        val boolean = super.addViewer(player)
        equipment.addViewer(player)
        return boolean
    }

    override fun destroy() {
        equipment.destroy()
        animationHandler.destroy()
        super.destroy()
    }

    fun playAnimation(animation: String) {
        animationHandler.playOnce(animation) {}
        equipment.animationHandler.playOnce(animation) {}
    }

    private val limbAnimator = LimbAnimator()

    fun updateLimbs() {
        val pos = entity.position
        val prevPos = entity.previousPosition
        val f = magnitude(pos.x - prevPos.x, 0.0, pos.z - prevPos.z).toFloat() / scale.let { if (it > 1) sqrt(it) else square(it) }
        updateLimbs(f)
    }

    private fun updateLimbs(posDelta: Float) {
        val f = (posDelta * 4.0f).coerceAtMost(1.0f)
        limbAnimator.updateLimbs(f, 0.4f, 1.0f)
    }

    private var limbFrequency = 0f
    private var limbAmplitudeMultiplier = 0f

    fun updateState() {
        val tickDelta = 1f
        limbFrequency = limbAnimator.getPos(tickDelta)
        limbAmplitudeMultiplier = limbAnimator.getSpeed(tickDelta)
    }

    companion object {
        val MODEL_JSON: JsonObject =
            Gson().fromJson(ClassLoader.getSystemResourceAsStream("player_model.geo.json")!!.reader(), JsonObject::class.java)

        private val DIFFS = mapOf(
            "head" to Vec(0.0, 1.4061 * -4, 0.0),
            "body" to Vec(0.0, 1.4061 * -4, 0.0),
            "right_arm" to Vec(-1.17, 1.2888 * -4, 0.0),
            "left_arm" to Vec(1.17, 1.2888 * -4, 0.0),
            "right_leg" to Vec(-0.4446, 0.7031 * -4, 0.0),
            "left_leg" to Vec(0.4446, 0.7031 * -4, 0.0)
        )

        private val BONE_TRANSLATIONS = mapOf(
            "head" to 0,
            "body" to -3072,
            "right_arm" to -1024,
            "left_arm" to -2048,
            "right_leg" to -4096,
            "left_leg" to -5120
        )

        const val MAGIC_NUMBER = 0.9375

        const val SNEAKING_HEAD_OFFSET_Y = -0.35 * 4 / MAGIC_NUMBER
        const val SNEAKING_BODY_OFFSET_Y = -0.3 * 4 / MAGIC_NUMBER
        const val SNEAKING_BODY_ROTATION_X = -0.5 * RAD_2_DEG
        const val SNEAKING_RIGHT_ARM_OFFSET_Y = -0.3 * 4 / MAGIC_NUMBER
        const val SNEAKING_RIGHT_ARM_ROTATION_X = -0.4 * RAD_2_DEG
        const val SNEAKING_LEFT_ARM_OFFSET_Y = -0.3 * 4 / MAGIC_NUMBER
        const val SNEAKING_LEFT_ARM_ROTATION_X = -0.4 * RAD_2_DEG
        const val SNEAKING_RIGHT_LEG_OFFSET_Y = -0.115 * 4 / MAGIC_NUMBER
        const val SNEAKING_RIGHT_LEG_OFFSET_Z = 0.23 * 4 / MAGIC_NUMBER
        const val SNEAKING_LEFT_LEG_OFFSET_Y = -0.115 * 4 / MAGIC_NUMBER
        const val SNEAKING_LEFT_LEG_OFFSET_Z = 0.23 * 4 / MAGIC_NUMBER

        val SPEED_MIN_FOR_PREDICTION_SQUARED = square(4.317 * 0.8 / 20)
    }
}

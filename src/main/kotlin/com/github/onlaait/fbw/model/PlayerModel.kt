package com.github.onlaait.fbw.model

import com.github.onlaait.fbw.entity.FEntity
import com.github.onlaait.fbw.math.*
import com.github.onlaait.fbw.server.Server
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.instance.Instance
import net.minestom.server.item.component.HeadProfile
import net.worldseed.multipart.model_bones.ModelBoneImpl
import net.worldseed.multipart.model_bones.ModelBoneViewable
import java.io.StringReader
import java.util.function.Function
import java.util.function.Predicate
import kotlin.math.cos

open class PlayerModel(val entity: FEntity, private val headProfile: HeadProfile, private val slim: Boolean) : FGenericModel() {

    lateinit var animationHandler: FAnimationHandler

    lateinit var head: PlayerModelBone
    lateinit var body: PlayerModelBone
    lateinit var rightArm: PlayerModelBone
    lateinit var leftArm: PlayerModelBone
    lateinit var rightLeg: PlayerModelBone
    lateinit var leftLeg: PlayerModelBone

    override fun registerBoneSuppliers() {
        boneSuppliers[Predicate { BONE_TRANSLATIONS.containsKey(it) }] =
            Function {
                PlayerModelBone(
                    it.pivot, it.name, it.rotation, it.model,
                    BONE_TRANSLATIONS[it.name()]!!, VERTICAL_OFFSETS.getOrDefault(it.name(), 0.0), headProfile, slim
                )
            }
    }

    override fun getId() = null

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

        animationHandler = FAnimationHandler(this)

        ModelManager.registerModel(this)
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
        position = pos

        arrayOf(head, body, rightArm, leftArm, rightLeg, leftLeg).forEach {
            arrayOf(it.extraOffset, it.extraRotation).forEach {
                it.run {
                    x = 0.0
                    y = 0.0
                    z = 0.0
                }
            }
        }

        globalRotation = pos.yaw.toDouble()
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
            head.extraOffset.y -= 0.35 * 4
            body.extraOffset.y -= 0.3 * 4
            body.extraRotation.x -= 0.5.toDeg()
            rightArm.extraOffset.y -= 0.3 * 4
            rightArm.extraRotation.x -= 0.4.toDeg()
            leftArm.extraOffset.y -= 0.3 * 4
            leftArm.extraRotation.x -= 0.4.toDeg()
            rightLeg.extraOffset.y -= 0.115 * 4
            rightLeg.extraOffset.z += 0.23 * 4
            leftLeg.extraOffset.y -= 0.115 * 4
            leftLeg.extraOffset.z += 0.23 * 4
        }

        prevPrevPos = prevPos
        prevPos = pos0

        super.draw()
    }

    private val limbAnimator = LimbAnimator()

    fun updateLimbs() {
        val pos = entity.position
        val prevPos = entity.previousPosition
        val f = magnitude(pos.x - prevPos.x, 0.0, pos.z - prevPos.z).toFloat() / scale
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

    override fun destroy() {
        animationHandler.destroy()
        super.destroy()
    }

    companion object {
        protected val GSON: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
        private val MODEL_JSON: JsonObject = GSON.fromJson(StringReader(SteveModel.MODEL_STRING), JsonObject::class.java)

        private val VERTICAL_OFFSETS = mapOf(
            "head" to 1.4,
            "body" to 1.4,
            "right_arm" to 1.2825,
            "left_arm" to 1.2825,
            "right_leg" to 0.7,
            "left_leg" to 0.7
        )

        private val BONE_TRANSLATIONS = mapOf(
            "head" to 0,
            "body" to -3072,
            "right_arm" to -1024,
            "left_arm" to -2048,
            "right_leg" to -4096,
            "left_leg" to -5120
        )

        val SPEED_MIN_FOR_PREDICTION_SQUARED = square(4.317 * 0.8 / 20)
    }
}

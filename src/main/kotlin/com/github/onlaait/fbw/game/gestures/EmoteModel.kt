package com.github.onlaait.fbw.game.gestures

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.PlayerSkin
import net.minestom.server.instance.Instance
import net.worldseed.multipart.GenericModelImpl
import net.worldseed.multipart.model_bones.ModelBoneImpl
import net.worldseed.multipart.model_bones.ModelBoneViewable
import java.io.StringReader
import java.util.function.Function
import java.util.function.Predicate

open class EmoteModel(private val skin: PlayerSkin, private val slim: Boolean) : GenericModelImpl() {

    override fun registerBoneSuppliers() {
        boneSuppliers[Predicate { true }] =
            Function { info ->
                ModelBoneEmote(
                    info.pivot(), info.name(), info.rotation(), info.model(),
                    BONE_TRANSLATIONS[info.name()]!!, VERTICAL_OFFSETS.getOrDefault(info.name(), 0.0), skin, slim
                )
            }
    }

    override fun getId() = null

    override fun init(instance: Instance?, position: Pos) {
        this.instance = instance
        this.position = position

        this.globalRotation = position.yaw().toDouble()

        super.loadBones(MODEL_JSON, 1f)

        for (modelBonePart in parts.values) {
            if (modelBonePart is ModelBoneViewable) viewableBones.add(modelBonePart as ModelBoneImpl)

            modelBonePart.spawn(instance, modelBonePart.calculatePosition()).join()
        }

        draw()
    }

    override fun getDiff(boneName: String) = null

    override fun setGlobalScale(scale: Float) {}

    override fun getOffset(boneName: String) = Vec.ZERO

    companion object {
        protected val GSON: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
        private val MODEL_JSON: JsonObject = GSON.fromJson(StringReader(SteveModel.MODEL_STRING), JsonObject::class.java)

        private val VERTICAL_OFFSETS = mapOf(
            "head" to 1.4,
            "right_arm" to 1.283,
            "left_arm" to 1.283,
            "body" to 1.4,
            "right_leg" to 0.7,
            "left_leg" to 0.7
        )

        private val BONE_TRANSLATIONS = mapOf(
            "head" to 0,
            "right_arm" to -1024,
            "left_arm" to -2048,
            "body" to -3072,
            "right_leg" to -4096,
            "left_leg" to -5120
        )
    }
}

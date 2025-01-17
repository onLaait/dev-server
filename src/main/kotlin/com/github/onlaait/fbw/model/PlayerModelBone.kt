package com.github.onlaait.fbw.model

import com.github.onlaait.fbw.math.Vec3d
import com.github.onlaait.fbw.math.minus
import com.github.onlaait.fbw.math.plus
import com.github.onlaait.fbw.math.times
import com.github.onlaait.fbw.utils.editMeta
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.metadata.display.ItemDisplayMeta
import net.minestom.server.item.ItemComponent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.item.component.HeadProfile
import net.worldseed.multipart.GenericModel
import net.worldseed.multipart.ModelLoader.AnimationType
import net.worldseed.multipart.Quaternion
import net.worldseed.multipart.model_bones.BoneEntity

class PlayerModelBone(
    pivot: Point,
    name: String,
    rotation: Point,
    model: GenericModel,
    translation: Int,
    private val extraDiff: Vec,
    headProfile: HeadProfile,
    private val slim: Boolean
) : HumanModelBone(pivot, name, rotation, model, 1f) {

    private val baseTranslation = translation.toDouble()

    init {
        if (this.offset != null) {
            this.stand = BoneEntity(EntityType.ITEM_DISPLAY, model, name)
            this.stand.editMeta<ItemDisplayMeta> {
                this.itemStack = ItemStack.builder(Material.PLAYER_HEAD)
                    .set(ItemComponent.PROFILE, headProfile)
                    .set(ItemComponent.CUSTOM_MODEL_DATA, customModelDataFromName(name))
                    .build()
//                this.translation = Vec(0.0, baseTranslation, 0.0)
                this.displayContext = ItemDisplayMeta.DisplayContext.THIRD_PERSON_RIGHT_HAND
                this.transformationInterpolationDuration = 1
                this.width = 2f
                this.height = 2f
                this.viewRange = 0.6f
            }
        }

        this.diff = this.pivot.add(extraDiff)
    }

    private fun customModelDataFromName(name: String): Int {
        return when (name) {
            "head" -> 1
            "body" -> 2
            "right_arm" -> if (!slim) 3 else 4
            "left_arm" -> if (!slim) 5 else 6
            "right_leg" -> 7
            "left_leg" -> 8
            else -> throw IllegalArgumentException()
        }
    }

    override val extraRotation = Vec3d()
    override val extraOffset = Vec3d()

    override fun draw() {
        if (offset == null || stand == null) return

        val q = Quaternion(calculateRotation())

        val scale = Vec.fromPoint(calculateScale() * super.scale)
        val rightRotation = floatArrayOf(q.x().toFloat(), q.y().toFloat(), q.z().toFloat(), q.w().toFloat())
        val translation = calculatePositionInternal().withY { it + baseTranslation }

        val meta = stand.entityMeta as ItemDisplayMeta
        val scaleChanged = meta.scale != scale
        val rightRotationChanged = !meta.rightRotation.contentEquals(rightRotation)
        val translationChanged = meta.translation != translation
        if (scaleChanged || rightRotationChanged || translationChanged) {
            stand.editMeta<ItemDisplayMeta> {
                this.transformationInterpolationStartDelta = 0
                if (scaleChanged) this.scale = scale
                if (rightRotationChanged) this.rightRotation = rightRotation
                if (translationChanged) this.translation = translation
            }
        }
    }

    override fun applyTransform(p: Point): Point {
        var endPos = p

        if (diff != null) {
            val a = pivot + extraOffset * PlayerModel.MAGIC_NUMBER - diff
            endPos = calculateScale(endPos, propogatedScale, a)
            endPos = calculateRotation(endPos, propogatedRotation, a)
        } else {
            val a = pivot + extraOffset * PlayerModel.MAGIC_NUMBER
            endPos = calculateScale(endPos, propogatedScale, a)
            endPos = calculateRotation(endPos, propogatedRotation, a)
        }

        for (currentAnimation in allAnimations) {
            if (currentAnimation != null && currentAnimation.isPlaying) {
                if (currentAnimation.type == AnimationType.TRANSLATION) {
                    val calculatedTransform = currentAnimation.transform * PlayerModel.MAGIC_NUMBER
                    endPos += calculatedTransform
                }
            }
        }

        parent?.let { endPos = it.applyTransform(endPos) }

        return endPos
    }

    private fun calculatePositionInternal(): Vec {
        if (offset == null) return Vec.ZERO
        var p = offset
        p += extraOffset * PlayerModel.MAGIC_NUMBER
        p += Vec(0.0, -extraDiff.y, 0.0)
        p = applyTransform(p)
        p = calculateGlobalRotation(p)
        return Pos.fromPoint(p).div(4.0).mul(scale.toDouble()).asVec()
    }
}
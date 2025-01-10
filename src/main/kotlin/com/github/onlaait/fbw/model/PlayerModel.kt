package com.github.onlaait.fbw.model

import com.github.onlaait.fbw.entity.FEntity
import com.github.onlaait.fbw.entity.UntickingEntity
import com.github.onlaait.fbw.math.*
import com.github.onlaait.fbw.utils.broadcast
import com.github.onlaait.fbw.utils.editMeta
import net.kyori.adventure.text.Component
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.PlayerSkin
import net.minestom.server.entity.metadata.display.ItemDisplayMeta
import net.minestom.server.item.ItemComponent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.item.component.HeadProfile
import kotlin.math.cos

class PlayerModel(val entity: FEntity, headProfile: HeadProfile) : Model() {

    constructor(entity: FEntity, playerSkin: PlayerSkin) : this(entity, HeadProfile(playerSkin))

    private companion object {
        const val INTERPOLATION_DURATION = 1
        val SPEED_MIN_FOR_PREDICTION_SQUARED = (4.317 / 20 * 0.8).let { it * it }
        val DEFAULT_ROTATION = floatArrayOf(0f, 0f, 0f, 1f)
        val TORSO_T9N = Vec(0.0, -3072.0, 0.0)
        val RIGHT_LEG_T9N = Vec(0.0, -4096.0, 0.0)
        val LEFT_LEG_T9N = Vec(0.0, -5120.0, 0.0)
    }

    private val headE = UntickingEntity(EntityType.ITEM_DISPLAY).apply {
        editMeta<ItemDisplayMeta> {
            posRotInterpolationDuration = INTERPOLATION_DURATION
            transformationInterpolationDuration = 1
            itemStack = ItemStack.of(Material.PLAYER_HEAD).builder()
                .set(ItemComponent.PROFILE, headProfile)
                .set(ItemComponent.ITEM_MODEL, "player_display:player/head")
                .build()
            displayContext = ItemDisplayMeta.DisplayContext.THIRD_PERSON_RIGHT_HAND
            viewRange = 0.6f
            translation = Vec(0.0, 0.0, 0.0)
            width = 1f
            height = 1f
        }
    }

    private val torsoE = UntickingEntity(EntityType.ITEM_DISPLAY).apply {
        editMeta<ItemDisplayMeta> {
            posRotInterpolationDuration = INTERPOLATION_DURATION
            transformationInterpolationDuration = 2
            itemStack = ItemStack.of(Material.PLAYER_HEAD).builder()
                .set(ItemComponent.PROFILE, headProfile)
                .set(ItemComponent.ITEM_MODEL, "player_display:player/torso")
                .build()
            displayContext = ItemDisplayMeta.DisplayContext.THIRD_PERSON_RIGHT_HAND
            viewRange = 0.6f
            translation = TORSO_T9N
            width = 1f
            height = -1f
        }
    }

    private val rightArmE = UntickingEntity(EntityType.ITEM_DISPLAY).apply {
        editMeta<ItemDisplayMeta> {
            posRotInterpolationDuration = INTERPOLATION_DURATION
            transformationInterpolationDuration = 2
            itemStack = ItemStack.of(Material.PLAYER_HEAD).builder()
                .set(ItemComponent.PROFILE, headProfile)
                .set(ItemComponent.ITEM_MODEL, "player_display:player/right_arm")
                .build()
            displayContext = ItemDisplayMeta.DisplayContext.THIRD_PERSON_RIGHT_HAND
            viewRange = 0.6f
            translation = Vec(0.0, -1024.0, 0.0)
            width = 1f
            height = -1f
        }
    }

    private val leftArmE = UntickingEntity(EntityType.ITEM_DISPLAY).apply {
        editMeta<ItemDisplayMeta> {
            posRotInterpolationDuration = INTERPOLATION_DURATION
            transformationInterpolationDuration = 2
            itemStack = ItemStack.of(Material.PLAYER_HEAD).builder()
                .set(ItemComponent.PROFILE, headProfile)
                .set(ItemComponent.ITEM_MODEL, "player_display:player/left_arm")
                .build()
            displayContext = ItemDisplayMeta.DisplayContext.THIRD_PERSON_RIGHT_HAND
            viewRange = 0.6f
            translation = Vec(0.0, -2048.0, 0.0)
            width = 1f
            height = -1f
        }
    }

    private val rightLegE = UntickingEntity(EntityType.ITEM_DISPLAY).apply {
        editMeta<ItemDisplayMeta> {
            posRotInterpolationDuration = INTERPOLATION_DURATION
            transformationInterpolationDuration = 2
            itemStack = ItemStack.of(Material.PLAYER_HEAD).builder()
                .set(ItemComponent.PROFILE, headProfile)
                .set(ItemComponent.ITEM_MODEL, "player_display:player/right_leg")
                .build()
            displayContext = ItemDisplayMeta.DisplayContext.THIRD_PERSON_RIGHT_HAND
            viewRange = 0.6f
            translation = RIGHT_LEG_T9N
            width = 1f
            height = -1f
        }
    }

    private val leftLegE = UntickingEntity(EntityType.ITEM_DISPLAY).apply {
        editMeta<ItemDisplayMeta> {
            posRotInterpolationDuration = INTERPOLATION_DURATION
            transformationInterpolationDuration = 2
            itemStack = ItemStack.of(Material.PLAYER_HEAD).builder()
                .set(ItemComponent.PROFILE, headProfile)
                .set(ItemComponent.ITEM_MODEL, "player_display:player/left_leg")
                .build()
            displayContext = ItemDisplayMeta.DisplayContext.THIRD_PERSON_RIGHT_HAND
            viewRange = 0.6f
            translation = LEFT_LEG_T9N
            width = 1f
            height = -1f
        }
    }

    private var isSpawned = false
    private var prevPrevPos = Pos.ZERO
    private var prevPos = Pos.ZERO
    private var lastDisplayedPos = Pos.ZERO
    private var lastSneaking = false
    private var posChangedBefore = false

    override fun onTick() {
        val pos0 = entity.position
        var pos = pos0
        val posChangedThisTime = pos0 != prevPos

        if (!isSpawned) {
            val instance = entity.instance
            headE.setInstance(instance, pos)
            torsoE.setInstance(instance, pos)
            rightArmE.setInstance(instance, pos)
            leftArmE.setInstance(instance, pos)
            rightLegE.setInstance(instance, pos)
            leftLegE.setInstance(instance, pos)
            isSpawned = true
        } else if (!posChangedThisTime && !posChangedBefore && !prevPos.samePoint(prevPrevPos)) {
            val v = prevPos.toVec3d() - prevPrevPos.toVec3d()
            if (v.lengthSquared() > SPEED_MIN_FOR_PREDICTION_SQUARED) {
                pos += v * 0.5
                broadcast(Component.text("predict"))
            }
        }

        val sneaking = entity.isSneaking

        if (pos != lastDisplayedPos || sneaking != lastSneaking) {
            var upperY = pos.y + 1.4
            var torsoY = upperY
            if (sneaking) {
                upperY -= 0.35
                torsoY -= 0.3
            }
            val yaw = fixYaw(pos.yaw + 180)
            val upperPos = Pos(pos.x, upperY, pos.z, yaw, 0f)
            val torsoPos = Pos(pos.x, torsoY, pos.z, yaw, 0f)
            val lowerPos = Pos(pos.x, pos.y + 0.7, pos.z, yaw, 0f)
            headE.teleport(upperPos)
            torsoE.teleport(torsoPos)
            rightArmE.teleport(upperPos)
            leftArmE.teleport(upperPos)
            rightLegE.teleport(lowerPos)
            leftLegE.teleport(lowerPos)

            if (pos.pitch != lastDisplayedPos.pitch) {
                headE.editMeta<ItemDisplayMeta> {
                    transformationInterpolationStartDelta = 0
                    leftRotation = Quatf()
                        .rotateX((-pos.pitch).toRad())
                        .normalize()
                        .toFloatArray()
                }
            }

            lastDisplayedPos = pos
        }

        if (sneaking != lastSneaking) {
            torsoE.editMeta<ItemDisplayMeta> {
                transformationInterpolationStartDelta = 0
                leftRotation =
                    if (sneaking) {
                        Quatf()
                            .rotateX(-0.5f)
                            .normalize()
                            .toFloatArray()
                    } else {
                        DEFAULT_ROTATION
                    }
            }
        }

        val g = limbFrequency
        val h = limbAmplitudeMultiplier
        val a = g * 0.6662f
        val b = cos(a + PI_F)
        val c = cos(a)
        val d = 1.4f * h
        var rightArmPitch = b * h
        var leftArmPitch = c * h
        if (sneaking) {
            rightArmPitch -= 0.4f
            leftArmPitch -= 0.4f
        }
        val rightLegPitch = c * d
        val leftLegPitch = b * d

        rightArmE.editMeta<ItemDisplayMeta> {
            transformationInterpolationStartDelta = 0
            leftRotation = Quatf()
                .rotateX(rightArmPitch)
                .normalize()
                .toFloatArray()
        }
        leftArmE.editMeta<ItemDisplayMeta> {
            transformationInterpolationStartDelta = 0
            leftRotation = Quatf()
                .rotateX(leftArmPitch)
                .normalize()
                .toFloatArray()
        }
        rightLegE.editMeta<ItemDisplayMeta> {
            transformationInterpolationStartDelta = 0
            leftRotation = Quatf()
                .rotateX(rightLegPitch)
                .normalize()
                .toFloatArray()
            if (sneaking) {
                translation = RIGHT_LEG_T9N.withY { it - 0.115 }.withZ(0.23)
            } else {
                translation = RIGHT_LEG_T9N
            }
        }
        leftLegE.editMeta<ItemDisplayMeta> {
            transformationInterpolationStartDelta = 0
            leftRotation = Quatf()
                .rotateX(leftLegPitch)
                .normalize()
                .toFloatArray()
            if (sneaking) {
                translation = LEFT_LEG_T9N.withY { it - 0.115 }.withZ(0.23)
            } else {
                translation = LEFT_LEG_T9N
            }
        }

        lastSneaking = sneaking
        if (!posChangedBefore || posChangedThisTime) {
            prevPrevPos = prevPos
            prevPos = pos0
        }
        posChangedBefore = posChangedThisTime
    }

    override fun onRemoved() {
        headE.remove()
        torsoE.remove()
        rightArmE.remove()
        leftArmE.remove()
        rightLegE.remove()
        leftLegE.remove()

    }

    private val limbAnimator = LimbAnimator()

    fun updateLimbs() {
        val pos = entity.position
        val prevPos = entity.previousPosition
        val f = magnitude(pos.x - prevPos.x, 0.0, pos.z - prevPos.z).toFloat()
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
}
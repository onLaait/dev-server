package com.github.onlaait.fbw.game

import LimbAnimator
import com.github.onlaait.fbw.entity.FEntity
import com.github.onlaait.fbw.entity.UntickingEntity
import com.github.onlaait.fbw.math.*
import com.github.onlaait.fbw.utils.editMeta
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

class PlayerModel(val entity: FEntity, playerSkin: PlayerSkin) {

    companion object {
        private const val INTERPOLATION_DURATION = 2
    }

    val headE = UntickingEntity(EntityType.ITEM_DISPLAY).apply {
        editMeta<ItemDisplayMeta> {
            posRotInterpolationDuration = INTERPOLATION_DURATION
            transformationInterpolationDuration = 1
            itemStack = ItemStack.of(Material.PLAYER_HEAD).builder()
                .set(ItemComponent.PROFILE, HeadProfile(playerSkin))
                .set(ItemComponent.ITEM_MODEL, "player_display:player/head")
                .build()
            displayContext = ItemDisplayMeta.DisplayContext.THIRD_PERSON_RIGHT_HAND
            viewRange = 0.6f
            translation = Vec(0.0, 0.0, 0.0)
            width = 1f
            height = 1f
        }
    }

    val torsoE = UntickingEntity(EntityType.ITEM_DISPLAY).apply {
        editMeta<ItemDisplayMeta> {
            posRotInterpolationDuration = INTERPOLATION_DURATION
            transformationInterpolationDuration = INTERPOLATION_DURATION
            itemStack = ItemStack.of(Material.PLAYER_HEAD).builder()
                .set(ItemComponent.PROFILE, HeadProfile(playerSkin))
                .set(ItemComponent.ITEM_MODEL, "player_display:player/torso")
                .build()
            displayContext = ItemDisplayMeta.DisplayContext.THIRD_PERSON_RIGHT_HAND
            viewRange = 0.6f
            translation = Vec(0.0, -3072.0, 0.0)
            width = 1f
            height = -1f
        }
    }

    val rightArmE = UntickingEntity(EntityType.ITEM_DISPLAY).apply {
        editMeta<ItemDisplayMeta> {
            posRotInterpolationDuration = INTERPOLATION_DURATION
            transformationInterpolationDuration = INTERPOLATION_DURATION
            itemStack = ItemStack.of(Material.PLAYER_HEAD).builder()
                .set(ItemComponent.PROFILE, HeadProfile(playerSkin))
                .set(ItemComponent.ITEM_MODEL, "player_display:player/right_arm")
                .build()
            displayContext = ItemDisplayMeta.DisplayContext.THIRD_PERSON_RIGHT_HAND
            viewRange = 0.6f
            translation = Vec(0.0, -1024.0, 0.0)
            width = 1f
            height = -1f
        }
    }

    val leftArmE = UntickingEntity(EntityType.ITEM_DISPLAY).apply {
        editMeta<ItemDisplayMeta> {
            posRotInterpolationDuration = INTERPOLATION_DURATION
            transformationInterpolationDuration = INTERPOLATION_DURATION
            itemStack = ItemStack.of(Material.PLAYER_HEAD).builder()
                .set(ItemComponent.PROFILE, HeadProfile(playerSkin))
                .set(ItemComponent.ITEM_MODEL, "player_display:player/left_arm")
                .build()
            displayContext = ItemDisplayMeta.DisplayContext.THIRD_PERSON_RIGHT_HAND
            viewRange = 0.6f
            translation = Vec(0.0, -2048.0, 0.0)
            width = 1f
            height = -1f
        }
    }

    val rightLegE = UntickingEntity(EntityType.ITEM_DISPLAY).apply {
        editMeta<ItemDisplayMeta> {
            posRotInterpolationDuration = INTERPOLATION_DURATION
            transformationInterpolationDuration = INTERPOLATION_DURATION
            itemStack = ItemStack.of(Material.PLAYER_HEAD).builder()
                .set(ItemComponent.PROFILE, HeadProfile(playerSkin))
                .set(ItemComponent.ITEM_MODEL, "player_display:player/right_leg")
                .build()
            displayContext = ItemDisplayMeta.DisplayContext.THIRD_PERSON_RIGHT_HAND
            viewRange = 0.6f
            translation = Vec(0.0, -4096.0, 0.0)
            width = 1f
            height = -1f
        }
    }

    val leftLegE = UntickingEntity(EntityType.ITEM_DISPLAY).apply {
        editMeta<ItemDisplayMeta> {
            posRotInterpolationDuration = INTERPOLATION_DURATION
            transformationInterpolationDuration = INTERPOLATION_DURATION
            itemStack = ItemStack.of(Material.PLAYER_HEAD).builder()
                .set(ItemComponent.PROFILE, HeadProfile(playerSkin))
                .set(ItemComponent.ITEM_MODEL, "player_display:player/left_leg")
                .build()
            displayContext = ItemDisplayMeta.DisplayContext.THIRD_PERSON_RIGHT_HAND
            viewRange = 0.6f
            translation = Vec(0.0, -5120.0, 0.0)
            width = 1f
            height = -1f
        }
    }

    var isSpawned = false

    fun tick() {
        val pos = entity.position
        if (!isSpawned) {
            val instance = entity.instance
            headE.setInstance(instance, pos)
            torsoE.setInstance(instance, pos)
            rightArmE.setInstance(instance, pos)
            leftArmE.setInstance(instance, pos)
            rightLegE.setInstance(instance, pos)
            leftLegE.setInstance(instance, pos)
            isSpawned = true
        }

        var upperY = pos.y + 1.40625
        if (entity.isSneaking) upperY -= 0.35
        val upperPos = Pos(pos.x, upperY, pos.z)
        val lowerPos = Pos(pos.x, pos.y + 0.703125, pos.z)
        headE.teleport(upperPos)
        torsoE.teleport(upperPos)
        rightArmE.teleport(upperPos)
        leftArmE.teleport(upperPos)
        rightLegE.teleport(lowerPos)
        leftLegE.teleport(lowerPos)
        val rotY = fixYaw(-pos.yaw + 180).toRad()
        headE.editMeta<ItemDisplayMeta> {
            transformationInterpolationStartDelta = 0
            leftRotation = Quatf()
                .rotateY(rotY)
                .rotateX(-pos.pitch.toRad())
                .normalize()
                .toFloatArray()
        }
        torsoE.editMeta<ItemDisplayMeta> {
            transformationInterpolationStartDelta = 0
            leftRotation = Quatf()
                .rotateY(rotY)
                .normalize()
                .toFloatArray()
        }

        // setAngles
        val g = limbFrequency
        val h = limbAmplitudeMultiplier

        val rightArmPitch = cos(g * 0.6662F + PI_F) * 2.0F * h * 0.5F
        val leftArmPitch = cos(g * 0.6662F) * 2.0F * h * 0.5F
        val rightLegPitch = cos(g * 0.6662F) * 1.4F * h
        val leftLegPitch = cos(g * 0.6662F + PI_F) * 1.4F * h

        rightArmE.editMeta<ItemDisplayMeta> {
            transformationInterpolationStartDelta = 0
            leftRotation = Quatf()
                .rotateY(rotY)
                .rotateX(rightArmPitch)
                .normalize()
                .toFloatArray()
        }
        leftArmE.editMeta<ItemDisplayMeta> {
            transformationInterpolationStartDelta = 0
            leftRotation = Quatf()
                .rotateY(rotY)
                .rotateX(leftArmPitch)
                .normalize()
                .toFloatArray()
        }
        rightLegE.editMeta<ItemDisplayMeta> {
            transformationInterpolationStartDelta = 0
            leftRotation = Quatf()
                .rotateY(rotY)
                .rotateX(rightLegPitch)
                .normalize()
                .toFloatArray()
        }
        leftLegE.editMeta<ItemDisplayMeta> {
            transformationInterpolationStartDelta = 0
            leftRotation = Quatf()
                .rotateY(rotY)
                .rotateX(leftLegPitch)
                .normalize()
                .toFloatArray()
        }
    }

    private val limbAnimator: LimbAnimator = LimbAnimator()

    fun updateLimbs() {
        val pos = entity.position
        val prevPos = entity.previousPosition
        val f = magnitude(pos.x - prevPos.x, 0.0, pos.z - prevPos.z).toFloat()
        updateLimbs(f)
    }

    private fun updateLimbs(posDelta: Float) {
        val f = (posDelta * 4.0F).coerceAtMost(1.0F)
        limbAnimator.updateLimbs(f, 0.4F, 1.0F)
    }

    private var limbFrequency: Float = 0f
    private var limbAmplitudeMultiplier: Float = 0f

    fun updateRenderState() {
        val tickDelta = 0f
        limbFrequency = limbAnimator.getPos(tickDelta)
        limbAmplitudeMultiplier = limbAnimator.getSpeed(tickDelta)
    }
}
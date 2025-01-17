package com.github.onlaait.fbw.model

import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.Instance
import net.worldseed.multipart.GenericModelImpl
import net.worldseed.multipart.ModelLoader
import net.worldseed.multipart.model_bones.ModelBoneImpl
import net.worldseed.multipart.model_bones.ModelBoneViewable
import net.worldseed.multipart.model_bones.misc.ModelBoneVFX
import java.util.function.Function
import java.util.function.Predicate

class EquipmentModel(val player: PlayerModel) : GenericModelImpl() {

    lateinit var animationHandler: FAnimationHandler

    override fun getId() = player.id

    override fun registerBoneSuppliers() {
        boneSuppliers[Predicate { it == "base" }] = Function { ModelBoneVFX(it.pivot, it.name, it.rotation, it.model, it.scale) }
        boneSuppliers[Predicate { it == "head" }] = Function { EquipmentModelBone(it.pivot, it.name, it.rotation, it.model, it.scale, player.head) }
        boneSuppliers[Predicate { it == "body" }] = Function { EquipmentModelBone(it.pivot, it.name, it.rotation, it.model, it.scale, player.body) }
        boneSuppliers[Predicate { it == "right_arm" }] = Function { EquipmentModelBone(it.pivot, it.name, it.rotation, it.model, it.scale, player.rightArm) }
        boneSuppliers[Predicate { it == "left_arm" }] = Function { EquipmentModelBone(it.pivot, it.name, it.rotation, it.model, it.scale, player.leftArm) }
        boneSuppliers[Predicate { it == "right_leg" }] = Function { EquipmentModelBone(it.pivot, it.name, it.rotation, it.model, it.scale, player.rightLeg) }
        boneSuppliers[Predicate { it == "left_leg" }] = Function { EquipmentModelBone(it.pivot, it.name, it.rotation, it.model, it.scale, player.leftLeg) }
        boneSuppliers[Predicate { true }] = Function { EquipmentModelBone(it.pivot, it.name, it.rotation, it.model, it.scale, null) }
    }

    override fun init(instance: Instance?, position: Pos, scale: Float) {
        this.instance = instance
        this.position = position
        val loadedModel = ModelLoader.loadModel(id)

        super.loadBones(loadedModel, scale)

        for (modelBonePart in getParts()) {
            if (modelBonePart is ModelBoneViewable) viewableBones.add(modelBonePart as ModelBoneImpl)

            modelBonePart.spawn(instance, modelBonePart.calculatePosition()).join()
        }

        animationHandler = FAnimationHandler(this)
    }

    override fun draw() {
        position = player.position
        globalRotation = player.globalRotation
        super.draw()
    }
}
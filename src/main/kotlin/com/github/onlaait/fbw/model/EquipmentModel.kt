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

class EquipmentModel(val model: PlayerModel) : GenericModelImpl() {

    override fun getId() = model.id

    lateinit var animationHandler: FAnimationHandler

    override fun registerBoneSuppliers() {
        boneSuppliers[Predicate { it == "base" }] = Function { ModelBoneVFX(it.pivot, it.name, it.rotation, it.model, it.scale) }
        boneSuppliers[Predicate { it == "head" }] = Function { EquipmentModelBone(it.pivot, it.name, it.rotation, it.model, it.scale, model.head) }
        boneSuppliers[Predicate { it == "body" }] = Function { EquipmentModelBone(it.pivot, it.name, it.rotation, it.model, it.scale, model.body) }
        boneSuppliers[Predicate { it == "right_arm" }] = Function { EquipmentModelBone(it.pivot, it.name, it.rotation, it.model, it.scale, model.rightArm) }
        boneSuppliers[Predicate { it == "left_arm" }] = Function { EquipmentModelBone(it.pivot, it.name, it.rotation, it.model, it.scale, model.leftArm) }
        boneSuppliers[Predicate { it == "right_leg" }] = Function { EquipmentModelBone(it.pivot, it.name, it.rotation, it.model, it.scale, model.rightLeg) }
        boneSuppliers[Predicate { it == "left_leg" }] = Function { EquipmentModelBone(it.pivot, it.name, it.rotation, it.model, it.scale, model.leftLeg) }
        boneSuppliers[Predicate { true }] = Function { EquipmentModelBone(it.pivot, it.name, it.rotation, it.model, it.scale, model.rightArm) }
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
        setPosition(model.position)
        globalRotation = model.globalRotation
        super.draw()
    }
}
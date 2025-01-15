package com.github.onlaait.fbw.model

import com.github.onlaait.fbw.math.Vec3d
import net.minestom.server.coordinate.Point
import net.worldseed.multipart.GenericModel
import net.worldseed.multipart.model_bones.display_entity.ModelBonePartDisplay

class EquipmentModelBone(pivot: Point, name: String, rotation: Point, model: GenericModel, scale: Float) : ModelBonePartDisplay(pivot, name, rotation, model, scale), FModelBone {

    override val extraRotation: Vec3d
        get() = TODO("Not yet implemented")
    override val extraOffset: Vec3d
        get() = TODO("Not yet implemented")

}
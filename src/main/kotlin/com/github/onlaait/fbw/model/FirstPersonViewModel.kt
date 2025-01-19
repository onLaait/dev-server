package com.github.onlaait.fbw.model

import com.github.onlaait.fbw.entity.FPlayer
import com.github.onlaait.fbw.entity.UntickingEntity
import com.github.onlaait.fbw.utils.editMeta
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.metadata.other.InteractionMeta
import net.minestom.server.instance.Instance
import net.worldseed.multipart.ModelLoader
import net.worldseed.multipart.model_bones.ModelBoneImpl
import net.worldseed.multipart.model_bones.ModelBoneViewable
import java.util.function.Function
import java.util.function.Predicate

class FirstPersonViewModel(val player: FPlayer, val modelId: String = "default") : FGenericModel() {

    private val realId = "${modelId}_fpv.bbmodel"
    override fun getId() = realId

    override fun registerBoneSuppliers() {
        boneSuppliers[Predicate { true }] = Function { FirstPersonViewModelBone(it.pivot, it.name, it.rotation, it.model, it.scale) }
    }

    override fun init(instance: Instance?, position: Pos, scale: Float) {
        val pos = position.withView(0f, 0f)

        this.instance = instance
        this.position = pos

        val loadedModel = ModelLoader.loadModel(id)

        setGlobalRotation(180.0)

        loadBones(loadedModel, 0.3f)

        for (modelBonePart in parts.values) {
            if (modelBonePart is ModelBoneViewable)
                viewableBones.add(modelBonePart as ModelBoneImpl)

            modelBonePart.spawn(instance, pos).join()
        }

        val w = UntickingEntity(EntityType.INTERACTION)
        w.isAutoViewable = false
        w.editMeta<InteractionMeta> {
            width = 0f
            height = -0.18f
        }
        w.setInstance(instance!!, pos)
        w.addViewer(player)
        addViewer(player)
        getParts().forEach {
            it.entity?.let { w.addPassenger(it) }
        }
        player.addPassenger(w)

        draw()

        ModelManager.registerModel(this)
    }
}
package com.github.onlaait.fbw.physx

import physx.PxTopLevelFunctions
import physx.common.PxDefaultAllocator
import physx.common.PxErrorCallbackImpl
import physx.common.PxErrorCodeEnum
import physx.common.PxTolerancesScale
import physx.cooking.PxCookingParams
import physx.cooking.PxMeshMidPhaseEnum
import physx.cooking.PxMidphaseDesc

object PxManager {

    val cookingParams: PxCookingParams

    init {
        PxTopLevelFunctions.CreateFoundation(
            PxTopLevelFunctions.getPHYSICS_VERSION(), PxDefaultAllocator(), MyErrorCallback()
        )
        val scale = PxTolerancesScale()
        cookingParams = PxCookingParams(scale)
        cookingParams.midphaseDesc = PxMidphaseDesc().apply {
            setToDefault(PxMeshMidPhaseEnum.eBVH34)
            val bvh34 = mbvH34Desc
            bvh34.numPrimsPerLeaf = 4
            mbvH34Desc = bvh34
        }
        cookingParams.suppressTriangleMeshRemapTable = true

        PxCylinderGeometry
    }

    private class MyErrorCallback : PxErrorCallbackImpl() {
        override fun reportError(code: PxErrorCodeEnum, message: String, file: String, line: Int) {
            PxLogging.logPhysics(code.value, message, file, line)
        }
    }
}
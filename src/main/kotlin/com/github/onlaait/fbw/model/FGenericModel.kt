package com.github.onlaait.fbw.model

import net.worldseed.multipart.GenericModelImpl

abstract class FGenericModel : GenericModelImpl() {

    var shouldTick = true

    override fun destroy() {
        ModelManager.unregisterModel(this)
        super.destroy()
    }
}
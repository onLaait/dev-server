package com.github.onlaait.fbw.model

import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.Instance
import net.worldseed.multipart.GenericModelImpl

class Minimal : GenericModelImpl() {

    override fun getId() = "test.bbmodel"

    override fun init(instance: Instance?, position: Pos) {
        init(instance, position.withY { it - 0.00625 }, 0.9375f)
    }
}
package com.github.laaitq.fbw

import com.github.laaitq.fbw.system.Logger
import net.minestom.server.MinecraftServer
import net.minestom.server.instance.InstanceManager
import net.minestom.server.instance.block.Block

object Instance {

    private val instanceManager: InstanceManager = MinecraftServer.getInstanceManager()
    val instance = instanceManager.createInstanceContainer()

    init {
        Logger.info("Preparing instances")
        instance.setGenerator { unit -> unit.modifier().fillHeight(0, 1, Block.STONE) }
        instance.timeRate = 0
        instance.timeUpdate = null
        instance.enableAutoChunkLoad(false)
        for (x in -50..50) {
            for (z in -50..50) {
                instance.loadChunk(x, z)
            }
        }
    }
}
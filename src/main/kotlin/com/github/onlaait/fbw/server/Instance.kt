package com.github.onlaait.fbw.server

import net.minestom.server.MinecraftServer
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.InstanceManager
import net.minestom.server.instance.block.Block
import net.minestom.server.utils.NamespaceID
import net.minestom.server.world.DimensionType

object Instance {

    private val instanceManager: InstanceManager = MinecraftServer.getInstanceManager()
    val instance: InstanceContainer

    init {
        Logger.info("Preparing instances")

        val dimension = DimensionType.builder(NamespaceID.from("fbw"))
            .ambientLight(1f)
            .build()
        MinecraftServer.getDimensionTypeManager()
            .addDimension(dimension)

        instance = instanceManager.createInstanceContainer(dimension).apply {
            setGenerator { unit -> unit.modifier().fillHeight(0, 1, Block.GRASS_BLOCK) }
            timeUpdate = null
            timeRate = 0
            enableAutoChunkLoad(false)
            for (x in -50..50) {
                for (z in -50..50) {
                    loadChunk(x, z)
                }
            }
        }
    }
}
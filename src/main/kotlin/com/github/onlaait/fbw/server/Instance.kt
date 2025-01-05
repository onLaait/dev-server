package com.github.onlaait.fbw.server

import net.minestom.server.MinecraftServer
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.block.Block
import net.minestom.server.world.DimensionType

object Instance {

    val instance: InstanceContainer

    init {
        Logger.info("Preparing instances")

        val dimensionType = DimensionType.builder()
            .ambientLight(1f)
            .build()
        val dimensionTypeId = MinecraftServer.getDimensionTypeRegistry()
            .register("fbw:default", dimensionType)

        instance = MinecraftServer.getInstanceManager().createInstanceContainer(dimensionTypeId).apply {
            setGenerator { unit -> unit.modifier().fillHeight(0, 1, Block.GRASS_BLOCK) }
            enableAutoChunkLoad(false)
            for (x in -50..50) {
                for (z in -50..50) {
                    loadChunk(x, z)
                }
            }
            timeSynchronizationTicks = 0
            timeRate = 0
        }
    }
}
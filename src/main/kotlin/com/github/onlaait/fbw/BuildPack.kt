package com.github.onlaait.fbw

import net.minestom.server.MinecraftServer
import net.minestom.server.item.Material
import net.worldseed.multipart.ModelEngine
import net.worldseed.resourcepack.PackBuilder
import kotlin.io.path.*
import kotlin.system.exitProcess

@OptIn(ExperimentalPathApi::class)
fun main() {
    MinecraftServer.init()
    ModelEngine.setModelMaterial(Material.EGG)
    val modelPath = Path("src/main/resources/models")
    modelPath.deleteRecursively()
    val packPath = Path("src/test/resources/resourcepack")
    packPath.deleteRecursively()
    Path("src/test/resources/resourcepack_template").copyToRecursively(
        packPath,
        { _, _, e -> throw e },
        followLinks = false,
        overwrite = true
    )
    val config = PackBuilder.Generate(Path("src/test/resources/bbmodel"), packPath, modelPath)
    Path("src/main/resources/model_mappings.json").writeText(config.modelMappings())
    exitProcess(0)
}
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
    val basePath = Path("src/test/resources")
    val modelPath = basePath.resolve("models")
    modelPath.deleteRecursively()
    val packPath = basePath.resolve("resourcepack")
    packPath.deleteRecursively()
    basePath.resolve("resourcepack_template").copyToRecursively(
        packPath,
        { _, _, e -> throw e },
        followLinks = false,
        overwrite = true
    )
    val config = PackBuilder.Generate(basePath.resolve("bbmodel"), packPath, modelPath)
    basePath.resolve("model_mappings.json").writeText(config.modelMappings())
    exitProcess(0)
}
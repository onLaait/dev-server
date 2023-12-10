package com.github.onlaait.fbw.utils

import com.github.onlaait.fbw.server.Logger
import com.github.onlaait.fbw.system.ServerProperties
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.minestom.server.MinecraftServer
import net.minestom.server.ping.ResponseData
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import javax.imageio.ImageIO

object ServerUtils {

    val responseData = ResponseData().apply {
        description = LegacyComponentSerializer.legacySection().deserialize(ServerProperties.MOTD)
        maxPlayer = ServerProperties.MAX_PLAYERS
        Logger.debug("Loading server icon")
        val serverIconPath = "server-icon.png"
        File(serverIconPath).let { file ->
            val inputStream = if (file.isFile) {
                file.inputStream()
            } else {
                ClassLoader.getSystemResourceAsStream(serverIconPath)!!
            }
            val img = ImageIO.read(inputStream).getScaledInstance(64, 64, Image.SCALE_SMOOTH)
            val bufferedImg = BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB)
            bufferedImg.graphics.run {
                drawImage(img, 0, 0, null)
                dispose()
            }
            val base64Str = ByteArrayOutputStream().use { outputStream ->
                ImageIO.write(bufferedImg, "png", outputStream)
                Base64.getEncoder().encodeToString(outputStream.toByteArray())
            }
            favicon = "data:image/png;base64,$base64Str"
        }
    }

    fun ResponseData.refreshEntries() {
        if (ServerProperties.HIDE_ONLINE_PLAYERS) return
        this.clearEntries()
        this.addEntries(MinecraftServer.getConnectionManager().onlinePlayers)
    }
}
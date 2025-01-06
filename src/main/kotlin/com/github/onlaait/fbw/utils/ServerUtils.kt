package com.github.onlaait.fbw.utils

import com.github.onlaait.fbw.server.Logger
import com.github.onlaait.fbw.system.ServerProperties
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.minestom.server.ServerFlag
import net.minestom.server.ping.ResponseData
import net.minestom.server.timer.TaskSchedule
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import javax.imageio.ImageIO

object ServerUtils {

    val responseData =
        ResponseData().apply {
            description = LegacyComponentSerializer.legacySection().deserialize(ServerProperties.MOTD)
            maxPlayer = ServerProperties.MAX_PLAYERS
            favicon = run {
                Logger.debug { "Loading server icon" }
                val serverIconPath = "server-icon.png"
                val file = File(serverIconPath)
                val inputStream =
                    if (file.isFile) {
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
                val base64Str =
                    ByteArrayOutputStream().use { outputStream ->
                        ImageIO.write(bufferedImg, "png", outputStream)
                        Base64.getEncoder().encodeToString(outputStream.toByteArray())
                    }
                "data:image/png;base64,$base64Str"
            }
        }

    fun refreshResponse() {
        responseData.run {
            online = allPlayersCount
            if (ServerProperties.HIDE_ONLINE_PLAYERS) return
            clearEntries()
            addEntries(allPlayers.takeRandom(15))
        }
    }
}

/*val CLIENT_2_SERVER_TICKS: Int = run {
    val d = Tick.CLIENT_TICKS.duration.toMillis() / Tick.SERVER_TICKS.duration.toMillis().toDouble()
    val i = d.toInt()
    if (d != i.toDouble()) throw IllegalArgumentException("Client tick not divisible by server tick")
    MinecraftServer.TICK_MS
    return@run i
}*/

inline val Double.seconds: TaskSchedule
    get() = TaskSchedule.tick((this * ServerFlag.SERVER_TICKS_PER_SECOND).toInt())
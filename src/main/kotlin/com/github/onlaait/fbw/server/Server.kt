package com.github.onlaait.fbw.server

import com.github.onlaait.fbw.command.CommandRegister
import com.github.onlaait.fbw.entity.FPlayer
import com.github.onlaait.fbw.physx.PxManager
import com.github.onlaait.fbw.system.*
import com.github.onlaait.fbw.utils.*
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.minestom.server.MinecraftServer
import net.minestom.server.extras.MojangAuth
import net.minestom.server.item.Material
import net.minestom.server.ping.ResponseData
import net.minestom.server.thread.MinestomThread
import net.worldseed.multipart.ModelEngine
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.io.IoBuilder
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.file.Paths
import java.text.DecimalFormat
import java.util.*
import javax.imageio.ImageIO
import kotlin.io.path.Path
import kotlin.io.path.inputStream
import kotlin.system.exitProcess


object Server {

    var ticks: Long = 0
        internal set

    val CLIENT_2_SERVER_TICKS: Int

    var isClientTickTime = true
        internal set

    val pingResponse: ResponseData

    init {
        val startTime = System.currentTimeMillis()
        Logger.info("Starting Minecraft server version ${MinecraftServer.VERSION_NAME}")

        System.setProperty("joml.fastmath", "true")
        System.setProperty("joml.sinLookup", "true")
        System.setProperty("minestom.tps", "40")

        System.setOut(IoBuilder.forLogger(Logger.default).setLevel(Level.INFO).buildPrintStream())
        System.setErr(IoBuilder.forLogger(Logger.default).setLevel(Level.ERROR).buildPrintStream())

        Thread.setDefaultUncaughtExceptionHandler(DefaultExceptionHandler)

        ServerProperties

        if (ServerProperties.DEBUG) {
            Configurator.setLevel("DefaultLogger", Level.DEBUG)
            Configurator.setLevel("ConsoleLogger", Level.DEBUG)
            Configurator.setLevel("FileLogger", Level.DEBUG)
        }
        val viewDistanceStr = ServerProperties.VIEW_DISTANCE.toString()
        System.setProperty("minestom.chunk-view-distance", viewDistanceStr)
        System.setProperty("minestom.entity-view-distance", viewDistanceStr)
//        VelocityProxy.enable("9gXnEEDghmNr")

        MinestomThread.setDefaultUncaughtExceptionHandler(DefaultExceptionHandler)

        val minecraftServer = MinecraftServer.init()

        MinecraftServer.getSchedulerManager().buildShutdownTask {
            Logger.info("Stopping server")

            Terminal.stop()

            val closeMessage = Component.translatable("multiplayer.disconnect.server_shutdown")
            allPlayers.forEach {
                PlayerData.store(it)
                it.kick(closeMessage)
            }

            runBlocking { CoroutineManager.mustJobs.joinAll() }

//            Thread.sleep(100)
            Logger.debug { 0 }
        }

        CLIENT_2_SERVER_TICKS = run {
            val d = 50.0 / MinecraftServer.TICK_MS
            val i = d.toInt()
            if (d != i.toDouble()) {
                throw IllegalArgumentException("Server tick is not a multiple of client tick")
            }
            i
        }
        Logger.debug { "CLIENT_2_SERVER_TICKS: $CLIENT_2_SERVER_TICKS" }

        if (ServerProperties.ONLINE_MODE) MojangAuth.init()

        MinecraftServer.setCompressionThreshold(
            ServerProperties.NETWORK_COMPRESSION_THRESHOLD.let {
                when (it) {
                    -1 -> 0 // Minestom에선 0이 비활성화
                    in 0..63 -> 64 // The Ethernet spec requires that packets less than 64 bytes become padded to 64 bytes. Thus, setting a value lower than 64 may not be beneficial.
                    else -> it
                }
            }
        )

        MinecraftServer.getConnectionManager().setPlayerProvider(::FPlayer)

        ComponentUtils
        CoroutineManager

        PlayerData
        BanSystem
        MuteSystem
        OpSystem
        Whitelist
        if (ServerProperties.ENABLE_KAKC) Kakc

        Event
        PacketListener
        Schedule
        ServerStatusMonitor
        CommandRegister

        Instance

        PxManager

        ModelEngine.setModelMaterial(Material.EGG)
        val mappingsData = ClassLoader.getSystemResourceAsStream("model_mappings.json")!!.reader()
        val modelPath = Paths.get(ClassLoader.getSystemResource("models").toURI())
        ModelEngine.loadMappings(mappingsData, modelPath)

        pingResponse =
            ResponseData().apply {
                description = LegacyComponentSerializer.legacySection().deserialize(ServerProperties.MOTD)
                maxPlayer = ServerProperties.MAX_PLAYERS
                favicon = getServerIconAsBase64()
            }

        val ip = ServerProperties.SERVER_IP
        val port = ServerProperties.SERVER_PORT
        Logger.info("Hosting Minecraft server on ${ip.ifBlank { "*" }}:$port")
        try {
            minecraftServer.start(ip.ifBlank { "0.0.0.0" }, port)
        } catch (e: Exception) {
            MinecraftServer.stopCleanly()
            exitProcess(-1)
        }

        Terminal.start()

        val bootTime = (System.currentTimeMillis() - startTime) / 1000.0
        Logger.info("Done (${DecimalFormat("#.##").format(bootTime)}s)! For help, type \"help\"")
    }

    val IS_SERVER_TICK_EQUAL_TO_CLIENT_TICK = CLIENT_2_SERVER_TICKS == 1

    fun refreshPingResponse() {
        pingResponse.run {
            online = allPlayersCount
            if (ServerProperties.HIDE_ONLINE_PLAYERS) return
            clearEntries()
            addEntries(allPlayers.takeRandom(15))
        }
    }

    private fun getServerIconAsBase64(): String {
        Logger.debug { "Loading server icon" }
        val iconPathStr = "server-icon.png"
        val iconPath = Path(iconPathStr)
        val inputStream =
            try {
                iconPath.inputStream()
            } catch (_: Exception) {
                ClassLoader.getSystemResourceAsStream(iconPathStr)!!
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
        return "data:image/png;base64,$base64Str"
    }
}
package com.github.laaitq.fbw.system

import com.github.laaitq.fbw.utils.MyCoroutines
import com.github.laaitq.fbw.utils.MyCoroutines.mustBeCompleted
import kotlinx.coroutines.launch
import java.nio.file.FileAlreadyExistsException
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.reader
import kotlin.io.path.writer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties

object ServerProperties {

    private object Info {
        const val filePath = "server.properties"
        var init = false
    }

    var MOTD: String by Property("§e테스트 §c서버")
    var MAX_PLAYERS: Int by Property(20)
    var ENABLE_STATUS: Boolean by Property(true)
    var HIDE_ONLINE_PLAYERS: Boolean by Property(false)
    var NETWORK_COMPRESSION_THRESHOLD: Int by Property(256)
    var VIEW_DISTANCE: Int by Property(16)
    var SERVER_IP: String by Property("")
    var SERVER_PORT: Int by Property(25565)
    var REQUIRE_RESOURCE_PACK: Boolean by Property(true)
    var RESOURCE_PACK: String by Property("https://example.org")
    var RESOURCE_PACK_SHA1: String by Property("SOME_SHA1")
    var DEBUG: Boolean by Property(false)
    var WHITE_LIST: Boolean by Property(false)
    var ENFORCE_WHITELIST: Boolean by Property(false)

    init {
        Logger.info("Loading properties")
        val properties = Properties()
        val path = Path(Info.filePath)
        try {
            path.createFile()
        } catch (e: FileAlreadyExistsException) {
            path.reader().use { properties.load(it) }
        }

        MOTD = properties.getProperty("motd") ?: MOTD
        MAX_PLAYERS = (properties.getProperty("max-players") ?: "").toIntOrNull() ?: MAX_PLAYERS
        ENABLE_STATUS = (properties.getProperty("enable-status") ?: "").toBooleanStrictOrNull() ?: ENABLE_STATUS
        HIDE_ONLINE_PLAYERS = (properties.getProperty("hide-online-players") ?: "").toBooleanStrictOrNull() ?: HIDE_ONLINE_PLAYERS
        NETWORK_COMPRESSION_THRESHOLD = (properties.getProperty("network-compression-threshold") ?: "").toIntOrNull() ?: NETWORK_COMPRESSION_THRESHOLD
        VIEW_DISTANCE = (properties.getProperty("view-distance") ?: "").toIntOrNull() ?: VIEW_DISTANCE
        SERVER_IP = properties.getProperty("server-ip") ?: SERVER_IP
        SERVER_PORT = (properties.getProperty("server-port") ?: "").toIntOrNull() ?: SERVER_PORT
        REQUIRE_RESOURCE_PACK = (properties.getProperty("require-resource-pack") ?: "").toBooleanStrictOrNull() ?: REQUIRE_RESOURCE_PACK
        RESOURCE_PACK = properties.getProperty("resource-pack") ?: RESOURCE_PACK
        RESOURCE_PACK_SHA1 = properties.getProperty("resource-pack-sha1") ?: RESOURCE_PACK_SHA1
        DEBUG = (properties.getProperty("debug") ?: "").toBooleanStrictOrNull() ?: DEBUG
        WHITE_LIST = (properties.getProperty("white-list") ?: "").toBooleanStrictOrNull() ?: WHITE_LIST
        ENFORCE_WHITELIST = (properties.getProperty("enforce-whitelist") ?: "").toBooleanStrictOrNull() ?: ENFORCE_WHITELIST

        Info.init = true
        write()
    }

    fun write() {
        Logger.debug("Storing properties")
        val newProperties = Properties()
        for (field in ServerProperties.javaClass.kotlin.declaredMemberProperties) {
            if (field.name == "INSTANCE") continue
            val name = field.name.lowercase().replace('_', '-')
            newProperties.setProperty(name, field.get(this).toString())
        }
        MyCoroutines.fileOutputScope.launch {
            Path(Info.filePath).writer().use { newProperties.store(it, "FantasyBattleWorld server properties") }
        }.mustBeCompleted()
    }

    private class Property<T>(initVal: T) : ReadWriteProperty<Any?, T> {
        private var value = initVal

        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return value
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            if (this.value == value) return
            this.value = value
            if (Info.init) write()
        }
    }
}
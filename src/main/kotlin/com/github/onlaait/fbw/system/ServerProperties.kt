package com.github.onlaait.fbw.system

import com.github.onlaait.fbw.server.Logger
import com.github.onlaait.fbw.utils.CoroutineManager
import com.github.onlaait.fbw.utils.CoroutineManager.mustBeCompleted
import kotlinx.coroutines.launch
import java.nio.file.FileAlreadyExistsException
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.reader
import kotlin.io.path.writer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object ServerProperties {

    private val PATH = Path("server.properties")

    private val properties = Properties()

    init {
        Logger.info("Loading properties")
        try {
            PATH.createFile()
        } catch (e: FileAlreadyExistsException) {
            PATH.reader().use { properties.load(it) }
        }
    }

    private val temp = mutableSetOf<String>()

    var MOTD: String by Property("motd", "§e테스트 §c서버")
    var MAX_PLAYERS: Int by Property("max-players", 20)
    var ENABLE_STATUS: Boolean by Property("enable-status", true)
    var HIDE_ONLINE_PLAYERS: Boolean by Property("hide-online-players", false)
    var NETWORK_COMPRESSION_THRESHOLD: Int by Property("network-compression-threshold", 256)
    var VIEW_DISTANCE: Int by Property("view-distance", 16)
    var SERVER_IP: String by Property("server-ip", "")
    var SERVER_PORT: Int by Property("server-port", 25565)
    var ONLINE_MODE: Boolean by Property("online-mode", true)
    var REQUIRE_RESOURCE_PACK: Boolean by Property("require-resource-pack", true)
    var RESOURCE_PACK: String by Property("resource-pack", "https://example.org")
    var RESOURCE_PACK_SHA1: String by Property("resource-pack-sha1", "SOME_SHA1")
    var DEBUG: Boolean by Property("debug", false)
    var WHITE_LIST: Boolean by Property("white-list", false)
    var ENFORCE_WHITELIST: Boolean by Property("enforce-whitelist", false)
    var ENABLE_KAKC: Boolean by Property("enable-kakc", false)


    init {
        val iter = properties.iterator()
        for (p in iter) {
            if (temp.contains(p.key)) continue
            iter.remove()
        }
        temp.clear()
        write()
    }

    fun write() = CoroutineManager.fileOutputScope.launch {
        Logger.debug { "Storing properties" }
        properties.store(PATH.writer(), "FantasyBattleWorld server properties")
    }.mustBeCompleted()

    private class Property<T>(val name: String, initVal: T) : ReadWriteProperty<Any?, T> {
        private var value =
            run {
                temp += name
                val v = properties.getProperty(name) ?: return@run initVal
                (when (initVal) {
                    is String -> v
                    is Int -> v.toIntOrNull()
                    is Boolean -> v.lowercase().toBooleanStrictOrNull()
                    else -> initVal
                } ?: initVal) as T
            }.also {
                properties[name] = it.toString()
            }

        override fun getValue(thisRef: Any?, property: KProperty<*>) = value

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            if (this.value == value) return
            this.value = value
            properties[name] = value.toString()
            write()
        }
    }
}
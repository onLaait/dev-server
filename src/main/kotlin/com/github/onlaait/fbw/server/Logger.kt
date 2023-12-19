package com.github.onlaait.fbw.server

import com.github.onlaait.fbw.utils.ComponentUtils.plainText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object Logger {
    val default: Logger = LogManager.getLogger("DefaultLogger")
    val console: Logger = LogManager.getLogger("ConsoleLogger")
    val file: Logger = LogManager.getLogger("FileLogger")

    private fun serialize(component: Component) = ANSIComponentSerializer.ansi().serialize(component)

    fun debug(supplier: () -> Any?) {
        val l1 = {
            val invoke = supplier.invoke()
            if (invoke is Component) serialize(invoke) else invoke
        }
        val l2 = {
            val invoke = supplier.invoke()
            if (invoke is Component) invoke.plainText() else invoke
        }
        console.debug(l1)
        file.debug(l2)
    }

    fun info(msg: String) {
        default.info(msg)
    }

    fun info(msg: Component) {
        console.info(serialize(msg))
        file.info(msg.plainText())
    }

    fun warn(msg: String) {
        default.warn(msg)
    }

    fun warn(msg: Component) {
        console.warn(serialize(msg))
        file.warn(msg.plainText())
    }

    fun error(msg: String) {
        default.error(msg)
    }

    fun error(msg: Component) {
        console.error(serialize(msg))
        file.error(msg.plainText())
    }
}
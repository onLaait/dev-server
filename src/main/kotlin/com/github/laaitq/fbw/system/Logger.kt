package com.github.laaitq.fbw.system

import com.github.laaitq.fbw.utils.ComponentUtils.plainText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer
import org.apache.logging.log4j.kotlin.Logging
import org.apache.logging.log4j.kotlin.logger

object Logger : Logging {
    private val console = logger("ConsoleLogger")
    private val file = logger("FileLogger")

    private fun serialize(component: Component) = ANSIComponentSerializer.ansi().serialize(component)

    fun debug(supplier: () -> Any?) {
        console.debug {
            val invoke = supplier.invoke()
            if (invoke is Component) serialize(invoke) else invoke
        }
        file.debug {
            val invoke = supplier.invoke()
            if (invoke is Component) invoke.plainText() else invoke
        }
    }

    fun debug(msg: Any) {
        console.debug(msg)
        file.debug(msg)
    }

    fun info(msg: String) {
        console.info(msg)
        file.info(msg)
    }

    fun info(msg: Component) {
        console.info(serialize(msg))
        file.info(msg.plainText())
    }

    fun warn(msg: String) {
        console.warn(msg)
        file.warn(msg)
    }

    fun warn(msg: Component) {
        console.warn(serialize(msg))
        file.warn(msg.plainText())
    }

    fun error(msg: String) {
        console.error(msg)
        file.error(msg)
    }

    fun error(msg: Component) {
        console.error(serialize(msg))
        file.error(msg.plainText())
    }
}
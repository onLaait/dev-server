package com.github.laaitq.fbw.system

import com.github.laaitq.fbw.utils.ComponentUtils.plainText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.slf4j.LoggerFactory

object Logger {
    private val console = ComponentLogger.logger("ConsoleLogger")
    private val file = LoggerFactory.getLogger("FileLogger")

    fun debug(msg: String) {
        if (ServerProperties.DEBUG) {
            console.debug(msg)
            file.debug(msg)
        }
    }

    fun debug(msg: Component) {
        if (ServerProperties.DEBUG) {
            console.debug(msg)
            file.debug(msg.plainText())
        }
    }

    fun info(msg: String) {
        console.info(msg)
        file.info(msg)
    }

    fun info(msg: Component) {
        console.info(msg)
        file.info(msg.plainText())
    }

    fun warn(msg: String) {
        console.warn(msg)
        file.warn(msg)
    }

    fun warn(msg: Component) {
        console.warn(msg)
        file.warn(msg.plainText())
    }

    fun error(msg: String) {
        console.error(msg)
        file.error(msg)
    }

    fun error(msg: Component) {
        console.error(msg)
        file.error(msg.plainText())
    }
}
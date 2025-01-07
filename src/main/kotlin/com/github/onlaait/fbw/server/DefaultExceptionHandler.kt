package com.github.onlaait.fbw.server

import net.minestom.server.MinecraftServer
import kotlin.system.exitProcess

object DefaultExceptionHandler : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread?, e: Throwable?) {
        Logger.error("Encountered an unexpected exception\n${e?.stackTraceToString()}")
        try {
            MinecraftServer.stopCleanly()
        } catch (_: Throwable) {
            exitProcess(1)
        }
    }
}
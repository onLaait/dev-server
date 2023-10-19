package com.github.laaitq.fbw

import com.github.laaitq.fbw.system.Logger
import net.minestom.server.MinecraftServer

object ExceptionHandler : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        Logger.error("Encountered an unexpected exception\n${e.stackTraceToString()}")
        MinecraftServer.stopCleanly()
    }
}
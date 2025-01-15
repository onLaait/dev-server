package com.github.onlaait.fbw.server

object DefaultExceptionHandler : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread?, e: Throwable?) {
        e ?: return
        Logger.error("Encountered an unexpected exception\n${e.stackTraceToString()}")
/*        try {
            MinecraftServer.stopCleanly()
        } catch (_: Throwable) {
            exitProcess(1)
        }*/
    }
}
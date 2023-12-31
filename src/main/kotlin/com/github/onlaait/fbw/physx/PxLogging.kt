package com.github.onlaait.fbw.physx

import com.github.onlaait.fbw.server.Logger

object PxLogging {

    const val LOG_MASK_NONE = 0
    const val LOG_MASK_ALL = -1

    const val DEBUG_INFO = 1
    const val DEBUG_WARNING = 2
    const val INVALID_PARAMETER = 4
    const val INVALID_OPERATION = 8
    const val OUT_OF_MEMORY = 16
    const val INTERNAL_ERROR = 32
    const val ABORT = 64
    const val PERF_WARNING = 128

    var logMask = LOG_MASK_ALL

    internal fun logPhysics(code: Int, message: String, file: String, line: Int) {
        if (code and logMask != 0) {
            val logMsg = "PhysX: [${codeToString(code)}] ${message.trim()} [$file:$line]"
            when (code) {
                DEBUG_INFO -> Logger.info(logMsg)
                DEBUG_WARNING, PERF_WARNING -> Logger.warn(logMsg)
                else -> Logger.error(logMsg)
            }
        }
    }

    private fun codeToString(code: Int): String {
        return when (code) {
            DEBUG_INFO -> "DEBUG_INFO"
            DEBUG_WARNING -> "DEBUG_WARNING"
            INVALID_PARAMETER -> "INVALID_PARAMETER"
            INVALID_OPERATION -> "INVALID_OPERATION"
            OUT_OF_MEMORY -> "OUT_OF_MEMORY"
            INTERNAL_ERROR -> "INTERNAL_ERROR"
            ABORT -> "ABORT"
            PERF_WARNING -> "PERF_WARNING"
            else -> "UNKNOWN($code)"
        }
    }
}
package com.github.onlaait.fbw.utils

import kotlinx.coroutines.*

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
object CoroutineManager {

    val mustJobs = mutableListOf<Job>()

    val CHAT_SCOPE = CoroutineScope(newSingleThreadContext("ChatDispatcher"))
    val FILE_OUT_SCOPE = CoroutineScope(newSingleThreadContext("FileOutDispatcher"))
    val SUB_SCOPE = CoroutineScope(newSingleThreadContext("SubDispatcher"))

    fun Job.mustBeCompleted(): Job {
        mustJobs += this
        return this
    }
}
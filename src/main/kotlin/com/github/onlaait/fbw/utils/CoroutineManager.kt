package com.github.onlaait.fbw.utils

import kotlinx.coroutines.*

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
object CoroutineManager {

    val mustJobs = mutableListOf<Job>()

    val chatScope = CoroutineScope(newSingleThreadContext("ChatThread"))
    val fileOutputScope = CoroutineScope(newSingleThreadContext("FileOutputThread"))
    val subScope = CoroutineScope(newSingleThreadContext("SubThread"))

    fun Job.mustBeCompleted(): Job {
        mustJobs += this
        return this
    }
}
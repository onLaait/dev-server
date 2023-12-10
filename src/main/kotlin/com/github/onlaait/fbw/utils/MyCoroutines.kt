package com.github.onlaait.fbw.utils

import kotlinx.coroutines.*

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
object MyCoroutines {

    val mustJobs = mutableListOf<Job>()

    fun Job.mustBeCompleted(): Job {
        mustJobs += this
        return this
    }

    val chatScope = CoroutineScope(newSingleThreadContext("ChatThread"))
    val fileOutputScope = CoroutineScope(newSingleThreadContext("FileOutputThread"))
    val subScope = CoroutineScope(newSingleThreadContext("SubThread"))
}
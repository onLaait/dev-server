package com.github.laaitq.fbw.utils

import kotlinx.coroutines.*

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
object MyCoroutines {

    val mustJobs = mutableListOf<Job>()

    fun Job.mustBeCompleted(): Job {
        mustJobs += this
        return this
    }

    val fileOutputScope = CoroutineScope(newSingleThreadContext("FileOutputDispatcher"))
    val subScope = CoroutineScope(newSingleThreadContext("SubDispatcher"))
}
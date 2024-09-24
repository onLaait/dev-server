package com.github.onlaait.fbw.utils

import kotlin.time.Duration

fun Duration.formattedString(): String {
    val str = mutableListOf<String>()
    toComponents { days, hours, minutes, seconds, _ ->
        if (days != 0L) {
            str += "${days}일"
        }
        if (hours != 0) {
            str += "${hours}시간"
        }
        if (minutes != 0) {
            str += "${minutes}분"
        }
        if (seconds != 0) {
            str += "${seconds}초"
        }
    }
    return str.joinToString(" ")
}

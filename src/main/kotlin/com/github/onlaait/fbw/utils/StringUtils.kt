package com.github.onlaait.fbw.utils

import java.util.*

fun String.toUUID(): UUID {
    when (length) {
        32 -> {
            return UUID.fromString(
                StringBuilder(this)
                    .insert(8, '-')
                    .insert(13, '-')
                    .insert(18, '-')
                    .insert(23, '-')
                    .toString()
            )
        }
        36 -> {
            return UUID.fromString(this)
        }
        else -> {
            throw IllegalArgumentException()
        }
    }
}

private val IPV4_RGX: Regex =
    Regex("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")

fun String.isIPv4Address(): Boolean = IPV4_RGX.matches(this)

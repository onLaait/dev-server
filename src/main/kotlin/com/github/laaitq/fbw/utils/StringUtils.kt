package com.github.laaitq.fbw.utils

import java.util.*
import java.util.regex.Pattern

object StringUtils {

    fun String.toUUID(): UUID {
        when (this.length) {
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

    private val ipv4Pattern: Pattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")

    fun isIPv4Address(str: String): Boolean = ipv4Pattern.matcher(str).matches()
}
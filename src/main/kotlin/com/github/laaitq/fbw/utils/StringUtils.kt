package com.github.laaitq.fbw.utils

import java.util.*

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
}
package com.github.onlaait.fbw.utils

import com.github.onlaait.fbw.serializer.UuidAsStringSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class UuidAndName(
    @Serializable(with = UuidAsStringSerializer::class)
    val uuid: UUID,
    val name: String
)
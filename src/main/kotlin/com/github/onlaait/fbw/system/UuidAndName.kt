package com.github.onlaait.fbw.system

import com.github.onlaait.fbw.serializer.UUIDAsStringSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class UuidAndName(
    @Serializable(with = UUIDAsStringSerializer::class)
    val uuid: UUID,
    val name: String
)
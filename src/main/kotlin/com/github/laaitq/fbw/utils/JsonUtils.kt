package com.github.laaitq.fbw.utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

object JsonUtils {
    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
        isLenient = true
        ignoreUnknownKeys = true
    }

    fun cleanJson(value: String): String = if (value == "[\n]") "[]" else value
}
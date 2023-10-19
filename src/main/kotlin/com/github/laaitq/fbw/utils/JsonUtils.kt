package com.github.laaitq.fbw.utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

object JsonUtils {
    val prettyJson = Json {
        prettyPrint = true
        @OptIn(ExperimentalSerializationApi::class)
        prettyPrintIndent = "  "
    }

    fun cleanJson(value: String): String = if (value == "[\n]") "[]" else value
}
package com.github.onlaait.fbw.model

object AnimationConfig {

    val configs: Map<String, Map<String, Map<String, Array<Config>>>> = mapOf(
        "shiroko" to mapOf(
            "default" to mapOf(
                "right_arm" to arrayOf(Config.FIXED, Config.FOLLOWING_HEAD),
                "left_arm" to arrayOf(Config.FIXED, Config.FOLLOWING_HEAD)
            )
        )
    )

    enum class Config {
        FIXED,
        FOLLOWING_HEAD
    }
}
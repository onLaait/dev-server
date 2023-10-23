package com.github.laaitq.fbw.command

import net.minestom.server.command.builder.Command

object ListCommand : Command("list") {
    init {
        val MSG = "최대 %s명 중 %s명이 접속 중입니다: %s"
        setDefaultExecutor { sender, _ ->

        }
    }
}
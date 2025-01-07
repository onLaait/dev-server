package com.github.onlaait.fbw

import com.github.onlaait.fbw.server.Server

fun main() {
    println("Starting Main")
    println("System Info: Java ${Runtime.version().feature()} (${System.getProperty("java.vm.name")} ${Runtime.version()}) Host: ${System.getProperty("os.name")} ${System.getProperty("os.version")} (${System.getProperty("os.arch")})")
    Server
}
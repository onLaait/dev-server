package com.github.onlaait.fbw.server

import net.minecrell.terminalconsole.SimpleTerminalConsole
import net.minestom.server.MinecraftServer
import net.minestom.server.listener.TabCompleteListener
import org.jline.reader.Candidate
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import kotlin.concurrent.thread


object Terminal : SimpleTerminalConsole() {

    private var running = false

    override fun start() {
        thread(name = "Terminal") {
            Logger.debug("Starting terminal")
            running = true
            super.start()
        }
    }

    fun stop() {
        running = false
    }

    override fun buildReader(builder: LineReaderBuilder): LineReader {
        return super.buildReader(builder
            .completer { _, line, candidates ->
                val commandManager = MinecraftServer.getCommandManager()
                if (line.wordIndex() == 0) {
                    val commandString = line.word().lowercase()
                    candidates.addAll(
                        commandManager.dispatcher.commands
                            .map { it.name }
                            .let {
                                if (commandString.isBlank())
                                    it
                                else
                                    it.filter { s -> s.lowercase().startsWith(commandString) }
                            }
                            .map(::Candidate)
                            .toList()
                    )
                } else {
                    TabCompleteListener.getSuggestion(commandManager.consoleSender, line.line())?.entries?.forEach {
                        candidates.add(Candidate(it.entry))
                    }
                }
            })
    }

    override fun isRunning(): Boolean = running

    override fun runCommand(command: String) {
        val commandManager = MinecraftServer.getCommandManager()
        commandManager.execute(commandManager.consoleSender, command)
    }

    override fun shutdown() {
        MinecraftServer.stopCleanly()
    }
}

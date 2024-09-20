package com.github.onlaait.fbw.command

import net.minestom.server.command.builder.Command
import net.minestom.server.network.packet.server.common.TransferPacket

object TransferCommand : Command("transfer") {
    init {
        TransferPacket("a", 25565)
    }
}
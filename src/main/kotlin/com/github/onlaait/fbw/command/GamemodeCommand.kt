package com.github.onlaait.fbw.command

import com.github.onlaait.fbw.system.OpSystem.isOp
import com.github.onlaait.fbw.utils.sendMsg
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.ArgumentCallback
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentEnum
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.command.builder.exception.ArgumentSyntaxException
import net.minestom.server.entity.Entity
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player

object GamemodeCommand : Command("gamemode", "gm") {
    init {

        //GameMode parameter
        val gamemode = ArgumentType.Enum(
            "gamemode",
            GameMode::class.java
        ).setFormat(ArgumentEnum.Format.LOWER_CASED)
        gamemode.callback = ArgumentCallback { sender: CommandSender, exception: ArgumentSyntaxException ->
            sender.sendMsg(
                Component.text("Invalid gamemode ", NamedTextColor.RED)
                    .append(Component.text(exception.input, NamedTextColor.WHITE))
                    .append(Component.text("!"))
            )
        }
        val player = ArgumentType.Entity("targets").onlyPlayers(true)

        //Upon invalid usage, print the correct usage of the command to the sender
        defaultExecutor =
            CommandExecutor { sender: CommandSender, context: CommandContext ->
                val commandName = context.commandName
                sender.sendMsg(
                    Component.text(
                        "Usage: /$commandName <gamemode> [targets]",
                        NamedTextColor.RED
                    )
                )
            }

        //Command Syntax for /gamemode <gamemode>
        addSyntax({ sender: CommandSender, context: CommandContext ->
            //Limit execution to players only
            if (sender !is Player) {
                sender.sendMsg(
                    Component.text(
                        "Please run this command in-game.",
                        NamedTextColor.RED
                    )
                )
                return@addSyntax
            }

            //Check permission, this could be replaced with hasPermission
            if (!sender.isOp) {
                sender.sendMsg(
                    Component.text(
                        "You don't have permission to use this command.",
                        NamedTextColor.RED
                    )
                )
                return@addSyntax
            }
            val mode = context.get(gamemode)

            //Set the gamemode for the sender
            executeSelf(sender, mode)
        }, gamemode)

        //Command Syntax for /gamemode <gamemode> [targets]
        addSyntax({ sender: CommandSender, context: CommandContext ->
            //Check permission for players only
            //This allows the console to use this syntax too
            if (sender is Player && !sender.isOp) {
                sender.sendMsg(
                    Component.text(
                        "You don't have permission to use this command.",
                        NamedTextColor.RED
                    )
                )
                return@addSyntax
            }
            val finder = context.get(player)
            val mode = context.get(gamemode)

            //Set the gamemode for the targets
            executeOthers(sender, mode, finder.find(sender))
        }, gamemode, player)
    }

    /**
     * Sets the gamemode for the specified entities, and
     * notifies them (and the sender) in the chat.
     */
    private fun executeOthers(sender: CommandSender, mode: GameMode, entities: List<Entity>) {
        if (entities.isEmpty()) {
            //If there are no players that could be modified, display an error message
            if (sender is Player) sender.sendMsg(
                Component.translatable(
                    "argument.entity.notfound.player",
                    NamedTextColor.RED
                )
            ) else sender.sendMsg(
                Component.text("No player was found", NamedTextColor.RED)
            )
        } else for (entity in entities) {
            if (entity is Player) {
                if (entity === sender) {
                    //If the player is the same as the sender, call
                    //executeSelf to display one message instead of two
                    executeSelf(sender, mode)
                } else {
                    entity.gameMode = mode
                    val gamemodeString = "gameMode." + mode.name.lowercase()
                    val gamemodeComponent: Component = Component.translatable(gamemodeString)
                    val playerName: Component = if (entity.displayName == null) entity.name else entity.displayName!!

                    //Send a message to the changed player and the sender
                    entity.sendMsg(
                        Component.translatable("gameMode.changed", gamemodeComponent)
                    )
                    sender.sendMsg(
                        Component.translatable(
                            "commands.gamemode.success.other",
                            playerName,
                            gamemodeComponent
                        )
                    )
                }
            }
        }
    }

    /**
     * Sets the gamemode for the executing Player, and
     * notifies them in the chat.
     */
    private fun executeSelf(sender: Player, mode: GameMode) {
        sender.gameMode = mode

        //The translation keys 'gameMode.survival', 'gameMode.creative', etc.
        //correspond to the translated game mode names.
        val gamemodeString = "gameMode." + mode.name.lowercase()
        val gamemodeComponent: Component = Component.translatable(gamemodeString)

        //Send the translated message to the player.
        sender.sendMsg(
            Component.translatable("commands.gamemode.success.self", gamemodeComponent)
        )
    }
}
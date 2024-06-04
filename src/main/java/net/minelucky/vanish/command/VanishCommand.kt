package net.minelucky.vanish.command

import cloud.commandframework.ArgumentDescription
import cloud.commandframework.DescriptiveCompletion
import cloud.commandframework.arguments.flags.CommandFlag
import cloud.commandframework.arguments.standard.IntegerArgument
import cloud.commandframework.arguments.standard.StringArgument
import net.minelucky.vanish.GoodbyeGonePoof
import net.minelucky.vanish.command.library.Command
import net.minelucky.vanish.command.library.interfaces.ISender
import net.minelucky.vanish.configuration.Message
import net.minelucky.vanish.configuration.Settings
import net.minelucky.vanish.ruom.Ruom
import net.minelucky.vanish.utils.sendMessage
import net.minelucky.vanish.utils.string.TextReplacement
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class VanishCommand(
    private val plugin: GoodbyeGonePoof
) : Command("vanish", "vanish.command.vanish",  "v") {

    init {
        val vanishCommand = builder
            .argument(
                StringArgument.builder<ISender?>("player").asOptional().withCompletionsProvider { _, _ -> Ruom.onlinePlayers.map { getVanishDescription(it) } },
                ArgumentDescription.of("The player you want to vanish/unvanish")
            )
            .flag(CommandFlag.builder("state").withAliases("s").withArgument(
                StringArgument.builder<String>("state").withCompletionsProvider { _, _ -> listOf(DescriptiveCompletion.of("off", "Turn off vanish"), DescriptiveCompletion.of("on", "Turn on vanish")) }
            ))
            .handler { context ->
                val playerName = context.getOptional<String>("player")
                val player = if (playerName.isPresent) Bukkit.getPlayerExact(playerName.get()).let {
                    if (it != null) {
                        it
                    } else {
                        context.sender.getSender().sendMessage(Message.PLAYER_NOT_FOUND)
                        return@handler
                    }
                } else context.sender.player() ?: return@handler

                val state = context.flags().getValue<String>("state")

                if (state.isPresent) {
                    when (state.get()) {
                        "on" -> {
                            plugin.vanishManager.vanish(player, callPostEvent = true)

                            if (plugin.getVanishedPlayers().containsKey(player.uniqueId.toString()))
                                player.sendMessage(Message.VANISH_USE_VANISH)

                            return@handler
                        }
                        "off" -> {
                            plugin.vanishManager.unVanish(player, callPostEvent = true)

                            if (plugin.getVanishedPlayers().containsKey(player.uniqueId.toString()))
                                player.sendMessage(Message.VANISH_USE_UNVANISH)

                            return@handler
                        }
                    }
                }

                if (plugin.getVanishedPlayers().containsKey(player.uniqueId.toString())) {
                    player.sendMessage(Message.VANISH_USE_UNVANISH)
                    plugin.vanishManager.unVanish(player, callPostEvent = true)
                } else {
                    player.sendMessage(Message.VANISH_USE_VANISH)
                    plugin.vanishManager.vanish(player, callPostEvent = true)
                }
            }
        saveCommand(vanishCommand)

        val helpCommand = builder
            .literal("help")
            .permission(getPermission("help"))
            .argument(StringArgument.optional("query", StringArgument.StringMode.GREEDY))
            .handler { context ->
                help.queryCommands("$name ${context.getOrDefault("query", "")}", context.sender)
            }
        saveCommand(helpCommand)

        val reloadLiteral = addLiteral("reload", ArgumentDescription.of("Reload plugin's configuration files"))
            .permission(getPermission("reload"))
            .handler { context ->
                Settings.load()
                context.sender.getSender().sendMessage(Message.RELOAD_USE)
            }
        saveCommand(reloadLiteral)

        val setLevel = addLiteral("setlevel", ArgumentDescription.of("Set the vanish level of specific player"))
            .permission(getPermission("setlevel"))
            .argument(StringArgument.builder<ISender?>("player").withCompletionsProvider { _, _ -> Ruom.onlinePlayers.map { getVanishDescription(it) } })
            .argument(IntegerArgument.builder<ISender?>("level").withMin(0))
            .handler { context ->
                val player = Bukkit.getPlayerExact(context.get("player")) ?: let {
                    context.sender.getSender().sendMessage(Message.PLAYER_NOT_FOUND)
                    return@handler
                }
                val level = context.get<Int>("level")

                val addAttachment = player.addAttachment(Ruom.plugin)
                addAttachment.setPermission("vanishlevel.$level", true)
                context.sender.getSender().sendMessage(Message.LEVEL_SET, TextReplacement("level", level.toString()), TextReplacement("player", player.name))
            }
        saveCommand(setLevel)

        val getLevel = addLiteral("getlevel", ArgumentDescription.of("Get the vanish level of specific player"))
            .permission(getPermission("getlevel"))
            .argument(StringArgument.builder<ISender?>("player").withCompletionsProvider { _, _ -> Ruom.onlinePlayers.map { getVanishDescription(it) } })
            .handler { context ->
                val player = Bukkit.getPlayerExact(context.get("player")) ?: let {
                    context.sender.getSender().sendMessage(Message.PLAYER_NOT_FOUND)
                    return@handler
                }

                player.sendMessage(Message.LEVEL_GET, TextReplacement("player", player.name), TextReplacement("level", plugin.vanishManager.getVanishLevel(player).toString()))
            }
        saveCommand(getLevel)

        val setStateCommand = addLiteral("setstate", ArgumentDescription.of("Set vanish state"))
            .permission(getPermission("setstate"))
            .argument(
                StringArgument.builder<ISender?>("state").withCompletionsProvider { _, _ -> listOf(DescriptiveCompletion.of("off", "Turn off vanish"), DescriptiveCompletion.of("on", "Turn on vanish")) },
                ArgumentDescription.of("The state of vanish (on/off)")
            )
            .handler { context ->
                val player = context.sender.player() ?: return@handler
                val state = context.get<String>("state")

                if (state == "off") {
                    player.sendMessage(Message.VANISH_USE_UNVANISH)
                    plugin.vanishManager.unVanish(player, callPostEvent = true)
                } else {
                    player.sendMessage(Message.VANISH_USE_VANISH)
                    plugin.vanishManager.vanish(player, callPostEvent = true)
                }
            }
        saveCommand(setStateCommand)
    }

    fun getVanishDescription(player: Player): DescriptiveCompletion {
        return DescriptiveCompletion.of(
            player.name,
            if (plugin.getVanishedPlayers().containsKey(player.uniqueId.toString()))
                "${player.name} is currently vanished"
            else
                "${player.name} is not vanished"
        )
    }
}
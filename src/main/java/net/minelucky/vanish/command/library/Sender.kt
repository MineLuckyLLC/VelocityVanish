package net.minelucky.vanish.command.library

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.minelucky.vanish.command.library.interfaces.ISender
import net.minelucky.vanish.ruom.Ruom
import net.minelucky.vanish.ruom.adventure.AdventureApi
import net.minelucky.vanish.storage.Message
import net.minelucky.vanish.storage.Settings
import net.minelucky.vanish.utils.component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

open class Sender(
    private var commandSender: CommandSender
): ISender {

    var ONLY_PLAYERS_MESSAGE = Settings.formatMessage(Message.ONLY_PLAYERS).component()

    override fun player(): Player? {
        if (commandSender is Player) return (commandSender as Player).player

        AdventureApi.get().sender(commandSender).sendMessage(ONLY_PLAYERS_MESSAGE)
        return null
    }

    override fun audience(): Audience {
        return BukkitAudiences.create(Ruom.plugin).sender(commandSender)
    }

    override fun setSender(sender: CommandSender) {
        commandSender = sender
    }

    override fun getSender(): CommandSender {
        return commandSender
    }

    override fun sentOnlyPlayersMessage(message: Component) {
        ONLY_PLAYERS_MESSAGE = message
    }

}
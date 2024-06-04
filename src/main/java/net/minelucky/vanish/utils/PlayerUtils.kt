package net.minelucky.vanish.utils

import net.minelucky.vanish.configuration.Message
import net.minelucky.vanish.configuration.Settings
import net.minelucky.vanish.utils.Utils.getSerializedMessage
import net.minelucky.vanish.utils.adventure.AdventureApi
import net.minelucky.vanish.utils.string.TextReplacement
import net.minelucky.vanish.utils.string.component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun CommandSender.sendMessage(message: Message, vararg replacements: TextReplacement) {
    val formattedMessage = Settings.formatMessage(message, *replacements)

    if (formattedMessage.isBlank())
        return

    val serializedMessage = getSerializedMessage(formattedMessage)
    AdventureApi.get().sender(this).sendMessage(serializedMessage.component())
}

fun Player.sendMessage(message: Message, vararg replacements: TextReplacement) {
    val formattedMessage = Settings.formatMessage(message, *replacements)

    if (formattedMessage.isBlank())
        return

    Settings.commandSound.let {
        if (it != null)
            this.playSound(this.location, it, 1f, 1f)
    }

    val serializedMessage = getSerializedMessage(formattedMessage)
    AdventureApi.get().sender(this).sendMessage(serializedMessage.component())
}

fun Player.sendMessageOnly(message: Message, vararg replacements: TextReplacement) {
    val serializedMessage = getSerializedMessage(Settings.formatMessage(message, *replacements))
    AdventureApi.get().sender(this).sendMessage(serializedMessage.component())
}

fun Player.sendActionbar(message: Message, vararg replacements: TextReplacement) {
    val serializedMessage = getSerializedMessage(Settings.formatMessage(message, *replacements))
    AdventureApi.get().sender(this).sendActionBar(serializedMessage.component())
}
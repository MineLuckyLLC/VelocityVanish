package net.minelucky.vanish.utils

import net.minelucky.vanish.ruom.adventure.AdventureApi
import net.minelucky.vanish.storage.Message
import net.minelucky.vanish.storage.Settings
import net.minelucky.vanish.utils.Utils.getSerializedMessage
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
    val formattedMessage = Settings.formatMessage(this, message, *replacements)

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
    val serializedMessage = getSerializedMessage(Settings.formatMessage(this, message, *replacements))
    AdventureApi.get().sender(this).sendMessage(serializedMessage.component())
}

fun Player.sendActionbar(message: Message, vararg replacements: TextReplacement) {
    val serializedMessage = getSerializedMessage(Settings.formatMessage(this, message, *replacements))
    AdventureApi.get().sender(this).sendActionBar(serializedMessage.component())
}
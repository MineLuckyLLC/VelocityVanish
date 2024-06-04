package net.minelucky.vanish.configuration

import com.cryptomorin.xseries.XSound
import net.minelucky.vanish.ruom.Ruom
import net.minelucky.vanish.utils.string.TextReplacement
import org.bukkit.Sound
import org.bukkit.configuration.file.FileConfiguration

object Settings {

    lateinit var settings: YamlConfig
    private lateinit var language: YamlConfig
    private lateinit var settingsConfig: FileConfiguration
    private lateinit var languageConfig: FileConfiguration

    private val messages = mutableMapOf<Message, String>()

    var redisURI: String? = null

    var commandSound: Sound? = null
    var vanishSound: Sound? = null
    var unVanishSound: Sound? = null

    var actionbar = true
    var seeAsSpectator = true
    var silentOpenContainer = true

    var preventPickup = true
    var preventBlockBreak = false
    var preventBlockPlace = false
    var preventInteract = false

    init {
        load()
    }

    fun load() {
        settings = YamlConfig(Ruom.plugin.dataFolder, "settings.yml")
        settingsConfig = settings.config

        redisURI = settingsConfig.getString("redis_uri") ?: "redis://localhost:6379"

        commandSound = settingsConfig.getString("sounds.command")?.let {
            runCatching { XSound.valueOf(it).parseSound() }.getOrNull()
        }
        vanishSound = settingsConfig.getString("sounds.vanish")?.let {
            runCatching { XSound.valueOf(it).parseSound() }.getOrNull()
        }
        unVanishSound = settingsConfig.getString("sounds.unvanish")?.let {
            runCatching { XSound.valueOf(it).parseSound() }.getOrNull()
        }

        actionbar = settingsConfig.getBoolean("vanish.actionbar")
        seeAsSpectator = settingsConfig.getBoolean("vanish.see_as_spectator")
        silentOpenContainer = settingsConfig.getBoolean("vanish.silent_open_container")

        preventPickup = settingsConfig.getBoolean("vanish.prevent.pickup")
        preventBlockBreak = settingsConfig.getBoolean("vanish.prevent.block_break")
        preventBlockPlace = settingsConfig.getBoolean("vanish.prevent.block_place")
        preventInteract = settingsConfig.getBoolean("vanish.prevent.interact")

        language = YamlConfig(Ruom.plugin.dataFolder, "messages.yml")
        languageConfig = language.config

        messages.apply {
            this.clear()
            for (message in Message.entries) {
                if (message == Message.EMPTY) {
                    this[message] = ""
                    continue
                }

                this[message] =
                    languageConfig.getString(message.path) ?: languageConfig.getString(Message.UNKNOWN_MESSAGE.path)
                            ?: "Cannot find message: ${message.name}"
            }
        }

        settings.saveConfig()
        settings.reloadConfig()
        language.saveConfig()
        language.reloadConfig()
    }

    fun formatMessage(message: String, vararg replacements: TextReplacement): String {
        var formattedMessage = message
            .replace("\$prefix", getMessage(Message.PREFIX))
            .replace("\$successful_prefix", getMessage(Message.SUCCESSFUL_PREFIX))
            .replace("\$warn_prefix", getMessage(Message.WARN_PREFIX))
            .replace("\$error_prefix", getMessage(Message.ERROR_PREFIX))

        for (replacement in replacements)
            formattedMessage = formattedMessage.replace("\$${replacement.from}", replacement.to)

        return formattedMessage
    }

    fun formatMessage(message: Message, vararg replacements: TextReplacement): String {
        return formatMessage(getMessage(message), *replacements)
    }

    fun formatMessage(messages: List<String>, vararg replacements: TextReplacement): List<String> {
        val messageList = mutableListOf<String>()

        for (message in messages)
            messageList.add(formatMessage(message, *replacements))

        return messageList
    }

    private fun getMessage(message: Message): String {
        return messages[message] ?: messages[Message.UNKNOWN_MESSAGE]?.replace(
            "\$error_prefix",
            messages[Message.ERROR_PREFIX] ?: ""
        ) ?: "Unknown message ($message)"
    }

    fun getConsolePrefix(): String {
        return getMessage(Message.CONSOLE_PREFIX)
    }
}
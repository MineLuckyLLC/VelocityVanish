package net.minelucky.vanish.storage

import com.cryptomorin.xseries.XSound
import me.clip.placeholderapi.PlaceholderAPI
import net.minelucky.vanish.configuration.YamlConfig
import net.minelucky.vanish.hook.DependencyManager
import net.minelucky.vanish.ruom.Ruom
import net.minelucky.vanish.ruom.adventure.AdventureApi
import net.minelucky.vanish.utils.TextReplacement
import net.minelucky.vanish.utils.component
import org.bukkit.Sound
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.nio.file.Files
import java.time.LocalDate

object Settings {

    private const val LATEST_CONFIG_VERSION = 7

    lateinit var settings: YamlConfig
    private lateinit var language: YamlConfig
    private lateinit var settingsConfig: FileConfiguration
    private lateinit var languageConfig: FileConfiguration

    private val messages = mutableMapOf<Message, String>()

    private var settingsConfigVersion = 1

    private lateinit var defaultLanguage: String

    var commandSound: Sound? = null
    var vanishSound: Sound? = null
    var unVanishSound: Sound? = null

    var actionbar = true
    var seeAsSpectator = true
    var invincible = true
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

        settingsConfigVersion = settingsConfig.getInt("config_version", 1)

        if (settingsConfigVersion < LATEST_CONFIG_VERSION) {
            val backupFileName = "settings.yml-bak-${LocalDate.now()}"
            val settingsFile = File(Ruom.plugin.dataFolder, "settings.yml")
            val backupFile = File(Ruom.plugin.dataFolder, backupFileName)

            if (backupFile.exists())
                backupFile.delete()

            Files.copy(settingsFile.toPath(), backupFile.toPath())
            settingsFile.delete()
            settings = YamlConfig(Ruom.plugin.dataFolder, "settings.yml")
            settingsConfig = settings.config
            sendBackupMessage(backupFileName)
        }

        defaultLanguage = settingsConfig.getString("default_language") ?: "en_US"

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
        invincible = settingsConfig.getBoolean("vanish.invincible")
        silentOpenContainer = settingsConfig.getBoolean("vanish.silent_open_container")

        preventPickup = settingsConfig.getBoolean("vanish.prevent.pickup")
        preventBlockBreak = settingsConfig.getBoolean("vanish.prevent.block_break")
        preventBlockPlace = settingsConfig.getBoolean("vanish.prevent.block_place")
        preventInteract = settingsConfig.getBoolean("vanish.prevent.interact")

        language = YamlConfig(
            Ruom.plugin.dataFolder,
            "languages/$defaultLanguage.yml"
        )
        languageConfig = language.config

        messages.apply {
            this.clear()
            for (message in Message.entries) {
                if (message == Message.EMPTY) {
                    this[message] = ""
                    continue
                }

                this[message] = languageConfig.getString(message.path) ?: languageConfig.getString(Message.UNKNOWN_MESSAGE.path) ?: "Cannot find message: ${message.name}"
            }
        }

        settings.saveConfig()
        settings.reloadConfig()
        language.saveConfig()
        language.reloadConfig()
    }


    fun formatMessage(player: Player, message: String, vararg replacements: TextReplacement): String {
        var formattedMessage = formatMessage(message, *replacements)

        if (DependencyManager.placeholderAPIHook.exists)
            formattedMessage = PlaceholderAPI.setPlaceholders(player, formattedMessage)

        return formattedMessage
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

    fun formatMessage(player: Player, message: Message, vararg replacements: TextReplacement): String {
        return formatMessage(player, getMessage(message), *replacements)
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

    private fun sendBackupMessage(fileName: String) {
        AdventureApi.get().console().sendMessage("<red>=============================================================".component())
        AdventureApi.get().console().sendMessage("<red>Config version updated to $LATEST_CONFIG_VERSION. Please set your preferred values again.".component())
        AdventureApi.get().console().sendMessage("<gray>Previous values are still accessible via $fileName in plugin folder.".component())
        AdventureApi.get().console().sendMessage("<red>=============================================================".component())
    }
}
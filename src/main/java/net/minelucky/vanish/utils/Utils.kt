package net.minelucky.vanish.utils

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.minelucky.vanish.GoodbyeGonePoof
import net.minelucky.vanish.configuration.Message
import net.minelucky.vanish.configuration.Settings
import net.minelucky.vanish.ruom.Ruom
import net.minelucky.vanish.utils.string.CharAnimation
import net.minelucky.vanish.utils.string.TextReplacement
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

object Utils {

    val actionbarPlayers = mutableSetOf<Player>()
    private val charAnimation = CharAnimation(CharAnimation.Style.SQUARE_BLOCK)
    var lastChar = ""

    init {
        Ruom.runSync({
            lastChar = charAnimation.get().toString()
        }, 0, 30)
    }

    fun sendVanishActionbar(player: Player) {
        if (actionbarPlayers.contains(player))
            return

        if (Settings.actionbar)
            object : BukkitRunnable() {
                override fun run() {
                    if (Bukkit.getPlayer(player.uniqueId) == null) {
                        cancel()
                        return
                    }

                    if (!GoodbyeGonePoof.instance.getVanishedPlayers().containsKey(player.uniqueId.toString()))
                        return

                    player.sendActionbar(Message.VANISH_ACTIONBAR, TextReplacement("animation", lastChar))
                }
            }.runTaskTimer(GoodbyeGonePoof.instance, 0, 20)
    }

    fun getSerializedMessage(message: String): String {
        var legacyMessage = message.replace("&", "§")
        legacyMessage = MiniMessage.miniMessage().serialize(LegacyComponentSerializer.legacySection().deserialize(legacyMessage)).replace("\\<", "<")
        return legacyMessage
    }
}
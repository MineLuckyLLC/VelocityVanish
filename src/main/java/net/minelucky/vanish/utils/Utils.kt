package net.minelucky.vanish.utils

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.minelucky.vanish.GoodbyeGonePoof
import net.minelucky.vanish.ruom.Ruom
import net.minelucky.vanish.ruom.string.CharAnimation
import net.minelucky.vanish.storage.Message
import net.minelucky.vanish.storage.Settings
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

object Utils {

    // TODO: Crate new Actionbar class

    val actionbarPlayers = mutableSetOf<Player>()
    val charAnimation = CharAnimation(CharAnimation.Style.SQUARE_BLOCK)
    var lastChar = ""

    init {
        Ruom.runSync({
            lastChar = charAnimation.get().toString()
        }, 0, 30)
    }

    fun sendVanishActionbar(player: Player) {
        if (actionbarPlayers.contains(player))
            return

        if (Settings.actionbar && player.hasPermission("velocityvanish.admin.actionbar"))
            object : BukkitRunnable() {
                override fun run() {
                    if (Bukkit.getPlayer(player.uniqueId) == null) {
                        cancel()
                        return
                    }

                    if (!GoodbyeGonePoof.instance.vanishedNames.contains(player.name))
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
package net.minelucky.vanish.listener

import net.minelucky.vanish.GoodbyeGonePoof
import net.minelucky.vanish.ruom.Ruom
import net.minelucky.vanish.utils.Utils
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener(
    private val plugin: GoodbyeGonePoof
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player

        if (plugin.vanishedNames.contains(player.name))
            event.quitMessage = null

        Utils.actionbarPlayers.remove(player)
    }
}
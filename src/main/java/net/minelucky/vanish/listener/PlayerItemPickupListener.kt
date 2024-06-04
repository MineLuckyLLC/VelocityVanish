package net.minelucky.vanish.listener

import net.minelucky.vanish.GoodbyeGonePoof
import net.minelucky.vanish.configuration.Settings
import net.minelucky.vanish.ruom.Ruom
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerPickupItemEvent

class PlayerItemPickupListener(
    private val plugin: GoodbyeGonePoof
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    private fun onPlayerItemPickup(event: PlayerPickupItemEvent) {
        if (Settings.preventPickup)
            if (plugin.getVanishedPlayers().containsKey(event.player.uniqueId.toString()))
                event.isCancelled = true
    }
}
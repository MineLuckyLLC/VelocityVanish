package net.minelucky.vanish.listener

import net.minelucky.vanish.GoodbyeGonePoof
import net.minelucky.vanish.ruom.Ruom
import net.minelucky.vanish.storage.Settings
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
            if (plugin.vanishedNames.contains(event.player.name))
                event.isCancelled = true
    }
}